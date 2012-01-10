package com.orbekk.rpc;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.googlecode.jsonrpc4j.JsonRpcServer;
import com.orbekk.same.CallerInfoListener;

public class RpcHandler extends AbstractHandler {
    private JsonRpcServer rpcServer;
    private CallerInfoListener callerInfoListener;
    
    public RpcHandler(JsonRpcServer rpcServer,
            CallerInfoListener callerInfoListener) {
        this.rpcServer = rpcServer;
        this.callerInfoListener = callerInfoListener;
    }
    
    @Override
    public synchronized void handle(String target, Request baseRequest,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        if (callerInfoListener != null) {
            callerInfoListener.setCaller(request.getRemoteAddr());
        }
        rpcServer.handle(request, response);
    }
}
