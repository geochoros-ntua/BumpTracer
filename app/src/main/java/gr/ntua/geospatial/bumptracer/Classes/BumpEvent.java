package gr.ntua.geospatial.bumptracer.Classes;

public class BumpEvent {
    public float gForce = 0;
    public int count = 0;
    public double lat = 0;
    public double lon = 0;

    //constructor
    public BumpEvent(float gForce, int count, double lat, double lon) {
        this.gForce = gForce;
        this.count = count;
        this.lat = lat;
        this.lon = lon;

    }
}
