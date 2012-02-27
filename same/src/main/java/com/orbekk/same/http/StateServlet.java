package com.orbekk.same.http;

import static com.orbekk.same.StackTraceUtil.throwableToString;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orbekk.same.ClientInterface;
import com.orbekk.same.UpdateConflict;
import com.orbekk.same.Variable;
import com.orbekk.same.VariableFactory;

public class StateServlet extends HttpServlet {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private ClientInterface client;
    private VariableFactory variableFactory;
    private final static String TITLE = "State viewer";

    public StateServlet(ClientInterface client,
            VariableFactory variableFactory) {
        this.client = client;
        this.variableFactory = variableFactory;
    }

    private void handleSetState(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        if (request.getParameter("key") == null ||
                request.getParameter("value") == null) {
            response.getWriter().println(
                    "Usage: action=set&key=DesiredKey&value=DesiredValue");
        }

        try {
            String key = request.getParameter("key");
            String value = request.getParameter("value");
            Variable<String> variable = variableFactory.createString(key);
            variable.set(value);

            response.getWriter().println("Updated component: " +
                    key + "=" + value);
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
            response.sendRedirect(request.getServletPath() + "?message=OK");
        } else {
            response.setContentType("text/html; charset=utf8");
            writeHeader(response);            
            if (request.getParameter("message") != null) {
                response.getWriter().println("<p>");
                response.getWriter().println(request.getParameter("message"));
            }
            writeState(response);
            writeSetStateForm(response);
            writeFooter(response);
            response.setStatus(HttpServletResponse.SC_OK);
        }        
    }

    private void writeState(HttpServletResponse response) throws IOException {
        PrintWriter w = response.getWriter();
        w.println("<h2>State</h2>");
        w.println("<pre>");
        w.println(client.getState());
        w.println("</pre>");
    }

    private void writeSetStateForm(HttpServletResponse response)
            throws IOException {
        PrintWriter w = response.getWriter();
        w.println("<h3>Change state</h3>");
        w.println("<form name=\"stateInput\" action=\"\">");
        w.println("<p>Key: <input type=\"text\" name=\"key\" />");
        w.println("<p>Value: <input type=\"text\" name=\"value\" />");
        w.println("<input type=\"hidden\" name=\"action\" value=\"set\" />");
        w.println("<p><input type=\"submit\" value=\"Sumbit\" />");
        w.println("</form>");
    }

    private void writeHeader(HttpServletResponse response) throws IOException {
        PrintWriter w = response.getWriter();
        w.println("<html>");
        w.println("<head>");
        w.println("<title>" + TITLE + "</title>");
        w.println("</head>");
        w.println("<body>");
    }

    private void writeFooter(HttpServletResponse response) throws IOException {
        PrintWriter w = response.getWriter();
        w.println("</body>");
        w.println("</html>");
    }    
}
