package com.orbekk.same.android;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
import com.orbekk.util.DelayedOperation;

public class ClientInterfaceBridge implements ClientInterface {
    private State state;
    private ArrayList<StateChangedListener> listeners = 
            new ArrayList<StateChangedListener>();
    private Map<Integer, DelayedOperation> ongoingOperations =
            new HashMap<Integer, DelayedOperation>();
    private int nextOperationNumber = 0;
    
    class ResponseHandler extends Handler {
        @Override public void handleMessage(Message message) {
            if (serviceMessenger == null) {
                logger.warn("Ignoring message to disabled ResponseHandler.");
                return;
            }
            switch (message.what) {
            case SameService.UPDATED_STATE_CALLBACK:
                State.Component component =
                        new ComponentBundle(message.getData()).getComponent();
                updateState(component);
                break;
            case SameService.OPERATION_STATUS_CALLBACK:
                int operationNumber = message.arg1;
                logger.info("Received callback for operation {}", operationNumber);
                int statusCode = message.getData().getInt("statusCode");
                String statusMessage = message.getData().getString("statusMessage");
                DelayedOperation.Status status =
                        new DelayedOperation.Status(statusCode, statusMessage);
                completeOperation(operationNumber, status);
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
            synchronized (ClientInterfaceBridge.this) {
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
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceMessenger = null;
        }
    };
    
    private void updateState(State.Component component) {
        state.forceUpdate(component.getName(), component.getData(),
                component.getRevision());
        for (StateChangedListener listener : listeners) {
            listener.stateChanged(component);
        }
    }
    
    private synchronized DelayedOperation createOperation() {
        DelayedOperation op = new DelayedOperation();
        op.setIdentifier(nextOperationNumber);
        nextOperationNumber += 1;
        ongoingOperations.put(op.getIdentifier(), op);
        return op;
    }
    
    private synchronized void completeOperation(int operationNumber,
        DelayedOperation.Status status) {
        DelayedOperation op = ongoingOperations.remove(operationNumber);
        if (op != null) {
            op.complete(status);
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
    public DelayedOperation set(Component component) {
        DelayedOperation op = createOperation();
        if (serviceMessenger == null) {
            logger.warn("Not connected to service. Ignore update: {}", component);
            completeOperation(op.getIdentifier(),
                    DelayedOperation.Status.createError(
                            "Not connected to service."));
            return op;
        }
        
        Message message = Message.obtain(null, SameService.SET_STATE);
        message.arg1 = op.getIdentifier();
        message.setData(new ComponentBundle(component).getBundle());
        message.replyTo = responseMessenger;
        try {
            logger.info("Sending update to service. No state.");
            serviceMessenger.send(message);
            logger.info("Service finished update.");
        } catch (RemoteException e) {
            e.printStackTrace();
            completeOperation(op.getIdentifier(), 
                    DelayedOperation.Status.createError(
                            "Error contacting service: " + e.getMessage()));
        }
        return op;
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
