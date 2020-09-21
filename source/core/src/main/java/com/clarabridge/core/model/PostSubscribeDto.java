package com.clarabridge.core.model;

/**
 * A request model to be sent when subscribing to a conversation in the backend.
 */
public class PostSubscribeDto {

    private final PostAuthorDto author;

    public PostSubscribeDto(PostAuthorDto author) {
        this.author = author;
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

        PostSubscribeDto that = (PostSubscribeDto) o;

        return author != null ? author.equals(that.author) : that.author == null;
    }

    @Override
    public int hashCode() {
        return author != null ? author.hashCode() : 0;
    }
}
