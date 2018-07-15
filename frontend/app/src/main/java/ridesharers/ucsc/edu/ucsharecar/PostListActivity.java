package ridesharers.ucsc.edu.ucsharecar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import ridesharers.ucsc.edu.ucsharecar.dummy.DummyContent;
import de.hdodenhof.circleimageview.CircleImageView;
import ridesharers.ucsc.edu.ucsharecar.dummy.RecyclerViewAdapter;
import ridesharers.ucsc.edu.ucsharecar.PostDefinition;

import java.util.ArrayList;
import java.util.List;


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
    private ArrayList<PostDefinition> postList = new ArrayList<>();

    private BackendClient backend;
    private Context postListContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_list);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        FloatingActionButton fab = findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(postListContext, CreatePostActivity.class);
                postListContext.startActivity(intent);
            }
        });


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
        postList.add(new PostDefinition("Science Hill", "Beach Boardwalk","2:30","4",null, "Freshmen only", true));
        postList.add(new PostDefinition("McHenry Library", "Woodstock's Pizza","4:00", "3", null, "No baggage", false));
        postList.add(new PostDefinition("Porter College","Penny's Ice Creamery","12:00", "4",null, "No alcohol",false));
        postList.add(new PostDefinition("Crown College","San Jose Diridon Station","1:30","2",null,"No music/radio",false));
        postList.add(new PostDefinition("Science Hill", "Beach Boardwalk","2:30","4",null, "Freshmen only", true));
        postList.add(new PostDefinition("McHenry Library", "Woodstock's Pizza","4:00", "3", null, "No baggage", false));
        postList.add(new PostDefinition("Porter College","Penny's Ice Creamery","12:00", "4",null, "No alcohol",false));
        postList.add(new PostDefinition("Crown College","San Jose Diridon Station","1:30","2",null,"No music/radio",false));

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, postList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //recyclerView.setAdapter(new RecyclerViewAdapter(this,tempDestinations, tempDepartureTimes, tempArrivalTimes));
    }

}
