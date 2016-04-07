/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.rules.expr;

import eu.itesla_project.modules.histo.HistoDbAttributeId;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class Attribute implements ExpressionNode {

    private final HistoDbAttributeId id;

    public Attribute(HistoDbAttributeId id) {
        this.id = id;
    }

    public HistoDbAttributeId getId() {
        return id;
    }

    @Override
    public <R, A> R accept(ExpressionVisitor<R, A> visitor, A arg) {
        return visitor.visit(this, arg);
    }

    @Override
    public Attribute clone() {
        return new Attribute(id);
    }
}
