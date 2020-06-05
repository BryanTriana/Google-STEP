package com.google.sps.servlets;

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
  private static final String EMAIL_PROPERTY_KEY = "email";
  private static final String TITLE_PROPERTY_KEY = "title";
  private static final String MESSAGE_PROPERTY_KEY = "message";
  private static final String RECIPIENT_EMAIL = "bryantriana@google.com";

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String firstName = request.getParameter(FIRST_NAME_PARAMETER_KEY);
    String lastName = request.getParameter(LAST_NAME_PARAMETER_KEY);
    String senderEmail = request.getParameter(EMAIL_PROPERTY_KEY);
    String title = request.getParameter(TITLE_PROPERTY_KEY);
    String message = request.getParameter(MESSAGE_PROPERTY_KEY);

    PrintWriter printWriter = response.getWriter();

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
}
