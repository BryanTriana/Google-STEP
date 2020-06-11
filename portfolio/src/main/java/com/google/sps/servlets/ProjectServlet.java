package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Stores and fetches project ideas requests.
 */
@WebServlet("/project-data")
public class ProjectServlet extends HttpServlet {
  private static final String PROJECT_KIND = "project";
  private static final String PROJECT_TYPE_PROPERTY = "projectType";
  private static final String VOTE_COUNT_PROPERTY = "voteCount";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query(PROJECT_KIND);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery queryResults = datastore.prepare(query);

    Map<String, Long> projectVoteMap = new HashMap<>();

    for (Entity projectEntity : queryResults.asIterable()) {
      String projectType = (String) projectEntity.getProperty(PROJECT_TYPE_PROPERTY);
      Long voteCount = (Long) projectEntity.getProperty(VOTE_COUNT_PROPERTY);

      projectVoteMap.put(projectType, voteCount);
    }

    response.setContentType("application/json");
    response.getWriter().println(new Gson().toJson(projectVoteMap));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String project = request.getParameter(PROJECT_KIND);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Query query =
        new Query(PROJECT_KIND)
            .setFilter(new FilterPredicate(PROJECT_TYPE_PROPERTY, FilterOperator.EQUAL, project));
    PreparedQuery queryResults = datastore.prepare(query);

    Entity projectEntity = queryResults.asSingleEntity();

    if (projectEntity == null) {
      projectEntity = new Entity(PROJECT_KIND, project);
      projectEntity.setProperty(PROJECT_TYPE_PROPERTY, project);
      projectEntity.setProperty(VOTE_COUNT_PROPERTY, 1);
    } else {
      projectEntity.setProperty(
          VOTE_COUNT_PROPERTY, ((Long) projectEntity.getProperty(VOTE_COUNT_PROPERTY)) + 1);
    }

    datastore.put(projectEntity);

    response.sendRedirect("/index.html");
  }
}
