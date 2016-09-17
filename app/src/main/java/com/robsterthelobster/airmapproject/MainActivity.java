package com.robsterthelobster.airmapproject;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.models.Coordinate;
import com.airmap.airmapsdk.models.status.AirMapStatus;
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.services.AirMap;
import com.airmap.airmapsdk.networking.services.MappingService.AirMapAirspaceType;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private final String TAG = MainActivity.class.getSimpleName();
    private final int LOCATION_REQUEST_CODE = 1;

    @BindView(R.id.recycler_view) RecyclerView mRecyclerView;
    @BindView(R.id.fab) FloatingActionButton fab;
    @BindView(R.id.toolbar) Toolbar toolbar;

    private GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;
    private RecyclerView.LayoutManager mLayoutManager;

    AirMapAdapter mAdapter;
    List<AirMapStatus> mData;
    Location mLocation;

    boolean mRequestingLocationUpdates = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        AirMap.init(MainActivity.this);

        setSupportActionBar(toolbar);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        mData = new ArrayList<>();
        mAdapter = new AirMapAdapter(mData);

        mLayoutManager = new LinearLayoutManager(this);
        ((LinearLayoutManager) mLayoutManager).setStackFromEnd(true);
        ((LinearLayoutManager) mLayoutManager).setReverseLayout(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
        callAirMap(location.getLatitude(), location.getLongitude());
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST_CODE);
            return;
        }
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(10000); // get location every 10s

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    private void callAirMap(double lat, double lon){
        Coordinate coordinate = new Coordinate(lat, lon);
        boolean showWeather = true; //Return weather information
        Date date = new Date(); //The current date/time
        double buffer = 1000; //1000 meters

        // All the types
        List<AirMapAirspaceType> types = Arrays.asList(
                AirMapAirspaceType.Airport,
                AirMapAirspaceType.ControlledAirspace,
                AirMapAirspaceType.SpecialUse,
                AirMapAirspaceType.TFR,
                AirMapAirspaceType.Wildfires,
                AirMapAirspaceType.Park,
                AirMapAirspaceType.PowerPlant,
                AirMapAirspaceType.School,
                AirMapAirspaceType.SpecialUse,
                AirMapAirspaceType.Heliport,
                AirMapAirspaceType.Hospitals);

        AirMap.checkCoordinate(coordinate, buffer, types, null, showWeather, date, new AirMapCallback<AirMapStatus>() {
            @Override
            public void onSuccess(final AirMapStatus response) {
                updateUI(response);
            }

            @Override
            public void onError(final AirMapException e) {
                Log.v(TAG, "Error getting Status");
                e.printStackTrace();
            }
        });
    }

    private void updateUI(final AirMapStatus airMapStatus) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mData.add(airMapStatus);
                mAdapter.notifyItemChanged(mData.size() - 1);
                mRecyclerView.smoothScrollToPosition(mData.size() - 1);
            }
        });
    }

    @OnClick(R.id.fab)
    public void updateWithLastLocation(View view) {
        if(mLocation != null){
            callAirMap(mLocation.getLatitude(), mLocation.getLongitude());
            Snackbar.make(view, "Getting flight status", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }else {
            Snackbar.make(view, "Location not available", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }
}
