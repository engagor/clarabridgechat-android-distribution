/*
 * Copyright (C) 2007-2008 OpenIntents.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//
//   Modifications copyright (c) 2018 ClarabridgeChat Technologies.
//

package com.clarabridge.core.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;

import com.clarabridge.core.BuildConfig;

@android.support.annotation.RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class FileUtils {
    private FileUtils() {
    }

    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "FileUtils";

    private static String localAuthority;

    private static String getExtension(String uri) {
        if (uri == null) {
            return null;
        }

        int dot = uri.lastIndexOf(".");
        if (dot >= 0) {
            return uri.substring(dot);
        } else {
            // No extension.
            return "";
        }
    }

    private static boolean isLocalStorageDocument(Uri uri) {
        if (localAuthority != null) {
            return localAuthority.equals(uri.getAuthority());
        }

        return false;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    private static boolean isMediaStoreUri(Uri uri) {
        return "content".equalsIgnoreCase(uri.getScheme());
    }

    private static boolean isFileUri(Uri uri) {
        return "file".equalsIgnoreCase(uri.getScheme());
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                if (DEBUG) {
                    DatabaseUtils.dumpCursor(cursor);
                }

                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    public static String getMimeType(File file) {
        String extension = getExtension(file.getName());
        String mimeType = null;

        if (extension.length() > 0) {
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.substring(1));
        }

        return mimeType != null ? mimeType : "application/octet-stream";
    }

    public static String getPath(final Context context, final Uri uri) {

        if (DEBUG) {
            Log.d(TAG + " File -",
                    "Authority: " + uri.getAuthority() +
                            ", Fragment: " + uri.getFragment() +
                            ", Port: " + uri.getPort() +
                            ", Query: " + uri.getQuery() +
                            ", Scheme: " + uri.getScheme() +
                            ", Host: " + uri.getHost() +
                            ", Segments: " + uri.getPathSegments().toString()
            );
        }

        // DocumentProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, uri)) {
            // LocalStorageProvider
            if (isLocalStorageDocument(uri)) {
                // The path is the id
                return DocumentsContract.getDocumentId(uri);

            } else if (isExternalStorageDocument(uri)) {
                return getExternalStorageProviderPath(context, uri);

            } else if (isDownloadsDocument(uri)) {
                return getDownloadsProviderPath(context, uri);

            } else if (isMediaDocument(uri)) {
                return getMediaProviderPath(context, uri);
            }

        } else if (isMediaStoreUri(uri)) {
            return getMediaStorePath(context, uri);

        } else if (isFileUri(uri)) {
            return uri.getPath();
        }

        return null;
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private static String getExternalStorageProviderPath(final Context context, final Uri uri) {
        final String docId = DocumentsContract.getDocumentId(uri);
        final String[] split = docId.split(":");
        final String type = split[0];

        if ("primary".equalsIgnoreCase(type)) {
            return Environment.getExternalStorageDirectory() + "/" + split[1];
        }

        // TODO handle non-primary volumes
        return null;
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private static String getDownloadsProviderPath(final Context context, final Uri uri) {
        final String id = DocumentsContract.getDocumentId(uri);
        if (!TextUtils.isEmpty(id)) {
            if (id.startsWith("raw:")) {
                return id.replaceFirst("raw:", "");
            }

            try {
                return getDataColumn(context, uri, null, null);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Downloads provider returned unexpected uri " + uri.toString() + ", quitting");
            }
        }
        return null;
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private static String getMediaProviderPath(final Context context, final Uri uri) {
        final String docId = DocumentsContract.getDocumentId(uri);
        final String[] split = docId.split(":");
        final String type = split[0];

        Uri contentUri = null;
        if ("image".equals(type)) {
            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else if ("video".equals(type)) {
            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        } else if ("audio".equals(type)) {
            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }

        final String selection = "_id=?";
        final String[] selectionArgs = new String[]{
                split[1]
        };

        return getDataColumn(context, contentUri, selection, selectionArgs);
    }

    private static String getMediaStorePath(final Context context, final Uri uri) {
        // Return the remote address
        if (isGooglePhotosUri(uri)) {
            return uri.getLastPathSegment();
        }
        return getDataColumn(context, uri, null, null);
    }

    public static String getLocalAuthority() {
        return localAuthority;
    }

    public static void setLocalAuthority(String localAuthority) {
        FileUtils.localAuthority = localAuthority;
    }
}
