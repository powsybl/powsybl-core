/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.extensions.BatteryShortCircuit;
import com.powsybl.iidm.network.extensions.BatteryShortCircuitAdder;
import com.powsybl.iidm.serde.IidmVersion;
import com.powsybl.iidm.serde.NetworkDeserializerContext;
import com.powsybl.iidm.serde.NetworkSerializerContext;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;

/**
 * @author Coline Piloquet {@literal <coline.piloquet@rte-france.fr>}
 */
@AutoService(ExtensionSerDe.class)
public class BatteryShortCircuitSerDe extends AbstractVersionableNetworkExtensionSerDe<Battery, BatteryShortCircuit> {

    public static final String V1_0_LEGACY = "1.0-legacy";

    public BatteryShortCircuitSerDe() {
        super(BatteryShortCircuit.NAME, BatteryShortCircuit.class, "bsc",
                new ImmutableMap.Builder<IidmVersion, ImmutableSortedSet<String>>()
                        .put(IidmVersion.V_1_0, ImmutableSortedSet.of(V1_0_LEGACY))
                        .put(IidmVersion.V_1_1, ImmutableSortedSet.of(V1_0_LEGACY))
                        .put(IidmVersion.V_1_2, ImmutableSortedSet.of(V1_0_LEGACY))
                        .put(IidmVersion.V_1_3, ImmutableSortedSet.of(V1_0_LEGACY))
                        .put(IidmVersion.V_1_4, ImmutableSortedSet.of(V1_0_LEGACY))
                        .put(IidmVersion.V_1_5, ImmutableSortedSet.of(V1_0_LEGACY))
                        .put(IidmVersion.V_1_6, ImmutableSortedSet.of(V1_0_LEGACY))
                        .put(IidmVersion.V_1_7, ImmutableSortedSet.of(V1_0_LEGACY))
                        .put(IidmVersion.V_1_8, ImmutableSortedSet.of(V1_0_LEGACY))
                        .put(IidmVersion.V_1_9, ImmutableSortedSet.of(V1_0_LEGACY))
                        .put(IidmVersion.V_1_10, ImmutableSortedSet.of(V1_0_LEGACY))
                        .put(IidmVersion.V_1_11, ImmutableSortedSet.of(V1_0_LEGACY))
                        .put(IidmVersion.V_1_12, ImmutableSortedSet.of(V1_0_LEGACY))
                        .put(IidmVersion.V_1_13, ImmutableSortedSet.<String>reverseOrder().add(V1_0_LEGACY, "1.0").build())
                        .build(),
                new ImmutableMap.Builder<String, String>()
                        .put(V1_0_LEGACY, "http://www.itesla_project.eu/schema/iidm/ext/battery_short_circuits/1_0")
                        .put("1.0", "http://www.itesla_project.eu/schema/iidm/ext/battery_short_circuit/1_0")
                        .build(),
                List.of(new AlternativeSerializationData("batteryShortCircuits", List.of(V1_0_LEGACY)))
        );
    }

    @Override
    public InputStream getXsdAsStream() {
        return getClass().getResourceAsStream("/xsd/batteryShortCircuit_V1_0.xsd");
    }

    @Override
    public List<InputStream> getXsdAsStreamList() {
        return List.of(Objects.requireNonNull(getClass().getResourceAsStream("/xsd/batteryShortCircuit_V1_0.xsd")),
                Objects.requireNonNull(getClass().getResourceAsStream("/xsd/batteryShortCircuit_V1_0_legacy.xsd")));
    }

    @Override
    public void write(BatteryShortCircuit batteryShortCircuit, SerializerContext context) {
        NetworkSerializerContext networkContext = (NetworkSerializerContext) context;
        String extVersionStr = networkContext.getExtensionVersion(getExtensionName())
                .orElseGet(() -> getVersion(networkContext.getVersion()));
        if (V1_0_LEGACY.compareTo(extVersionStr) == 0) {
            context.getWriter().writeFloatAttribute("transientReactance", (float) batteryShortCircuit.getDirectTransX());
            context.getWriter().writeFloatAttribute("stepUpTransformerReactance", (float) batteryShortCircuit.getStepUpTransformerX());
        } else {
            context.getWriter().writeDoubleAttribute("directSubtransX", batteryShortCircuit.getDirectSubtransX());
            context.getWriter().writeDoubleAttribute("directTransX", batteryShortCircuit.getDirectTransX());
            context.getWriter().writeDoubleAttribute("stepUpTransformerX", batteryShortCircuit.getStepUpTransformerX());
        }
    }

    @Override
    public BatteryShortCircuit read(Battery battery, DeserializerContext context) {
        NetworkDeserializerContext networkContext = (NetworkDeserializerContext) context;
        String extVersionStr = networkContext.getExtensionVersion(this).orElseThrow(IllegalStateException::new);
        BatteryShortCircuitAdder batteryShortCircuitAdder = battery.newExtension(BatteryShortCircuitAdder.class);
        if (V1_0_LEGACY.compareTo(extVersionStr) == 0) {
            batteryShortCircuitAdder
                    .withDirectTransX(context.getReader().readFloatAttribute("transientReactance"))
                    .withStepUpTransformerX(context.getReader().readFloatAttribute("stepUpTransformerReactance"));
        } else {
            batteryShortCircuitAdder
                    .withDirectSubtransX(context.getReader().readDoubleAttribute("directSubtransX"))
                    .withDirectTransX(context.getReader().readDoubleAttribute("directTransX"))
                    .withStepUpTransformerX(context.getReader().readDoubleAttribute("stepUpTransformerX"));
        }
        context.getReader().readEndNode();
        return batteryShortCircuitAdder.add();
    }
}
