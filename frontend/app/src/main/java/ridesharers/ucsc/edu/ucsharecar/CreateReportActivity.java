package ridesharers.ucsc.edu.ucsharecar;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

public class CreateReportActivity extends AppCompatActivity {

    private final String TAG = "Report_Activity";
    private BackendClient backend;
    private AlertDialog.Builder builder;
    private AlertDialog popup;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_report);

        //Get backend object
        backend = BackendClient.getSingleton(this);

        //EditTexts
        final EditText title_text = findViewById(R.id.title);
        final EditText details_text = findViewById(R.id.details);
        final EditText contact_text = findViewById(R.id.contact);

        //Button click for upload button
        Button upload_button = findViewById(R.id.ok_editor);
        upload_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                builder = new AlertDialog.Builder(CreateReportActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.report_upload_check,null);
                Button yesButton = mView.findViewById(R.id.yes);
                builder.setView(mView);
                popup = builder.create();

                //Button click for upload report button
                yesButton.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        Log.d(TAG, "clicked on upload button from post");
                        String title = title_text.getText().toString();
                        String details = details_text.getText().toString();
                        String contact = contact_text.getText().toString();
                        final ReportInfo myReport = new ReportInfo(contact, title, details);

                        //Email extension
                        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                                "mailto","abc@gmail.com", null));
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
                        emailIntent.putExtra(Intent.EXTRA_TEXT, "Body");
                        startActivity(Intent.createChooser(emailIntent, "Send email..."));

                        backend.createReport(new ReportInfo(contact, title, details), new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                //Log.e(myReport.toString());
                            }
                        }, new ErrorListener() {
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
                            }
                        });

                    }
                });
                            }
        });

        //Button click for back button
        Button back_button = findViewById(R.id.no_editor);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent back_intent = new Intent(getApplicationContext(), PostListActivity.class);
                startActivity(back_intent);
            }
        });



    }
}
