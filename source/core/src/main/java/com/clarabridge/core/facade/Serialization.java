package com.clarabridge.core.facade;

/**
 * Describes the operations for transforming objects to and from data
 */
public interface Serialization {

    /**
     * Serialize the given {@link Object} into a {@link String}
     *
     * @param object the {@link Object} to serialize
     * @return the serialised object
     */
    String serialize(Object object);

    /**
     * Deserialize the given {@link String} data into an object of type {@link T}
     *
     * @param data the data to be deserialized
     * @param clazz specifies the type of of the deserialized {@link Object}
     * @param <T> the type being returned
     * @return an {@link Object} of type {@link T}
     */
    <T> T deserialize(String data, Class<T> clazz);

}
