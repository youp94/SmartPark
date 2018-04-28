package app.team2.com.smartpark;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
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

import static java.security.AccessController.getContext;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Marker chosen;
    Button tous;
    SeekBar seekBar;

    private DatabaseReference mDatabase;

    private FusedLocationProviderClient mFusedLocationClient;
    private Location location;
    private LatLng destinationLocation = null;

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
                        if(park.getNb_place_libre()>0) {
                            mMap.addMarker(new MarkerOptions().position(park.getLatLng()).title(park.getNom() + " /" +
                                    "Distance: " + String.valueOf(distance) + " KM/ " +
                                    "Places libre: " + String.valueOf(park.getNb_place_libre()))
                                    .snippet("Tarif: 1 Heure: 50DA 2 Heures: 100DA 3 Heures:" +
                                            " 150DA Plus de 3 Heures: 60DA/30min")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ava)));
                        }
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
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title("Votre destination")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.dest)));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));

                destinationLocation = place.getLatLng();

                float r;
                r= 10;
                LatLng sydney = new LatLng(location.getLatitude(),location.getLongitude());
                mMap.addMarker(new MarkerOptions().position(sydney).title("Position actuelle")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.me)));
                for(int i=0; i<listParking.size(); i++){
                    Parking park = listParking.get(i);
                    float distance = calculateDistanceInKilometer(destinationLocation.latitude,
                            destinationLocation.longitude,
                            park.getLat(), park.getLon());
                    if(distance< r){
                        if(park.getNb_place_libre()>0) {
                            mMap.addMarker(new MarkerOptions().position(park.getLatLng()).title(park.getNom() + " /" +
                                    "Distance: " + String.valueOf(distance) + " KM/ " +
                                    "Places libre: " + String.valueOf(park.getNb_place_libre()))
                                    .snippet("Tarif: 1 Heure: 50DA 2 Heures: 100DA 3 Heures:" +
                                            " 150DA Plus de 3 Heures: 60DA/30min")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ava)));
                        }
                    }
                }

            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        tous = findViewById(R.id.btn_all);
        seekBar = findViewById(R.id.seekBar);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float r = progress/10;

                mMap.clear();
                if(destinationLocation == null) {
                    LatLng sydney = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(sydney).title("Position actuelle")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.me)));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
                    for (int i = 0; i < listParking.size(); i++) {
                        Parking park = listParking.get(i);
                        float distance = calculateDistanceInKilometer(location.getLatitude(), location.getLongitude(),
                                park.getLat(), park.getLon());
                        if (distance < r) {
                            if(park.getNb_place_libre()>0) {
                                mMap.addMarker(new MarkerOptions().position(park.getLatLng()).title(park.getNom() + " /" +
                                        "Distance: " + String.valueOf(distance) + " KM/ " +
                                        "Places libre: " + String.valueOf(park.getNb_place_libre()))
                                        .snippet("Tarif: 1 Heure: 50DA 2 Heures: 100DA 3 Heures:" +
                                                " 150DA Plus de 3 Heures: 60DA/30min")
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ava)));
                            }
                        }
                    }
                }else{
                    LatLng sydney = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(sydney).title("Position actuelle")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.me)));
                    mMap.addMarker(new MarkerOptions().position(destinationLocation).title("Votre Destination")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.dest)));
                    for (int i = 0; i < listParking.size(); i++) {
                        Parking park = listParking.get(i);
                        float distance = calculateDistanceInKilometer(destinationLocation.latitude,
                                destinationLocation.longitude,
                                park.getLat(), park.getLon());
                        if (distance < r) {
                            if(park.getNb_place_libre()>0) {
                                mMap.addMarker(new MarkerOptions().position(park.getLatLng()).title(park.getNom() + " /" +
                                        "Distance: " + String.valueOf(distance) + " KM/ " +
                                        "Places libre: " + String.valueOf(park.getNb_place_libre()))
                                        .snippet("Tarif: 1 Heure: 50DA 2 Heures: 100DA 3 Heures:" +
                                                " 150DA Plus de 3 Heures: 60DA/30min")
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ava)));
                            }
                        }
                    }
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        tous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float r = 10;
                mMap.clear();
                if(destinationLocation == null) {
                    LatLng sydney = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(sydney).title("Position actuelle")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.me)));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
                    for (int i = 0; i < listParking.size(); i++) {
                        Parking park = listParking.get(i);
                        float distance = calculateDistanceInKilometer(location.getLatitude(), location.getLongitude(),
                                park.getLat(), park.getLon());
                        if (distance < r) {
                            if(park.getNb_place_libre()>0) {
                                mMap.addMarker(new MarkerOptions().position(park.getLatLng()).title(park.getNom() + " /" +
                                        "Distance: " + String.valueOf(distance) + " KM/ " +
                                        "Places libre: " + String.valueOf(park.getNb_place_libre()))
                                        .snippet("Tarif: 1 Heure: 50DA 2 Heures: 100DA 3 Heures:" +
                                                " 150DA Plus de 3 Heures: 60DA/30min")
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ava)));
                            }else {
                                mMap.addMarker(new MarkerOptions().position(park.getLatLng()).title(park.getNom() + " /" +
                                        "Distance: " + String.valueOf(distance) + " KM/ " +
                                        "Places libre: " + String.valueOf(park.getNb_place_libre()))
                                        .snippet("Tarif: 1 Heure: 50DA 2 Heures: 100DA 3 Heures:" +
                                                " 150DA Plus de 3 Heures: 60DA/30min")
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.full)));
                            }
                        }
                    }
                }else{
                    LatLng sydney = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(sydney).title("Position actuelle")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.me)));
                    mMap.addMarker(new MarkerOptions().position(destinationLocation).title("Votre Destination")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.dest)));
                    for (int i = 0; i < listParking.size(); i++) {
                        Parking park = listParking.get(i);
                        float distance = calculateDistanceInKilometer(destinationLocation.latitude,
                                destinationLocation.longitude,
                                park.getLat(), park.getLon());
                        if (distance < r) {
                            if(park.getNb_place_libre()>0) {
                                mMap.addMarker(new MarkerOptions().position(park.getLatLng()).title(park.getNom() + " /" +
                                        "Distance: " + String.valueOf(distance) + " KM/ " +
                                        "Places libre: " + String.valueOf(park.getNb_place_libre()))
                                        .snippet("Tarif: 1 Heure: 50DA 2 Heures: 100DA 3 Heures:" +
                                                " 150DA Plus de 3 Heures: 60DA/30min")
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ava)));
                            }else {
                                mMap.addMarker(new MarkerOptions().position(park.getLatLng()).title(park.getNom() + " /" +
                                        "Distance: " + String.valueOf(distance) + " KM/ " +
                                        "Places libre: " + String.valueOf(park.getNb_place_libre()))
                                        .snippet("Tarif: 1 Heure: 50DA 2 Heures: 100DA 3 Heures:" +
                                                " 150DA Plus de 3 Heures: 60DA/30min")
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.full)));
                            }
                        }
                    }
                }
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
                            mMap.addMarker(new MarkerOptions().position(sydney).title("Position actuelle")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.me)));
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

                            mMap.animateCamera(
                                    CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),
                                            location.getLongitude()), 12.0f));
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

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Toast.makeText(getApplicationContext(), marker.getTitle(), Toast.LENGTH_LONG).show();
                chosen = marker;
                AlertDialog.Builder b = new AlertDialog.Builder(MapsActivity.this);

                LayoutInflater inflater = getLayoutInflater();
                View dialogLayout = inflater.inflate(R.layout.dialog, null);
                b.setView(dialogLayout);
                TextView textView = dialogLayout.findViewById(R.id.info2);
                Button button = dialogLayout.findViewById(R.id.reserver_bouton);

                textView.setText(chosen.getTitle()+"\n"+chosen.getSnippet());
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(), Reservation.class);
                        intent.putExtra("titre", chosen.getTitle());
                        intent.putExtra("snippet", chosen.getSnippet());
                        startActivity(intent);
                    }
                });
                b.show();
                return false;
            }
        });
    }

    public final static double AVERAGE_RADIUS_OF_EARTH_KM = 6371;

    public float calculateDistanceInKilometer(double userLat, double userLng, double venueLat, double venueLng) {
        double latDistance = Math.toRadians(userLat - venueLat);
        double lngDistance = Math.toRadians(userLng - venueLng);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(userLat)) * Math.cos(Math.toRadians(venueLat))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return (float) ((AVERAGE_RADIUS_OF_EARTH_KM * c));
    }
}
