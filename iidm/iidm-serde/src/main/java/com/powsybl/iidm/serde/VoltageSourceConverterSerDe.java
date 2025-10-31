/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.VoltageSourceConverter;
import com.powsybl.iidm.network.VoltageSourceConverterAdder;
import com.powsybl.iidm.network.VoltageLevel;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public class VoltageSourceConverterSerDe extends AbstractAcDcConverterSerDe<VoltageSourceConverter, VoltageSourceConverterAdder> {

    static final VoltageSourceConverterSerDe INSTANCE = new VoltageSourceConverterSerDe();
    static final String ROOT_ELEMENT_NAME = "voltageSourceConverter";
    static final String ARRAY_ELEMENT_NAME = "voltageSourceConverters";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected void writeRootElementAttributes(final VoltageSourceConverter vsc, final VoltageLevel parent, final NetworkSerializerContext context) {
        super.writeRootElementAttributes(vsc, parent, context);
        context.getWriter().writeBooleanAttribute("voltageRegulatorOn", vsc.isVoltageRegulatorOn());
        context.getWriter().writeDoubleAttribute("voltageSetpoint", vsc.getVoltageSetpoint());
        context.getWriter().writeDoubleAttribute("reactivePowerSetpoint", vsc.getReactivePowerSetpoint());
    }

    @Override
    protected void writeSubElements(VoltageSourceConverter vsc, VoltageLevel parent, NetworkSerializerContext context) {
        ReactiveLimitsSerDe.INSTANCE.write(vsc, context);
    }

    @Override
    protected VoltageSourceConverterAdder createAdder(final VoltageLevel voltageLevel) {
        return voltageLevel.newVoltageSourceConverter();
    }

    @Override
    protected VoltageSourceConverter readRootElementAttributes(final VoltageSourceConverterAdder adder, final VoltageLevel parent, final NetworkDeserializerContext context) {
        super.readRootElementCommonAttributes(adder, parent, context);
        boolean voltageRegulatorOn = context.getReader().readBooleanAttribute("voltageRegulatorOn");
        double voltageSetpoint = context.getReader().readDoubleAttribute("voltageSetpoint");
        double reactivePowerSetpoint = context.getReader().readDoubleAttribute("reactivePowerSetpoint");
        return adder
                .setReactivePowerSetpoint(reactivePowerSetpoint)
                .setVoltageSetpoint(voltageSetpoint)
                .setVoltageRegulatorOn(voltageRegulatorOn)
                .add();
    }

    @Override
    protected void readSubElements(VoltageSourceConverter vsc, NetworkDeserializerContext context) {
        context.getReader().readChildNodes(elementName -> {
            switch (elementName) {
                case ReactiveLimitsSerDe.ELEM_REACTIVE_CAPABILITY_CURVE -> ReactiveLimitsSerDe.INSTANCE.readReactiveCapabilityCurve(vsc, context);
                case ReactiveLimitsSerDe.ELEM_MIN_MAX_REACTIVE_LIMITS -> ReactiveLimitsSerDe.INSTANCE.readMinMaxReactiveLimits(vsc, context);
                default -> readSubElement(elementName, vsc, context);
            }
        });
    }
}
