package com.orbekk.same.http;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StateServlet extends HttpServlet {
    private Logger logger = LoggerFactory.getLogger(getClass());
    
    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        logger.error("GOT HERE");
        response.setContentType("text/plain; charset=utf8");
        response.getWriter().println("HI");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
