/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.GeneratorStartup;
import com.powsybl.iidm.network.extensions.GeneratorStartupAdder;
import com.powsybl.iidm.serde.IidmVersion;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class GeneratorStartupSerDe extends AbstractVersionableNetworkExtensionSerDe<Generator, GeneratorStartup, GeneratorStartupSerDe.Version> {

    public enum Version implements SerDeVersion<Version> {
        ITESLA_1_0("/xsd/generatorStartup_itesla_V1_0.xsd", "http://www.itesla_project.eu/schema/iidm/ext/generator_startup/1_0",
                new VersionNumbers(1, 0, "itesla"), IidmVersion.V_1_0, null),
        V_1_0("/xsd/generatorStartup_V1_0.xsd", "http://www.powsybl.org/schema/iidm/ext/generator_startup/1_0",
                new VersionNumbers(1, 0), IidmVersion.V_1_0, null),
        V_1_1("/xsd/generatorStartup_V1_1.xsd", "http://www.powsybl.org/schema/iidm/ext/generator_startup/1_1",
                new VersionNumbers(1, 1), IidmVersion.V_1_0, null);

        private final VersionInfo versionInfo;

        Version(String xsdResourcePath, String namespaceUri, VersionNumbers versionNumbers, IidmVersion minIidmVersionIncluded, IidmVersion maxIidmVersionExcluded) {
            this.versionInfo = new VersionInfo(xsdResourcePath, namespaceUri, "gs", versionNumbers,
                    minIidmVersionIncluded, maxIidmVersionExcluded, GeneratorStartup.NAME);
        }

        @Override
        public VersionInfo getVersionInfo() {
            return versionInfo;
        }
    }

    public GeneratorStartupSerDe() {
        super(GeneratorStartup.NAME, GeneratorStartup.class, Version.values());
    }

    @Override
    public void write(GeneratorStartup startup, SerializerContext context) {
        Version extensionVersionToExport = getExtensionVersionToExport(context);
        context.getWriter().writeDoubleAttribute(getPlannedActivePowerSetpointName(extensionVersionToExport), startup.getPlannedActivePowerSetpoint());
        context.getWriter().writeDoubleAttribute(getStartupCostName(extensionVersionToExport), startup.getStartupCost());
        context.getWriter().writeDoubleAttribute("marginalCost", startup.getMarginalCost());
        context.getWriter().writeDoubleAttribute("plannedOutageRate", startup.getPlannedOutageRate());
        context.getWriter().writeDoubleAttribute("forcedOutageRate", startup.getForcedOutageRate());
    }

    @Override
    public GeneratorStartup read(Generator generator, DeserializerContext context) {
        Version extensionVersionImported = getExtensionVersionImported(context);
        double plannedActivePowerSetpoint = context.getReader().readDoubleAttribute(getPlannedActivePowerSetpointName(extensionVersionImported));
        double startUpCost = context.getReader().readDoubleAttribute(getStartupCostName(extensionVersionImported));
        double marginalCost = context.getReader().readDoubleAttribute("marginalCost");
        double plannedOutageRate = context.getReader().readDoubleAttribute("plannedOutageRate");
        double forcedOutageRate = context.getReader().readDoubleAttribute("forcedOutageRate");
        context.getReader().readEndNode();
        return generator.newExtension(GeneratorStartupAdder.class)
                .withPlannedActivePowerSetpoint(plannedActivePowerSetpoint)
                .withStartupCost(startUpCost)
                .withMarginalCost(marginalCost)
                .withPlannedOutageRate(plannedOutageRate)
                .withForcedOutageRate(forcedOutageRate)
                .add();
    }

    private static String getStartupCostName(Version extensionVersionToExport) {
        return switch (extensionVersionToExport) {
            case ITESLA_1_0, V_1_0 -> "startUpCost";
            case V_1_1 -> "startupCost";
        };
    }

    private static String getPlannedActivePowerSetpointName(Version extensionVersionToExport) {
        return switch (extensionVersionToExport) {
            case ITESLA_1_0, V_1_0 -> "predefinedActivePowerSetpoint";
            case V_1_1 -> "plannedActivePowerSetpoint";
        };
    }
}
