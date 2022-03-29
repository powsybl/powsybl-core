/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.extensions;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.SlackTerminal;
import com.powsybl.iidm.network.extensions.SlackTerminalAdder;
import com.powsybl.iidm.xml.IidmXmlVersion;
import com.powsybl.iidm.xml.NetworkXmlReaderContext;
import com.powsybl.iidm.xml.NetworkXmlWriterContext;
import com.powsybl.iidm.xml.TerminalRefXml;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.util.List;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class SlackTerminalXmlSerializer extends AbstractVersionableNetworkExtensionXmlSerializer<VoltageLevel, SlackTerminal> {

    public SlackTerminalXmlSerializer() {
        super("slackTerminal", SlackTerminal.class, false, "slt",
                ImmutableMap.of(IidmXmlVersion.V_1_3, ImmutableSortedSet.of("1.0"),
                        IidmXmlVersion.V_1_4, ImmutableSortedSet.of("1.1"),
                        IidmXmlVersion.V_1_5, ImmutableSortedSet.of("1.2"),
                        IidmXmlVersion.V_1_6, ImmutableSortedSet.of("1.3"),
                        IidmXmlVersion.V_1_7, ImmutableSortedSet.of("1.4")),
                ImmutableMap.of("1.0", "http://www.powsybl.org/schema/iidm/ext/slack_terminal/1_0",
                        "1.1", "http://www.powsybl.org/schema/iidm/ext/slack_terminal/1_1",
                        "1.2", "http://www.powsybl.org/schema/iidm/ext/slack_terminal/1_2",
                        "1.3", "http://www.powsybl.org/schema/iidm/ext/slack_terminal/1_3",
                        "1.4", "http://www.powsybl.org/schema/iidm/ext/slack_terminal/1_4"));
    }

    @Override
    public InputStream getXsdAsStream() {
        return getClass().getResourceAsStream("/xsd/slackTerminal_V1_4.xsd");
    }

    @Override
    public List<InputStream> getXsdAsStreamList() {
        return ImmutableList.of(getClass().getResourceAsStream("/xsd/slackTerminal_V1_0.xsd"),
                getClass().getResourceAsStream("/xsd/slackTerminal_V1_1.xsd"),
                getClass().getResourceAsStream("/xsd/slackTerminal_V1_2.xsd"),
                getClass().getResourceAsStream("/xsd/slackTerminal_V1_3.xsd"),
                getClass().getResourceAsStream("/xsd/slackTerminal_V1_4.xsd"));
    }

    @Override
    public void write(SlackTerminal slackTerminal, XmlWriterContext context) throws XMLStreamException {
        Terminal terminal = slackTerminal.getTerminal();
        if (terminal != null) {
            TerminalRefXml.writeTerminalRefAttribute(slackTerminal.getTerminal(), (NetworkXmlWriterContext) context);
        }
    }

    @Override
    public SlackTerminal read(VoltageLevel voltageLevel, XmlReaderContext context) {
        NetworkXmlReaderContext networkContext = (NetworkXmlReaderContext) context;
        String id = networkContext.getAnonymizer().deanonymizeString(networkContext.getReader().getAttributeValue(null, "id"));
        String side = networkContext.getReader().getAttributeValue(null, "side");
        Terminal terminal = TerminalRefXml.readTerminalRef(voltageLevel.getNetwork(), id, side);
        voltageLevel.newExtension(SlackTerminalAdder.class).withTerminal(terminal).add();

        return voltageLevel.getExtension(SlackTerminal.class);
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
