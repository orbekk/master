package com.orbekk.same;

import java.util.ArrayList;
import java.util.Properties;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.widget.Toast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orbekk.same.NetworkNotificationListener;
import com.orbekk.same.SameController;
import com.orbekk.same.android.net.AndroidBroadcasterFactory;
import com.orbekk.same.android.net.Broadcaster;
import com.orbekk.same.config.Configuration;

public class SameService extends Service {
    public final static int DISPLAY_MESSAGE = 1;
    public final static int SEARCH_NETWORKS = 2;

    public final static String AVAILABLE_NETWORKS_UPDATE =
            "com.orbekk.same.SameService.action.AVAILABLE_NETWORKS_UPDATE";
    public final static String AVAILABLE_NETWORKS =
            "com.orbekk.same.SameService.action.AVAILABLE_NETWORKS";

    final static int SERVICE_PORT = 15068;
    final static int DISCOVERY_PORT = 15066;
    
    private Logger logger = LoggerFactory.getLogger(getClass());
    private SameController sameController = null;
    private Configuration configuration = null;
    
    private ArrayList<String> networkNames = new ArrayList<String>();
    private ArrayList<String> networkUrls = new ArrayList<String>();
    
    private NetworkNotificationListener networkListener =
            new NetworkNotificationListener() {
        @Override
        public void notifyNetwork(String networkName, String masterUrl) {
            logger.info("notifyNetwork({})", networkName);
            networkNames.add(networkName);
            networkUrls.add(masterUrl);
            Intent intent = new Intent(AVAILABLE_NETWORKS_UPDATE);
            intent.putStringArrayListExtra(AVAILABLE_NETWORKS,
                    networkNames);
            sendBroadcast(intent);
        }
    };
    
    class InterfaceHandler extends Handler {
        @Override public void handleMessage(Message message) {
            switch (message.what) {
                case DISPLAY_MESSAGE:
                    Toast.makeText(SameService.this,
                        (String)message.obj, Toast.LENGTH_SHORT)
                            .show();
                    break;
                case SEARCH_NETWORKS:
                    logger.info("SEARCH_NETWORKS");
                    sameController.searchNetworks();
                    break;
                default:
                    super.handleMessage(message);
            }
        }
    }
    
    private final Messenger messenger = new Messenger(new InterfaceHandler());

    private void initializeConfiguration() {
        Properties properties = new Properties();
        properties.setProperty("port", ""+SERVICE_PORT);
        properties.setProperty("localIp",
                new Broadcaster(this).getWlanAddress().getHostAddress());
        properties.setProperty("masterUrl", "http://10.0.0.6:10010/MasterService.json");
        properties.setProperty("discoveryPort", ""+DISCOVERY_PORT);
        configuration = new Configuration(properties);
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        logger.info("onBind()");
        
        // Make sure service continues to run after it is unbound.
        Intent service = new Intent(this, getClass());
        startService(service);
        
        return messenger.getBinder();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        logger.info("onStartCommand()");
        return START_STICKY;
    }
    
    @Override
    public void onCreate() {
        logger.info("onCreate()");
        
        if (sameController == null) {
            initializeConfiguration();
            sameController = SameController.create(
                    new AndroidBroadcasterFactory(this),
                    configuration);
            try {
                sameController.start();
                sameController.getClient().setNetworkListener(networkListener);
            } catch (Exception e) {
                logger.error("Failed to start server", e);
            }
        }
    }
    
    @Override
    public void onDestroy() {
        logger.info("onDestroy()");
        if (sameController != null) {
            sameController.stop();
        }
    }

}
