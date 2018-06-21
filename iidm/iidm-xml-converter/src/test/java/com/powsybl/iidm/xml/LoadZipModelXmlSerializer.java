/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
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
import com.powsybl.iidm.network.test.LoadZipModel;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class LoadZipModelXmlSerializer implements ExtensionXmlSerializer<Load, LoadZipModel> {

    @Override
    public String getExtensionName() {
        return "loadZipModel";
    }

    @Override
    public String getCategoryName() {
        return "network";
    }

    @Override
    public Class<? super LoadZipModel> getExtensionClass() {
        return LoadZipModel.class;
    }

    @Override
    public boolean hasSubElements() {
        return false;
    }

    @Override
    public InputStream getXsdAsStream() {
        return getClass().getResourceAsStream("/xsd/loadZipModel.xsd");
    }

    @Override
    public String getNamespaceUri() {
        return "http://www.itesla_project.eu/schema/iidm/ext/loadzipmodel/1_0";
    }

    @Override
    public String getNamespacePrefix() {
        return "extZip";
    }

    @Override
    public void write(LoadZipModel zipModel, XmlWriterContext context) throws XMLStreamException {
        XmlUtil.writeDouble("a1", zipModel.getA1(), context.getWriter());
        XmlUtil.writeDouble("a2", zipModel.getA2(), context.getWriter());
        XmlUtil.writeDouble("a3", zipModel.getA3(), context.getWriter());
        XmlUtil.writeDouble("a4", zipModel.getA4(), context.getWriter());
        XmlUtil.writeDouble("a5", zipModel.getA5(), context.getWriter());
        XmlUtil.writeDouble("a6", zipModel.getA6(), context.getWriter());
        XmlUtil.writeDouble("v0", zipModel.getV0(), context.getWriter());
    }

    @Override
    public LoadZipModel read(Load load, XmlReaderContext context) {
        double a1 = XmlUtil.readDoubleAttribute(context.getReader(), "a1");
        double a2 = XmlUtil.readDoubleAttribute(context.getReader(), "a2");
        double a3 = XmlUtil.readDoubleAttribute(context.getReader(), "a3");
        double a4 = XmlUtil.readDoubleAttribute(context.getReader(), "a4");
        double a5 = XmlUtil.readDoubleAttribute(context.getReader(), "a5");
        double a6 = XmlUtil.readDoubleAttribute(context.getReader(), "a6");
        double v0 = XmlUtil.readDoubleAttribute(context.getReader(), "v0");
        return new LoadZipModel(load, a1, a2, a3, a4, a5, a6, v0);
    }
}
