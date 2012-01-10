package com.orbekk.rpc;

import java.net.MalformedURLException;
import java.net.URL;

import com.orbekk.same.SameService;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.googlecode.jsonrpc4j.ProxyUtil;

public class Client {
    
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Arguments: networkAddress port");
            System.exit(1);
        }
        String networkAddress = args[0];
        int port = Integer.parseInt(args[1]);
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
        service.participateNetwork("FirstNetwork", port);
    }
}
