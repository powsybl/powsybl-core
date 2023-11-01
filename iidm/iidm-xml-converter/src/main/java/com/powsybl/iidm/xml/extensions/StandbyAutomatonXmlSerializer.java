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
import com.powsybl.commons.extensions.XmlReaderContext;
import com.powsybl.commons.extensions.XmlWriterContext;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.extensions.StandbyAutomaton;
import com.powsybl.iidm.network.extensions.StandbyAutomatonAdder;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
@AutoService(ExtensionXmlSerializer.class)
public class StandbyAutomatonXmlSerializer extends AbstractExtensionXmlSerializer<StaticVarCompensator, StandbyAutomaton> {

    // TODO make this serializer versionable to fix lowVoltageSetpoint/highVoltageSetpoint
    public StandbyAutomatonXmlSerializer() {
        super("standbyAutomaton", "network", StandbyAutomaton.class, "standbyAutomaton.xsd",
                "http://www.itesla_project.eu/schema/iidm/ext/standby_automaton/1_0", "sa");
    }

    @Override
    public void write(StandbyAutomaton standbyAutomaton, XmlWriterContext context) {
        context.getWriter().writeDoubleAttribute("b0", standbyAutomaton.getB0());
        context.getWriter().writeBooleanAttribute("standby", standbyAutomaton.isStandby());
        context.getWriter().writeDoubleAttribute("lowVoltageSetPoint", standbyAutomaton.getLowVoltageSetpoint());
        context.getWriter().writeDoubleAttribute("highVoltageSetPoint", standbyAutomaton.getHighVoltageSetpoint());
        context.getWriter().writeDoubleAttribute("lowVoltageThreshold", standbyAutomaton.getLowVoltageThreshold());
        context.getWriter().writeDoubleAttribute("highVoltageThreshold", standbyAutomaton.getHighVoltageThreshold());
    }

    @Override
    public StandbyAutomaton read(StaticVarCompensator svc, XmlReaderContext context) {
        double b0 = context.getReader().readDoubleAttribute("b0");
        boolean standby = context.getReader().readBooleanAttribute("standby");
        double lowVoltageSetpoint = context.getReader().readDoubleAttribute("lowVoltageSetPoint");
        double highVoltageSetpoint = context.getReader().readDoubleAttribute("highVoltageSetPoint");
        double lowVoltageThreshold = context.getReader().readDoubleAttribute("lowVoltageThreshold");
        double highVoltageThreshold = context.getReader().readDoubleAttribute("highVoltageThreshold");
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
