# Animal Classification Android App using Firebase
### Overview
This is an android app that detects and classifies animals. It uses the camera to get input data. I uploaded the TensorFlow Lite model to [Firebase](https://firebase.google.com/). The app downloads the model when initiated, and then runs inference on that.

### Training the Model
I have used the [animals-10](https://www.kaggle.com/alessiocorrado99/animals10) dataset to train the model. The dataset contains around 26000 images of 10 classes: dog, cat, sheep, horse, spider, butterfly, cow, squirrel, elephant and hen. I have used AlexNet architecture for this task. The model was trained using GPU in a Google colab notebook found [here](https://colab.research.google.com/drive/1A7ygwjQto6N-btHTbAnILP7c4kHFZjKV).
<br/>
You can also use Firebase Auto ML console to direclty upload your dataset if the number of images are less than 1000. this can be used to train the model directly on Firebase console without writing any single line of code.

### TensorFlow Lite Model
TensorFlow Lite is not designed to train a model, the model can be trained on a higher power device. Then, the pretrained model can be converted to a TensorFlow Lite format (.tflite), which has a smaller footprint than can be easily run on a mobile or other embedded devices for classification, regresion or other such tasks. I converted the .h5 model obtained after the training to a .tflite model using this command: <br/><br/> 
```tflite_convert --output_file=animal_model.tflite --keras_model_file=animal_model.h5```
<br/><br/> Though it is not necessary, just for your reference I have also added the .tflite model in the assets folder. The class labels (.txt) file should be placed in the [assets](https://github.com/mrinalTheCoder/ObjectDetectionApp/tree/master/app/src/main/assets) folder of the android app. 

### Firebase
Firebase is a mobile platform by Google, which helps develop mobile apps quickly. [This](https://firebase.google.com/docs/ml-kit/android/use-custom-models) is an excellent tutorial to follow.
* First upload your model to [Firebase ML console](https://console.firebase.google.com/project/_/ml/apis), you will give a name to the model here, example "Animal-Detector"
* In the Android App, create a ```FirebaseCustomRemoteModel``` object and download it using the folloiwng code:<br/><br/> 
```
FirebaseCustomRemoteModel remoteModel = new FirebaseCustomRemoteModel.Builder("your_model").build();
FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
        .requireWifi()
        .build();
FirebaseModelManager.getInstance().download(remoteModel, conditions)
        .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // Success.
            }
        });
```
* Next, we need to instantiate the Firebase Interpreter as follows:
```
FirebaseModelInterpreter interpreter;
try {
    FirebaseModelInterpreterOptions options =
            new FirebaseModelInterpreterOptions.Builder(localModel).build();
    interpreter = FirebaseModelInterpreter.getInstance(options);
} catch (FirebaseMLException e) {
    // ...
}
```
* Finally we are ready to draw inferences using Firebase, we need set the input and output and then run the prediction on the model as:
```
# Create the input and output options
FirebaseModelInputOutputOptions inputOutputOptions =
        new FirebaseModelInputOutputOptions.Builder()
                .setInputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 75, 57, 3})
                .setOutputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 10})
                .build();
                
# Prepare the input data  
ByteBuffer imgData = ...
FirebaseModelInputs inputs = new FirebaseModelInputs.Builder().add(imgData).build();

# Run the model
firebaseInterpreter.run(inputs, inputOutputOptions)
        .addOnSuccessListener(
                new OnSuccessListener<FirebaseModelOutputs>() {
                    @Override
                    public void onSuccess(FirebaseModelOutputs result) {
                        // ...
                    }
                })
        .addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Task failed with an exception
                        // ...
                    }
                });                

```
