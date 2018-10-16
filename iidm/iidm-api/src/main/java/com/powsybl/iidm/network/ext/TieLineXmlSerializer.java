/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.ext;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.Line;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;

/**
 * @author Yichen Tang <yichen.tang at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class TieLineXmlSerializer implements ExtensionXmlSerializer<Line, TieLineExt> {

    @Override
    public boolean hasSubElements() {
        return false;
    }

    @Override
    public InputStream getXsdAsStream() {
        return getClass().getResourceAsStream("/xsd/tieLine.xsd");
    }

    @Override
    public String getNamespaceUri() {
        return "http://www.itesla_project.eu/schema/iidm/ext/tieLine/1_0";
    }

    @Override
    public String getNamespacePrefix() {
        return "tl";
    }

    @Override
    public void write(TieLineExt extension, XmlWriterContext context) throws XMLStreamException {
        writeHalf(extension.getHalf1(), context, 1);
        writeHalf(extension.getHalf2(), context, 2);
    }

    private static void writeHalf(TieLineExt.HalfLine halfLine, XmlWriterContext context, int side) throws XMLStreamException {
        context.getWriter().writeAttribute("id_" + side, halfLine.getId());
        if (halfLine.getName() != null) {
            context.getWriter().writeAttribute("name_" + side, halfLine.getName());
        }
        XmlUtil.writeDouble("r_" + side, halfLine.getR(), context.getWriter());
        XmlUtil.writeDouble("x_" + side, halfLine.getX(), context.getWriter());
        XmlUtil.writeDouble("g1_" + side, halfLine.getG1(), context.getWriter());
        XmlUtil.writeDouble("b1_" + side, halfLine.getB1(), context.getWriter());
        XmlUtil.writeDouble("g2_" + side, halfLine.getG2(), context.getWriter());
        XmlUtil.writeDouble("b2_" + side, halfLine.getB2(), context.getWriter());
        XmlUtil.writeDouble("xnodeP_" + side, halfLine.getXnodeP(), context.getWriter());
        XmlUtil.writeDouble("xnodeQ_" + side, halfLine.getXnodeQ(), context.getWriter());
    }

    @Override
    public TieLineExt read(Line extendable, XmlReaderContext context) throws XMLStreamException {
        TieLineExt.HalfLineImpl hl1 = readHalf(context, 1);
        TieLineExt.HalfLineImpl hl2 = readHalf(context, 2);
        String code = context.getReader().getAttributeValue(null, "code");
        return new TieLineExt(extendable, code, hl1, hl2);
    }

    private TieLineExt.HalfLineImpl readHalf(XmlReaderContext context, int side) {
        String id = context.getReader().getAttributeValue(null, "id_" + side);
        String name = context.getReader().getAttributeValue(null, "name_" + side);
        double r = XmlUtil.readDoubleAttribute(context.getReader(), "r_" + side);
        double x = XmlUtil.readDoubleAttribute(context.getReader(), "x_" + side);
        double g1 = XmlUtil.readDoubleAttribute(context.getReader(), "g1_" + side);
        double b1 = XmlUtil.readDoubleAttribute(context.getReader(), "b1_" + side);
        double g2 = XmlUtil.readDoubleAttribute(context.getReader(), "g2_" + side);
        double b2 = XmlUtil.readDoubleAttribute(context.getReader(), "b2_" + side);
        double xnodeP = XmlUtil.readDoubleAttribute(context.getReader(), "xnodeP_" + side);
        double xnodeQ = XmlUtil.readDoubleAttribute(context.getReader(), "xnodeQ_" + side);
        TieLineExt.HalfLineImpl hl = new TieLineExt.HalfLineImpl();
        hl.setId(id);
        hl.setName(name);
        hl.setR(r);
        hl.setX(x);
        hl.setG1(g1);
        hl.setB1(b1);
        hl.setG2(g2);
        hl.setB2(b2);
        hl.setXnodeP(xnodeP);
        hl.setXnodeQ(xnodeQ);
        return hl;
    }

    @Override
    public String getExtensionName() {
        return "tieLine";
    }

    @Override
    public String getCategoryName() {
        return "network";
    }

    @Override
    public Class<? super TieLineExt> getExtensionClass() {
        return TieLineExt.class;
    }
}
