package com.example.vikashkumarsharma.eagleeye;

import android.*;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.User;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,View.OnClickListener {


    private static final int RC_SIGN_IN = 123;


    public static final String ANONYMOUS = "anonymous";

    private String mUsername;
    private String mEmail;
    private String mDeviceId;
    private String mPushId;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;


    private TextView mUsernameTextView;

    private LocationManager locationManager;
    private LocationListener locationListener;

    GoogleMap g_map;
    boolean map_ready = false;

    private LatLng mLatLng;

    private MapFragment mapFragment;

    private ImageView moreOptions;
    private ImageView logOut;

    private PopupMenu popupmenu;


    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildEventListener;

    private static final String MyPREFERENCES = "MyPrefs" ;

    private boolean foundDevice = false;

    private UserDetails mUserDetails;
    HashMap<String,Marker> stringMarkerHashMap = new HashMap<String,Marker>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toast.makeText(MainActivity.this, ""+ Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID), Toast.LENGTH_SHORT).show();
        mDeviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);


        mUsername = ANONYMOUS;

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference().child("userlocations");



        mFirebaseAuth = FirebaseAuth.getInstance();

        //mUsernameTextView = (TextView)findViewById(R.id.usernameTextView);

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    onSignedInInitialize(user.getDisplayName(),user.getEmail());
                    //initializePushId();
                    Toast.makeText(MainActivity.this, "mPushId = "+mPushId, Toast.LENGTH_SHORT).show();
                }else{

                    onSignedOutCleanup();
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(
                                            Arrays.asList(
                                                    new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),

                                                    new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mLatLng = new LatLng(location.getLatitude(),location.getLongitude());
                LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
                mUserDetails = new UserDetails(
                        mDeviceId,
                        mUsername,
                        mEmail,
                        new LocationDetails(
                                location.getLatitude(),location.getLongitude()));


                //Toast.makeText(MainActivity.this, "Lat = "+location.getLatitude(), Toast.LENGTH_SHORT).show();
                plotOnMap(latLng,mDeviceId);
                saveLocation();
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

    }

    void saveLocation(){
        if(mPushId != null)
            mDatabaseReference.child(mPushId).setValue(mUserDetails);
        else{
            mDatabaseReference.orderByKey()
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                       if(dataSnapshot!=null){
                           for(DataSnapshot ds : dataSnapshot.getChildren()){
                               UserDetails details = ds.getValue(UserDetails.class);
                               //Toast.makeText(MainActivity.this, "Push Ids = "+ ds.getKey()+"data="+details.getmDeviceId()+"==="+details.getmDeviceId().matches(mDeviceId), Toast.LENGTH_SHORT).show();
                               if(details.getmDeviceId().matches(mDeviceId)){
                                   //Toast.makeText(MainActivity.this, "Id matched", Toast.LENGTH_SHORT).show();
                                   mPushId = ds.getKey();
                                   break;
                               }
                           }
                           if(mPushId == null)
                               mPushId = mDatabaseReference.push().getKey();
                           mDatabaseReference.child(mPushId).setValue(mUserDetails);
                           mDatabaseReference.removeEventListener(this);
                       }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        }
    }
    void plotOnMap(LatLng latLng,String deviceId){

        MarkerOptions a = new MarkerOptions()
                .position(latLng).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_car));;

        if(map_ready){
            if(stringMarkerHashMap.containsKey(deviceId)){
                Marker marker = stringMarkerHashMap.get(deviceId);
                marker.remove();
                Marker newMarker = g_map.addMarker(a);
                stringMarkerHashMap.put(deviceId,newMarker);
                newMarker.setPosition(latLng);
            }else{
                Marker newMarker = g_map.addMarker(a);
                stringMarkerHashMap.put(deviceId,newMarker);
                newMarker.setPosition(latLng);
            }

//            if(mDeviceId == deviceId){
//                CameraPosition target = CameraPosition.builder().target(latLng).zoom(14).build();
//                g_map.moveCamera(CameraUpdateFactory.newCameraPosition(target));
//            }


        }
    }

    private void initializeMap() {
        mapFragment = (MapFragment)getFragmentManager().findFragmentById(R.id.googleMap);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        moreOptions = (ImageView)findViewById(R.id.moreOptionsImageView);
        moreOptions.setOnClickListener(this);
        logOut = (ImageView)findViewById(R.id.logoutImageView);
        logOut.setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Toast.makeText(this, "onActivityResult called", Toast.LENGTH_SHORT).show();
        if(requestCode == RC_SIGN_IN){
            //Toast.makeText(this, "RC_SIGN_IN", Toast.LENGTH_SHORT).show();
            if(resultCode == RESULT_OK){
                //Toast.makeText(this, "Signed In!", Toast.LENGTH_SHORT).show();
                configureLocation();
            }else if(resultCode == RESULT_CANCELED){
                //Toast.makeText(this, "Sign In Cancelled!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
        initializeMap();
        configureLocation();
    }


    @Override
    protected void onPause() {
        super.onPause();
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        detachDatabaseReadListener();
    }


    private void onSignedInInitialize(String displayName, String email) {
        mUsername = displayName;
        mEmail = email;
        //mUsernameTextView.setText(mUsername);
        SharedPreferences myPrefs = getApplicationContext().getSharedPreferences("APP_PREFS",MODE_PRIVATE);
        SharedPreferences.Editor editor = myPrefs.edit();
        editor.putString("mUsername",mUsername);
        editor.putString("mEmail",mEmail);
        editor.putString("mDeviceId",mDeviceId);
        editor.commit();
        attachDatabaseReadListener();

    }

    private void onSignedOutCleanup() {
        mUsername = ANONYMOUS;
        detachDatabaseReadListener();
    }

    private void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    UserDetails ud = dataSnapshot.getValue(UserDetails.class);
                    LatLng latLng = new LatLng(ud.getmLocation().getmLatitudte(),ud.getmLocation().getmLongitude());
                    plotOnMap(latLng,ud.getmDeviceId());
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };

            mDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

    private void detachDatabaseReadListener() {
        if(mChildEventListener != null) {
            mDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    public void onMapReady(GoogleMap googleMap) {
        map_ready = true;
        g_map = googleMap;

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            configureLocation();
        }
    }

    private void configureLocation() {
        Toast.makeText(MainActivity.this, "Configure Button", Toast.LENGTH_SHORT).show();
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
            locationManager.requestLocationUpdates("gps", 5000, 0, locationListener);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id == R.id.moreOptionsImageView){
            displayMenu();
        }

        if(id == R.id.logoutImageView){
            AuthUI.getInstance().signOut(this);
        }
    }

    private void displayMenu() {

        popupmenu = new PopupMenu(MainActivity.this, moreOptions);

        popupmenu.getMenuInflater().inflate(R.menu.pop_up_menu, popupmenu.getMenu());

        popupmenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            public boolean onMenuItemClick(MenuItem item) {

                Toast.makeText(MainActivity.this, item.getTitle(),Toast.LENGTH_LONG).show();

                if(item.getItemId() == R.id.aboutApp){
                    startActivity(new Intent(getApplicationContext(),AboutActivity.class));
                }

                if(item.getItemId() == R.id.listOfDevices){
                    startActivity(new Intent(getApplicationContext(),DeviceListActivity.class));
                }

                if(item.getItemId() == R.id.startJourney){
                    startActivity(new Intent(getApplicationContext(),StartJourneyActivity.class));
                }

                return true;
            }
        });

        popupmenu.show();

    }


}
