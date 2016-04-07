/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.rules.expr;

import eu.itesla_project.modules.histo.HistoDbAttributeId;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ExpressionEvaluator implements ExpressionVisitor<Object, Void> {

    private final Map<HistoDbAttributeId, Object> attributeValues;

    private final List<HistoDbAttributeId> missingAttributes;

    public ExpressionEvaluator(Map<HistoDbAttributeId, Object> attributeValues, List<HistoDbAttributeId> missingAttributes) {
        this.attributeValues = attributeValues;
        this.missingAttributes = missingAttributes;
    }

    public static boolean eval(ExpressionNode node, Map<HistoDbAttributeId, Object> attributesValues, List<HistoDbAttributeId> missingAttributes) {
        Objects.requireNonNull(node);
        Objects.requireNonNull(attributesValues);
        return (Boolean) node.accept(new ExpressionEvaluator(attributesValues, missingAttributes), null);
    }

    @Override
    public Object visit(Attribute node, Void arg) {
        Object value = attributeValues.get(node.getId());
        if (value == null) {
            if (missingAttributes != null) {
                missingAttributes.add(node.getId());
            }
            return Double.NaN;
        }
        if (value instanceof Double) {
            return value;
        } else if (value instanceof Float) {
            return ((Float) value).doubleValue();
        } else {
            throw new RuntimeException("Attribute " + node.getId() + " is not a float or a double");
        }
    }

    @Override
    public Object visit(Litteral node, Void arg) {
        return node.getValue();
    }

    @Override
    public Object visit(ComparisonOperator node, Void arg) {
        double value1 = (Double) node.getNode1().accept(this, arg);
        double value2 = (Double) node.getNode2().accept(this, arg);
        // if value is undefined (no calculation?), we consider ok
        if (Double.isNaN(value1) || Double.isNaN(value2)) {
            return Boolean.TRUE;
        }
        switch (node.getType()) {
            case LESS:
                return value1 < value2;

            case GREATER_EQUAL:
                return value1 >= value2;

            default:
                throw new InternalError();
        }
    }

    @Override
    public Object visit(AndOperator node, Void arg) {
        return (Boolean) node.getNode1().accept(this, arg) && (Boolean) node.getNode2().accept(this, arg);
    }

    @Override
    public Object visit(OrOperator node, Void arg) {
        return (Boolean) node.getNode1().accept(this, arg) || (Boolean) node.getNode2().accept(this, arg);
    }

}
