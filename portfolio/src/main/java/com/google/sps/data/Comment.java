package com.google.sps.data;

/**
 * Holds relevant information for comments stored on the server.
 */
public class Comment {
  private final String email;
  private final String name;
  private final String message;
  private final long timestampMillis;

  public Comment(String email, String name, String message, long timestampMillis) {
    this.email = email;
    this.name = name;
    this.message = message;
    this.timestampMillis = timestampMillis;
  }

  public String getEmail() {
    return email;
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
