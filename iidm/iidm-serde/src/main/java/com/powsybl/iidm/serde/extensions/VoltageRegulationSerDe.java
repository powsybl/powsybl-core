/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
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
import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.extensions.VoltageRegulation;
import com.powsybl.iidm.network.extensions.VoltageRegulationAdder;
import com.powsybl.iidm.serde.IidmVersion;
import com.powsybl.iidm.serde.NetworkDeserializerContext;
import com.powsybl.iidm.serde.NetworkSerializerContext;
import com.powsybl.iidm.serde.TerminalRefSerDe;

import java.io.InputStream;
import java.util.Objects;

/**
 * @author Coline Piloquet {@literal <coline.piloquet@rte-france.fr>}
 */
@AutoService(ExtensionSerDe.class)
public class VoltageRegulationSerDe extends AbstractVersionableNetworkExtensionSerDe<Battery, VoltageRegulation, VoltageRegulationSerDe.Version> {

    public enum Version implements SerDeVersion<Version> {
        V_1_0_LEGACY("/xsd/voltageRegulation_V1_0_legacy.xsd", "http://www.itesla_project.eu/schema/iidm/ext/voltageregulation/1_0",
                new VersionNumbers(1, 0, "legacy"), IidmVersion.V_1_0, IidmVersion.V_1_1),
        V_1_0("/xsd/voltageRegulation_V1_0.xsd", "http://www.powsybl.org/schema/iidm/ext/voltage_regulation/1_0",
                new VersionNumbers(1, 0), IidmVersion.V_1_0, IidmVersion.V_1_1),
        V_1_1("/xsd/voltageRegulation_V1_1.xsd", "http://www.powsybl.org/schema/iidm/ext/voltage_regulation/1_1",
                new VersionNumbers(1, 1), IidmVersion.V_1_1, null),
        V_1_2("/xsd/compatibility/voltage_regulation/voltageRegulation_V1_2.xsd", "http://www.powsybl.org/schema/iidm/ext/voltage_regulation/1_2",
                new VersionNumbers(1, 2), IidmVersion.V_1_2, IidmVersion.V_1_3),
        V_1_3("/xsd/compatibility/voltage_regulation/voltageRegulation_V1_3.xsd", "http://www.powsybl.org/schema/iidm/ext/voltage_regulation/1_3",
                new VersionNumbers(1, 3), IidmVersion.V_1_3, IidmVersion.V_1_4),
        V_1_4("/xsd/compatibility/voltage_regulation/voltageRegulation_V1_4.xsd", "http://www.powsybl.org/schema/iidm/ext/voltage_regulation/1_4",
                new VersionNumbers(1, 4), IidmVersion.V_1_4, IidmVersion.V_1_5),
        V_1_5("/xsd/compatibility/voltage_regulation/voltageRegulation_V1_5.xsd", "http://www.powsybl.org/schema/iidm/ext/voltage_regulation/1_5",
                new VersionNumbers(1, 5), IidmVersion.V_1_5, IidmVersion.V_1_6),
        V_1_6("/xsd/compatibility/voltage_regulation/voltageRegulation_V1_6.xsd", "http://www.powsybl.org/schema/iidm/ext/voltage_regulation/1_6",
                new VersionNumbers(1, 6), IidmVersion.V_1_6, IidmVersion.V_1_7),
        V_1_7("/xsd/compatibility/voltage_regulation/voltageRegulation_V1_7.xsd", "http://www.powsybl.org/schema/iidm/ext/voltage_regulation/1_7",
                new VersionNumbers(1, 7), IidmVersion.V_1_7, IidmVersion.V_1_8),
        V_1_8("/xsd/compatibility/voltage_regulation/voltageRegulation_V1_8.xsd", "http://www.powsybl.org/schema/iidm/ext/voltage_regulation/1_8",
                new VersionNumbers(1, 8), IidmVersion.V_1_8, IidmVersion.V_1_9),
        V_1_9("/xsd/compatibility/voltage_regulation/voltageRegulation_V1_9.xsd", "http://www.powsybl.org/schema/iidm/ext/voltage_regulation/1_9",
                new VersionNumbers(1, 9), IidmVersion.V_1_9, IidmVersion.V_1_10),
        V_1_10("/xsd/compatibility/voltage_regulation/voltageRegulation_V1_10.xsd", "http://www.powsybl.org/schema/iidm/ext/voltage_regulation/1_10",
                new VersionNumbers(1, 10), IidmVersion.V_1_10, IidmVersion.V_1_11),
        V_1_11("/xsd/compatibility/voltage_regulation/voltageRegulation_V1_11.xsd", "http://www.powsybl.org/schema/iidm/ext/voltage_regulation/1_11",
                new VersionNumbers(1, 11), IidmVersion.V_1_11, IidmVersion.V_1_12),
        V_1_12("/xsd/compatibility/voltage_regulation/voltageRegulation_V1_12.xsd", "http://www.powsybl.org/schema/iidm/ext/voltage_regulation/1_12",
                new VersionNumbers(1, 12), IidmVersion.V_1_12, IidmVersion.V_1_13);

        private final VersionInfo versionInfo;

        Version(String xsdResourcePath, String namespaceUri, VersionNumbers versionNumbers, IidmVersion minIidmVersionIncluded, IidmVersion maxIidmVersionExcluded) {
            this.versionInfo = new VersionInfo(xsdResourcePath, namespaceUri, "vr", versionNumbers,
                    minIidmVersionIncluded, maxIidmVersionExcluded, VoltageRegulation.NAME);
        }

        @Override
        public VersionInfo getVersionInfo() {
            return versionInfo;
        }
    }

    public VoltageRegulationSerDe() {
        super(VoltageRegulation.NAME, VoltageRegulation.class, Version.values());
    }

    @Override
    public InputStream getXsdAsStream() {
        return getClass().getResourceAsStream("/xsd/compatibility/voltage_regulation/voltageRegulation_V1_1.xsd");
    }

    @Override
    public void write(VoltageRegulation voltageRegulation, SerializerContext context) {
        NetworkSerializerContext networkContext = convertContext(context);
        networkContext.getExtensionVersion(getExtensionName())
            .ifPresent(extensionVersion -> checkWritingCompatibility(extensionVersion, networkContext.getVersion()));

        networkContext.getWriter().writeBooleanAttribute("voltageRegulatorOn", voltageRegulation.isVoltageRegulatorOn());
        networkContext.getWriter().writeDoubleAttribute("targetV", voltageRegulation.getTargetV());

        if (voltageRegulation.getRegulatingTerminal() != null
            && !Objects.equals(voltageRegulation.getRegulatingTerminal().getBusBreakerView().getConnectableBus(),
            voltageRegulation.getExtendable().getTerminal().getBusBreakerView().getConnectableBus())) {
            TerminalRefSerDe.writeTerminalRef(voltageRegulation.getRegulatingTerminal(), networkContext,
                    getExtensionVersionToExport(context).getNamespaceUri(), "terminalRef", networkContext.getWriter());
        }
    }

    @Override
    public VoltageRegulation read(Battery battery, DeserializerContext context) {
        boolean voltageRegulatorOn = context.getReader().readBooleanAttribute("voltageRegulatorOn");
        double targetV = context.getReader().readDoubleAttribute("targetV");

        VoltageRegulation voltageRegulation = battery.newExtension(VoltageRegulationAdder.class).withVoltageRegulatorOn(voltageRegulatorOn).withTargetV(targetV).add();

        context.getReader().readChildNodes(elementName -> {
            if (elementName.equals("terminalRef")) {
                TerminalRefSerDe.readTerminalRef(context, battery.getTerminal().getVoltageLevel().getNetwork(), voltageRegulation::setRegulatingTerminal);
            } else {
                throw new AssertionError("Unexpected element: " + elementName);
            }
        });
        return voltageRegulation;
    }

    @Override
    protected Version getDefaultVersion() {
        // Default version is v1.1, the subsequent ones have been added without any change
        return Version.V_1_1;
    }
}
