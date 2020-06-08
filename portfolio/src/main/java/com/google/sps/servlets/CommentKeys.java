package com.google.sps.servlets;

/**
 * Holds values for the keys used to access Comment Entities in the Datastore Service.
 */
final class CommentKeys {
  static final String COMMENT_KIND = "Comment";
  static final String EMAIL_PROPERTY = "email";
  static final String NAME_PROPERTY = "name";
  static final String MESSAGE_PROPERTY = "message";
  static final String TIMESTAMP_MILLIS_PROPERTY = "timestampMillis";
  static final String COMMENT_LIMIT_PROPERTY = "commentLimit";

  private CommentKeys() {}
}
