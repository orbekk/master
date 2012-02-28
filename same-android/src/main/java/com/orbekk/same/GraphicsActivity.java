package com.orbekk.same;

import org.codehaus.jackson.type.TypeReference;

import com.orbekk.same.android.ClientInterfaceBridge;

import android.app.Activity;
import android.os.Bundle;

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
        TypeReference playerType = new TypeReference<GameView.Player>() {};
        Variable<GameView.Player> player = client.createVariableFactory()
                .create("Player", playerType);
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
