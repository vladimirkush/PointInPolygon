package com.vladimirkush.pointinpolygon;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.kml.KmlContainer;
import com.google.maps.android.kml.KmlGeometry;
import com.google.maps.android.kml.KmlLayer;
import com.google.maps.android.kml.KmlPlacemark;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import BL.Checker;
import BL.CheckerTask;

public class MainActivity extends Activity
        implements OnMapReadyCallback, ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
    //consts
    public static final String TAG = "MAIN_ACT";
    public static final float ZOOM_RATE = 14;

    //fields
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private ArrayList<LatLng> mPolygonCoords;
    private Checker mChecker;

    private TextView mTvIsInside;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        createLocationRequest();

        // init mapFragment and map object
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mTvIsInside = (TextView) findViewById(R.id.isInsideTextView);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        showLastKnownLocationOnMap();

        try {
            KmlLayer kmlLayer = new KmlLayer(mMap, R.raw.allowed_area, this);
            kmlLayer.addLayerToMap();
            mPolygonCoords = getCoordinatesFromKmlLayer(kmlLayer);
            mChecker = new Checker(mPolygonCoords);
            Log.d(TAG, mPolygonCoords.toString());

            Log.d(TAG, "KMLlayer added successfully");
        } catch (XmlPullParserException e) {
            Log.d(TAG, "XmlPullParserException: "+e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "IOException: "+e.getMessage());
        }
        //mChecker = new Checker(null,mPolygonCoords);

    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (!(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION}, Constants.PERMISSION_LOCATION_REQUEST);

        } else {

            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            showLastKnownLocationOnMap();
            startLocationUpdates();

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();


    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();


    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Constants.PERMISSION_LOCATION_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    showLastKnownLocationOnMap();

                } else {

                    alertNoLocationPermissions();

                }

            }

        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        showLastKnownLocationOnMap();
        Log.d(TAG, "Lat: " + mLastLocation.getLatitude() + ", Lon: " + mLastLocation.getLongitude());
        if(mChecker != null){
            LatLng pos = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
            new CheckerTask(mTvIsInside,pos).execute(mChecker);
        }
    }

    private void showLastKnownLocationOnMap() {
        if (mLastLocation != null) {
            //Toast.makeText(this, "Lat:" + mLastLocation.getLatitude() + " Lon:" + mLastLocation.getLongitude(), Toast.LENGTH_LONG).show();
            if (mMap != null) {
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    // show device on the map - blue dot
                    mMap.setMyLocationEnabled(true);

                }
                // center camera on device's location
                LatLng lastLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                CameraUpdate upd = CameraUpdateFactory.newLatLngZoom(lastLatLng, ZOOM_RATE);
                mMap.moveCamera(upd);
            }
        }
    }

    /*
    private boolean areLocationPermissionsGranted() {
        return (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }
    */

    /* setup location updates' properties */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                //final LocationSettingsStates states = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.d(TAG, "SETTINGS SUCCESS");

                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.d(TAG, "SETTINGS RESOLUTION REQUIRED");
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.d(TAG, "SETTINGS UNAVAILABLE");

                        break;
                }
            }
        });
    }


    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }


    protected void startLocationUpdates() {
        Log.d(TAG, "Location updates started");
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    private void alertNoLocationPermissions() {
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        dlgAlert.setMessage("This app needs access to your location");
        dlgAlert.setTitle("No location");
        dlgAlert.setPositiveButton("Close app", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }


    private ArrayList<LatLng> getCoordinatesFromKmlLayer(KmlLayer layer){

        List<LatLng> points = new ArrayList<>();
        for (KmlContainer c : layer.getContainers()) {
            for (KmlPlacemark p : c.getPlacemarks()) {
                KmlGeometry g = p.getGeometry();
                if (g.getGeometryType().equals("Polygon")) {
                    points.addAll((Collection<? extends LatLng>) g.getGeometryObject());
                }
            }
        }
        Log.d(TAG, points.toString());

        Object[] o = points.toArray();
        return (ArrayList<LatLng>) o[0];
    }


    /*public void onTestBtnClick(View view) {
        if (mChecker == null)
            mChecker = new Checker(mPolygonCoords);
        else{
            LatLng pos = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
            mChecker.isInside(pos);
        }
    }*/
}
