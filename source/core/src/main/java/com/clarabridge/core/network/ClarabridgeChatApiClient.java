package com.clarabridge.core.network;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;

import com.clarabridge.core.AuthenticationCallback;
import com.clarabridge.core.CreditCard;
import com.clarabridge.core.Logger;
import com.clarabridge.core.Message;
import com.clarabridge.core.MessageType;
import com.clarabridge.core.Settings;
import com.clarabridge.core.ClarabridgeChat;
import com.clarabridge.core.di.SdkScope;
import com.clarabridge.core.model.ActivityDto;
import com.clarabridge.core.model.AppUserDto;
import com.clarabridge.core.model.ConversationResponseDto;
import com.clarabridge.core.model.ConversationsListResponseDto;
import com.clarabridge.core.model.FileUploadDto;
import com.clarabridge.core.model.GetConfigDto;
import com.clarabridge.core.model.MessageActionDto;
import com.clarabridge.core.model.MessageDto;
import com.clarabridge.core.model.NewMessageDto;
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
import com.clarabridge.core.model.PostbackDto;
import com.clarabridge.core.model.SdkUserDto;
import com.clarabridge.core.model.StripeCustomerDto;
import com.clarabridge.core.model.StripeTokenDto;
import com.clarabridge.core.model.UpgradeDto;
import com.clarabridge.core.service.ServiceSettings;
import com.clarabridge.core.utils.ClientProvider;
import com.clarabridge.core.utils.FileUtils;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;

/**
 * An API client used to make REST requests to the ClarabridgeChat API. Its initial configuration is defined
 * by {@link Settings} and {@link ServiceSettings}. Use the setters to update its configuration or
 * behaviour.
 *
 * @see #setBaseUrl(String)
 * @see #setAuthenticationCallback(AuthenticationCallback)
 */
@SdkScope
public class ClarabridgeChatApiClient {

    private static final String LOG_TAG = "ClarabridgeChatApiClient";
    private static final String STRIPE_API_BASE_URL = "https://api.stripe.com";

    private final String integrationId;
    private final ApiFactory apiFactory;
    private final AuthRetryCallbackFactory authRetryCallbackFactory;
    private final ClientProvider clientProvider;

    private String appId;
    private ClarabridgeChatApi clarabridgeChatApi;
    private final StripeApi stripeApi;

    @Nullable
    private AuthenticationCallback authenticationCallback;

    @Inject
    public ClarabridgeChatApiClient(
            Settings settings,
            ServiceSettings serviceSettings,
            ApiFactory apiFactory,
            AuthRetryCallbackFactory authRetryCallbackFactory,
            ClientProvider clientProvider) {

        this.integrationId = settings.getIntegrationId();
        this.apiFactory = apiFactory;
        this.authRetryCallbackFactory = authRetryCallbackFactory;
        this.clientProvider = clientProvider;

        String baseUrl = serviceSettings.getBaseUrl(integrationId);
        this.clarabridgeChatApi = apiFactory.createClarabridgeChatApi(baseUrl);
        this.stripeApi = apiFactory.createStripeApi(STRIPE_API_BASE_URL);
    }

    // region Setters

    /**
     * Sets the new base URL for all REST requests.
     *
     * @param baseUrl the new base URL
     */
    public void setBaseUrl(@NonNull String baseUrl) {
        this.clarabridgeChatApi = apiFactory.createClarabridgeChatApi(baseUrl);
    }

    /**
     * Sets the app id to be used by the REST requests that require it. The app id can be retrieved
     * as the result of {@link #getConfig(ClarabridgeChatApiClientCallback)}.
     *
     * @param appId the app id
     */
    public void setAppId(@NonNull String appId) {
        this.appId = appId;
    }

    /**
     * Sets the {@link AuthenticationCallback} that will be invoked if a requests can be retried.
     *
     * @param authenticationCallback an instance of {@link AuthenticationCallback}
     */
    public void setAuthenticationCallback(@Nullable AuthenticationCallback authenticationCallback) {
        this.authenticationCallback = authenticationCallback;
    }
    // endregion

    // region SDK endpoints

    /**
     * Retrieves the {@link GetConfigDto} for this {@link ClarabridgeChatApiClient#integrationId}, invoking
     * the callback when the request is finished.
     *
     * @param callback an instance of {@link ClarabridgeChatApiClientCallback}&lt;{@link GetConfigDto}>
     */
    public void getConfig(ClarabridgeChatApiClientCallback<GetConfigDto> callback) {
        clarabridgeChatApi.getConfig(integrationId)
                .enqueue(authRetryCallbackFactory.createCallback(callback, authenticationCallback));
    }
    // endregion

    // region User endpoints

    /**
     * Creates a user in the backend with the details of {@link PostAppUserDto}, invoking the
     * callback when the request is finished.
     *
     * @param appUser  the details of the user to be created
     * @param userId   a unique arbitrary id for the user
     * @param intent   the intent to be passed to the server
     * @param callback an instance of {@link ClarabridgeChatApiClientCallback}&lt;{@link SdkUserDto}>
     */
    public void createUser(
            AppUserDto appUser,
            String userId,
            String intent,
            ClarabridgeChatApiClientCallback<SdkUserDto> callback) {

        if (!configured()) {
            return;
        }

        PostAppUserDto postAppUser = new PostAppUserDto(appUser);
        postAppUser.setClient(clientProvider.buildClient());
        postAppUser.setUserId(userId);
        postAppUser.setIntent(intent);

        clarabridgeChatApi.createUser(appId, postAppUser)
                .enqueue(authRetryCallbackFactory.createCallback(callback, authenticationCallback));
    }

    /**
     * Retrieves the {@link SdkUserDto} for the given user id, invoking the callback when the
     * request is finished.
     *
     * @param appUserId the id of the {@link SdkUserDto}
     * @param callback  an instance of {@link ClarabridgeChatApiClientCallback}&lt;{@link SdkUserDto}>
     */
    public void getAppUser(
            String appUserId,
            ClarabridgeChatApiClientCallback<SdkUserDto> callback) {

        if (!configured()) {
            return;
        }

        clarabridgeChatApi.getAppUser(appId, appUserId)
                .enqueue(authRetryCallbackFactory.createCallback(callback, authenticationCallback));
    }

    /**
     * Update the given user in the backend, invoking the callback when the request is finished.
     *
     * @param appUser   the {@link AppUserDto} to be updated
     * @param appUserId the id of the {@link AppUserDto}
     * @param callback  an instance of {@link ClarabridgeChatApiClientCallback}&lt;{@link Void}>
     */
    public void updateAppUser(
            AppUserDto appUser,
            String appUserId,
            ClarabridgeChatApiClientCallback<Void> callback) {

        if (!configured()) {
            return;
        }

        boolean appUserExists = appUser != null && appUserId != null;

        if (!appUserExists) {
            Logger.e(LOG_TAG, "Attempted to call updateAppUser without appUser. Ignoring!");
            return;
        }

        clarabridgeChatApi.updateAppUser(appId, appUserId, appUser)
                .enqueue(authRetryCallbackFactory.createCallback(callback, authenticationCallback));
    }

    /**
     * Upgrades a legacy client, invoking the callback when the request is finished.
     *
     * @param clientId the id of the client being upgraded
     * @param callback an instance of {@link ClarabridgeChatApiClientCallback}&lt;{@link UpgradeDto}>
     */
    public void upgradeAppUser(
            String clientId,
            ClarabridgeChatApiClientCallback<UpgradeDto> callback) {

        if (!configured()) {
            return;
        }

        PostClientIdDto clientDto = new PostClientIdDto(clientId);

        clarabridgeChatApi.upgradeAppUser(appId, clientDto)
                .enqueue(authRetryCallbackFactory.createCallback(callback, authenticationCallback));
    }

    /**
     * Authenticates the current user, invoking the callback when the request is finished.
     *
     * @param authCode the auth code to authenticate the user
     * @param callback an instance of {@link ClarabridgeChatApiClientCallback}&lt;{@link UpgradeDto}>
     */
    public void consumeAuthCode(
            String authCode,
            ClarabridgeChatApiClientCallback<UpgradeDto> callback) {

        if (!configured()) {
            return;
        }

        PostConsumeAuthCodeDto consumeAuthCodeDto = new PostConsumeAuthCodeDto(authCode, clientProvider.buildClient());

        clarabridgeChatApi.consumeAuthCode(appId, consumeAuthCodeDto)
                .enqueue(authRetryCallbackFactory.createCallback(callback, authenticationCallback));
    }

    /**
     * Login the user, invoking the callback when the request is finished.
     *
     * @param appUserId    the id of the user
     * @param userId       the user id of the user
     * @param sessionToken the session token of the user
     * @param callback     an instance of {@link ClarabridgeChatApiClientCallback}&lt;{@link SdkUserDto}>
     */
    public void login(
            String appUserId,
            String userId,
            String sessionToken,
            ClarabridgeChatApiClientCallback<SdkUserDto> callback) {

        if (!configured()) {
            return;
        }

        PostLoginDto loginDto = new PostLoginDto(appUserId, sessionToken, userId, clientProvider.buildClient());

        clarabridgeChatApi.login(appId, loginDto)
                .enqueue(authRetryCallbackFactory.createCallback(callback, authenticationCallback));
    }

    /**
     * Logout the user, invoking the callback when the request is finished.
     *
     * @param appUserId the id of the user
     * @param callback  an instance of {@link ClarabridgeChatApiClientCallback}&lt;{@link Void}>
     */
    public void logout(
            String appUserId,
            ClarabridgeChatApiClientCallback<Void> callback) {

        if (!configured()) {
            return;
        }

        PostLogoutDto logoutDto = new PostLogoutDto(clientProvider.buildClient());

        clarabridgeChatApi.logout(appId, appUserId, logoutDto)
                .enqueue(authRetryCallbackFactory.createCallback(callback, authenticationCallback));
    }

    /**
     * Updates the push notification token of this user in the backend, invoking the callback when
     * the request is finished.
     *
     * @param appUserId                   the id of the user
     * @param clientId                    the client id of the user
     * @param firebaseCloudMessagingToken the new push token value
     * @param callback                    an instance of {@link ClarabridgeChatApiClientCallback}&lt;{@link Void}>
     */
    public void updatePushToken(
            String appUserId,
            String clientId,
            String firebaseCloudMessagingToken,
            ClarabridgeChatApiClientCallback<Void> callback) {

        if (!configured()) {
            return;
        }

        if (appUserId == null) {
            Logger.e(LOG_TAG, "Attempted to call updatePushToken without the user id. Ignoring!");
            return;
        }

        PostPushTokenDto pushTokenDto = new PostPushTokenDto(firebaseCloudMessagingToken);

        clarabridgeChatApi.updatePushToken(appId, appUserId, clientId, pushTokenDto)
                .enqueue(authRetryCallbackFactory.createCallback(callback, authenticationCallback));

    }
    // endregion

    // region Conversation endpoints

    /**
     * Subscribe the user to a conversation, invoking the callback when the request is finished.
     *
     * @param conversationId the id of the conversation
     * @param appUserId      the id of the user subscribing to the conversation
     * @param callback       an instance of {@link ClarabridgeChatApiClientCallback}&lt;{@link ConversationResponseDto}>
     */
    public void subscribe(
            String conversationId,
            String appUserId,
            ClarabridgeChatApiClientCallback<ConversationResponseDto> callback) {

        if (!configured()) {
            return;
        }

        PostAuthorDto authorDto = createAuthorDto(appUserId);
        PostSubscribeDto request = new PostSubscribeDto(authorDto);

        clarabridgeChatApi.subscribe(appId, conversationId, request)
                .enqueue(authRetryCallbackFactory.createCallback(callback, authenticationCallback));
    }

    /**
     * Create a conversation for the user, invoking the callback when the request is finished.
     *
     * @param intent    the intent to be passed to the server
     * @param appUserId the id of the user starting the conversation
     * @param callback  an instance of {@link ClarabridgeChatApiClientCallback}&lt;{@link ConversationResponseDto}>
     */
    public void createConversation(
            String intent,
            String appUserId,
            ClarabridgeChatApiClientCallback<ConversationResponseDto> callback) {

        if (!configured()) {
            return;
        }

        PostIntentDto intentDto = new PostIntentDto(intent, clientProvider.buildClient());

        clarabridgeChatApi.createConversation(appId, appUserId, intentDto)
                .enqueue(authRetryCallbackFactory.createCallback(callback, authenticationCallback));
    }

    /**
     * Get all conversations for the user, invoking the callback when the request is finished.
     *
     * @param appUserId the id of the user retrieving the conversations list
     * @param callback  an instance of {@link ClarabridgeChatApiClientCallback}&lt;{@link ConversationResponseDto}>
     */
    public void getConversations(
            String appUserId,
            ClarabridgeChatApiClientCallback<ConversationsListResponseDto> callback) {

        if (!configured()) {
            return;
        }

        clarabridgeChatApi.getConversations(appId, appUserId)
                .enqueue(authRetryCallbackFactory.createCallback(callback, authenticationCallback));
    }

    /**
     * Retrieves the conversation given its id, invoking the callback when the request is finished.
     *
     * @param conversationId the id of the conversation to be retrieved
     * @param callback       an instance of {@link ClarabridgeChatApiClientCallback}&lt;{@link ConversationResponseDto}>
     */
    public void getConversation(
            String conversationId,
            ClarabridgeChatApiClientCallback<ConversationResponseDto> callback) {

        if (!configured()) {
            return;
        }

        if (conversationId == null) {
            Logger.e(LOG_TAG, "Attempted to call getConversation without the conversation id. Ignoring!");
            return;
        }

        clarabridgeChatApi.getConversation(appId, conversationId)
                .enqueue(authRetryCallbackFactory.createCallback(callback, authenticationCallback));
    }

    /**
     * Retrieves the messages of a conversation given its id, invoking the callback when the request
     * is finished.
     *
     * @param conversationId the id of the conversation to be retrieved
     * @param callback       an instance of {@link ClarabridgeChatApiClientCallback}&lt;{@link ConversationResponseDto}>
     */
    public void getMessages(
            String conversationId,
            ClarabridgeChatApiClientCallback<ConversationResponseDto> callback) {

        if (!configured()) {
            return;
        }

        if (conversationId == null) {
            Logger.e(LOG_TAG, "Attempted to call getMessages without the conversation id. Ignoring!");
            return;
        }

        clarabridgeChatApi.getMessages(appId, conversationId)
                .enqueue(authRetryCallbackFactory.createCallback(callback, authenticationCallback));
    }

    /**
     * Sends a message to a conversation, invoking the callback when the request is finished.
     *
     * @param conversationId the id of the conversation
     * @param message        the message to be sent
     * @param appUserId      the id of the user
     * @param callback       an instance of {@link ClarabridgeChatApiClientCallback}&lt;{@link PostMetadataDto}>
     */
    public void postMessage(
            String conversationId,
            MessageDto message,
            String appUserId,
            ClarabridgeChatApiClientCallback<PostMessageDto> callback) {

        if (!configured()) {
            return;
        }

        boolean isLocation = MessageType.LOCATION.getValue().equals(message.getType());

        NewMessageDto messageDto = new NewMessageDto(
                message.getText(),
                "appUser",
                message.getType(),
                isLocation ? message.getCoordinates() : null,
                message.getPayload(),
                message.getMetadata());

        PostAuthorDto authorDto = createAuthorDto(appUserId);
        PostNewMessageDto newMessageDto = new PostNewMessageDto(messageDto, authorDto);

        clarabridgeChatApi.postMessage(appId, conversationId, newMessageDto)
                .enqueue(authRetryCallbackFactory.createCallback(callback, authenticationCallback));
    }

    /**
     * Sends an activity to a conversation, invoking the callback when the request is finished.
     *
     * @param conversationId the id of the conversation
     * @param type           the type of activity being sent
     * @param appUserId      the id of the user
     * @param callback       an instance of {@link ClarabridgeChatApiClientCallback}&lt;{@link Void}>
     */
    public void sendConversationActivity(
            String conversationId,
            String type,
            String appUserId,
            ClarabridgeChatApiClientCallback<Void> callback) {

        if (!configured()) {
            return;
        }

        ActivityDto activityDto = new ActivityDto(type);
        PostAuthorDto authorDto = createAuthorDto(appUserId);
        PostConversationActivityDto conversationActivityDto = new PostConversationActivityDto(
                activityDto,
                authorDto);

        clarabridgeChatApi.sendConversationActivity(appId, conversationId, conversationActivityDto)
                .enqueue(authRetryCallbackFactory.createCallback(callback, authenticationCallback));
    }

    /**
     * Sends a postback to a conversation, invoking the callback when the request is finished.
     *
     * @param conversationId the id of the conversation
     * @param action         the {@link MessageActionDto}
     * @param appUserId      the id of the user
     * @param callback       an instance of {@link ClarabridgeChatApiClientCallback}&lt;{@link Void}>
     */
    public void postback(
            String conversationId,
            MessageActionDto action,
            String appUserId,
            ClarabridgeChatApiClientCallback<Void> callback) {

        if (!configured()) {
            return;
        }

        PostbackDto postbackDto = new PostbackDto(action.getId());
        PostAuthorDto authorDto = createAuthorDto(appUserId);
        PostPostbackDto postPostbackDto = new PostPostbackDto(postbackDto, authorDto);

        clarabridgeChatApi.postback(appId, conversationId, postPostbackDto)
                .enqueue(authRetryCallbackFactory.createCallback(callback, authenticationCallback));
    }

    /**
     * Uploads an image to a conversation, invoking the callback when the request is finished.
     *
     * @param conversationId the id of the conversation
     * @param imageMessage   the {@link Message} containing the image being sent
     * @param appUserId      the id of the user
     * @param callback       an instance of {@link ClarabridgeChatApiClientCallback}&lt;{@link FileUploadDto}>
     */
    public void uploadImage(
            String conversationId,
            Message imageMessage,
            String appUserId,
            ClarabridgeChatApiClientCallback<FileUploadDto> callback) {

        if (!configured()) {
            return;
        }

        Bitmap image = imageMessage.getImage();

        if (image == null) {
            Logger.e(LOG_TAG, "Attempted to call uploadImage without a valid source. Ignoring!");
            callback.onResult(false, 400, null);
            return;
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 75, outputStream);
        byte[] source = outputStream.toByteArray();

        String fileName = "clarabridgeChat-image.jpg";
        MediaType mediaType = MediaType.parse("image/jpeg");

        uploadFile(conversationId, imageMessage.getMetadata(), appUserId, fileName,
                mediaType, source, callback);
    }

    /**
     * Uploads a file to a conversation, invoking the callback when the request is finished.
     *
     * @param conversationId the id of the conversation
     * @param fileMessage    the {@link Message} containing the file being sent
     * @param appUserId      the id of the user
     * @param callback       an instance of {@link ClarabridgeChatApiClientCallback}&lt;{@link FileUploadDto}>
     */
    public void uploadFile(
            String conversationId,
            Message fileMessage,
            String appUserId,
            ClarabridgeChatApiClientCallback<FileUploadDto> callback) {

        if (!configured()) {
            return;
        }

        File file = fileMessage.getFile();

        if (file == null) {
            Logger.e(LOG_TAG, "Attempted to call uploadFile without a valid source. Ignoring!");
            return;
        }

        try {
            byte[] source = new byte[(int) file.length()];
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
            DataInputStream dataInputStream = new DataInputStream(bufferedInputStream);
            dataInputStream.readFully(source);
            dataInputStream.close();

            String fileName = file.getName();
            MediaType mediaType = MediaType.parse(FileUtils.getMimeType(file));

            uploadFile(conversationId, fileMessage.getMetadata(), appUserId, fileName,
                    mediaType, source, callback);
        } catch (IOException ioException) {
            Logger.e(LOG_TAG, "Error reading file to upload", ioException);
            callback.onResult(false, 400, null);
        }
    }

    /**
     * Internal method to upload the byte array of a file to the backend. The callback is invoked
     * when the request is finished.
     *
     * @param conversationId  the id of the conversation
     * @param messageMetadata the metadata about the message
     * @param appUserId       the id of the user
     * @param fileName        the name of the file being uploaded
     * @param mediaType       the {@link MediaType} of the file being uploaded
     * @param source          the file being uploaded, as a byte array
     * @param callback        an instance of {@link ClarabridgeChatApiClientCallback}&lt;{@link FileUploadDto}>
     */
    @VisibleForTesting
    void uploadFile(
            String conversationId,
            Map<String, Object> messageMetadata,
            String appUserId,
            String fileName,
            MediaType mediaType,
            byte[] source,
            ClarabridgeChatApiClientCallback<FileUploadDto> callback) {

        PostAuthorDto authorDto = createAuthorDto(appUserId);
        PostMetadataDto metadataDto = new PostMetadataDto(messageMetadata);
        RequestBody uploadRequestBody = RequestBody.create(mediaType, source);

        Call<FileUploadDto> call = clarabridgeChatApi.uploadFile(
                appId,
                conversationId,
                authorDto,
                metadataDto,
                MultipartBody.Part.createFormData("source", fileName, uploadRequestBody));

        call.enqueue(authRetryCallbackFactory.createCallback(callback, authenticationCallback));
    }
    // endregion

    // region Stripe endpoints

    /**
     * Retrieves a Stripe token for the given {@link CreditCard}, invoking the callback when the
     * request is finished
     *
     * @param creditCard the {@link CreditCard} used to retrieve the token
     * @param callback   an instance of {@link ClarabridgeChatApiClientCallback}&lt;{@link StripeTokenDto}>
     */
    public void getStripeToken(
            CreditCard creditCard,
            final ClarabridgeChatApiClientCallback<StripeTokenDto> callback) {

        Call<StripeTokenDto> call = stripeApi.getToken(creditCard.getCardNumber(),
                Integer.toString(creditCard.getExpYear()),
                Integer.toString(creditCard.getExpMonth()),
                creditCard.getSecurityCode());

        call.enqueue(new Callback<StripeTokenDto>() {
            @Override
            public void onResponse(@NonNull Call<StripeTokenDto> call, @NonNull Response<StripeTokenDto> response) {
                callback.onResult(response.isSuccessful(), response.code(), response.body());
            }

            @Override
            public void onFailure(@NonNull Call<StripeTokenDto> call, @NonNull Throwable t) {
                callback.onResult(false, HTTP_INTERNAL_ERROR, null);
            }
        });
    }

    /**
     * Places a charge action in the backend, invoking the callback when the request is finished.
     *
     * @param appUserId   the id of the user
     * @param action      the {@link MessageActionDto} originating the charge
     * @param stripeToken the stripe token
     * @param callback    an instance of {@link ClarabridgeChatApiClientCallback}&lt;{@link Void}>
     */
    public void stripeCharge(
            String appUserId,
            MessageActionDto action,
            String stripeToken,
            ClarabridgeChatApiClientCallback<Void> callback) {

        if (!configured()) {
            return;
        }

        if (appUserId == null) {
            Logger.e(LOG_TAG, "Attempted to call stripeCharge without a user id. Ignoring!");
            return;
        }

        PostStripeDto postStripeDto = new PostStripeDto(action.getId(), stripeToken);

        clarabridgeChatApi.stripeCharge(appId, appUserId, postStripeDto)
                .enqueue(authRetryCallbackFactory.createCallback(callback, authenticationCallback));
    }

    /**
     * Stores a stripe token in the backend, invoking the callback when the request is finished.
     *
     * @param appUserId   the id of the user
     * @param stripeToken the stripe token to be stored
     * @param callback    an instance of {@link ClarabridgeChatApiClientCallback}&lt;{@link Void}>
     */
    public void storeStripeToken(
            String appUserId,
            String stripeToken,
            ClarabridgeChatApiClientCallback<Void> callback) {

        if (!configured()) {
            return;
        }

        PostStripeDto postStripeDto = new PostStripeDto(null, stripeToken);

        clarabridgeChatApi.storeStripeToken(appId, appUserId, postStripeDto)
                .enqueue(authRetryCallbackFactory.createCallback(callback, authenticationCallback));
    }

    /**
     * Retrieves a {@link StripeCustomerDto} from the backend, invoking the callback when the request
     * is finished.
     *
     * @param appUserId the id of the user
     * @param callback  an instance of {@link ClarabridgeChatApiClientCallback}&lt;{@link StripeCustomerDto}>
     */
    public void getStripeCustomer(
            String appUserId,
            ClarabridgeChatApiClientCallback<StripeCustomerDto> callback) {

        if (!configured()) {
            return;
        }

        clarabridgeChatApi.getStripeCustomer(appId, appUserId)
                .enqueue(authRetryCallbackFactory.createCallback(callback, authenticationCallback));
    }
    // endregion

    private PostAuthorDto createAuthorDto(String appUserId) {
        return new PostAuthorDto("appUser", clientProvider.buildClient(), appUserId);
    }

    /**
     * Indicates whether or not this API client has been properly configured to make network requests,
     * which means that the {@link #appId} was set.
     *
     * @return true if the {@link #appId} is not null, false otherwise
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean configured() {
        if (appId == null) {
            Logger.e(LOG_TAG, "Attempted to make a network request before setting the app id. Ignoring!");
            return false;
        }

        return true;
    }
}
