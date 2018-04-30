/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.entsoe.util;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.Line;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class MergedXnodeXmlSerializer implements ExtensionXmlSerializer<Line, MergedXnode> {

    @Override
    public String getExtensionName() {
        return "mergedXnode";
    }

    @Override
    public String getCategoryName() {
        return "network";
    }

    @Override
    public Class<MergedXnode> getExtensionClass() {
        return MergedXnode.class;
    }

    @Override
    public boolean hasSubElements() {
        return false;
    }

    @Override
    public InputStream getXsdAsStream() {
        return getClass().getResourceAsStream("/xsd/mergedXnode.xsd");
    }

    @Override
    public String getNamespaceUri() {
        return "http://www.itesla_project.eu/schema/iidm/ext/merged_xnode/1_0";
    }

    @Override
    public String getNamespacePrefix() {
        return "mxn";
    }

    @Override
    public void write(MergedXnode xnode, XmlWriterContext context) throws XMLStreamException {
        XmlUtil.writeFloat("rdp", xnode.getRdp(), context.getWriter());
        XmlUtil.writeFloat("xdp", xnode.getXdp(), context.getWriter());
        XmlUtil.writeFloat("xnodeP1", xnode.getXnodeP1(), context.getWriter());
        XmlUtil.writeFloat("xnodeQ1", xnode.getXnodeQ1(), context.getWriter());
        XmlUtil.writeFloat("xnodeP2", xnode.getXnodeP2(), context.getWriter());
        XmlUtil.writeFloat("xnodeQ2", xnode.getXnodeQ2(), context.getWriter());
        context.getWriter().writeAttribute("code", xnode.getCode());
    }

    @Override
    public MergedXnode read(Line line, XmlReaderContext context) {
        float rdp = XmlUtil.readFloatAttribute(context.getReader(), "rdp");
        float xdp = XmlUtil.readFloatAttribute(context.getReader(), "xdp");
        float xnodeP1 = XmlUtil.readFloatAttribute(context.getReader(), "xnodeP1");
        float xnodeQ1 = XmlUtil.readFloatAttribute(context.getReader(), "xnodeQ1");
        float xnodeP2 = XmlUtil.readFloatAttribute(context.getReader(), "xnodeP2");
        float xnodeQ2 = XmlUtil.readFloatAttribute(context.getReader(), "xnodeQ2");
        String code = context.getReader().getAttributeValue(null, "code");
        return new MergedXnode(line, rdp, xdp, xnodeP1, xnodeQ1, xnodeP2, xnodeQ2, code);
    }
}
