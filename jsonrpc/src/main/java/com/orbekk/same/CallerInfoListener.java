package com.orbekk.same;

/**
 * An interface to get notified of the current caller.
 *
 * This interface is needed because jsonrpc4j does not pass the
 * HttpServletRequest to the service implementation.
 */
public interface CallerInfoListener {
    void setCaller(String callerIp);
}
