package com.kn0en.jmtollapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class RiderMapsActivity extends AppCompatActivity implements OnMapReadyCallback ,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, NavigationView.OnNavigationItemSelectedListener {

    final int LOCATION_REQUEST_CODE = 1;
    Location mLocation;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;

    List<Address> addresses;
    Address obj;
    String add;
    boolean getDriversAroundStarted = false;
    List<Marker> markers = new ArrayList<>();

    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private LatLng pickupLocation;

    private Boolean requestBol = false;

    private LinearLayout mUserField;

    private CircleImageView mImageHeader;
    private TextView mYourLocation;
    private MaterialTextView mNameHeader, mPhoneHeader;
    private MaterialButton mRequest,mConfirmationRequest;

    private DrawerLayout drawer;
    private NavigationView navigationView;
    Toolbar toolbar;

    FirebaseAuth mAuth;
    String riderId;
    private DatabaseReference riderUserHeaderRef;

    private RadioGroup mRuasGroup;
    RadioButton ruasButton;
    private TextInputEditText mNameRider,mPhoneRider,mCarNumberRider;
    private String mRuas,mName,mPhoneNumber,mCarNumber;

    View itemView;
    MaterialAlertDialogBuilder builder;
    androidx.appcompat.app.AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState == null) {
            mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            navigationView.setCheckedItem(R.id.nav_maps);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(RiderMapsActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        } else {
            mapFragment.getMapAsync(this);
        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        mUserField = (LinearLayout) findViewById(R.id.userField);

        mYourLocation = (TextView) findViewById(R.id.yourLocation);

        mRequest = (MaterialButton) findViewById(R.id.btnCallOfficer);
        mConfirmationRequest = (MaterialButton) findViewById(R.id.btnConfirmationOfficer);

        View itemView = navigationView.getHeaderView(0);
        mImageHeader = (CircleImageView) itemView.findViewById(R.id.image_header);
        mNameHeader = (MaterialTextView) itemView.findViewById(R.id.name_header);
        mPhoneHeader = (MaterialTextView) itemView.findViewById(R.id.phone_header);

        mAuth = FirebaseAuth.getInstance();
        riderId = mAuth.getCurrentUser().getUid();
        riderUserHeaderRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Riders").child(riderId);

        getRiderInfoHeader();
        getRuasInfo();

        mRequest.setOnClickListener(view -> {
            if (!requestBol){
                saveRuasInformation();
                requestBol = true;

                String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("riderRequest");
                GeoFire geoFire = new GeoFire(ref);
                geoFire.setLocation(userId, new GeoLocation(mLocation.getLatitude(),mLocation.getLongitude()));

                showFindingOfficerLayout();

                mUserField.setVisibility(View.GONE);
                mRequest.setVisibility(View.GONE);
                mConfirmationRequest.setVisibility(View.VISIBLE);
                mConfirmationRequest.setEnabled(true);
            }
        });
        mConfirmationRequest.setOnClickListener(view -> endRide());

    }

    private void  showFindingOfficerLayout() {
        itemView = LayoutInflater.from(this).inflate(R.layout.layout_finding_officer, null);

        itemView.setPadding(10,10,10,10);
        MaterialButton mX = (MaterialButton) itemView.findViewById(R.id.btn_x);
        builder = new MaterialAlertDialogBuilder(this,R.style.MaterialAlertDialog_rounded);
        builder.setView(itemView);
        builder.setCancelable(false);

        dialog = builder.create();
        dialog.show();

        mX.setOnClickListener(view -> dialog.dismiss());
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_profile:
                Intent intentProfile = new Intent(RiderMapsActivity.this, RiderSettingActivity.class);
                startActivity(intentProfile);
                navigationView.setCheckedItem(R.id.nav_profile);
                break;
            case R.id.nav_password:
                Intent intentPassword = new Intent(RiderMapsActivity.this, UpdatePasswordActivity.class);
                startActivity(intentPassword);
                navigationView.setCheckedItem(R.id.nav_password);
                break;
            case R.id.nav_maps:
                mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
                navigationView.setCheckedItem(R.id.nav_maps);
                break;
            case R.id.nav_history:
                Intent intentHistory = new Intent(RiderMapsActivity.this, HistoryActivity.class);
                intentHistory.putExtra("riderOrOfficerOrOperator", "Riders");
                startActivity(intentHistory);
                navigationView.setCheckedItem(R.id.nav_history);
                break;
            case R.id.nav_tutorial:
                Intent intentTutorial = new Intent(RiderMapsActivity.this, TutorialSlidePagerActivity.class);
                intentTutorial.putExtra("riderOrOfficer", "Riders");
                startActivity(intentTutorial);
                navigationView.setCheckedItem(R.id.nav_tutorial);
                break;
            case R.id.nav_signOut:
                String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("riderRequest");
                GeoFire geoFire = new GeoFire(ref);
                geoFire.removeLocation(userId);

                FirebaseAuth.getInstance().signOut();
                Intent intentSignOut = new Intent(RiderMapsActivity.this, SplashScreenActivity.class);
                startActivity(intentSignOut);
                finish();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(RiderMapsActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
        mMap.setOnCameraChangeListener(cameraPosition -> {
            pickupLocation = cameraPosition.target;

            try {

                mLocation = new Location("");
                mLocation.setLatitude(pickupLocation.latitude);
                mLocation.setLongitude(pickupLocation.longitude);

                String dataTitle = getAddress();
                mYourLocation.setText(dataTitle);

            } catch (Exception e) {
                e.printStackTrace();
            }
            getDriversAround();

        });
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mapFragment.getMapAsync(this);
                }
                else {
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(RiderMapsActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, getPendingIntent());

    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
//        if (getApplicationContext() != null) {
//            mLocation = location;
//
//            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//            mMap.setTrafficEnabled(true);
//
//            float zoomLevel = 16.0f; //This goes up to 21
//
//            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoomLevel));
//
//        }
    }

    private void getRiderInfoHeader() {
        riderUserHeaderRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) snapshot.getValue();
                    if (map.get("name") != null) {
                        String mName = map.get("name").toString();
                        mNameHeader.setText(mName);
                    }
                    if (map.get("phone") != null) {
                        String mPhone = map.get("phone").toString();
                        mPhoneHeader.setText(mPhone);
                    }
                    if (map.get("profileImageUrl") != null) {
                        String mProfileImageHeader = map.get("profileImageUrl").toString();
                        Glide.with(getApplication()).load(mProfileImageHeader).into(mImageHeader);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public String getAddress() {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            //diganti pickuplocation
            addresses = geocoder.getFromLocation(pickupLocation.latitude, pickupLocation.longitude, 1);
            obj = addresses.get(0);
            add = obj.getAddressLine(0);
            return add;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void endRide(){
        requestBol = false;

        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("riderRequest");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId);

        dialog.dismiss();
        mUserField.setVisibility(View.VISIBLE);
        mRequest.setVisibility(View.VISIBLE);
        mRequest.setText("FIND OFFICER");
        mConfirmationRequest.setVisibility(View.GONE);
    }

    private void getDriversAround(){
        getDriversAroundStarted = true;
        DatabaseReference officerLocation = FirebaseDatabase.getInstance().getReference().child("officersAvailable");

        GeoFire geoFire = new GeoFire(officerLocation);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(mLocation.getLatitude(), mLocation.getLongitude()), 10000);

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                for(Marker markerIt : markers){
                    if(Objects.equals(markerIt.getTag(), key))
                        return;
                }

                LatLng officerLocation = new LatLng(location.latitude, location.longitude);

                Marker mOfficerMarker = mMap.addMarker(new MarkerOptions().position(officerLocation).title("Officer").icon(BitmapDescriptorFactory.fromResource(R.drawable.tow)));
                mOfficerMarker.setTag(key);

                markers.add(mOfficerMarker);

            }

            @Override
            public void onKeyExited(String key) {
                for(Marker markerIt : markers){
                    if(Objects.equals(markerIt.getTag(), key)){
                        markerIt.remove();
                        markers.remove(markerIt);
                        return;
                    }
                }
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                for(Marker markerIt : markers){
                    if(Objects.equals(markerIt.getTag(), key)){
                        markerIt.setPosition(new LatLng(location.latitude, location.longitude));
                    }
                }
            }

            @Override
            public void onGeoQueryReady() {
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
            }
        });
    }

    private PendingIntent getPendingIntent() {

        Intent intent = new Intent(this,MyLocationService.class);
        intent.setAction(MyLocationService.ACTION_PROCESS_UPDATE);
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void getRuasInfo(){
        mNameRider = (TextInputEditText) findViewById(R.id.rider_name);
        mPhoneRider = (TextInputEditText) findViewById(R.id.rider_phone);
        mCarNumberRider = (TextInputEditText) findViewById(R.id.rider_nopol);
        mRuasGroup = (RadioGroup) findViewById(R.id.ruasGroup);
        riderUserHeaderRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getChildrenCount()>0) {
                    Map<String, Object> map = (Map<String, Object>) snapshot.getValue();
                    if (map.get("name") != null) {
                        mName = map.get("name").toString();
                        mNameRider.setText(mName);
                    }
                    if (map.get("phone") != null){
                        mPhoneNumber = map.get("phone").toString();
                        mPhoneRider.setText(mPhoneNumber);
                    }
                    if (map.get("carNumber") != null){
                        mCarNumber = map.get("carNumber").toString();
                        mCarNumberRider.setText(mCarNumber);
                    }
                    if (map.get("ruas") != null) {
                        mRuas = map.get("ruas").toString();
                        switch (mRuas) {
                            case "Kanan":
                                mRuasGroup.check(R.id.kanan);
                                break;
                            case "Kiri":
                                mRuasGroup.check(R.id.kiri);
                                break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void saveRuasInformation() {
        int selectedRuas = mRuasGroup.getCheckedRadioButtonId();
        ruasButton = (RadioButton) findViewById(selectedRuas);
        if (ruasButton.getText() == null){
            return;
        }
        mRuas = ruasButton.getText().toString();
        mName = mNameRider.getText().toString();
        mPhoneNumber = mPhoneRider.getText().toString();
        mCarNumber = mCarNumberRider.getText().toString();

        Map userInfo = new HashMap();
        userInfo.put("ruas", mRuas);
        userInfo.put("name", mName);
        userInfo.put("phone", mPhoneNumber);
        userInfo.put("carNumber", mCarNumber);
        userInfo.put("locationPickup",getAddress());
        //diganti pickuplocation
        userInfo.put("pickupLocationLat",pickupLocation.latitude);
        userInfo.put("pickupLocationLng",pickupLocation.longitude);

        riderUserHeaderRef.updateChildren(userInfo);
    }
}