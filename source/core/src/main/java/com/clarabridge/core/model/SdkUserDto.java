package com.clarabridge.core.model;

import java.io.Serializable;
import java.util.List;

/**
 * A model that describes everything about the SDK user. It is returned by endpoints that result
 * in the user being created, authenticated or updated (such as /login, /appusers, /appusers/:id).
 */
public class SdkUserDto implements Serializable {

    private AppUserDto appUser;
    private String sessionToken;
    private UserSettingsDto settings;
    private List<ConversationDto> conversations;

    public AppUserDto getAppUser() {
        return appUser;
    }

    public void setAppUser(AppUserDto appUser) {
        this.appUser = appUser;
    }

    public UserSettingsDto getSettings() {
        return settings;
    }

    public void setSettings(UserSettingsDto settings) {
        this.settings = settings;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public List<ConversationDto> getConversations() {
        return conversations;
    }

    public void setConversations(List<ConversationDto> conversations) {
        this.conversations = conversations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SdkUserDto that = (SdkUserDto) o;

        if (appUser != null ? !appUser.equals(that.appUser) : that.appUser != null) {
            return false;
        }
        if (sessionToken != null ? !sessionToken.equals(that.sessionToken) : that.sessionToken != null) {
            return false;
        }
        if (settings != null ? !settings.equals(that.settings) : that.settings != null) {
            return false;
        }
        return conversations != null ? conversations.equals(that.conversations) : that.conversations == null;
    }

    @Override
    public int hashCode() {
        int result = appUser != null ? appUser.hashCode() : 0;
        result = 31 * result + (sessionToken != null ? sessionToken.hashCode() : 0);
        result = 31 * result + (settings != null ? settings.hashCode() : 0);
        result = 31 * result + (conversations != null ? conversations.hashCode() : 0);
        return result;
    }
}
