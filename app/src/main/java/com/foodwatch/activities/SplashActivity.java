package com.foodwatch.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.foodwatch.R;

public class SplashActivity extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Typeface sTypeFace = Typeface.createFromAsset(getAssets(), "oswald_light.ttf");

        TextView sloganTextView = (TextView) findViewById(R.id.tag_textview);
        sloganTextView.setTypeface(sTypeFace);

        final ImageView wheel = (ImageView) findViewById(R.id.wheel_imageview);
        final Animation an = AnimationUtils.loadAnimation(getBaseContext(), R.anim.rotate);

        wheel.startAnimation(an);
        an.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                finish();
                Intent intent = new Intent(SplashActivity.this, RecognizeConceptsActivity.class);
                startActivity(intent);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
    }
}