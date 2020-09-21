package com.clarabridge.core.model;

import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * Describes a participant of a conversation
 */
public class ParticipantDto {

    @SerializedName("_id")
    private String id;
    private String appUserId;
    private int unreadCount;
    @Nullable
    private Double lastRead;

    public ParticipantDto(
            String id,
            String appUserId,
            int unreadCount,
            @Nullable Double lastRead) {
        this.id = id;
        this.appUserId = appUserId;
        this.unreadCount = unreadCount;
        this.lastRead = lastRead;
    }

    /**
     * @return the participant ID
     */
    public String getId() {
        return id;
    }

    /**
     * @return the app user ID
     */
    public String getAppUserId() {
        return appUserId;
    }

    /**
     * @return the unread count for this participant
     */
    public int getUnreadCount() {
        return unreadCount;
    }

    /**
     * Set the unread count of this participant.
     *
     * @param unreadCount the new unread count value
     */
    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    /**
     * @return the time this participant last read this conversation
     */
    @Nullable
    public Double getLastRead() {
        return lastRead;
    }

    /**
     * Set the last read timestamp of this participant
     *
     * @param lastRead the new last read value
     */
    public void setLastRead(@Nullable Double lastRead) {
        this.lastRead = lastRead;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ParticipantDto that = (ParticipantDto) o;

        if (unreadCount != that.unreadCount) {
            return false;
        }
        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }
        if (appUserId != null ? !appUserId.equals(that.appUserId) : that.appUserId != null) {
            return false;
        }
        return lastRead != null ? lastRead.equals(that.lastRead) : that.lastRead == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (appUserId != null ? appUserId.hashCode() : 0);
        result = 31 * result + unreadCount;
        result = 31 * result + (lastRead != null ? lastRead.hashCode() : 0);
        return result;
    }
}
