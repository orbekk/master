package com.orbekk.same;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class BroadcastRunner {
    private ConnectionManager connections;
    private Executor executor;

    /**
     * Get a BroadcastRunner for ClientService using a thread pool of size 20.
     */
    public static BroadcastRunner getDefaultBroadcastRunner() {
        return new BroadcastRunner(Executors.newFixedThreadPool(20));
    }
    
    public BroadcastRunner(Executor executor, ConnectionManager connections) {
        this.executor = executor;
    }

    public synchronized void broadcast(final List<String> targets,
            final ServiceOperation operation) {
        for (String t : targets) {
            final ClientService client = connections.getConnection(t);
            executor.execute(new Runnable() {
                @Override public void run() {
                    operation.run(client);
                }
            });
        }
    }
}
