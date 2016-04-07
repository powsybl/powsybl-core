/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.rules.expr;

import eu.itesla_project.modules.histo.HistoDbAttributeId;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ExpressionAttributeList extends AbstractExpressionVisitor<Void, Void> {

    private final Set<HistoDbAttributeId> attributes;

    public ExpressionAttributeList(Set<HistoDbAttributeId> attributes) {
        this.attributes = attributes;
    }

    public static Set<HistoDbAttributeId> list(ExpressionNode node) {
        if (node == null) {
            return Collections.emptySet();
        } else {
            Set<HistoDbAttributeId> attributes = new LinkedHashSet<>();
            node.accept(new ExpressionAttributeList(attributes), null);
            return attributes;
        }
    }

    @Override
    public Void visit(Attribute attribute, Void arg) {
        attributes.add(attribute.getId());
        return null;
    }

}
