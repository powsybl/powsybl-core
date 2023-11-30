/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.serde.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtensionSerDe;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.TreeDataReader;
import com.powsybl.commons.io.TreeDataWriter;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.extensions.DiscreteMeasurement;
import com.powsybl.iidm.network.extensions.DiscreteMeasurementAdder;
import com.powsybl.iidm.network.extensions.DiscreteMeasurements;
import com.powsybl.iidm.network.extensions.DiscreteMeasurementsAdder;

import java.util.Map;
import java.util.Optional;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class DiscreteMeasurementsSerDe<I extends Identifiable<I>> extends AbstractExtensionSerDe<I, DiscreteMeasurements<I>> {

    private static final String DISCRETE_MEASUREMENT_ROOT = "discreteMeasurement";
    private static final String DISCRETE_MEASUREMENT_ARRAY = "discreteMeasurements";
    private static final String VALUE = "value";
    public static final String PROPERTY_ROOT = "property";
    private static final String PROPERTY_ARRAY = "properties";

    public DiscreteMeasurementsSerDe() {
        super("discreteMeasurements", "network", DiscreteMeasurements.class,
                "discreteMeasurements.xsd", "http://www.powsybl.org/schema/iidm/ext/discrete_measurements/1_0", "dm");
    }

    @Override
    public Map<String, String> getArrayNameToSingleNameMap() {
        return Map.of(DISCRETE_MEASUREMENT_ARRAY, DISCRETE_MEASUREMENT_ROOT,
                PROPERTY_ARRAY, PROPERTY_ROOT);
    }

    @Override
    public void write(DiscreteMeasurements<I> extension, SerializerContext context) {
        TreeDataWriter writer = context.getWriter();
        writer.writeStartNodes();
        for (DiscreteMeasurement discreteMeasurement : extension.getDiscreteMeasurements()) {
            writer.writeStartNode(getNamespaceUri(), DISCRETE_MEASUREMENT_ROOT);
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
            writer.writeBooleanAttribute("valid", discreteMeasurement.isValid());

            writer.writeStartNodes();
            for (String name : discreteMeasurement.getPropertyNames()) {
                writer.writeStartNode(getNamespaceUri(), PROPERTY_ROOT);
                writer.writeStringAttribute("name", name);
                writer.writeStringAttribute(VALUE, discreteMeasurement.getProperty(name));
                writer.writeEndNode();
            }
            writer.writeEndNodes();

            writer.writeEndNode();
        }
        writer.writeEndNodes();
    }

    @Override
    public DiscreteMeasurements<I> read(I extendable, DeserializerContext context) {
        DiscreteMeasurementsAdder<I> adder = extendable.newExtension(DiscreteMeasurementsAdder.class);
        DiscreteMeasurements<I> discreteMeasurements = adder.add();
        context.getReader().readChildNodes(elementName -> {
            if (elementName.equals(DISCRETE_MEASUREMENT_ROOT)) {
                readDiscreteMeasurement(discreteMeasurements, context.getReader());
            } else {
                throw new PowsyblException("Unknown element name '" + elementName + "' in 'discreteMeasurements'");
            }
        });
        return discreteMeasurements;
    }

    private static <I extends Identifiable<I>> void readDiscreteMeasurement(DiscreteMeasurements<I> discreteMeasurements, TreeDataReader reader) {
        String id = reader.readStringAttribute("id");
        DiscreteMeasurement.Type type = reader.readEnumAttribute("type", DiscreteMeasurement.Type.class);
        DiscreteMeasurement.TapChanger tapChanger = reader.readEnumAttribute("tapChanger", DiscreteMeasurement.TapChanger.class);
        DiscreteMeasurement.ValueType valueType = reader.readEnumAttribute("valueType", DiscreteMeasurement.ValueType.class);

        DiscreteMeasurementAdder adder = discreteMeasurements.newDiscreteMeasurement()
                .setId(id)
                .setType(type)
                .setTapChanger(tapChanger);
        switch (valueType) {
            case BOOLEAN -> Optional.ofNullable(reader.readBooleanAttribute(VALUE)).ifPresent(adder::setValue);
            case INT -> Optional.ofNullable(reader.readIntAttribute(VALUE)).ifPresent(adder::setValue);
            case STRING -> Optional.ofNullable(reader.readStringAttribute(VALUE)).ifPresent(adder::setValue);
            default -> throw new PowsyblException("Unsupported value type: " + valueType);
        }

        adder.setValid(reader.readBooleanAttribute("valid", true));

        reader.readChildNodes(elementName -> {
            if (elementName.equals(PROPERTY_ROOT)) {
                adder.putProperty(reader.readStringAttribute("name"),
                        reader.readStringAttribute(VALUE));
                reader.readEndNode();
            } else {
                throw new PowsyblException("Unknown element name '" + elementName + "' in 'discreteMeasurement'");
            }
        });
        adder.add();
    }

    @Override
    public boolean isSerializable(DiscreteMeasurements<I> extension) {
        return !extension.getDiscreteMeasurements().isEmpty();
    }
}
