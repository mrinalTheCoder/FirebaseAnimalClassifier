package com.objdetector.deepmodel;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.custom.FirebaseCustomRemoteModel;
import com.google.firebase.ml.custom.FirebaseModelDataType;
import com.google.firebase.ml.custom.FirebaseModelInputOutputOptions;
import com.google.firebase.ml.custom.FirebaseModelInputs;
import com.google.firebase.ml.custom.FirebaseModelInterpreter;
import com.google.firebase.ml.custom.FirebaseModelInterpreterOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class AnimalDetector {
    private static final String LABEL_FILENAME = "labels.txt";
    private static final int INPUT_SIZE = 75;
    private static final int NUM_BYTES_PER_CHANNEL = 4;
    private static final String LOGGING_TAG = AnimalDetector.class.getName();

    private FirebaseModelInterpreter interpreter;
    private FirebaseModelInputOutputOptions dataOptions;

    private ByteBuffer imgData;
    private int[] intValues;
    private Vector<String> labels = new Vector<String>();

    private AnimalDetector(final Context ctx, final AssetManager assetManager)
            throws IOException, FirebaseMLException {
        init(ctx, assetManager);
    }

    private void init(final Context ctx, final AssetManager assetManager)
            throws IOException, FirebaseMLException {
        imgData = ByteBuffer.allocateDirect(1 * INPUT_SIZE * INPUT_SIZE * 1 * NUM_BYTES_PER_CHANNEL);
        imgData.order(ByteOrder.nativeOrder());
        intValues = new int[INPUT_SIZE * INPUT_SIZE];

        InputStream labelsInput = assetManager.open(LABEL_FILENAME);
        BufferedReader br = new BufferedReader(new InputStreamReader(labelsInput));
        String line;
        while ((line = br.readLine()) != null) {
            labels.add(line);
        }
        br.close();

        String remoteModelName = "Animal-Detector";
        final FirebaseCustomRemoteModel remoteModel =
                new FirebaseCustomRemoteModel.Builder(remoteModelName).build();
        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().build();

        FirebaseModelManager.getInstance().download(remoteModel, conditions)
                .addOnFailureListener(e -> {
                    Log.i(LOGGING_TAG, "Failed to download the remote model " + e.getMessage());
                    e.printStackTrace();
                })
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.i(LOGGING_TAG, "Model downloaded successfully");
                        FirebaseModelInterpreterOptions interpreterOptions =
                                new FirebaseModelInterpreterOptions.Builder(
                                        new FirebaseCustomRemoteModel.Builder(remoteModelName).build())
                                        .build();
                        try {
                            interpreter = FirebaseModelInterpreter.getInstance(interpreterOptions);
                            Log.i(LOGGING_TAG, "Interpreter initialized successfully");
                        } catch (FirebaseMLException ex) {
                            ex.printStackTrace();
                            Log.e(LOGGING_TAG, "Failed to initialize the Interpreter " + ex.getMessage());
                        }
                    }
                });

        int[] inputDims = {1, INPUT_SIZE, INPUT_SIZE, 1};
        int[] outputDims = {1, 10};

        int dataType = FirebaseModelDataType.FLOAT32;
        dataOptions =
                new FirebaseModelInputOutputOptions.Builder()
                        .setInputFormat(0, dataType, inputDims)
                        .setOutputFormat(0, dataType, outputDims)
                        .build();
    }

    public static AnimalDetector create(final Context ctx, final AssetManager assetManager)
            throws IOException, FirebaseMLException {
        return new AnimalDetector(ctx, assetManager);
    }

    public void close() {
        interpreter.close();
    }

    public Task<List<String>> detectObjects(final Bitmap bitmap) throws FirebaseMLException {
        while(interpreter == null) {
            Log.i(LOGGING_TAG, "Interpreter not yet initialized");
        }

        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        imgData.rewind();
        for (int i = 0; i < INPUT_SIZE; ++i) {
            for (int j = 0; j < INPUT_SIZE; ++j) {
                int pixelValue = intValues[i * INPUT_SIZE + j];
                float red = ((pixelValue >> 16) & 0xFF);
                float blue = ((pixelValue >> 8) & 0xFF);
                float green = (pixelValue & 0xFF);
                float normalizedPixelValue = (red + green + blue) / 3.0f;
                imgData.putFloat(normalizedPixelValue);
            }
        }

        FirebaseModelInputs inputs = new FirebaseModelInputs.Builder().add(imgData).build();
        return interpreter
                .run(inputs, dataOptions)
                .addOnFailureListener(e -> {
                    Log.e(LOGGING_TAG, "Failed to classify the image: " + e.getMessage());
                    e.printStackTrace();
                })
                .continueWith(
                        task -> {
                            float[][] labelProbArray =
                                    task.getResult().<float[][]>getOutput(0);
                            return getTopLabel(labelProbArray);
                        });
    }

    private synchronized List<String> getTopLabel(float[][] labelProbArray) {
        float max = 0;
        int labelIndex = 0;
        for (int i = 0; i < labels.size(); ++i) {
            if(labelProbArray[0][i] > max) {
                max = labelProbArray[0][i];
                labelIndex = i;
            }
        }
        List topLabels = new ArrayList();
        topLabels.add(labelIndex + ":" + labels.get(labelIndex) + ":" + max);
        return topLabels;
    }
}
