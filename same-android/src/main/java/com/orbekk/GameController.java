package com.orbekk;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Paint;

public class GameController {
    private List<Player> remotePlayers = new ArrayList<Player>();
    private Player localPlayer;
    private ChangeListener changeListener = null;
    
    public static class Player {
        public Paint color;
        public float posX;
        public float posY;
    }
    
    public interface ChangeListener {
        void playerStatesChanged();
    }
    
    public static Player newPlayer() {
        Player player = new Player();
        player.color = new Paint();
        player.color.setARGB(255, 255, 0, 0);
        player.posX = 0.5f;
        player.posY = 0.5f;
        return player;
    }
    
    public GameController(Player localPlayer) {
        this.localPlayer = localPlayer;
    }
    
    public void setMyPosition(float x, float y) {
        this.localPlayer.posX = x;
        this.localPlayer.posY = y;
        changeListener.playerStatesChanged();
    }
    
    public Player getLocalPlayer() {
        return localPlayer;
    }
    
    public List<Player> getRemotePlayers() {
        return remotePlayers;
    }
    
    public void setChangeListener(ChangeListener listener) {
        this.changeListener = listener;
    }
}
