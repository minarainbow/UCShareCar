package ridesharers.ucsc.edu.ucsharecar;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.ArrayList;

/**
 * An activity representing a single Post detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link PostListActivity}.
 */
public class PostDetailActivity extends AppCompatActivity {

    private static final String TAG = "PostDetailActivity";

    String startingLocation, endingLocation, departureTime, names, memos, post_id;
    int seats;
    boolean driver_status;
    private BackendClient backend;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        Log.d(TAG, "onCreate: started.");

        getIncomingIntent();
        backend = BackendClient.getSingleton(this);

        Button join = (Button) findViewById(R.id.fab);
        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(driver_status) {
                    backend.addDriver(post_id, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.e("driver", "added well");
                            Toast.makeText(getApplicationContext(), "Successfully Added", Toast.LENGTH_SHORT).show();
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, error.toString());
                            Toast.makeText(getApplicationContext(), "No seats available", Toast.LENGTH_LONG).show();
                        }
                    });
                }
                else {
                    backend.addPassenger(post_id, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.e("passenger", "added well");
                            Toast.makeText(getApplicationContext(), "Successfully Added", Toast.LENGTH_SHORT).show();
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, error.toString());
                            Toast.makeText(getApplicationContext(), "No seats available", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    private void getIncomingIntent(){
        Log.d(TAG,"getIncomingIntent: checking for incoming intents.");
        if(getIntent().hasExtra("starting_loc") &&
                getIntent().hasExtra("ending_loc") &&
                getIntent().hasExtra("leaving_time") &&
                getIntent().hasExtra("avail_seats") &&
                getIntent().hasExtra("notes")) {

            Log.d(TAG, "getIncomingIntent: found intent extras.");
            startingLocation = getIntent().getStringExtra("starting_loc");
            endingLocation = getIntent().getStringExtra("ending_loc");
            departureTime = getIntent().getStringExtra("leaving_time");
            names = getIntent().getStringExtra("passenger_names");
            memos = getIntent().getStringExtra("notes");
            seats = getIntent().getIntExtra("avail_seats", 0);
            driver_status = getIntent().getBooleanExtra("driver_status", true);
            post_id = getIntent().getStringExtra("post_id");

            setPostDetails(startingLocation, endingLocation, departureTime, seats, names, memos, driver_status);
        }
    }

    private void setPostDetails(String startingLocation, String endingLocation,
                                String departureTime, int seats, String names,
                                String memos, boolean driver_status){
        Log.d(TAG, "setPostDetails: setting the post details to widgets");

        TextView origin = findViewById(R.id.starting_loc);
        origin.setText(startingLocation);

        TextView destination = findViewById(R.id.ending_loc);
        destination.setText(endingLocation);

        TextView leaving_time = findViewById(R.id.leaving_time);
        leaving_time.setText(departureTime);

        TextView avail_seats = findViewById(R.id.avail_seats);
        avail_seats.setText("" + seats);

        //TextView passenger_names = findViewById(R.id.passenger_names);
        //passenger_names.setText(names);

        TextView notes = findViewById(R.id.notes);
        notes.setText(memos);

        TextView driver_status_text = findViewById(R.id.driver_status);
        if(driver_status) {
            driver_status_text.setText("Join as a driver?");
        }
        else {
            driver_status_text.setText("Join as a passenger?");
        }
    }
}
