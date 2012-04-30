package com.orbekk.same.android;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.widget.Toast;

import com.google.protobuf.RpcCallback;
import com.orbekk.protobuf.Rpc;
import com.orbekk.same.NetworkNotificationListener;
import com.orbekk.same.SameController;
import com.orbekk.same.Services;
import com.orbekk.same.Services.MasterState;
import com.orbekk.same.Services.NetworkDirectory;
import com.orbekk.same.State;
import com.orbekk.same.State.Component;
import com.orbekk.same.StateChangedListener;
import com.orbekk.same.android.net.Networking;
import com.orbekk.same.config.Configuration;
import com.orbekk.util.DelayedOperation;

public class SameService extends Service {
    public final static int CREATE_NETWORK = 3;
    
    /**
     * masterUrl: getData().getString("masterUrl")
     */
    public final static int JOIN_NETWORK = 4;
    public final static int ADD_STATE_RECEIVER = 5;
    public final static int REMOVE_STATE_RECEIVER = 6;

    /**
     * arg1: Operation number.
     * bundle: A Bundle created with ComponentBundle
     */
    public final static int SET_STATE = 7;
    
    /**
     * bundle: A Bundle created with ComponentBundle.
     */
    public final static int UPDATED_STATE_CALLBACK = 8;
    
    /**
     * arg1: Operation number.
     * arg2: Status code.
     * obj: Status message.
     */
    public final static int OPERATION_STATUS_CALLBACK = 9;

    public final static int KILL_MASTER = 10;
    
    // TODO: Remove these and use messengers instead of broadcast intents.
    public final static String AVAILABLE_NETWORKS_UPDATE =
            "com.orbekk.same.SameService.action.AVAILABLE_NETWORKS_UPDATE";
    public final static String AVAILABLE_NETWORKS =
            "com.orbekk.same.SameService.action.AVAILABLE_NETWORKS";
    public final static String NETWORK_URLS =
            "com.orbekk.same.SameService.action.NETWORK_URLS";

    final static int PPORT = 15070;
    final static int SERVICE_PORT = 15068;
    final static int DISCOVERY_PORT = 15066;
    final static String DIRECTORY_URL = "flode.pvv.ntnu.no:15072";

    private Logger logger = LoggerFactory.getLogger(getClass());
    private SameController sameController = null;
    private Configuration configuration = null;
    private Vector<Messenger> stateReceivers = new Vector<Messenger>();
    
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
            intent.putStringArrayListExtra(NETWORK_URLS,
                    networkUrls);
            sendBroadcast(intent);
        }
    };
    
    class InterfaceHandler extends Handler {
        @Override public void handleMessage(Message message) {
            switch (message.what) {
                case CREATE_NETWORK:
                    logger.info("CREATE_NETWORK");
                    create();
                    break;
                case JOIN_NETWORK:
                    logger.info("JOIN_NETWORK");
                    String masterUrl = message.getData().getString("masterLocation");
                    MasterState master = MasterState.newBuilder()
                            .setMasterLocation(masterUrl).build();
                    sameController.getClient().joinNetwork(master);
                    break;
                case ADD_STATE_RECEIVER:
                    logger.info("ADD_STATE_RECEIVER: {}", message);
                    Messenger messenger = message.replyTo;
                    if (messenger != null) {
                        stateReceivers.add(messenger);
                        sendAllState(messenger);
                    } else {
                        logger.error("ADD_STATE_RECEIVER: Missing Messenger.");
                    }
                    break;
                case REMOVE_STATE_RECEIVER:
                    logger.info("REMOVE_STATE_RECEIVER: {}", message);
                    Messenger droppedMessenger = (Messenger)message.obj;
                    stateReceivers.remove(droppedMessenger);
                    break;
                case SET_STATE:
                    logger.info("SET_STATE: oId: {}, comp: {}", message.arg1, message.obj); 
                    State.Component updatedComponent =
                            new ComponentBundle(message.getData()).getComponent();
                    int id = message.arg1;
                    logger.info("Running operation. Component: " + updatedComponent);
                    DelayedOperation op = sameController.getClient().getInterface()
                            .set(updatedComponent);
                    logger.info("Operation finished. Sending callback.");
                    operationStatusCallback(op, id, message.replyTo);
                    logger.info("Callback sent.");
                    break;
                case KILL_MASTER:
                    logger.info("Kill master.");
                    sameController.killMaster();
                default:
                    super.handleMessage(message);
            }
            logger.info("Finished handling message.");
        }
    }
    
    private final Messenger messenger = new Messenger(new InterfaceHandler());

    private StateChangedListener stateListener = new StateChangedListener() {
        @Override
        public void stateChanged(Component component) {
            synchronized (stateReceivers) {
                ArrayList<Messenger> dropped = new ArrayList<Messenger>();
                for (Messenger messenger : stateReceivers) {
                    Message message = Message.obtain(null, UPDATED_STATE_CALLBACK);
                    message.setData(new ComponentBundle(component).getBundle());
                    try {
                        messenger.send(message);
                    } catch (RemoteException e) {
                        logger.warn("Failed to send update. Dropping state receiver.");
                        e.printStackTrace();
                        dropped.add(messenger);
                    }
                }
                stateReceivers.removeAll(dropped);
            }
        }
    };
    
    private void operationStatusCallback(DelayedOperation op, int id, Messenger replyTo) {
        op.waitFor();
        synchronized (stateReceivers) {
            Message message = Message.obtain(null,
                    OPERATION_STATUS_CALLBACK);
            message.arg1 = id;
            message.arg2 = op.getStatus().getStatusCode();
            message.obj = op.getStatus().getMessage();
            try {
                messenger.send(message);
            } catch (RemoteException e) {
                logger.warn("Unable to send update result: " + 
                        op.getStatus());
                e.printStackTrace();
            }
        }
    }
    
    private void sendAllState(Messenger messenger) {
        State state = sameController.getClient().getInterface().getState();
        for (Component c : state.getComponents()) {
            Message message = Message.obtain(null, UPDATED_STATE_CALLBACK);
            message.setData(new ComponentBundle(c).getBundle());
            try {
                messenger.send(message);
            } catch (RemoteException e) {
                logger.warn("Failed to send state.");
                e.printStackTrace();
                return;
            }
        }
    }
    
    private void findNetworks() {
        logger.info("Looking up networks.");
        Services.Directory directory = sameController.getDirectory();
        if (directory == null) {
            logger.warn("No discovery service configured.");
            return;
        }
        final Rpc rpc = new Rpc();
        rpc.setTimeout(10000);
        RpcCallback<Services.NetworkDirectory> done =
                new RpcCallback<Services.NetworkDirectory>() {
            @Override public void run(Services.NetworkDirectory networks) {
                if (!rpc.isOk()) {
                    logger.warn("Unable to find networks: {}", rpc.errorText());
                    return;
                }
                for (Services.MasterState network : networks.getNetworkList()) {
                    networkListener.notifyNetwork(network.getNetworkName(),
                            network.getMasterLocation());
                }
            }
        };
        directory.getNetworks(rpc, Services.Empty.getDefaultInstance(), done);
    }
    
    private void initializeConfiguration() {
        Properties properties = new Properties();
        String localIp = new Networking(this)
                .getWlanAddress().getHostAddress();
        String baseUrl = "http://" + localIp + ":" + SERVICE_PORT + "/";
        properties.setProperty("port", ""+SERVICE_PORT);
        properties.setProperty("pport", ""+PPORT);
        properties.setProperty("localIp", localIp);
        properties.setProperty("baseUrl", baseUrl);
        properties.setProperty("enableDiscovery", "true");
        properties.setProperty("discoveryPort", ""+DISCOVERY_PORT);
        properties.setProperty("networkName", "AndroidNetwork");
        properties.setProperty("directoryLocation", DIRECTORY_URL);
        configuration = new Configuration(properties);
    }
    
    /** Create a public network. */
    private void create() {
        sameController.createNetwork(configuration.get("networkName"));
        sameController.registerCurrentNetwork();
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
        return START_NOT_STICKY;
    }
    
    @Override
    public void onCreate() {
        logger.info("onCreate()");
        
        if (sameController == null) {
            initializeConfiguration();
            sameController = SameController.create(configuration);
            try {
                sameController.start();
                sameController.getClient().getInterface()
                    .addStateListener(stateListener);
                findNetworks();
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
