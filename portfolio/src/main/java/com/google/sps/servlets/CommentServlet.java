package com.google.sps.servlets;

import com.google.sps.data.Comment;
import com.google.gson.Gson;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.PreparedQuery;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Stores and fetches comment submission requests.
 */
@WebServlet("/comment-data")
public class CommentServlet extends HttpServlet {
    private static final String COMMENT_ENTITY_KEY = "Comment";
    private static final String NAME_PROPERTY_KEY = "name";
    private static final String MESSAGE_PROPERTY_KEY = "message";
    private static final String TIMESTAMP_PROPERTY_KEY = "timestamp";

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Query query = new Query(COMMENT_ENTITY_KEY).addSort(TIMESTAMP_PROPERTY_KEY, SortDirection.DESCENDING);

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        PreparedQuery queryResults = datastore.prepare(query);

        List<Comment> comments = new ArrayList<>();

        for(Entity commentEntity : queryResults.asIterable()) {
            String name = (String) commentEntity.getProperty(NAME_PROPERTY_KEY);
            String message = (String) commentEntity.getProperty(MESSAGE_PROPERTY_KEY);
            long timestamp = (long) commentEntity.getProperty(TIMESTAMP_PROPERTY_KEY);

            comments.add(new Comment(name, message, timestamp));
        }
        
        response.setContentType("application/json");
        response.getWriter().println(new Gson().toJson(comments));
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String name = request.getParameter(NAME_PROPERTY_KEY);
        String message = request.getParameter(MESSAGE_PROPERTY_KEY);
        long timestamp = System.currentTimeMillis();

        Entity commentEntity = new Entity(COMMENT_ENTITY_KEY);
        commentEntity.setProperty(NAME_PROPERTY_KEY, name);
        commentEntity.setProperty(MESSAGE_PROPERTY_KEY, message);
        commentEntity.setProperty(TIMESTAMP_PROPERTY_KEY, timestamp);

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(commentEntity);
       
        response.sendRedirect("/blog.html");
    }
}
