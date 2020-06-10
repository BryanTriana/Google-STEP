package com.google.sps.data;

/**
 * Holds information for Google Maps Markers.
 */
public class Marker {
  private float latitude;
  private float longitude;

  public Marker(float latitude, float longitude) {
    this.latitude = latitude;
    this.longitude = longitude;
  }

  public float getLatitude() {
    return latitude;
  }

  public float getLongitude() {
    return longitude;
  }
}
