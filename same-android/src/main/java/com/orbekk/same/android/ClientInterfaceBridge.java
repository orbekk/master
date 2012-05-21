/**
 * Copyright 2012 Kjetil Ørbekk <kjetil.orbekk@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.orbekk.same.android;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

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
import com.orbekk.same.ConnectionState;
import com.orbekk.same.ConnectionStateListener;
import com.orbekk.same.State;
import com.orbekk.same.State.Component;
import com.orbekk.same.StateChangedListener;
import com.orbekk.same.VariableFactory;
import com.orbekk.util.DelayedOperation;

public class ClientInterfaceBridge implements ClientInterface {
    private State state;
    private final List<StateChangedListener> listeners = 
            new CopyOnWriteArrayList<StateChangedListener>();
    private final Map<Integer, DelayedOperation> ongoingOperations =
            new HashMap<Integer, DelayedOperation>();
    /** This is used to queue operations until connected to the service. */
    private final ArrayList<Message> pendingOperations = new ArrayList<Message>();
    private volatile int nextOperationNumber = 0;
    
    class ResponseHandler extends Handler {
        @Override public synchronized void handleMessage(Message message) {
            logger.info("ResponseHandler: Got here. Message: {}", message);
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
    private volatile Messenger serviceMessenger = null;
    private volatile Messenger responseMessenger = new Messenger(new ResponseHandler());
    private volatile Context context;
    
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
                sendPendingOperations();
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
    
    public synchronized void connect() {
        state = new State();
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
    
    public synchronized void disconnect() {
        if (serviceMessenger != null) {
            disconnectFromService();
        }
        if (serviceConnection != null) {
            context.unbindService(serviceConnection);
        }
    }

    @Override
    public State getState() {
        return new State(state);
    }

    @Override
    public synchronized DelayedOperation set(Component component) {
        DelayedOperation op = createOperation();
        Message message = Message.obtain(null, SameService.SET_STATE);
        message.arg1 = op.getIdentifier();
        message.setData(new ComponentBundle(component).getBundle());
        message.replyTo = responseMessenger;
        pendingOperations.add(message);
        sendPendingOperations();
        return op;
    }
    
    private synchronized void sendPendingOperations() {
        if (serviceMessenger == null) {
            logger.warn("Not connected to service. Delaying operations {}",
                    pendingOperations);
            return;
        }
        for (Message message : pendingOperations) {
            try {
                serviceMessenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
                completeOperation(message.arg1, 
                        DelayedOperation.Status.createError(
                                "Error contacting service: " + e.getMessage()));
            }
        }
        pendingOperations.clear();
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

    /** Doesn't work yet. Maybe we needs a listener interface? */
    @Override
    public ConnectionState getConnectionState() {
        return null;
    }

    @Override
    public void addConnectionStateListener(ConnectionStateListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeConnectionStateListener(ConnectionStateListener listener) {
        throw new UnsupportedOperationException();
    }
}
