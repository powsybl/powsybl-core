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
import com.powsybl.iidm.network.Substation;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class EntsoeAreaXmlSerializer implements ExtensionXmlSerializer<Substation, EntsoeArea> {

    @Override
    public String getExtensionName() {
        return "entsoeArea";
    }

    @Override
    public String getCategoryName() {
        return "network";
    }

    @Override
    public Class<EntsoeArea> getExtensionClass() {
        return EntsoeArea.class;
    }

    @Override
    public boolean hasSubElements() {
        return true;
    }

    @Override
    public InputStream getXsdAsStream() {
        return getClass().getResourceAsStream("/xsd/entsoeArea.xsd");
    }

    @Override
    public String getNamespaceUri() {
        return "http://www.itesla_project.eu/schema/iidm/ext/entsoe_area/1_0";
    }

    @Override
    public String getNamespacePrefix() {
        return "ea";
    }

    @Override
    public void write(EntsoeArea country, XmlWriterContext context) throws XMLStreamException {
        context.getWriter().writeCharacters(country.getCode().name());
    }

    @Override
    public EntsoeArea read(Substation substation, XmlReaderContext context) throws XMLStreamException {
        EntsoeGeographicalCode code = EntsoeGeographicalCode.valueOf(XmlUtil.readUntilEndElement(getExtensionName(), context.getReader(), null));
        return new EntsoeArea(substation, code);
    }
}
