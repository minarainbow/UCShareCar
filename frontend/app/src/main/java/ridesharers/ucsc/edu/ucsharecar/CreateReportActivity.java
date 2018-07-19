package ridesharers.ucsc.edu.ucsharecar;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class CreateReportActivity extends AppCompatActivity {

    private final String TAG = "Report_Activity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_report);

        Button upload_button = findViewById(R.id.ok_editor);
        upload_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent upload_intent = new Intent(getApplicationContext(), CreateReportActivity.class);
                startActivity(upload_intent);
            }
        });



    }
}
