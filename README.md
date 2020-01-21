# Animal Classification Android App using Firebase
### Overview
This is an android app that detects and classifies animals. It uses the camera to get input data. I uploaded the TensorFlow Lite model to [Firebase](https://firebase.google.com/). The app downloads the model when initiated, and then runs inference on that.

### Training the Model
I have used the [animals-10](https://www.kaggle.com/alessiocorrado99/animals10) dataset to train the model. The dataset contains around 26000 images of 10 classes: dog, cat, sheep, horse, spider, butterfly, cow, squirrel, elephant and hen. I have used AlexNet architecture for this task. The model was trained using GPU in a Google colab notebook found [here](https://colab.research.google.com/drive/1A7ygwjQto6N-btHTbAnILP7c4kHFZjKV).

### TensorFlow Lite Model
TensorFlow Lite is not designed to train a model, the model can be trained on a higher power device. Then, the pretrained model can be converted to a TensorFlow Lite format (.tflite), which has a smaller footprint than can be easily run on a mobile or other embedded devices for classification, regresion or other such tasks. I converted the .h5 model obtained after the training to a .tflite model using this command: <br/><br/> 

```tflite_convert --output_file=animal_model.tflite --keras_model_file=animal_model.h5```

<br/><br/> Though it is not necessary, just for your reference I have also added the .tflite model in the assets folder. The class labels (.txt) file should be placed in the [assets](https://github.com/mrinalTheCoder/ObjectDetectionApp/tree/master/app/src/main/assets) folder of the android app. 

### Firebase
Firebase is a mobile platform by Google, which helps develop mobile apps quickly. I have trained my model, and converted in to a `.tflite` file. This file has been uploaded to Firebase ML console, and the app runs inference on it.
