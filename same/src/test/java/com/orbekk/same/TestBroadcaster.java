package com.orbekk.same;

import java.util.List;

public class TestBroadcaster implements Broadcaster {
    private ConnectionManager connections;

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
