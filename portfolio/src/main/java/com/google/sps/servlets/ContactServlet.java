package com.google.sps.servlets;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Sends contact information to the main recipient.
 */
@WebServlet("/contact-data")
public class ContactServlet extends HttpServlet {
  private static final String FIRST_NAME_PARAMETER_KEY = "first-name";
  private static final String LAST_NAME_PARAMETER_KEY = "last-name";
  private static final String TITLE_PROPERTY_KEY = "title";
  private static final String MESSAGE_PROPERTY_KEY = "message";
  private static final String RECIPIENT_EMAIL = "bryantriana@google.com";
  private static final int MAX_NAME_CHARS = 32;
  private static final int MAX_TITLE_CHARS = 64;
  private static final int MAX_MESSAGE_CHARS = 1024;

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();

    if (!userService.isUserLoggedIn()) {
      return;
    }

    String firstName = request.getParameter(FIRST_NAME_PARAMETER_KEY);
    String lastName = request.getParameter(LAST_NAME_PARAMETER_KEY);
    String senderEmail = userService.getCurrentUser().getEmail();
    String title = request.getParameter(TITLE_PROPERTY_KEY);
    String message = request.getParameter(MESSAGE_PROPERTY_KEY);

    PrintWriter printWriter = response.getWriter();

    if (!areInputFieldsValid(printWriter, firstName, lastName, title, message)) {
      return;
    }

    Properties properties = new Properties();
    Session session = Session.getDefaultInstance(properties, null);

    try {
      Message emailMessage = new MimeMessage(session);
      emailMessage.setFrom(new InternetAddress(senderEmail));
      emailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(RECIPIENT_EMAIL));
      emailMessage.setSubject(firstName + " " + lastName + " - " + title);
      emailMessage.setText(message);

      Transport.send(emailMessage);

      response.setContentType("text/html");

      printWriter.println("<h1>Message sent successfully!</h1>");
      printWriter.println("<p>I'll get back to you as soon as possible.</p>");
    } catch (AddressException e) {
      e.printStackTrace();

      printWriter.println("<h1>Email address could not be parsed! Please try again.</h1>");
    } catch (MessagingException e) {
      e.printStackTrace();

      printWriter.println("<h1>Message was not able to be sent! Please try again.</h1>");
    }
  }

  /**
   * Checks if the input fields from the request parameter exceed their maximum limit.
   *
   * @param printWriter - the PrintWriter used to print output to the user explaining the possible
   *     validation mistakes
   * @param firstName First name field given by the HTTP request
   * @param lastName Last name field given by the HTTP request
   * @param title Title field given by the HTTP request
   * @param message Message field given by the HTTP request
   * @return boolean value that is true if all the input fields are valid, otherwise false
   */
  private static boolean areInputFieldsValid(
      PrintWriter printWriter, String firstName, String lastName, String title, String message) {
    boolean isInputValid = true;

    if (firstName.length() > MAX_NAME_CHARS) {
      printWriter.println(
          "<h1>First name cannot exceed more than " + MAX_NAME_CHARS + " characters!</h1>");
      isInputValid = false;
    }
    if (lastName.length() > MAX_NAME_CHARS) {
      printWriter.println(
          "<h1>Last name cannot exceed more than " + MAX_NAME_CHARS + " characters!</h1>");
      isInputValid = false;
    }
    if (title.length() > MAX_TITLE_CHARS) {
      printWriter.println(
          "<h1>Title cannot exceed more than " + MAX_TITLE_CHARS + " characters!</h1>");
      isInputValid = false;
    }
    if (message.length() > MAX_MESSAGE_CHARS) {
      printWriter.println(
          "<h1>Message cannot exceed more than " + MAX_MESSAGE_CHARS + " characters!</h1>");
      isInputValid = false;
    }

    return isInputValid;
  }
}
