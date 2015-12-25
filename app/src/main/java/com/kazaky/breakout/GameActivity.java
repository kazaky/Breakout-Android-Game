package com.kazaky.breakout;

import android.app.Activity;
import android.os.Bundle;

public class GameActivity extends Activity {

    // gameView will be the view of the Menu_Layout
    // It will also hold the logic of the Menu_Layout
    // and respond to screen touches as well
  BreakOutView breakoutView;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        breakoutView = new BreakOutView(this);

        // Initialize gameView and set it as the view
        setContentView(breakoutView);







    }

    // This is the end of our BreakoutView inner class

    // This method executes when the player starts the Menu_Layout
    @Override
    protected void onResume() {
        super.onResume();

        // Tell the gameView resume method to execute
        breakoutView.resume();
    }

    // This method executes when the player quits the Menu_Layout
    @Override
    protected void onPause() {
        super.onPause();

        // Tell the gameView pause method to execute
        breakoutView.pause();
    }
}
// This is the end of the BreakoutGame class


