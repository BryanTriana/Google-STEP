package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
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

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    int commentLimit = getCommentLimit(request);

    Query query = new Query(CommentServletKeys.COMMENT_KIND)
                      .addSort(CommentServletKeys.TIMESTAMP_MILLIS_PROPERTY, SortDirection.DESCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery queryResults = datastore.prepare(query);

    List<Comment> comments = new ArrayList<>();

    for (Entity commentEntity :
        queryResults.asIterable(FetchOptions.Builder.withLimit(commentLimit))) {
      String name = (String) commentEntity.getProperty(CommentServletKeys.NAME_PROPERTY);
      String message = (String) commentEntity.getProperty(CommentServletKeys.MESSAGE_PROPERTY);
      long timestampMillis = (long) commentEntity.getProperty(CommentServletKeys.TIMESTAMP_MILLIS_PROPERTY);

      comments.add(new Comment(name, message, timestampMillis));
    }

    response.setContentType("application/json");
    response.getWriter().println(new Gson().toJson(comments));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String name = request.getParameter(CommentServletKeys.NAME_PROPERTY);
    String message = request.getParameter(CommentServletKeys.MESSAGE_PROPERTY);
    long timestampMillis = System.currentTimeMillis();

    Entity commentEntity = new Entity(CommentServletKeys.COMMENT_KIND);
    commentEntity.setProperty(CommentServletKeys.NAME_PROPERTY, name);
    commentEntity.setProperty(CommentServletKeys.MESSAGE_PROPERTY, message);
    commentEntity.setProperty(CommentServletKeys.TIMESTAMP_MILLIS_PROPERTY, timestampMillis);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);

    response.sendRedirect("/blog.html");
  }

  /**
   * Retrieves the comment limit parameter from an HTTP request and checks if its valid.
   *
   * @param request - The HTTP request that holds the comment limit parameter
   * @return - The integer value for the comments limit, if the value found is invalid then
   *           it returns a default value
   */
  private static int getCommentLimit(HttpServletRequest request) {
    String commentLimitString = request.getParameter(CommentServletKeys.COMMENT_LIMIT_PROPERTY);

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
}
