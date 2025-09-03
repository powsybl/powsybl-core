/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSortedSet;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.extensions.BatteryShortCircuit;
import com.powsybl.iidm.network.extensions.BatteryShortCircuitAdder;
import com.powsybl.iidm.serde.IidmVersion;

import static com.powsybl.iidm.serde.extensions.BatteryShortCircuitSerDe.Version.V_1_0_LEGACY;
import static com.powsybl.iidm.serde.extensions.BatteryShortCircuitSerDe.Version.V_1_0_LEGACY_2;

/**
 * @author Coline Piloquet {@literal <coline.piloquet@rte-france.fr>}
 */
@AutoService(ExtensionSerDe.class)
public class BatteryShortCircuitSerDe extends AbstractVersionableNetworkExtensionSerDe<Battery, BatteryShortCircuit, BatteryShortCircuitSerDe.Version> {

    private static final ImmutableSortedSet<BatteryShortCircuitSerDe.Version> LEGACY_VERSIONS = ImmutableSortedSet.<BatteryShortCircuitSerDe.Version>reverseOrder().add(V_1_0_LEGACY, V_1_0_LEGACY_2).build();

    public enum Version implements SerDeVersion<BatteryShortCircuitSerDe.Version> {
        V_1_0_LEGACY("/xsd/batteryShortCircuit_V1_0_legacy.xsd", "http://www.itesla_project.eu/schema/iidm/ext/battery_short_circuits/1_0",
            new VersionNumbers(1, 0, "legacy"), IidmVersion.V_1_0, IidmVersion.V_1_14, "batteryShortCircuits"),
        V_1_0_LEGACY_2("/xsd/batteryShortCircuit_V1_0_legacy_2.xsd", "http://www.itesla_project.eu/schema/iidm/ext/batteryshortcircuits/1_0",
            new VersionNumbers(1, 0, "legacy-2"), IidmVersion.V_1_0, IidmVersion.V_1_14, "batteryShortCircuits"),
        V_1_0("/xsd/batteryShortCircuit_V1_0.xsd", "http://www.itesla_project.eu/schema/iidm/ext/battery_short_circuit/1_0",
            new VersionNumbers(1, 0), IidmVersion.V_1_13, null);

        private final VersionInfo versionInfo;

        Version(String xsdResourcePath, String namespaceUri, VersionNumbers versionNumbers, IidmVersion minIidmVersionIncluded, IidmVersion maxIidmVersionExcluded) {
            this.versionInfo = new VersionInfo(xsdResourcePath, namespaceUri, "bsc", versionNumbers,
                minIidmVersionIncluded, maxIidmVersionExcluded, BatteryShortCircuit.NAME);
        }

        Version(String xsdResourcePath, String namespaceUri, VersionNumbers versionNumbers, IidmVersion minIidmVersionIncluded, IidmVersion maxIidmVersionExcluded, String serializationName) {
            this.versionInfo = new VersionInfo(xsdResourcePath, namespaceUri, "bsc", versionNumbers,
                minIidmVersionIncluded, maxIidmVersionExcluded, serializationName);
        }

        @Override
        public VersionInfo getVersionInfo() {
            return versionInfo;
        }
    }

    public BatteryShortCircuitSerDe() {
        super(BatteryShortCircuit.NAME, BatteryShortCircuit.class, Version.values());
    }

    @Override
    public void write(BatteryShortCircuit batteryShortCircuit, SerializerContext context) {
        Version extensionVersionToExport = getExtensionVersionToExport(context);
        if (LEGACY_VERSIONS.contains(extensionVersionToExport)) {
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
        Version extensionVersionImported = getExtensionVersionImported(context);
        BatteryShortCircuitAdder batteryShortCircuitAdder = battery.newExtension(BatteryShortCircuitAdder.class);
        if (LEGACY_VERSIONS.contains(extensionVersionImported)) {
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
