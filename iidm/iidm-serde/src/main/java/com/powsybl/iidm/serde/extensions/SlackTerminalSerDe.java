/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
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

import java.io.InputStream;
import java.util.List;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class SlackTerminalSerDe extends AbstractVersionableNetworkExtensionSerDe<VoltageLevel, SlackTerminal> {

    public SlackTerminalSerDe() {

        super("slackTerminal", SlackTerminal.class, "slt",
            new ImmutableMap.Builder<IidmVersion, ImmutableSortedSet<String>>()
                .put(IidmVersion.V_1_3, ImmutableSortedSet.of("1.0"))
                .put(IidmVersion.V_1_4, ImmutableSortedSet.of("1.1"))
                .put(IidmVersion.V_1_5, ImmutableSortedSet.of("1.2"))
                .put(IidmVersion.V_1_6, ImmutableSortedSet.of("1.3"))
                .put(IidmVersion.V_1_7, ImmutableSortedSet.of("1.4"))
                .put(IidmVersion.V_1_8, ImmutableSortedSet.of("1.5"))
                .put(IidmVersion.V_1_9, ImmutableSortedSet.of("1.5"))
                .put(IidmVersion.V_1_10, ImmutableSortedSet.of("1.5"))
                .put(IidmVersion.V_1_11, ImmutableSortedSet.of("1.5"))
                .put(IidmVersion.V_1_12, ImmutableSortedSet.of("1.5"))
                .build(),
            new ImmutableMap.Builder<String, String>()
                .put("1.0", "http://www.powsybl.org/schema/iidm/ext/slack_terminal/1_0")
                .put("1.1", "http://www.powsybl.org/schema/iidm/ext/slack_terminal/1_1")
                .put("1.2", "http://www.powsybl.org/schema/iidm/ext/slack_terminal/1_2")
                .put("1.3", "http://www.powsybl.org/schema/iidm/ext/slack_terminal/1_3")
                .put("1.4", "http://www.powsybl.org/schema/iidm/ext/slack_terminal/1_4")
                .put("1.5", "http://www.powsybl.org/schema/iidm/ext/slack_terminal/1_5")
                .build());
    }

    @Override
    public InputStream getXsdAsStream() {
        return getClass().getResourceAsStream("/xsd/slackTerminal_V1_5.xsd");
    }

    @Override
    public List<InputStream> getXsdAsStreamList() {
        return List.of(getClass().getResourceAsStream("/xsd/slackTerminal_V1_0.xsd"),
                getClass().getResourceAsStream("/xsd/slackTerminal_V1_1.xsd"),
                getClass().getResourceAsStream("/xsd/slackTerminal_V1_2.xsd"),
                getClass().getResourceAsStream("/xsd/slackTerminal_V1_3.xsd"),
                getClass().getResourceAsStream("/xsd/slackTerminal_V1_4.xsd"),
                getClass().getResourceAsStream("/xsd/slackTerminal_V1_5.xsd"));
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
