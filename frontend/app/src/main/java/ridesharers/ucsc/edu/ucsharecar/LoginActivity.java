package ridesharers.ucsc.edu.ucsharecar;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

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
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "UCShareCar_Login";
    private static final String URL = "http://169.233.230.209:8000/";

    private GoogleSignInClient mGoogleSignInClient;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .setHostedDomain("ucsc.edu")
                .requestEmail()
                .requestIdToken(getString(R.string.server_client_id))
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Set up the request queue
        queue = Volley.newRequestQueue(this);
    }

    public void onClickLogin(View view) {
        Log.w(TAG, "Log in button clicked; opening login activity");
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        final GoogleSignInAccount account;
        try {
            account = completedTask.getResult(ApiException.class);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            dispToast("Log in failed. Try again.");
            // TODO Failed sign in route
            return;
        }

        // Log in was successful at this point
        Log.w(TAG, "Got a successful login client-side from "+account.getEmail());

        // Now check it was valid with the server.

        // First we need to make a JSON data object
        JSONObject jsonPostParamaters = new JSONObject();
        try {
            jsonPostParamaters = new JSONObject();
            jsonPostParamaters.put("token", account.getIdToken());
        } catch (JSONException e) {
            Log.w(TAG, "Failed to create JSON object to validate login");
            // TODO Failed sign in route
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                URL+"users/register",
                jsonPostParamaters,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        boolean success = false;
                        try {
                            success = response.getBoolean("success");
                        }
                        catch (Exception e) {
                            Log.w(TAG, "Failed to parse JSON result for login validation");
                            // We can pass on this since success starts as false.
                        }

                        if (success) {
                            dispToast("Successfully logged in " + account.getEmail());
                        }
                        else {
                            dispToast("Log in failed. Try again.");
                            Log.w(TAG, "Server rejected log in");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dispToast("Log in failed. Try again.");
                        Log.w(TAG, "Error occurred when validating login: "+error);
                    }
                });

        // Send the POST request to validate the user
        queue.add(request);
    }

    private void dispToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
