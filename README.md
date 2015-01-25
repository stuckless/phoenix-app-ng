# SageTV App for Android TV

<img src="https://raw.githubusercontent.com/stuckless/phoenix-app-ng/master/assets/screenshot_640.jpg"/>

This application is more of a proof of concept in terms of code quality.   It currently will synchronize with your SageTV setup and show your SageTV content in AndroidTV.   Nothing is configurable.

The application is building using Android Studio and Gradle.   If you want to work with this application, then use Android Studio, and import the build.gradle from the project root.  After several minutes, you should have a working Android Studio project with 4 modules.

## 6 Modules
### shared
* pure java code, can be shared with any pure java application (ie, no Android dependencies)
* contains the database code and libraries for talking to SageTV

### phoenixshared
* shared Android code between the mobile and tv projects
* contains the AppInstance and Sync Service code which is used to sync the database

### tv
* the Android TV project with Android TV
* everything tv specific is here

### mobile
* currently nothing, but where mobile (ie, tablet/phone) screen and code would go

### ijkmediaplayer
### ijkmediawidget
* the Player and Widget modules are copied from the ijkmediaplayer project to support an internal video player based on ffmpeg.
* x86 and armv7 support libraries are checked in, so there is no need to rebuild the jni libraries (if you do, then see the ijkmediaplayer project for details on rebuilding the native components)
 
## Built-in Media Player
* As stated the built-in media player is based on ijkmediaplaer.  The playback controls are as follows...
* DPAD_UP - show progress bar
* DPAD_DOWN - hide progress bar
* DPAD_RIGHT - skips ahead 30 seconds
* DPAD_LEFT - skips back 30 seconds
* A - Play/Pause media
