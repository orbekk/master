package com.orbekk.same;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orbekk.same.State.Component;
import com.orbekk.same.ClientService;
import com.orbekk.same.Client;
import com.orbekk.same.SameInterface;
import com.orbekk.same.UpdateConflict;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private GameThread thread;
    
    static class GameThread extends Thread
            implements GameController.ChangeListener {
        private Logger logger = LoggerFactory.getLogger(getClass());
        private int height = 0;
        private int width = 0;
        private SurfaceHolder holder;
        private Context context;
        private Paint background;
        private GameController controller;
        
        public GameThread(SurfaceHolder holder, Context context,
                GameController controller) {
            this.holder = holder;
            this.context = context;
            this.controller = controller;
            this.controller.setChangeListener(this);
            background = new Paint();
            background.setARGB(255, 0, 0, 0);
        }
        
        public void setSize(int width, int height) {
            synchronized(holder) {
                this.width = width;
                this.height = height;
            }
        }
        
        private void doDraw(Canvas c) {
            c.drawRect(0.0f, 0.0f, width+1.0f, height+1.0f, background);
            for (GameController.Player p : controller.getRemotePlayers()) {
                c.drawCircle(p.posX * width, p.posY * height, 20.0f, p.color);
            }
            GameController.Player localPlayer = controller.getLocalPlayer();
            c.drawCircle(localPlayer.posX * width, localPlayer.posY * height,
                    20.0f, localPlayer.color);
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
            controller.setMyPosition(x / this.width, y / this.height);
        }

        @Override
        public void playerStatesChanged() {
            run();
        }
    }
    
    public GameView(Context context, GameController controller) {
        super(context);
        getHolder().addCallback(this);
        thread = new GameThread(getHolder(), context, controller);
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
