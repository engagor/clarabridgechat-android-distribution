package com.clarabridge.core.network;

import androidx.annotation.VisibleForTesting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Set;

import javax.inject.Inject;

import com.clarabridge.core.di.NetworkHeaderQualifier;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A factory to create instances of the API interfaces.
 */
public class ApiFactory {

    private final Set<Interceptor> clarabridgeChatInterceptors;
    private final Set<Interceptor> stripeInterceptors;
    private final GsonConverterFactory gsonConverterFactory;

    @Inject
    public ApiFactory(
            @NetworkHeaderQualifier("clarabridgeChat") Set<Interceptor> clarabridgeChatInterceptors,
            @NetworkHeaderQualifier("stripe") Set<Interceptor> stripeInterceptors) {
        this.clarabridgeChatInterceptors = clarabridgeChatInterceptors;
        this.stripeInterceptors = stripeInterceptors;
        Gson gson = buildGson();
        this.gsonConverterFactory = GsonConverterFactory.create(gson);
    }

    public ClarabridgeChatApi createClarabridgeChatApi(String baseUrl) {
        OkHttpClient okHttpClient = buildOkHttpClient(clarabridgeChatInterceptors);
        Retrofit retrofit = buildRetrofit(okHttpClient, baseUrl, gsonConverterFactory);
        return retrofit.create(ClarabridgeChatApi.class);
    }

    public StripeApi createStripeApi(String baseUrl) {
        OkHttpClient okHttpClient = buildOkHttpClient(stripeInterceptors);
        Retrofit retrofit = buildRetrofit(okHttpClient, baseUrl, gsonConverterFactory);
        return retrofit.create(StripeApi.class);
    }

    /**
     * Creates an instance of {@link OkHttpClient} with the provided {@link Interceptor}s.
     *
     * @param interceptors a {@link Set} of {@link Interceptor}s to be added to the new client
     * @return the new instance
     */
    @VisibleForTesting
    OkHttpClient buildOkHttpClient(Set<Interceptor> interceptors) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        for (Interceptor interceptor : interceptors) {
            builder.addInterceptor(interceptor);
        }

        return builder.build();
    }

    /**
     * Creates an instance of {@link Gson} with the necessary setup for network requests.
     *
     * @return the new instance
     */
    private Gson buildGson() {
        return new GsonBuilder()
                .addSerializationExclusionStrategy(new LocalFieldExclusionStrategy())
                .create();
    }

    /**
     * Creates an instance of {@link Retrofit}.
     *
     * @param okHttpClient         the {@link OkHttpClient} to be used for all requests
     * @param baseUrl              the base URL for all requests executed by this instance
     * @param gsonConverterFactory the {@link GsonConverterFactory} to be used to serialize and
     *                             deserialize all request and response models
     * @return the new instance
     */
    @VisibleForTesting
    Retrofit buildRetrofit(
            OkHttpClient okHttpClient,
            String baseUrl,
            GsonConverterFactory gsonConverterFactory) {

        String actualUrl;
        if (baseUrl.endsWith("/")) {
            actualUrl = baseUrl;
        } else {
            actualUrl = baseUrl + "/";
        }

        return new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(actualUrl)
                .addConverterFactory(gsonConverterFactory)
                .build();
    }
}
