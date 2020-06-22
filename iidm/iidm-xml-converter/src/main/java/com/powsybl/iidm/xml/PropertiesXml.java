/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.iidm.xml;

import javax.xml.stream.XMLStreamException;

import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.xml.util.IidmXmlUtil;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public final class PropertiesXml {

    static final String PROPERTY = "property";

    private static final String NAME = "name";
    private static final String VALUE = "value";

    public static void write(Identifiable<?> identifiable, NetworkXmlWriterContext context) throws XMLStreamException {
        if (identifiable.hasProperty()) {
            for (String name : identifiable.getPropertyNames()) {
                if (!name.equals(Substation.GEOGRAPHICAL_TAGS_KEY)) {
                    String value = identifiable.getProperty(name);
                    context.getWriter().writeEmptyElement(context.getVersion().getNamespaceURI(), PROPERTY);
                    context.getWriter().writeAttribute(NAME, name);
                    context.getWriter().writeAttribute(VALUE, value);
                } else {
                    IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_4, context, () -> {
                        String value = context.getAnonymizer().anonymizeString(identifiable.getProperty(name));
                        try {
                            context.getWriter().writeEmptyElement(context.getVersion().getNamespaceURI(), PROPERTY);
                            context.getWriter().writeAttribute(NAME, name);
                            context.getWriter().writeAttribute(VALUE, value);
                        } catch (XMLStreamException e) {
                            throw new UncheckedXmlStreamException(e);
                        }
                    });
                }
            }
        }
    }

    public static void read(Identifiable identifiable, NetworkXmlReaderContext context) {
        assert context.getReader().getLocalName().equals(PROPERTY);
        String name = context.getReader().getAttributeValue(null, NAME);
        String value = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, VALUE));
        identifiable.setProperty(name, value);
    }

    private PropertiesXml() {
    }
}
