/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtensionXmlSerializer;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.xml.NetworkXmlReaderContext;
import com.powsybl.iidm.xml.NetworkXmlWriterContext;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class CgmesBoundarySideXmlSerializer extends AbstractExtensionXmlSerializer<DanglingLine, CgmesBoundarySide> {

    public CgmesBoundarySideXmlSerializer() {
        super("cgmesBoundarySide", "network", CgmesBoundarySide.class, false, "cgmesBoundarySide.xsd",
                "http://www.powsybl.org/schema/iidm/ext/cgmes_boundary_side/1_0", "cbs");
    }

    @Override
    public void write(CgmesBoundarySide extension, XmlWriterContext context) throws XMLStreamException {
        NetworkXmlWriterContext networkContext = (NetworkXmlWriterContext) context;
        XMLStreamWriter writer = networkContext.getWriter();
        XmlUtil.writeInt("boundarySide", extension.getBoundarySide(), writer);
    }

    @Override
    public CgmesBoundarySide read(DanglingLine extendable, XmlReaderContext context) {
        NetworkXmlReaderContext networkContext = (NetworkXmlReaderContext) context;
        XMLStreamReader reader = networkContext.getReader();
        extendable.newExtension(CgmesBoundarySideAdder.class)
                .setBoundarySide(XmlUtil.readIntAttribute(reader, "boundarySide"))
                .add();
        return extendable.getExtension(CgmesBoundarySide.class);
    }
}
