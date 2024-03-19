/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.ial.dsl.ast;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class NetworkComponentNode extends AbstractActionExpressionNode implements NetworkNode {

    public enum ComponentType {
        LINE,
        BRANCH,
        TRANSFORMER,
        GENERATOR,
        LOAD,
        SWITCH
    }

    private final String componentId;

    private final ComponentType componentType;

    public NetworkComponentNode(String componentId, ComponentType componentType) {
        this.componentId = Objects.requireNonNull(componentId);
        this.componentType = Objects.requireNonNull(componentType);
    }

    public String getComponentId() {
        return componentId;
    }

    public ComponentType getComponentType() {
        return componentType;
    }

    @Override
    public <R, A> R accept(ActionExpressionVisitor<R, A> visitor, A arg) {
        return visitor.visitNetworkComponent(this, arg);
    }
}
