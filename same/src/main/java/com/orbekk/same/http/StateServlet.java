package com.orbekk.same.http;

import static com.orbekk.same.StackTraceUtil.throwableToString;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orbekk.same.Client;
import com.orbekk.same.UpdateConflict;

public class StateServlet extends HttpServlet {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Client.ClientInterface client;
    
    public StateServlet(Client.ClientInterface client) {
        this.client = client;
    }
    
    private void handleSetState(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        if (request.getParameter("key") == null ||
                request.getParameter("value") == null) {
            response.getWriter().println(
                    "Usage: action=set&key=DesiredKey&value=DesiredValue");
        }
        
        try {
            client.set(request.getParameter("key"), request.getParameter("value"));
            response.getWriter().println("Updated component: " +
                    request.getParameter("key") + "=" +
                    request.getParameter("value"));
        } catch (UpdateConflict e) {
            response.getWriter().println("Update conflict: " +
                    throwableToString(e));
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        if ("set".equals(request.getParameter("action"))) {
            handleSetState(request, response);
        }
        
        response.setContentType("text/plain; charset=utf8");
        response.getWriter().println(client.getState());
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
