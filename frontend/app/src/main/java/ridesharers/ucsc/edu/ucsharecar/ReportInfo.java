package ridesharers.ucsc.edu.ucsharecar;

import org.json.JSONException;
import org.json.JSONObject;

public class ReportInfo {
    private String reported, title, body;

    ReportInfo(String reported_user, String title, String body) {
        this.reported = reported_user;
        this.title = title;
        this.body = body;
    }

    public JSONObject getJSON() throws JSONException {
        JSONObject res = new JSONObject();
        res.put("reported", reported);
        res.put("title", title);
        res.put("body", body);
        return res;
    }
}
