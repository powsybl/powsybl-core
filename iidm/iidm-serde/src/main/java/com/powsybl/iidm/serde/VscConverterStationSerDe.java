/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.VscConverterStation;
import com.powsybl.iidm.network.VscConverterStationAdder;
import com.powsybl.iidm.serde.util.IidmSerDeUtil;

import java.util.Objects;

import static com.powsybl.iidm.serde.ConnectableSerDeUtil.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class VscConverterStationSerDe extends AbstractSimpleIdentifiableSerDe<VscConverterStation, VscConverterStationAdder, VoltageLevel> {

    static final VscConverterStationSerDe INSTANCE = new VscConverterStationSerDe();

    static final String ROOT_ELEMENT_NAME = "vscConverterStation";
    static final String ARRAY_ELEMENT_NAME = "vscConverterStations";

    private static final String REGULATING_TERMINAL = "regulatingTerminal";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected void writeRootElementAttributes(VscConverterStation cs, VoltageLevel vl, NetworkSerializerContext context) {
        context.getWriter().writeBooleanAttribute("voltageRegulatorOn", cs.isVoltageRegulatorOn());
        context.getWriter().writeFloatAttribute("lossFactor", cs.getLossFactor());
        context.getWriter().writeDoubleAttribute("voltageSetpoint", cs.getVoltageSetpoint());
        context.getWriter().writeDoubleAttribute("reactivePowerSetpoint", cs.getReactivePowerSetpoint());
        writeNodeOrBus(null, cs.getTerminal(), context);
        writePQ(null, cs.getTerminal(), context.getWriter());
    }

    @Override
    protected void writeSubElements(VscConverterStation cs, VoltageLevel vl, NetworkSerializerContext context) {
        ReactiveLimitsSerDe.INSTANCE.write(cs, context);
        IidmSerDeUtil.assertMinimumVersionAndRunIfNotDefault(!Objects.equals(cs, cs.getRegulatingTerminal().getConnectable()),
                ROOT_ELEMENT_NAME, REGULATING_TERMINAL, IidmSerDeUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED,
                IidmVersion.V_1_6, context, () -> TerminalRefSerDe.writeTerminalRef(cs.getRegulatingTerminal(), context, REGULATING_TERMINAL));
    }

    @Override
    protected VscConverterStationAdder createAdder(VoltageLevel vl) {
        return vl.newVscConverterStation();
    }

    @Override
    protected VscConverterStation readRootElementAttributes(VscConverterStationAdder adder, VoltageLevel voltageLevel, NetworkDeserializerContext context) {
        boolean voltageRegulatorOn = context.getReader().readBooleanAttribute("voltageRegulatorOn");
        float lossFactor = context.getReader().readFloatAttribute("lossFactor");
        double voltageSetpoint = context.getReader().readDoubleAttribute("voltageSetpoint");
        double reactivePowerSetpoint = context.getReader().readDoubleAttribute("reactivePowerSetpoint");
        readNodeOrBus(adder, context, voltageLevel.getTopologyKind());
        adder
                .setLossFactor(lossFactor)
                .setVoltageSetpoint(voltageSetpoint)
                .setReactivePowerSetpoint(reactivePowerSetpoint)
                .setVoltageRegulatorOn(voltageRegulatorOn);
        VscConverterStation cs = adder.add();
        readPQ(null, cs.getTerminal(), context.getReader());
        return cs;
    }

    @Override
    protected void readSubElements(VscConverterStation cs, NetworkDeserializerContext context) {
        context.getReader().readChildNodes(elementName -> {
            switch (elementName) {
                case ReactiveLimitsSerDe.ELEM_REACTIVE_CAPABILITY_CURVE -> ReactiveLimitsSerDe.INSTANCE.readReactiveCapabilityCurve(cs, context);
                case ReactiveLimitsSerDe.ELEM_MIN_MAX_REACTIVE_LIMITS -> ReactiveLimitsSerDe.INSTANCE.readMinMaxReactiveLimits(cs, context);
                case REGULATING_TERMINAL -> {
                    IidmSerDeUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, REGULATING_TERMINAL, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_6, context);
                    TerminalRefSerDe.readTerminalRef(context, cs.getNetwork(), cs::setRegulatingTerminal);
                }
                default -> readSubElement(elementName, cs, context);
            }
        });
    }
}
