/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline.server.message;

import eu.itesla_project.modules.rules.SecurityRuleExpression;
import eu.itesla_project.modules.rules.expr.AndOperator;
import eu.itesla_project.modules.rules.expr.Attribute;
import eu.itesla_project.modules.rules.expr.ComparisonOperator;
import eu.itesla_project.modules.rules.expr.ExpressionVisitor;
import eu.itesla_project.modules.rules.expr.Litteral;
import eu.itesla_project.modules.rules.expr.OrOperator;
import java.util.Objects;
import javax.json.stream.JsonGenerator;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class StabilityMessage extends Message {

    private final SecurityRuleExpression securityRuleExpression;
    
    public StabilityMessage(SecurityRuleExpression securityRuleExpression) {
        this.securityRuleExpression = Objects.requireNonNull(securityRuleExpression);
    }

    @Override
    protected String getType() {
        return "stability";
    }

    @Override
    public void toJson(JsonGenerator generator) {
        if (securityRuleExpression.getStatus() != null) {
            generator.write("label", securityRuleExpression.getStatus().name());
            if (securityRuleExpression.getCondition() != null) {
                generator.writeStartArray("children");
                securityRuleExpression.getCondition().accept(new ExpressionVisitor<Void, JsonGenerator>() {

                    @Override
                    public Void visit(Attribute node, JsonGenerator generator) {
                        generator.writeStartObject();
                        generator.write("type", node.getClass().getName());
                        generator.write("label", node.getId().toString());
                        generator.writeEnd();
                        return null;
                    }

                    @Override
                    public Void visit(Litteral node, JsonGenerator generator) {
                        generator.writeStartObject();
                        generator.write("type", node.getClass().getName());
                        generator.write("label", node.getValue());
                        generator.writeEnd();
                        return null;
                    }

                    @Override
                    public Void visit(ComparisonOperator node, JsonGenerator generator) {
                        generator.writeStartObject();
                        generator.write("type", node.getClass().getName());
                        generator.write("label", node.getType().name());
                        generator.writeStartArray("children");
                        node.getNode1().accept(this, generator);
                        node.getNode2().accept(this, generator);
                        generator.writeEnd();
                        generator.writeEnd();
                        return null;
                    }

                    @Override
                    public Void visit(AndOperator node, JsonGenerator generator) {
                        generator.writeStartObject();
                        generator.write("type", node.getClass().getName());
                        generator.write("label", "And");
                        generator.writeStartArray("children");
                        node.getNode1().accept(this, generator);
                        node.getNode2().accept(this, generator);
                        generator.writeEnd();
                        generator.writeEnd();
                        return null;
                    }

                    @Override
                    public Void visit(OrOperator node, JsonGenerator generator) {
                        generator.writeStartObject();
                        generator.write("type", node.getClass().getName());
                        generator.write("label", "Or");
                        generator.writeStartArray("children");
                        node.getNode1().accept(this, generator);
                        node.getNode2().accept(this, generator);
                        generator.writeEnd();
                        generator.writeEnd();
                        return null;
                    }
                }, generator);
                generator.writeEnd();
            }
        }
    }

}
