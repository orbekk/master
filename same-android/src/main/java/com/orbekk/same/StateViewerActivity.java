package com.orbekk.same;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.orbekk.same.State.Component;
import com.orbekk.same.android.ClientInterfaceBridge;

public class StateViewerActivity extends Activity {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private ClientInterfaceBridge client;
    
    private StateChangedListener stateListener = new StateChangedListener() {
        @Override
        public void stateChanged(Component component) {
            displayState();
        }
    };
    
    private void displayState() {
        ArrayList<String> contentList = new ArrayList<String>();
        for (State.Component component : client.getState().getComponents()) {
            contentList.add(component.toString());
        }
        ListView list = (ListView)findViewById(R.id.state_view_list);
        list.setAdapter(new ArrayAdapter<String>(
            this, R.layout.list_text_item, contentList));
    }
    
    @Override public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.state_viewer);
    }
    
    @Override public void onResume() {
        super.onResume();
        client = new ClientInterfaceBridge(this);
        client.addStateListener(stateListener);
        client.connect();
    }
    
    @Override public void onStop() {
        super.onStop();
        client.removeStateListener(stateListener);
        client.disconnect();
    }
    
}
