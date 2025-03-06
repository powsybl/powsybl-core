/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.test.TerminalMockExt;
import com.powsybl.iidm.serde.extensions.AbstractVersionableNetworkExtensionSerDe;
import com.powsybl.iidm.serde.extensions.SerDeVersion;

import static com.powsybl.iidm.serde.AbstractIidmSerDeTest.getVersionDir;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class TerminalMockSerDe extends AbstractVersionableNetworkExtensionSerDe<Load, TerminalMockExt, TerminalMockSerDe.Version> {

    public enum Version implements SerDeVersion<Version> {
        V_1_0("http://www.itesla_project.eu/schema/iidm/ext/terminal_mock/1_0",
                new VersionNumbers(1, 0), IidmVersion.V_1_0, IidmVersion.V_1_1),
        V_1_1("http://www.powsybl.org/schema/iidm/ext/terminal_mock/1_1",
                new VersionNumbers(1, 1), IidmVersion.V_1_1, IidmVersion.V_1_2),
        V_1_2("http://www.powsybl.org/schema/iidm/ext/terminal_mock/1_2",
                new VersionNumbers(1, 2), IidmVersion.V_1_2, IidmVersion.V_1_3),
        V_1_3("http://www.powsybl.org/schema/iidm/ext/terminal_mock/1_3",
                new VersionNumbers(1, 3), IidmVersion.V_1_3, IidmVersion.V_1_4),
        V_1_4("http://www.powsybl.org/schema/iidm/ext/terminal_mock/1_4",
                new VersionNumbers(1, 4), IidmVersion.V_1_4, IidmVersion.V_1_5),
        V_1_5("http://www.powsybl.org/schema/iidm/ext/terminal_mock/1_5",
                new VersionNumbers(1, 5), IidmVersion.V_1_5, IidmVersion.V_1_6),
        V_1_6("http://www.powsybl.org/schema/iidm/ext/terminal_mock/1_6",
                new VersionNumbers(1, 6), IidmVersion.V_1_6, IidmVersion.V_1_7),
        V_1_7("http://www.powsybl.org/schema/iidm/ext/terminal_mock/1_7",
                new VersionNumbers(1, 7), IidmVersion.V_1_7, IidmVersion.V_1_8),
        V_1_8("http://www.powsybl.org/schema/iidm/ext/terminal_mock/1_8",
                new VersionNumbers(1, 8), IidmVersion.V_1_8, IidmVersion.V_1_9),
        V_1_9("http://www.powsybl.org/schema/iidm/ext/terminal_mock/1_9",
                new VersionNumbers(1, 9), IidmVersion.V_1_9, IidmVersion.V_1_10),
        V_1_10("http://www.powsybl.org/schema/iidm/ext/terminal_mock/1_10",
                new VersionNumbers(1, 10), IidmVersion.V_1_10, IidmVersion.V_1_11),
        V_1_11("http://www.powsybl.org/schema/iidm/ext/terminal_mock/1_11",
                new VersionNumbers(1, 11), IidmVersion.V_1_11, IidmVersion.V_1_12),
        V_1_12("http://www.powsybl.org/schema/iidm/ext/terminal_mock/1_12",
                new VersionNumbers(1, 12), IidmVersion.V_1_12, IidmVersion.V_1_13),
        V_1_13("http://www.powsybl.org/schema/iidm/ext/terminal_mock/1_13",
                new VersionNumbers(1, 13), IidmVersion.V_1_13, null);

        private final VersionInfo versionInfo;

        Version(String namespaceUri, VersionNumbers versionNumbers, IidmVersion minIidmVersionIncluded, IidmVersion maxIidmVersionExcluded) {
            String xsdResourcePath = getVersionDir(minIidmVersionIncluded) + "xsd/terminalMock.xsd";
            this.versionInfo = new VersionInfo(xsdResourcePath, namespaceUri, "mock", versionNumbers,
                    minIidmVersionIncluded, maxIidmVersionExcluded, "terminalMock");
        }

        @Override
        public VersionInfo getVersionInfo() {
            return versionInfo;
        }
    }

    public TerminalMockSerDe() {
        super("terminalMock", TerminalMockExt.class, Version.values());
    }

    @Override
    public void write(TerminalMockExt extension, SerializerContext context) {
        TerminalRefSerDe.writeTerminalRef(extension.getTerminal(), (NetworkSerializerContext) context,
                getExtensionVersionToExport(context).getNamespaceUri(), "terminal", context.getWriter());
    }

    @Override
    public TerminalMockExt read(Load load, DeserializerContext context) {
        NetworkDeserializerContext networkContext = (NetworkDeserializerContext) context;
        checkReadingCompatibility(networkContext);

        TerminalMockExt terminalMockExt = new TerminalMockExt(load);
        context.getReader().readChildNodes(elementName -> {
            if (elementName.equals("terminal")) {
                TerminalRefSerDe.readTerminalRef(networkContext, load.getNetwork(), terminalMockExt::setTerminal);
            } else {
                throw new IllegalStateException("Unexpected element: " + elementName);
            }
        });
        load.addExtension(TerminalMockExt.class, terminalMockExt);
        return terminalMockExt;
    }
}
