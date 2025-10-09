/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.LccConverterStation;
import com.powsybl.iidm.network.LccConverterStationAdder;
import com.powsybl.iidm.network.VoltageLevel;

import static com.powsybl.iidm.serde.ConnectableSerDeUtil.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public class LccConverterStationSerDe extends AbstractSimpleIdentifiableSerDe<LccConverterStation, LccConverterStationAdder, VoltageLevel> {

    static final LccConverterStationSerDe INSTANCE = new LccConverterStationSerDe();

    static final String ROOT_ELEMENT_NAME = "lccConverterStation";
    static final String ARRAY_ELEMENT_NAME = "lccConverterStations";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected void writeRootElementAttributes(LccConverterStation cs, VoltageLevel vl, NetworkSerializerContext context) {
        context.getWriter().writeFloatAttribute("lossFactor", cs.getLossFactor());
        context.getWriter().writeFloatAttribute("powerFactor", cs.getPowerFactor());
        writeNodeOrBus(null, cs.getTerminal(), context);
        writePQ(null, cs.getTerminal(), context.getWriter());
    }

    @Override
    protected LccConverterStationAdder createAdder(VoltageLevel vl) {
        return vl.newLccConverterStation();
    }

    @Override
    protected LccConverterStation readRootElementAttributes(LccConverterStationAdder adder, VoltageLevel voltageLevel, NetworkDeserializerContext context) {
        float lossFactor = context.getReader().readFloatAttribute("lossFactor");
        float powerFactor = context.getReader().readFloatAttribute("powerFactor");
        readNodeOrBus(adder, context, voltageLevel.getTopologyKind());
        LccConverterStation cs = adder
                .setLossFactor(lossFactor)
                .setPowerFactor(powerFactor)
                .add();
        readPQ(null, cs.getTerminal(), context.getReader());
        return cs;
    }

    @Override
    protected void readSubElements(LccConverterStation cs, NetworkDeserializerContext context) {
        context.getReader().readChildNodes(e -> readSubElement(e, cs, context));
    }
}
