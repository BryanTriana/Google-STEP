package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Stores and fetches user nicknames.
 */
@WebServlet("/nickname-data")
public class NicknameServlet extends HttpServlet {
  /**
   * The response of the GET request contains the nickname of the current user if they are logged
   * in.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();

    if (!userService.isUserLoggedIn()) {
      return;
    }

    String nickname = NicknameFinder.getNickname(userService.getCurrentUser().getUserId());

    response.setContentType("application/json");
    response.getWriter().println(new Gson().toJson(nickname));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();

    if (!userService.isUserLoggedIn()) {
      return;
    }

    String id = userService.getCurrentUser().getUserId();
    String nickname = request.getParameter(UserKeys.NICKNAME_PROPERTY);

    Entity userDataEntity = new Entity(UserKeys.USER_KIND, id);
    userDataEntity.setProperty(UserKeys.ID_PROPERTY, id);
    userDataEntity.setProperty(UserKeys.NICKNAME_PROPERTY, nickname);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(userDataEntity);

    response.sendRedirect("/blog.html");
  }
}
