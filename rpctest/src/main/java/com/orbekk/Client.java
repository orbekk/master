package com.orbekk;

import java.net.URL;
import org.apache.log4j.Logger;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.util.ClientFactory;

public class Client {
    public static void main(String[] args) throws Exception {
        Logger logger = Logger.getLogger("Client");
        logger.info("Client starting.");
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(new URL("http://localhost:10080/xmlrpc"));
        XmlRpcClient client = new XmlRpcClient();
        client.setConfig(config);
        ClientFactory factory = new ClientFactory(client);
        Calculator calculator = (Calculator)factory.newInstance(
                Calculator.class);
        int sum = calculator.add(40, 2);
        for (int i = 0; i < 100; i++) {
            sum = calculator.add(40, 2);
        }
        System.out.println("The answer is " + sum);
        logger.info("Client finished.");
    }
}
