package ridesharers.ucsc.edu.ucsharecar.dummy;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import ridesharers.ucsc.edu.ucsharecar.PostDefinition;
import ridesharers.ucsc.edu.ucsharecar.PostDetailActivity;
import ridesharers.ucsc.edu.ucsharecar.PostListActivity;
import ridesharers.ucsc.edu.ucsharecar.R;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "RecyclerViewAdapter";

    private ArrayList<PostDefinition> mpostList = new ArrayList<PostDefinition>();
    private Context mContext;

    public RecyclerViewAdapter(Context context, ArrayList<PostDefinition> postList){
        mpostList = postList;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listitem, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called.");

//            Glide.with(mContext)
//                    .asBitmap()
//                    .load(mImages.get(position))
//                    .into(holder.image);
        holder.origin.setText(mpostList.get(position).getmStart());
        holder.destination.setText(mpostList.get(position).getmEnd());
        holder.departureTime.setText(mpostList.get(position).getmDepartureTime());
        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked on: " + mpostList.get(position).getmDepartureTime());
                Toast.makeText(mContext, mpostList.get(position).getmDepartureTime(), Toast.LENGTH_SHORT).show();
                //Making posts clickable
                Intent intent = new Intent(mContext, PostDetailActivity.class);
                intent.putExtra("starting_loc", mpostList.get(position).getmStart());
                intent.putExtra("ending_loc", mpostList.get(position).getmEnd());
                intent.putExtra("leaving_time", mpostList.get(position).getmDepartureTime());
                intent.putExtra("avail_seats", mpostList.get(position).getmTotalSeats());
                intent.putExtra("passenger_names", mpostList.get(position).getmPassengers());
                intent.putExtra("notes", mpostList.get(position).getmMemo());
                intent.putExtra("driver_status", mpostList.get(position).ismDriverNeeded() ? "Yes" : "No");
                mContext.startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return mpostList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView image;
        TextView origin;
        TextView destination;
        TextView departureTime;
        RelativeLayout parentLayout;
        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            origin = itemView.findViewById(R.id.origin);
            destination = itemView.findViewById(R.id.destination);
            departureTime = itemView.findViewById(R.id.departure_time);
            parentLayout = itemView.findViewById(R.id.parent_layout);
        }
    }
}

