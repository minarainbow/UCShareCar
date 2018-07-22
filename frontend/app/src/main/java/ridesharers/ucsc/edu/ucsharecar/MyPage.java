package ridesharers.ucsc.edu.ucsharecar;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class MyPage extends AppCompatActivity {

    private ListView uploadedView, matchedView;
    private ArrayList<PostInfo> uploaded, matched;
    private BackendClient backendClient;
    ListViewAdapter uploadAdapter, matchedAdapter;
    private Context mContext;

    private String TAG = "UCShareCar_MyPage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_page);
        mContext = this;

        uploadedView = (ListView) findViewById(R.id.listView1);
        matchedView = (ListView) findViewById(R.id.listView2);

        uploaded = new ArrayList<>();
        matched = new ArrayList<>();
        backendClient = BackendClient.getSingleton(this);

        setUserInfo();
        setListView();
    }

    private void setListView() {
        uploadAdapter = new ListViewAdapter(this, uploaded);
        matchedAdapter = new ListViewAdapter(this, matched);

        uploadedView.setAdapter(uploadAdapter);
        matchedView.setAdapter(matchedAdapter);

        backendClient.getMyPage(new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray noMatchArray = response.getJSONArray("no_matches");
                    JSONArray matchArray = response.getJSONArray("matches");
                    Log.e("no_matches", noMatchArray.toString());
                    Log.e("matches", matchArray.toString());
                    for (int i = 0; i < noMatchArray.length(); i++) {
                        uploaded.add(new PostInfo(noMatchArray.getJSONObject(i)));
                    }
                    for (int i = 0; i < matchArray.length(); i++) {
                        matched.add(new PostInfo(matchArray.getJSONObject(i)));
                    }
                    uploadAdapter.notifyDataSetChanged();
                    matchedAdapter.notifyDataSetChanged();
                }
                catch(JSONException e) {

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.toString());
                Toast.makeText(getApplicationContext(), (String) error.toString(), Toast.LENGTH_LONG).show();
            }
        });

        uploadedView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Log.e("upload", "clicked");

                PostInfo postInfo = (PostInfo) adapterView.getItemAtPosition(position);
                String depart_time = postInfo.getDeparttime().toString();

                Intent intent = new Intent(mContext, PostDetailActivity.class);
                int extra = postInfo.getTotalseats() - postInfo.getPassengers().size();
                intent.putExtra("starting_loc", postInfo.getStart());
                intent.putExtra("ending_loc", postInfo.getEnd());
                intent.putExtra("leaving_time", depart_time);
                intent.putExtra("avail_seats", extra);
                intent.putExtra("notes", postInfo.getMemo());
                intent.putExtra("driver_status", postInfo.isDriverneeded());
                intent.putExtra("post_id", postInfo.getId());
                intent.putStringArrayListExtra("passengers", postInfo.getPassengers());
                intent.putExtra("join", true);
                intent.putExtra("driver", postInfo.getDriver());

                mContext.startActivity(intent);
            }
        });

        matchedView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                PostInfo postInfo = (PostInfo) adapterView.getItemAtPosition(position);
                String depart_time = postInfo.getDeparttime().toString();
                Intent intent = new Intent(mContext, PostDetailActivity.class);
                int extra = postInfo.getTotalseats() - postInfo.getPassengers().size();

                intent.putExtra("starting_loc", postInfo.getStart());
                intent.putExtra("ending_loc", postInfo.getEnd());
                intent.putExtra("leaving_time", depart_time);
                intent.putExtra("avail_seats", extra);
                intent.putExtra("notes", postInfo.getMemo());
                intent.putExtra("driver_status", postInfo.isDriverneeded());
                intent.putExtra("post_id", postInfo.getId());
                intent.putStringArrayListExtra("passengers", postInfo.getPassengers());
                intent.putExtra("join", true);
                intent.putExtra("driver", postInfo.getDriver());

                mContext.startActivity(intent);

            }
        });
    }

    private void setUserInfo() {
        backendClient.getUserById(backendClient.getUserId(), new Response.Listener<UserInfo>() {
            @Override
            public void onResponse(UserInfo response) {
                // Put their username in the text box
                TextView usernameText = findViewById(R.id.username);
                usernameText.setText(response.getName());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.w(TAG, "Failed to set user's name: "+error.toString());
            }
        });
    }

    private class ListViewAdapter extends ArrayAdapter<PostInfo> {
        private ArrayList<PostInfo> postInfoArrayList;
        private Context mContext;

        private class ViewHolder {
            TextView origin;
            TextView destination;
            TextView departure_time;
            TextView driver_status;
        }

        private ListViewAdapter(Context context, ArrayList<PostInfo> postInfos) {
            super(context, R.layout.layout_listitem, postInfos);
            mContext = context;
            postInfoArrayList = postInfos;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            PostInfo postInfo = postInfoArrayList.get(position);

            if(convertView == null) {
                viewHolder = new ViewHolder();
                LayoutInflater layoutInflater = LayoutInflater.from(mContext);
                convertView = layoutInflater.inflate(R.layout.layout_listitem, parent, false);
                viewHolder.origin = (TextView) convertView.findViewById(R.id.origin);
                viewHolder.destination = (TextView) convertView.findViewById(R.id.destination);
                viewHolder.departure_time = (TextView) convertView.findViewById(R.id.departure_time);
                viewHolder.driver_status = (TextView) convertView.findViewById(R.id.driver_status);

                viewHolder.origin.setText(postInfo.getStart());
                viewHolder.destination.setText(postInfo.getEnd());
                viewHolder.departure_time.setText(postInfo.getDeparttime().toString().split("P")[0]);

                convertView.setTag(viewHolder);
            }
            else {
                viewHolder = (ViewHolder) convertView.getTag();
            }


            return convertView;
        }
    }
}
