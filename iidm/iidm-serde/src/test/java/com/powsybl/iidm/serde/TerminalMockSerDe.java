/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.test.TerminalMockExt;
import com.powsybl.iidm.serde.extensions.AbstractVersionableNetworkExtensionSerDe;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.powsybl.iidm.serde.AbstractIidmSerDeTest.getVersionDir;
import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class TerminalMockSerDe extends AbstractVersionableNetworkExtensionSerDe<Load, TerminalMockExt> {

    public TerminalMockSerDe() {
        super("terminalMock", TerminalMockExt.class, "mock",
                ImmutableMap.<IidmVersion, ImmutableSortedSet<String>>builder()
                        .put(IidmVersion.V_1_0, ImmutableSortedSet.of("1.0"))
                        .put(IidmVersion.V_1_1, ImmutableSortedSet.of("1.1"))
                        .put(IidmVersion.V_1_2, ImmutableSortedSet.of("1.2"))
                        .put(IidmVersion.V_1_3, ImmutableSortedSet.of("1.3"))
                        .put(IidmVersion.V_1_4, ImmutableSortedSet.of("1.4"))
                        .put(IidmVersion.V_1_5, ImmutableSortedSet.of("1.5"))
                        .put(IidmVersion.V_1_6, ImmutableSortedSet.of("1.6"))
                        .put(IidmVersion.V_1_7, ImmutableSortedSet.of("1.7"))
                        .put(IidmVersion.V_1_8, ImmutableSortedSet.of("1.8"))
                        .put(IidmVersion.V_1_9, ImmutableSortedSet.of("1.9"))
                        .put(IidmVersion.V_1_10, ImmutableSortedSet.of("1.10"))
                        .put(IidmVersion.V_1_11, ImmutableSortedSet.of("1.11"))
                        .put(IidmVersion.V_1_12, ImmutableSortedSet.of("1.12"))
                        .put(IidmVersion.V_1_13, ImmutableSortedSet.of("1.13"))
                        .build(),
                ImmutableMap.<String, String>builder()
                        .put("1.0", "http://www.itesla_project.eu/schema/iidm/ext/terminal_mock/1_0")
                        .put("1.1", "http://www.powsybl.org/schema/iidm/ext/terminal_mock/1_1")
                        .put("1.2", "http://www.powsybl.org/schema/iidm/ext/terminal_mock/1_2")
                        .put("1.3", "http://www.powsybl.org/schema/iidm/ext/terminal_mock/1_3")
                        .put("1.4", "http://www.powsybl.org/schema/iidm/ext/terminal_mock/1_4")
                        .put("1.5", "http://www.powsybl.org/schema/iidm/ext/terminal_mock/1_5")
                        .put("1.6", "http://www.powsybl.org/schema/iidm/ext/terminal_mock/1_6")
                        .put("1.7", "http://www.powsybl.org/schema/iidm/ext/terminal_mock/1_7")
                        .put("1.8", "http://www.powsybl.org/schema/iidm/ext/terminal_mock/1_8")
                        .put("1.9", "http://www.powsybl.org/schema/iidm/ext/terminal_mock/1_9")
                        .put("1.10", "http://www.powsybl.org/schema/iidm/ext/terminal_mock/1_10")
                        .put("1.11", "http://www.powsybl.org/schema/iidm/ext/terminal_mock/1_11")
                        .put("1.12", "http://www.powsybl.org/schema/iidm/ext/terminal_mock/1_12")
                        .put("1.13", "http://www.powsybl.org/schema/iidm/ext/terminal_mock/1_13")
                        .build());
    }

    @Override
    public InputStream getXsdAsStream() {
        return getClass().getResourceAsStream(getVersionDir(CURRENT_IIDM_VERSION) + "xsd/terminalMock.xsd");
    }

    @Override
    public List<InputStream> getXsdAsStreamList() {
        return Arrays.stream(IidmVersion.values())
                .map(v -> getClass().getResourceAsStream(getVersionDir(v) + "xsd/terminalMock.xsd"))
                .collect(Collectors.toList());
    }

    @Override
    public void write(TerminalMockExt extension, SerializerContext context) {
        NetworkSerializerContext networkContext = (NetworkSerializerContext) context;
        String extensionVersion = networkContext.getOptions().getExtensionVersion(getExtensionName())
                .orElseGet(() -> getVersion(networkContext.getVersion()));
        TerminalRefSerDe.writeTerminalRef(extension.getTerminal(), networkContext, getNamespaceUri(extensionVersion), "terminal", context.getWriter());
    }

    @Override
    public TerminalMockExt read(Load extendable, DeserializerContext context) {
        NetworkDeserializerContext networkContext = (NetworkDeserializerContext) context;
        checkReadingCompatibility(networkContext);

        TerminalMockExt terminalMockExt = new TerminalMockExt(extendable);
        context.getReader().readChildNodes(elementName -> {
            if (elementName.equals("terminal")) {
                TerminalRefSerDe.readTerminalRef(networkContext, extendable.getNetwork(), terminalMockExt::setTerminal);
            } else {
                throw new IllegalStateException("Unexpected element: " + elementName);
            }
        });
        return terminalMockExt;
    }
}
