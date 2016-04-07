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
public class ExpressionTreePrinter implements ExpressionVisitor<Void, Integer> {

    private static final int INDENT_INC = 4;

    public static interface ExpressionTreeDecorator {

        void decorateAttribute(Attribute node, Writer writer);

        void decorateComparisonOperator(ComparisonOperator node, Writer writer);

        void decorateAndOperator(AndOperator node, Writer writer);

        void decorateOrOperator(OrOperator node, Writer writer);

    }

    private final Writer writer;

    private final ExpressionTreeDecorator decorator;

    public ExpressionTreePrinter(ExpressionTreeDecorator decorator, Writer writer) {
        this.decorator = decorator;
        this.writer = writer;
    }

    public static void print(ExpressionNode node, Writer writer) {
        node.accept(new ExpressionTreePrinter(null, writer), 0);
    }

    public static String toString(ExpressionNode node) throws IOException {
        try (Writer writer = new StringWriter()) {
            if (node != null) {
                node.accept(new ExpressionTreePrinter(null, writer), 0);
            }
            return writer.toString();
        }
    }

    private void writeIndent(int indent) throws IOException {
        if (indent > 0) {
            for (int i = 0; i < indent; i++) {
                if (i % INDENT_INC == 0) {
                    writer.write("|");
                } else {
                    writer.write(" ");
                }
            }
        }
    }

    @Override
    public Void visit(Attribute node, Integer indent) {
        try {
            writeIndent(indent);
            writer.write(node.getId().toString());
            writer.write(" ");
            if (decorator != null) {
                decorator.decorateAttribute(node, writer);
            }
            writer.write("\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public Void visit(Litteral node, Integer indent) {
        try {
            writeIndent(indent);
            writer.write(Double.toString(node.getValue()));
            writer.write("\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public Void visit(ComparisonOperator node, Integer indent) {
        try {
            writeIndent(indent);
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
            if (decorator != null) {
                decorator.decorateComparisonOperator(node, writer);
            }
            writer.write("\n");
            node.getNode1().accept(this, indent + INDENT_INC);
            node.getNode2().accept(this, indent + INDENT_INC);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public Void visit(AndOperator node, Integer indent) {
        try {
            writeIndent(indent);
            writer.write("AND ");
            if (decorator != null) {
                decorator.decorateAndOperator(node, writer);
            }
            writer.write("\n");
            node.getNode1().accept(this, indent + INDENT_INC);
            node.getNode2().accept(this, indent + INDENT_INC);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public Void visit(OrOperator node, Integer indent) {
        try {
            writeIndent(indent);
            writer.write("OR ");
            if (decorator != null) {
                decorator.decorateOrOperator(node, writer);
            }
            writer.write("\n");
            node.getNode1().accept(this, indent + INDENT_INC);
            node.getNode2().accept(this, indent + INDENT_INC);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

}
