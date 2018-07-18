package ridesharers.ucsc.edu.ucsharecar;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class PostListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private ArrayList<PostInfo> postList = new ArrayList<>();

    public class PostListViewHolder extends RecyclerView.ViewHolder{
        public TextView origin, destination, departure_time, driver_status;

        public PostListViewHolder(View view) {
            super(view);
            origin = (TextView) view.findViewById(R.id.origin);
            destination = (TextView) view.findViewById(R.id.destination);
            departure_time = (TextView) view.findViewById(R.id.departure_time);
            driver_status = (TextView) view.findViewById(R.id.driver_status);
        }
    }

    public PostListAdapter(Context context, ArrayList<PostInfo> postInfos) {
        mContext = context;
        postList = postInfos;
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_listitem, parent, false);
        return new PostListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        PostInfo postInfo = postList.get(position);
        PostListViewHolder post_holder = (PostListViewHolder) holder;
        post_holder.origin.setText(postInfo.getStart());
        post_holder.destination.setText(postInfo.getEnd());
        post_holder.departure_time.setText(postInfo.getDeparttime().toString());
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }
}
