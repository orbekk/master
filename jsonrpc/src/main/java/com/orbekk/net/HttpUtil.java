package com.orbekk.net;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpUtil {
    private static final Logger logger =
            LoggerFactory.getLogger(HttpUtil.class);

    public static void sendHttpRequest(String url) {
        try {
            URL pingUrl = new URL(url);
            pingUrl.openStream();
            // URLConnection connection = pingUrl.openConnection();
            // connection.connect();
        } catch (MalformedURLException e) {
            logger.warn("Unable to send ping to {}: {}.", url, e);
        } catch (IOException e) {
            logger.warn("Error when sending ping: {}", e);
        }
    }
}
