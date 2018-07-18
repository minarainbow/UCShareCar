package ridesharers.ucsc.edu.ucsharecar;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class MyPage extends AppCompatActivity {
    private ListView uploadedView, matchedView;
    private ArrayList<PostInfo> uploaded, matched;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_page);

        uploadedView = (ListView) findViewById(R.id.listView1);
        matchedView = (ListView) findViewById(R.id.listView2);

        uploaded = new ArrayList<>();
        matched = new ArrayList<>();

        setListView();
    }

    private void setListView() {
        ListViewAdapter uploadAdapter = new ListViewAdapter(this, uploaded);
        ListViewAdapter matchedAdapter = new ListViewAdapter(this, matched);

        uploadedView.setAdapter(uploadAdapter);
        matchedView.setAdapter(matchedAdapter);
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

                convertView.setTag(viewHolder);
            }
            else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.origin.setText(postInfo.getStart());
            viewHolder.destination.setText(postInfo.getEnd());

            return convertView;
        }
    }
}
