package com.google.sps.servlets;

import com.google.gson.Gson;
import com.google.sps.data.Marker;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handles marker data for Google Maps.
 */
@WebServlet("/maps-data")
public class MapsServlet extends HttpServlet {
  private List<Marker> markers;

  @Override
  public void init() {
    markers = new ArrayList<>();

    Scanner scanner =
        new Scanner(getServletContext().getResourceAsStream("/WEB-INF/travel-locations.csv"));

    while (scanner.hasNextLine()) {
      String line = scanner.nextLine();
      String[] tokens = line.split(",");

      if (tokens.length != 2) {
        System.err.println(
            "Line does not contain two comma-separated elements describing latitude and longitude: "
            + line);
        continue;
      }

      float latitude, longitude;
      try {
        latitude = Float.parseFloat(tokens[0]);
      } catch (NumberFormatException e) {
        System.err.println("Failed to parse latitude string into float!");
        continue;
      }

      try {
        longitude = Float.parseFloat(tokens[1]);
      } catch (NumberFormatException e) {
        System.err.println("Failed to parse longitude string into float!");
        continue;
      }

      markers.add(new Marker(latitude, longitude));
    }

    scanner.close();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    response.getWriter().println(new Gson().toJson(markers));
  }
}
