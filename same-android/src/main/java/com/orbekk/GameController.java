package com.orbekk;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Paint;

public class GameController {
    private List<Player> players = new ArrayList<Player>();
    private Player localPlayer;
    
    public static class Player {
        public Paint color;
        public int posX;
        public int posY;
    }
    
    public GameController(Player localPlayer) {
        this.localPlayer = localPlayer;
    }
    
    public Player getLocalPlayer() {
        return localPlayer;
    }
    
    public List<Player> getRemotePlayers() {
        return players;
    }
    
    
    
}
