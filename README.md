# Animal Classification using Remote tflite Model on Firebase
### Overview
This is an android app that detects and classifies animals. It uses the camera to get input data. Rather than use a local model, I have used a remote model on [Firebase](https://firebase.google.com/). The app downloads the Tensorflow Lite model when initiated, and then run inference on that.

### Training the Model
I have used the animals-10 dataset to train the model. The dataset contains around 26000 images of 10 classes: dog, cat, sheep, horse, spider, butterfly, cow, squirrel, elephant and hen. I have used AlexNet architecture for this task. The model was trained using GPU in a Google colab notebook found [here](https://colab.research.google.com/drive/1A7ygwjQto6N-btHTbAnILP7c4kHFZjKV).

### TensorFlow Lite Model
TensorFlow Lite is not designed to train a model, the model can be trained on a higher power device. Then, the pretrained model can be converted to a TensorFlow Lite format (.tflite), which has a smaller footprint than can be easily run on a mobile or other embedded devices for classification, regresion or other such tasks. The class labels (.txt) file need to be placed in the [assets](https://github.com/mrinalTheCoder/ObjectDetectionApp/tree/master/app/src/main/assets) folder of the android app. Though it is not necessary, I have also added the .tflite model in the assets folder.

### Firebase
Firebase is a mobile platform by Google, which helps develop mobile apps quickly. I have trained my model, and converted in to a `.tflite` file. This file has been uploaded to Firebase ML console, and the app runs inference on it.
