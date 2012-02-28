package com.orbekk.same;

import com.orbekk.same.http.TjwsServerContainer;

public class TjwsApp {
    public static void main(String[] args) {
        TjwsServerContainer server = TjwsServerContainer.create(8080);
        server.start();
        server.join();
    }
}
