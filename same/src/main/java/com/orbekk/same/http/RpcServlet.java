package com.orbekk.same.http;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.jsonrpc4j.JsonRpcServer;

public class RpcServlet extends HttpServlet {
    JsonRpcServer rpcServer;
    
    public RpcServlet(JsonRpcServer rpcServer) {
        super();
        this.rpcServer = rpcServer;
    }
    
    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        rpcServer.handle(request, response);
    }
}
