/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.iidm.xml;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.ValidationLevel;
import com.powsybl.iidm.xml.util.IidmXmlUtil;

import javax.xml.stream.XMLStreamException;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public final class PropertiesXml {

    static final String PROPERTY = "property";

    private static final String NAME = "name";
    private static final String VALUE = "value";

    public static void write(Identifiable<?> identifiable, NetworkXmlWriterContext context) throws XMLStreamException {
        if (identifiable.hasProperty()) {
            for (String name : IidmXmlUtil.sortedNames(identifiable.getPropertyNames(), context.getOptions())) {
                String value = identifiable.getProperty(name);
                context.getWriter().writeEmptyElement(context.getVersion().getNamespaceURI(identifiable.getNetwork().getValidationLevel() == ValidationLevel.LOADFLOW), PROPERTY);
                context.getWriter().writeAttribute(NAME, name);
                context.getWriter().writeAttribute(VALUE, value);
            }
        }
    }

    public static void read(Identifiable identifiable, NetworkXmlReaderContext context) {
        assert context.getReader().getLocalName().equals(PROPERTY);
        String name = context.getReader().getAttributeValue(null, NAME);
        String value = context.getReader().getAttributeValue(null, VALUE);
        identifiable.setProperty(name, value);
    }

    private PropertiesXml() {
    }
}
