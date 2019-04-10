package gr.ntua.geospatial.bumptracer;

import android.Manifest;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.util.ArrayList;

import gr.ntua.geospatial.bumptracer.Classes.BumpDetector;
import gr.ntua.geospatial.bumptracer.Classes.BumpEvent;
import gr.ntua.geospatial.bumptracer.Utils.Utils;

public class MainActivity extends AppCompatActivity
        implements  GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = "MainActivity";
    private static final long LOCATION_REFRESH_TIME = 1; //in secs
    private static final float LOCATION_REFRESH_DISTANCE = 1; //meters
    public static final int REQUEST_LOCATION = 197;
    static GoogleApiClient googleApiClient;
    LocationManager locationManager;
    LocationRequest locationRequest;
    Location latestLocation = null;
    int latestSpeed = 0;



    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private BumpDetector mShakeDetector;
    public Context con;
    private ArrayList<BumpEvent> bumpEvents = new ArrayList<BumpEvent>();
    TextView tv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        con = this;
        tv = (TextView) findViewById(R.id.infotxt);
        //request location permisssions on start up
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);


        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab1 = (FloatingActionButton) findViewById(R.id.fab1);
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                askToEnableLocServices(con);
                Toast.makeText(con, "start listening", Toast.LENGTH_SHORT).show();

                //register the listener to trace bump events
                mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);

            }
        });

        FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(con, "stop listening", Toast.LENGTH_SHORT).show();
                //register the listener to trace bump events
                mSensorManager.unregisterListener(mShakeDetector);
            }
        });


        // bumpDetector initialization
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new BumpDetector();
        mShakeDetector.setOnShakeListener(new BumpDetector.OnShakeListener() {

            @Override
            public void onShake(int count, float gForce) {
                /*
                 * just any actions when shake event exist
                 */

                Toast.makeText(con, "shake event detected count====" + count + ",  gForce==" + gForce, Toast.LENGTH_SHORT).show();
               // Location loc = getLastKnownLocation();
                Log.d(TAG, "LOCATIONNNNNNNNNNNNNNNNNNNNNNN++++++++++++++++++ ===" + latestLocation);

                bumpEvents.add(new BumpEvent(gForce, count, 0, 0));

                tv.setText("Number of Bumps recorded = " + bumpEvents.size() + "latestt speed="+ latestSpeed + " last location ===" + latestLocation);


            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "on resume called!!!!!!!");
        //do nthing
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.d(TAG,"request permissions result fired");
        if (requestCode == 1) {
            for (String s : permissions) {
                if (s.equals("android.permission.ACCESS_FINE_LOCATION")) {
                    if (grantResults[0] != -1) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                            if (googleApiClient == null) {
                                buildGoogleApiClient();
                            }
                        }
                    }
                }
            }
        }
    }


//----------------------------------location methods --------------------------------------------------------------------//
    /**
     * ask user to enable location services
     * @param con
     */
    protected void askToEnableLocServices(Context con){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d(TAG,"PackageManager.PERMISSION_GRANTED====" + PackageManager.PERMISSION_GRANTED);
            Log.d(TAG,"android.Manifest.permission.ACCESS_FINE_LOCATION)=====" + ContextCompat.checkSelfPermission(con, Manifest.permission.ACCESS_FINE_LOCATION));
            if (ContextCompat.checkSelfPermission(con, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();

            } else {
                Log.d(TAG, "no permissions to get the location!!!!!");
                Toast.makeText(con, "permission denide", Toast.LENGTH_SHORT).show();
            }
        } else {
            buildGoogleApiClient();

        }
    }
    /**
     * build the api client
     */
    public synchronized void buildGoogleApiClient() {
        Log.d(TAG, "google api client initialised");
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();

    }


    /**
     * this is a void. When connected to location services
     * configure the location request 
     * @param bundle
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(LOCATION_REFRESH_TIME*1000);
        locationRequest.setFastestInterval(LOCATION_REFRESH_TIME*1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        Log.d(TAG, "setting interval to " + LOCATION_REFRESH_TIME + " secs!!!");
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
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
                            status.startResolutionForResult(
                                    MainActivity.this,
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
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,LOCATION_REFRESH_TIME*1000,LOCATION_REFRESH_DISTANCE,this);

        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG,"onLocationChanged loc changeD");
        latestLocation = location;
        if (location==null){
            // if you can't get speed because reasons :)
            latestSpeed = 0;
        }
        else{
            //int speed=(int) ((location.getSpeed()) is the standard which returns meters per second. In this example i converted it to kilometers per hour

            int speed=(int) ((location.getSpeed()*3600)/1000);
            latestSpeed = speed;

        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
