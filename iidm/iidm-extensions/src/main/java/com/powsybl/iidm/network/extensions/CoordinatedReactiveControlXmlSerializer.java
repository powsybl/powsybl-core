/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.Generator;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class CoordinatedReactiveControlXmlSerializer implements ExtensionXmlSerializer<Generator, CoordinatedReactiveControl> {

    @Override
    public boolean hasSubElements() {
        return false;
    }

    @Override
    public InputStream getXsdAsStream() {
        return getClass().getResourceAsStream("/xsd/coordinatedReactiveControl.xsd");
    }

    @Override
    public String getNamespaceUri() {
        return "http://www.powsybl.org/schema/iidm/ext/coordinated_reactive_control/1_0";
    }

    @Override
    public String getNamespacePrefix() {
        return "crc";
    }

    @Override
    public void write(CoordinatedReactiveControl extension, XmlWriterContext context) throws XMLStreamException {
        context.getExtensionsWriter().writeAttribute("qPercent", Double.toString(extension.getQPercent()));
    }

    @Override
    public CoordinatedReactiveControl read(Generator extendable, XmlReaderContext context) throws XMLStreamException {
        double qPercent = XmlUtil.readDoubleAttribute(context.getReader(), "qPercent");
        return new CoordinatedReactiveControl(extendable, qPercent);
    }

    @Override
    public String getExtensionName() {
        return "coordinatedReactiveControl";
    }

    @Override
    public String getCategoryName() {
        return "network";
    }

    @Override
    public Class<? super CoordinatedReactiveControl> getExtensionClass() {
        return CoordinatedReactiveControl.class;
    }
}
