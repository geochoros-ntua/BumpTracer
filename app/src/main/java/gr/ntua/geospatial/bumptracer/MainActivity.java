package gr.ntua.geospatial.bumptracer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import gr.ntua.geospatial.bumptracer.Classes.BumpDetector;
import gr.ntua.geospatial.bumptracer.Classes.BumpEvent;
import gr.ntua.geospatial.bumptracer.Classes.LocationService;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final long LOC_SHAKE_TIME_DIFF = 1; //in secs
    public static final int REQUEST_LOCATION = 197;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private BumpDetector mShakeDetector;
    public static Context con;
    private ArrayList<BumpEvent> bumpEvents = new ArrayList<BumpEvent>();
    public static TextView tv;
    ServiceConnection SCon;


    /**
     * The main Activity holding the UI.
     * 2 fab buttons two start/stop shake and location services
     * A dummy toolbar
     * and a piece of logic for the shake event
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        con = this;
        tv = (TextView) findViewById(R.id.infotxt);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Class<?> LocationServiceMonitor = LocationService.class;
        //Request location permissions on start up
         ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_LOCATION);

        //set the button actions
        FloatingActionButton fab1 = (FloatingActionButton) findViewById(R.id.fab1);
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(con, "start listening", Toast.LENGTH_SHORT).show();
                //register the listener to trace bump events
                mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
                //and start the location service
                con.startService(new Intent(con, LocationServiceMonitor));

            }
        });

        FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(con, "stop listening", Toast.LENGTH_SHORT).show();
                //unregister the listener for bump events
                mSensorManager.unregisterListener(mShakeDetector);
                //Stop The location service
                con.stopService(new Intent(con, LocationServiceMonitor));
            }
        });

        // bumpDetector initialization
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new BumpDetector();
        /*
         *  Register the listener for accelerometer
         */
        mShakeDetector.setOnShakeListener(new BumpDetector.OnShakeListener() {

            @Override
            public void onShake(int count, float gForce) {
                /*
                 * just any actions when shake event exist
                 */
                if (LocationService.latestLocation != null) {
                    long lastLocTime = LocationService.latestLocation.getTime();
                    long nowLocTime = System.currentTimeMillis();
                    if ((nowLocTime-lastLocTime)<= LOC_SHAKE_TIME_DIFF*1000) {
                        Log.d(TAG, "lastLocTime" + lastLocTime);

                        Toast.makeText(con, "shake event detected count====" + count + ",  gForce==" + gForce, Toast.LENGTH_SHORT).show();
                        // Location loc = getLastKnownLocation();
                        Log.d(TAG, "LOCATIONNNNNNNNNNNNNNNNNNNNNNN++++++++++++++++++ ===" + LocationService.latestLocation);

                        bumpEvents.add(new BumpEvent(gForce, count,LocationService.latestLocation));

                        tv.setText("Number of Bumps recorded = " + bumpEvents.size() + " last location ===" + LocationService.latestLocation);
                    } else {
                        Log.d(TAG,"Location obtained is too old.  may not register the bump event without a proper location!!!!!");
                    }
                } else {
                    Log.d(TAG,"Location is null, may not register the bump event without location!!!!!");
                }


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








}
