package com.orbekk.same;

import com.googlecode.jsonrpc4j.JsonRpcServer;
import com.orbekk.net.HttpUtil;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcHandler extends AbstractHandler {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private JsonRpcServer rpcServer;
    private UrlReceiver urlReceiver;
    
    public RpcHandler(JsonRpcServer rpcServer,
            UrlReceiver urlReceiver) {
        this.rpcServer = rpcServer;
        this.urlReceiver = urlReceiver;
    }
    
    @Override
    public synchronized void handle(String target, Request baseRequest,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        logger.info("Handling request to target: " + target);

        if (urlReceiver != null) {
            String sameServiceUrl = "http://" + request.getLocalAddr() + 
                ":" + request.getLocalPort() + "/SameService.json";
            urlReceiver.setUrl(sameServiceUrl);
            urlReceiver = null;
        }

        if (target.equals("/ping")) {
            int remotePort = Integer.parseInt(request.getParameter("port"));
            String pongUrl = "http://" + request.getRemoteAddr() + ":" +
                remotePort + "/pong";
            logger.info("Got ping. Sending pong to {}", pongUrl);
            HttpUtil.sendHttpRequest(pongUrl);
        } else if (target.equals("/pong")) {
            logger.info("Received pong from {}", request.getRemoteAddr());
        } else {
            rpcServer.handle(request, response);
        }
        baseRequest.setHandled(true);
    }
}
