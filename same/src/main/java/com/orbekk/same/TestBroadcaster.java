package com.orbekk.same;

import java.util.List;

/**
 * This class is used in tests.
 */
public class TestBroadcaster implements Broadcaster {

    public TestBroadcaster() {
    }

    @Override
    public void broadcast(final  List<String> targets,
            final ServiceOperation operation) {
        for (String t : targets) {
            operation.run(t);
        }
    }
}
