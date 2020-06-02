package com.google.sps.data;

/**
 * Holds relevant information for comments stored on the server
 */
public class Comment {
    private String name;
    private String message;
    private long timestamp;

    public Comment(String name, String message, long timestamp) {
        this.name = name;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
