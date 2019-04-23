/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.StaticVarCompensatorAdder;
import com.powsybl.iidm.network.VoltageLevel;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class StaticVarCompensatorXml extends AbstractConnectableXml<StaticVarCompensator, StaticVarCompensatorAdder, VoltageLevel> {

    static final StaticVarCompensatorXml INSTANCE = new StaticVarCompensatorXml();

    static final String ROOT_ELEMENT_NAME = "staticVarCompensator";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected boolean hasSubElements(StaticVarCompensator svc) {
        return false;
    }

    @Override
    protected boolean hasControlValues(StaticVarCompensator svc) {
        return true;
    }

    @Override
    protected boolean hasStateValues(StaticVarCompensator svc) {
        return isTerminalHavingStateValues(svc.getTerminal());
    }

    @Override
    protected void writeRootElementAttributes(StaticVarCompensator svc, VoltageLevel vl, NetworkXmlWriterContext context) throws XMLStreamException {
        if (!context.getOptions().isIncrementalConversion()) {
            XmlUtil.writeDouble("bMin", svc.getBmin(), context.getWriter());
            XmlUtil.writeDouble("bMax", svc.getBmax(), context.getWriter());
        }
        if (!context.getOptions().isIncrementalConversion() || (context.getTargetFile() == IncrementalIidmFiles.CONTROL)) {
            XmlUtil.writeDouble("voltageSetPoint", svc.getVoltageSetPoint(), context.getWriter());
            XmlUtil.writeDouble("reactivePowerSetPoint", svc.getReactivePowerSetPoint(), context.getWriter());
            context.getWriter().writeAttribute("regulationMode", svc.getRegulationMode().name());
        }
        if (!context.getOptions().isIncrementalConversion() || (context.getTargetFile() == IncrementalIidmFiles.TOPO)) {
            writeNodeOrBus(null, svc.getTerminal(), context);
        }
        if (!context.getOptions().isIncrementalConversion() || context.getTargetFile() == IncrementalIidmFiles.STATE) {
            writePQ(null, svc.getTerminal(), context.getWriter());
        }
    }

    @Override
    protected StaticVarCompensatorAdder createAdder(VoltageLevel vl) {
        return vl.newStaticVarCompensator();
    }

    @Override
    protected StaticVarCompensator readRootElementAttributes(StaticVarCompensatorAdder adder, NetworkXmlReaderContext context) {
        double bMin = XmlUtil.readDoubleAttribute(context.getReader(), "bMin");
        double bMax = XmlUtil.readDoubleAttribute(context.getReader(), "bMax");
        double voltageSetPoint = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "voltageSetPoint");
        double reactivePowerSetPoint = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "reactivePowerSetPoint");
        StaticVarCompensator.RegulationMode regulationMode = StaticVarCompensator.RegulationMode.valueOf(context.getReader().getAttributeValue(null, "regulationMode"));
        adder.setBmin(bMin)
                .setBmax(bMax)
                .setVoltageSetPoint(voltageSetPoint)
                .setReactivePowerSetPoint(reactivePowerSetPoint)
                .setRegulationMode(regulationMode);
        readNodeOrBus(adder, context);
        StaticVarCompensator svc = adder.add();
        readPQ(null, svc.getTerminal(), context.getReader());
        return svc;
    }

    @Override
    protected void readSubElements(StaticVarCompensator svc, NetworkXmlReaderContext context) throws XMLStreamException {
        readUntilEndRootElement(context.getReader(), () -> super.readSubElements(svc, context));
    }

    static void updateStaticVarControlValues(XMLStreamReader reader, Network network, IncrementalIidmFiles targetFile) {
        if (targetFile != IncrementalIidmFiles.CONTROL) {
            return;
        }
        String id = reader.getAttributeValue(null, "id");
        double voltageSetPoint = XmlUtil.readOptionalDoubleAttribute(reader, "voltageSetPoint");
        double reactivePowerSetPoint = XmlUtil.readOptionalDoubleAttribute(reader, "reactivePowerSetPoint");
        String regulationMode = reader.getAttributeValue(null, "regulationMode");
        StaticVarCompensator svc = (StaticVarCompensator) network.getIdentifiable(id);
        svc.setReactivePowerSetPoint(reactivePowerSetPoint).setVoltageSetPoint(voltageSetPoint).setRegulationMode(StaticVarCompensator.RegulationMode.valueOf(regulationMode));
    }
}
