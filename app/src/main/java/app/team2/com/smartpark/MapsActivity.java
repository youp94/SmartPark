package app.team2.com.smartpark;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    //Button btn_search;
    //EditText locationSearch;

    private DatabaseReference mDatabase;

    private FusedLocationProviderClient mFusedLocationClient;
    private Location location;

    ArrayList<Parking> listParking = new ArrayList<>();

    public static final int ACCESSLOCATION=123;

    public static final String TAG = "tag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        mDatabase.child("Parkings").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listParking.clear();
                Iterator<DataSnapshot> i = dataSnapshot.getChildren().iterator();
                while (i.hasNext()) {
                    DataSnapshot data = i.next();
                    Parking park = data.getValue(Parking.class);
                    listParking.add(park);
                    float distance = calculateDistanceInKilometer(location.getLatitude(), location.getLongitude(),
                            park.getLat(), park.getLon());
                    if(distance< 10.0){
                        mMap.addMarker(new MarkerOptions().position(park.getLatLng()).title(park.getNom()+ " /" +
                        "Distance: "+String.valueOf(distance)+ " KM/ "+
                        "Places libre: "+String.valueOf(park.getNb_place_libre())));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName());
                mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title("Votre destination"));
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        checkPermission();
    }

    public void checkPermission(){
        if(Build.VERSION.SDK_INT >= 23){
            if(ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                String[] permissions = { Manifest.permission.ACCESS_COARSE_LOCATION};
                requestPermissions(permissions, ACCESSLOCATION);
                return;
            }
        }
        getLocalisation();
    }

    @SuppressLint("MissingPermission")
    public void getLocalisation(){
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            MapsActivity.this.location = location;
                            LatLng sydney = new LatLng(location.getLatitude(),location.getLongitude());
                            mMap.addMarker(new MarkerOptions().position(sydney).title("Position actuelle"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

                        }
                    }
                });
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case ACCESSLOCATION:
                if (grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    getLocalisation();
                }else{
                    Toast.makeText(this,"We cannot access to your location",Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera

    }

    public final static double AVERAGE_RADIUS_OF_EARTH_KM = 6371;

    public float calculateDistanceInKilometer(/*Location loc1,Location loc2*/
            double userLat, double userLng, double venueLat, double venueLng) {
        /*double userLat = loc1.getLatitude();
        double userLng = loc1.getLongitude();
        double venueLat = loc2.getLatitude();
        double venueLng = loc2.getLongitude();*/
        double latDistance = Math.toRadians(userLat - venueLat);
        double lngDistance = Math.toRadians(userLng - venueLng);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(userLat)) * Math.cos(Math.toRadians(venueLat))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return (float) ((AVERAGE_RADIUS_OF_EARTH_KM * c));
    }
}
