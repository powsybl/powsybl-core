/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.VscConverterStation;
import com.powsybl.iidm.network.VscConverterStationAdder;

import javax.xml.stream.XMLStreamException;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
class VscConverterStationXml extends AbstractConnectableXml<VscConverterStation, VscConverterStationAdder, VoltageLevel> {

    static final VscConverterStationXml INSTANCE = new VscConverterStationXml();

    static final String ROOT_ELEMENT_NAME = "vscConverterStation";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected boolean hasSubElements(VscConverterStation cs) {
        return true;
    }

    @Override
    protected void writeRootElementAttributes(VscConverterStation cs, VoltageLevel vl, NetworkXmlWriterContext context) throws XMLStreamException {
        context.getWriter().writeAttribute("voltageRegulatorOn", Boolean.toString(cs.isVoltageRegulatorOn()));
        XmlUtil.writeFloat("lossFactor", cs.getLossFactor(), context.getWriter());
        XmlUtil.writeDouble("voltageSetpoint", cs.getVoltageSetpoint(), context.getWriter());
        XmlUtil.writeDouble("reactivePowerSetpoint", cs.getReactivePowerSetpoint(), context.getWriter());
        writeNodeOrBus(null, cs.getTerminal(), context);
        writePQ(null, cs.getTerminal(), context.getWriter());
    }

    @Override
    protected void writeSubElements(VscConverterStation cs, VoltageLevel vl, NetworkXmlWriterContext context) throws XMLStreamException {
        ReactiveLimitsXml.INSTANCE.write(cs, context);
    }

    @Override
    protected VscConverterStationAdder createAdder(VoltageLevel vl) {
        return vl.newVscConverterStation();
    }

    @Override
    protected VscConverterStation readRootElementAttributes(VscConverterStationAdder adder, NetworkXmlReaderContext context) {
        boolean voltageRegulatorOn = XmlUtil.readBoolAttribute(context.getReader(), "voltageRegulatorOn");
        float lossFactor = XmlUtil.readFloatAttribute(context.getReader(), "lossFactor");
        double voltageSetpoint = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "voltageSetpoint");
        double reactivePowerSetpoint = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "reactivePowerSetpoint");
        readNodeOrBus(adder, context);
        VscConverterStation cs = adder
                .setLossFactor(lossFactor)
                .setVoltageRegulatorOn(voltageRegulatorOn)
                .setVoltageSetpoint(voltageSetpoint)
                .setReactivePowerSetpoint(reactivePowerSetpoint)
                .add();
        readPQ(null, cs.getTerminal(), context.getReader());
        return cs;
    }

    @Override
    protected void readSubElements(VscConverterStation cs, NetworkXmlReaderContext context) throws XMLStreamException {
        readUntilEndRootElement(context.getReader(), () -> {
            switch (context.getReader().getLocalName()) {
                case "reactiveCapabilityCurve":
                case "minMaxReactiveLimits":
                    ReactiveLimitsXml.INSTANCE.read(cs, context);
                    break;

                default:
                    super.readSubElements(cs, context);
            }
        });
    }
}
