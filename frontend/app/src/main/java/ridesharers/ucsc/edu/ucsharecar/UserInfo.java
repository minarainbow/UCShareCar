package ridesharers.ucsc.edu.ucsharecar;

import org.json.JSONException;
import org.json.JSONObject;

public class UserInfo {
    private String email, name, phoneNumber = null, id = null;
    private boolean banned;

    UserInfo(JSONObject raw) throws JSONException {
        email = raw.getString("email");
        name = raw.getString("name");
        banned = raw.getBoolean("banned");
        if (raw.has("phnum")) {
            phoneNumber = raw.getString("phnum");
        }
        if (raw.has("_id")) {
            id = raw.getString("_id");
        }
    }

    public boolean isBanned() {
        return banned;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}
