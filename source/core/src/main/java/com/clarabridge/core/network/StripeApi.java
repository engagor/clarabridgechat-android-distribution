package com.clarabridge.core.network;

import com.clarabridge.core.model.StripeTokenDto;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * A {@link retrofit2.Retrofit} interface used to make REST requests to the Stripe API.
 */
interface StripeApi {

    @FormUrlEncoded
    @POST("v1/tokens")
    Call<StripeTokenDto> getToken(
            @Field("card[number]") String cardNumber,
            @Field("card[exp_year]") String expiryYear,
            @Field("card[exp_month]") String expiryMonth,
            @Field("card[cvc]") String securityCode);
}
