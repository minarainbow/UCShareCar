package ridesharers.ucsc.edu.ucsharecar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
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

    private static final String TAG = "UCShareCar_PostDetail";

    ListViewAdapter listViewAdapter;
    ListView listView;
    AlertDialog.Builder builder;
    AlertDialog popup;
    BackendClient backend;
    Context mContext;

    private PostInfo postInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        mContext = this;
        backend = BackendClient.getSingleton(this);

        Log.d(TAG, "onCreate: started.");

        listView = findViewById(R.id.passengerList);
        getIncomingIntent();

        Button join = (Button) findViewById(R.id.fab);
        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(postInfo.isDriverneeded()) {
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
                            backend.addDriver(postInfo.getId(), avail, new Response.Listener<String>() {
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
                    backend.addPassenger(postInfo.getId(), new Response.Listener<String>() {
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
        Log.d(TAG, "getIncomingIntent: parsing intent extras.");
        Intent intent = getIntent();

        if (intent.hasExtra("post")) {
            Log.d(TAG, "Received a full post object to post detail");
            postInfo = intent.getParcelableExtra("post");
            setPostDetails();
        }
        else if (intent.hasExtra("post_id")) {
            Log.d(TAG, "Received a post id, sending a request to populate the page");

            backend.getPostById(intent.getStringExtra("post_id"), new Response.Listener<PostInfo>() {
                @Override
                public void onResponse(PostInfo response) {
                    postInfo = response;
                    setPostDetails();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.w(TAG, "Failed to get the post!");
                    Log.e(TAG, error.toString());
                    Toast.makeText(getApplicationContext(), "Could not get post details", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void setPostDetails(){
        TextView origin = findViewById(R.id.starting_loc);
        origin.setText(postInfo.getStart());

        TextView destination = findViewById(R.id.ending_loc);
        destination.setText(postInfo.getEnd());

        TextView leaving_time = findViewById(R.id.leaving_time);
        // Split on "P" to get rid of the timezone (PDT, PST)
        leaving_time.setText(postInfo.getDeparttime().toString().split("P")[0]);

        TextView avail_seats = findViewById(R.id.avail_seats);
        avail_seats.setText(""+(postInfo.getTotalseats()-postInfo.getPassengers().size()));

        TextView notes = findViewById(R.id.notes);
        notes.setText(postInfo.getMemo());

        TextView driver_status_text = findViewById(R.id.driver_status);
        if(postInfo.isDriverneeded()) {
            driver_status_text.setText("Join as a driver?");
        }
        else {
            driver_status_text.setText("Join as a passenger?");
        }

        if(postInfo.containsUser(backend.getUserId())) {
            LinearLayout joinLayout = (LinearLayout) findViewById(R.id.join_layout);
            joinLayout.setVisibility(View.GONE);

            ArrayList<String> userList = new ArrayList<>();
            if (!postInfo.isDriverneeded()) {
                userList.add(postInfo.getDriver());
            }
            userList.addAll(postInfo.getPassengers());

            listViewAdapter = new ListViewAdapter(mContext, userList);
            listView.setAdapter(listViewAdapter);
        }
    }

    private class ListViewAdapter extends ArrayAdapter<String> {
        private ArrayList<String> userList;
        private Context mContext;

        private class ViewHolder {
            TextView ucsc_id;
            TextView phNum;
        }

        private ListViewAdapter(Context context, ArrayList<String> users) {
            super(context, R.layout.contact_info, users);
            mContext = context;
            userList = users;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder viewHolder;
            String user_id = userList.get(position);

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
                    if(position == 0 && !postInfo.isDriverneeded()) {
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
