GalleryApp
==========

**Developed by:[Prashanth Ramakrishnan](prashanth_r03@yahoo.co.in)**


**Features**
- Gallery application which shows images in the SD card under "/GalleryApp"  folder
- Uses Firebase to store images, create Firebase project with the same application id and add the google-services.json file in the project to run the app

**NOTE**
Please set up a Firebase project with the application id as com.prashanth.galleryapp, download the google-service.json file. Set the Firebase storage rule to
allow read/write access and publish the rule (Follow the Firebase tutorial to achieve this).

**Overview**

The app comes preloaded with an image of my favourite Hefeweizen beer. When you open the app for the first time, this image is coped to the folder in the SD
card under /GalleryApp folder. This folder is root of all the images, any further images taken or edited will be stored under this folder.
The option to upload the image is given in the third screen where the image is viewed. There is an overflow menu to crop/rotate or to upload the photo.
Tapping the upload menu item will upload the image to Firebase storage which needs to configured in-order to run this project. Once the image is uploaded, the
image url is persisted in the application. The images are stored on Firebase under the directory "/images".

When the application detects at the start-up that the image is removed or deleted (note that removing or deleting is
done outside the scope of this application) the application downloads the images from the Firebase storage with the stored image url.

Note: For some reason, when a new file is added to the folder; even though the media scan broadcast is fired, it fails to detect the new image added which means
the application needs to be put to the background and got up again. I'm figuring out why this is happening, suspicion is that the async task which fetches the
images through a query is may be running in a different thread. This will go in as my first bug into this project.

![Demo](demo/galleryapp.gif)

**TODO**
- Instrumentation and unit testing

**Open source libaries used**
- **[Glide](https://github.com/bumptech/glide)**
- **[Timber](https://github.com/JakeWharton/timber)**
- **[ButterKnife](https://github.com/JakeWharton/butterknife)**
- **[Zoomage](https://github.com/jsibbold/zoomage)**
- **[Android image cropper](https://github.com/ArthurHub/Android-Image-Cropper)**

### License

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.