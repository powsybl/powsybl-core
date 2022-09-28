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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class XmlWriter implements TreeDataWriter {

    private final XMLStreamWriter writer;

    private String currentNodeNs;
    private String currentNodeName;
    private final List<String> names = new ArrayList<>();
    private final List<String> values = new ArrayList<>();
    private final List<String> prefixes = new ArrayList<>();
    private final List<String> namespaces = new ArrayList<>();

    public XmlWriter(XMLStreamWriter writer) {
        this.writer = Objects.requireNonNull(writer);
    }

    @Override
    public void writeStartNodes(String name) {
        // nothing to do
    }

    @Override
    public void writeEndNodes() {
        // nothing to do
    }

    @Override
    public void writeStartNode(String ns, String name) {
        try {
            if (currentNodeName != null) {
                writePrefixes();
                writer.writeStartElement(currentNodeNs, currentNodeName);
                flushAttributes();
            }
            currentNodeNs = ns;
            currentNodeName = name;
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    private void writePrefixes() throws XMLStreamException {
        for (int i = 0; i < prefixes.size(); i++) {
            writer.setPrefix(prefixes.get(i), namespaces.get(i));
        }
    }

    private void flushAttributes() throws XMLStreamException {
        for (int i = 0; i < prefixes.size(); i++) {
            writer.writeNamespace(prefixes.get(i), namespaces.get(i));
        }
        prefixes.clear();
        namespaces.clear();
        for (int i = 0; i < names.size(); i++) {
            writer.writeAttribute(names.get(i), values.get(i));
        }
        names.clear();
        values.clear();
    }

    @Override
    public void writeEndNode() {
        try {
            if (currentNodeName != null) {
                writePrefixes();
                writer.writeEmptyElement(currentNodeNs, currentNodeName);
                flushAttributes();
                currentNodeNs = null;
                currentNodeName = null;
            } else {
                writer.writeEndElement();
            }
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    @Override
    public void writeNs(String prefix, String ns) {
        prefixes.add(prefix);
        namespaces.add(ns);
    }

    @Override
    public void writeNodeContent(String value) {
        try {
            if (currentNodeName != null) {
                writePrefixes();
                writer.writeStartElement(currentNodeNs, currentNodeName);
                flushAttributes();
                currentNodeName = null;
                currentNodeNs = null;
            }
            writer.writeCharacters(value);
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    @Override
    public void writeStringAttribute(String name, String value) {
        if (value != null) {
            names.add(name);
            values.add(value);
        }
    }

    @Override
    public void writeFloatAttribute(String name, float value) {
        if (!Float.isNaN(value)) {
            names.add(name);
            values.add(Float.toString(value));
        }
    }

    @Override
    public void writeDoubleAttribute(String name, double value) {
        if (!Double.isNaN(value)) {
            names.add(name);
            values.add(Double.toString(value));
        }
    }

    @Override
    public void writeDoubleAttribute(String name, double value, double absentValue) {
        if (!Double.isNaN(value) && value != absentValue) {
            names.add(name);
            values.add(Double.toString(value));
        }
    }

    @Override
    public void writeIntAttribute(String name, int value) {
        names.add(name);
        values.add(Integer.toString(value));
    }

    @Override
    public void writeIntAttribute(String name, int value, int absentValue) {
        if (value != absentValue) {
            names.add(name);
            values.add(Integer.toString(value));
        }
    }

    @Override
    public <E extends Enum<E>> void writeEnumAttribute(String name, E value) {
        if (value != null) {
            names.add(name);
            values.add(value.name());
        }
    }

    @Override
    public void writeBooleanAttribute(String name, boolean value) {
        names.add(name);
        values.add(Boolean.toString(value));
    }

    @Override
    public void writeBooleanAttribute(String name, boolean value, boolean absentValue) {
        if (value != absentValue) {
            names.add(name);
            values.add(Boolean.toString(value));
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
