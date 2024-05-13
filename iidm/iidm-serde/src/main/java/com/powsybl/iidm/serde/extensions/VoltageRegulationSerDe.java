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
import com.powsybl.iidm.network.extensions.VoltageRegulation;
import com.powsybl.iidm.network.extensions.VoltageRegulationAdder;
import com.powsybl.iidm.serde.IidmVersion;
import com.powsybl.iidm.serde.NetworkDeserializerContext;
import com.powsybl.iidm.serde.NetworkSerializerContext;
import com.powsybl.iidm.serde.TerminalRefSerDe;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;

/**
 * @author Coline Piloquet {@literal <coline.piloquet@rte-france.fr>}
 */
@AutoService(ExtensionSerDe.class)
public class VoltageRegulationSerDe extends AbstractVersionableNetworkExtensionSerDe<Battery, VoltageRegulation> {

    public VoltageRegulationSerDe() {
        super("voltageRegulation", VoltageRegulation.class, "vr",
                ImmutableMap.<IidmVersion, ImmutableSortedSet<String>>builder()
                        .put(IidmVersion.V_1_0, ImmutableSortedSet.of("1.0", "1.0-legacy"))
                        .put(IidmVersion.V_1_1, ImmutableSortedSet.of("1.1"))
                        .put(IidmVersion.V_1_2, ImmutableSortedSet.of("1.1"))
                        .put(IidmVersion.V_1_3, ImmutableSortedSet.of("1.1"))
                        .put(IidmVersion.V_1_4, ImmutableSortedSet.of("1.1"))
                        .put(IidmVersion.V_1_5, ImmutableSortedSet.of("1.1"))
                        .put(IidmVersion.V_1_6, ImmutableSortedSet.of("1.1"))
                        .put(IidmVersion.V_1_7, ImmutableSortedSet.of("1.1"))
                        .put(IidmVersion.V_1_8, ImmutableSortedSet.of("1.1"))
                        .put(IidmVersion.V_1_9, ImmutableSortedSet.of("1.1"))
                        .put(IidmVersion.V_1_10, ImmutableSortedSet.of("1.1"))
                        .put(IidmVersion.V_1_11, ImmutableSortedSet.of("1.1"))
                        .put(IidmVersion.V_1_12, ImmutableSortedSet.of("1.1"))
                        .build(),
                ImmutableMap.<String, String>builder()
                        .put("1.0", "http://www.itesla_project.eu/schema/iidm/ext/voltage_regulation/1_0")
                        .put("1.0-legacy", "http://www.itesla_project.eu/schema/iidm/ext/voltageregulation/1_0")
                        .put("1.1", "http://www.powsybl.org/schema/iidm/ext/voltage_regulation/1_1")
                        .build());
    }

    @Override
    public InputStream getXsdAsStream() {
        return getClass().getResourceAsStream("/xsd/voltageRegulation_V1_1.xsd");
    }

    @Override
    public List<InputStream> getXsdAsStreamList() {
        return List.of(getClass().getResourceAsStream("/xsd/voltageRegulation_V1_0.xsd"),
                getClass().getResourceAsStream("/xsd/voltageRegulation_V1_0_legacy.xsd"),
                getClass().getResourceAsStream("/xsd/voltageRegulation_V1_1.xsd"));
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
            String extensionVersion = networkContext.getExtensionVersion(getExtensionName()).orElseGet(() -> getVersion(networkContext.getVersion()));
            TerminalRefSerDe.writeTerminalRef(voltageRegulation.getRegulatingTerminal(), networkContext, getNamespaceUri(extensionVersion),
                "terminalRef", networkContext.getWriter());
        }
    }

    @Override
    public VoltageRegulation read(Battery battery, DeserializerContext context) {
        NetworkDeserializerContext networkContext = convertContext(context);
        checkReadingCompatibility(networkContext);

        boolean voltageRegulatorOn = networkContext.getReader().readBooleanAttribute("voltageRegulatorOn");
        double targetV = networkContext.getReader().readDoubleAttribute("targetV");

        VoltageRegulation voltageRegulation = battery.newExtension(VoltageRegulationAdder.class).withVoltageRegulatorOn(voltageRegulatorOn).withTargetV(targetV).add();

        networkContext.getReader().readChildNodes(elementName -> {
            if (elementName.equals("terminalRef")) {
                TerminalRefSerDe.readTerminalRef(networkContext, battery.getTerminal().getVoltageLevel().getNetwork(), voltageRegulation::setRegulatingTerminal);
            } else {
                throw new AssertionError("Unexpected element: " + elementName);
            }
        });
        return voltageRegulation;
    }

    /**
     * Safe conversion of a XmlWriterContext to a NetworkXmlWriterContext
     */
    private static NetworkSerializerContext convertContext(SerializerContext context) {
        if (context instanceof NetworkSerializerContext networkSerializerContext) {
            return networkSerializerContext;
        }
        throw new IllegalArgumentException("context is not a NetworkXmlWriterContext");
    }

    /**
     * Safe conversion of a XmlReaderContext to a NetworkXmlReaderContext
     */
    private static NetworkDeserializerContext convertContext(DeserializerContext context) {
        if (context instanceof NetworkDeserializerContext networkDeserializerContext) {
            return networkDeserializerContext;
        }
        throw new IllegalArgumentException("context is not a NetworkXmlReaderContext");
    }
}
