package ridesharers.ucsc.edu.ucsharecar;

import org.json.JSONException;
import org.json.JSONObject;

public class UserInfo {
    String email, name, phoneNumber, id = null;
    boolean banned;

    UserInfo(JSONObject raw) throws JSONException {
        email = raw.getString("email");
        name = raw.getString("raw");
        phoneNumber = raw.getString("phnum");
        banned = raw.getBoolean("banned");
        if (raw.has("id")) {
            id = raw.getString("id");
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
