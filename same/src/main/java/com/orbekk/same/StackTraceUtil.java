package com.orbekk.same;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class StackTraceUtil {
    public static String throwableToString(Throwable t) {
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        t.printStackTrace(printWriter);
        return writer.toString();
    }
}
