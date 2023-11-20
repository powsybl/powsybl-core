/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.iidm.network.EnergySource;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.GeneratorAdder;
import com.powsybl.iidm.network.VoltageLevel;

import static com.powsybl.iidm.xml.ConnectableXmlUtil.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class GeneratorXml extends AbstractSimpleIdentifiableXml<Generator, GeneratorAdder, VoltageLevel> {

    static final GeneratorXml INSTANCE = new GeneratorXml();

    static final String ROOT_ELEMENT_NAME = "generator";
    static final String ARRAY_ELEMENT_NAME = "generators";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected void writeRootElementAttributes(Generator g, VoltageLevel vl, NetworkXmlWriterContext context) {
        context.getWriter().writeEnumAttribute("energySource", g.getEnergySource());
        context.getWriter().writeDoubleAttribute("minP", g.getMinP());
        context.getWriter().writeDoubleAttribute("maxP", g.getMaxP());
        context.getWriter().writeDoubleAttribute("ratedS", g.getRatedS());
        context.getWriter().writeBooleanAttribute("voltageRegulatorOn", g.isVoltageRegulatorOn());
        context.getWriter().writeDoubleAttribute("targetP", g.getTargetP());
        context.getWriter().writeDoubleAttribute("targetV", g.getTargetV());
        context.getWriter().writeDoubleAttribute("targetQ", g.getTargetQ());
        writeNodeOrBus(null, g.getTerminal(), context);
        writePQ(null, g.getTerminal(), context.getWriter());
    }

    @Override
    protected void writeSubElements(Generator g, VoltageLevel vl, NetworkXmlWriterContext context) {
        if (g != g.getRegulatingTerminal().getConnectable()) {
            TerminalRefXml.writeTerminalRef(g.getRegulatingTerminal(), context, "regulatingTerminal");
        }
        ReactiveLimitsXml.INSTANCE.write(g, context);
    }

    @Override
    protected GeneratorAdder createAdder(VoltageLevel vl) {
        return vl.newGenerator();
    }

    @Override
    protected Generator readRootElementAttributes(GeneratorAdder adder, VoltageLevel voltageLevel, NetworkXmlReaderContext context) {
        EnergySource energySource = context.getReader().readEnumAttribute("energySource", EnergySource.class);
        double minP = context.getReader().readDoubleAttribute("minP");
        double maxP = context.getReader().readDoubleAttribute("maxP");
        double ratedS = context.getReader().readDoubleAttribute("ratedS");
        Boolean voltageRegulatorOn = context.getReader().readBooleanAttribute("voltageRegulatorOn");
        double targetP = context.getReader().readDoubleAttribute("targetP");
        double targetV = context.getReader().readDoubleAttribute("targetV");
        double targetQ = context.getReader().readDoubleAttribute("targetQ");
        readNodeOrBus(adder, context);
        adder.setEnergySource(energySource)
                .setMinP(minP)
                .setMaxP(maxP)
                .setRatedS(ratedS)
                .setTargetP(targetP)
                .setTargetV(targetV)
                .setTargetQ(targetQ)
                .setVoltageRegulatorOn(voltageRegulatorOn);
        Generator g = adder.add();
        readPQ(null, g.getTerminal(), context.getReader());
        return g;
    }

    @Override
    protected void readSubElements(Generator g, NetworkXmlReaderContext context) {
        context.getReader().readChildNodes(elementName -> {
            switch (elementName) {
                case "regulatingTerminal" -> TerminalRefXml.readTerminalRef(context, g.getNetwork(), g::setRegulatingTerminal);
                case ReactiveLimitsXml.ELEM_REACTIVE_CAPABILITY_CURVE -> ReactiveLimitsXml.INSTANCE.readReactiveCapabilityCurve(g, context);
                case ReactiveLimitsXml.ELEM_MIN_MAX_REACTIVE_LIMITS -> ReactiveLimitsXml.INSTANCE.readMinMaxReactiveLimits(g, context);
                default -> readSubElement(elementName, g, context);
            }
        });
    }
}
