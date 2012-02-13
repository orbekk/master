package com.orbekk.same.android.net;

import android.content.Context;

import com.orbekk.net.BroadcasterFactory;
import com.orbekk.net.BroadcasterInterface;

public class AndroidBroadcasterFactory implements BroadcasterFactory {
    Context context;
    
    public AndroidBroadcasterFactory(Context context) {
        this.context = context;
    }
    
    @Override
    public BroadcasterInterface create() {
        return new Broadcaster(context);
    }

}
