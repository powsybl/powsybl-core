/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.rules.expr;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ExpressionFlatPrinter implements ExpressionVisitor<Void, Void> {

    private final Writer writer;

    public ExpressionFlatPrinter(Writer writer) {
        this.writer = writer;
    }

    public static void print(ExpressionNode node, Writer writer) {
        node.accept(new ExpressionFlatPrinter(writer), null);
    }

    public static String toString(ExpressionNode node) throws IOException {
        try (Writer writer = new StringWriter()) {
            if (node != null) {
                node.accept(new ExpressionFlatPrinter(writer), null);
            }
            return writer.toString();
        }
    }

    @Override
    public Void visit(Attribute node, Void arg) {
        try {
            writer.write(node.getId().toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public Void visit(Litteral node, Void arg) {
        try {
            writer.write(Double.toString(node.getValue()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public Void visit(ComparisonOperator node, Void arg) {
        try {
            writer.write("(");
            node.getNode1().accept(this, arg);
            writer.write(" ");
            switch (node.getType()) {
                case LESS:
                    writer.write("<");
                    break;

                case GREATER_EQUAL:
                    writer.write(">=");
                    break;

                default:
                    throw new InternalError();
            }
            writer.write(" ");
            node.getNode2().accept(this, arg);
            writer.write(")");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public Void visit(AndOperator node, Void arg) {
        try {
            writer.write("(");
            node.getNode1().accept(this, arg);
            writer.write(" AND ");
            node.getNode2().accept(this, arg);
            writer.write(")");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public Void visit(OrOperator node, Void arg) {
        try {
            writer.write("(");
            node.getNode1().accept(this, arg);
            writer.write(" OR ");
            node.getNode2().accept(this, arg);
            writer.write(")");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

}
