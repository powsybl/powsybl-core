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
        return updatesByMasterResourceId.computeIfAbsent(masterResourceId, id -> new ObjectUpdate(this, className, id));
    }

    /**
     * Start describing a single CGMES object in a buffer of its own, for instance
     * {@code newUpdates(CgmesNames.TERMINAL, terminalId).value("ACDCTerminal.connected", true).updates()}.
     *
     * <p>This is the entry point of the buffers built per change: a mapping describes the objects a change
     * affects and returns the buffer, which the caller merges, or drops if the change turns out to be
     * unsupported.</p>
     */
    static ObjectUpdate newUpdates(String className, String masterResourceId) {
        return new PartialSshUpdates().object(className, masterResourceId);
    }

    /** Return a new buffer holding every property of the given buffers, merged in the order they are given. */
    static PartialSshUpdates merge(PartialSshUpdates... parts) {
        PartialSshUpdates merged = new PartialSshUpdates();
        for (PartialSshUpdates part : parts) {
            merged.mergeFrom(part);
        }
        return merged;
    }

    boolean isEmpty() {
        return updatesByMasterResourceId.isEmpty();
    }

    /**
     * Copy every property of the given buffer into this one, as if its objects had been updated here.
     *
     * <p>This is how a change is committed once it is known to be entirely exportable: it is collected into a
     * buffer of its own and merged here only then, so that a change which turns out to be unsupported halfway
     * through leaves nothing behind. Objects and properties this buffer already holds keep their position, and a
     * property set on both sides takes the merged value, exactly as a second direct update would have done, so
     * merging change by change writes the same file as updating this buffer directly would have.</p>
     */
    void mergeFrom(PartialSshUpdates other) {
        other.updatesByMasterResourceId.forEach((masterResourceId, objectUpdate) ->
                object(objectUpdate.className, masterResourceId).properties.putAll(objectUpdate.properties));
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

        private final PartialSshUpdates buffer;
        private final String className;
        private final String masterResourceId;
        private final Map<String, Property> properties = new LinkedHashMap<>();

        private ObjectUpdate(PartialSshUpdates buffer, String className, String masterResourceId) {
            this.buffer = buffer;
            this.className = className;
            this.masterResourceId = masterResourceId;
        }

        /** Move on to another object of the same buffer, so that a change affecting several objects reads as one chain. */
        ObjectUpdate object(String className, String masterResourceId) {
            return buffer.object(className, masterResourceId);
        }

        /** The buffer this object belongs to, which is what a mapping returns once it has described everything. */
        PartialSshUpdates updates() {
            return buffer;
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
