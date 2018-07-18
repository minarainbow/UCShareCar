package ridesharers.ucsc.edu.ucsharecar;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.util.ArrayList;

// TODO this class should detect authorization errors and start the login window accordddingly
public class BackendClient {

    private static final String TAG = "UCShareCar_BackendCli";
    private static final String URL = "http://18.220.253.162:8000";

    private RequestQueue queue;
    private CookieManager cookieManager;

    // The userId field will be set when there is a successful log in request. It can be accessed
    // via the getUserId method. TODO does this make hasSession() redundant?
    private String userId = null;

    // Instance is just the reference to the only instance of this class that will ever exist
    private static BackendClient instance = null;

    // The constructor of this class is
    private BackendClient(Context context) {
        // TODO -- is there any downside to using the first context that gets the backend client singleton?
        // Set up the request queue
        queue = Volley.newRequestQueue(context);

        // Store the cookies! They store the session information from the server.
        cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);
    }

    public static BackendClient getSingleton(Context context) {
        if (instance == null) {
            instance = new BackendClient(context);
        }
        return instance;
    }

    // Returns true if there is a session with the server.
    public boolean hasSession() {
        for (HttpCookie cookie : cookieManager.getCookieStore().getCookies()) {
            if (cookie.getName().equals("session") && !cookie.hasExpired()) {
                return true;
            }
        }
        return false;
    }

    public String getUserId() {
        return userId;
    }

    // SignIn tries to sign an account with the backend server, calling the given response listener
    // on the result.
    public void SignIn(GoogleSignInAccount account, final Response.Listener<SignInResult> responseCallback,
                       Response.ErrorListener errorCallback) {
        // First we need to make a JSON data object for the POST arguments
        JSONObject jsonPostParamaters = new JSONObject();
        try {
            jsonPostParamaters.put("token", account.getIdToken());
        } catch (JSONException e) {
            Log.w(TAG, "Failed to create JSON object to validate login");
            errorCallback.onErrorResponse(null);
            return;
        }

        // Create the whole post request
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                URL + "/users/login", jsonPostParamaters,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Save the userid if it's in there
                        if (response.has("user_id")) {
                            try {
                                userId = response.getString("user_id");
                            } catch (JSONException e) {
                                Log.w(TAG, "Response has userid, but failed to read it: "+e);
                            }
                        }
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
            jsonPostParamaters.put("phnum", phnum);
        } catch (JSONException e) {
            Log.w(TAG, "Failed to create JSON object to register phone #");
            errorCallback.onErrorResponse(null);
            return;
        }

        // Create the whole post request
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                URL + "/users/register", jsonPostParamaters,
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


    // Gets all of the posts from the server. Returns a PostInfo array to the callback if it is
    // successful, otherwise the errorCallback is called (likely with a null error, check the logs)
    public void getAllPosts(final Response.Listener<ArrayList<PostInfo>> responseCallback,
                            final Response.ErrorListener errorCallback) {

        // Set up request and callbacks
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                URL + "/posts/all", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    // Check for valid response
                    if (hasError(response)) {
                        Log.w(TAG, "Got a bad result: "+response.getString("error"));
                        errorCallback.onErrorResponse(null);
                        return;
                    }

                    // Parse posts
                    ArrayList<PostInfo> posts = new ArrayList<PostInfo>();
                    JSONArray jsonArray = response.getJSONArray("posts");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        posts.add(new PostInfo(jsonArray.getJSONObject(i)));
                    }

                    // Send posts to callee
                    responseCallback.onResponse(posts);
                } catch (JSONException e) {
                    // If parsing fails, we fail
                    Log.w(TAG, "Could not parse posts from /posts/all: "+e.toString());
                    errorCallback.onErrorResponse(null);
                }
            }
        }, errorCallback);

        // Send request
        queue.add(request);
    }

    // Saves a PostInfo object to the database. responseCallback will always be called with the
    // ID of the new post.
    public void createPost(PostInfo post, final Response.Listener<String> responseCallback,
                           final Response.ErrorListener errorCallback) {

        // First we need to make a JSON data object for the POST arguments
        JSONObject jsonPostParamaters = new JSONObject();
        try {
            jsonPostParamaters.put("post", post.getJSON());
        } catch (JSONException e) {
            Log.w(TAG, "Failed to create JSON object to upload post:" +e.toString());
            errorCallback.onErrorResponse(null);
            return;
        }

        // Set up request and callbacks
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                URL + "/posts/create", jsonPostParamaters, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    // Check for valid response
                    if (hasError(response)) {
                        Log.w(TAG, "Got a bad result for post creation: "+response.getString("error"));
                        errorCallback.onErrorResponse(null);
                        return;
                    }

                    // Send success to callee
                    responseCallback.onResponse(response.getString("post_id"));
                } catch (JSONException e) {
                    // If parsing fails, we fail
                    Log.w(TAG, "Could not send post to create: "+e.toString());
                    errorCallback.onErrorResponse(null);
                }
            }
        }, errorCallback);

        // Send request
        queue.add(request);
    }

    // This method will send a PostInfo object to the server to *update*. Whatver is in the PostInfo
    // object _will_ be saved by the server, so make sure it is correct. On success, the
    // responseCallback will be called with the posts's id (for consistency).
    // Note that if you want to update a PostInfo, you should do these steps in order:
    // 1. PostInfo.offlineAddDriver(...) or PostInfo.offlineAddPassenger(...)
    // 2. backend.updatePost(PostInfo ...)
    public void updatePost(final PostInfo post, Response.Listener<String> responseCallback,
                           Response.ErrorListener errorCallback) {

        // Build the request
        GenericRequest<String> request = new GenericRequest<String>("/posts/update",
                Request.Method.POST, responseCallback, errorCallback) {
            void buildParameters(JSONObject object) throws JSONException {
                object.put("post", post.getJSON());
            }
            String parseResponse(JSONObject object) throws JSONException {
                return post.getId();
            }
        };

        // Run it
        request.run();
    }

    public void getPostById(String id, final Response.Listener<PostInfo> responseCallback,
                           final Response.ErrorListener errorCallback) {

        // Set up request and callbacks
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                URL + "/posts/by_id/"+id, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    // Check for valid response
                    if (hasError(response)) {
                        Log.w(TAG, "Got a bad result for post by id: "+response.getString("error"));
                        errorCallback.onErrorResponse(null);
                        return;
                    }

                    // Send success to callee
                    responseCallback.onResponse(new PostInfo(response.getJSONObject("post")));
                } catch (JSONException e) {
                    // If parsing fails, we fail
                    Log.w(TAG, "Could not get post by id: "+e.toString());
                    errorCallback.onErrorResponse(null);
                }
            }
        }, errorCallback);

        // Send request
        queue.add(request);
    }

    // This is very similar to getPostById. Accepts a string argument (the ObjectId of the user you
    // want, the same as what you get from PostInfo.getPassengers() or this.getUserId()). Returns a
    // UserInfo object via response listener, otherwise the errorCallback is called.
    public void getUserById(String id, final Response.Listener<UserInfo> responseCallback,
                            final Response.ErrorListener errorCallback) {

        // Set up request and callbacks
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                URL + "/users/by_id/"+id, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    // Check for valid response
                    if (hasError(response)) {
                        Log.w(TAG, "Got a bad result for user by id: "+response.getString("error"));
                        errorCallback.onErrorResponse(null);
                        return;
                    }

                    // Send success to callee
                    responseCallback.onResponse(new UserInfo(response.getJSONObject("user")));
                } catch (JSONException e) {
                    // If parsing fails, we fail
                    Log.w(TAG, "Could not get user by id: "+e.toString());
                    errorCallback.onErrorResponse(null);
                }
            }
        }, errorCallback);

        // Send request
        queue.add(request);
    }

    public void createReport(final ReportInfo report, final Response.Listener<String> responseCallback,
                             final Response.ErrorListener errorCallback) {

        // Build the request
        GenericRequest<String> request = new GenericRequest<String>("/report/create",
                Request.Method.POST, responseCallback, errorCallback) {
            void buildParameters(JSONObject object) throws JSONException {
                object.put("report", report.getJSON());
            }
            String parseResponse(JSONObject object) throws JSONException {
                return object.getString("report_id");
            }
        };

        // Run it
        request.run();
    }

    // GenericRequest attempts to make generic the code to write new requests.
    abstract class GenericRequest<T> {
        abstract void buildParameters(JSONObject object) throws JSONException;
        abstract T parseResponse(JSONObject object) throws JSONException;

        String endpoint;
        int method;
        Response.Listener<T> responseCallback;
        Response.ErrorListener errorCallback;

        GenericRequest(final String endpoint, int method, final Response.Listener<T> responseCallback,
                                 final Response.ErrorListener errorCallback) {
            this.endpoint = endpoint;
            this.method = method;
            this.responseCallback = responseCallback;
            this.errorCallback = errorCallback;
        }

        void run() {
            JSONObject jsonPostParamaters = null;

            // If the method is POST, then we construct the arguments. Otherwise we can skip it.
            if (method == Request.Method.POST) {

                // To construct the arguments, create the object and have our builder put the
                // callee's info in it
                jsonPostParamaters = new JSONObject();
                try {
                    buildParameters(jsonPostParamaters);
                } catch (JSONException e) {
                    Log.w(TAG, "Failed to create JSON object for " + endpoint + "'s args:" + e.toString());
                    errorCallback.onErrorResponse(null);
                    return;
                }
            }

            // Set up request and callbacks
            JsonObjectRequest request = new JsonObjectRequest(method,
                    URL + endpoint, jsonPostParamaters, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        // Check for valid response
                        if (hasError(response)) {
                            Log.w(TAG, "Got a bad result for " + endpoint + ": " + response.getString("error"));
                            errorCallback.onErrorResponse(null);
                            return;
                        }

                        // Send success to callee
                        responseCallback.onResponse(parseResponse(response));
                    } catch (JSONException e) {
                        // If parsing fails, we fail
                        Log.w(TAG, "Request to " + endpoint + " failed: " + e.toString());
                        errorCallback.onErrorResponse(null);
                    }
                }
            }, errorCallback);

            // Send request
            queue.add(request);
        }
    }

    private boolean hasError(JSONObject response) throws JSONException {
        return response.has("error") || response.getInt("result") != 1;
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
