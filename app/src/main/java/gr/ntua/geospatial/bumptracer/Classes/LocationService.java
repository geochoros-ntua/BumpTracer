package gr.ntua.geospatial.bumptracer.Classes;
/**
 * This is the class to handle location service
 * It is executed as a service, so make sure it still works on the background no matter what
 * Might not be necessary but it will run for sure
 * and also helps to keep location functionality together as a separate class
 * Author: p.tsagkis@gmail.com
 */

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.text.SimpleDateFormat;

import gr.ntua.geospatial.bumptracer.MainActivity;


/**
 * Here is the service class
 * implementing LocationListener as well as google api client
 * Google api client is implemented to grant permissions and
 * to show the default dialog to enable GPS
 */
public class LocationService extends Service implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    //declare some properties here
    private static final long LOCATION_REFRESH_TIME = 0; //in msecs || 0 means get location every 1 second
    private static final float LOCATION_REFRESH_DISTANCE = 0; //meters
    private static final String TAG = "LocationService";
    private GoogleApiClient mGoogleApiClient;
    private LocationManager locationManager;
    //declare it public so we can use in our main activity
    public static Location latestLocation;


    /**
     * This is the start command. It is executed on fab1 click button
     * inside out main activity
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {

        //  Logging.i(CLAZZ, "onHandleIntent", "invoked");
        Log.d(TAG,"action====="+intent.getAction());


            Log.d(TAG,"start listening");
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "no permission granted");

            } else {
                askToEnableLocServices(MainActivity.con);
            }



        return START_STICKY;

    }

    /**
     * TODO test
     * Calling on destroy. destroy the location service.
     * No test at all, ...... test it!
     */
    @Override
    public void onDestroy(){
        Log.d(TAG,"on destroy called");
        //locationManager.removeUpdates(this);
        locationManager = null;
    }

    /**
     * Just necessary to implement a Service
     * @param intent
     * @return
     */
    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }


    /**
     * No usage at all. Just Dummy.
     * @param location
     */
    public void onLocationChanged(Location location) {
        //no need to handle . Just an obligated listener
        //this.location = location;
        //listen = this;
        Log.d(TAG, "onLocationChanged=="+location);
        // TODO this is where you'd do something like context.sendBroadcast()
    }


    /**
     * This updates our  current location --> latestLocation @public
     *
     */
    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult (LocationResult locationResult){
            for (Location location : locationResult.getLocations()) {
                // Update UI with location data
                Log.d(TAG, locationResult+"");
                SimpleDateFormat time_formatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss.SSS");
                String current_time_str = time_formatter.format(System.currentTimeMillis());
                String location_time_str = time_formatter.format(location.getTime());
                //update it
                latestLocation = location;
                //MainActivity.tv.setText("Lacation = " +location.getLatitude()+","+location.getLongitude()+", time==="+current_time_str);


            }
        }
    };


    /**
     * ask user to enable location services.
     * Checks for permissions as well
     * Build the google api client and let the listener (further down) to show the dialog if needed
     * @param con
     */

    public void askToEnableLocServices(Context con){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d(TAG,"PackageManager.PERMISSION_GRANTED====" + PackageManager.PERMISSION_GRANTED);
            Log.d(TAG,"android.Manifest.permission.ACCESS_FINE_LOCATION)=====" + ContextCompat.checkSelfPermission(con, Manifest.permission.ACCESS_FINE_LOCATION));
            if (ContextCompat.checkSelfPermission(con, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                //build google api client
                mGoogleApiClient = new GoogleApiClient.Builder(MainActivity.con)
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .build();
                mGoogleApiClient.connect();

            } else {
                Log.d(TAG, "no permissions to get the location!!!!!");
                Toast.makeText(con, "permission denide", Toast.LENGTH_SHORT).show();
            }
        } else {
          //for older versions of Android...........TODO
            Log.d(TAG, "THIS IS AN OLDER VERSION!!!!!");
        }
    }


    /**
     * This is executed while connected to google API client
     * Set up and start the location request and interval
     * Check for gps on. If not, ask with the default dialog to enable
     * @param bundle
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //set up the location request. Do not start yet!
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(LOCATION_REFRESH_TIME);
        locationRequest.setFastestInterval(LOCATION_REFRESH_TIME);
        locationRequest.setSmallestDisplacement(LOCATION_REFRESH_DISTANCE);


        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true); //this is the key ingredient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> result = settingsClient.checkLocationSettings(builder.build());
        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {

            @Override
            public void onComplete(Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    // All location settings are satisfied. The client can initialize location
                    // requests here.

                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                resolvable.startResolutionForResult(
                                        (Activity) MainActivity.con,
                                        MainActivity.REQUEST_LOCATION);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            } catch (ClassCastException e) {
                                // Ignore, should be an impossible error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            break;
                    }
                }
            }
        });

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //start the location request
            FusedLocationProviderClient fusedProvider = LocationServices.getFusedLocationProviderClient(MainActivity.con);
            fusedProvider.requestLocationUpdates(locationRequest, mLocationCallback, null);
           }
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


}