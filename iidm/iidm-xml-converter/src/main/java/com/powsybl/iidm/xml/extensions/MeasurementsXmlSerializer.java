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
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlWriter;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.extensions.Measurement;
import com.powsybl.iidm.network.extensions.MeasurementAdder;
import com.powsybl.iidm.network.extensions.Measurements;
import com.powsybl.iidm.network.extensions.MeasurementsAdder;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class MeasurementsXmlSerializer<C extends Connectable<C>> extends AbstractExtensionXmlSerializer<C, Measurements<C>> {

    private static final String MEASUREMENT = "measurement";
    private static final String VALUE = "value";

    public MeasurementsXmlSerializer() {
        super("measurements", "network", Measurements.class, true, "measurements.xsd",
                "http://www.powsybl.org/schema/iidm/ext/measurements/1_0", "m");
    }

    @Override
    public void write(Measurements<C> extension, XmlWriterContext context) {
        XmlWriter writer = context.getWriter();
        for (Measurement measurement : extension.getMeasurements()) {
            boolean hasProperty = !measurement.getPropertyNames().isEmpty();
            if (hasProperty) {
                writer.writeStartNode(getNamespaceUri(), MEASUREMENT);
            } else {
                writer.writeEmptyNode(getNamespaceUri(), MEASUREMENT);
            }
            if (measurement.getId() != null) {
                writer.writeStringAttribute("id", measurement.getId());
            }
            writer.writeEnumAttribute("type", measurement.getType());
            writer.writeEnumAttribute("side", measurement.getSide());
            writer.writeDoubleAttribute(VALUE, measurement.getValue());
            writer.writeDoubleAttribute("standardDeviation", measurement.getStandardDeviation());
            writer.writeBooleanAttribute("valid", measurement.isValid());
            for (String name : measurement.getPropertyNames()) {
                writer.writeEmptyNode(getNamespaceUri(), "property");
                writer.writeStringAttribute("name", name);
                writer.writeStringAttribute(VALUE, measurement.getProperty(name));
            }
            if (hasProperty) {
                writer.writeEndNode();
            }
        }
    }

    @Override
    public Measurements<C> read(C extendable, XmlReaderContext context) {
        MeasurementsAdder<C> measurementsAdder = extendable.newExtension(MeasurementsAdder.class);
        Measurements<C> measurements = measurementsAdder.add();
        var reader = context.getReader();
        reader.readUntilEndNode(getExtensionName(), () -> {
            if (reader.getNodeName().equals(MEASUREMENT)) {
                MeasurementAdder adder = measurements.newMeasurement()
                        .setId(reader.readStringAttribute("id"))
                        .setType(reader.readEnumAttribute("type", Measurement.Type.class))
                        .setValue(reader.readDoubleAttribute(VALUE))
                        .setStandardDeviation(reader.readDoubleAttribute("standardDeviation"))
                        .setValid(reader.readBooleanAttribute("valid"))
                        .setSide(reader.readEnumAttribute("side", Measurement.Side.class));
                reader.readUntilEndNode(MEASUREMENT, () -> {
                    if (reader.getNodeName().equals("property")) {
                        adder.putProperty(reader.readStringAttribute("name"),
                                reader.readStringAttribute(VALUE));
                    } else {
                        throw new PowsyblException("Unexpected element: " + reader.getNodeName());
                    }
                });
                adder.add();
            } else {
                throw new PowsyblException("Unexpected element: " + reader.getNodeName());
            }
        });
        return measurements;
    }

    @Override
    public boolean isSerializable(Measurements<C> extension) {
        return !extension.getMeasurements().isEmpty();
    }
}
