package ridesharers.ucsc.edu.ucsharecar;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * An activity representing a single Post detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link PostListActivity}.
 */
public class PostDetailActivity extends AppCompatActivity {

    private static final String TAG = "PostDetailActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        Log.d(TAG, "onCreate: started.");

        getIncomingIntent();
    }

    private void getIncomingIntent(){
        Log.d(TAG,"getIncomingIntent: checking for incoming intents.");
        if(getIntent().hasExtra("starting_loc") &&
                getIntent().hasExtra("ending_loc") &&
                getIntent().hasExtra("leaving_time") &&
                getIntent().hasExtra("avail_seats") &&
                getIntent().hasExtra("passenger_names") &&
                getIntent().hasExtra("notes") &&
                getIntent().hasExtra("driver_status")) {

            Log.d(TAG, "getIncomingIntent: found intent extras.");
            String startingLocation = getIntent().getStringExtra("starting_loc");
            String endingLocation = getIntent().getStringExtra("ending_loc");
            String departureTime = getIntent().getStringExtra("leaving_time");
            String seats = getIntent().getStringExtra("avail_seats");
            String names = getIntent().getStringExtra("passenger_names");
            String memos = getIntent().getStringExtra("notes");
            String driverStatus = getIntent().getStringExtra("driver_status");

            setPostDetails(startingLocation, endingLocation, departureTime, seats, names, memos, driverStatus);
        }
    }

    private void setPostDetails(String startingLocation, String endingLocation,
                                String departureTime, String seats, String names,
                                String memos, String driverStatus){
        Log.d(TAG, "setPostDetails: setting the post details to widgets");

        TextView origin = findViewById(R.id.starting_loc);
        origin.setText(startingLocation);

        TextView destination = findViewById(R.id.ending_loc);
        destination.setText(endingLocation);

        TextView leaving_time = findViewById(R.id.leaving_time);
        leaving_time.setText(departureTime);

        TextView avail_seats = findViewById(R.id.avail_seats);
        avail_seats.setText(seats);

        //TextView passenger_names = findViewById(R.id.passenger_names);
        //passenger_names.setText(names);

        TextView notes = findViewById(R.id.notes);
        notes.setText(memos);

        TextView driver_status = findViewById(R.id.driver_status);
        driver_status.setText(driverStatus);

        //ImageView image = findViewById(R.id.post_pic);
    }
}
