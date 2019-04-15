package gr.ntua.geospatial.bumptracer.Classes;

import android.location.Location;

/**
 * This is the class to represent the object of bumps
 * Represents the values to hold
 * and any methods
 */
public class BumpEvent {
    //gforce
    public float gForce = 0;
    //count of shakes within 500ms (go to BumpDetector.SHAKE_SLOP_TIME_MS) from first shake.
    //TODO this needs further investigation.
    //Just hold it for the time being, should be useful.
    public int count = 0;
    //the location obj. Maybe too heavy?
    public Location loc = null;

    //constructor
    public BumpEvent(float gForce, int count, Location loc) {
        this.gForce = gForce;
        this.count = count;
        this.loc = loc;


    }
    //Place here any methods
}
