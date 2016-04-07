/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.rules.expr;

import eu.itesla_project.modules.histo.HistoDbNetworkAttributeId;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ExpressionGraphvizPrinter {

    private static final int INDENT_INC = 4;

    enum GraphvizShape {
        diamond,
        box,
        oval
    }

    private class NodeVisitor implements ExpressionVisitor<Void, Void> {

        private void writeNode(ExpressionNode node, String str, GraphvizShape shape, String fillColor) {
            try {
                writeIndent(1);
                writer.write(Integer.toString(System.identityHashCode(node)));
                writer.write(" [label=\"");
                writer.write(str);
                writer.write("\" shape=");
                writer.write(shape.toString());
                writer.write(" style=filled");
                if (fillColor != null) {
                    writer.write(" fillcolor=");
                    writer.write(fillColor);
                }
                writer.write("]\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Void visit(Attribute node, Void arg) {
            HistoDbNetworkAttributeId attrId = (HistoDbNetworkAttributeId) node.getId();
            String fillColor = null;
            switch (attrId.getAttributeType()) {
                case P:
                    fillColor = "skyblue";
                    break;
                case Q:
                    fillColor = "palegreen";
                    break;
                case V:
                    fillColor = "orange";
                    break;
            }
            writeNode(node, attrId.toString(), GraphvizShape.oval, fillColor);
            return null;
        }

        @Override
        public Void visit(Litteral node, Void arg) {
            writeNode(node, Double.toString(node.getValue()), GraphvizShape.oval, null);
            return null;
        }

        @Override
        public Void visit(ComparisonOperator node, Void arg) {
            switch (node.getType()) {
                case LESS:
                    writeNode(node, "<", GraphvizShape.diamond, null);
                    break;

                case GREATER_EQUAL:
                    writeNode(node, ">=", GraphvizShape.diamond, null);
                    break;

                default:
                    throw new InternalError();
            }
            node.getNode1().accept(this, null);
            node.getNode2().accept(this, null);
            return null;
        }

        @Override
        public Void visit(AndOperator node, Void arg) {
            writeNode(node, "AND", GraphvizShape.diamond, null);
            node.getNode1().accept(this, null);
            node.getNode2().accept(this, null);
            return null;
        }

        @Override
        public Void visit(OrOperator node, Void arg) {
            writeNode(node, "OR", GraphvizShape.diamond, null);
            node.getNode1().accept(this, null);
            node.getNode2().accept(this, null);
            return null;
        }
    }

    private class EdgeVisitor extends AbstractExpressionVisitor<Void, Void> {

        private void writeEdge(ExpressionNode parent, ExpressionNode child1, ExpressionNode child2) {
            try {
                writeIndent(1);
                writer.write(Integer.toString(System.identityHashCode(parent)));
                writer.write(" -- ");
                writer.write(Integer.toString(System.identityHashCode(child1)));
                writer.write("\n");
                writeIndent(1);
                writer.write(Integer.toString(System.identityHashCode(parent)));
                writer.write(" -- ");
                writer.write(Integer.toString(System.identityHashCode(child2)));
                writer.write("\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Void visit(ComparisonOperator node, Void arg) {
            writeEdge(node, node.getNode1(), node.getNode2());
            return super.visit(node, arg);
        }

        @Override
        public Void visit(AndOperator node, Void arg) {
            writeEdge(node, node.getNode1(), node.getNode2());
            return super.visit(node, arg);
        }

        @Override
        public Void visit(OrOperator node, Void arg) {
            writeEdge(node, node.getNode1(), node.getNode2());
            return super.visit(node, arg);
        }
    }

    private final ExpressionNode node;

    private final Writer writer;

    public ExpressionGraphvizPrinter(ExpressionNode node, Writer writer) {
        this.node = node;
        this.writer = writer;
    }

    public void print() {
        try {
            writer.append("graph \"rule\" {\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        node.accept(new NodeVisitor(), null);
        node.accept(new EdgeVisitor(), null);
        try {
            writer.append("}\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void print(ExpressionNode node, Writer writer) {
        new ExpressionGraphvizPrinter(node, writer).print();
    }

    public static String toString(ExpressionNode node) throws IOException {
        try (Writer writer = new StringWriter()) {
            if (node != null) {
                print(node, writer);
            }
            return writer.toString();
        }
    }

    private void writeIndent(int indent) throws IOException {
        if (indent > 0) {
            for (int i = 0; i < indent * INDENT_INC; i++) {
                writer.write(" ");
            }
        }
    }

}
