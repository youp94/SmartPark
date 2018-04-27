package app.team2.com.smartpark;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class LoggedIn extends AppCompatActivity {

    private FusedLocationProviderClient mFusedLocationClient;

    TextView loc;

    public static final int ACCESSLOCATION=123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in);

        loc = findViewById(R.id.loc);

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
                            loc.setText(String.valueOf(location.getLatitude()) + " " + String.valueOf(location.getLongitude()));
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
}
