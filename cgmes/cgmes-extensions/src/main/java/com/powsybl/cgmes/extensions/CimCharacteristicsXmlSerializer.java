/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtensionXmlSerializer;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.commons.xml.XmlWriter;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.xml.NetworkXmlReaderContext;
import com.powsybl.iidm.xml.NetworkXmlWriterContext;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class CimCharacteristicsXmlSerializer extends AbstractExtensionXmlSerializer<Network, CimCharacteristics> {

    public CimCharacteristicsXmlSerializer() {
        super("cimCharacteristics", "network", CimCharacteristics.class, false, "cimCharacteristics.xsd",
                "http://www.powsybl.org/schema/iidm/ext/cim_characteristics/1_0", "cc");
    }

    @Override
    public void write(CimCharacteristics extension, XmlWriterContext context) throws XMLStreamException {
        NetworkXmlWriterContext networkContext = (NetworkXmlWriterContext) context;
        XmlWriter writer = networkContext.getWriter();
        writer.writeEnumAttribute("topologyKind", extension.getTopologyKind());
        writer.writeIntAttribute("cimVersion", extension.getCimVersion());
    }

    @Override
    public CimCharacteristics read(Network extendable, XmlReaderContext context) throws XMLStreamException {
        NetworkXmlReaderContext networkContext = (NetworkXmlReaderContext) context;
        XMLStreamReader reader = networkContext.getReader();
        extendable.newExtension(CimCharacteristicsAdder.class)
                .setTopologyKind(CgmesTopologyKind.valueOf(reader.getAttributeValue(null, "topologyKind")))
                .setCimVersion(XmlUtil.readIntAttribute(reader, "cimVersion"))
                .add();
        return extendable.getExtension(CimCharacteristics.class);
    }
}
