# How to distribute

## Publish on Clarabridge Maven Repository

### Prerequisites
1. AWS CLI client installed
1. Have an AWS account with access to the `engage-maven-public-repo` S3 bucket
1. Android Studio 2021.1.1

### Publication

1. [Request a AWS MFA session](https://aws.amazon.com/premiumsupport/knowledge-center/authenticate-mfa-cli/)
1. Fill in obtained details from the AWS session in `gradle.properties`
```
repoName=
s3Bucket=
s3AccessKey=
s3SecretKey=
s3SessionToken=
```
1. Create a build and publish
```
cd source
./build.sh
```

~You might need to ma~


## Publish the API reference
To distribute the documentation, host the files located at `build/docs/javadoc/` at your preferred URL.
