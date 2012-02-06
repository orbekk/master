package com.orbekk.same.http;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orbekk.same.Client;

public class StateServlet extends HttpServlet {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Client.ClientInterface client;
    
    public StateServlet(Client.ClientInterface client) {
        this.client = client;
    }
    
    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        response.setContentType("text/plain; charset=utf8");
        response.getWriter().println(client.getState());
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
