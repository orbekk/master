package com.orbekk.same;

import java.util.List;

/**
 * This class is used in tests.
 */
public class TestBroadcaster implements Broadcaster {
    public ConnectionManager connections;

    public TestBroadcaster() {
    }

    public TestBroadcaster(ConnectionManager connections) {
        this.connections = connections;
    }

    public void broadcast(final  List<String> targets,
            final ServiceOperation operation) {
        for (String t : targets) {
            ClientService client = connections.getClient(t);
            operation.run(client);
        }
    }
}
