/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.commons.PowsyblException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import static com.powsybl.cgmes.model.CgmesNamespace.RDF_NAMESPACE;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public abstract class AbstractCgmesProfileWriter {
    protected final CgmesExportContext context;
    protected final XMLStreamWriter xmlWriter;
    protected final String cimNamespace;

    protected AbstractCgmesProfileWriter(CgmesExportContext context, XMLStreamWriter xmlWriter) {
        this.context = context;
        this.xmlWriter = xmlWriter;
        this.cimNamespace = context.getCim().getNamespace();
    }

    public static AbstractCgmesProfileWriter create(String profile, CgmesExportContext context, XMLStreamWriter xmlWriter) {
        if ("EQ".equals(profile)) {
            return new EquipmentExport(context, xmlWriter);
        } else if ("TP".equals(profile)) {
            return new TopologyExport(context, xmlWriter);
        } else if ("SSH".equals(profile)) {
            return new SteadyStateHypothesisExport(context, xmlWriter);
        } else if ("SV".equals(profile)) {
            return new StateVariablesExport(context, xmlWriter);
        }
        throw new PowsyblException("No CGMES profile writer for profile " + profile);
    }

    private static String toRdfId(String id) {
        // Handling ids: if received id is not prefixed by "_", add it to make it a valid RDF:Id
        // We have to be careful with "resource" and "about" references, and apply the same conversions
        return id.startsWith("_") ? id : "_" + id;
    }

    private static String toMasterResourceId(String id) {
        // Handling ids: if received id is prefixed by "_", remove it. Assuming it was added to comply with URN rules
        return id.startsWith("_") ? id.substring(1) : id;
    }

    public abstract void write();

    protected void writeStartId(String className, String id, boolean writeMasterResourceId) throws XMLStreamException {
        xmlWriter.writeStartElement(cimNamespace, className);
        // Writing mRID was optional in CIM 16, but is required since CIM 100
        // Only classes extending IdentifiedObject have an mRID
        // points of tables and curve data objects do not have mRID, although they have an RDF:ID
        xmlWriter.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, toRdfId(id));
        if (writeMasterResourceId) {
            xmlWriter.writeStartElement(cimNamespace, "IdentifiedObject.mRID");
            xmlWriter.writeCharacters(toMasterResourceId(id));
            xmlWriter.writeEndElement();
        }
    }

    protected void writeStartIdName(String className, String id, String name) throws XMLStreamException {
        writeStartId(className, id, true);
        xmlWriter.writeStartElement(cimNamespace, CgmesNames.NAME);
        xmlWriter.writeCharacters(name);
        xmlWriter.writeEndElement();
    }

    protected void writeReference(String refName, String referredId) throws XMLStreamException {
        xmlWriter.writeEmptyElement(cimNamespace, refName);
        xmlWriter.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + toRdfId(referredId));
    }

    protected void writeStartAbout(String className, String id) throws XMLStreamException {
        xmlWriter.writeStartElement(cimNamespace, className);
        xmlWriter.writeAttribute(RDF_NAMESPACE, CgmesNames.ABOUT, "#" + toRdfId(id));
    }

}
