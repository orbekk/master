package com.orbekk.same.android;

import android.app.Activity;
import android.os.Bundle;

import com.orbekk.same.Types;
import com.orbekk.same.Variable;

public class GraphicsActivity extends Activity {
    private GameView gameView;
    private ClientInterfaceBridge client;
    
    @Override
    public void onCreate(Bundle savedBundle) {
        super.onCreate(savedBundle);
    }
    
    public void onResume() {
        super.onResume();
        client = new ClientInterfaceBridge(this);
        client.connect();
        Variable<GameView.Player> player = client.createVariableFactory()
                .create("Player", Types.fromType(GameView.Player.class));
        gameView = new GameView(this, player);
        gameView.setUp();
        setContentView(gameView);
    }
    
    public void onStop() {
        super.onStop();
        gameView.tearDown();
        gameView = null;
        client.disconnect();
        client = null;
    }
}
