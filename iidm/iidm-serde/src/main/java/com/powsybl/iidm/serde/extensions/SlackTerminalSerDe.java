/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
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
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.SlackTerminal;
import com.powsybl.iidm.network.extensions.SlackTerminalAdder;
import com.powsybl.iidm.serde.IidmVersion;
import com.powsybl.iidm.serde.NetworkDeserializerContext;
import com.powsybl.iidm.serde.NetworkSerializerContext;
import com.powsybl.iidm.serde.TerminalRefSerDe;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class SlackTerminalSerDe extends AbstractVersionableNetworkExtensionSerDe<VoltageLevel, SlackTerminal, SlackTerminalSerDe.Version> {

    public enum Version implements SerDeVersion<Version> {
        V_1_0("/xsd/slackTerminal_V1_0.xsd", "http://www.powsybl.org/schema/iidm/ext/slack_terminal/1_0",
                new VersionNumbers(1, 0), IidmVersion.V_1_3, IidmVersion.V_1_4),
        V_1_1("/xsd/slackTerminal_V1_1.xsd", "http://www.powsybl.org/schema/iidm/ext/slack_terminal/1_1",
                new VersionNumbers(1, 1), IidmVersion.V_1_4, IidmVersion.V_1_5),
        V_1_2("/xsd/slackTerminal_V1_2.xsd", "http://www.powsybl.org/schema/iidm/ext/slack_terminal/1_2",
                new VersionNumbers(1, 2), IidmVersion.V_1_5, IidmVersion.V_1_6),
        V_1_3("/xsd/slackTerminal_V1_3.xsd", "http://www.powsybl.org/schema/iidm/ext/slack_terminal/1_3",
                new VersionNumbers(1, 3), IidmVersion.V_1_6, IidmVersion.V_1_7),
        V_1_4("/xsd/slackTerminal_V1_4.xsd", "http://www.powsybl.org/schema/iidm/ext/slack_terminal/1_4",
                new VersionNumbers(1, 4), IidmVersion.V_1_7, IidmVersion.V_1_8),
        V_1_5("/xsd/slackTerminal_V1_5.xsd", "http://www.powsybl.org/schema/iidm/ext/slack_terminal/1_5",
                new VersionNumbers(1, 5), IidmVersion.V_1_8, null);

        private final VersionInfo versionInfo;

        Version(String xsdResourcePath, String namespaceUri, VersionNumbers versionNumbers, IidmVersion minIidmVersionIncluded, IidmVersion maxIidmVersionExcluded) {
            this.versionInfo = new VersionInfo(xsdResourcePath, namespaceUri, "slt", versionNumbers,
                    minIidmVersionIncluded, maxIidmVersionExcluded, SlackTerminal.NAME);
        }

        @Override
        public VersionInfo getVersionInfo() {
            return versionInfo;
        }
    }

    public SlackTerminalSerDe() {
        super(SlackTerminal.NAME, SlackTerminal.class, Version.values());
    }

    @Override
    public void write(SlackTerminal slackTerminal, SerializerContext context) {
        TerminalRefSerDe.writeTerminalRefAttribute(slackTerminal.getTerminal(), (NetworkSerializerContext) context);
    }

    @Override
    public SlackTerminal read(VoltageLevel voltageLevel, DeserializerContext context) {
        Terminal terminal = TerminalRefSerDe.readTerminal((NetworkDeserializerContext) context, voltageLevel.getNetwork());
        return voltageLevel.newExtension(SlackTerminalAdder.class)
                .withTerminal(terminal)
                .add();
    }

    /**
     * A {@link SlackTerminal} extension is serializable if the terminal for the current variant is not null
     *
     * @param slackTerminal The extension to check
     * @return true if the terminal for the current variant is not null, false otherwise
     */
    @Override
    public boolean isSerializable(SlackTerminal slackTerminal) {
        return slackTerminal.getTerminal() != null;
    }
}
