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
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.extensions.DiscreteMeasurement;
import com.powsybl.iidm.network.extensions.DiscreteMeasurementAdder;
import com.powsybl.iidm.network.extensions.DiscreteMeasurements;
import com.powsybl.iidm.network.extensions.DiscreteMeasurementsAdder;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class DiscreteMeasurementsXmlSerializer<I extends Identifiable<I>> extends AbstractExtensionXmlSerializer<I, DiscreteMeasurements<I>> {

    private static final String DISCRETE_MEASUREMENT = "discreteMeasurement";

    public DiscreteMeasurementsXmlSerializer() {
        super("discreteMeasurements", "network", DiscreteMeasurements.class, true,
                "discreteMeasurements.xsd", "http://www.powsybl.org/schema/iidm/ext/discrete_measurements/1_0", "dm");
    }

    @Override
    public void write(DiscreteMeasurements<I> extension, XmlWriterContext context) throws XMLStreamException {
        XMLStreamWriter writer = context.getWriter();
        for (DiscreteMeasurement discreteMeasurement : extension.getDiscreteMeasurements()) {
            writer.writeStartElement(getNamespaceUri(), DISCRETE_MEASUREMENT);
            if (discreteMeasurement.getId() != null) {
                writer.writeAttribute("id", discreteMeasurement.getId());
            }
            writer.writeAttribute("type", discreteMeasurement.getType().toString());
            if (discreteMeasurement.getTapChanger() != null) {
                writer.writeAttribute("tapChanger", discreteMeasurement.getTapChanger().toString());
            }
            if (!discreteMeasurement.getValueAsString().equals(String.valueOf(discreteMeasurement.getValueAsInt()))) {
                writer.writeAttribute("stringValue", discreteMeasurement.getValueAsString());
            }
            if (discreteMeasurement.getValueAsInt() != -1) {
                XmlUtil.writeInt("intValue", discreteMeasurement.getValueAsInt(), writer);
            }
            writer.writeAttribute("valid", String.valueOf(discreteMeasurement.isValid()));
            for (String name : discreteMeasurement.getPropertyNames()) {
                writer.writeEmptyElement(getNamespaceUri(), "property");
                writer.writeAttribute("name", name);
                writer.writeAttribute("value", discreteMeasurement.getProperty(name));
            }
            writer.writeEndElement();
        }
    }

    @Override
    public DiscreteMeasurements<I> read(I extendable, XmlReaderContext context) throws XMLStreamException {
        extendable.newExtension(DiscreteMeasurementsAdder.class).add();
        DiscreteMeasurements<I> discreteMeasurements = extendable.getExtension(DiscreteMeasurements.class);
        XMLStreamReader reader = context.getReader();
        XmlUtil.readUntilEndElement(getExtensionName(), reader, () -> {
            if (reader.getLocalName().equals(DISCRETE_MEASUREMENT)) {
                readDiscreteMeasurement(discreteMeasurements, reader);
            } else {
                throw new PowsyblException("Unexpected element: " + reader.getLocalName());
            }
        });
        return discreteMeasurements;
    }

    private static <I extends Identifiable<I>> void readDiscreteMeasurement(DiscreteMeasurements<I> discreteMeasurements, XMLStreamReader reader) throws XMLStreamException {
        DiscreteMeasurementAdder adder = discreteMeasurements.newDiscreteMeasurement()
                .setId(reader.getAttributeValue(null, "id"))
                .setType(DiscreteMeasurement.Type.valueOf(reader.getAttributeValue(null, "type")))
                .setValid(XmlUtil.readBoolAttribute(reader, "valid"));
        String tapChanger = reader.getAttributeValue(null, "tapChanger");
        if (tapChanger != null) {
            adder.setTapChanger(DiscreteMeasurement.TapChanger.valueOf(reader.getAttributeValue(null, tapChanger)));
        }
        String stringValue = reader.getAttributeValue(null, "stringValue");
        if (stringValue != null) {
            adder.setStringValue(stringValue);
        }
        String intValue = reader.getAttributeValue(null, "intValue");
        if (intValue != null) {
            adder.setIntValue(Integer.valueOf(intValue));
        }
        XmlUtil.readUntilEndElement(DISCRETE_MEASUREMENT, reader, () -> {
            if (reader.getLocalName().equals("property")) {
                adder.putProperty(reader.getAttributeValue(null, "name"),
                        reader.getAttributeValue(null, "value"));
            } else {
                throw new PowsyblException("Unexpected element: " + reader.getLocalName());
            }
        });
        adder.add();
    }

    @Override
    public boolean isSerializable(DiscreteMeasurements<I> extension) {
        return !extension.getDiscreteMeasurements().isEmpty();
    }
}
