package ridesharers.ucsc.edu.ucsharecar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import ridesharers.ucsc.edu.ucsharecar.dummy.RecyclerViewAdapter;

import java.util.ArrayList;
import java.util.Date;

/**
 * An activity representing a list of Posts. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link PostDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class PostListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    //private boolean mTwoPane;

    private final String TAG = "UCShareCar_PostList";

    //vars
    private ArrayList<PostInfo> postList = new ArrayList<>();

    private BackendClient backend;
    private Context postListContext = this;
    final PostListAdapter adapter = new PostListAdapter(postListContext, postList);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_list);

        //Spinners
        final Spinner originSpinner = findViewById(R.id.start_spinner);
        final Spinner destinationSpinner = findViewById(R.id.destination_spinner);

        // Get the backend object
        backend = BackendClient.getSingleton(this);
        setupRecyclerView();

        ImageButton my_page = findViewById(R.id.my_page);
        my_page.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "clicked my_page button");
                Intent my_page_intent = new Intent(getApplicationContext(), MyPage.class);
                startActivity(my_page_intent);
            }
        });

        ImageButton add_post = findViewById(R.id.add_post);
        add_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "clicked add_post button");
                Intent add_intent = new Intent(getApplicationContext(), CreatePostActivity.class);
                startActivity(add_intent);
            }
        });

        ImageButton add_report = findViewById(R.id.add_report);
        add_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"clicked add_report button");
                Intent this_intent = new Intent(getApplicationContext(), CreateReportActivity.class);
                startActivity(this_intent);
            }
        });


        ImageButton search = findViewById(R.id.search);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Log.d(TAG, "clicked search button");
                    String origin = originSpinner.getSelectedItem().toString();
                    String destination = destinationSpinner.getSelectedItem().toString();
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("start", origin);
                    jsonObject.put("end", destination);

                    backend.getSearch(jsonObject, new Response.Listener<ArrayList<PostInfo>>() {
                        @Override
                        public void onResponse(ArrayList<PostInfo> response) {
                            postList.clear();
                            postList.addAll(response);
                            adapter.notifyDataSetChanged();
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, error.toString());
                            Toast.makeText(getApplicationContext(), (String) error.toString(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
                catch (Exception e) {

                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!backend.hasSession()) {
            Log.w(TAG, "Sending user to login page");
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Log.d(TAG, "setupRecyclerView: init recycleview.");

        backend.getAllPosts(new Response.Listener<ArrayList<PostInfo>>() {
            @Override
            public void onResponse(ArrayList<PostInfo> response) {
                postList.addAll(response);
                adapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.toString());
                Toast.makeText(getApplicationContext(), (String) error.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
