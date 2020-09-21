package com.clarabridge.core.di;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

/**
 * A {@link Module} to provide any Android related services.
 */
@Module
abstract class AndroidModule {

    @Binds
    @SdkScope
    abstract Context context(Application application);

    @Provides
    @SdkScope
    static Handler handler() {
        return new Handler(Looper.getMainLooper());
    }
}
