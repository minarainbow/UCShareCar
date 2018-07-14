package ridesharers.ucsc.edu.ucsharecar.dummy;

import android.content.Context;
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
import ridesharers.ucsc.edu.ucsharecar.PostListActivity;
import ridesharers.ucsc.edu.ucsharecar.R;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "RecyclerViewAdapter";

    private ArrayList<String> mDestinations = new ArrayList<String>();
    private ArrayList<String> mDepartureTimes = new ArrayList<String>();
    private ArrayList<String> mArrivalTimes = new ArrayList<String>();
    private Context mContext;

    public RecyclerViewAdapter(Context context, ArrayList<String> destinations, ArrayList<String> departureTimes, ArrayList<String> arrivalTimes) {
        mDestinations = destinations;
        mDepartureTimes = departureTimes;
        mArrivalTimes = arrivalTimes;
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
        holder.destination.setText(mDestinations.get(position));
        holder.departureTime.setText(mDepartureTimes.get(position));
        holder.arrivalTime.setText(mArrivalTimes.get(position));
        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked on: " + mDepartureTimes.get(position));
                Toast.makeText(mContext, mDepartureTimes.get(position), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public int getItemCount() {
        return mDestinations.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView image;
        TextView destination;
        TextView departureTime;
        TextView arrivalTime;
        RelativeLayout parentLayout;
        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            destination = itemView.findViewById(R.id.destination);
            departureTime = itemView.findViewById(R.id.departure_time);
            arrivalTime = itemView.findViewById(R.id.arrival_time);
            parentLayout = itemView.findViewById(R.id.parent_layout);
        }
    }
}

