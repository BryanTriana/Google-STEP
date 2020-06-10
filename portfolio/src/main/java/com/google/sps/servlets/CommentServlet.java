package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.sps.data.Comment;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Stores and fetches comment submission requests.
 */
@WebServlet("/comment-data")
public class CommentServlet extends HttpServlet {
  private static final int DEFAULT_COMMENT_LIMIT = 10;
  private static final int MAX_COMMENT_CHARS = 320;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    int commentLimit = getCommentLimit(request);

    Query query = new Query(CommentKeys.COMMENT_KIND)
                      .addSort(CommentKeys.TIMESTAMP_MILLIS_PROPERTY, SortDirection.DESCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery queryResults = datastore.prepare(query);

    List<Comment> comments = new ArrayList<>();

    for (Entity commentEntity :
        queryResults.asIterable(FetchOptions.Builder.withLimit(commentLimit))) {
      String email = (String) commentEntity.getProperty(CommentKeys.EMAIL_PROPERTY);
      String name = (String) commentEntity.getProperty(CommentKeys.NAME_PROPERTY);
      String message = (String) commentEntity.getProperty(CommentKeys.MESSAGE_PROPERTY);
      long timestampMillis =
          (long) commentEntity.getProperty(CommentKeys.TIMESTAMP_MILLIS_PROPERTY);

      comments.add(new Comment(email, name, message, timestampMillis));
    }

    response.setContentType("application/json");
    response.getWriter().println(new Gson().toJson(comments));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();

    if (!userService.isUserLoggedIn()) {
      return;
    }

    String message = request.getParameter(CommentKeys.MESSAGE_PROPERTY);

    if (!isMessageValid(message)) {
      return;
    }

    User user = userService.getCurrentUser();

    String email = user.getEmail();
    String name = NicknameFinder.getNickname(user.getUserId()).orElse(user.getUserId());
    long timestampMillis = System.currentTimeMillis();

    Entity commentEntity = new Entity(CommentKeys.COMMENT_KIND);
    commentEntity.setProperty(CommentKeys.EMAIL_PROPERTY, email);
    commentEntity.setProperty(CommentKeys.NAME_PROPERTY, name);
    commentEntity.setProperty(CommentKeys.MESSAGE_PROPERTY, message);
    commentEntity.setProperty(CommentKeys.TIMESTAMP_MILLIS_PROPERTY, timestampMillis);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);

    response.sendRedirect("/blog.html");
  }

  /**
   * Retrieves the comment limit parameter from an HTTP request and checks if its valid.
   *
   * @param request The HTTP request that holds the comment limit parameter
   * @return The integer value for the comments limit, if the value found is invalid then
   *         it returns a default value.
   */
  private static int getCommentLimit(HttpServletRequest request) {
    String commentLimitString = request.getParameter(CommentKeys.COMMENT_LIMIT_PROPERTY);

    int commentLimit;
    try {
      commentLimit = Integer.parseInt(commentLimitString);
    } catch (NumberFormatException e) {
      System.err.println("Could not convert to int: " + commentLimitString);
      return DEFAULT_COMMENT_LIMIT;
    }

    if (commentLimit < 1 || commentLimit > 10) {
      System.err.println("Comment limit cannot be outside the range [1-10]: " + commentLimit);
      return DEFAULT_COMMENT_LIMIT;
    }

    return commentLimit;
  }

  /**
   * Checks if the message in the comment is of valid length.
   *
   * @return boolean value of true if the message is valid, otherwise false.
   */
  private static boolean isMessageValid(String message) {
    return !message.isEmpty() && message.length() <= MAX_COMMENT_CHARS;
  }
}
