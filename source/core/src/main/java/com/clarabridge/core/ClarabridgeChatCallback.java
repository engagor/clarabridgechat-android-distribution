package com.clarabridge.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

/**
 * Callback used to be notified of the result of an asynchronous action
 *
 * @param <T> the type of the data being returned
 */
public interface ClarabridgeChatCallback<T> {

    /**
     * The response body passed into a {@link ClarabridgeChatCallback} to pass along information
     * about the result of the action being performed
     *
     * @param <T> the type of the data being returned
     */
    class Response<T> {

        private int status;
        @Nullable
        private String error;
        @Nullable
        private T data;

        /**
         * Creates an instance of {@link Response} with the given information
         *
         * @param status a http status representing the state of the action
         * @param error an error message if there was an issue, null otherwise
         * @param data the resulting data from the action being performed
         */
        private Response(final int status, @Nullable final String error, @Nullable final T data) {
            this.status = status;
            this.error = error;
            this.data = data;
        }

        /**
         * Returns the status code of the action.
         *
         * @return the status code of the action
         */
        public int getStatus() {
            return status;
        }

        /**
         * Returns the error message from the action if there was a problem.
         *
         * @return the error message from the action if there was a problem, null otherwise
         */
        @Nullable
        public String getError() {
            return error;
        }

        /**
         * Returns the resulting data from the action being performed.
         *
         * @return the resulting data from the action being performed, null otherwise
         */
        @Nullable
        public T getData() {
            return data;
        }

        @NonNull
        @Override
        public String toString() {
            return "Response{" +
                    "status=" + status +
                    ", error='" + error + '\'' +
                    ", data=" + data +
                    '}';
        }

        /**
         * Builder for creating instances of {@link Response} with a status code and any additional
         * information required for the callback being notified.
         *
         * @param <T> the type of the resulting data from the action being performed
         */
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        public static class Builder<T> {

            private int status;
            @Nullable
            private String error;
            @Nullable
            private T data;

            /**
             * Constructs an instance of this {@link Builder}
             *
             * @param status the status code of the action being performed
             */
            public Builder(int status) {
                this.status = status;
            }

            /**
             * Adds an error message to the response builder
             *
             * @param error the error message to be added to the {@link Response}
             * @return this {@link Builder}
             */
            @NonNull
            public Builder<T> withError(@Nullable String error) {
                this.error = error;
                return this;
            }

            /**
             * Adds data to the response builder
             *
             * @param data the data to be added to the {@link Response}
             * @return this {@link Builder}
             */
            @NonNull
            public Builder<T> withData(@Nullable T data) {
                this.data = data;
                return this;
            }

            /**
             * Builds the {@link Response} with the provided information
             *
             * @return an instance of {@link Response}
             */
            @NonNull
            public Response<T> build() {
                return new Response<>(status, error, data);
            }

        }
    }

    /**
     * Invoke the {@link ClarabridgeChatCallback} with the results of the action
     *
     * @param response an instance of {@link Response} containing the results of the action
     */
    void run(@NonNull final Response<T> response);
}
