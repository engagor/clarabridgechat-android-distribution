package com.clarabridge.core.model;

/**
 * A request model to send a conversation activity performed by a user on a conversation.
 */
public class PostConversationActivityDto {

    private final ActivityDto activity;
    private final PostAuthorDto author;

    public PostConversationActivityDto(
            ActivityDto activity,
            PostAuthorDto author) {
        this.activity = activity;
        this.author = author;
    }

    public ActivityDto getActivity() {
        return activity;
    }

    public PostAuthorDto getAuthor() {
        return author;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PostConversationActivityDto that = (PostConversationActivityDto) o;

        if (activity != null ? !activity.equals(that.activity) : that.activity != null) {
            return false;
        }
        return author != null ? author.equals(that.author) : that.author == null;
    }

    @Override
    public int hashCode() {
        int result = activity != null ? activity.hashCode() : 0;
        result = 31 * result + (author != null ? author.hashCode() : 0);
        return result;
    }
}
