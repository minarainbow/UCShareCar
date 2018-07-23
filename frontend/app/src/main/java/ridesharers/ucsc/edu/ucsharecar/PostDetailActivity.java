package ridesharers.ucsc.edu.ucsharecar;

import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.ArrayList;
import java.util.List;

/**
 * An activity representing a single Post detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link PostListActivity}.
 */
public class PostDetailActivity extends AppCompatActivity {

    private static final String TAG = "PostDetailActivity";

    String startingLocation, endingLocation, departureTime, names, memos, post_id, driver;
    boolean driver_status, is_joined;
    ListViewAdapter listViewAdapter;
    ListView listView;
    ArrayList<String> passengers;
    AlertDialog.Builder builder;
    AlertDialog popup;
    BackendClient backend;
    Context mContext;
    int seats;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        mContext = this;
        Log.d(TAG, "onCreate: started.");

        listView = findViewById(R.id.passengerList);
        getIncomingIntent();
        backend = BackendClient.getSingleton(this);

        Button join = (Button) findViewById(R.id.fab);
        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(driver_status) {
                    Log.e("add", "driver");
                    builder = new AlertDialog.Builder(mContext);
                    View mView = getLayoutInflater().inflate(R.layout.activity_driver_join, null);
                    final EditText input = mView.findViewById(R.id.input_seats);
                    Button joinButton = mView.findViewById(R.id.confirm);
                    Button noJoinButton = mView.findViewById(R.id.no_join);
                    builder.setView(mView);
                    builder.create().show();

                    joinButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String availString = input.getText().toString();
                            int avail = Integer.parseInt(availString);
                            backend.addDriver(post_id, avail, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    Log.e("driver", "added well");
                                    Toast.makeText(getApplicationContext(), "Successfully Added", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.e(TAG, error.toString());
                                    Toast.makeText(getApplicationContext(), "No seats available", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    });

                    noJoinButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            popup.cancel();
                        }
                    });
                }
                else {
                    backend.addPassenger(post_id, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.e("passenger", "added well");
                            Toast.makeText(getApplicationContext(), "Successfully Added", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
//                            Log.e(TAG, error.toString());
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
                getIntent().hasExtra("avail_seats") &&
                getIntent().hasExtra("notes")) {

            Log.d(TAG, "getIncomingIntent: found intent extras.");
            startingLocation = getIntent().getStringExtra("starting_loc");
            endingLocation = getIntent().getStringExtra("ending_loc");
            departureTime = getIntent().getStringExtra("leaving_time");
            memos = getIntent().getStringExtra("notes");
            seats = getIntent().getIntExtra("avail_seats", 0);
            driver_status = getIntent().getBooleanExtra("driver_status", true);
            post_id = getIntent().getStringExtra("post_id");
            passengers = getIntent().getStringArrayListExtra("passengers");
            is_joined = getIntent().getBooleanExtra("join", false);
            driver = getIntent().getStringExtra("driver");

            setPostDetails(startingLocation, endingLocation, departureTime, seats, names, memos, driver_status, passengers);
        }
    }

    private void setPostDetails(String startingLocation, String endingLocation,
                                String departureTime, int seats, String names,
                                String memos, boolean driver_status, ArrayList<String> passengers){
        TextView origin = findViewById(R.id.starting_loc);
        origin.setText(startingLocation);

        TextView destination = findViewById(R.id.ending_loc);
        destination.setText(endingLocation);

        TextView leaving_time = findViewById(R.id.leaving_time);
        leaving_time.setText(departureTime.split("P")[0]);

        TextView avail_seats = findViewById(R.id.avail_seats);
        avail_seats.setText("" + seats);

        TextView notes = findViewById(R.id.notes);
        notes.setText(memos);

        TextView driver_status_text = findViewById(R.id.driver_status);
        if(driver_status) {
            driver_status_text.setText("Join as a driver?");
        }
        else {
            driver_status_text.setText("Join as a passenger?");
        }

        if(is_joined) {
            Log.e("passegners", passengers.toString());

            LinearLayout joinLayout = (LinearLayout) findViewById(R.id.join_layout);
            joinLayout.setVisibility(View.GONE);

            if(!driver_status) {
                passengers.add(0, driver);
            }

            Log.e("passengers", passengers.toString());
            listViewAdapter = new ListViewAdapter(mContext, passengers);
            listView.setAdapter(listViewAdapter);
        }
    }

    private class ListViewAdapter extends ArrayAdapter<String> {
        private ArrayList<String> passengerList;
        private Context mContext;

        private class ViewHolder {
            TextView ucsc_id;
            TextView phNum;
        }

        private ListViewAdapter(Context context, ArrayList<String> passengers) {
            super(context, R.layout.contact_info, passengers);
            mContext = context;
            this.passengerList = passengers;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder viewHolder;
            String user_id = passengerList.get(position);

            if(convertView == null) {
                viewHolder = new ViewHolder();
                LayoutInflater layoutInflater = LayoutInflater.from(mContext);
                convertView = layoutInflater.inflate(R.layout.contact_info, parent, false);
                viewHolder.ucsc_id = convertView.findViewById(R.id.ucsc_id);
                viewHolder.phNum = convertView.findViewById(R.id.phnum);

                convertView.setTag(viewHolder);
            }
            else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            // Blank out the text while we wait for the server
            viewHolder.ucsc_id.setText("");
            viewHolder.phNum.setText("");

            backend.getUserById(user_id, new Response.Listener<UserInfo>() {
                @Override
                public void onResponse(UserInfo response) {
                    Log.e("response", response.toString());
                    if(position == 0 && !driver_status) {
                        viewHolder.ucsc_id.setText("Driver :\n" + response.getName());
                    }
                    else {
                        viewHolder.ucsc_id.setText(response.getName());
                    }
                    viewHolder.phNum.setText(response.getPhoneNumber());
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("user_info", error.toString());
                }
            });

            return convertView;
        }
    }
}
