package ridesharers.ucsc.edu.ucsharecar;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

public class RegisterActivity extends AppCompatActivity {

    //This class allows new users to register.

    private static final String TAG = "UCShareCar_Register";

    private static BackendClient backend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        backend = BackendClient.getSingleton(this);
    }

    public void onClickRegister(View view) {
        // Get the phone number that was input
        String phnum = ((EditText) findViewById(R.id.phnum)).getText().toString();

        // Log it
        Log.w(TAG, "Sending "+phnum+" as the user's phone number");

        // TODO sometimes errors in the following code is because of an invalid session.
        // In theory, the server should only ask for a registration after it creates a session.
        // But you never know.

        // Send that phone number away!
        backend.Register(phnum, new Response.Listener<BackendClient.RegisterResult>() {
            @Override
            public void onResponse(BackendClient.RegisterResult response) {
                Log.w(TAG, "Got a register response");
                String error = response.Error();
                if (error != null) {
                    Log.e(TAG, "Error when registering: " + error);
                    dispToast("Failed to register: Server error");
                }
                else if (response.Succeeded()){
                    Log.w(TAG, "Successfully registered!");
                    dispToast("You are now registered!");
                    goToPostList();
                } else {
                    Log.w(TAG, "Mystery failure");
                    dispToast("The registration failed. Try again.");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.w(TAG, "An error occurred while trying to register.");
                dispToast("The registration failed. Try again.");
            }
        });
    }

    private void goToPostList() {
        Intent intent = new Intent(this, PostListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void dispToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
