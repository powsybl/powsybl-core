/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.iidm.network.LccConverterStation;
import com.powsybl.iidm.network.LccConverterStationAdder;
import com.powsybl.iidm.network.VoltageLevel;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class LccConverterStationXml extends AbstractConnectableXml<LccConverterStation, LccConverterStationAdder, VoltageLevel> {

    static final LccConverterStationXml INSTANCE = new LccConverterStationXml();

    static final String ROOT_ELEMENT_NAME = "lccConverterStation";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected void writeRootElementAttributes(LccConverterStation cs, VoltageLevel vl, NetworkXmlWriterContext context) {
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
    protected LccConverterStation readRootElementAttributes(LccConverterStationAdder adder, NetworkXmlReaderContext context) {
        float lossFactor = context.getReader().readFloatAttribute("lossFactor");
        float powerFactor = context.getReader().readFloatAttribute("powerFactor");
        readNodeOrBus(adder, context);
        LccConverterStation cs = adder
                .setLossFactor(lossFactor)
                .setPowerFactor(powerFactor)
                .add();
        readPQ(null, cs.getTerminal(), context.getReader());
        return cs;
    }

    @Override
    protected void readSubElements(LccConverterStation cs, NetworkXmlReaderContext context) {
        context.getReader().readUntilEndNode(getRootElementName(), () -> LccConverterStationXml.super.readSubElements(cs, context));
    }
}
