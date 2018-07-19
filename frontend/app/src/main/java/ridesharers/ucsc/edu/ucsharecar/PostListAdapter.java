package ridesharers.ucsc.edu.ucsharecar;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import java.util.ArrayList;

public class PostListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private ArrayList<PostInfo> postList = new ArrayList<>();
    private View.OnClickListener onClickListener;

    public class PostListViewHolder extends RecyclerView.ViewHolder {
        public TextView origin_text, destination_text, departure_time_text, driver_status_text;
        public String origin, destination, departure_time, driver_status, posttime, memo, driver, uploader, totalseats;
        public ArrayList<String> passengers = new ArrayList<String>();

        public PostListViewHolder(View view) {
            super(view);
            origin_text = (TextView) view.findViewById(R.id.origin);
            destination_text = (TextView) view.findViewById(R.id.destination);
            departure_time_text = (TextView) view.findViewById(R.id.departure_time);
            driver_status_text = (TextView) view.findViewById(R.id.driver_status);
        }
    }

    public PostListAdapter(Context context, ArrayList<PostInfo> postInfos) {
        mContext = context;
        postList = postInfos;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_listitem, parent, false);
        view.setOnClickListener(onClickListener);
        return new PostListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        PostInfo postInfo = postList.get(position);
        final PostListViewHolder post_holder = (PostListViewHolder) holder;

        post_holder.origin = postInfo.getStart().toString();
        post_holder.destination = postInfo.getEnd().toString();
        post_holder.departure_time = postInfo.getDeparttime().toString();

        post_holder.posttime = postInfo.getPosttime().toString();
        post_holder.memo = postInfo.getMemo().toString();
        post_holder.uploader = postInfo.getUploader().toString();
        post_holder.totalseats = "" + postInfo.getTotalseats();
        post_holder.passengers = postInfo.getPassengers();

        post_holder.origin_text.setText(post_holder.origin);
        post_holder.destination_text.setText(post_holder.destination);
        post_holder.departure_time_text.setText(post_holder.departure_time);

        this.onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("Hello", "" + position);
                Intent intent = new Intent(mContext, PostDetailActivity.class);
                intent.putExtra("starting_loc", post_holder.origin);
                intent.putExtra("ending_loc", post_holder.destination);
                intent.putExtra("leaving_time", post_holder.departure_time);
                intent.putExtra("avail_seats", post_holder.totalseats);
                mContext.startActivity(intent);
            }
        };
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }
}
