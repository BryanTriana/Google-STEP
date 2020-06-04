package com.google.sps.data;

/**
 * Holds relevant information for comments stored on the server.
 */
public class Comment {
  private final String name;
  private final String message;
  private final long timestampMillis;

  public Comment(String name, String message, long timestampMillis) {
    this.name = name;
    this.message = message;
    this.timestampMillis = timestampMillis;
  }

  public String getName() {
    return name;
  }

  public String getMessage() {
    return message;
  }

  public long getTimestampMillis() {
    return timestampMillis;
  }
}
