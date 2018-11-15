/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import static com.powsybl.iidm.xml.IidmXmlConstants.IIDM_URI;

import javax.xml.stream.XMLStreamException;

import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.VoltageLevel;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class NodeBreakerViewInternalConnectionXml {

    static final NodeBreakerViewInternalConnectionXml INSTANCE = new NodeBreakerViewInternalConnectionXml();
    static final String ROOT_ELEMENT_NAME = "internalConnection";

    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    protected void write(int node1, int node2, NetworkXmlWriterContext context) throws XMLStreamException {
        context.getWriter().writeEmptyElement(IIDM_URI, getRootElementName());
        context.getWriter().writeAttribute("node1", Integer.toString(node1));
        context.getWriter().writeAttribute("node2", Integer.toString(node2));
    }

    protected void read(VoltageLevel vl, NetworkXmlReaderContext context) {
        int node1 = XmlUtil.readIntAttribute(context.getReader(), "node1");
        int node2 = XmlUtil.readIntAttribute(context.getReader(), "node2");
        vl.getNodeBreakerView().newInternalConnection().setNode1(node1).setNode2(node2).add();
    }
}
