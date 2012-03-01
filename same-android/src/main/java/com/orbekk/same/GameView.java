package com.orbekk.same;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private GameThread thread;
    
    static class Player {
        public Player() {
        }
        public Player(float posX, float posY) {
            this.posX = posX;
            this.posY = posY;
        }
        
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
        private VariableUpdaterTask<Player> updater;
        private AtomicBoolean shouldRedraw = new AtomicBoolean(true);
        
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
            player.addOnChangeListener(this);
            updater = new VariableUpdaterTask<Player>(player);
            updater.set(new Player(0.5f, 0.5f));
            updater.start();
        }
        
        public void tearDown() {
            player.removeOnChangeListener(this);
            updater.interrupt();
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
            while (true) {
                Canvas c = null;
                try {
                    c = holder.lockCanvas();
                    if (c != null) {
                        synchronized(holder) {
                            doDraw(c);
                        }
                    }
                } finally {
                    if (c != null) {
                        holder.unlockCanvasAndPost(c);
                    }
                }
                synchronized (this) {
                    if (Thread.interrupted()) {
                        break;
                    }
                    try {
                        while (!shouldRedraw.get()) {
                            wait();
                        }
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        }
        
        private synchronized void setShouldRedraw() {
            shouldRedraw.set(true);
            notifyAll();
        }
        
        private synchronized void setPosition(final float x, final float y) {
            if (player.get() == null || player.get().posX != x ||
                    player.get().posY != y) {
                Player newPlayer = new Player(x / width, y / height);
                updater.set(newPlayer);
            }
        }

        @Override
        public synchronized void valueChanged(Variable<Player> unused) {
            logger.info("Variable updated.");
            player.update();
            setShouldRedraw();
        }
    }
    
    public GameView(Context context, Variable<Player> player) {
        super(context);
        getHolder().addCallback(this);
        thread = new GameThread(getHolder(), context, player);
    }

    public void setUp() {
        thread.setUp();
    }
    
    public void tearDown() {
        thread.tearDown();
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
        thread.interrupt();
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        thread.setPosition(e.getX(), e.getY());
        return true;
    }

}
