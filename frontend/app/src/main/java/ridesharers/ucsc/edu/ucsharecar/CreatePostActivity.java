package ridesharers.ucsc.edu.ucsharecar;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class CreatePostActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        Spinner originSpinner = findViewById(R.id.start_spinner);
        Spinner destinationSpinner = findViewById(R.id.destination_spinner);

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String origin = parent.getItemAtPosition(position).toString();
        Toast.makeText(parent.getContext(), origin, Toast.LENGTH_SHORT).show();

        String destination = parent.getItemAtPosition(position).toString();
        Toast.makeText(parent.getContext(), destination, Toast.LENGTH_SHORT);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
