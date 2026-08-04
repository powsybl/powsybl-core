/**
 * Copyright (c) 2026, Elia Group (https://www.eliagroup.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.cgmes.model.CgmesNames;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.powsybl.cgmes.model.CgmesNamespace.RDF_NAMESPACE;

/**
 * The CIM properties that a partial Steady State Hypothesis export has to write, buffered per CGMES object.
 *
 * <p>The mapping between IIDM changes and CGMES properties is not one to one: several IIDM changes can describe
 * the same CGMES object (the active and the reactive setpoint of a load), and several IIDM objects can describe a
 * single CGMES object (the voltage setpoint of a generator and of a shunt compensator both describe a
 * RegulatingControl). Buffering the properties per master resource identifier guarantees that every object is
 * described exactly once and that its description is internally consistent, whatever the order and the number of
 * the recorded changes.</p>
 *
 * <p>Objects are written in the order in which they were first updated, and the properties of an object in the
 * order in which they were first set, so that the export is reproducible.</p>
 *
 * @author Nico Westerbeck {@literal <nico.westerbeck at 50hertz.com>}
 */
class PartialSshUpdates {

    private final Map<String, ObjectUpdate> updatesByMasterResourceId = new LinkedHashMap<>();

    /**
     * Return the description of the CGMES object with the given master resource identifier, creating an empty one
     * if this object has not been updated yet.
     *
     * @param className the CIM class of the object, used as the element name
     * @param masterResourceId the CGMES master resource identifier (mRID) of the object
     */
    ObjectUpdate object(String className, String masterResourceId) {
        return updatesByMasterResourceId.computeIfAbsent(masterResourceId, id -> new ObjectUpdate(className, id));
    }

    boolean isEmpty() {
        return updatesByMasterResourceId.isEmpty();
    }

    void write(String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (ObjectUpdate objectUpdate : updatesByMasterResourceId.values()) {
            objectUpdate.write(cimNamespace, writer, context);
        }
    }

    /**
     * The properties to write for a single CGMES object. Setting a property that has already been set replaces its
     * value, keeping its original position.
     */
    static final class ObjectUpdate {

        private final String className;
        private final String masterResourceId;
        private final Map<String, Property> properties = new LinkedHashMap<>();

        private ObjectUpdate(String className, String masterResourceId) {
            this.className = className;
            this.masterResourceId = masterResourceId;
        }

        ObjectUpdate value(String property, boolean value) {
            return literal(property, CgmesExportUtil.format(value));
        }

        ObjectUpdate value(String property, int value) {
            return literal(property, CgmesExportUtil.format(value));
        }

        ObjectUpdate value(String property, double value) {
            return literal(property, CgmesExportUtil.format(value));
        }

        /**
         * Set a property pointing to a CIM enumeration literal, for instance
         * {@code enumValue("VsConverter.qPccControl", "VsQpccControlKind", "voltagePcc")}.
         */
        ObjectUpdate enumValue(String property, String enumerationName, String literal) {
            properties.put(property, new Property(enumerationName + "." + literal, true));
            return this;
        }

        private ObjectUpdate literal(String property, String value) {
            properties.put(property, new Property(value, false));
            return this;
        }

        private void write(String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
            CgmesExportUtil.writeStartAbout(className, masterResourceId, cimNamespace, writer, context);
            for (Map.Entry<String, Property> entry : properties.entrySet()) {
                Property property = entry.getValue();
                if (property.enumeration()) {
                    writer.writeEmptyElement(cimNamespace, entry.getKey());
                    writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, cimNamespace + property.value());
                } else {
                    writer.writeStartElement(cimNamespace, entry.getKey());
                    writer.writeCharacters(property.value());
                    writer.writeEndElement();
                }
            }
            writer.writeEndElement();
        }
    }

    private record Property(String value, boolean enumeration) {
    }
}
