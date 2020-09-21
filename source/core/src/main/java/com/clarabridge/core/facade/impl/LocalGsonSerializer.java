package com.clarabridge.core.facade.impl;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.inject.Inject;

import com.clarabridge.core.facade.Serialization;

public class LocalGsonSerializer implements Serialization {

    private static final String TAG = "LocalGsonSerializer";
    private final Gson gson;

    @Inject
    public LocalGsonSerializer() {
        this.gson = new GsonBuilder().create();
    }

    @Override
    public String serialize(final Object object) {
        if (object == null) {
            return "";
        }

        return gson.toJson(object);
    }

    @Override
    public <T> T deserialize(final String data, final Class<T> clazz) {
        if (data == null) {
            return null;
        }

        try {
            return gson.fromJson(data, clazz);
        } catch (final Exception ex) {
            Log.d(TAG, String.format("Unable to deserialize data: %s", data), ex);
        }

        return null;
    }
}

