/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.xml.util.IidmXmlUtil;

import javax.xml.stream.XMLStreamException;

/**
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
        if (ShuntCompensatorModelType.NON_LINEAR.equals(sc.getModelType())) {
            throw new PowsyblException("Non linear shunts are not supported for IIDM-XML version " + context.getVersion().toString(".")
                    + ". IIDM-XML version should be >= 1.2");
        }
        IidmXmlUtil.assertMinimumVersionIfNotDefault(sc.isVoltageRegulatorOn(), ROOT_ELEMENT_NAME, "voltageRegulatorOn",
                IidmXmlUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED, IidmXmlVersion.V_1_2, context);
        IidmXmlUtil.assertMinimumVersionIfNotDefault(!Double.isNaN(sc.getTargetV()), ROOT_ELEMENT_NAME, "targetV",
                IidmXmlUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED, IidmXmlVersion.V_1_2, context);
        IidmXmlUtil.assertMinimumVersionIfNotDefault(!Double.isNaN(sc.getTargetDeadband()), ROOT_ELEMENT_NAME, "targetDeadband",
                IidmXmlUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED, IidmXmlVersion.V_1_2, context);
        IidmXmlUtil.assertMinimumVersionIfNotDefault(sc.getRegulatingTerminal().getConnectable() != sc, ROOT_ELEMENT_NAME, "regulatingTerminal",
                IidmXmlUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED, IidmXmlVersion.V_1_2, context);
        XmlUtil.writeDouble("bPerSection", sc.getModel(ShuntCompensatorLinearModel.class).getbPerSection(), context.getWriter());
        context.getWriter().writeAttribute("maximumSectionCount", Integer.toString(sc.getMaximumSectionCount()));
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
        IidmXmlUtil.assertMaximumVersion(ROOT_ELEMENT_NAME, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_1, context);
        double bPerSection = XmlUtil.readDoubleAttribute(context.getReader(), "bPerSection");
        int maximumSectionCount = XmlUtil.readIntAttribute(context.getReader(), "maximumSectionCount");
        int currentSectionCount = XmlUtil.readIntAttribute(context.getReader(), "currentSectionCount");
        adder.setCurrentSectionCount(currentSectionCount)
                .newLinearModel()
                .setMaximumSectionCount(maximumSectionCount)
                .setbPerSection(bPerSection)
                .add();
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
