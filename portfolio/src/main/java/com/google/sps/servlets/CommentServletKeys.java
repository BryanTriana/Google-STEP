package com.google.sps.servlets;

/**
 * Holds the values for the keys to access elements in the Datastore Service.
 */
final class CommentServletKeys {
  static final String COMMENT_KIND = "Comment";
  static final String NAME_PROPERTY = "name";
  static final String MESSAGE_PROPERTY = "message";
  static final String TIMESTAMP_MILLIS_PROPERTY = "timestampMillis";
  static final String COMMENT_LIMIT_PROPERTY = "commentLimit";

  private CommentServletKeys() {}
}
