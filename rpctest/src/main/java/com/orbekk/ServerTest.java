package com.orbekk;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcHandlerMapping;
import org.apache.xmlrpc.webserver.ServletWebServer;
import org.apache.xmlrpc.webserver.XmlRpcServlet;

public class ServerTest {
    final static int port = 10080;

    public static class HandlerServlet extends XmlRpcServlet {
        private XmlRpcHandlerMapping mapping;

        public HandlerServlet(XmlRpcHandlerMapping mapping)
                throws ServletException {
            this.mapping = mapping;
        }

        @Override
        protected XmlRpcHandlerMapping newXmlRpcHandlerMapping()
                throws XmlRpcException {
            return mapping;
        }
    }

    public static void main(String[] args) {
        System.out.println("Starting " + ServerTest.class.getName());
        try {
            PropertyHandlerMapping phm = new PropertyHandlerMapping();
            phm.setVoidMethodEnabled(true);
            phm.addHandler(Calculator.class.getName(), CalculatorImpl.class);

            XmlRpcServlet servlet = new HandlerServlet(phm);
            ServletWebServer server = new ServletWebServer(servlet, port);
            server.start();
        } catch (XmlRpcException e) {
            System.err.println("Error creating property mapping.");
            e.printStackTrace();
        } catch (ServletException e) {
            System.err.println("Unable to initialize servlet.");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Exception from web server.");
            e.printStackTrace();
        }
    }
}
