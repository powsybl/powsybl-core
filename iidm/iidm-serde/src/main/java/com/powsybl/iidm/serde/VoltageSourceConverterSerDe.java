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
import com.powsybl.iidm.network.util.VoltageRegulationUtils;
import com.powsybl.iidm.serde.util.IidmSerDeUtil;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.powsybl.iidm.serde.util.VoltageRegulationSerdeUtil.writeReactivePowerSetpoint;
import static com.powsybl.iidm.serde.util.VoltageRegulationSerdeUtil.writeVoltageSetpoint;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public class VoltageSourceConverterSerDe extends AbstractAcDcConverterSerDe<VoltageSourceConverter, VoltageSourceConverterAdder> {

    static final VoltageSourceConverterSerDe INSTANCE = new VoltageSourceConverterSerDe();
    static final String ROOT_ELEMENT_NAME = "voltageSourceConverter";
    static final String ARRAY_ELEMENT_NAME = "voltageSourceConverters";
    private static final String LOCAL_TARGET_V = "localTargetV";
    private static final String LOCAL_TARGET_Q = "localTargetQ";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected void writeRootElementAttributes(final VoltageSourceConverter vsc, final VoltageLevel parent, final NetworkSerializerContext context) {
        super.writeRootElementAttributes(vsc, parent, context);
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_16, context,
            () -> context.getWriter().writeBooleanAttribute("voltageRegulatorOn", vsc.isRegulatingWithMode(RegulationMode.VOLTAGE)));
        writeVoltageSetpoint(vsc, context);
        writeReactivePowerSetpoint(vsc, context);
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_17, context, () -> context.getWriter().writeDoubleAttribute(LOCAL_TARGET_V, vsc.getLocalTargetV()));
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_17, context, () -> context.getWriter().writeDoubleAttribute(LOCAL_TARGET_Q, vsc.getLocalTargetQ()));
        super.writeRootElementPqiAttributes(vsc, context);
    }

    @Override
    protected void writeSubElements(VoltageSourceConverter vsc, VoltageLevel parent, NetworkSerializerContext context) {
        super.writeSubElements(vsc, parent, context);
        ReactiveLimitsSerDe.INSTANCE.write(vsc, context);
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_17, context,
            () -> VoltageRegulationSerDe.writeVoltageRegulation(vsc.getVoltageRegulation(), context));
    }

    @Override
    protected VoltageSourceConverterAdder createAdder(final VoltageLevel voltageLevel) {
        return voltageLevel.newVoltageSourceConverter();
    }

    @Override
    protected void readRootElementAttributes(final VoltageSourceConverterAdder adder, final VoltageLevel parent, List<Consumer<VoltageSourceConverter>> toApply, final NetworkDeserializerContext context) {
        super.readRootElementCommonAttributes(adder, parent, context);
        AtomicReference<Boolean> voltageRegulatorOnRef = new AtomicReference<>(null);
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_16, context, () -> voltageRegulatorOnRef.set(context.getReader().readBooleanAttribute("voltageRegulatorOn")));

        AtomicReference<Double> voltageSetpoint = new AtomicReference<>(Double.NaN);
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_16, context, () -> voltageSetpoint.set(context.getReader().readDoubleAttribute("voltageSetpoint")));

        AtomicReference<Double> reactivePowerSetpoint = new AtomicReference<>(Double.NaN);
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_16, context, () -> reactivePowerSetpoint.set(context.getReader().readDoubleAttribute("reactivePowerSetpoint")));

        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_17, context, () -> {
            adder.setLocalTargetV(context.getReader().readDoubleAttribute(LOCAL_TARGET_V, Double.NaN));
            adder.setLocalTargetQ(context.getReader().readDoubleAttribute(LOCAL_TARGET_Q, Double.NaN));
        });

        readVoltageRegulationPrevious117(adder, context, voltageRegulatorOnRef, voltageSetpoint, reactivePowerSetpoint);

        super.readRootElementPqiAttributes(toApply, adder, context);
    }

    private static void readVoltageRegulationPrevious117(VoltageSourceConverterAdder adder, NetworkDeserializerContext context, AtomicReference<Boolean> voltageRegulatorOnRef, AtomicReference<Double> voltageSetpoint, AtomicReference<Double> reactivePowerSetpoint) {
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_16, context, () -> {
            VoltageRegulationUtils.VoltageRegulationData voltageRegulationData = VoltageRegulationUtils.buildVoltageRegulationData(voltageRegulatorOnRef.get(), voltageSetpoint.get(), reactivePowerSetpoint.get());
            adder.setLocalTargetV(voltageRegulationData.targetV());
            adder.setLocalTargetQ(voltageRegulationData.targetQ());
            if (voltageRegulationData.regulationMode() != null) {
                adder.newVoltageRegulation()
                    .withMode(voltageRegulationData.regulationMode())
                    .add();
            }
        });
    }

    @Override
    protected void readSubElements(String id, VoltageSourceConverterAdder adder, List<Consumer<VoltageSourceConverter>> toApply, NetworkDeserializerContext context) {
        context.getReader().readChildNodes(elementName -> {
            switch (elementName) {
                case ReactiveLimitsSerDe.ELEM_REACTIVE_CAPABILITY_CURVE -> ReactiveLimitsSerDe.INSTANCE.readReactiveCapabilityCurve(toApply, context);
                case ReactiveLimitsSerDe.ELEM_MIN_MAX_REACTIVE_LIMITS -> ReactiveLimitsSerDe.INSTANCE.readMinMaxReactiveLimits(toApply, context);
                case VoltageRegulationSerDe.ELEMENT_NAME -> VoltageRegulationSerDe.readVoltageRegulation(toApply, adder, context);
                default -> super.readSubElement(elementName, id, toApply, context);
            }
        });
    }
}
