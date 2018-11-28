/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dsl.ast;

import org.apache.commons.io.output.WriterOutputStream;

import java.io.PrintStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ExpressionPrinter extends DefaultExpressionVisitor<Void, Void> {

    protected final PrintStream out;

    public static String toString(ExpressionNode node) {
        StringWriter writer = new StringWriter();
        try (PrintStream os = new PrintStream(new WriterOutputStream(writer, StandardCharsets.UTF_8))) {
            print(node, os);
        }

        return writer.toString();
    }

    public static void print(ExpressionNode node) {
        print(node, System.out);
    }

    public static void print(ExpressionNode node, PrintStream out) {
        node.accept(new ExpressionPrinter(out), null);
    }

    public ExpressionPrinter(PrintStream out) {
        this.out = Objects.requireNonNull(out);
    }

    @Override
    public Void visitLiteral(AbstractLiteralNode node, Void arg) {
        out.print(node.getValue().toString());
        return null;
    }

    @Override
    public Void visitLogicalOperator(LogicalBinaryOperatorNode node, Void arg) {
        out.print("(");
        node.getLeft().accept(this, arg);
        out.print(" ");
        switch (node.getOperator()) {
            case AND:
                out.print("&&");
                break;
            case OR:
                out.print("||");
                break;
            default:
                throw createUnexpectedOperatorException(node.getOperator().name());
        }
        out.print(" ");
        node.getRight().accept(this, arg);
        out.print(")");
        return null;
    }

    @Override
    public Void visitArithmeticOperator(ArithmeticBinaryOperatorNode node, Void arg) {
        out.print("(");
        node.getLeft().accept(this, arg);
        out.print(" ");
        switch (node.getOperator()) {
            case PLUS:
                out.print("+");
                break;
            case MINUS:
                out.print("-");
                break;
            case MULTIPLY:
                out.print("*");
                break;
            case DIVIDE:
                out.print("/");
                break;
            default:
                throw createUnexpectedOperatorException(node.getOperator().name());
        }
        out.print(" ");
        node.getRight().accept(this, arg);
        out.print(")");
        return null;
    }

    @Override
    public Void visitNotOperator(LogicalNotOperator node, Void arg) {
        out.print("!(");
        node.getChild().accept(this, arg);
        out.print(")");
        return null;
    }

    @Override
    public Void visitComparisonOperator(ComparisonOperatorNode node, Void arg) {
        out.print("(");
        node.getLeft().accept(this, arg);
        out.print(" ");
        switch (node.getOperator()) {
            case EQUALS:
                out.print("==");
                break;
            case NOT_EQUALS:
                out.print("!=");
                break;
            case GREATER_THAN:
                out.print(">");
                break;
            case LESS_THAN:
                out.print("<");
                break;
            case GREATER_THAN_OR_EQUALS_TO:
                out.print(">=");
                break;
            case LESS_THAN_OR_EQUALS_TO:
                out.print("<=");
                break;
            default:
                throw createUnexpectedOperatorException(node.getOperator().name());
        }
        out.print(" ");
        node.getRight().accept(this, arg);
        out.print(")");
        return null;
    }

    private static AssertionError createUnexpectedOperatorException(String operatorName) {
        return new AssertionError("Unexpected operator: " + operatorName);
    }
}
