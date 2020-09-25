package com.clarabridge.core.di;

import androidx.annotation.Nullable;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import com.clarabridge.core.AuthenticationDelegate;
import com.clarabridge.core.Settings;
import com.clarabridge.core.facade.Serialization;
import com.clarabridge.core.facade.impl.LocalGsonSerializer;

/**
 * A {@link Module} to provide any ClarabridgeChat specific services.
 */
@Module
abstract class ClarabridgeChatModule {

    @Nullable
    @Provides
    static AuthenticationDelegate authenticationDelegate(Settings settings) {
        return settings.getAuthenticationDelegate();
    }

    @Binds
    abstract Serialization serialization(
            LocalGsonSerializer serializationLayerGson);
}

