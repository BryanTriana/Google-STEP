package com.google.sps.servlets;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handles login and logout requests from users.
 */
@WebServlet("/login-data")
public class LoginServlet extends HttpServlet {
  /**
   * The response to the GET request contains a boolean value that serves as a way to know if the
   * user is logged in or not.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();

    response.setContentType("application/json");
    response.getWriter().println(new Gson().toJson(userService.isUserLoggedIn()));
  }

  /**
   * The POST request calls Google's Users API to ask the user to either login or logout given their
   * current login status.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String redirectURL = request.getParameter("redirectURL");

    UserService userService = UserServiceFactory.getUserService();

    if (userService.isUserLoggedIn()) {
      response.sendRedirect(userService.createLogoutURL(redirectURL));
    } else {
      response.sendRedirect(userService.createLoginURL(redirectURL));
    }
  }
}
