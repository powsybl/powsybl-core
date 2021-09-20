/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.entsoe.util;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.xml.IidmXmlVersion;
import com.powsybl.iidm.xml.NetworkXmlReaderContext;
import com.powsybl.iidm.xml.NetworkXmlWriterContext;
import com.powsybl.iidm.xml.extensions.AbstractVersionableNetworkExtensionXmlSerializer;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class MergedXnodeXmlSerializer extends AbstractVersionableNetworkExtensionXmlSerializer<Line, MergedXnode> {

    public MergedXnodeXmlSerializer() {
        super("mergedXnode", MergedXnode.class, false, "mxn",
                ImmutableMap.<IidmXmlVersion, ImmutableSortedSet<String>>builder()
                        .put(IidmXmlVersion.V_1_0, ImmutableSortedSet.of("1.0", "1.1"))
                        .put(IidmXmlVersion.V_1_1, ImmutableSortedSet.of("1.0", "1.1"))
                        .put(IidmXmlVersion.V_1_2, ImmutableSortedSet.of("1.0", "1.1"))
                        .put(IidmXmlVersion.V_1_3, ImmutableSortedSet.of("1.0", "1.1"))
                        .put(IidmXmlVersion.V_1_4, ImmutableSortedSet.of("1.0", "1.1"))
                        .put(IidmXmlVersion.V_1_5, ImmutableSortedSet.of("1.0", "1.1"))
                        .put(IidmXmlVersion.V_1_6, ImmutableSortedSet.of("1.1", "1.1"))
                        .build(),
                ImmutableMap.<String, String>builder()
                        .put("1.0", "http://www.itesla_project.eu/schema/iidm/ext/merged_xnode/1_0")
                        .put("1.1", "http://www.powsybl.org/schema/iidm/ext/merged_xnode/1_1")
                        .build());
    }

    @Override
    public InputStream getXsdAsStream() {
        return getClass().getResourceAsStream("/xsd/V1_1/mergedXnode.xsd");
    }

    @Override
    public List<InputStream> getXsdAsStreamList() {
        return List.of(getClass().getResourceAsStream("/xsd/mergedXnode_V1_0.xsd"),
                getClass().getResourceAsStream("/xsd/mergedXnode_V1_1.xsd"));
    }

    @Override
    public void write(MergedXnode xnode, XmlWriterContext context) throws XMLStreamException {
        XmlUtil.writeDouble("rdp", xnode.getRdp(), context.getWriter());
        XmlUtil.writeDouble("xdp", xnode.getXdp(), context.getWriter());
        XmlUtil.writeDouble("xnodeP1", xnode.getXnodeP1(), context.getWriter());
        XmlUtil.writeDouble("xnodeQ1", xnode.getXnodeQ1(), context.getWriter());
        XmlUtil.writeDouble("xnodeP2", xnode.getXnodeP2(), context.getWriter());
        XmlUtil.writeDouble("xnodeQ2", xnode.getXnodeQ2(), context.getWriter());
        context.getWriter().writeAttribute("code", xnode.getCode());
        if (context instanceof NetworkXmlWriterContext) {
            NetworkXmlWriterContext networkXmlWriterContext = (NetworkXmlWriterContext) context;
            String extVersionStr = networkXmlWriterContext.getExtensionVersion("mergedXnode")
                    .orElseGet(() -> getVersion(networkXmlWriterContext.getVersion()));
            if ("1.1".equals(extVersionStr)) {
                writeLinesNames(xnode, context);
            }
        }
    }

    private void writeLinesNames(MergedXnode xnode, XmlWriterContext context) throws XMLStreamException {
        context.getWriter().writeAttribute("line1Name", xnode.getLine1Name());
        context.getWriter().writeAttribute("line2Name", xnode.getLine2Name());
    }

    @Override
    public MergedXnode read(Line line, XmlReaderContext context) {
        double rdp = XmlUtil.readDoubleAttribute(context.getReader(), "rdp");
        double xdp = XmlUtil.readDoubleAttribute(context.getReader(), "xdp");
        double xnodeP1 = XmlUtil.readDoubleAttribute(context.getReader(), "xnodeP1");
        double xnodeQ1 = XmlUtil.readDoubleAttribute(context.getReader(), "xnodeQ1");
        double xnodeP2 = XmlUtil.readDoubleAttribute(context.getReader(), "xnodeP2");
        double xnodeQ2 = XmlUtil.readDoubleAttribute(context.getReader(), "xnodeQ2");
        String code = context.getReader().getAttributeValue(null, "code");
        String line1Name = "";
        String line2Name = "";
        if (context instanceof NetworkXmlReaderContext) {
            NetworkXmlReaderContext networkXmlReaderContext = (NetworkXmlReaderContext) context;
            String extensionVersionStr = networkXmlReaderContext.getExtensionVersion(this).orElseThrow(AssertionError::new);
            if ("1.1".equals(extensionVersionStr)) {
                line1Name = context.getReader().getAttributeValue(null, "line1Name");
                line2Name = context.getReader().getAttributeValue(null, "line2Name");
            }
        }

        line.newExtension(MergedXnodeAdder.class).withRdp(rdp).withXdp(xdp).withXnodeP1(xnodeP1).withXnodeQ1(xnodeQ1)
                .withXnodeP2(xnodeP2).withXnodeQ2(xnodeQ2).withLine1Name(line1Name).withLine2Name(line2Name).withCode(code).add();
        return line.getExtension(MergedXnode.class);
    }
}
