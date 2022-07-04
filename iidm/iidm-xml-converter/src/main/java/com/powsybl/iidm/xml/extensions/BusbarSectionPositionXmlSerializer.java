/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import com.powsybl.iidm.network.extensions.BusbarSectionPositionAdder;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class BusbarSectionPositionXmlSerializer implements ExtensionXmlSerializer<BusbarSection, BusbarSectionPosition> {

    @Override
    public String getExtensionName() {
        return "busbarSectionPosition";
    }

    @Override
    public String getCategoryName() {
        return "network";
    }

    @Override
    public Class<? super BusbarSectionPosition> getExtensionClass() {
        return BusbarSectionPosition.class;
    }

    @Override
    public boolean hasSubElements() {
        return false;
    }

    @Override
    public InputStream getXsdAsStream() {
        return getClass().getResourceAsStream("/xsd/busbarSectionPosition.xsd");
    }

    @Override
    public String getNamespaceUri() {
        return "http://www.itesla_project.eu/schema/iidm/ext/busbarsectionposition/1_0";
    }

    @Override
    public String getNamespacePrefix() {
        return "bbsp";
    }

    @Override
    public void write(BusbarSectionPosition busbarSectionPosition, XmlWriterContext context) throws XMLStreamException {
        XmlUtil.writeInt("busbarIndex", busbarSectionPosition.getBusbarIndex(), context.getExtensionsWriter());
        XmlUtil.writeInt("sectionIndex", busbarSectionPosition.getSectionIndex(), context.getExtensionsWriter());
    }

    @Override
    public BusbarSectionPosition read(BusbarSection busbarSection, XmlReaderContext context) {
        int busbarIndex = XmlUtil.readIntAttribute(context.getReader(), "busbarIndex");
        int sectionIndex = XmlUtil.readIntAttribute(context.getReader(), "sectionIndex");
        busbarSection.newExtension(BusbarSectionPositionAdder.class)
            .withBusbarIndex(busbarIndex)
            .withSectionIndex(sectionIndex)
            .add();
        return busbarSection.getExtension(BusbarSectionPosition.class);
    }
}
