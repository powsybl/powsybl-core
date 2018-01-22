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
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.test.LoadBarExt;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class LoadBarXmlSerializer implements ExtensionXmlSerializer<Load, LoadBarExt> {

    @Override
    public String getExtensionName() {
        return "loadBar";
    }

    @Override
    public String getCategoryName() {
        return "network";
    }

    @Override
    public Class<? super LoadBarExt> getExtensionClass() {
        return LoadBarExt.class;
    }

    @Override
    public boolean hasSubElements() {
        return false;
    }

    @Override
    public InputStream getXsdAsStream() {
        return getClass().getResourceAsStream("/xsd/loadBar.xsd");
    }

    @Override
    public String getNamespaceUri() {
        return "http://www.itesla_project.eu/schema/iidm/ext/loadbar/1_0";
    }

    @Override
    public String getNamespacePrefix() {
        return "bar";
    }

    @Override
    public void write(LoadBarExt loadBar, XmlWriterContext context) throws XMLStreamException {
    }

    @Override
    public LoadBarExt read(Load load, XmlReaderContext context) {
        return new LoadBarExt(load);
    }
}
