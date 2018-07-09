package ridesharers.ucsc.edu.ucsharecar;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

public class BackendClient {

    private static final String TAG = "UCShareCar_BackendCli";
    private static final String URL = "http://169.233.230.209:8000/";

    private RequestQueue queue;

    // Instance is just the reference to the only instance of this class that will ever exist
    private static BackendClient instance = null;

    // The constructor of this class is
    private BackendClient(Context context) {
        // TODO -- is there any downside to using the first context that gets the backend client singleton?
        // Set up the request queue
        queue = Volley.newRequestQueue(context);

        // Store the cookies! They store the session information from the server.
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);
    }

    public static BackendClient getSingleton(Context context) {
        if (instance == null) {
            instance = new BackendClient(context);
        }
        return instance;
    }

    // SignIn tries to sign an account with the backend server, calling the given response listener
    // on the result.
    public void SignIn(GoogleSignInAccount account, final Response.Listener<SignInResult> responseCallback,
                       Response.ErrorListener errorCallback) {
        // First we need to make a JSON data object for the POST arguments
        JSONObject jsonPostParamaters = new JSONObject();
        try {
            jsonPostParamaters = new JSONObject();
            jsonPostParamaters.put("token", account.getIdToken());
        } catch (JSONException e) {
            Log.w(TAG, "Failed to create JSON object to validate login");
            errorCallback.onErrorResponse(null);
        }

        // Create the whole post request
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                URL + "users/login", jsonPostParamaters,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // We handle parsing the JSON so that the activities don't have to
                        SignInResult result = new SignInResult(response);
                        responseCallback.onResponse(result);
                    }
                }, errorCallback);

        // Send the POST request to validate the user
        queue.add(request);
    }

    public void Register(String phnum, final Response.Listener<RegisterResult> responseCallback,
                       Response.ErrorListener errorCallback) {
        // First we need to make a JSON data object for the POST arguments
        JSONObject jsonPostParamaters = new JSONObject();
        try {
            jsonPostParamaters = new JSONObject();
            jsonPostParamaters.put("phnum", phnum);
        } catch (JSONException e) {
            Log.w(TAG, "Failed to create JSON object to register phone #");
            errorCallback.onErrorResponse(null);
        }

        // Create the whole post request
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                URL + "users/register", jsonPostParamaters,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // We handle parsing the JSON so that the activities don't have to
                        RegisterResult result = new RegisterResult(response);
                        responseCallback.onResponse(result);
                    }
                }, errorCallback);

        // Send the POST request to register the user
        queue.add(request);
    }

    /*
    Everything from here on out is classes that defines results from servers.
    This allows us to skip a lot of JSON extracting try/except blocks that I am not a fan of.
     */
    public class SignInResult {
        private JSONObject data;
        SignInResult(JSONObject data) { this.data = data; }
        public boolean Succeeded() {
            try {
                return data.getBoolean("success");
            } catch (Exception e) {
                return false;
            }
        }
        public boolean needsRegister() {
            try {
                return data.getBoolean("needs_register");
            } catch (Exception e) {
                return false;
            }
        }
    }

    public class RegisterResult {
        private JSONObject data;
        RegisterResult(JSONObject data) { this.data = data; }
        public boolean Succeeded() {
            try {
                return data.getBoolean("success");
            } catch (Exception e) {
                return false;
            }
        }
        public String Error() {
            try {
                return data.getString("error");
            } catch (Exception e) {
                return null;
            }
        }
    }
}
