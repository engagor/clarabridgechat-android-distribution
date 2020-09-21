package com.clarabridge.core.model;

/**
 * Model used when the user is sending a new message to the backend through the API.
 */
public class PostNewMessageDto {

    private final NewMessageDto message;
    private final PostAuthorDto author;

    public PostNewMessageDto(
            NewMessageDto message,
            PostAuthorDto author) {
        this.message = message;
        this.author = author;
    }

    public PostAuthorDto getAuthor() {
        return author;
    }

    public NewMessageDto getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PostNewMessageDto that = (PostNewMessageDto) o;

        if (message != null ? !message.equals(that.message) : that.message != null) {
            return false;
        }
        return author != null ? author.equals(that.author) : that.author == null;
    }

    @Override
    public int hashCode() {
        int result = message != null ? message.hashCode() : 0;
        result = 31 * result + (author != null ? author.hashCode() : 0);
        return result;
    }
}
