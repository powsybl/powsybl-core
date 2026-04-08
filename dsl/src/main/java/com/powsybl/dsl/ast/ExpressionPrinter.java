/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dsl.ast;

import java.io.*;
import java.nio.charset.Charset;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class ExpressionPrinter extends DefaultExpressionVisitor<Void, Void> {

    protected final PrintWriter out;

    public static String toString(ExpressionNode node) {
        try (StringWriter writer = new StringWriter()) {
            write(node, writer);
            return writer.toString();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void print(ExpressionNode node) {
        print(node, System.out);
    }

    public static void print(ExpressionNode node, OutputStream out) {
        node.accept(new ExpressionPrinter(out), null);
    }

    private static void write(ExpressionNode node, StringWriter out) {
        node.accept(new ExpressionPrinter(out), null);
    }

    /**
     * Create an ExpressionPrinter that uses the default character encoding.
     *
     * @param out The {@link OutputStream} used by this printer
     */
    public ExpressionPrinter(OutputStream out) {
        this.out = new PrintWriter(out);
    }

    /**
     * Create an ExpressionPrinter that uses the given charset.
     *
     * @param out The {@link OutputStream} used by this printer
     * @param cs Charset to use by the {@link OutputStreamWriter} instance
     */
    public ExpressionPrinter(OutputStream out, Charset cs) {
        this.out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(out, cs)));
    }

    public ExpressionPrinter(Writer writer) {
        out = new PrintWriter(writer);
    }

    @Override
    public Void visitLiteral(AbstractLiteralNode node, Void arg) {
        out.write(node.getValue().toString());
        out.flush();
        return null;
    }

    @Override
    public Void visitLogicalOperator(LogicalBinaryOperatorNode node, Void arg) {
        out.write("(");
        node.getLeft().accept(this, arg);
        out.write(" ");
        switch (node.getOperator()) {
            case AND:
                out.write("&&");
                break;
            case OR:
                out.write("||");
                break;
            default:
                throw createUnexpectedOperatorException(node.getOperator().name());
        }
        out.write(" ");
        node.getRight().accept(this, arg);
        out.write(")");
        out.flush();
        return null;
    }

    @Override
    public Void visitArithmeticOperator(ArithmeticBinaryOperatorNode node, Void arg) {
        out.write("(");
        node.getLeft().accept(this, arg);
        out.write(" ");
        switch (node.getOperator()) {
            case PLUS:
                out.write("+");
                break;
            case MINUS:
                out.write("-");
                break;
            case MULTIPLY:
                out.write("*");
                break;
            case DIVIDE:
                out.write("/");
                break;
            default:
                throw createUnexpectedOperatorException(node.getOperator().name());
        }
        out.write(" ");
        node.getRight().accept(this, arg);
        out.write(")");
        out.flush();
        return null;
    }

    @Override
    public Void visitNotOperator(LogicalNotOperator node, Void arg) {
        out.write("!(");
        node.getChild().accept(this, arg);
        out.write(")");
        out.flush();
        return null;
    }

    @Override
    public Void visitComparisonOperator(ComparisonOperatorNode node, Void arg) {
        out.write("(");
        node.getLeft().accept(this, arg);
        out.write(" ");
        switch (node.getOperator()) {
            case EQUALS:
                out.write("==");
                break;
            case NOT_EQUALS:
                out.write("!=");
                break;
            case GREATER_THAN:
                out.write(">");
                break;
            case LESS_THAN:
                out.write("<");
                break;
            case GREATER_THAN_OR_EQUALS_TO:
                out.write(">=");
                break;
            case LESS_THAN_OR_EQUALS_TO:
                out.write("<=");
                break;
            default:
                throw createUnexpectedOperatorException(node.getOperator().name());
        }
        out.write(" ");
        node.getRight().accept(this, arg);
        out.write(")");
        out.flush();
        return null;
    }

    private static IllegalStateException createUnexpectedOperatorException(String operatorName) {
        return new IllegalStateException("Unexpected operator: " + operatorName);
    }
}
