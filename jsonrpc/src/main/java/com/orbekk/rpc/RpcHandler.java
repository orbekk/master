package com.orbekk.rpc;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.googlecode.jsonrpc4j.JsonRpcServer;

public class RpcHandler extends AbstractHandler {
    private JsonRpcServer rpcServer;
    
    public RpcHandler(JsonRpcServer rpcServer) {
        this.rpcServer = rpcServer;
    }
    
    @Override
    public void handle(String target, Request baseRequest,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        rpcServer.handle(request, response);
    }
}
