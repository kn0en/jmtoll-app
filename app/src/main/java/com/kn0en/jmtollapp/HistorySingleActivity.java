package com.kn0en.jmtollapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class HistorySingleActivity extends AppCompatActivity implements OnMapReadyCallback, RoutingListener {

    String rideId,currentUserId,officerId,riderId,operatorId,userOfficerOrRiderOrOperator;

    private TextView rideLocation, rideDate, rideRuas, rideDistance;
    private TextView riderName, riderPhone, riderCarNumber;
    private TextView officerName, officerPhone, officerCarNumber;

    private CircleImageView riderImage,officerImage;

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;

    private DatabaseReference historyRideInfoDb;

    private LatLng pickupLocationLatLng,officerLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_single);

        polylines = new ArrayList<>();
        rideId = getIntent().getExtras().getString("rideId");
        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        //info route
        rideLocation = (TextView) findViewById(R.id.rideLocation);
        rideDistance = (TextView) findViewById(R.id.rideDistance);
        rideDate = (TextView) findViewById(R.id.rideDate);
        rideRuas = (TextView) findViewById(R.id.rideRuas);

        //info user
        riderImage = (CircleImageView) findViewById(R.id.userImage);
        riderName = (TextView) findViewById(R.id.userName);
        riderPhone = (TextView) findViewById(R.id.userPhone);
        riderCarNumber = (TextView) findViewById(R.id.userCarNumber);

        //info officer
        officerImage = (CircleImageView) findViewById(R.id.officerImage);
        officerName = (TextView) findViewById(R.id.officerName);
        officerPhone = (TextView) findViewById(R.id.officerPhone);
        officerCarNumber = (TextView) findViewById(R.id.officerCarNumber);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        historyRideInfoDb = FirebaseDatabase.getInstance().getReference().child("history").child(rideId);

        getRideInformation();

    }

    private void getRideInformation() {
        historyRideInfoDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot child : snapshot.getChildren()){
                        if (child.getKey().equals("operator")){
                            operatorId = child.getValue().toString();
                            if (operatorId.equals(currentUserId)){
                                userOfficerOrRiderOrOperator = "Operators";
                                riderPhone.setVisibility(View.VISIBLE);
                                officerPhone.setVisibility(View.VISIBLE);
                            }
                        }
                        if (child.getKey().equals("rider")){
                            riderId = child.getValue().toString();
                            if (riderId.equals(currentUserId)){
                                userOfficerOrRiderOrOperator = "Riders";
                                riderPhone.setVisibility(View.GONE);
                                officerPhone.setVisibility(View.GONE);
                            }
                        }
                        if (child.getKey().equals("officer")){
                            officerId = child.getValue().toString();
                            if (officerId.equals(currentUserId)){
                                userOfficerOrRiderOrOperator = "Officers";
                                riderPhone.setVisibility(View.GONE);
                                officerPhone.setVisibility(View.GONE);
                            }
                        }
                        //info Rider
                        if (child.getKey().equals("riderImage")){
                            Glide.with(getApplication()).load(child.getValue().toString()).into(riderImage);
                        }
                        if (child.getKey().equals("nameRider")){
                            riderName.setText("Name : " + child.getValue().toString());
                        }
                        if (child.getKey().equals("phoneRider")){
                            riderPhone.setText("Phone : " + child.getValue().toString());
                        }
                        if (child.getKey().equals("carNumberRider")){
                            riderCarNumber.setText("Car plate : " + child.getValue().toString());
                        }

                        //Info Officer
                        if (child.getKey().equals("officerImage")){
                            Glide.with(getApplication()).load(child.getValue().toString()).into(officerImage);
                        }
                        if (child.getKey().equals("nameOfficer")){
                            officerName.setText("Name : " + child.getValue().toString());
                        }
                        if (child.getKey().equals("phoneOfficer")){
                            officerPhone.setText("Phone : " + child.getValue().toString());
                        }
                        if (child.getKey().equals("carNumberOfficer")){
                            officerCarNumber.setText("Car plate : " + child.getValue().toString());
                        }

                        //info Route
                        if (child.getKey().equals("timestamp")){
                            rideDate.setText(getDate(Long.valueOf(child.getValue().toString())));
                        }
                        if (child.getKey().equals("locationPickup")){
                            rideLocation.setText("Pickup Location : " + child.getValue().toString());
                        }
                        if (child.getKey().equals("ruas")){
                            rideRuas.setText("Ruas Tol : " + child.getValue().toString());
                        }
                        if (child.getKey().equals("distance")){
                            rideDistance.setText("Distance : " + child.getValue().toString() +" Km");
                        }
                        if (child.getKey().equals("location")){
                            officerLatLng = new LatLng(Double.valueOf(child.child("from").child("lat").getValue().toString()), Double.valueOf(child.child("from").child("lng").getValue().toString()));
                            pickupLocationLatLng = new LatLng(Double.valueOf(child.child("to").child("lat").getValue().toString()), Double.valueOf(child.child("to").child("lng").getValue().toString()));

                            if(!pickupLocationLatLng.equals(new LatLng(0, 0))){
                                getRouteToMarker();
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String getDate(Long timestamp) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(timestamp*1000);
        String date = DateFormat.format("dd-MM-yyyy hh:mm",cal).toString();

        return  date;
    }

    private void getRouteToMarker() {
        String apiKey = getString(R.string.api_key);
        Routing routing = new Routing.Builder()
                .key(apiKey)
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(officerLatLng,pickupLocationLatLng)
                .build();
        routing.execute();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }


    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.quantum_googblue};

    @Override
    public void onRoutingFailure(RouteException e) {
        if (e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(officerLatLng);
        builder.include(pickupLocationLatLng);

        LatLngBounds bounds = builder.build();

        int padding = (int) (100);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds,padding);

        mMap.moveCamera(cameraUpdate);

        mMap.addMarker(new MarkerOptions().position(pickupLocationLatLng).title("Pickup Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.pin)));
        mMap.addMarker(new MarkerOptions().position(officerLatLng).title("Officer").icon(BitmapDescriptorFactory.fromResource(R.drawable.tow)));

        if (polylines.size() > 0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i < route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

//            Toast.makeText(getApplicationContext(), "Route " + (i + 1) + ": distance - " + route.get(i).getDistanceValue() + ": duration - " + route.get(i).getDurationValue(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingCancelled() {

    }
}