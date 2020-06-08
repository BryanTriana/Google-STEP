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
 * Handles login requests from users.
 */
@WebServlet("/login-data")
public class LoginServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();

    response.setContentType("application/json");
    response.getWriter().println(new Gson().toJson(userService.isUserLoggedIn()));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String redirectURL = request.getParameter("redirectURL");

    UserService userService = UserServiceFactory.getUserService();

    String loginURL = userService.createLoginURL(redirectURL);

    response.sendRedirect(loginURL);
  }
}
