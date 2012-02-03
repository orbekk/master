package com.orbekk.same.http;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.ContextHandler.Context;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.jsonrpc4j.JsonRpcServer;
import com.orbekk.paxos.PaxosService;
import com.orbekk.same.ClientService;
import com.orbekk.same.MasterService;

public class HandlerFactory {
    Logger logger = LoggerFactory.getLogger(getClass());
    
    public Handler createServletHandler() {
        logger.info("Creating servlet handler.");
        ServletContextHandler context = new ServletContextHandler(
                ServletContextHandler.SESSIONS);
        context.setContextPath("/*");
        context.addServlet(new ServletHolder(new StateServlet()), "/*");
        return context;
    }
    
    public RpcHandler createRpcHandler(MasterService master,
            ClientService client, PaxosService paxos) {
        RpcHandler rpcHandler = new RpcHandler(null);
        rpcHandler.addRpcServer("/MasterService.json", 
                new JsonRpcServer(master, MasterService.class));
        rpcHandler.addRpcServer("/ClientService.json", 
                new JsonRpcServer(client, ClientService.class));
        rpcHandler.addRpcServer("/PaxosService.json", 
                new JsonRpcServer(paxos, PaxosService.class));
        return rpcHandler;
    }
    
    public Handler createMainHandler(RpcHandler rpcHandler,
            Handler servletHandler) {
        HandlerList handler = new HandlerList();
        handler.addHandler(rpcHandler);
        handler.addHandler(servletHandler);
        
        return handler;
    }
            
}
