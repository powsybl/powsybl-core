/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
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
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.GeneratorEntsoeCategory;
import com.powsybl.iidm.network.extensions.GeneratorEntsoeCategoryAdder;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class GeneratorEntsoeCategoryXmlSerializer implements ExtensionXmlSerializer<Generator, GeneratorEntsoeCategory> {

    @Override
    public String getExtensionName() {
        return "entsoeCategory";
    }

    @Override
    public String getCategoryName() {
        return "network";
    }

    @Override
    public Class<? super GeneratorEntsoeCategory> getExtensionClass() {
        return GeneratorEntsoeCategory.class;
    }

    @Override
    public boolean hasSubElements() {
        return true;
    }

    @Override
    public InputStream getXsdAsStream() {
        return getClass().getResourceAsStream("/xsd/generatorEntsoeCategory.xsd");
    }

    @Override
    public String getNamespaceUri() {
        return "http://www.itesla_project.eu/schema/iidm/ext/generator_entsoe_category/1_0";
    }

    @Override
    public String getNamespacePrefix() {
        return "gec";
    }

    @Override
    public void write(GeneratorEntsoeCategory entsoeCategory, XmlWriterContext context) throws XMLStreamException {
        context.getExtensionsWriter().writeCharacters(Integer.toString(entsoeCategory.getCode()));
    }

    @Override
    public GeneratorEntsoeCategory read(Generator generator, XmlReaderContext context) throws XMLStreamException {
        int code = Integer.parseInt(XmlUtil.readUntilEndElement(getExtensionName(), context.getReader(), null));
        generator.newExtension(GeneratorEntsoeCategoryAdder.class).withCode(code).add();
        return generator.getExtension(GeneratorEntsoeCategory.class);
    }
}
