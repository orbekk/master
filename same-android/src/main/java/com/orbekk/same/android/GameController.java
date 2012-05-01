/**
 * Copyright 2012 Kjetil Ørbekk <kjetil.orbekk@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.orbekk.same.android;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orbekk.same.StateChangedListener;
import com.orbekk.same.UpdateConflict;

import android.graphics.Paint;

public class GameController {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private List<Player> remotePlayers = new ArrayList<Player>();
    private Player localPlayer;
    private ChangeListener changeListener = null;
//    private SameInterface same;
    
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
    
    public static GameController create(Player localPlayer) {
        GameController controller = new GameController(localPlayer);
//        same.addStateChangedListener(controller);
        return controller;
    }
    
    GameController(Player localPlayer) {
        this.localPlayer = localPlayer;
    }
    
    public void setMyPosition(float x, float y) throws UpdateConflict {
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

//    @Override
//    public void stateChanged(String id, String data) {
//        logger.info("StateChanged({}, {})", id, data);        
//    }
}
