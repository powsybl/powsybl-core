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
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.extensions.Measurement;
import com.powsybl.iidm.network.extensions.MeasurementAdder;
import com.powsybl.iidm.network.extensions.Measurements;
import com.powsybl.iidm.network.extensions.MeasurementsAdder;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

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
    public void write(Measurements<C> extension, XmlWriterContext context) throws XMLStreamException {
        XMLStreamWriter writer = context.getWriter();
        for (Measurement measurement : extension.getMeasurements()) {
            boolean hasProperty = !measurement.getPropertyNames().isEmpty();
            if (hasProperty) {
                writer.writeStartElement(getNamespaceUri(), MEASUREMENT);
            } else {
                writer.writeEmptyElement(getNamespaceUri(), MEASUREMENT);
            }
            if (measurement.getId() != null) {
                writer.writeAttribute("id", measurement.getId());
            }
            writer.writeAttribute("type", measurement.getType().toString());
            if (measurement.getSide() != null) {
                writer.writeAttribute("side", measurement.getSide().toString());
            }
            XmlUtil.writeDouble(VALUE, measurement.getValue(), writer);
            XmlUtil.writeDouble("standardDeviation", measurement.getStandardDeviation(), writer);
            writer.writeAttribute("valid", String.valueOf(measurement.isValid()));
            for (String name : measurement.getPropertyNames()) {
                writer.writeEmptyElement(getNamespaceUri(), "property");
                writer.writeAttribute("name", name);
                writer.writeAttribute(VALUE, measurement.getProperty(name));
            }
            if (hasProperty) {
                writer.writeEndElement();
            }
        }
    }

    @Override
    public Measurements<C> read(C extendable, XmlReaderContext context) throws XMLStreamException {
        MeasurementsAdder<C> measurementsAdder = extendable.newExtension(MeasurementsAdder.class);
        Measurements<C> measurements = measurementsAdder.add();
        XMLStreamReader reader = context.getReader();
        XmlUtil.readUntilEndElement(getExtensionName(), reader, () -> {
            if (reader.getLocalName().equals(MEASUREMENT)) {
                MeasurementAdder adder = measurements.newMeasurement()
                        .setId(reader.getAttributeValue(null, "id"))
                        .setType(Measurement.Type.valueOf(reader.getAttributeValue(null, "type")))
                        .setValue(XmlUtil.readOptionalDoubleAttribute(reader, VALUE))
                        .setStandardDeviation(XmlUtil.readOptionalDoubleAttribute(reader, "standardDeviation"))
                        .setValid(XmlUtil.readBoolAttribute(reader, "valid"));
                String side = reader.getAttributeValue(null, "side");
                if (side != null) {
                    adder.setSide(Measurement.Side.valueOf(side));
                }
                XmlUtil.readUntilEndElement(MEASUREMENT, reader, () -> {
                    if (reader.getLocalName().equals("property")) {
                        adder.putProperty(reader.getAttributeValue(null, "name"),
                                reader.getAttributeValue(null, VALUE));
                    } else {
                        throw new PowsyblException("Unexpected element: " + reader.getLocalName());
                    }
                });
                adder.add();
            } else {
                throw new PowsyblException("Unexpected element: " + reader.getLocalName());
            }
        });
        return measurements;
    }

    @Override
    public boolean isSerializable(Measurements<C> extension) {
        return !extension.getMeasurements().isEmpty();
    }
}
