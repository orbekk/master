package com.orbekk.same.android;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.orbekk.same.ClientInterface;
import com.orbekk.same.SameService;
import com.orbekk.same.State;
import com.orbekk.same.State.Component;
import com.orbekk.same.StateChangedListener;
import com.orbekk.same.UpdateConflict;
import com.orbekk.same.VariableFactory;

public class ClientInterfaceBridge implements ClientInterface {
    private State state;
    private ArrayList<StateChangedListener> listeners = 
            new ArrayList<StateChangedListener>();
    
    class ResponseHandler extends Handler {
        @Override public void handleMessage(Message message) {
            if (serviceMessenger == null) {
                logger.warn("Ignoring message to disabled ResponseHandler.");
                return;
            }
            switch (message.what) {
            case SameService.UPDATED_STATE_MESSAGE:
                State.Component component = (State.Component)message.obj;
                updateState(component);
                break;
            default:
                logger.warn("Received unknown message from service: {}",
                        message);
            }
        }
    }

    private Logger logger = LoggerFactory.getLogger(getClass());
    private Messenger serviceMessenger = null;
    private Messenger responseMessenger = new Messenger(new ResponseHandler());
    private Context context;
    
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            serviceMessenger = new Messenger(service);
            Message message = Message.obtain(null,
                    SameService.ADD_STATE_RECEIVER);
            message.replyTo = responseMessenger;
            try {
                serviceMessenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceMessenger = null;
        }
    };
    
    private void updateState(State.Component component) {
        state.update(component.getName(), component.getData(),
                component.getRevision());
        for (StateChangedListener listener : listeners) {
            listener.stateChanged(component);
        }
    }
    
    public ClientInterfaceBridge(Context context) {
        this.context = context;
    }
    
    public void connect() {
        state = new State(".Temporary");
        Intent intent = new Intent(context, SameService.class);
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }
        
    private void disconnectFromService() {
        Message message = Message.obtain(null, SameService.REMOVE_STATE_RECEIVER);
        message.obj = responseMessenger;
        try {
            serviceMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    
    public void disconnect() {
        if (serviceMessenger != null) {
            disconnectFromService();
            context.unbindService(serviceConnection);
            state = null;
        }
    }

    @Override
    public State getState() {
        return new State(state);
    }

    @Override
    public void set(String name, String data, long revision) throws UpdateConflict {
        set(new Component(name, revision, data));
    }

    @Override
    public void set(Component component) throws UpdateConflict {
        Message message = Message.obtain(null, SameService.SET_STATE);
        message.obj = component;
        if (serviceMessenger == null) {
            logger.warn("Not connected to service. Ignore update: {}", component);
            throw new UpdateConflict("Not connected to Android Service.");
        }
        try {
            serviceMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
            throw new UpdateConflict(e.getMessage());
        }
    }

    @Override
    public void addStateListener(StateChangedListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeStateListener(StateChangedListener listener) {
        listeners.remove(listener);
    }

    public VariableFactory createVariableFactory() {
        return VariableFactory.create(this);
    }

}
