/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.serializer;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.BusbarSectionAdder;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.serializer.util.IidmXmlUtil;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class BusbarSectionXml extends AbstractSimpleIdentifiableXml<BusbarSection, BusbarSectionAdder, VoltageLevel> {

    static final BusbarSectionXml INSTANCE = new BusbarSectionXml();

    static final String ROOT_ELEMENT_NAME = "busbarSection";
    static final String ARRAY_ELEMENT_NAME = "busbarSections";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected void writeRootElementAttributes(BusbarSection bs, VoltageLevel vl, NetworkXmlWriterContext context) {
        context.getWriter().writeIntAttribute("node", bs.getTerminal().getNodeBreakerView().getNode());
        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_0, context, () -> {
            context.getWriter().writeDoubleAttribute("v", bs.getV());
            context.getWriter().writeDoubleAttribute("angle", bs.getAngle());
        });
    }

    @Override
    protected BusbarSectionAdder createAdder(VoltageLevel vl) {
        return vl.getNodeBreakerView().newBusbarSection();
    }

    @Override
    protected BusbarSection readRootElementAttributes(BusbarSectionAdder adder, VoltageLevel voltageLevel, NetworkXmlReaderContext context) {
        int node = context.getReader().readIntAttribute("node");
        BusbarSection bbs = adder.setNode(node)
                .add();

        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_0, context, () -> {
            double v = context.getReader().readDoubleAttribute("v");
            double angle = context.getReader().readDoubleAttribute("angle");
            context.getEndTasks().add(() -> {
                Bus b = bbs.getTerminal().getBusView().getBus();
                if (b != null) {
                    b.setV(v).setAngle(angle);
                }
            });
        });

        return bbs;
    }

    @Override
    protected void readSubElements(BusbarSection bs, NetworkXmlReaderContext context) {
        context.getReader().readChildNodes(e -> readSubElement(e, bs, context));
    }
}
