package com.orbekk.same;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orbekk.same.State.Component;
import com.orbekk.same.ClientService;
import com.orbekk.same.Client;
import com.orbekk.same.UpdateConflict;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private GameThread thread;
    
    static class Player {
        public float posX;
        public float posY;
    }
    
    static class GameThread extends Thread
            implements Variable.OnChangeListener<Player> {
        private Logger logger = LoggerFactory.getLogger(getClass());
        private int height = 0;
        private int width = 0;
        private SurfaceHolder holder;
        private Context context;
        private Paint background;
        private Variable<Player> player;
        private Paint color = new Paint();
        
        public GameThread(SurfaceHolder holder, Context context,
                Variable<Player> player) {
            this.holder = holder;
            this.context = context;
            this.player = player;
            background = new Paint();
            background.setARGB(255, 0, 0, 0);
            color.setARGB(255, 255, 0, 0);
        }
        
        public void setUp() {
            Player player_ = new Player();
            player_.posX = 0.5f;
            player_.posY = 0.5f;
            player.setOnChangeListener(this);
            try {
                player.set(player_);
            } catch (UpdateConflict e) {
                e.printStackTrace();
            }
        }
        
        public void setSize(int width, int height) {
            synchronized(holder) {
                this.width = width;
                this.height = height;
            }
        }
        
        private void doDraw(Canvas c) {
            c.drawRect(0.0f, 0.0f, width+1.0f, height+1.0f, background);
            Player player_ = player.get();
            if (player_ == null) {
                return;
            }
            c.drawCircle(player_.posX * width, player_.posY * height,
                    20.0f, color);
        }
        
        @Override public void run() {
            Canvas c = null;
            try {
                c = holder.lockCanvas();
                synchronized(holder) {
                    doDraw(c);
                }
            } finally {
                holder.unlockCanvasAndPost(c);
            }
        }
        
        private synchronized void setPosition(float x, float y) {
            Player newPlayer = new Player();
            newPlayer.posX = x / width;
            newPlayer.posY = y / width;
            try {
                if (!player.waitingForUpdate()) {
                    player.set(newPlayer);
                }
            } catch (UpdateConflict e) {
                Toast.makeText(context, "Failed to update position.",
                        Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void valueChanged(Variable<Player> unused) {
            logger.info("Variable updated.");
            player.update();
            run();
        }
    }
    
    public GameView(Context context, Variable<Player> player) {
        super(context);
        getHolder().addCallback(this);
        thread = new GameThread(getHolder(), context, player);
        thread.setUp();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setARGB(255, 255, 0, 0);
        canvas.drawCircle(50.0f, 50.0f, 50.0f, paint);
    }
    
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
        logger.info("SurfaceChanged(w={}, h={})", width, height);
        thread.setSize(width, height);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        logger.info("SurfaceCreated()");
        thread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        logger.info("SurfaceDestroyed()");
        // TODO: Stop thread.
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        thread.setPosition(e.getX(), e.getY());
        return true;
    }

}
