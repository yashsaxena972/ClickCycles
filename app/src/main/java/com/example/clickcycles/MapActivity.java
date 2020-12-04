package com.example.clickcycles;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.Map;

public class MapActivity<mRollNumber> extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Location mOrigin, mDestination;
    LocationRequest mLocationRequest;
    private Marker mDriverMarker;

    private String userId;
    private DatabaseReference mCustomerDatabase;
    private DatabaseReference mRequest;

    private ImageView profilePhoto;
    private TextView nameTextView;
    private TextView idTextView;
    private TextView statusTextView;
    private TextView rideInfoTextView;
    private ImageButton interactionButton;
    private ImageButton menuImageButton;
    private DrawerLayout mDrawerLayout;

    private String mName, mRollNumber;
    private String mCycleId, mRequestRollNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userId);
        mRequest = FirebaseDatabase.getInstance().getReference().child("Request");

        profilePhoto = (ImageView)findViewById(R.id.profile_photo);
        nameTextView = (TextView)findViewById(R.id.name_text_view);
        idTextView = (TextView)findViewById(R.id.id_text_view);
        rideInfoTextView = (TextView)findViewById(R.id.ride_info_text_view);
        statusTextView = (TextView)findViewById(R.id.ride_status_text_view);
        menuImageButton = (ImageButton)findViewById(R.id.menu_image_button);
        interactionButton = (ImageButton) findViewById(R.id.interaction_button);

        mDrawerLayout = findViewById(R.id.drawer_layout);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        getUserInfo();

        interactionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // for when a new request comes up
                mRequest.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                            Map<String, Object> map = (Map<String, Object>) snapshot.getValue();

                            if (map.get("RollNo") != null) {
                                mRequestRollNumber = map.get("RollNo").toString();


                                if (mRequestRollNumber.equals(mRollNumber)) {
                                    statusTextView.setText("Ride in progress");
                                    statusTextView.setTextColor(Color.parseColor("#2E7D32"));
                                    mOrigin = mLastLocation;
                                }
                            }
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                            Map<String, Object> map = (Map<String, Object>) snapshot.getValue();

                            if (map.get("RollNo") != null) {
                                mRequestRollNumber = map.get("RollNo").toString();
                                if (mRequestRollNumber.equals(mRollNumber)) {
                                    statusTextView.setText("Ride in progress");
                                    statusTextView.setTextColor(Color.parseColor("#2E7D32"));
                                }
                            }
                        }
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                            Map<String, Object> map = (Map<String, Object>) snapshot.getValue();

                            if (map.get("RollNo") != null) {
                                mRequestRollNumber = map.get("RollNo").toString();

                                if (mRequestRollNumber.equals(mRollNumber)) {
                                    statusTextView.setText("Ride ended");
                                    statusTextView.setTextColor(Color.parseColor("#E53935"));
                                    mDestination = mLastLocation;
                                    generateRideInfo();
                                }
                            }
                        }
                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });






        menuImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });


        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                // set item as selected to persist highlight
                menuItem.setChecked(true);
                // close drawer when item is tapped
                mDrawerLayout.closeDrawers();

                // Add code here to update the UI based on the item selected
                // For example, swap UI fragments here
                switch (menuItem.getItemId()){
                    case R.id.logout_customer:
                        FirebaseAuth.getInstance().signOut();
                        Intent logoutIntent = new Intent(MapActivity.this,MainActivity.class);
                        startActivity(logoutIntent);
                        finish();
                        break;

                    case R.id.settings_customer:
                        Intent settingsIntent = new Intent(MapActivity.this,SettingsActivity.class);
                        startActivity(settingsIntent);
                        // We don't want to finish this activity because we want to open the settings activity on top of this one
                        break;
                }

                return true;
            }
        });
    }

    private void generateRideInfo() {
        int distance = (int) mOrigin.distanceTo(mDestination);
        rideInfoTextView.setText("Distance travelled\n"+String.valueOf(distance)+"m");
        interactionButton.setVisibility(View.GONE);
        rideInfoTextView.setVisibility(View.VISIBLE);
    }

    private void getUserInfo() {
        mCustomerDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) snapshot.getValue();
                    if (map.get("name") != null) {
                        mName = map.get("name").toString();
                        nameTextView.setText(mName);
                    }

                    if (map.get("roll number") != null) {
                        mRollNumber = map.get("roll number").toString();
                        idTextView.setText(mRollNumber);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null && user.getPhotoUrl() != null){
            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .into(profilePhoto);
        }
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

        // After the map is ready, we need to enable the location(gps of the device), for which we must first check if the app
        // has the required permissions
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
    }

    // Helper method for building the Google API
    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    // This method is called each time the location is updated, ie, each second in this case
    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;

        // Get the latitude and longitude of the marker at each location update
        LatLng latlng = new LatLng(location.getLatitude(),location.getLongitude());
        // To keep the camera moving with the marker
        // Zoom values range from 1 to 21
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng,15));

        LocationHelper helper = new LocationHelper(
                location.getLatitude(),
                location.getLongitude()
        );

        FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userId)
                .child("CurrentLocation").setValue(helper);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}