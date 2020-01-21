package com.objdetector;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.Image;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.util.Log;
import android.util.Size;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.FirebaseMLException;
import com.objdetector.deepmodel.AnimalDetector;
import com.objdetector.utils.ImageUtils;

import java.util.List;

public class MainActivity extends CameraActivity implements OnImageAvailableListener {
    private static int MODEL_IMAGE_INPUT_SIZE = 75;
    private static String LOGGING_TAG = MainActivity.class.getName();

    private Integer sensorOrientation;
    private int previewWidth = 0;
    private int previewHeight = 0;
    private AnimalDetector animalDetector;
    private Bitmap imageBitmapForModel = null;
    private Bitmap rgbBitmapForCameraImage = null;
    private boolean computing = false;
    private Matrix imageTransformMatrix;


    @Override
    public void onPreviewSizeChosen(final Size previewSize, final int rotation) {

        try {
            animalDetector = AnimalDetector.create(getApplicationContext(), getAssets());
            Log.i(LOGGING_TAG, "Model Initiated successfully.");
            Toast.makeText(getApplicationContext(), "AnimalDetector created", Toast.LENGTH_SHORT).show();
        } catch(Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "AnimalDetector could not be created", Toast.LENGTH_SHORT).show();
            finish();
        }

        final int screenOrientation = getWindowManager().getDefaultDisplay().getRotation();
        //Sensor orientation: 90, Screen orientation: 0
        sensorOrientation = rotation + screenOrientation;
        Log.i(LOGGING_TAG, String.format("Camera rotation: %d, Screen orientation: %d, Sensor orientation: %d",
                rotation, screenOrientation, sensorOrientation));

        previewWidth = previewSize.getWidth();
        previewHeight = previewSize.getHeight();
        Log.i(LOGGING_TAG, "preview width: " + previewWidth);
        Log.i(LOGGING_TAG, "preview height: " + previewHeight);
        // create empty bitmap
        imageBitmapForModel = Bitmap.createBitmap(MODEL_IMAGE_INPUT_SIZE, MODEL_IMAGE_INPUT_SIZE, Config.ARGB_8888);
        rgbBitmapForCameraImage = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);

        imageTransformMatrix = ImageUtils.getTransformationMatrix(previewWidth, previewHeight,
                MODEL_IMAGE_INPUT_SIZE, MODEL_IMAGE_INPUT_SIZE, sensorOrientation,true);
        imageTransformMatrix.invert(new Matrix());
    }

    @Override
    public void onImageAvailable(final ImageReader reader) {
        Image imageFromCamera = null;

        try {
            imageFromCamera = reader.acquireLatestImage();
            if (imageFromCamera == null) {
                return;
            }
            if (computing) {
                imageFromCamera.close();
                return;
            }
            computing = true;
            preprocessImageForModel(imageFromCamera);
            imageFromCamera.close();
        } catch (final Exception ex) {
            if (imageFromCamera != null) {
                imageFromCamera.close();
            }
            Log.e(LOGGING_TAG, ex.getMessage());
        }

        if (animalDetector == null) {
            Log.e(LOGGING_TAG, "AnimalDetector not yet initialized");
        } else {
            try {
                animalDetector.detectObjects(imageBitmapForModel)
                        .addOnSuccessListener(
                                this,
                                new OnSuccessListener<List<String>>() {
                                    @Override
                                    public void onSuccess(List<String> results) {
                                        Toast.makeText(MainActivity.this, results.get(0), Toast.LENGTH_LONG).show();
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(LOGGING_TAG, "Custom classifier failed: " + e);
                                        e.printStackTrace();
                                    }
                                });
                computing = false;
            } catch (FirebaseMLException ex) {
                ex.printStackTrace();
                Log.e(LOGGING_TAG, ex.getMessage());
            }
        }

    }

    private void preprocessImageForModel(final Image imageFromCamera) {
        rgbBitmapForCameraImage.setPixels(ImageUtils.convertYUVToARGB(imageFromCamera, previewWidth, previewHeight),
                0, previewWidth, 0, 0, previewWidth, previewHeight);

        new Canvas(imageBitmapForModel).drawBitmap(rgbBitmapForCameraImage, imageTransformMatrix, null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (animalDetector != null) {
            animalDetector.close();
        }
    }
}
