/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.xml;

import com.powsybl.commons.exceptions.UncheckedXmlStreamException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class XmlWriter implements HierarchicalDataWriter {

    private final XMLStreamWriter writer;

    public XmlWriter(XMLStreamWriter writer) {
        this.writer = Objects.requireNonNull(writer);
    }

    @Override
    public void writeStartElement(String ns, String name) {
        try {
            writer.writeStartElement(ns, name);
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    @Override
    public void writeEmptyElement(String ns, String name) {
        try {
            writer.writeEmptyElement(ns, name);
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    @Override
    public void writeEndElement() {
        try {
            writer.writeEndElement();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    @Override
    public void writeElementContent(String value) {
        try {
            writer.writeCharacters(value);
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    @Override
    public void writeStringAttribute(String name, String value) {
        try {
            if (value != null) {
                writer.writeAttribute(name, value);
            }
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    @Override
    public void writeFloatAttribute(String name, float value) {
        try {
            XmlUtil.writeFloat(name, value, writer);
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    @Override
    public void writeDoubleAttribute(String name, double value) {
        try {
            XmlUtil.writeDouble(name, value, writer);
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    @Override
    public void writeDoubleAttribute(String name, double value, double absentValue) {
        try {
            XmlUtil.writeOptionalDouble(name, value, absentValue, writer);
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    @Override
    public void writeIntAttribute(String name, int value) {
        try {
            XmlUtil.writeInt(name, value, writer);
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    @Override
    public void writeIntAttribute(String name, int value, int absentValue) {
        try {
            XmlUtil.writeOptionalInt(name, value, absentValue, writer);
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    @Override
    public <E extends Enum<E>> void writeEnumAttribute(String name, E value) {
        try {
            XmlUtil.writeOptionalEnum(name, value, writer);
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    @Override
    public void writeBooleanAttribute(String name, boolean value) {
        try {
            writer.writeAttribute(name, Boolean.toString(value));
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    @Override
    public void writeBooleanAttribute(String name, boolean value, boolean absentValue) {
        try {
            XmlUtil.writeOptionalBoolean(name, value, absentValue, writer);
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    @Override
    public void close() {
        try {
            writer.writeEndDocument();
            writer.close();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }
}
