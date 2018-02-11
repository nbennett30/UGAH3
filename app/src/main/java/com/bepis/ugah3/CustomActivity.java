package com.bepis.ugah3;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class CustomActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom);

        Button custom = (Button) findViewById(R.id.button2);
        EditText text = (EditText) findViewById(R.id.editText);
        String color = text.getText().toString();
        try {
            Integer.parseInt(color, 16);
        } catch (NumberFormatException e) {
            Toast.makeText(getApplicationContext(), "Invalid Hex Code", Toast.LENGTH_SHORT).show();
        }
        custom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), CameraActivity.class);
                startActivity(intent);
            }
        });
    }

}