/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.model.statement;

import com.powsybl.cgmes.model.CgmesNames;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.Objects;
import java.util.function.UnaryOperator;

import static com.powsybl.cgmes.model.CgmesNamespace.RDF_NAMESPACE;

/**
 * Write {@link CgmesObject} instances in RDF/XML, as elements with an rdf:about reference.
 *
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public final class CgmesObjectXmlWriter {

    private final XMLStreamWriter writer;
    private final String cimNamespace;
    private final UnaryOperator<String> toRdfAbout;

    /**
     * @param writer the XML writer
     * @param cimNamespace the CIM namespace of the class and of the predicates
     * @param toRdfAbout convert the subject of an object to its rdf:about reference
     */
    public CgmesObjectXmlWriter(XMLStreamWriter writer, String cimNamespace, UnaryOperator<String> toRdfAbout) {
        this.writer = Objects.requireNonNull(writer);
        this.cimNamespace = Objects.requireNonNull(cimNamespace);
        this.toRdfAbout = Objects.requireNonNull(toRdfAbout);
    }

    /**
     * Write the given object.
     */
    public void write(CgmesObject cgmesObject) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, cgmesObject.type());
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ABOUT, toRdfAbout.apply(cgmesObject.subject()));
        for (CgmesProperty property : cgmesObject.properties()) {
            if (property.resource()) {
                writer.writeEmptyElement(cimNamespace, property.predicate());
                writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, property.object());
            } else {
                writer.writeStartElement(cimNamespace, property.predicate());
                writer.writeCharacters(property.object());
                writer.writeEndElement();
            }
        }
        writer.writeEndElement();
    }
}
