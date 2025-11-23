package tn.esprit.dam.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;


import androidx.activity.ComponentActivity;
import androidx.appcompat.app.AppCompatActivity;

import tn.esprit.dam.R;

public class SplashActiv extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash); // Set your splash screen layout

        // Find the ImageView (this should be the ImageView for your logo)
        ImageView splashImage = findViewById(R.id.splashImage); // Make sure you have an ImageView with this ID in activity_splash.xml

        // Load the animation from res/anim/translate.xml
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.translate);

        // Start the animation on the ImageView
        splashImage.startAnimation(animation);

        // Set up an AnimationListener to detect when the animation ends
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // You can do something when the animation starts if needed
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // When the animation ends, navigate to the next screen
                // You can replace "WelcomeScreenActivity.class" with your actual activity
                Intent intent = new Intent(SplashActiv.this, WelcomeScreenActivity.class);
                startActivity(intent);
                finish(); // Finish this Splash Activity so that the user can't return to it
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // You can handle animation repeat logic here (if needed)
            }
        });
    }
}
