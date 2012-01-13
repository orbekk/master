package com.orbekk.same;

/**
 * An interface that returns a connection for a participant.
 *
 * When testing, this interface can be mocked to use local participants only.
 */
public interface ConnectionManager {
    SameService getConnection(String url);
}
