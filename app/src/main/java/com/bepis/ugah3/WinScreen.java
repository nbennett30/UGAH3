package com.bepis.ugah3;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import static com.bepis.ugah3.getColors.getBlue;

public class WinScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_win_screen);
        //getActionBar().setTitle("Congratulations!");

        int color = (int) getIntent().getExtras().get("color");

        ImageView img = (ImageView) findViewById(R.id.imageView2);
        GradientDrawable shp = (GradientDrawable) img.getBackground();
        shp.setColor(Color.argb(0xff, getColors.getRed(color), getColors.getGreen(color), getBlue(color)));

        Button fab = (Button) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), MainActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
