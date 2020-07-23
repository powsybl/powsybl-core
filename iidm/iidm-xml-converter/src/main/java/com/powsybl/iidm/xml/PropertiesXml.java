/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.iidm.xml;

import javax.xml.stream.XMLStreamException;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.xml.util.IidmXmlUtil;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public final class PropertiesXml {

    static final String PROPERTY = "property";

    private static final String NAME = "name";
    private static final String TYPE = "type";
    private static final String VALUE = "value";

    private static final String TYPE_STRING = "string";
    private static final String TYPE_INTEGER = "integer";
    private static final String TYPE_DOUBLE = "double";
    private static final String TYPE_BOOLEAN = "boolean";

    public static void write(Identifiable<?> identifiable, NetworkXmlWriterContext context) throws XMLStreamException {
        if (identifiable.hasProperty()) {
            for (String name : identifiable.getPropertyNames()) {
                Identifiable.PropertyType type = identifiable.getPropertyType(name);
                switch (type) {
                    case STRING:
                        String value = identifiable.getStringProperty(name);
                        writeProperty(name, TYPE_STRING, value, context);
                        break;
                    case INTEGER:
                        value = Integer.toString(identifiable.getIntegerProperty(name));
                        writeProperty(name, TYPE_INTEGER, value, context);
                        break;
                    case DOUBLE:
                        value = Double.toString(identifiable.getDoubleProperty(name));
                        writeProperty(name, TYPE_DOUBLE, value, context);
                        break;
                    case BOOLEAN:
                        value = Boolean.toString(identifiable.getBooleanProperty(name));
                        writeProperty(name, TYPE_BOOLEAN, value, context);
                        break;
                }
            }
        }
    }

    private static void writeProperty(String name, String type, String value, NetworkXmlWriterContext context) throws XMLStreamException {
        context.getWriter().writeEmptyElement(context.getVersion().getNamespaceURI(), PROPERTY);
        context.getWriter().writeAttribute(NAME, name);
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_4, context, () -> {
            try {
                context.getWriter().writeAttribute(TYPE, type);
            } catch (XMLStreamException e) {
                throw new UncheckedXmlStreamException(e);
            }
        });
        context.getWriter().writeAttribute(VALUE, value);
    }

    public static void read(Identifiable<?> identifiable, NetworkXmlReaderContext context) {
        assert context.getReader().getLocalName().equals(PROPERTY);
        String name = context.getReader().getAttributeValue(null, NAME);

        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_3, context, () -> {
            String value = context.getReader().getAttributeValue(null, VALUE);
            identifiable.setStringProperty(name, value);
        });
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_4, context, () -> {
            String type = context.getReader().getAttributeValue(null, TYPE);
            String value = context.getReader().getAttributeValue(null, VALUE);
            switch (type) {
                case TYPE_STRING:
                    identifiable.setStringProperty(name, value);
                    break;
                case TYPE_INTEGER:
                    identifiable.setIntegerProperty(name, Integer.parseInt(value));
                    break;
                case TYPE_DOUBLE:
                    identifiable.setDoubleProperty(name, Double.parseDouble(value));
                    break;
                case TYPE_BOOLEAN:
                    identifiable.setBooleanProperty(name, Boolean.parseBoolean(value));
                    break;
                default:
                    throw new PowsyblException("Property type " + type + " is not supported");
            }
        });
    }

    private PropertiesXml() {
    }
}
