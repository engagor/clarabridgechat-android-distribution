package com.clarabridge.core.network;

import com.clarabridge.core.model.AppUserDto;
import com.clarabridge.core.model.ConversationResponseDto;
import com.clarabridge.core.model.ConversationsListResponseDto;
import com.clarabridge.core.model.FileUploadDto;
import com.clarabridge.core.model.GetConfigDto;
import com.clarabridge.core.model.PostAppUserDto;
import com.clarabridge.core.model.PostAuthorDto;
import com.clarabridge.core.model.PostClientIdDto;
import com.clarabridge.core.model.PostConsumeAuthCodeDto;
import com.clarabridge.core.model.PostConversationActivityDto;
import com.clarabridge.core.model.PostIntentDto;
import com.clarabridge.core.model.PostLoginDto;
import com.clarabridge.core.model.PostLogoutDto;
import com.clarabridge.core.model.PostMessageDto;
import com.clarabridge.core.model.PostMetadataDto;
import com.clarabridge.core.model.PostNewMessageDto;
import com.clarabridge.core.model.PostPostbackDto;
import com.clarabridge.core.model.PostPushTokenDto;
import com.clarabridge.core.model.PostStripeDto;
import com.clarabridge.core.model.PostSubscribeDto;
import com.clarabridge.core.model.SdkUserDto;
import com.clarabridge.core.model.StripeCustomerDto;
import com.clarabridge.core.model.UpgradeDto;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

/**
 * A {@link retrofit2.Retrofit} interface used to make REST requests to the ClarabridgeChat API.
 */
interface ClarabridgeChatApi {

    // region SDK endpoints
    @Headers("Content-Type:application/json")
    @GET("sdk/v2/integrations/{integrationId}/config")
    Call<GetConfigDto> getConfig(@Path("integrationId") String integrationId);
    // endregion

    // region User endpoints
    @Headers("Content-Type:application/json")
    @POST("v2/apps/{appId}/appusers")
    Call<SdkUserDto> createUser(
            @Path("appId") String appId,
            @Body PostAppUserDto postAppUserDto);

    @Headers("Content-Type:application/json")
    @GET("v2/apps/{appId}/appusers/{appUserId}")
    Call<SdkUserDto> getAppUser(
            @Path("appId") String appId,
            @Path("appUserId") String appUserId);

    @Headers("Content-Type:application/json")
    @PUT("v2/apps/{appId}/appusers/{appUserId}")
    Call<Void> updateAppUser(
            @Path("appId") String appId,
            @Path("appUserId") String appUserId,
            @Body AppUserDto appUserDto);

    @Headers("Content-Type:application/json")
    @POST("v2/apps/{appId}/appusers/upgrade")
    Call<UpgradeDto> upgradeAppUser(
            @Path("appId") String appId,
            @Body PostClientIdDto postClientIdDto);

    @Headers("Content-Type:application/json")
    @POST("v2/apps/{appId}/appusers/authcode")
    Call<UpgradeDto> consumeAuthCode(
            @Path("appId") String appId,
            @Body PostConsumeAuthCodeDto postConsumeAuthCodeDto);

    @Headers("Content-Type:application/json")
    @POST("v2/apps/{appId}/login")
    Call<SdkUserDto> login(
            @Path("appId") String appId,
            @Body PostLoginDto postLoginDto);

    @Headers("Content-Type:application/json")
    @POST("v2/apps/{appId}/appusers/{appUserId}/logout")
    Call<Void> logout(
            @Path("appId") String appId,
            @Path("appUserId") String appUserId,
            @Body PostLogoutDto postLogoutDto);

    @Headers("Content-Type:application/json")
    @PUT("v2/apps/{appId}/appusers/{appUserId}/clients/{clientId}")
    Call<Void> updatePushToken(
            @Path("appId") String appId,
            @Path("appUserId") String appUserId,
            @Path("clientId") String clientId,
            @Body PostPushTokenDto postPushTokenDto);
    // endregion

    // region Conversation endpoints
    @Headers("Content-Type:application/json")
    @POST("v2/apps/{appId}/conversations/{conversationId}/subscribe")
    Call<ConversationResponseDto> subscribe(
            @Path("appId") String appId,
            @Path("conversationId") String conversationId,
            @Body PostSubscribeDto postSubscribeDto);

    @Headers("Content-Type:application/json")
    @POST("v2/apps/{appId}/appusers/{appUserId}/conversation")
    Call<ConversationResponseDto> createConversation(
            @Path("appId") String appId,
            @Path("appUserId") String appUserId,
            @Body PostIntentDto postIntentDto);

    @Headers("Content-Type:application/json")
    @GET("v2/apps/{appId}/appusers/{appUserId}/conversations")
    Call<ConversationsListResponseDto> getConversations(
            @Path("appId") String appId,
            @Path("appUserId") String appUserId);

    @Headers("Content-Type:application/json")
    @GET("v2/apps/{appId}/conversations/{conversationId}")
    Call<ConversationResponseDto> getConversation(
            @Path("appId") String appId,
            @Path("conversationId") String conversationId);

    @Headers("Content-Type:application/json")
    @GET("v2/apps/{appId}/conversations/{conversationId}/messages")
    Call<ConversationResponseDto> getMessages(
            @Path("appId") String appId,
            @Path("conversationId") String conversationId);

    @Headers("Content-Type:application/json")
    @POST("v2/apps/{appId}/conversations/{conversationId}/messages")
    Call<PostMessageDto> postMessage(
            @Path("appId") String appId,
            @Path("conversationId") String conversationId,
            @Body PostNewMessageDto postNewMessageDto);

    @Headers("Content-Type:application/json")
    @POST("v2/apps/{appId}/conversations/{conversationId}/activity")
    Call<Void> sendConversationActivity(
            @Path("appId") String appId,
            @Path("conversationId") String conversationId,
            @Body PostConversationActivityDto postConversationActivityDto);

    @Headers("Content-Type:application/json")
    @POST("v2/apps/{appId}/conversations/{conversationId}/postback")
    Call<Void> postback(
            @Path("appId") String appId,
            @Path("conversationId") String conversationId,
            @Body PostPostbackDto postPostbackDto);

    @Multipart
    @POST("v2/apps/{appId}/conversations/{conversationId}/files")
    Call<FileUploadDto> uploadFile(
            @Path("appId") String appId,
            @Path("conversationId") String conversationId,
            @Part("author") PostAuthorDto postAuthorDto,
            @Part("message") PostMetadataDto postMetadataDto,
            @Part MultipartBody.Part file);
    // endregion

    // region Stripe endpoints
    @Headers("Content-Type:application/json")
    @POST("v2/apps/{appId}/appusers/{appUserId}/stripe/transaction")
    Call<Void> stripeCharge(
            @Path("appId") String appId,
            @Path("appUserId") String appUserId,
            @Body PostStripeDto postStripeDto);

    @Headers("Content-Type:application/json")
    @POST("v2/apps/{appId}/appusers/{appUserId}/stripe/customer")
    Call<Void> storeStripeToken(
            @Path("appId") String appId,
            @Path("appUserId") String appUserId,
            @Body PostStripeDto postStripeDto);

    @Headers("Content-Type:application/json")
    @GET("v2/apps/{appId}/appusers/{appUserId}/stripe/customer")
    Call<StripeCustomerDto> getStripeCustomer(
            @Path("appId") String appId,
            @Path("appUserId") String appUserId);
    // endregion
}
