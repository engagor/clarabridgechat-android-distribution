package com.clarabridge.core.network;

import android.support.annotation.Nullable;

import javax.inject.Inject;

import com.clarabridge.core.facade.PersistenceFacade;
import com.clarabridge.core.model.AppUserDto;
import com.clarabridge.core.utils.StringUtils;
import okhttp3.Credentials;
import okhttp3.Interceptor;

/**
 * An {@link Interceptor} that adds the Authorization header to HTTP requests.
 * <p>
 * The header value is based on values read from {@link PersistenceFacade} before each request.
 * <p>
 * If {@link PersistenceFacade#getJwt()} is not null or empty then Authorization Bearer is used,
 * otherwise {@link PersistenceFacade#getSessionToken} and {@link PersistenceFacade#getAppUserRemote}
 * are used to check if the user can be authenticated anonymously using Authentication Basic.
 */
public class AuthorizationHeaderInterceptor extends HeaderInterceptor {

    private final PersistenceFacade persistenceFacade;

    @Inject
    public AuthorizationHeaderInterceptor(PersistenceFacade persistenceFacade) {
        super("Authorization");
        this.persistenceFacade = persistenceFacade;
    }

    @Nullable
    @Override
    String getHeaderValue() {
        String jwt = persistenceFacade.getJwt();

        if (!StringUtils.isEmpty(jwt)) {
            return "Bearer " + jwt;
        }

        AppUserDto appUserDto = persistenceFacade.getAppUserRemote();
        String appUserId = appUserDto == null ? null : appUserDto.getAppUserId();
        String userId = appUserDto == null ? null : appUserDto.getUserId();
        String sessionToken = persistenceFacade.getSessionToken();

        boolean isAnonymousUser = appUserId != null && sessionToken != null && userId == null;

        if (isAnonymousUser) {
            return Credentials.basic(appUserId, sessionToken);
        }

        return null;
    }
}
