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
        ArrayAdapter<CharSequence> originAdapter = ArrayAdapter.createFromResource(this, R.array.StartingLocations, android.R.layout.simple_spinner_item);
        originAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        originSpinner.setAdapter(originAdapter);
        originSpinner.setOnItemSelectedListener(this);

        Spinner destinationSpinner = findViewById(R.id.destination_spinner);
        ArrayAdapter<CharSequence> destinationAdapter = ArrayAdapter.createFromResource(this, R.array.EndingLocations, android.R.layout.simple_spinner_item);
        destinationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        destinationSpinner.setAdapter(destinationAdapter);
        destinationSpinner.setOnItemSelectedListener(this);

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
