package com.clarabridge.core.di;

import android.app.Application;
import android.os.Handler;

import androidx.annotation.Nullable;

import dagger.BindsInstance;
import dagger.Component;
import com.clarabridge.core.AuthenticationDelegate;
import com.clarabridge.core.Settings;
import com.clarabridge.core.facade.PersistenceFacade;
import com.clarabridge.core.network.ClarabridgeChatApiClient;
import com.clarabridge.core.service.ConversationManager;
import com.clarabridge.core.service.ServiceSettings;

/**
 * The main {@link Component} that should be built when the SDK is initialised. It requires an
 * {@link Application} and the {@link Settings} that were used to initialise the SDK.
 */
@Component(modules = {
        ClarabridgeChatModule.class,
        NetworkModule.class,
        AndroidModule.class
})
@SdkScope
public interface ClarabridgeChatComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder application(Application application);

        @BindsInstance
        Builder settings(Settings settings);

        ClarabridgeChatComponent build();
    }

    Handler handler();

    ServiceSettings serviceSettings();

    PersistenceFacade persistenceFacade();

    ClarabridgeChatApiClient clarabridgeChatApiClient();

    @Nullable
    AuthenticationDelegate authenticationDelegate();

    ConversationManager conversationManager();

}

