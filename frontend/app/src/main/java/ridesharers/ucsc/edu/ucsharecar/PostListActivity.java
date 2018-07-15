package ridesharers.ucsc.edu.ucsharecar;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import de.hdodenhof.circleimageview.CircleImageView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_list);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);


            /*
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.w(TAG, "User clicked FAB, going to CreatePostActivity");
                Intent intent = new Intent(PostListActivity.this, CreatePostActivity.class);
                startActivity(intent);
            }
        });/*

        if (findViewById(R.id.post_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }*/

//        View recyclerView = findViewById(R.id.post_list);
//        assert recyclerView != null;
//        setupRecyclerView((RecyclerView) recyclerView);

//        RecyclerView recyclerView = findViewById(R.id.recycler_view);
//        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, mDestinations, mDepartureTimes, mArrivalTimes);
//        recyclerView.setAdapter(adapter);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Get the backend object
        backend = BackendClient.getSingleton(this);
        setupRecyclerView();
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
        Log.d(TAG, "setupRecyclerView: init recycleview.");
        //These are just test code, but the app should be able to get the info from the data base
        postList.add(new PostInfo(new Date(), new Date(), "start", "end", "memo", true, null, null, null, 5));
//        postList.add(new PostInfo("McHenry Library", "Woodstock's Pizza","4:00", "3", null, "No baggage", false));
//        postList.add(new PostInfo("Porter College","Penny's Ice Creamery","12:00", "4",null, "No alcohol",false));
//        postList.add(new PostInfo("Crown College","San Jose Diridon Station","1:30","2",null,"No music/radio",false));
//        postList.add(new PostInfo("Science Hill", "Beach Boardwalk","2:30","4",null, "Freshmen only", true));
//        postList.add(new PostInfo("McHenry Library", "Woodstock's Pizza","4:00", "3", null, "No baggage", false));
//        postList.add(new PostInfo("Porter College","Penny's Ice Creamery","12:00", "4",null, "No alcohol",false));
//        postList.add(new PostInfo("Crown College","San Jose Diridon Station","1:30","2",null,"No music/radio",false));

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, postList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //recyclerView.setAdapter(new RecyclerViewAdapter(this,tempDestinations, tempDepartureTimes, tempArrivalTimes));
    }

}
