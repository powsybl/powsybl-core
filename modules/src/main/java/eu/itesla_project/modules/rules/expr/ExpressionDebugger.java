/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.rules.expr;

import eu.itesla_project.modules.histo.HistoDbAttributeId;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ExpressionDebugger extends ExpressionTreePrinter {

    public ExpressionDebugger(final Map<HistoDbAttributeId, Object> attrs, Writer writer) {
        super(new ExpressionTreeDecorator() {

            @Override
            public void decorateAttribute(Attribute node, Writer writer) {
                try {
                    writer.write("(");
                    Object val = attrs.get(node.getId());
                    writer.write(val != null ? val.toString() : "ATTRIBUTE NOT FOUND!!!");
                    writer.write(")");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            private void decorateOperator(ExpressionNode node, Writer writer) {
                try {
                    writer.write("(");
                    writer.write(Boolean.toString(ExpressionEvaluator.eval(node, attrs, null)));
                    writer.write(")");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void decorateComparisonOperator(ComparisonOperator node, Writer writer) {
                decorateOperator(node, writer);
            }

            @Override
            public void decorateAndOperator(AndOperator node, Writer writer) {
                decorateOperator(node, writer);
            }

            @Override
            public void decorateOrOperator(OrOperator node, Writer writer) {
                decorateOperator(node, writer);
            }

        }, writer);
    }

    public static String toString(ExpressionNode node, Map<HistoDbAttributeId, Object> attrs) throws IOException {
        try (Writer writer = new StringWriter()) {
            if (node != null) {
                node.accept(new ExpressionDebugger(attrs, writer), 0);
            }
            return writer.toString();
        }
    }
}
