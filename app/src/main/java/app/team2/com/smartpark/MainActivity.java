package app.team2.com.smartpark;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    EditText txt_num;
    Button btn_confirmer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txt_num = findViewById(R.id.txt_num);
        btn_confirmer = findViewById(R.id.btn_confirmer);

        btn_confirmer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String num = txt_num.getText().toString();
                Toast.makeText(getApplicationContext(), num, Toast.LENGTH_LONG).show();
            }
        });
    }
}
