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
  private static int MAX_NICKNAME_CHARS = 32;

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

    String nickname =
        NicknameFinder.getNickname(userService.getCurrentUser().getUserId()).orElse(null);

    response.setContentType("application/json");
    response.getWriter().println(new Gson().toJson(nickname));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();

    if (!userService.isUserLoggedIn()) {
      return;
    }

    String nickname = request.getParameter(UserKeys.NICKNAME_PROPERTY);

    if (!isNicknameValid(nickname)) {
      return;
    }

    String id = userService.getCurrentUser().getUserId();

    Entity userDataEntity = new Entity(UserKeys.USER_KIND, id);
    userDataEntity.setProperty(UserKeys.ID_PROPERTY, id);
    userDataEntity.setProperty(UserKeys.NICKNAME_PROPERTY, nickname);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(userDataEntity);

    response.sendRedirect("/blog.html");
  }

  /**
   * Checks if the nickname given by the user is valid.
   *
   * @return boolean value of true if the nickname is valid, otherwise false
   */
  private static boolean isNicknameValid(String nickname) {
    return nickname != null && !nickname.isEmpty() && nickname.length() <= MAX_NICKNAME_CHARS;
  }
}
