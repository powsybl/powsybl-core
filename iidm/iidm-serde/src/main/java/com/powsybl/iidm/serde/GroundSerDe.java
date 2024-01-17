/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.Ground;
import com.powsybl.iidm.network.GroundAdder;
import com.powsybl.iidm.network.VoltageLevel;

import static com.powsybl.iidm.serde.ConnectableSerDeUtil.readNodeOrBus;
import static com.powsybl.iidm.serde.ConnectableSerDeUtil.writeNodeOrBus;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class GroundSerDe extends AbstractSimpleIdentifiableSerDe<Ground, GroundAdder, VoltageLevel> {

    static final GroundSerDe INSTANCE = new GroundSerDe();

    static final String ROOT_ELEMENT_NAME = "ground";
    static final String ARRAY_ELEMENT_NAME = "grounds";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected void writeRootElementAttributes(Ground ground, VoltageLevel vl, NetworkSerializerContext context) {
        writeNodeOrBus(null, ground.getTerminal(), context);
    }

    @Override
    protected GroundAdder createAdder(VoltageLevel voltageLevel) {
        return voltageLevel.newGround();
    }

    @Override
    protected Ground readRootElementAttributes(GroundAdder adder, VoltageLevel parent, NetworkDeserializerContext context) {
        readNodeOrBus(adder, context, parent.getTopologyKind());
        return adder.add();
    }

    @Override
    protected void readSubElements(Ground ground, NetworkDeserializerContext context) {
        context.getReader().readChildNodes(elementName -> readSubElement(elementName, ground, context));
    }
}
