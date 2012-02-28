package com.orbekk.same;

import com.orbekk.same.http.HelloServlet;
import com.orbekk.same.http.TjwsServerBuilder;
import com.orbekk.same.http.TjwsServerContainer;

public class TjwsApp {
    public static void main(String[] args) {
        TjwsServerContainer server = new TjwsServerBuilder(8080)
                .withServlet(new HelloServlet(), "/hello")
                .build();
        server.start();
        server.join();
    }
}
