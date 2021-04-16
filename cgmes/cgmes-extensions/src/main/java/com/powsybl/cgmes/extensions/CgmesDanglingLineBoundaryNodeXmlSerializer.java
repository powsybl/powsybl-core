/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
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
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.DanglingLine;

import javax.xml.stream.XMLStreamException;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class CgmesDanglingLineBoundaryNodeXmlSerializer extends AbstractExtensionXmlSerializer<DanglingLine, CgmesDanglingLineBoundaryNode> {

    public CgmesDanglingLineBoundaryNodeXmlSerializer() {
        super("cgmesDanglingLineBoundaryNode", "network", CgmesDanglingLineBoundaryNode.class,
                false, "cgmesDanglingLineBoundaryNode.xsd",
                "http://www.powsybl.org/schema/iidm/ext/cgmes_dangling_line_boundary_node/1_0", "cdlbn");
    }

    @Override
    public void write(CgmesDanglingLineBoundaryNode extension, XmlWriterContext context) throws XMLStreamException {
        context.getWriter().writeAttribute("isHvdc", String.valueOf(extension.isHvdc()));
        context.getWriter().writeAttribute("lineEnergyIdentificationCodeEic", extension.getLineEnergyIdentificationCodeEic());
    }

    @Override
    public CgmesDanglingLineBoundaryNode read(DanglingLine extendable, XmlReaderContext context) throws XMLStreamException {
        boolean isHvdc = XmlUtil.readBoolAttribute(context.getReader(), "isHvdc");
        String lineEnergyIdentificationCodeEic = context.getReader().getAttributeValue(null, "lineEnergyIdentificationCodeEic");
        extendable.newExtension(CgmesDanglingLineBoundaryNodeAdder.class).setHvdc(isHvdc).setLineEnergyIdentificationCodeEic(lineEnergyIdentificationCodeEic).add();
        return extendable.getExtension(CgmesDanglingLineBoundaryNode.class);
    }
}
