package app.team2.com.smartpark;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class Reservation extends AppCompatActivity {

    DatabaseReference mDatabase;
    FirebaseAuth mAuth;

    final String datetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

    TextView t;
    TextView t2;
    TextView t3;
    TextView t4;
    Button button;

    int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        t = findViewById(R.id.textView5);
        t2 = findViewById(R.id.textView6);
        t3 = findViewById(R.id.textView7);
        t4 = findViewById(R.id.textView8);

        final Bundle b = getIntent().getExtras();
        if(b != null){
            t2.setText(b.getString("titre").split("/")[0]);
            t3.setText("Durée de validité: 30 min");
            Random r = new Random();
            id = r.nextInt(83000)+100000;
            String str = String.valueOf(id);
            t4.setText("Votre code de confirmation:\n" + str.substring(0,2)+"-"+
                    str.substring(2,4)+"-"+str.substring(4,6));
        }

        button = findViewById(R.id.button2);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabase.child("Reservation").
                        child(mAuth.getCurrentUser().getPhoneNumber()).child("Parking").setValue(b.getString("titre").split("/")[0]);
                mDatabase.child("Reservation").
                        child(mAuth.getCurrentUser().getPhoneNumber()).child("Date").setValue(datetime);
                mDatabase.child("Reservation").
                        child(mAuth.getCurrentUser().getPhoneNumber()).child("Code").setValue(id);
            }
        });

    }
}
