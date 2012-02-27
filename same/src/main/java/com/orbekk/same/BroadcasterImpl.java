package com.orbekk.same;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class BroadcasterImpl implements Broadcaster {
    private Executor executor;

    /**
     * Get a BroadcastRunner for ClientService using a thread pool of size 20.
     */
    public static BroadcasterImpl getDefaultBroadcastRunner() {
        return new BroadcasterImpl(Executors.newFixedThreadPool(20));
    }

    public BroadcasterImpl(Executor executor) {
        this.executor = executor;
    }

    @Override
    public synchronized void broadcast(final List<String> targets,
            final ServiceOperation operation) {
        for (final String t : targets) {
            executor.execute(new Runnable() {
                @Override public void run() {
                    operation.run(t);
                }
            });
        }
    }
}
