package com.example.vikashkumarsharma.eagleeye;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class StartJourneyActivity extends AppCompatActivity implements OnMapReadyCallback,View.OnClickListener {

    private LocationManager locationManager;
    private LocationListener locationListener;

    GoogleMap g_map;
    boolean map_ready = false;

    private LatLng mLatLng;

    private MapFragment mapFragment;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildEventListener;

    private String mUsername;
    private String mDeviceId;
    private String mEmail;

    private Button startJourneyButton;
    private Button endJourneyButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_journey);

        getCredentialValues();

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference().child("journeys");


        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                startJourneyButton.setVisibility(View.VISIBLE);
                mLatLng = new LatLng(location.getLatitude(),location.getLongitude());
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };

        configureLocation();

    }

    @Override
    protected void onStart() {
        super.onStart();
        startJourneyButton = (Button)findViewById(R.id.startJourney);
        startJourneyButton.setEnabled(false);
        endJourneyButton = (Button)findViewById(R.id.stopJourney);
    }

    private void getCredentialValues() {

        SharedPreferences myPrefs = getApplicationContext().getSharedPreferences("APP_PREFS",MODE_PRIVATE);
        mUsername = myPrefs.getString("mUsername",null);
        mDeviceId = myPrefs.getString("mDeviceId",null);
        mEmail = myPrefs.getString("mEmail",null);
    }

    private void initializeMap() {
        mapFragment = (MapFragment)getFragmentManager().findFragmentById(R.id.googleMap);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }


    @Override
    protected void onResume() {
        super.onResume();
        initializeMap();
        configureLocation();
    }


    @Override
    protected void onPause() {
        super.onPause();
        detachDatabaseReadListener();
    }


    private void detachDatabaseReadListener() {
        if(mChildEventListener != null) {
            mDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    private void configureLocation() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(MainActivity.this, "Access Permissions Missing", Toast.LENGTH_SHORT).show();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //Toast.makeText(MainActivity.this, "Requesting for permissions", Toast.LENGTH_SHORT).show();
                requestPermissions(new String[]{
                        android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.INTERNET
                },10);
            }

            return;
        }else{
            locationManager.requestLocationUpdates("gps", 1000, 1, locationListener);
        }
    }

    @Override
    public void onClick(View view) {

    }
}
