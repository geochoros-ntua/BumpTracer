package gr.ntua.geospatial.bumptracer.Classes;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;

import android.location.LocationManager;
import android.os.Binder;
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
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.LocationListener;

import gr.ntua.geospatial.bumptracer.MainActivity;
import gr.ntua.geospatial.bumptracer.Utils.Utils;


public class LocationService extends Service implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private LocationManager locationManager;
    public Location location;
    public LocationListener listen;
    GoogleApiClient mGoogleApiClient;

    private static final long LOCATION_REFRESH_TIME = 0; //in secs
    private static final float LOCATION_REFRESH_DISTANCE = 1; //meters
    private static final String TAG = "LocationService";

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {

        //  Logging.i(CLAZZ, "onHandleIntent", "invoked");
        Log.d(TAG,"action====="+intent.getAction());


            Log.d(TAG,"start listening");
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "should ask permission");


               // locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, listen);
            } else {
                askToEnableLocServices(MainActivity.con);
            }



        return START_STICKY;

    }

    @Override
    public void onDestroy(){
        Log.d(TAG,"on destroy called");
        //locationManager.removeUpdates(this);
        locationManager = null;
    }





    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    public void onLocationChanged(Location location) {
        this.location = location;
        listen = this;
        Log.d(TAG, "onLocationChanged");
        // TODO this is where you'd do something like context.sendBroadcast()
    }



    public void onProviderDisabled(final String provider) {
        Log.d(TAG, "onProviderDisabled");
    }

    public void onProviderEnabled(final String provider) {
        Log.d(TAG, "onProviderEnabled");
    }


    /**
     * ask user to enable location services
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
            //buildGoogleApiClient();

        }
    }

    LocationRequest locationRequest;
    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult (LocationResult locationResult){
            for (Location location : locationResult.getLocations()) {
                // Update UI with location data
                Log.d(TAG, locationResult+"");

            }
        }
    };

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(LOCATION_REFRESH_TIME * 1000);
        locationRequest.setFastestInterval(LOCATION_REFRESH_TIME * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true); //this is the key ingredient
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                //final LocationSettingsStates loc = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        //...
                        Log.d(TAG,"THIS IS NICE!!!!!!");

                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.d(TAG,"THIS IS NTO VERY NICE. USER MAST ENABLE IT!!!!!!");
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            Log.d(TAG,"starting resolution");
                            status.startResolutionForResult(
                                    (Activity) MainActivity.con,
                                    1000);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.d(TAG,"THIS IS A PROBLEM. NOT MUCH WE CAN DO!!!!!!");
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        //...
                        break;
                }
            }
        });


        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            FusedLocationProviderClient fusedProvider = LocationServices.getFusedLocationProviderClient(MainActivity.con);
            fusedProvider.requestLocationUpdates(locationRequest, mLocationCallback, null);
           // LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, listen);
        }
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}