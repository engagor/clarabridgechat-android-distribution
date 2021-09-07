# ClarabridgeChat

This repo contains all the source code for the Android SDK, as well as a built version.

The structure for the repo is as follows:

| Filename        | Description                                               |
|-----------------|-----------------------------------------------------------|
| source          | Contains the source for the SDK                           |
| build           | Contains the ready to distribute binaries                 |
| build/docs      | API reference documentation in javadoc.jar format         |
| README          | This guide                                                |

# Development

## Using the published aar

1. Add the Clarabridge repository to your project's `build.gradle` file
    ```gradle
        maven {
            url 'https://s3.amazonaws.com/engage-maven-public-repo'
        }
    ```
2. Add the dependencies to your project's dependencies inside `build.gradle` for your application.
    ```gradle
    implementation 'com.clarabridge:core:2.+'

    implementation 'com.google.firebase:firebase-messaging:18.0.0'
    implementation 'com.google.code.gson:gson:2.8.6'
    implementation 'com.squareup.okhttp3:okhttp:3.12.6'
    implementation 'com.squareup.retrofit2:retrofit:2.6.2'
    implementation 'com.squareup.retrofit2:converter-gson:2.6.2'
    implementation 'com.google.dagger:dagger:2.25.2'
    implementation 'com.android.support:support-annotations:__SUPPORT_LIBRARY_VERSION__'

    annotationProcessor 'com.google.dagger:dagger-compiler:2.25.2'

    implementation 'com.clarabridge:ui:2.+'

    implementation 'com.github.bumptech.glide:glide:4.9.0'
    implementation 'com.android.support:support-v4:__SUPPORT_LIBRARY_VERSION__'
    implementation 'com.android.support:appcompat-v7:__SUPPORT_LIBRARY_VERSION__'
    implementation 'com.android.support:exifinterface:__SUPPORT_LIBRARY_VERSION__'
    implementation 'com.android.support:recyclerview-v7:__SUPPORT_LIBRARY_VERSION__'
    implementation 'com.android.support:support-media-compat:__SUPPORT_LIBRARY_VERSION__'
    implementation 'com.google.android.gms:play-services-location:16.0.0'
    implementation 'com.davemorrissey.labs:subsampling-scale-image-view:3.10.0'
    ```


## Using the pre-built aar

Alternatively use the pre-built aar in your own application, follow the steps below to add it as a dependency in your Android app.

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

# Getting Started

1. Find your integration id in the Clarabridge Engage application.
1. Initialize the Chat in your application as follows:
```gradle
        ClarabridgeChat.init(this, new Settings("your integration id"), new ClarabridgeChatCallback<InitializationStatus>() {
            @Override
            public void run(Response response) {
                // Handle init response here!
            }
        });
```

# Version

This is version 2.0.0 of ClarabridgeChat. It corresponds to version [8.0.0](https://github.com/smooch/smooch-android/releases/tag/8.0.0) of the Smooch Android SDK.
