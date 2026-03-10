/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.xml;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.commons.io.AbstractTreeDataWriter;
import org.apache.commons.lang3.StringUtils;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class XmlWriter extends AbstractTreeDataWriter {

    private final XMLStreamWriter writer;

    private String currentNodeNamespace;
    private String currentNodeName;
    private final List<String> names = new ArrayList<>();
    private final List<String> values = new ArrayList<>();
    private final List<String> prefixes = new ArrayList<>();
    private final List<String> namespaces = new ArrayList<>();
    private final Map<String, Namespace> extensionNamespaces = new LinkedHashMap<>();

    public XmlWriter(OutputStream os, String indent, Charset charset, String rootNamespaceURI, String rootPrefix) throws XMLStreamException {
        this.writer = XmlUtil.initializeWriter(!StringUtils.isEmpty(indent), indent,
                Objects.requireNonNull(os), Objects.requireNonNull(charset));
        this.namespaces.add(Objects.requireNonNull(rootNamespaceURI));
        this.prefixes.add(Objects.requireNonNull(rootPrefix));
    }

    @Override
    public void writeStartNodes() {
        // nothing to do
    }

    @Override
    public void writeEndNodes() {
        // nothing to do
    }

    @Override
    public void writeStartNode(String namespace, String name) {
        try {
            if (currentNodeName != null) {
                writePrefixes();
                writer.writeStartElement(currentNodeNamespace, currentNodeName);
                flushAttributes();
            }
            currentNodeNamespace = namespace;
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
                writer.writeEmptyElement(currentNodeNamespace, currentNodeName);
                flushAttributes();
                currentNodeNamespace = null;
                currentNodeName = null;
            } else {
                writer.writeEndElement();
            }
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    @Override
    public void writeNamespace(String prefix, String namespace) {
        prefixes.add(prefix);
        namespaces.add(namespace);
    }

    @Override
    public void writeNodeContent(String value) {
        try {
            if (currentNodeName != null) {
                writePrefixes();
                writer.writeStartElement(currentNodeNamespace, currentNodeName);
                flushAttributes();
                currentNodeName = null;
                currentNodeNamespace = null;
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
    public void writeIntArrayAttribute(String name, Collection<Integer> values) {
        if (!values.isEmpty()) {
            writeStringAttribute(name, values.stream()
                    .map(i -> Integer.toString(i))
                    .collect(Collectors.joining(",")));
        }
    }

    @Override
    public void writeStringArrayAttribute(String name, Collection<String> values) {
        if (!values.isEmpty()) {
            writeStringAttribute(name, String.join(",", values));
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

    @Override
    public void setVersions(Map<String, String> versions) {
        Objects.requireNonNull(versions).keySet()
                .stream()
                .map(extensionName -> {
                    Namespace namespace = extensionNamespaces.get(extensionName);
                    if (namespace == null) {
                        throw new PowsyblException("No namespace known for extension " + extensionName);
                    }
                    return namespace;
                })
                .forEach(namespace -> {
                    prefixes.add(namespace.prefix());
                    namespaces.add(namespace.uri());
                });
    }

    public void setExtensionNamespace(String extensionName, String namespaceUri, String namespacePrefix) {
        extensionNamespaces.put(extensionName, new Namespace(namespaceUri, namespacePrefix));
    }

    private record Namespace(String uri, String prefix) {
    }
}
