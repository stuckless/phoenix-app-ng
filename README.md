# SageTV App for Android TV

This application is more of a proof of concept in terms of code quality.   It currently will synchronize with your SageTV setup and show your SageTV content in AndroidTV.   Nothing is configurable.

The application is building using Android Studio and Gradle.   If you want to work with this application, then use Android Studio, and import the build.gradle from the project root.  After several minutes, you should have a working Android Studio project with 4 modules.

## 4 Modules
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

