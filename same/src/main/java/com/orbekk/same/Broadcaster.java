package com.orbekk.same;

import java.util.List;

/**
 * An interface for broadcasting a message to all clients.
 */
public interface Broadcaster {
    public void broadcast(final List<String> targets,
            final ServiceOperation operation);
}
