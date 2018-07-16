package ridesharers.ucsc.edu.ucsharecar;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

public class PostInfo {
    private String TAG = "PostInfo";

    private Date posttime, departtime;
    private String start, end, memo;
    private boolean driverneeded;
    // ObjectId fields are saved as String
    private String driver, uploader;
    private ArrayList<String> passengers;
    private int totalseats;
    private String id;

    PostInfo(Date posttime, Date departtime, String start, String end, String memo,
             boolean driverneeded, String driver, String uploader,
             ArrayList<String> passengers, int totalseats) {
        this.posttime = posttime;
        this.departtime = departtime;
        this.start = start;
        this.end = end;
        this.memo = memo;
        this.driverneeded = driverneeded;
        this.driver = driver;
        this.uploader = uploader;
        this.passengers = passengers;
        this.totalseats = totalseats;
        this.id = null;
    }

    PostInfo(JSONObject raw) throws JSONException {
        // First get fields that we know should be there
        this.id = raw.getString("_id");
        this.posttime = new Date(raw.getLong("posttime"));
        this.departtime = new Date(raw.getLong("departtime"));
        this.start = raw.getString("start");
        this.end = raw.getString("end");
        this.totalseats = raw.getInt("totalseats");
        this.memo = raw.getString("memo");
        this.uploader = raw.getString("uploader");
        this.driverneeded = raw.getBoolean("driverneeded");
        JSONArray tmp_passengers = raw.getJSONArray("passengers");
        this.passengers = new ArrayList<String>(tmp_passengers.length());
        for (int i = 0; i < tmp_passengers.length(); i++) {
            this.passengers.add(tmp_passengers.getString(i));
        }

        // Driver is an optional field.
        try {
            if (!this.driverneeded) {
                this.driver = raw.getString("driver");
            }
            else {
                this.driver = null;
            }
        } catch (JSONException e) {
            Log.w(TAG, "Could not get driver field from Post JSON object");
            this.driver = null;
        }

    }

    public JSONObject getJSON() throws JSONException {
        JSONObject res = new JSONObject();
        res.put("driverneeded", driverneeded);
        res.put("driver", driver);
        res.put("posttime", posttime.getTime());
        res.put("departtime", departtime.getTime());
        res.put("start", start);
        res.put("end", end);
        res.put("totalseats", totalseats);
        res.put("memo", memo);
        res.put("uploader", uploader);
        res.put("passengers", new JSONArray(passengers));
        return res;
    }

    public String getUploader() {
        return uploader;
    }

    public void setUploader(String uploader) {
        this.uploader = uploader;
    }

    public int getTotalseats() {
        return totalseats;
    }

    public void setTotalseats(int totalseats) {
        this.totalseats = totalseats;
    }

    public ArrayList<String> getPassengers() {
        return passengers;
    }

    public void setPassengers(ArrayList<String> passengers) {
        this.passengers = passengers;
    }

    public boolean isDriverneeded() {
        return driverneeded;
    }

    public void setDriverneeded(boolean driverneeded) {
        this.driverneeded = driverneeded;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public Date getPosttime() {
        return posttime;
    }

    public void setPosttime(Date posttime) {
        this.posttime = posttime;
    }

    public Date getDeparttime() {
        return departtime;
    }

    public void setDeparttime(Date departtime) {
        this.departtime = departtime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
