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
public class EntsoeCountryXmlSerializer implements ExtensionXmlSerializer<Substation, EntsoeCountry> {

    @Override
    public String getExtensionName() {
        return "entsoeCountry";
    }

    @Override
    public String getCategoryName() {
        return "network";
    }

    @Override
    public Class<EntsoeCountry> getExtensionClass() {
        return EntsoeCountry.class;
    }

    @Override
    public boolean hasSubElements() {
        return true;
    }

    @Override
    public InputStream getXsdAsStream() {
        return getClass().getResourceAsStream("/xsd/entsoeCountry.xsd");
    }

    @Override
    public String getNamespaceUri() {
        return "http://www.itesla_project.eu/schema/iidm/ext/entsoe_country/1_0";
    }

    @Override
    public String getNamespacePrefix() {
        return "ec";
    }

    @Override
    public void write(EntsoeCountry country, XmlWriterContext context) throws XMLStreamException {
        context.getWriter().writeCharacters(country.getCode().name());
    }

    @Override
    public EntsoeCountry read(Substation substation, XmlReaderContext context) throws XMLStreamException {
        EntsoeGeographicalCode code = EntsoeGeographicalCode.valueOf(XmlUtil.readUntilEndElement(getExtensionName(), context.getReader(), null));
        return new EntsoeCountry(substation, code);
    }
}
