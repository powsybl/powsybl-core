/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.cgmes.model.CgmesMetadataModel;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.iidm.network.Network;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Common class for writing CGMES XML files.
 *
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */
public abstract class AbstractCgmesWriter {

    protected static final double SCIENTIFIC_FORMAT_MIN_THRESHOLD = 1E-10;
    private static final DecimalFormatSymbols DOUBLE_FORMAT_SYMBOLS = new DecimalFormatSymbols(Locale.US);
    private static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("0.##############", DOUBLE_FORMAT_SYMBOLS);
    private static final DecimalFormat SCIENTIFIC_FORMAT = new DecimalFormat("0.######E0", DOUBLE_FORMAT_SYMBOLS);

    protected final XMLStreamWriter writer;
    protected final CgmesExportContext context;
    protected final String cimNamespace;
    protected final String euNamespace;

    protected AbstractCgmesWriter(XMLStreamWriter writer, CgmesExportContext context) {
        this.writer = writer;
        this.context = context;
        cimNamespace = context.getCim().getNamespace();
        euNamespace = context.getCim().getEuNamespace();
    }

    /**
     * Write the RDF root element that includes rdf, md, cim and entsoe/eu namespaces definition.
     */
    public void writeRdfRoot() throws XMLStreamException {
        //TODO move the method from CgmesExportUtil here when other writers inheriting AbstractCgmesWriter will be created
        CgmesExportUtil.writeRdfRoot(context, writer);
    }

    /**
     * Write the network metadata into the FullModel element in accordance with IEC-61970-552.
     */
    public void writeModelDescription(Network network, CgmesSubset subset, CgmesMetadataModel model) throws XMLStreamException {
        //TODO move the method from CgmesExportUtil here when other writers inheriting AbstractCgmesWriter will be created
        CgmesExportUtil.writeModelDescription(network, subset, writer, model, context);
    }

    /**
     * Write the closing tag of the RDF root element.
     */
    public void writeEndDocument() throws XMLStreamException {
        writer.writeEndDocument();
    }

    /**
     * Write the CGMES opening tag of the given element including the rdf:Id resource.
     */
    public void writeStartId(String className, String id, boolean writeMasterResourceId) throws XMLStreamException {
        //TODO move the method from CgmesExportUtil here when other writers inheriting AbstractCgmesWriter will be created
        CgmesExportUtil.writeStartId(className, id, writeMasterResourceId, writer, context);
    }

    /**
     * Write the given CGMES attribute.
     */
    public void writeAttribute(String namespace, String attributeName, String attributeValue) throws XMLStreamException {
        //TODO move the method from CgmesExportUtil here when other writers inheriting AbstractCgmesWriter will be created
        CgmesExportUtil.writeAttribute(namespace, attributeName, attributeValue, writer);
    }

    /**
     * Write the given CGMES association or enumerate attribute.
     */
    public void writeResource(String namespace, String resourceName, String resourceValue) throws XMLStreamException {
        //TODO move the method from CgmesExportUtil here when other writers inheriting AbstractCgmesWriter will be created
        CgmesExportUtil.writeResource(namespace, resourceName, resourceValue, writer);
    }

    /**
     * Write the closing tag of the current element.
     */
    public void writeEnd() throws XMLStreamException {
        writer.writeEndElement();
    }

    /**
     * Convert the given id to an acceptable rdf:about reference.
     */
    public String toRdfAbout(String id) {
        //TODO move the method from CgmesExportUtil here when other writers inheriting AbstractCgmesWriter will be created
        return CgmesExportUtil.toRdfAbout(id, context);
    }

    /**
     * Format the given boolean to a String value.
     */
    public String format(boolean value) {
        return String.valueOf(value);
    }

    /**
     * Format the given int to a String value.
     */
    public String format(int value) {
        //TODO move the method from CgmesExportUtil here when other writers inheriting AbstractCgmesWriter will be created
        return CgmesExportUtil.format(value);
    }

    /**
     * Format the given double to a String value.
     */
    public String format(double value) {
        // CGMES expects xsd:float values
        double floatValue;
        if (value > Float.MAX_VALUE) {
            floatValue = Float.MAX_VALUE;
        } else if (value < -Float.MAX_VALUE) {
            floatValue = -Float.MAX_VALUE;
        } else {
            floatValue = fixValue(value, 0.0);
        }

        // Always use scientific format for extreme values
        if (0.0 < Math.abs(floatValue) && Math.abs(floatValue) < SCIENTIFIC_FORMAT_MIN_THRESHOLD
            || Math.abs(floatValue) >= Float.MAX_VALUE) {
            return SCIENTIFIC_FORMAT.format(floatValue);
        } else {
            return DOUBLE_FORMAT.format(floatValue);
        }
    }

    /**
     * Make sure the given double is not NaN. Return the default value if that is the case.
     */
    public double fixValue(double value, double defaultValue) {
        return Double.isNaN(value) ? defaultValue : value;
    }

    /**
     * Make sure the given name is no longer than 32 characters. Truncate it if that's the case.
     */
    public String getValidName(String name) {
        //TODO move the method from CgmesExportUtil here when other writers inheriting AbstractCgmesWriter will be created
        return CgmesExportUtil.getValidName(name);
    }

    /**
     * Check if the given value is not equal to the minimum or maximum double value.
     */
    public boolean isNotMinOrMaxValue(double value) {
        //TODO move the method from CgmesExportUtil here when other writers inheriting AbstractCgmesWriter will be created
        return value != -Double.MAX_VALUE && value != Double.MAX_VALUE;
    }

}
