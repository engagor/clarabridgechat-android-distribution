# How to distribute

This guide provides instructions on how to publish ClarabridgeChat as a library to Bintray as well as the associated API reference documentation

## Publish on Gradle

### Prerequisites
1. Have [Node 8 and npm](http://blog.npmjs.org/post/85484771375/how-to-install-npm) installed
1. Create a [Bintray](https://bintray.com) account and organization with at least a [pro edition](https://bintray.com/account/pricing) plan (required for closed source repositories)
1. Create a public Maven repository in your Bintray org
    - Go to `https://bintray.com/yourorg` and click on "Add New Repository"
1. Run `npm install` in the `build` folder

### Publication
1. At the root of the repo, create a `bintray.properties` file based on the [provided example](./bintray.properties.example) and fill in these fields :
    - BINTRAY_USER: The username of the account whose API key will be used to publish the build
    - BINTRAY\_API\_KEY: The bintray API key (go to your profile's [edit page](https://bintray.com/profile/edit) and click on "API Key")
    - BINTRAY\_ORG: The organization name in Bintray
    - BINTRAY\_MAVEN_REPO: The maven repository name in bintray (created in prerequisites)
    - BINTRAY\_PACKAGE: The package name in the maven repository
    - BINTRAY\_DESCRIPTION: A description for the SDK
    - BINTRAY\_WEBSITE: A website to point SDK users to

1. Upload to Bintray via the `bintrayUpload:build` script
    ```bash
    npm run bintrayUpload:build
    ```
1. Go to `https://bintray.com/yourorg/yourmavenrepo/yourpackage` and click on Add to JCenter (This step only needs to be done once)
    Once your package is linked to JCenter, it can be downloaded easily via gradle:
    ```
    compile 'com.clarabridge:core:latest.release'
    compile 'com.clarabridge:ui:latest.release'
    ```

## Publish the API reference
To distribute the documentation, host the files located at `build/docs/javadoc/` at your preferred URL.

## Publish to S3 based maven repo
    gradlew build publish -Ps3AccessKey=XXX -Ps3SecretKey=XXX

