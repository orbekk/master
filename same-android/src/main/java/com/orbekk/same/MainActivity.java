package com.orbekk.same;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MainActivity extends Activity {
    Logger logger = LoggerFactory.getLogger(getClass());
    
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
    
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
    
    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
        case R.id.same_settings:
            startActivity(new Intent(this, SameControllerActivity.class));
            break;
        case R.id.variable_test:
            startActivity(new Intent(this, VariableTestActivity.class));
            break;
        default:
            logger.error("Unknown menu entry: {}", item);
        }
        return true;
    }
}
