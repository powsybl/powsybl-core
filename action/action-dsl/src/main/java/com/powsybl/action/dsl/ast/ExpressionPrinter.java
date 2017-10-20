/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.dsl.ast;

import org.apache.commons.io.output.WriterOutputStream;

import java.io.PrintStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ExpressionPrinter extends DefaultExpressionVisitor<Void, Void> {

    private final PrintStream out;

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
    public Void visitNetworkComponent(NetworkComponentNode node, Void arg) {
        switch (node.getComponentType()) {
            case BRANCH:
                out.print("branch");
                break;
            case LINE:
                out.print("line");
                break;
            case TRANSFORMER:
                out.print("transformer");
                break;
            case GENERATOR:
                out.print("generator");
                break;
            case LOAD:
                out.print("load");
                break;
            case SWITCH:
                out.print("switch_");
                break;
            default:
                throw new AssertionError();
        }
        out.print("('");
        out.print(node.getComponentId());
        out.print("')");
        return null;
    }

    @Override
    public Void visitNetworkProperty(NetworkPropertyNode node, Void arg) {
        node.getParent().accept(this, arg);
        out.print(".");
        out.print(node.getPropertyName());
        return null;
    }

    @Override
    public Void visitNetworkMethod(NetworkMethodNode node, Void arg) {
        node.getParent().accept(this, arg);
        out.print(".");
        out.print(node.getMethodName());
        out.print("(");
        for (int i = 0; i < node.getArgs().length; i++) {
            Object arg2 = node.getArgs()[i];
            out.print(arg2);
            if (i < node.getArgs().length - 1) {
                out.print(", ");
            }
        }
        out.print(")");
        return null;
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
                throw new AssertionError();
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
                throw new AssertionError();
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
                throw new AssertionError();
        }
        out.print(" ");
        node.getRight().accept(this, arg);
        out.print(")");
        return null;
    }

    @Override
    public Void visitActionTaken(ActionTakenNode node, Void arg) {
        out.print("actionTaken('");
        out.print(node.getActionId());
        out.print("')");
        return null;
    }

    @Override
    public Void visitContingencyOccurred(ContingencyOccurredNode node, Void arg) {
        out.print("contingencyOccurred(");
        if (node.getContingencyId() != null) {
            out.print("'");
            out.print(node.getContingencyId());
            out.print("'");
        }
        out.print(")");
        return null;
    }

    @Override
    public Void visitLoadingRank(LoadingRankNode node, Void arg) {
        out.print("loadingRank(");
        node.getBranchIdToRankNode().accept(this, arg);
        out.print(", [");
        Iterator<ExpressionNode> it = node.getBranchIds().iterator();
        while (it.hasNext()) {
            out.print(it.next().accept(this, arg));
            if (it.hasNext()) {
                out.print(", ");
            }
        }
        out.print("])");
        return null;
    }

    @Override
    public Void visitMostLoaded(MostLoadedNode node, Void arg) {
        out.print("mostLoaded(");
        out.print("[");
        out.print(String.join(", ", node.getBranchIds()));
        out.print("])");
        return null;
    }

    @Override
    public Void visitIsOverloaded(IsOverloadedNode node, Void arg) {
        out.print("isOverloaded(");
        out.print("[");
        out.print(String.join(", ", node.getBranchIds()));
        out.print("])");
        return null;
    }

}
