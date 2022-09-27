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
import com.powsybl.commons.xml.TreeDataReader;
import com.powsybl.commons.xml.TreeDataWriter;
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.extensions.DiscreteMeasurement;
import com.powsybl.iidm.network.extensions.DiscreteMeasurementAdder;
import com.powsybl.iidm.network.extensions.DiscreteMeasurements;
import com.powsybl.iidm.network.extensions.DiscreteMeasurementsAdder;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class DiscreteMeasurementsXmlSerializer<I extends Identifiable<I>> extends AbstractExtensionXmlSerializer<I, DiscreteMeasurements<I>> {

    private static final String DISCRETE_MEASUREMENT = "discreteMeasurement";
    private static final String VALUE = "value";

    public DiscreteMeasurementsXmlSerializer() {
        super("discreteMeasurements", "network", DiscreteMeasurements.class, true,
                "discreteMeasurements.xsd", "http://www.powsybl.org/schema/iidm/ext/discrete_measurements/1_0", "dm");
    }

    @Override
    public void write(DiscreteMeasurements<I> extension, XmlWriterContext context) {
        TreeDataWriter writer = context.getWriter();
        for (DiscreteMeasurement discreteMeasurement : extension.getDiscreteMeasurements()) {
            writer.writeStartNode(getNamespaceUri(), DISCRETE_MEASUREMENT);
            writer.writeStringAttribute("id", discreteMeasurement.getId());
            writer.writeEnumAttribute("type", discreteMeasurement.getType());
            writer.writeEnumAttribute("tapChanger", discreteMeasurement.getTapChanger());
            writer.writeEnumAttribute("valueType", discreteMeasurement.getValueType());
            switch (discreteMeasurement.getValueType()) {
                case BOOLEAN:
                    writer.writeBooleanAttribute(VALUE, discreteMeasurement.getValueAsBoolean());
                    break;
                case INT:
                    writer.writeIntAttribute(VALUE, discreteMeasurement.getValueAsInt());
                    break;
                case STRING:
                    writer.writeStringAttribute(VALUE, discreteMeasurement.getValueAsString());
                    break;
                default:
                    throw new PowsyblException("Unsupported serialization for value type: " + discreteMeasurement.getValueType());
            }
            writer.writeStringAttribute("valid", String.valueOf(discreteMeasurement.isValid()));
            for (String name : discreteMeasurement.getPropertyNames()) {
                writer.writeStartNode(getNamespaceUri(), "property");
                writer.writeStringAttribute("name", name);
                writer.writeStringAttribute(VALUE, discreteMeasurement.getProperty(name));
                writer.writeEndNode();
            }
            writer.writeEndNode();
        }
    }

    @Override
    public DiscreteMeasurements<I> read(I extendable, XmlReaderContext context) {
        DiscreteMeasurementsAdder<I> adder = extendable.newExtension(DiscreteMeasurementsAdder.class);
        DiscreteMeasurements<I> discreteMeasurements = adder.add();
        TreeDataReader reader = context.getReader();
        reader.readUntilEndNode(getExtensionName(), () -> {
            if (reader.getNodeName().equals(DISCRETE_MEASUREMENT)) {
                readDiscreteMeasurement(discreteMeasurements, reader);
            } else {
                throw new PowsyblException("Unexpected element: " + reader.getNodeName());
            }
        });
        return discreteMeasurements;
    }

    private static <I extends Identifiable<I>> void readDiscreteMeasurement(DiscreteMeasurements<I> discreteMeasurements, TreeDataReader reader) {
        DiscreteMeasurementAdder adder = discreteMeasurements.newDiscreteMeasurement()
                .setId(reader.readStringAttribute("id"))
                .setType(reader.readEnumAttribute("type", DiscreteMeasurement.Type.class))
                .setValid(reader.readBooleanAttribute("valid"))
                .setTapChanger(reader.readEnumAttribute("tapChanger", DiscreteMeasurement.TapChanger.class));
        DiscreteMeasurement.ValueType valueType = reader.readEnumAttribute("valueType", DiscreteMeasurement.ValueType.class);
        String value = reader.readStringAttribute(VALUE);
        if (value != null) {
            switch (valueType) {
                case BOOLEAN:
                    adder.setValue(Boolean.parseBoolean(value));
                    break;
                case INT:
                    adder.setValue(Integer.parseInt(value));
                    break;
                case STRING:
                    adder.setValue(value);
                    break;
                default:
                    throw new PowsyblException("Unsupported value type: " + valueType);
            }
        }
        reader.readUntilEndNode(DISCRETE_MEASUREMENT, () -> {
            if (reader.getNodeName().equals("property")) {
                adder.putProperty(reader.readStringAttribute("name"),
                        reader.readStringAttribute(VALUE));
            } else {
                throw new PowsyblException("Unexpected element: " + reader.getNodeName());
            }
        });
        adder.add();
    }

    @Override
    public boolean isSerializable(DiscreteMeasurements<I> extension) {
        return !extension.getDiscreteMeasurements().isEmpty();
    }
}
