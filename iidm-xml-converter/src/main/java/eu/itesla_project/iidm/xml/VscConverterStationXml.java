/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.xml;

import eu.itesla_project.iidm.network.VoltageLevel;
import eu.itesla_project.iidm.network.VscConverterStation;
import eu.itesla_project.iidm.network.VscConverterStationAdder;

import javax.xml.stream.XMLStreamException;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
class VscConverterStationXml extends ConnectableXml<VscConverterStation, VscConverterStationAdder, VoltageLevel> {

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
    protected void writeRootElementAttributes(VscConverterStation cs, VoltageLevel vl, XmlWriterContext context) throws XMLStreamException {
        context.getWriter().writeAttribute("voltageRegulatorOn", Boolean.toString(cs.isVoltageRegulatorOn()));
        XmlUtil.writeFloat("voltageSetPoint", cs.getVoltageSetPoint(), context.getWriter());
        XmlUtil.writeFloat("reactivePowerSetPoint", cs.getReactivePowerSetPoint(), context.getWriter());
        writeNodeOrBus(null, cs.getTerminal(), context);
        writePQ(null, cs.getTerminal(), context.getWriter());
    }

    @Override
    protected void writeSubElements(VscConverterStation cs, VoltageLevel vl, XmlWriterContext context) throws XMLStreamException {
        ReactiveLimitsXml.INSTANCE.write(cs, context);
    }

    @Override
    protected VscConverterStationAdder createAdder(VoltageLevel vl) {
        return vl.newVscConverterStation();
    }

    @Override
    protected VscConverterStation readRootElementAttributes(VscConverterStationAdder adder, XmlReaderContext context) {
        boolean voltageRegulatorOn = XmlUtil.readBoolAttribute(context.getReader(), "voltageRegulatorOn");
        float voltageSetPoint = XmlUtil.readOptionalFloatAttribute(context.getReader(), "voltageSetPoint");
        float reactivePowerSetPoint = XmlUtil.readOptionalFloatAttribute(context.getReader(), "reactivePowerSetPoint");
        readNodeOrBus(adder, context);
        VscConverterStation cs = adder
                .setVoltageRegulatorOn(voltageRegulatorOn)
                .setVoltageSetPoint(voltageSetPoint)
                .setReactivePowerSetPoint(reactivePowerSetPoint)
                .add();
        readPQ(null, cs.getTerminal(), context.getReader());
        return cs;
    }

    @Override
    protected void readSubElements(VscConverterStation cs, XmlReaderContext context) throws XMLStreamException {
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
