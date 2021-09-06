# How to distribute

## Publish on Clarabridge Maven Repository

### Prerequisites

1. [AWS CLI client installed](https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-install.html)
1. Have an AWS account with access to the `engage-maven-public-repo` S3 bucket
1. Android Studio 2021.1.1

### Publication

1. [Request a AWS MFA (Multi-Factor Authentication) session](https://aws.amazon.com/premiumsupport/knowledge-center/authenticate-mfa-cli/)
2. Fill in obtained details from the AWS session in `gradle.properties`
```
repoName=engage-maven-public-repo
s3Bucket=s3://engage-maven-public-repo.s3.amazonaws.com
s3AccessKey=
s3SecretKey=
s3SessionToken=
``
3. Create a build, and publish the artifacts to the repository
```
cd source
chmod +x build.sh
./build.sh
```
4. Verify the artifacts are published
```
aws ls s3://engage-maven-public-repo
```

## Publish the API reference

To distribute the documentation, host the files located at `build/docs/javadoc/` at your preferred URL.

## Updating the Public Repository

- Update the public branch
```
git checkout public
git merge origin master
```
- Update the pubic repository
```
git clone --single-branch --branch branch_name github_repo_url
```
