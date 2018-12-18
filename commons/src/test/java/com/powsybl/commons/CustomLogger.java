package com.powsybl.commons;
/**
 * @author Chamseddine BENHAMED <chamseddine.benhamed at rte-france.com>
 */

import org.slf4j.Logger;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;

public class CustomLogger extends MarkerIgnoringBase implements Logger {

    private StringBuilder buffer = new StringBuilder();

    private static final int LOG_LEVEL_TRACE = 0;
    private static final int LOG_LEVEL_DEBUG = 10;
    private static final int LOG_LEVEL_INFO = 20;
    private static final int LOG_LEVEL_WARN = 30;
    private static final int LOG_LEVEL_ERROR = 40;
    private static final int LOG_LEVEL_OFF = 50;
    private static int DEFAULT_LOG_LEVEL = 20;
    private static String name;
    private int currentLogLevel = 10;
    private static boolean LEVEL_IN_BRACKETS = true;

    public CustomLogger(String name) {
        this.name = name;
    }

    public void setLogLevel(String levelStr) {
        this.currentLogLevel = stringToLevel(levelStr);
    }

    private static int stringToLevel(String levelStr) {
        if ("trace".equalsIgnoreCase(levelStr)) {
            return 0;
        } else if ("debug".equalsIgnoreCase(levelStr)) {
            return 10;
        } else if ("info".equalsIgnoreCase(levelStr)) {
            return 20;
        } else if ("warn".equalsIgnoreCase(levelStr)) {
            return 30;
        } else if ("error".equalsIgnoreCase(levelStr)) {
            return 40;
        } else {
            return "off".equalsIgnoreCase(levelStr) ? 50 : 10;
        }
    }

    protected boolean isLevelEnabled(int logLevel) {
        return logLevel >= this.currentLogLevel;
    }

    private void log(int level, String message, Throwable t) {
        if (this.isLevelEnabled(level)) {
            if (LEVEL_IN_BRACKETS) {
                buffer.append('[');
            }

            switch (level) {
                case 0:
                    buffer.append("TRACE");
                    break;
                case 10:
                    buffer.append("DEBUG");
                    break;
                case 20:
                    buffer.append("INFO");
                    break;
                case 30:
                    buffer.append("WARN");
                    break;
                case 40:
                    buffer.append("ERROR");
            }

            if (LEVEL_IN_BRACKETS) {
                buffer.append(']');
            }
            buffer.append(' ');
            buffer.append(message);
        }
    }

    private void formatAndLog(int level, String format, Object arg1, Object arg2) {
        if (this.isLevelEnabled(level)) {
            FormattingTuple tp = MessageFormatter.format(format, arg1, arg2);
            this.log(level, tp.getMessage(), tp.getThrowable());
        }
    }

    private void formatAndLog(int level, String format, Object... arguments) {
        if (this.isLevelEnabled(level)) {
            FormattingTuple tp = MessageFormatter.arrayFormat(format, arguments);
            this.log(level, tp.getMessage(), tp.getThrowable());
        }
    }

    public String getContent() {
        return buffer.toString();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean isTraceEnabled() {
        return this.isLevelEnabled(0);
    }

    @Override
    public void trace(String s) {
        this.log(0, s, (Throwable) null);
    }

    public void trace(String format, Object param1) {
        this.formatAndLog(0, format, param1, (Object) null);
    }

    public void trace(String format, Object param1, Object param2) {
        this.formatAndLog(0, format, param1, param2);
    }

    public void trace(String format, Object... argArray) {
        this.formatAndLog(0, format, argArray);
    }

    public void trace(String msg, Throwable t) {
        this.log(0, msg, t);
    }

    public boolean isDebugEnabled() {
        return this.isLevelEnabled(10);
    }

    public void debug(String msg) {
        this.log(10, msg, (Throwable) null);
    }

    public void debug(String format, Object param1) {
        this.formatAndLog(10, format, param1, (Object) null);
    }

    public void debug(String format, Object param1, Object param2) {
        this.formatAndLog(10, format, param1, param2);
    }

    public void debug(String format, Object... argArray) {
        this.formatAndLog(10, format, argArray);
    }

    public void debug(String msg, Throwable t) {
        this.log(10, msg, t);
    }

    public boolean isInfoEnabled() {
        return this.isLevelEnabled(20);
    }

    public void info(String msg) {
        this.log(20, msg, (Throwable) null);
    }

    public void info(String format, Object arg) {
        this.formatAndLog(20, format, arg, (Object) null);
    }

    public void info(String format, Object arg1, Object arg2) {
        this.formatAndLog(20, format, arg1, arg2);
    }

    public void info(String format, Object... argArray) {
        this.formatAndLog(20, format, argArray);
    }

    public void info(String msg, Throwable t) {
        this.log(20, msg, t);
    }

    public boolean isWarnEnabled() {
        return this.isLevelEnabled(30);
    }

    public void warn(String msg) {
        this.log(30, msg, (Throwable) null);
    }

    public void warn(String format, Object arg) {
        this.formatAndLog(30, format, arg, (Object) null);
    }

    public void warn(String format, Object arg1, Object arg2) {
        this.formatAndLog(30, format, arg1, arg2);
    }

    public void warn(String format, Object... argArray) {
        this.formatAndLog(30, format, argArray);
    }

    public void warn(String msg, Throwable t) {
        this.log(30, msg, t);
    }

    public boolean isErrorEnabled() {
        return this.isLevelEnabled(40);
    }

    public void error(String msg) {
        this.log(40, msg, (Throwable) null);
    }

    public void error(String format, Object arg) {
        this.formatAndLog(40, format, arg, (Object) null);
    }

    public void error(String format, Object arg1, Object arg2) {
        this.formatAndLog(40, format, arg1, arg2);
    }

    public void error(String format, Object... argArray) {
        this.formatAndLog(40, format, argArray);
    }

    public void error(String msg, Throwable t) {
        this.log(40, msg, t);
    }
}
