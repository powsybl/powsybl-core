/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ext.base;

import groovy.lang.GroovyRuntimeException;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.control.messages.Message;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ScriptError implements Serializable {

    private static final long serialVersionUID = 8116688293120382652L;

    private final String message;
    private final int startLine;
    private final int startColumn;
    private final int endLine;
    private final int endColumn;

    public ScriptError(String message, int startLine, int startColumn, int endLine, int endColumn) {
        this.message = Objects.requireNonNull(message);
        this.startLine = startLine;
        this.startColumn = startColumn;
        this.endLine = endLine;
        this.endColumn = endColumn;
    }

    public static ScriptError fromGroovyException(MultipleCompilationErrorsException e) {
        ErrorCollector errorCollector = e.getErrorCollector();
        if (errorCollector.getErrorCount() > 0) {
            Message error = errorCollector.getError(0);
            if (error instanceof SyntaxErrorMessage) {
                SyntaxException cause = ((SyntaxErrorMessage) error).getCause();
                return new ScriptError(cause.getMessage(), cause.getStartLine(), cause.getStartColumn(),
                        cause.getEndLine(), cause.getEndColumn());
            } else {
                throw new AssertionError("SyntaxErrorMessage is expected");
            }
        } else {
            throw new AssertionError("At least one error is expected");
        }
    }

    public static ScriptError fromGroovyException(GroovyRuntimeException e, String scriptName) {
        Objects.requireNonNull(scriptName);
        for (StackTraceElement element : e.getStackTrace()) {
            if (scriptName.equals(element.getFileName())
                    && scriptName.equals(element.getClassName())
                    && "run".equals(element.getMethodName())) {
                return new ScriptError(e.getMessage(), element.getLineNumber(), -1, element.getLineNumber(), -1);
            }
        }
        return null;
    }

    public String getMessage() {
        return message;
    }

    public int getStartLine() {
        return startLine;
    }

    public int getStartColumn() {
        return startColumn;
    }

    public int getEndLine() {
        return endLine;
    }

    public int getEndColumn() {
        return endColumn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, startLine, startColumn, endLine, endColumn);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ScriptError) {
            ScriptError other = (ScriptError) obj;
            return message.equals(other.message) &&
                    startLine == other.startLine &&
                    startColumn == other.startColumn &&
                    endLine == other.endLine &&
                    endColumn == other.endColumn;
        }
        return false;
    }

    @Override
    public String toString() {
        return "ScriptError(message=" + message + ", startLine=" + startLine  + ", startColumn=" + startColumn +
                ", endLine=" + endLine + ", endColumn=" + endColumn + ")";
    }
}
