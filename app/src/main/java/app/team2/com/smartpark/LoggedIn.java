package app.team2.com.smartpark;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;

public class LoggedIn extends AppCompatActivity {

    private DatabaseReference mDatabase;

    private FusedLocationProviderClient mFusedLocationClient;
    private Location location;

    TextView loc;
    ListView list_parking;
    ArrayList<Parking> listParking = new ArrayList<>();
    ParkingAdapter parkingAdapter;

    public static final int ACCESSLOCATION=123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        loc = findViewById(R.id.loc);
        list_parking = findViewById(R.id.list_parking);

        parkingAdapter = new ParkingAdapter(this, listParking);
        list_parking.setAdapter(parkingAdapter);

        mDatabase.child("Parkings").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> i = dataSnapshot.getChildren().iterator();
                while (i.hasNext()) {
                    DataSnapshot data = i.next();
                    Parking park = data.getValue(Parking.class);
                    listParking.add(park);
                }
                parkingAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        checkPermission();
    }

    public void checkPermission(){
        if(Build.VERSION.SDK_INT >= 23){
            if(ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
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
                            loc.setText(String.valueOf(location.getLatitude()) + "-"
                                    + String.valueOf(location.getLongitude()));
                            LoggedIn.this.location = location;
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

    public class ParkingAdapter extends BaseAdapter{

        Context context;
        ArrayList<Parking> listParking;

        public ParkingAdapter(Context context, ArrayList<Parking> listParking){
            this.context = context;
            this.listParking = listParking;
        }

        @Override
        public int getCount() {
            return listParking.size();
        }

        @Override
        public Object getItem(int position) {
            return listParking.get(position);
        }

        @Override
        public long getItemId(int position) {
            return Long.parseLong(String.valueOf(position));
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Parking parking = listParking.get(position);

            LayoutInflater inflater = getLayoutInflater();
            convertView = inflater.inflate(R.layout.item_parking, null);

            TextView nom = convertView.findViewById(R.id.nom_park);
            TextView dist = convertView.findViewById(R.id.dist_park);

            float distance = calculateDistanceInKilometer(location.getLatitude(), location.getLongitude(),
                    parking.getLat(), parking.getLon());

            nom.setText(parking.getNom());
            dist.setText("Distance: " + String.valueOf(distance) + " KM");

            return convertView;
        }
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
