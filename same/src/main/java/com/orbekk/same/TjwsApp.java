package com.orbekk.same;

import com.orbekk.same.http.HelloServlet;
import com.orbekk.same.http.ServerContainer;
import com.orbekk.same.http.TjwsServerBuilder;

public class TjwsApp {
    public static void main(String[] args) {
        ServerContainer server = new TjwsServerBuilder(8080)
                .withServlet(new HelloServlet(), "/hello")
                .build();
        try {
            server.start();
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
