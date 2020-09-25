# ClarabridgeChat

This repo contains all the source code for the Android SDK, as well as a built version and instructions on how to upload it to to Bintray via Gradle.

The structure for the repo is as follows:

| Filename        | Description                                               |
|-----------------|-----------------------------------------------------------|
| source          | Contains the source for the SDK                           |
| build           | Contains the ready to distribute binaries                 |
| build/docs      | API reference documentation in javadoc.jar format         |
| HOWTODISTRIBUTE | Guide which explains how to publish this SDK to gradle    |
| README          | This guide                                                |

# Distribution

For instructions on how to distribute, follow the [how to distribute](./HOWTODISTRIBUTE.MD) guide.

# Development / Testing

## Using the pre-built aar

To test the pre-built aar in your own application, follow the steps below to add it as a dependency in your Android app.

1. Import `core-2.0.0.aar` and `ui-2.0.0.aar` from the `build` directory into your Android Studio project by going to `File -> New -> New Module -> Import .JAR/.AAR`.
1. In your project's `build.gradle` file add the following lines:
    ```gradle
    implementation project(':core-2.0.0')

    implementation 'com.google.firebase:firebase-messaging:18.0.0'
    implementation 'com.google.code.gson:gson:2.8.6'
    implementation 'com.squareup.okhttp3:okhttp:3.12.6'
    implementation 'com.squareup.retrofit2:retrofit:2.6.2'
    implementation 'com.squareup.retrofit2:converter-gson:2.6.2'
    implementation 'com.google.dagger:dagger:2.25.2'
    implementation 'com.android.support:support-annotations:__SUPPORT_LIBRARY_VERSION__'

    annotationProcessor 'com.google.dagger:dagger-compiler:2.25.2'

    implementation project(':ui-2.0.0')

    implementation 'com.github.bumptech.glide:glide:4.9.0'
    implementation 'com.android.support:support-v4:__SUPPORT_LIBRARY_VERSION__'
    implementation 'com.android.support:appcompat-v7:__SUPPORT_LIBRARY_VERSION__'
    implementation 'com.android.support:exifinterface:__SUPPORT_LIBRARY_VERSION__'
    implementation 'com.android.support:recyclerview-v7:__SUPPORT_LIBRARY_VERSION__'
    implementation 'com.android.support:support-media-compat:__SUPPORT_LIBRARY_VERSION__'
    implementation 'com.google.android.gms:play-services-location:16.0.0'
    implementation 'com.davemorrissey.labs:subsampling-scale-image-view:3.10.0'
    ```

You should now be able to import the libraries (`import com.clarabridge.core`, `import com.clarabridge.ui`) and start using them in your code.

## Using the source code

To test using the provided Android projects and source code, follow the steps below.

1. Open the `source` folder in Android Studio.
1. Create a new Android library by going to `File -> New -> New Module -> Android Library`.
1. In the `build.gradle` file, change the line `apply plugin: 'com.android.library'` to `apply plugin: 'com.android.application'`
1. Add the following to the new module's app level `build.gradle` file in the `dependencies` section:
    ```gradle
    implementation project(':core')
    implementation project(':ui')
    ```

You should now be able to import the libraries (`import com.clarabridge.core`, `import com.clarabridge.ui`) and start using them in your code.

## Static Analysis

There are a number of static analysis tools applied to the project to ensure coding consistency.
To run all static analysis on the project run the following command:
* `./gradlew staticAnalysis`

All of the analysis tasks are also available under the `code quality` group in the Gradle panel.

### Checkstyle - Java

The Checkstyle plugin is applied to the project for analysing Java code.
There are a few commands that can be used to invoke the checkstyle tasks.

* `./gradlew checkStyle` - Run Checkstyle for all source sets of all submodules
* `./gradlew {MODULE_NAME}:checkStyle` - Run Checkstyle for all source sets of this module
* `./gradlew {MODULE_NAME}:checkStyleMain` - Run Checkstyle for main source set of this module
* `./gradlew {MODULE_NAME}:checkStyleTest` - Run Checkstyle for test source set of this module

The Checkstyle configuration file can be found in `analysis/checkstyle/checkstyle.xml`.

### Detekt - Kotlin

The Detekt plugin is applied to the project for analysing Kotlin code.
There are a few commands that can be used to invoke the detekt tasks.

* `./gradlew detekt` - Run Detekt for all source sets of all submodules
* `./gradlew {MODULE_NAME}:detekt` - Run Detekt for all source sets of this module

The Detekt configuration file can be found in `analysis/detekt/detekt.yml`.

# Version

This is version 2.0.0 of ClarabridgeChat. It corresponds to version [8.0.0](https://github.com/smooch/smooch-android/releases/tag/8.0.0) of the Smooch Android SDK.
