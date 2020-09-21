package com.clarabridge.core.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class UpgradeDto implements Serializable {

    @SerializedName("appUser")
    private UpgradeAppUserDto appUser;

    public UpgradeAppUserDto getAppUser() {
        return appUser;
    }

    public void setAppUser(UpgradeAppUserDto appUser) {
        this.appUser = appUser;
    }
}
