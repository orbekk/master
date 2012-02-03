package com.orbekk.same.http;

import com.googlecode.jsonrpc4j.JsonRpcServer;
import com.orbekk.net.HttpUtil;
import com.orbekk.same.UrlReceiver;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcHandler extends AbstractHandler {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private UrlReceiver urlReceiver;
    private Map<String, JsonRpcServer> rpcServers =
            new HashMap<String, JsonRpcServer>();
    
    public RpcHandler(UrlReceiver urlReceiver) {
        this.urlReceiver = urlReceiver;
    }

    /**
     * Add an RpcServer to this Handler.
     *
     * @param url the base url of the service, e.g.
     *          /MyService.json
     */
    public void addRpcServer(String url, JsonRpcServer rpcServer) {
        rpcServers.put(url, rpcServer);
    }
    
    @Override
    public synchronized void handle(String target, Request baseRequest,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        logger.info("Handling request to target: " + target);

        if (urlReceiver != null) {
            String sameServiceUrl = "http://" + request.getLocalAddr() + 
                ":" + request.getLocalPort() + "/";
            urlReceiver.setUrl(sameServiceUrl);
            urlReceiver = null;
        }

        if (target.equals("/ping")) {
            int remotePort = Integer.parseInt(request.getParameter("port"));
            String pongUrl = "http://" + request.getRemoteAddr() + ":" +
                remotePort + "/pong";
            logger.info("Got ping. Sending pong to {}", pongUrl);
            HttpUtil.sendHttpRequest(pongUrl);
            baseRequest.setHandled(true);
        } else if (target.equals("/pong")) {
            logger.info("Received pong from {}", request.getRemoteAddr());
            baseRequest.setHandled(true);
        } else {
            JsonRpcServer server = rpcServers.get(target);
            if (server != null) {
                server.handle(request, response);
                baseRequest.setHandled(true);
            }
        }
    }
}
