package com.orbekk.same.android.widget;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.orbekk.same.android.R;

/**
 * This class extends ArrayAdapter incompletely.
 * 
 * Only use as follows:
 * 
 *   list.setAdapter(new NetworkListAdapter(...));
 */
public class NetworkListAdapter extends ArrayAdapter<String> {
    private List<String> networkNames;
    private List<String> masterUrls;
    
    public NetworkListAdapter(Context context, int resource_id,
            List<String> networkNames, List<String> masterUrls) {
        super(context, resource_id, networkNames);
        this.networkNames = networkNames;
        this.masterUrls = masterUrls;
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = getLayoutInflater();
            convertView = inflater.inflate(R.layout.network_list_item, null);
        }
        if (networkNames.get(position) != null) {
            TextView nameText = (TextView)convertView.findViewById(
                    R.id.network_name_text);
            TextView urlText = (TextView)convertView.findViewById(
                    R.id.master_url_text);
            nameText.setText(networkNames.get(position));
            urlText.setText(masterUrls.get(position));
        }
        return convertView;
    }
    
    private LayoutInflater getLayoutInflater() {
        return (LayoutInflater)getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);   
    }
}
