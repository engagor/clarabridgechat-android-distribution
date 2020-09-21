#!/usr/bin/env node

'use strict';
const fs = require('fs');
const path = require('path');
const Bintray = require('bintray');
const readline = require('readline');

const argv = require('yargs')
  .boolean('whitelabel')
  .string('buildVersion')
  .argv

const projectFolder = path.join(__dirname, '..');

const isWhitelabel = argv.whitelabel;
const version = isWhitelabel ? '1.0.0' : argv.buildVersion;

(async function () {
  const bintrayProps = {};

  if (fs.existsSync('bintray.properties')) {
    const rd = readline.createInterface({
      input: fs.createReadStream('bintray.properties'),
      console: false
    });

    await new Promise((resolve, reject) => {
      rd.on('line', function(line, last) {
        const [key, value] = line.split('=');
        bintrayProps[key] = value;
      });

      rd.on('close', resolve);
    });
  } else {
    Object.assign(bintrayProps, {
      BINTRAY_USER: process.env.BINTRAY_USER,
      BINTRAY_API_KEY: process.env.BINTRAY_API_KEY,
      BINTRAY_ORG: process.env.BINTRAY_ORG,
      BINTRAY_MAVEN_REPO: process.env.BINTRAY_MAVEN_REPO,
      BINTRAY_PACKAGE: process.env.BINTRAY_PACKAGE,
      BINTRAY_DESCRIPTION: process.env.BINTRAY_DESCRIPTION,
      BINTRAY_WEBSITE: process.env.BINTRAY_WEBSITE
    });
  }

  const repository = new Bintray({
    username: bintrayProps.BINTRAY_USER,
    apikey: bintrayProps.BINTRAY_API_KEY,
    organization: bintrayProps.BINTRAY_ORG,
    repository: bintrayProps.BINTRAY_MAVEN_REPO
  });

  const myPackage = {
    name: bintrayProps.BINTRAY_PACKAGE,
    desc: bintrayProps.BINTRAY_DESCRIPTION,
    licenses: ['MIT'],
    website_url: bintrayProps.BINTRAY_WEBSITE
  };

  const packages = (await repository.getPackages()).data;
  const packageExists = packages.find((mavenPackage) => {
    return mavenPackage.name === myPackage.name;
  });

  if (!packageExists) {
    console.log('package not found... creating');
    await repository.createPackage(myPackage);
  }

  try {
    await repository.getPackageVersion(myPackage.name, version);
  } catch (e) {
    if (e.data && e.data.message === `Version '${version}' was not found`) {
      console.log('version not found... creating');
      await repository.createPackageVersion(myPackage.name, {
        name: version,
        vcs_tag: version
      });
    } else {
      throw e;
    }
  }

  async function uploadFile(filePath, remotePath) {
    console.log('uploading :: ', filePath, remotePath);
    await repository.uploadPackage(myPackage.name, version, path.join(projectFolder, filePath), remotePath, true);
  }

  const javadocFile = isWhitelabel ?
    `./build/docs/clarabridgechat-${version}-javadoc.jar` :
    `./ui/build/libs/clarabridgechat-${version}-javadoc.jar`;
  const pomFile = (module) => isWhitelabel ?
    `./build/${module}/${module}-${version}.pom` :
    `./${module}/build/poms/pom-default.xml`;
  const aarFile = (module) => isWhitelabel ?
    `./build/${module}/${module}-${version}.aar` :
    `./${module}/build/outputs/aar/${module}-release.aar`;
  const classesFile = (module) => isWhitelabel ?
    `./build/${module}/${module}-${version}-classes.jar` :
    `./${module}/build/libs/${module}-${version}-classes.jar`;

  const javadocRemote = `com/clarabridge/ui/${version}/clarabridgechat-${version}-javadoc.jar`;
  const pomRemote = (module) => `com/clarabridge/${module}/${version}/${module}-${version}.pom`;
  const aarRemote = (module) => `com/clarabridge/${module}/${version}/${module}-${version}.aar`;
  const classesRemote = (module) => `com/clarabridge/${module}/${version}/${module}-${version}-classes.jar`;

  await uploadFile(classesFile('core'), classesRemote('core'));
  await uploadFile(aarFile('core'), aarRemote('core'));
  await uploadFile(pomFile('core'), pomRemote('core'));

  await uploadFile(classesFile('ui'), classesRemote('ui'));
  await uploadFile(aarFile('ui'), aarRemote('ui'));
  await uploadFile(pomFile('ui'), pomRemote('ui'));
  await uploadFile(javadocFile, javadocRemote)

  await repository.publishPackage(myPackage.name, version);
})().then(() => {
  console.log('files uploaded successfully');
}).catch((e) => {
  console.log('an error occurred :: ', e);
  process.exit(1);
});
