package io.github.ulisse1996.jaorm.spatial;

public class Geography {

    private double latitude;
    private double longitude;
    private int srid;

    public Geography() {
        this.latitude = 0;
        this.longitude = 0;
    }

    public Geography(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Geography(double latitude, double longitude, int srid) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.srid = srid;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getSrid() {
        return srid;
    }

    public void setSrid(int srid) {
        this.srid = srid;
    }
}
