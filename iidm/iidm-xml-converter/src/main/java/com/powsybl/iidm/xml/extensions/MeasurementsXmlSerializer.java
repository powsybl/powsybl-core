/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtensionXmlSerializer;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.io.TreeDataWriter;
import com.powsybl.commons.extensions.XmlReaderContext;
import com.powsybl.commons.extensions.XmlWriterContext;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.extensions.Measurement;
import com.powsybl.iidm.network.extensions.MeasurementAdder;
import com.powsybl.iidm.network.extensions.Measurements;
import com.powsybl.iidm.network.extensions.MeasurementsAdder;

import java.util.Map;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
@AutoService(ExtensionXmlSerializer.class)
public class MeasurementsXmlSerializer<C extends Connectable<C>> extends AbstractExtensionXmlSerializer<C, Measurements<C>> {

    private static final String MEASUREMENT_ROOT_ELEMENT = "measurement";
    private static final String MEASUREMENT_ARRAY_ELEMENT = "measurements";
    private static final String VALUE = "value";
    public static final String PROPERTY_ROOT_ELEMENT = "property";
    private static final String PROPERTY_ARRAY_ELEMENT = "properties";

    public MeasurementsXmlSerializer() {
        super("measurements", "network", Measurements.class, "measurements.xsd",
                "http://www.powsybl.org/schema/iidm/ext/measurements/1_0", "m");
    }

    @Override
    public Map<String, String> getArrayNameToSingleNameMap() {
        return Map.of(MEASUREMENT_ARRAY_ELEMENT, MEASUREMENT_ROOT_ELEMENT,
                PROPERTY_ARRAY_ELEMENT, PROPERTY_ROOT_ELEMENT);
    }

    @Override
    public void write(Measurements<C> extension, XmlWriterContext context) {
        TreeDataWriter writer = context.getWriter();
        writer.writeStartNodes(MEASUREMENT_ARRAY_ELEMENT);
        for (Measurement measurement : extension.getMeasurements()) {
            writer.writeStartNode(getNamespaceUri(), MEASUREMENT_ROOT_ELEMENT);
            if (measurement.getId() != null) {
                writer.writeStringAttribute("id", measurement.getId());
            }
            writer.writeEnumAttribute("type", measurement.getType());
            writer.writeEnumAttribute("side", measurement.getSide());
            writer.writeDoubleAttribute(VALUE, measurement.getValue());
            writer.writeDoubleAttribute("standardDeviation", measurement.getStandardDeviation());
            writer.writeBooleanAttribute("valid", measurement.isValid());
            writeProperties(measurement, writer);
            writer.writeEndNode();
        }
        writer.writeEndNodes();
    }

    private void writeProperties(Measurement measurement, TreeDataWriter writer) {
        writer.writeStartNodes(PROPERTY_ARRAY_ELEMENT);
        for (String name : measurement.getPropertyNames()) {
            writer.writeStartNode(getNamespaceUri(), PROPERTY_ROOT_ELEMENT);
            writer.writeStringAttribute("name", name);
            writer.writeStringAttribute(VALUE, measurement.getProperty(name));
            writer.writeEndNode();
        }
        writer.writeEndNodes();
    }

    @Override
    public Measurements<C> read(C extendable, XmlReaderContext context) {
        MeasurementsAdder<C> measurementsAdder = extendable.newExtension(MeasurementsAdder.class);
        Measurements<C> measurements = measurementsAdder.add();
        var reader = context.getReader();
        reader.readChildNodes(elementName -> {
            if (elementName.equals(MEASUREMENT_ROOT_ELEMENT)) {
                MeasurementAdder adder = measurements.newMeasurement()
                        .setId(reader.readStringAttribute("id"))
                        .setType(reader.readEnumAttribute("type", Measurement.Type.class))
                        .setValue(reader.readDoubleAttribute(VALUE))
                        .setStandardDeviation(reader.readDoubleAttribute("standardDeviation"))
                        .setValid(reader.readBooleanAttribute("valid"))
                        .setSide(reader.readEnumAttribute("side", Measurement.Side.class));
                reader.readChildNodes(subElementName -> {
                    if (subElementName.equals(PROPERTY_ROOT_ELEMENT)) {
                        adder.putProperty(reader.readStringAttribute("name"),
                                reader.readStringAttribute(VALUE));
                        reader.readEndNode();
                    } else {
                        throw new PowsyblException("Unexpected element: " + subElementName);
                    }
                });
                adder.add();
            } else {
                throw new PowsyblException("Unexpected element: " + elementName);
            }
        });
        return measurements;
    }

    @Override
    public boolean isSerializable(Measurements<C> extension) {
        return !extension.getMeasurements().isEmpty();
    }
}
