package com.kazaky.breakout;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.kazaky.breakout.objects.Ball;
import com.kazaky.breakout.objects.Brick;
import com.kazaky.breakout.objects.Paddle;

import java.io.IOException;

// Here is our implementation of BreakoutView
// It is an inner class.

// Notice we implement runnable so we have
// A thread and can override the run method.
public class BreakOutView extends SurfaceView implements Runnable {


    // This is our thread
    Thread gameThread = null;
    // This is new. We need a SurfaceHolder
    // When we use Paint and Canvas in a thread
    // We will see it in action in the draw method soon.
    SurfaceHolder ourHolder;

    // A boolean which we will set and unset
    // when the Menu_Layout is running- or not.
    volatile boolean playing;

    // Game is paused at the start
    boolean paused = true;

    // A Canvas and a Paint object
    Canvas canvas;
    Paint paint;

    // This variable tracks the Menu_Layout frame rate
    long fps;
    Bitmap bitmapBob;
    Bitmap bitmapBall;
    Bitmap bitmapPaddal;
    Bitmap bitmapBrick1;
    Bitmap bitmapBrick2;
    Bitmap bitmapBrick3;


    // The size of the screen in pixels
    int screenX;
    int screenY;
    // The players paddle
    Paddle paddle;
    // A ball
    Ball ball;


    // Up to 200 bricks
    Brick[] bricks = new Brick[24];
    int numBricks = 0;
    // For sound FX
    SoundPool soundPool;
    int beep1ID = -1;
    int beep2ID = -1;
    int beep3ID = -1;
    int loseLifeID = -1;
    int explodeID = -1;
    // The score
    int score = 0;
    int level = 1;
    // Lives
    int lives = 3;
    Rect dest;
    DisplayMetrics dm;
    int densityDpi;

    // When we initialize (call new()) on BreakOutView
    // This special constructor method runs


    public BreakOutView(Context context) {
        super(context);

        // The next line of code asks the
        // SurfaceView class to set up our object.
        // How kind.

        // Initialize ourHolder and paint objects
        ourHolder = getHolder();
        paint = new Paint();

        // Get a Display object to access screen details
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        // Load the resolution into a Point object
        Point size = new Point();

        //  TODO target API < 13
        display.getSize(size);

        screenX = size.x;
        screenY = size.y;

        // using dpi to set sizes for objects
        dm = context.getResources().getDisplayMetrics();
        densityDpi = dm.densityDpi;
        paddle = new Paddle(screenX, screenY, densityDpi);


        // Create a ball
        ball = new Ball(screenX, screenY);

        // Load the sounds
        // This SoundPool is deprecated but don't worry
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);

        try {
            // Create objects of the 2 required classes
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;

            // Load our fx in memory ready for use
            descriptor = assetManager.openFd("beep1.wav");
            beep1ID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("beep2.wav");
            beep2ID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("beep3.wav");
            beep3ID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("loseLife.wav");
            loseLifeID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("explode.wav");
            explodeID = soundPool.load(descriptor, 0);

        } catch (IOException e) {
            // Print an error message to the console
            Log.e("error", "failed to load sound files");
        }


        // Load Images from resource files
        bitmapBob = BitmapFactory.decodeResource(this.getResources(), R.drawable.wall);
        bitmapBall = BitmapFactory.decodeResource(this.getResources(), R.drawable.ball);
        bitmapPaddal = BitmapFactory.decodeResource(this.getResources(), R.drawable.ball);
        bitmapBrick1 = BitmapFactory.decodeResource(this.getResources(), R.drawable.brick_red);
        bitmapBrick2 = BitmapFactory.decodeResource(this.getResources(), R.drawable.brick_green);
        bitmapBrick3 = BitmapFactory.decodeResource(this.getResources(), R.drawable.brick_monster);

        //Make Sizes Depending on DPI
        int heightX = densityDpi / 8;
        float length_Paddal = densityDpi / 1.50f;
        int height_Paddal = densityDpi / 7;
        int brickWidth = screenX / 8;
        int brickHeight = screenY / 10;

        bitmapBall = getResizedBitmap(bitmapBall, heightX, heightX);
        bitmapPaddal = getResizedBitmap(bitmapPaddal, length_Paddal, height_Paddal);
        bitmapBrick1 = getResizedBitmap(bitmapBrick1, brickWidth, brickHeight);
        bitmapBrick2 = getResizedBitmap(bitmapBrick2, brickWidth, brickHeight);
        bitmapBrick3 = getResizedBitmap(bitmapBrick3, brickWidth, brickHeight);


        // Create bricks for level 1
        createBricksAndRestart(1);

    }


    public void createBricksAndRestart(int Xlevel) {

        // Put the ball back to the start
        ball.reset(screenX, screenY);

        level = Xlevel;
        switch (Xlevel) {

            case 2:
                // level 2
                ball.xVelocity = 600;
                ball.yVelocity = -1000;

                break;

            // level 3
            case 3:
                ball.xVelocity = 1000;
                ball.yVelocity = -1400;
                break;

            // level 1
            default:
                ball.xVelocity = 400;
                ball.yVelocity = -800;
                break;


        }

        // Brick Size
        int brickWidth = screenX / 8;
        int brickHeight = screenY / 10;

        // Build a wall of bricks
        numBricks = 0;
        for (int column = 0; column < 8; column++) {
            for (int row = 0; row < 3; row++) {
                bricks[numBricks] = new Brick(row, column, brickWidth, brickHeight);
                numBricks++;
            }
        }

        // if Game is over reset scores ,lives &Level
        if (lives == 0) {
            score = 0;
            lives = 3;
            level = 1;
        }

    }

    @Override
    public void run() {
        while (playing) {

            // Capture the current time in milliseconds in startFrameTime
            long startFrameTime = System.currentTimeMillis();

            // Update the frame
            if (!paused) {
                update();
            }

            // Draw the frame
            draw();

            // Calculate the fps this frame
            // We can then use the result to
            // time animations and more.
            long timeThisFrame = System.currentTimeMillis() - startFrameTime;
            if (timeThisFrame >= 1) {
                fps = 1000 / timeThisFrame;
            }

        }

    }

    // Everything that needs to be updated goes in here
    // Movement, collision detection etc.
    public void update() {

        // Move the paddle if required
        paddle.update(fps);
        ball.update(fps);

        // Check for ball colliding with a brick
        for (int i = 0; i < numBricks; i++) {

            if (bricks[i].getVisibility()) {

                if (RectF.intersects(bricks[i].getRect(), ball.getRect())) {
                    bricks[i].setInvisible();
                    ball.reverseYVelocity();
                    score = score + 10;

                    soundPool.play(explodeID, 1, 1, 0, 0, 1);
                }
            }
        }

        // Check for ball colliding with paddle
        if (
                ball.getRect().intersect(paddle.getRect()) ||
                        RectF.intersects(paddle.getRect(), ball.getRect()) ||
                        paddle.getRect().intersect(ball.getRect())

                ) {


            ball.reverseYVelocity();

            // ReverseX Direction + IncreaseX speed
            if (paddle.getMovementState() == paddle.RIGHT && ball.xVelocity < 0 || paddle.getMovementState() == paddle.LEFT && ball.xVelocity > 0) {
                ball.reverseXVelocity();
            }

            // SameX Direction + IncreaseX speed
            else if (paddle.getMovementState() == paddle.RIGHT && ball.xVelocity > 0 || paddle.getMovementState() == paddle.LEFT && ball.xVelocity < 0) {
                ball.sameXVelocity();
            }

            /*// Paddle is still, DecreaseX speed
             else if (paddle.getMovementState() == paddle.STOPPED) {
                ball.zeroXVelocity();
            }*/

            // Some intersection Bugs
            ball.clearObstacleY(paddle.getRect().top - 20);

            soundPool.play(beep1ID, 1, 1, 0, 0, 1);
        }

        // Bounce the ball back when it hits the bottom of screen
        // And Lose a life
        if (ball.getRect().bottom > screenY) {
            ball.reverseYVelocity();
            ball.clearObstacleY(screenY - 5);

            // Lose a life
            lives--;
            soundPool.play(loseLifeID, 1, 1, 0, 0, 1);

            if (lives == 0) {
                paused = true;

                //draw Loss;
                canvas = ourHolder.lockCanvas();
                paint.setColor(getResources().getColor(R.color.orange));
                paint.setTextSize(getResources().getDimension(R.dimen.text_size_big));
                canvas.drawText("أنت خسرت!",
                        screenX / 2 - (densityDpi / 1.90f), screenY / 2 + (densityDpi), paint);
                ourHolder.unlockCanvasAndPost(canvas);

                try {
                    // Wait 3 seconds then reset a new game
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Create bricks at level 1
                createBricksAndRestart(1);

            }

        }
        // Pause if cleared screen
        if (score == numBricks * 10) {

            // Create bricks at level 2
            createBricksAndRestart(2);

            // fix for a pause bug
            // so that it won't Pause After finishing the Game
            score = score + 10;
            // Gift the player with 1 new live
            lives = lives + 1;

        } else if (score == (numBricks * 20) + 10) {

            // Create bricks at level 3
            createBricksAndRestart(3);

            // fix for a pause bug
            // so that it won't Pause After finishing the Game
            score = score + 10;
            // Gift the player with 2 new lives
            lives = lives + 2;

        }
        // Pause if cleared screen
        // if score equals to the whole Bricks scores after 3 levels
        else if (score == (numBricks * 10 * 3) + 20) {
            paused = true;
        }


        // Bounce the ball back when it hits the top of screen
        if (ball.getRect().top < 0) {
            ball.reverseYVelocity();
            ball.clearObstacleY(40);

            soundPool.play(beep2ID, 1, 1, 0, 0, 1);
        }

        // If the ball hits left wall bounce
        if (ball.getRect().left < 0) {
            ball.reverseXVelocity();
            ball.clearObstacleX(2);

            soundPool.play(beep3ID, 1, 1, 0, 0, 1);
        }

        // If the ball hits right wall Velocity
        if (ball.getRect().right > screenX) {
            ball.reverseXVelocity();
            ball.clearObstacleX(screenX - 57);

            soundPool.play(beep3ID, 1, 1, 0, 0, 1);
        }


    }

    // Draw the newly updated scene
    public void draw() {

        // Make sure our drawing surface is valid or we crash
        if (ourHolder.getSurface().isValid()) {
            // Lock the canvas ready to draw
            canvas = ourHolder.lockCanvas();

            // Draw the background color
            // canvas.drawColor(getResources().getColor(R.color.deeppurple));

            dest = new Rect(0, 0, getWidth(), getHeight());
            // Draw bob as background with dest size
            canvas.drawBitmap(bitmapBob, null, dest, paint);

            // Choose the brush color for drawing
            paint.setColor(Color.argb(255, 255, 255, 255));

            // Draw the ball
            // canvas.drawCircle(ball.getRect().centerX(), ball.getRect().centerY(), 25, paint);
            canvas.drawBitmap(bitmapBall, ball.getRect().left, ball.getRect().top, null);

            // Draw the paddle
            //canvas.drawRect(paddle.getRect(), paint);
            canvas.drawBitmap(bitmapPaddal, paddle.getRect().left, paddle.getRect().top, null);


            // Change the brush color for drawing
            // paint.setColor(getResources().getColor(R.color.redorange));

            // Draw the bricks if visible
            for (int i = 0; i < numBricks; i++) {
                if (bricks[i].getVisibility()) {
                    // canvas.drawRect(bricks[i].getRect(), paint);

                    switch (level) {
                        case 1:
                            canvas.drawBitmap(bitmapBrick1, bricks[i].getRect().left, bricks[i].getRect().top, null);

                            break;

                        case 2:
                            canvas.drawBitmap(bitmapBrick2, bricks[i].getRect().left, bricks[i].getRect().top, null);

                            break;
                        case 3:
                            canvas.drawBitmap(bitmapBrick3, bricks[i].getRect().left, bricks[i].getRect().top, null);
                            break;
                    }


                }
            }

            // Choose the brush color for drawing
            paint.setColor(Color.argb(255, 255, 255, 255));
            // Draw the score
            paint.setTextSize(getResources().getDimension(R.dimen.text_size));

            // Score Text
            canvas.drawText(
                    "النقاط: " + score
                    , screenX - (densityDpi / 1.50f), screenY / 2, paint);

            // Lives Text
            canvas.drawText("الصحة: " + lives
                    , densityDpi / 5, screenY / 2, paint);

            // Levels Text
            canvas.drawText("المرحلة: " + level
                    , screenX / 2 - (densityDpi / 5), screenY / 2 + (densityDpi / 5), paint);

            // Has the player cleared the screen?
            if (score >= (numBricks * 10 * 3) + 20) {
                paint.setColor(getResources().getColor(R.color.colorAccent));
                paint.setTextSize(getResources().getDimension(R.dimen.text_size_big));
                canvas.drawText("أنت كسبت!", screenX / 2 - (densityDpi / 1.90f), screenY / 2 + (densityDpi / 1), paint);

            }

            // Draw everything to the screen
            ourHolder.unlockCanvasAndPost(canvas);
        }

    }

    // If GameActivity is paused/stopped
    // shutdown our thread.
    public void pause() {
        playing = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            Log.e("Error:", "joining thread");
        }

    }

    // If GameActivity is started
    // start our thread.
    public void resume() {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    // The SurfaceView class implements onTouchListener
    // So we can override this method and detect screen touches.
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {

            // Player has touched the screen
            case MotionEvent.ACTION_DOWN:

                if (!(lives == 0)) {

                    paused = false;
                }

                // If touch motion > Half of the Screen
                if (motionEvent.getX() > screenX / 2) {

                    // move paddle right
                    paddle.setMovementState(paddle.RIGHT);

                } else {

                    // move paddle left
                    paddle.setMovementState(paddle.LEFT);
                }

                break;

            // Player has removed finger from screen
            case MotionEvent.ACTION_UP:

                // paddle stopped
                paddle.setMovementState(paddle.STOPPED);
                break;
        }
        return true;
    }

    // Resize Bitmap function to Handle all the Images from resources the right size
    public Bitmap getResizedBitmap(Bitmap bm, float newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = newWidth / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }


}