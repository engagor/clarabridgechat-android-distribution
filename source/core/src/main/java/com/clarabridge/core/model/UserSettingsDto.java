package com.clarabridge.core.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class UserSettingsDto implements Serializable {

    @SerializedName("realtime")
    private RealtimeSettingsDto realtime;
    @SerializedName("profile")
    private ProfileSettingsDto profile;
    @SerializedName("typing")
    private TypingSettingsDto typing;

    public RealtimeSettingsDto getRealtime() {
        return realtime;
    }

    public void setRealtime(RealtimeSettingsDto realtime) {
        this.realtime = realtime;
    }

    public ProfileSettingsDto getProfile() {
        return profile;
    }

    public void setProfile(ProfileSettingsDto profile) {
        this.profile = profile;
    }

    public TypingSettingsDto getTyping() {
        return typing;
    }

    public void setTyping(TypingSettingsDto typing) {
        this.typing = typing;
    }
}
