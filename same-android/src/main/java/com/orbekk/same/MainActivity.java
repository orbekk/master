package com.orbekk.same;

import com.orbekk.same.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends Activity {
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
        if (item.getItemId() == R.id.same_settings) {
            startActivity(new Intent(this, SameControllerActivity.class));
        }
        return true;
    }
}
