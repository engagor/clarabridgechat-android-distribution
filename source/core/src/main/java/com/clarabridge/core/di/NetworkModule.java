package com.clarabridge.core.di;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;
import com.clarabridge.core.network.AppIdHeaderInterceptor;
import com.clarabridge.core.network.AppNameHeaderInterceptor;
import com.clarabridge.core.network.AuthorizationHeaderInterceptor;
import com.clarabridge.core.network.ClientIdHeaderInterceptor;
import com.clarabridge.core.network.PushHeaderInterceptor;
import com.clarabridge.core.network.SdkHeaderInterceptor;
import com.clarabridge.core.network.StripeAuthorizationHeaderInterceptor;
import com.clarabridge.core.network.UserAgentHeaderInterceptor;
import okhttp3.Interceptor;

/**
 * A {@link Module} to provide network related services.
 */
@Module
abstract class NetworkModule {

    @Binds
    @IntoSet
    @NetworkHeaderQualifier("clarabridgeChat")
    abstract Interceptor appIdHeaderInterceptor(AppIdHeaderInterceptor appIdHeaderInterceptor);

    @Binds
    @IntoSet
    @NetworkHeaderQualifier("clarabridgeChat")
    abstract Interceptor appNameHeaderInterceptor(AppNameHeaderInterceptor appNameHeaderInterceptor);

    @Binds
    @IntoSet
    @NetworkHeaderQualifier("clarabridgeChat")
    abstract Interceptor authorizationHeaderInteceptor(AuthorizationHeaderInterceptor authorizationHeaderInterceptor);

    @Binds
    @IntoSet
    @NetworkHeaderQualifier("clarabridgeChat")
    abstract Interceptor clientIdHeaderInterceptor(ClientIdHeaderInterceptor clientIdHeaderInterceptor);

    @Binds
    @IntoSet
    @NetworkHeaderQualifier("clarabridgeChat")
    abstract Interceptor pushHeaderInterceptor(PushHeaderInterceptor pushHeaderInterceptor);

    @Binds
    @IntoSet
    @NetworkHeaderQualifier("clarabridgeChat")
    abstract Interceptor sdkHeaderInterceptor(SdkHeaderInterceptor sdkHeaderInterceptor);

    @Binds
    @IntoSet
    @NetworkHeaderQualifier("clarabridgeChat")
    abstract Interceptor userAgentHeaderInterceptor(UserAgentHeaderInterceptor userAgentHeaderInterceptor);

    @Binds
    @IntoSet
    @NetworkHeaderQualifier("stripe")
    abstract Interceptor stripeAuthorizationHeaderInterceptor(
            StripeAuthorizationHeaderInterceptor stripeAuthorizationHeaderInterceptor);
}

