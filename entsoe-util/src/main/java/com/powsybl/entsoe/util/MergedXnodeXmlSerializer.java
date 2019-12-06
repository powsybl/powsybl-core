/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.entsoe.util;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtensionXmlSerializer;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.Line;

import javax.xml.stream.XMLStreamException;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class MergedXnodeXmlSerializer extends AbstractExtensionXmlSerializer<Line, MergedXnode> {

    public MergedXnodeXmlSerializer() {
        super("mergedXnode", "network", MergedXnode.class, false, "mergedXnode.xsd",
                "http://www.itesla_project.eu/schema/iidm/ext/merged_xnode/1_0", "mxn");
    }

    @Override
    public void write(MergedXnode xnode, XmlWriterContext context) throws XMLStreamException {
        XmlUtil.writeFloat("rdp", xnode.getRdp(), context.getExtensionsWriter());
        XmlUtil.writeFloat("xdp", xnode.getXdp(), context.getExtensionsWriter());
        XmlUtil.writeDouble("xnodeP1", xnode.getXnodeP1(), context.getExtensionsWriter());
        XmlUtil.writeDouble("xnodeQ1", xnode.getXnodeQ1(), context.getExtensionsWriter());
        XmlUtil.writeDouble("xnodeP2", xnode.getXnodeP2(), context.getExtensionsWriter());
        XmlUtil.writeDouble("xnodeQ2", xnode.getXnodeQ2(), context.getExtensionsWriter());
        context.getExtensionsWriter().writeAttribute("code", xnode.getCode());
    }

    @Override
    public MergedXnode read(Line line, XmlReaderContext context) {
        float rdp = XmlUtil.readFloatAttribute(context.getReader(), "rdp");
        float xdp = XmlUtil.readFloatAttribute(context.getReader(), "xdp");
        double xnodeP1 = XmlUtil.readDoubleAttribute(context.getReader(), "xnodeP1");
        double xnodeQ1 = XmlUtil.readDoubleAttribute(context.getReader(), "xnodeQ1");
        double xnodeP2 = XmlUtil.readDoubleAttribute(context.getReader(), "xnodeP2");
        double xnodeQ2 = XmlUtil.readDoubleAttribute(context.getReader(), "xnodeQ2");
        String code = context.getReader().getAttributeValue(null, "code");
        return new MergedXnode(line, rdp, xdp, xnodeP1, xnodeQ1, xnodeP2, xnodeQ2, "", "", code);
    }
}
