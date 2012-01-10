package com.orbekk.rpc;

import java.net.MalformedURLException;
import java.net.URL;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.googlecode.jsonrpc4j.ProxyUtil;

public class Client {
    
    public static void main(String[] args) {
        JsonRpcHttpClient client = null;
        try {
            client = new JsonRpcHttpClient(
                    new URL("http://10.0.0.96:10080/PingService.json"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        PingService service = ProxyUtil.createProxy(
                client.getClass().getClassLoader(),
                PingService.class,
                client);
        System.out.println(service.ping());
    }
}
