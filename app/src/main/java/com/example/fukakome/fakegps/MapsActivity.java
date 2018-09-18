package com.example.fukakome.fakegps;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    static final int REQUEST_CODE = 1;

    private GoogleMap mMap;

    private LocationManager mLocationManager;
    private CountDownTimer mCountDownTimer;

    LocationListener listener = new MyLocationListener();

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("", "onStop");
    }

    void clearLM() {
        mLocationManager.clearTestProviderLocation(LocationManager.GPS_PROVIDER);
        mLocationManager.clearTestProviderStatus(LocationManager.GPS_PROVIDER);
        mLocationManager.clearTestProviderEnabled(LocationManager.GPS_PROVIDER);
        mLocationManager.removeTestProvider(LocationManager.GPS_PROVIDER);
        mLocationManager.removeUpdates(listener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearLM();
        mCountDownTimer.cancel();
        Log.i("", "onDestroy");
    }

    class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {

            String msg = "Lat=" + location.getLatitude()
                    + "\nLng=" + location.getLongitude();
            Log.d("GPS", msg);
//                            mLocationManager.removeUpdates(this);
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        final boolean gpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!gpsEnabled) {
            // Build an alert dialog here that requests that the user enable
            // the location services, then when the user clicks the "OK" button,
            Log.println(Log.INFO, "", "gps: start settings");
            enableLocationSettings();
            Log.println(Log.INFO, "", "gps: finish settings");
        } else {
            Log.println(Log.INFO, "", "gps: ok");
        }

        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE);
        }

//        try {
//
//            mLocationManager.requestLocationUpdates(
//                    LocationManager.GPS_PROVIDER, //LocationManager.NETWORK_PROVIDER,
//                    0, 0,
//                    listener);
//            mLocationManager.requestLocationUpdates(
//                    LocationManager.NETWORK_PROVIDER,
//                    0, 0,
//                    listener);
//        } catch (SecurityException ex) {
//            Log.e("ERROR", ex.toString());
//        }

        startFakingGPS();

        mapFragment.getMapAsync(this);
    }

    private List<Location> allMockedPositions;
    int position = 0;

    void startFakingGPS() {
        Log.i("TAG", " now faking gps ");
        allMockedPositions = new ArrayList<Location>();
        allMockedPositions.add(createNewLocation(126.837095, 30.800168 ));
        allMockedPositions.add(createNewLocation(126.837095, 30.800168  ));
        allMockedPositions.add(createNewLocation(126.837095, 30.800168 ));

        mCountDownTimer = new CountDownTimer(2000, 2000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                if (allMockedPositions.size() > position) {
                    Location mockedLocation = allMockedPositions.get(position++);
                    mockedLocation.setTime(System.currentTimeMillis());
                    mockedLocation.setAccuracy(50);
                    mockedLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());

                    mLocationManager.addTestProvider(LocationManager.GPS_PROVIDER, false,
                            false, false , false,
                            true, true, true, 0, 5);
                    mLocationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
                    mLocationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, mockedLocation);

                    mLocationManager.addTestProvider(LocationManager.NETWORK_PROVIDER, false, false, false, false, true, true, true,
                            Criteria.POWER_LOW, Criteria.ACCURACY_FINE);

                    mLocationManager.setTestProviderEnabled(LocationManager.NETWORK_PROVIDER, true);
                    mLocationManager.setTestProviderLocation(LocationManager.NETWORK_PROVIDER, mockedLocation);
                    mLocationManager.setTestProviderStatus(LocationManager.NETWORK_PROVIDER, LocationProvider.AVAILABLE, null, System.currentTimeMillis());
//                    mLocationManager.setTestProviderEnabled(LocationManager.PASSIVE_PROVIDER, true);
//                    mLocationManager.setTestProviderLocation(LocationManager.PASSIVE_PROVIDER, mockedLocation);
//                    mLocationManager.setTestProviderStatus(LocationManager.PASSIVE_PROVIDER, LocationProvider.AVAILABLE, null, System.currentTimeMillis());

                    mLocationManager.setTestProviderStatus(LocationManager.GPS_PROVIDER, LocationProvider.AVAILABLE, null, System.currentTimeMillis());
                    LatLng marker = new LatLng(mockedLocation.getLatitude(), mockedLocation.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(marker).title("Marker"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(marker));

                    String msg = "SetLocation[" + position + "]: (" + mockedLocation.getLatitude()
                            + ", " + mockedLocation.getLongitude() + ")";
                    Log.d("GPS", msg);
                } else {
                    position = 0;
                }
                Log.i("", "timer: finish: position = " + position);
                start();
            }
        }.start();
    }

    Location createNewLocation(double longitude, double latitude) {
        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setLongitude(longitude);
        location.setLatitude(latitude);
        location.setAltitude(0.0f);
        location.setAccuracy(5.0f);
        return location;
    }

    private void enableLocationSettings() {
        Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(settingsIntent);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}
