# phoenix-app-ng
SageTV Application for AndroidTV and Mobile

This application is more of a proof of concept in terms of code quality.   It currently will syncronize with your SageTV setup and show your SageTV content in AndroidTV.   Nothing is configurable.

The application is building using Android Studio and Gradle.   If you want to work with this application, then use Android Studio, and import the build.gradle from the project root.  After several minutes, you should have a working Android Studio project with 4 modules.

The 4 modules are
shared
- pure java code, can be shared with any pure java application (ie, no Android dependencies)

phoenixshared
- shared Android code between the mobile and tv projects

tv
- the Android TV project with Android TV

mobile
- currently nothing, but where mobile (ie, tablet/phone) screen and code would go

