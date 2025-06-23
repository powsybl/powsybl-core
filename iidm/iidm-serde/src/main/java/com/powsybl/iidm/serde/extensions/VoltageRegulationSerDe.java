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
        super(VoltageRegulation.NAME, VoltageRegulation.class, "vr",
                ImmutableMap.<IidmVersion, ImmutableSortedSet<String>>builder()
                        .put(IidmVersion.V_1_0, ImmutableSortedSet.of("1.0", "1.0-legacy"))
                        .put(IidmVersion.V_1_1, ImmutableSortedSet.of("1.1"))
                        .put(IidmVersion.V_1_2, reversedNaturalOrderOf("1.1", "1.2"))
                        .put(IidmVersion.V_1_3, reversedNaturalOrderOf("1.1", "1.3"))
                        .put(IidmVersion.V_1_4, reversedNaturalOrderOf("1.1", "1.4"))
                        .put(IidmVersion.V_1_5, reversedNaturalOrderOf("1.1", "1.5"))
                        .put(IidmVersion.V_1_6, reversedNaturalOrderOf("1.1", "1.6"))
                        .put(IidmVersion.V_1_7, reversedNaturalOrderOf("1.1", "1.7"))
                        .put(IidmVersion.V_1_8, reversedNaturalOrderOf("1.1", "1.8"))
                        .put(IidmVersion.V_1_9, reversedNaturalOrderOf("1.1", "1.9"))
                        .put(IidmVersion.V_1_10, reversedNaturalOrderOf("1.1", "1.10"))
                        .put(IidmVersion.V_1_11, reversedNaturalOrderOf("1.1", "1.11"))
                        .put(IidmVersion.V_1_12, reversedNaturalOrderOf("1.1", "1.12"))
                        .put(IidmVersion.V_1_13, ImmutableSortedSet.of("1.1"))
                        .put(IidmVersion.V_1_14, ImmutableSortedSet.of("1.1"))
                        .build(),
                ImmutableMap.<String, String>builder()
                        .put("1.0", "http://www.itesla_project.eu/schema/iidm/ext/voltage_regulation/1_0")
                        .put("1.0-legacy", "http://www.itesla_project.eu/schema/iidm/ext/voltageregulation/1_0")
                        .put("1.1", "http://www.powsybl.org/schema/iidm/ext/voltage_regulation/1_1")
                        .put("1.2", "http://www.powsybl.org/schema/iidm/ext/voltage_regulation/1_2")
                        .put("1.3", "http://www.powsybl.org/schema/iidm/ext/voltage_regulation/1_3")
                        .put("1.4", "http://www.powsybl.org/schema/iidm/ext/voltage_regulation/1_4")
                        .put("1.5", "http://www.powsybl.org/schema/iidm/ext/voltage_regulation/1_5")
                        .put("1.6", "http://www.powsybl.org/schema/iidm/ext/voltage_regulation/1_6")
                        .put("1.7", "http://www.powsybl.org/schema/iidm/ext/voltage_regulation/1_7")
                        .put("1.8", "http://www.powsybl.org/schema/iidm/ext/voltage_regulation/1_8")
                        .put("1.9", "http://www.powsybl.org/schema/iidm/ext/voltage_regulation/1_9")
                        .put("1.10", "http://www.powsybl.org/schema/iidm/ext/voltage_regulation/1_10")
                        .put("1.11", "http://www.powsybl.org/schema/iidm/ext/voltage_regulation/1_11")
                        .put("1.12", "http://www.powsybl.org/schema/iidm/ext/voltage_regulation/1_12")
                        .build());
    }

    private static ImmutableSortedSet<String> reversedNaturalOrderOf(String... versions) {
        return ImmutableSortedSet.<String>reverseOrder().add(versions).build();
    }

    @Override
    public InputStream getXsdAsStream() {
        return getClass().getResourceAsStream("/xsd/voltageRegulation_V1_1.xsd");
    }

    @Override
    public List<InputStream> getXsdAsStreamList() {
        return List.of(Objects.requireNonNull(getClass().getResourceAsStream("/xsd/voltageRegulation_V1_0.xsd")),
                Objects.requireNonNull(getClass().getResourceAsStream("/xsd/voltageRegulation_V1_0_legacy.xsd")),
                Objects.requireNonNull(getClass().getResourceAsStream("/xsd/voltageRegulation_V1_1.xsd")),
                Objects.requireNonNull(getClass().getResourceAsStream("/xsd/compatibility/voltage_regulation/voltageRegulation_V1_2.xsd")),
                Objects.requireNonNull(getClass().getResourceAsStream("/xsd/compatibility/voltage_regulation/voltageRegulation_V1_3.xsd")),
                Objects.requireNonNull(getClass().getResourceAsStream("/xsd/compatibility/voltage_regulation/voltageRegulation_V1_4.xsd")),
                Objects.requireNonNull(getClass().getResourceAsStream("/xsd/compatibility/voltage_regulation/voltageRegulation_V1_5.xsd")),
                Objects.requireNonNull(getClass().getResourceAsStream("/xsd/compatibility/voltage_regulation/voltageRegulation_V1_6.xsd")),
                Objects.requireNonNull(getClass().getResourceAsStream("/xsd/compatibility/voltage_regulation/voltageRegulation_V1_7.xsd")),
                Objects.requireNonNull(getClass().getResourceAsStream("/xsd/compatibility/voltage_regulation/voltageRegulation_V1_8.xsd")),
                Objects.requireNonNull(getClass().getResourceAsStream("/xsd/compatibility/voltage_regulation/voltageRegulation_V1_9.xsd")),
                Objects.requireNonNull(getClass().getResourceAsStream("/xsd/compatibility/voltage_regulation/voltageRegulation_V1_10.xsd")),
                Objects.requireNonNull(getClass().getResourceAsStream("/xsd/compatibility/voltage_regulation/voltageRegulation_V1_11.xsd")),
                Objects.requireNonNull(getClass().getResourceAsStream("/xsd/compatibility/voltage_regulation/voltageRegulation_V1_12.xsd")));
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
