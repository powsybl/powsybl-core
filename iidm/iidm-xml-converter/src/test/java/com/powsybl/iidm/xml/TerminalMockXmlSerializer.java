/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.TerminalMockExt;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;

import static com.powsybl.iidm.xml.AbstractXmlConverterTest.getVersionDir;
import static com.powsybl.iidm.xml.IidmXmlConstants.CURRENT_IIDM_XML_VERSION;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class TerminalMockXmlSerializer implements ExtensionXmlSerializer<Load, TerminalMockExt> {

    @Override
    public boolean hasSubElements() {
        return true;
    }

    @Override
    public InputStream getXsdAsStream() {
        return getClass().getResourceAsStream(getVersionDir(CURRENT_IIDM_XML_VERSION) + "xsd/terminalMock.xsd");
    }

    @Override
    public String getNamespaceUri() {
        return "http://www.itesla_project.eu/schema/iidm/ext/terminal_mock/1_1";
    }

    @Override
    public String getNamespacePrefix() {
        return "mock";
    }

    @Override
    public void write(TerminalMockExt extension, XmlWriterContext context) throws XMLStreamException {
        NetworkXmlWriterContext networkContext = (NetworkXmlWriterContext) context;
        TerminalRefXml.writeTerminalRef(extension.getTerminal(), networkContext, getNamespaceUri(), "terminal", context.getExtensionsWriter());
    }

    @Override
    public TerminalMockExt read(Load extendable, XmlReaderContext context) throws XMLStreamException {
        NetworkXmlReaderContext networkContext = (NetworkXmlReaderContext) context;
        TerminalMockExt terminalMockExt = new TerminalMockExt(extendable);
        XmlUtil.readUntilEndElement(getExtensionName(), networkContext.getReader(), () -> {
            if (networkContext.getReader().getLocalName().equals("terminal")) {
                String id = networkContext.getAnonymizer().deanonymizeString(networkContext.getReader().getAttributeValue(null, "id"));
                String side = networkContext.getReader().getAttributeValue(null, "side");
                networkContext.getEndTasks().add(() -> {
                    Network network = extendable.getTerminal().getVoltageLevel().getSubstation().getNetwork();
                    terminalMockExt.setTerminal(TerminalRefXml.readTerminalRef(network, id, side));
                });
            } else {
                throw new AssertionError("Unexpected element: " + networkContext.getReader().getLocalName());
            }
        });
        return terminalMockExt;
    }

    @Override
    public String getExtensionName() {
        return "terminalMock";
    }

    @Override
    public String getCategoryName() {
        return "network";
    }

    @Override
    public Class<? super TerminalMockExt> getExtensionClass() {
        return TerminalMockExt.class;
    }
}
