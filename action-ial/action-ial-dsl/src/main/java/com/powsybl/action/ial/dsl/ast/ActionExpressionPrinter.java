/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.ial.dsl.ast;

import com.powsybl.dsl.ast.ExpressionNode;
import com.powsybl.dsl.ast.ExpressionPrinter;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Iterator;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class ActionExpressionPrinter extends ExpressionPrinter implements ActionExpressionVisitor<Void, Void> {

    public static String toString(ExpressionNode node) {
        try (StringWriter writer = new StringWriter()) {
            write(node, writer);
            return writer.toString();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void write(ExpressionNode node, StringWriter out) {
        node.accept(new ActionExpressionPrinter(out), null);
    }

    public static void print(ExpressionNode node) {
        print(node, System.out);
    }

    public static void print(ExpressionNode node, OutputStream out) {
        node.accept(new ActionExpressionPrinter(out), null);
    }

    public static void print(ExpressionNode node, OutputStream out, Charset cs) {
        node.accept(new ActionExpressionPrinter(out, cs), null);
    }

    public ActionExpressionPrinter(Writer out) {
        super(out);
    }

    public ActionExpressionPrinter(OutputStream out) {
        super(out);
    }

    public ActionExpressionPrinter(OutputStream out, Charset cs) {
        super(out, cs);
    }

    @Override
    public Void visitNetworkComponent(NetworkComponentNode node, Void arg) {
        switch (node.getComponentType()) {
            case BRANCH:
                out.write("branch");
                break;
            case LINE:
                out.write("line");
                break;
            case TRANSFORMER:
                out.write("transformer");
                break;
            case GENERATOR:
                out.write("generator");
                break;
            case LOAD:
                out.write("load");
                break;
            case SWITCH:
                out.write("switch_");
                break;
            default:
                throw new IllegalStateException();
        }
        out.write("('");
        out.write(node.getComponentId());
        out.write("')");
        out.flush();
        return null;
    }

    @Override
    public Void visitNetworkProperty(NetworkPropertyNode node, Void arg) {
        node.getParent().accept(this, arg);
        out.write(".");
        out.write(node.getPropertyName());
        out.flush();
        return null;
    }

    @Override
    public Void visitNetworkMethod(NetworkMethodNode node, Void arg) {
        node.getParent().accept(this, arg);
        out.write(".");
        out.write(node.getMethodName());
        out.write("(");
        for (int i = 0; i < node.getArgs().length; i++) {
            Object arg2 = node.getArgs()[i];
            out.write(arg2.toString());
            if (i < node.getArgs().length - 1) {
                out.write(", ");
            }
        }
        out.write(")");
        out.flush();
        return null;
    }

    @Override
    public Void visitActionTaken(ActionTakenNode node, Void arg) {
        out.write("actionTaken('");
        out.write(node.getActionId());
        out.write("')");
        out.flush();
        return null;
    }

    @Override
    public Void visitContingencyOccurred(ContingencyOccurredNode node, Void arg) {
        out.write("contingencyOccurred(");
        if (node.getContingencyId() != null) {
            out.write("'");
            out.write(node.getContingencyId());
            out.write("'");
        }
        out.write(")");
        out.flush();
        return null;
    }

    @Override
    public Void visitLoadingRank(LoadingRankNode node, Void arg) {
        out.write("loadingRank('");
        node.getBranchIdToRankNode().accept(this, arg);
        out.write("', [");
        Iterator<ExpressionNode> it = node.getBranchIds().iterator();
        while (it.hasNext()) {
            out.write("'");
            it.next().accept(this, arg);
            out.write("'");
            if (it.hasNext()) {
                out.write(", ");
            }
        }
        out.write("])");
        out.flush();
        return null;
    }

    @Override
    public Void visitMostLoaded(MostLoadedNode node, Void arg) {
        out.write("mostLoaded(");
        out.write("['");
        out.write(String.join("', '", node.getBranchIds()));
        out.write("'])");
        out.flush();
        return null;
    }

    @Override
    public Void visitIsOverloaded(IsOverloadedNode node, Void arg) {
        out.write("isOverloaded(");
        out.write("['");
        out.write(String.join("', '", node.getBranchIds()));
        out.write("'])");
        out.flush();
        return null;
    }

    @Override
    public Void visitAllOverloaded(AllOverloadedNode node, Void arg) {
        out.write("allOverloaded(");
        out.write("['");
        out.write(String.join("', '", node.getBranchIds()));
        out.write("'])");
        out.flush();
        return null;
    }

}
