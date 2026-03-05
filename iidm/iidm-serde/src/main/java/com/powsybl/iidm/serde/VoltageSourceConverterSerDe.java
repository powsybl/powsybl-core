/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.serde.util.IidmSerDeUtil;

import java.util.concurrent.atomic.AtomicReference;

import static com.powsybl.iidm.serde.util.VoltageRegulationSerdeUtil.writeReactivePowerSetpointByVersion;
import static com.powsybl.iidm.serde.util.VoltageRegulationSerdeUtil.writeVoltageSetpointByVersion;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public class VoltageSourceConverterSerDe extends AbstractAcDcConverterSerDe<VoltageSourceConverter, VoltageSourceConverterAdder> {

    static final VoltageSourceConverterSerDe INSTANCE = new VoltageSourceConverterSerDe();
    static final String ROOT_ELEMENT_NAME = "voltageSourceConverter";
    static final String ARRAY_ELEMENT_NAME = "voltageSourceConverters";
    private static final String TARGET_V = "targetV";
    private static final String TARGET_Q = "targetQ";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected void writeRootElementAttributes(final VoltageSourceConverter vsc, final VoltageLevel parent, final NetworkSerializerContext context) {
        super.writeRootElementAttributes(vsc, parent, context);
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_15, context,
            () -> context.getWriter().writeBooleanAttribute("voltageRegulatorOn", vsc.isWithMode(RegulationMode.VOLTAGE)));
        writeVoltageSetpointByVersion(vsc, context);
        writeReactivePowerSetpointByVersion(vsc, context);
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_16, context, () -> context.getWriter().writeDoubleAttribute(TARGET_V, vsc.getTargetV()));
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_16, context, () -> context.getWriter().writeDoubleAttribute(TARGET_Q, vsc.getTargetQ()));
        super.writeRootElementPqiAttributes(vsc, context);
    }

    @Override
    protected void writeSubElements(VoltageSourceConverter vsc, VoltageLevel parent, NetworkSerializerContext context) {
        super.writeSubElements(vsc, parent, context);
        ReactiveLimitsSerDe.INSTANCE.write(vsc, context);
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_16, context,
            () -> VoltageRegulationSerDe.writeVoltageRegulation(vsc.getVoltageRegulation(), context, vsc));
    }

    @Override
    protected VoltageSourceConverterAdder createAdder(final VoltageLevel voltageLevel) {
        return voltageLevel.newVoltageSourceConverter();
    }

    @Override
    protected VoltageSourceConverter readRootElementAttributes(final VoltageSourceConverterAdder adder, final VoltageLevel parent, final NetworkDeserializerContext context) {
        super.readRootElementCommonAttributes(adder, parent, context);
        AtomicReference<Boolean> voltageRegulatorOnRef = new AtomicReference<>(null);
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_15, context, () -> voltageRegulatorOnRef.set(context.getReader().readBooleanAttribute("voltageRegulatorOn")));

        AtomicReference<Double> voltageSetpoint = new AtomicReference<>(Double.NaN);
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_15, context, () -> voltageSetpoint.set(context.getReader().readDoubleAttribute("voltageSetpoint")));

        AtomicReference<Double> reactivePowerSetpoint = new AtomicReference<>(Double.NaN);
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_15, context, () -> reactivePowerSetpoint.set(context.getReader().readDoubleAttribute("reactivePowerSetpoint")));

        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_16, context, () -> {
            adder.setTargetV(context.getReader().readDoubleAttribute(TARGET_V, Double.NaN));
            adder.setTargetQ(context.getReader().readDoubleAttribute(TARGET_Q, Double.NaN));
        });

        readVoltageRegulationPrevious116(adder, context, voltageRegulatorOnRef, voltageSetpoint, reactivePowerSetpoint);

        VoltageSourceConverter vsc = adder.add();
        super.readRootElementPqiAttributes(vsc, context);
        return vsc;
    }

    private static void readVoltageRegulationPrevious116(VoltageSourceConverterAdder adder, NetworkDeserializerContext context, AtomicReference<Boolean> voltageRegulatorOnRef, AtomicReference<Double> voltageSetpoint, AtomicReference<Double> reactivePowerSetpoint) {
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_15, context, () -> {
            Boolean voltageRegulatorOn = voltageRegulatorOnRef.get();
            RegulationMode regulationMode;
            if (voltageRegulatorOn == null) {
                if (!Double.isNaN(voltageSetpoint.get())) {
                    regulationMode = RegulationMode.VOLTAGE;
                } else if (!Double.isNaN(reactivePowerSetpoint.get())) {
                    regulationMode = RegulationMode.REACTIVE_POWER;
                } else {
                    regulationMode = RegulationMode.VOLTAGE;
                }
            } else {
                regulationMode = voltageRegulatorOn ? RegulationMode.VOLTAGE : RegulationMode.REACTIVE_POWER;
            }
            double targetValue;
            if (regulationMode == RegulationMode.REACTIVE_POWER) {
                targetValue = reactivePowerSetpoint.get();
                adder.setTargetV(voltageSetpoint.get());
            } else {
                targetValue = voltageSetpoint.get();
                adder.setTargetQ(reactivePowerSetpoint.get());
            }
            adder.newVoltageRegulation()
                .withTargetValue(targetValue)
                .withMode(regulationMode)
                .add();
        });
    }

    @Override
    protected void readSubElement(String elementName, VoltageSourceConverter vsc, NetworkDeserializerContext context) {
        switch (elementName) {
            case ReactiveLimitsSerDe.ELEM_REACTIVE_CAPABILITY_CURVE -> ReactiveLimitsSerDe.INSTANCE.readReactiveCapabilityCurve(vsc, context);
            case ReactiveLimitsSerDe.ELEM_MIN_MAX_REACTIVE_LIMITS -> ReactiveLimitsSerDe.INSTANCE.readMinMaxReactiveLimits(vsc, context);
            case VoltageRegulationSerDe.ELEMENT_NAME -> VoltageRegulationSerDe.readVoltageRegulation(vsc, context, vsc.getNetwork());
            default -> super.readSubElement(elementName, vsc, context);
        }
    }
}
