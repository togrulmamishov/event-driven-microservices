package com.togrul.twitter_to_kafka_service.exception;

public class TwitterToKafkaException extends RuntimeException {
    public TwitterToKafkaException(String message) {
        super(message);
    }

    public TwitterToKafkaException(String message, Throwable cause) {
        super(message, cause);
    }
}
