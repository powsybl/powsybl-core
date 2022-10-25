/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtensionXmlSerializer;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.extensions.StandbyAutomaton;
import com.powsybl.iidm.network.extensions.StandbyAutomatonAdder;

import javax.xml.stream.XMLStreamException;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class StandbyAutomatonXmlSerializer extends AbstractExtensionXmlSerializer<StaticVarCompensator, StandbyAutomaton> {

    // TODO make this serializer versionable to fix lowVoltageSetpoint/highVoltageSetpoint
    public StandbyAutomatonXmlSerializer() {
        super("standbyAutomaton", "network", StandbyAutomaton.class, false, "standbyAutomaton.xsd",
                "http://www.itesla_project.eu/schema/iidm/ext/standby_automaton/1_0", "sa");
    }

    @Override
    public void write(StandbyAutomaton standbyAutomaton, XmlWriterContext context) throws XMLStreamException {
        XmlUtil.writeDouble("b0", standbyAutomaton.getB0(), context.getWriter());
        context.getWriter().writeAttribute("standby", Boolean.toString(standbyAutomaton.isStandby()));
        XmlUtil.writeDouble("lowVoltageSetPoint", standbyAutomaton.getLowVoltageSetpoint(), context.getWriter());
        XmlUtil.writeDouble("highVoltageSetPoint", standbyAutomaton.getHighVoltageSetpoint(), context.getWriter());
        XmlUtil.writeDouble("lowVoltageThreshold", standbyAutomaton.getLowVoltageThreshold(), context.getWriter());
        XmlUtil.writeDouble("highVoltageThreshold", standbyAutomaton.getHighVoltageThreshold(), context.getWriter());
    }

    @Override
    public StandbyAutomaton read(StaticVarCompensator svc, XmlReaderContext context) {
        double b0 = XmlUtil.readDoubleAttribute(context.getReader(), "b0");
        boolean standby = XmlUtil.readBoolAttribute(context.getReader(), "standby");
        double lowVoltageSetpoint = XmlUtil.readDoubleAttribute(context.getReader(), "lowVoltageSetPoint");
        double highVoltageSetpoint = XmlUtil.readDoubleAttribute(context.getReader(), "highVoltageSetPoint");
        double lowVoltageThreshold = XmlUtil.readDoubleAttribute(context.getReader(), "lowVoltageThreshold");
        double highVoltageThreshold = XmlUtil.readDoubleAttribute(context.getReader(), "highVoltageThreshold");
        svc.newExtension(StandbyAutomatonAdder.class)
                .withB0(b0)
                .withStandbyStatus(standby)
                .withLowVoltageSetpoint(lowVoltageSetpoint)
                .withHighVoltageSetpoint(highVoltageSetpoint)
                .withLowVoltageThreshold(lowVoltageThreshold)
                .withHighVoltageThreshold(highVoltageThreshold)
                .add();
        return svc.getExtension(StandbyAutomaton.class);
    }
}
