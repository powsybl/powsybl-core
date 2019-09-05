/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.ShuntCompensatorAdder;
import com.powsybl.iidm.network.ShuntCompensatorLinearModel;
import com.powsybl.iidm.network.VoltageLevel;

import javax.xml.stream.XMLStreamException;

import static com.powsybl.iidm.network.ShuntCompensatorModelType.NON_LINEAR;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class ShuntXml extends AbstractConnectableXml<ShuntCompensator, ShuntCompensatorAdder, VoltageLevel> {

    static final ShuntXml INSTANCE = new ShuntXml();

    static final String ROOT_ELEMENT_NAME = "shunt";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected boolean hasSubElements(ShuntCompensator sc) {
        return false;
    }

    @Override
    protected void writeRootElementAttributes(ShuntCompensator sc, VoltageLevel vl, NetworkXmlWriterContext context) throws XMLStreamException {
        if (sc.getModelType() == NON_LINEAR) {
            throw new PowsyblException(sc.getId() + " :non linear shunt compensators are not yet supported"); // TODO: support non linear shunt in XIIDM export
        }
        XmlUtil.writeDouble("bPerSection", sc.getModel(ShuntCompensatorLinearModel.class).getbPerSection(), context.getWriter());
        context.getWriter().writeAttribute("maximumSectionCount", Integer.toString(sc.getModel(ShuntCompensatorLinearModel.class).getMaximumSectionCount()));
        context.getWriter().writeAttribute("currentSectionCount", Integer.toString(sc.getCurrentSectionCount()));
        writeNodeOrBus(null, sc.getTerminal(), context);
        writePQ(null, sc.getTerminal(), context.getWriter());
    }

    @Override
    protected ShuntCompensatorAdder createAdder(VoltageLevel vl) {
        return vl.newShuntCompensator();
    }

    @Override
    protected ShuntCompensator readRootElementAttributes(ShuntCompensatorAdder adder, NetworkXmlReaderContext context) {
        double bPerSection = XmlUtil.readDoubleAttribute(context.getReader(), "bPerSection");
        int maximumSectionCount = XmlUtil.readIntAttribute(context.getReader(), "maximumSectionCount");
        int currentSectionCount = XmlUtil.readIntAttribute(context.getReader(), "currentSectionCount");
        adder.newLinearModel() // TODO: support non linear shunt in XIIDM import
                    .setbPerSection(bPerSection)
                    .setMaximumSectionCount(maximumSectionCount)
                .add()
                .setCurrentSectionCount(currentSectionCount);
        readNodeOrBus(adder, context);
        ShuntCompensator sc = adder.add();
        readPQ(null, sc.getTerminal(), context.getReader());
        return sc;
    }

    @Override
    protected void readSubElements(ShuntCompensator sc, NetworkXmlReaderContext context) throws XMLStreamException {
        readUntilEndRootElement(context.getReader(), () -> ShuntXml.super.readSubElements(sc, context));
    }
}
