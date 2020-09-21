#!/usr/bin/env node

"use strict";
const request = require("request-promise-native");
const path = require("path");
const fs = require("fs");

const argv = require("yargs").string("buildVersion").argv;

const projectFolder = path.join(__dirname, "..");
const sdkVersion = argv.buildVersion;

(async function() {
  console.log(`Uploading app with SDK v${sdkVersion}`);

  const apiKey = process.env.DATATHEOREM_API_KEY;

  if (!apiKey) {
    throw new Error("Missing API Key");
  }

  const { upload_url: uploadUrl } = await request.post(
    "https://api.securetheorem.com/uploadapi/v1/upload_init",
    {
      headers: { Authorization: `APIKey ${apiKey}` },
      json: true
    }
  );

  const releaseApkPath = path.join(
    projectFolder,
    "./shellapp/build/outputs/apk/release/shellapp-release.apk"
  );

  return request.post(uploadUrl, {
    formData: {
      file: fs.createReadStream(releaseApkPath),
      comments: `APK with SDK v${sdkVersion}`
    }
  });
})()
  .then(() => {
    console.log("file uploaded successfully");
  })
  .catch(e => {
    console.log("an error occurred :: ", e.message);
    process.exit(1);
  });
