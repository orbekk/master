package com.orbekk.rpc;

import java.net.MalformedURLException;
import java.net.URL;

import com.orbekk.same.SameService;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.googlecode.jsonrpc4j.ProxyUtil;

public class Client {
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Arguments: networkAddress");
            System.exit(1);
        }
        String networkAddress = args[0];
        JsonRpcHttpClient client = null;
        try {
            client = new JsonRpcHttpClient(new URL(networkAddress));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        SameService service = ProxyUtil.createProxy(
                client.getClass().getClassLoader(),
                SameService.class,
                client);
        service.notifyNetwork("NoNetwork");
        System.out.println(service.participateNetwork("FirstNetwork"));
    }
}
