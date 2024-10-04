/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtensionSerDe;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.extensions.BatteryShortCircuit;
import com.powsybl.iidm.network.extensions.BatteryShortCircuitAdder;

/**
 * @author Coline Piloquet {@literal <coline.piloquet@rte-france.fr>}
 */
@AutoService(ExtensionSerDe.class)
public class BatteryShortCircuitSerDe extends AbstractExtensionSerDe<Battery, BatteryShortCircuit> {

    public BatteryShortCircuitSerDe() {
        super("batteryShortCircuit", "network", BatteryShortCircuit.class,
            "batteryShortCircuit.xsd", "http://www.itesla_project.eu/schema/iidm/ext/battery_short_circuit/1_0",
            "bsc");
    }

    @Override
    public void write(BatteryShortCircuit batteryShortCircuit, SerializerContext context) {
        context.getWriter().writeDoubleAttribute("directSubtransX", batteryShortCircuit.getDirectSubtransX());
        context.getWriter().writeDoubleAttribute("directTransX", batteryShortCircuit.getDirectTransX());
        context.getWriter().writeDoubleAttribute("stepUpTransformerX", batteryShortCircuit.getStepUpTransformerX());
    }

    @Override
    public BatteryShortCircuit read(Battery battery, DeserializerContext context) {
        double directSubtransX = context.getReader().readDoubleAttribute("directSubtransX");
        double directTransX = context.getReader().readDoubleAttribute("directTransX");
        double stepUpTransformerX = context.getReader().readDoubleAttribute("stepUpTransformerX");
        context.getReader().readEndNode();
        return battery.newExtension(BatteryShortCircuitAdder.class)
            .withDirectSubtransX(directSubtransX)
            .withDirectTransX(directTransX)
            .withStepUpTransformerX(stepUpTransformerX)
            .add();
    }
}
