package ridesharers.ucsc.edu.ucsharecar;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

// TODO this class should detect authorization errors and start the login window accordingly
public class BackendClient {

    private static final String TAG = "UCShareCar_BackendCli";
    private static final String URL = "http://18.220.253.162:8000";
    private static URI URI; // This is set up in the constructor.

    // Shared preferences identifier string
    private static final String PREFS = "ridesharers.ucsc.edu.ucsharecar.sessions";
    // Shared preferences keys
    private static final String USERID_KEY = "userid", CK_VALUE = "sesscookievalue",
                                CK_DOMAIN = "sesscookiedomain", CK_MAXAGE = "sesscookiemaxage",
                                CK_SAVED_AT = "sesscookiesavedat", FCM_TOKEN = "fcmtoken",
                                FCM_REGISTERED = "fcmregistered";

    // Default max cookie age (just in case we lose that info)
    private static long DEFAULT_MAX_AGE = 30 /*hours*/ * 60 /*minutes*/;

    private RequestQueue queue;
    private CookieManager cookieManager;
    private SharedPreferences sessionSettings;

    // The userId field will be set when there is a successful log in request. It can be accessed
    // via the getUserId method. TODO does this make hasSession() redundant?
    private String userId = null;

    // User information has to be fetched a lot when we show posts, and it rarely changes. So we
    // will try to maintain a cache of UserInfo objects.
    // Users will be stored by ObjectId (that's the String key), and will have an attached UserInfo
    private HashMap<String, UserInfo> userCache = new HashMap<>();

    // The request that should be sent ASAP to register the user's push notifications.
    // If this is non-null, it should be called by onStartSession.
    private GenericRequest<Boolean> queuedFCMRegisterRequest = null;

    // For saving the firebase token if we have connectivity issues.
    private String fcmToken = null;
    private boolean fcmRegistered = false;

    // Instance is just the reference to the only instance of this class that will ever exist
    private static BackendClient instance = null;

    // The constructor of this class is private to defeat instantiation. You must use the singleton.
    private BackendClient(Context context) {
        // Set the URI field using the URL. This is required for persisting cookies.
        try {
            // The port cannot be in the URI for the cookies, so we strip it out.
            // TODO if no port is specified, I believe this will fail
            URI = new URI(URL.substring(0, URL.lastIndexOf(':')));
        } catch (URISyntaxException e) {
            Log.w(TAG,"Failed to parse a URI from URL");
            Log.w(TAG, e);
        }

        // TODO -- is there any downside to using the first context that gets the backend client singleton?
        // Set up the request queue
        queue = Volley.newRequestQueue(context);

        // Store the cookies! They store the session information from the server.
        cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);

        // Set up the SharedPreferences to persist cookies
        sessionSettings = context.getSharedPreferences(PREFS, 0);
        // And then retrieve the saved session
        loadSession();

        // Retrieve FCM token. If necessary, queue up a registration
        fcmToken = sessionSettings.getString(FCM_TOKEN, null);
        fcmRegistered = sessionSettings.getBoolean(FCM_REGISTERED, false);
        if (!fcmRegistered) {
            registerFCM(fcmToken);
        }
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

    private void onStartSession(String userId) {
        // Save the session for restarts
        saveSession(userId);

        // Register with FCM if necessary
        if (queuedFCMRegisterRequest != null) {
            queuedFCMRegisterRequest.run();
            queuedFCMRegisterRequest = null;
        }
    }

    private void saveSession(String userId) {
        Log.d(TAG, "Saving a session to sessionSettings");

        // Open the sessionSettings to edit the fields
        SharedPreferences.Editor editor = sessionSettings.edit();

        // Save the user ID
        this.userId = userId;
        editor.putString(USERID_KEY, userId);

        // Save the cookies to disk for later
        for (HttpCookie cookie : cookieManager.getCookieStore().getCookies()) {
            if (cookie.getName().equals("session") && !cookie.hasExpired()) {
                editor.putString(CK_VALUE, cookie.getValue());
                editor.putString(CK_DOMAIN, cookie.getDomain());
                editor.putLong(CK_MAXAGE, cookie.getMaxAge());
                editor.putLong(CK_SAVED_AT, new Date().getTime());
            }
        }

        // Save & close the session settings
        editor.apply();
    }

    private void loadSession() {
        Log.d(TAG, "Loading a session from sessionSettings");

        // We only want to load a session if there is a *cookie* for sure.
        if (sessionSettings.contains(CK_VALUE)) {
            String cookieValue = sessionSettings.getString(CK_VALUE, null);
            Long oldMaxAge = sessionSettings.getLong(CK_MAXAGE, DEFAULT_MAX_AGE);
            Long createdDateMillis = sessionSettings.getLong(CK_SAVED_AT, 0);

            // Compute the time left. Determine expiration date by converting millis to seconds,
            // adding the max age. Then we subtract the current time to see how many seconds left.
            Long timeLeft = ((createdDateMillis/1000)+oldMaxAge) - (new Date().getTime()/1000);
            if (timeLeft < 0) {
                // If there is no time left, we cannot use this session.
                Log.w(TAG, "Old session cookie is probably expired, time left is "+timeLeft);
                return;
            }

            // First retrieve user id (or null if not there -- should not happen)
            userId = sessionSettings.getString(USERID_KEY, null);

            // Rebuild the cookie from the values saved
            HttpCookie cookie = new HttpCookie("session", cookieValue);
            cookie.setDomain(sessionSettings.getString(CK_DOMAIN, null));
            cookie.setMaxAge(timeLeft);
            cookieManager.getCookieStore().add(URI, cookie);
        }
    }

    private void clearSession() {
        Log.d(TAG, "Deleting session info");

        // Open the sessionSettings to edit the fields
        SharedPreferences.Editor editor = sessionSettings.edit();

        editor.clear();

        // Save & close the session settings
        editor.apply();
    }

    public String getUserId() {
        return userId;
    }

    public void registerFCM(final String token) {
        Log.d(TAG, "Received a FCM token to send");
        cacheUnregisteredFCMToken(token);

        // Set up the request to send a token
        GenericRequest<Boolean> request = new GenericRequest<Boolean>("/users/register_fcm",
                Request.Method.POST, new Response.Listener<Boolean>() {
            @Override
            public void onResponse(Boolean response) {
                Log.d(TAG, "Successfully sent FCM token to the server");
                onFCMTokenRegistered();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.w(TAG, "Could not send FCM token to the server");
            }
        }) {
            @Override
            void buildParameters(JSONObject args) throws JSONException {
                args.put("token", token);
            }

            @Override
            Boolean parseResponse(JSONObject response) throws JSONException {
                // There is no actual response to parse for this endpoint
                return true;
            }
        };

        // Run the request if we have a valid session currently
        if (hasSession()) {
            Log.d(TAG, "Sending FCM token now");
            request.run();
        }
        // If there is no session, we have to queue it.
        else {
            Log.d(TAG, "Queuing FCM token send when actually logged in");
            queuedFCMRegisterRequest = request;
        }
    }

    private void cacheUnregisteredFCMToken(String token) {
        // Open the sessionSettings to edit the fields
        SharedPreferences.Editor editor = sessionSettings.edit();

        editor.putString(FCM_TOKEN, token);
        editor.putBoolean(FCM_REGISTERED, false);

        // Save & close the session settings
        editor.apply();
    }

    private void onFCMTokenRegistered() {
        // Open the sessionSettings to edit the fields
        SharedPreferences.Editor editor = sessionSettings.edit();

        // Mark the registration as complete
        editor.putBoolean(FCM_REGISTERED, true);
        fcmRegistered = true;

        // Save & close the session settings
        editor.apply();
    }

    // SignIn tries to sign an account with the backend server, calling the given response listener
    // on the result.
    public void SignIn(GoogleSignInAccount account, final Response.Listener<SignInResult> responseCallback,
                       Response.ErrorListener errorCallback) {
        // First we need to make a JSON data object for the POST arguments
        JSONObject jsonPostParameters = new JSONObject();
        try {
            jsonPostParameters.put("token", account.getIdToken());
        } catch (JSONException e) {
            Log.w(TAG, "Failed to create JSON object to validate login");
            errorCallback.onErrorResponse(null);
            return;
        }

        // Create the whole post request
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                URL + "/users/login", jsonPostParameters,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Save the userid if it's in there
                        if (response.has("user_id")) {
                            try {
                                onStartSession(response.getString("user_id"));
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
        JSONObject jsonPostParameters = new JSONObject();
        try {
            jsonPostParameters.put("phnum", phnum);
        } catch (JSONException e) {
            Log.w(TAG, "Failed to create JSON object to register phone #");
            errorCallback.onErrorResponse(null);
            return;
        }

        // Create the whole post request
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                URL + "/users/register", jsonPostParameters,
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
    public void getAllPosts(Response.Listener<ArrayList<PostInfo>> responseCallback,
                            Response.ErrorListener errorCallback) {

        GenericRequest<ArrayList<PostInfo>> request = new GenericRequest<ArrayList<PostInfo>>(
                "/posts/all", Request.Method.GET, responseCallback, errorCallback) {
            @Override
            void buildParameters(JSONObject args) throws JSONException {}

            @Override
            ArrayList<PostInfo> parseResponse(JSONObject response) throws JSONException {
                // Parse posts
                ArrayList<PostInfo> posts = new ArrayList<PostInfo>();
                JSONArray jsonArray = response.getJSONArray("posts");
                Log.e("posts", jsonArray.toString());
                for (int i = 0; i < jsonArray.length(); i++) {
                    posts.add(new PostInfo(jsonArray.getJSONObject(i)));
                }

                return posts;
            }
        };

        request.run();
    }

    public void getSearch(String start, String end, final Response.Listener<ArrayList<PostInfo>> responseCallback,
                            final Response.ErrorListener errorCallback) {

        String make_url = "/" + start.replace(' ', '_') + "/" + end.replace(' ', '_');

        Log.e("make_url", make_url);

        GenericRequest<ArrayList<PostInfo>> request = new GenericRequest<ArrayList<PostInfo>>(
                "/posts/search" + make_url, Request.Method.GET, responseCallback, errorCallback) {
            @Override
            void buildParameters(JSONObject args) throws JSONException {}

            @Override
            ArrayList<PostInfo> parseResponse(JSONObject response) throws JSONException {

                ArrayList<PostInfo> posts = new ArrayList<PostInfo>();
                JSONObject post = response.getJSONObject("posts");
                JSONArray sameArray = post.getJSONArray("same");
                JSONArray startArray = post.getJSONArray("start");
                JSONArray endArray = post.getJSONArray("end");
                Log.e("same", sameArray.toString());
                Log.e("start", startArray.toString());
                Log.e("end", endArray.toString());
                for (int i = 0; i < sameArray.length(); i++) {
                    posts.add(new PostInfo(sameArray.getJSONObject(i)));
                }
                for (int i = 0; i < startArray.length(); i++) {
                    posts.add(new PostInfo(startArray.getJSONObject(i)));
                }
                for (int i = 0; i < endArray.length(); i++) {
                    posts.add(new PostInfo(endArray.getJSONObject(i)));
                }
                return posts;
            }
        };

        request.run();
    }

    // Saves a PostInfo object to the database. responseCallback will always be called with the
    // ID of the new post.
    public void createPost(final PostInfo post, Response.Listener<String> responseCallback,
                           Response.ErrorListener errorCallback) {

        // Build request object. We have to pass the post as "post" in the request JSON. Then we get
        // post_id back in the response.
        GenericRequest<String> request = new GenericRequest<String>("/posts/create",
                Request.Method.POST, responseCallback, errorCallback) {
            @Override
            void buildParameters(JSONObject args) throws JSONException {
                args.put("post", post.getJSON());
            }

            @Override
            String parseResponse(JSONObject response) throws JSONException {
                return response.getString("post_id");
            }
        };

        request.run();
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

        // Set up gen. request object. Post id goes in URL, so no arg building.
        GenericRequest<PostInfo> request = new GenericRequest<PostInfo>("/posts/by_id/"+id,
                Request.Method.GET, responseCallback, errorCallback) {
            @Override
            void buildParameters(JSONObject args) throws JSONException {}

            @Override
            PostInfo parseResponse(JSONObject response) throws JSONException {
                return new PostInfo(response.getJSONObject("post"));
            }
        };

        // Send the request
        request.run();
    }

    // This is very similar to getPostById. Accepts a string argument (the ObjectId of the user you
    // want, the same as what you get from PostInfo.getPassengers() or this.getUserId()). Returns a
    // UserInfo object via response listener, otherwise the errorCallback is called.
    public void getUserById(String id, final Response.Listener<UserInfo> responseCallback,
                            final Response.ErrorListener errorCallback) {

        // If we already have this user cached, just return that.
        if (userCache.containsKey(id)) {
            responseCallback.onResponse(userCache.get(id));
            return;
        }

        // Build the request. User id is URL argument.
        GenericRequest<UserInfo> request = new GenericRequest<UserInfo>("/users/by_id/"+id,
                Request.Method.GET, responseCallback, errorCallback) {
            @Override
            void buildParameters(JSONObject args) throws JSONException {}
            @Override
            UserInfo parseResponse(JSONObject response) throws JSONException {
                // User should be the field in the response object
                UserInfo result = new UserInfo(response.getJSONObject("user"));

                // Cache the user for later
                userCache.put(result.getId(), result);

                return result;
            }
        };

        // Run the request
        request.run();
    }

    public void createReport(final ReportInfo report, final Response.Listener<String> responseCallback,
                             final Response.ErrorListener errorCallback) {

        // Build the request
        GenericRequest<String> request = new GenericRequest<String>("/report",
                Request.Method.POST, responseCallback, errorCallback) {
            void buildParameters(JSONObject args) throws JSONException {
                args.put("report", report.getJSON());
                Log.d("test", report.getJSON().toString());
            }
            String parseResponse(JSONObject response) throws JSONException {
                return response.getString("report_id");
            }
        };

        // Run it
        request.run();
    }

    public void addDriver(final String post_id, final int avail_seats, final Response.Listener<String> responseCallback,
                          final Response.ErrorListener errorCallback) {

        // Build the request
        GenericRequest<String> request = new GenericRequest<String>("/posts/add_driver",
                Request.Method.POST, responseCallback, errorCallback) {
            void buildParameters(JSONObject args) throws JSONException {
                args.put("post_id", post_id);
                args.put("avail", avail_seats);
            }
            String parseResponse(JSONObject response) throws JSONException {
                return "" + response.getInt("result");
            }
        };

        // Run it
        request.run();
    }

    public void addPassenger(final String post_id, final Response.Listener<String> responseCallback,
                          final Response.ErrorListener errorCallback) {

        // Build the request
        GenericRequest<String> request = new GenericRequest<String>("/posts/add_passenger",
                Request.Method.POST, responseCallback, errorCallback) {
            void buildParameters(JSONObject args) throws JSONException {
                args.put("post_id", post_id);
            }
            String parseResponse(JSONObject response) throws JSONException {
                return "" + response.getInt("result");
            }
        };

        // Run it
        request.run();
    }

    public void getMyPage(Response.Listener<JSONObject> responseCallback,
                            Response.ErrorListener errorCallback) {

        GenericRequest<JSONObject> request = new GenericRequest<JSONObject>(
                "/posts/my_page", Request.Method.GET, responseCallback, errorCallback) {
            @Override
            void buildParameters(JSONObject args) throws JSONException {}

            @Override
            JSONObject parseResponse(JSONObject response) throws JSONException {
                JSONObject post = response.getJSONObject("posts");
                return post;
            }
        };

        request.run();
    }

    // GenericRequest attempts to make generic the code to write new requests.
    abstract class GenericRequest<T> {
        abstract void buildParameters(JSONObject args) throws JSONException;
        abstract T parseResponse(JSONObject response) throws JSONException;

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
            JSONObject jsonPostParameters = null;

            // If the method is POST, then we construct the arguments. Otherwise we can skip it.
            if (method == Request.Method.POST) {

                // To construct the arguments, create the object and have our builder put the
                // callee's info in it
                jsonPostParameters = new JSONObject();
                try {
                    buildParameters(jsonPostParameters);
                } catch (JSONException e) {
                    Log.w(TAG, "Failed to create JSON object for " + endpoint + "'s args:" + e.toString());
                    errorCallback.onErrorResponse(new VolleyError(e));
                    return;
                }
            }

            // Set up request and callbacks
            JsonObjectRequest request = new JsonObjectRequest(method,
                    URL + endpoint, jsonPostParameters, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        // Check for valid response
                        if (hasError(response)) {
                            String error = response.getString("error");
                            Log.w(TAG, "Got a bad result for " + endpoint + ": " + error);
                            errorCallback.onErrorResponse(new VolleyError(error));
                            return;
                        }

                        // Send success to callee
                        Log.d(TAG, "Generic req to "+endpoint+" successful, handing off to parse");
                        responseCallback.onResponse(parseResponse(response));
                    } catch (JSONException e) {
                        // If parsing fails, we fail
                        Log.w(TAG, "Request to " + endpoint + " failed: " + e.toString());
                        errorCallback.onErrorResponse(new VolleyError(e));
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
