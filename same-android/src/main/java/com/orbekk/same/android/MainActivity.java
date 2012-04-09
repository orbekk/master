package com.orbekk.same.android;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.orbekk.same.android.R;
import com.orbekk.same.android.benchmark.ExampleProtobufServerActivity;
import com.orbekk.same.android.benchmark.RepeatedSetVariableActivity;

public class MainActivity extends Activity {
    Logger logger = LoggerFactory.getLogger(getClass());
    
    public final static Map<String, Class<? extends Activity>> activities;
    static {
        activities = new HashMap<String, Class<? extends Activity>>();
        activities.put("Same settings", SameControllerActivity.class);
        activities.put("Variable test", VariableTestActivity.class);
        activities.put("State monitor", StateViewerActivity.class);
        activities.put("Graphics demo", GraphicsActivity.class);
        activities.put("Benchmark", RepeatedSetVariableActivity.class);
        activities.put("ExampleProtobufServer (temp)", 
                ExampleProtobufServerActivity.class);
    }
    
    public final static List<String> activityList =
        new ArrayList<String>(activities.keySet());

    private AdapterView.OnItemClickListener activityListClickListener =
            new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> unused_parent, View unused_view,
                int position, long id) {
            String activityName = activityList.get(position);
            Class<? extends Activity> activity = activities.get(activityName);
            startActivity(new Intent(MainActivity.this, activity));
        }
    };
    
    private void createActivityList() {
        ListView list = (ListView)findViewById(R.id.activities_menu);
        list.setAdapter(new ArrayAdapter<String>(
                this, R.layout.list_text_item, activityList));
        list.setOnItemClickListener(activityListClickListener);
    }
    
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        createActivityList();
    }
}
