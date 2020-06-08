package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Responsible for deleting comments upon request.
 */
@WebServlet("/delete-comment-data")
public class DeleteCommentServlet extends HttpServlet {
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Query query = new Query(CommentKeys.COMMENT_KIND);
    PreparedQuery queryResults = datastore.prepare(query);

    TransactionOptions options = TransactionOptions.Builder.withXG(true);
    Transaction transaction = datastore.beginTransaction(options);

    try {
      for (Entity commentEntity : queryResults.asIterable()) {
        datastore.delete(transaction, commentEntity.getKey());
      }

      transaction.commit();
    } finally {
      if (transaction.isActive()) {
        transaction.rollback();
      }
    }

    response.sendRedirect("/blog.html");
  }
}
