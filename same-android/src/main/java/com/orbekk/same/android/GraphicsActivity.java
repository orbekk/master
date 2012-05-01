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

import org.codehaus.jackson.type.TypeReference;

import android.app.Activity;
import android.os.Bundle;

import com.orbekk.same.Variable;
import com.orbekk.same.android.GameView.Player;

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
                .create("Player", new TypeReference<Player>() {});
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
