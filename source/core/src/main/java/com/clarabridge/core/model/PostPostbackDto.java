package com.clarabridge.core.model;

/**
 * A request model to send a {@link PostbackDto} to the backend.
 */
public class PostPostbackDto {

    private final PostbackDto postback;
    private final PostAuthorDto author;

    public PostPostbackDto(
            PostbackDto postback,
            PostAuthorDto author) {
        this.postback = postback;
        this.author = author;
    }

    public PostbackDto getPostback() {
        return postback;
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

        PostPostbackDto that = (PostPostbackDto) o;

        if (postback != null ? !postback.equals(that.postback) : that.postback != null) {
            return false;
        }
        return author != null ? author.equals(that.author) : that.author == null;
    }

    @Override
    public int hashCode() {
        int result = postback != null ? postback.hashCode() : 0;
        result = 31 * result + (author != null ? author.hashCode() : 0);
        return result;
    }
}
