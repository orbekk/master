package com.orbekk.same;

/**
 * An interface to get notified of the URL to this computer.
 *
 * This interface is used to reliably obtain the URL of this host.
 */
public interface UrlReceiver {
    void setUrl(String url);
}
