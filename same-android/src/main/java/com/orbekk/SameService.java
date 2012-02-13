package com.orbekk;

import java.util.List;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
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

import com.orbekk.same.DiscoveryListener;
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
    
    private NetworkNotificationListener networkListener =
            new NetworkNotificationListener() {
        @Override
        public void notifyNetwork(String networkName, String masterUrl) {
            Message message = Message.obtain();
            message.obj = "notifyNetwork(" + networkName + ")";
            toastHandler.sendMessage(message);
        }
    };
    
    private Handler toastHandler = new Handler() {
        @Override public void handleMessage(Message message) {
            Toast.makeText(SameService.this,
                    (String)message.obj, Toast.LENGTH_SHORT)
                            .show();
            logger.info("Display toast: {}", (String)message.obj);
        }
    };
    
    
    class InterfaceHandler extends Handler {
        @Override public void handleMessage(Message message) {
            switch (message.what) {
                case DISPLAY_MESSAGE:
                    Toast.makeText(SameService.this,
                        (String)message.obj, Toast.LENGTH_SHORT)
                            .show();
                            
                    Intent intent = new Intent(AVAILABLE_NETWORKS_UPDATE);
                    ArrayList<String> networkList = new ArrayList<String>();
                    networkList.add("FirstNetwork");
                    intent.putStringArrayListExtra(AVAILABLE_NETWORKS,
                        networkList);
                    sendBroadcast(intent);
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
        return messenger.getBinder();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        logger.info("onStartCommand()");

        
        // TODO: Move this to the bound interface.
//        if (intent.getAction().equals("create")) {
//        } else if (intent.getAction().equals("join")) {
//            String masterUrl = intent.getExtras().getString("masterUrl"); 
//            sameController.joinNetwork(masterUrl);
//        }
        return START_STICKY;
    }
    
    @Override
    public void onCreate() {
        logger.info("onCreate()");
        
        // Ensure service is started.
        Intent intent = new Intent(this, getClass());
        startService(intent);
        
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
