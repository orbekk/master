package com.orbekk.same;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class BroadcasterImpl {
    private ConnectionManager connections;
    private Executor executor;

    /**
     * Get a BroadcastRunner for ClientService using a thread pool of size 20.
     */
    public static BroadcasterImpl getDefaultBroadcastRunner() {
        return new BroadcasterImpl(Executors.newFixedThreadPool(20),
                new ConnectionManagerImpl());
    }
    
    public BroadcasterImpl(Executor executor,
            ConnectionManager connections) {
        this.connections = connections;
        this.executor = executor;
    }

    public synchronized void broadcast(final List<String> targets,
            final ServiceOperation operation) {
        for (String t : targets) {
            final ClientService client = connections.getClient(t);
            executor.execute(new Runnable() {
                @Override public void run() {
                    operation.run(client);
                }
            });
        }
    }
}
