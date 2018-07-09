package ridesharers.ucsc.edu.ucsharecar;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import org.json.JSONException;
import org.json.JSONObject;

public class BackendClient {

    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "UCShareCar_BackendCli";
    private static final String URL = "http://10.0.1.39:8000/";

    private GoogleSignInClient mGoogleSignInClient;
    private RequestQueue queue;
    private Context parentContext;

    // Instance is just the reference to the only instance of this class that will ever exist
    private static BackendClient instance = null;

    // The constructor of this class is
    private BackendClient(Context context) {
        // TODO -- is there any downside to using the first context that gets the backend client singleton?
        // Set up the request queue
        queue = Volley.newRequestQueue(context);
    }

    public static BackendClient GetSingleton(Context context) {
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

    /*
    Everything from here on out is classes that defines results from servers.
    This allows us to skip a lot of JSON extracting try/except blocks that I am not a fan of.
     */
    public class SignInResult {
        private boolean success, not_registered;
        SignInResult(JSONObject data) {
            try {
                success = data.getBoolean("success");
                not_registered = data.getBoolean("not_registered");
            }
            catch (Exception e) {
                Log.e(TAG, "Failed to parse a SignInResult");
                success = false;
                not_registered = false;
            }
        }

        public boolean Succeeded() {
            return success;
        }

        public boolean notRegistered() {
            return not_registered;
        }
    }
}
