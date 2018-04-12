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
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.DanglingLine;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class XnodeXmlSerializer implements ExtensionXmlSerializer<DanglingLine, Xnode> {

    @Override
    public String getExtensionName() {
        return "xnode";
    }

    @Override
    public String getCategoryName() {
        return "network";
    }

    @Override
    public Class<Xnode> getExtensionClass() {
        return Xnode.class;
    }

    @Override
    public boolean hasSubElements() {
        return false;
    }

    @Override
    public InputStream getXsdAsStream() {
        return getClass().getResourceAsStream("/xsd/xnode.xsd");
    }

    @Override
    public String getNamespaceUri() {
        return "http://www.itesla_project.eu/schema/iidm/ext/xnode/1_0";
    }

    @Override
    public String getNamespacePrefix() {
        return "xn";
    }

    @Override
    public void write(Xnode xnode, XmlWriterContext context) throws XMLStreamException {
        context.getWriter().writeAttribute("code", xnode.getCode());
    }

    @Override
    public Xnode read(DanglingLine dl, XmlReaderContext context) {
        String code = context.getReader().getAttributeValue(null, "code");
        return new Xnode(dl, code);
    }
}
