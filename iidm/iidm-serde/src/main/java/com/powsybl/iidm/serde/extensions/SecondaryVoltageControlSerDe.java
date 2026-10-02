/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.commons.io.TreeDataWriter;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.*;
import com.powsybl.iidm.serde.IidmVersion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class SecondaryVoltageControlSerDe extends AbstractVersionableNetworkExtensionSerDe<Network, SecondaryVoltageControl, SecondaryVoltageControlSerDe.Version> {

    public enum Version implements SerDeVersion<Version> {
        V_1_0("/xsd/secondaryVoltageControl_V1_0.xsd", "http://www.powsybl.org/schema/iidm/ext/secondary_voltage_control/1_0",
                new VersionNumbers(1, 0), IidmVersion.V_1_0, null),
        V_1_1("/xsd/secondaryVoltageControl_V1_1.xsd", "http://www.powsybl.org/schema/iidm/ext/secondary_voltage_control/1_1",
                new VersionNumbers(1, 1), IidmVersion.V_1_0, null);

        private final VersionInfo versionInfo;

        Version(String xsdResourcePath, String namespaceUri, VersionNumbers versionNumbers, IidmVersion minIidmVersionIncluded, IidmVersion maxIidmVersionExcluded) {
            this.versionInfo = new VersionInfo(xsdResourcePath, namespaceUri, "svc", versionNumbers,
                    minIidmVersionIncluded, maxIidmVersionExcluded, SecondaryVoltageControl.NAME);
        }

        @Override
        public VersionInfo getVersionInfo() {
            return versionInfo;
        }
    }

    private static final String CONTROL_ZONE_ROOT_ELEMENT = "controlZone";
    private static final String CONTROL_ZONE_ARRAY_ELEMENT = "controlZones";
    private static final String PILOT_POINT_ELEMENT = "pilotPoint";
    private static final String BUS_ROOT_ELEMENT = "bus";
    private static final String BUS_ARRAY_ELEMENT = "buses";
    private static final String BUSBAR_SECTION_ROOT_ELEMENT = "busbarSection";
    private static final String BUSBAR_SECTION_ARRAY_ELEMENT = "busbarSections";
    private static final String BUSBAR_SECTION_OR_BUS_ID_ROOT_ELEMENT = "busbarSectionOrBusId";
    private static final String BUSBAR_SECTION_OR_BUS_ID_ARRAY_ELEMENT = "busbarSectionOrBusIds";
    private static final String VOLTAGE_LEVEL_ATTRIBUTE = "voltageLevel";
    private static final String CONTROL_UNIT_ROOT_ELEMENT = "controlUnit";
    private static final String CONTROL_UNIT_ARRAY_ELEMENT = "controlUnits";

    public SecondaryVoltageControlSerDe() {
        super(SecondaryVoltageControl.NAME, SecondaryVoltageControl.class, Version.values());
    }

    @Override
    public Map<String, String> getArrayNameToSingleNameMap() {
        return Map.of(CONTROL_ZONE_ARRAY_ELEMENT, CONTROL_ZONE_ROOT_ELEMENT,
                CONTROL_UNIT_ARRAY_ELEMENT, CONTROL_UNIT_ROOT_ELEMENT,
                BUS_ARRAY_ELEMENT, BUS_ROOT_ELEMENT,
                BUSBAR_SECTION_ARRAY_ELEMENT, BUSBAR_SECTION_ROOT_ELEMENT,
                BUSBAR_SECTION_OR_BUS_ID_ARRAY_ELEMENT, BUSBAR_SECTION_OR_BUS_ID_ROOT_ELEMENT);
    }

    @Override
    public void write(SecondaryVoltageControl control, SerializerContext context) {
        Version version = getExtensionVersionToExport(context);
        String namespaceUri = getNamespaceUri(version.getVersionString());
        TreeDataWriter writer = context.getWriter();
        writer.writeStartNodes();
        for (ControlZone controlZone : control.getControlZones()) {
            writer.writeStartNode(namespaceUri, CONTROL_ZONE_ROOT_ELEMENT);
            writer.writeStringAttribute("name", controlZone.getName());
            writePilotPoint(controlZone, writer, version, namespaceUri);
            writeControlUnits(controlZone.getControlUnits(), writer, namespaceUri);
            writer.writeEndNode();
        }
        writer.writeEndNodes();
    }

    private void writePilotPoint(ControlZone controlZone, TreeDataWriter writer, Version version, String namespaceUri) {
        PilotPoint pilotPoint = controlZone.getPilotPoint();
        writer.writeStartNode(namespaceUri, PILOT_POINT_ELEMENT);
        writer.writeDoubleAttribute("targetV", pilotPoint.getTargetV());
        if (version.isGreaterThan(Version.V_1_0)) {
            writer.writeStartNodes();
            for (PilotPoint.BusRef bus : pilotPoint.getBuses()) {
                writer.writeStartNode(namespaceUri, BUS_ROOT_ELEMENT);
                writer.writeStringAttribute(VOLTAGE_LEVEL_ATTRIBUTE, bus.voltageLevelId());
                writer.writeNodeContent(bus.busId());
                writer.writeEndNode();
            }
            writer.writeEndNodes();
            writer.writeStartNodes();
            for (String busbarSectionId : pilotPoint.getBusbarSectionIds()) {
                writer.writeStartNode(namespaceUri, BUSBAR_SECTION_ROOT_ELEMENT);
                writer.writeNodeContent(busbarSectionId);
                writer.writeEndNode();
            }
            writer.writeEndNodes();
        } else {
            // Version 1.0 does not distinguish buses from busbar sections: the bus and busbar section IDs
            // are written as a flat list. The voltage level of the buses is not serialized (it is resolved
            // from the network when reading back a 1.0 file).
            writer.writeStartNodes();
            for (PilotPoint.BusRef bus : pilotPoint.getBuses()) {
                writer.writeStartNode(namespaceUri, BUSBAR_SECTION_OR_BUS_ID_ROOT_ELEMENT);
                writer.writeNodeContent(bus.busId());
                writer.writeEndNode();
            }
            for (String busbarSectionId : pilotPoint.getBusbarSectionIds()) {
                writer.writeStartNode(namespaceUri, BUSBAR_SECTION_OR_BUS_ID_ROOT_ELEMENT);
                writer.writeNodeContent(busbarSectionId);
                writer.writeEndNode();
            }
            writer.writeEndNodes();
        }
        writer.writeEndNode();
    }

    private void writeControlUnits(List<ControlUnit> controlUnits, TreeDataWriter writer, String namespaceUri) {
        writer.writeStartNodes();
        for (ControlUnit controlUnit : controlUnits) {
            writer.writeStartNode(namespaceUri, CONTROL_UNIT_ROOT_ELEMENT);
            writer.writeBooleanAttribute("participate", controlUnit.isParticipate());
            writer.writeNodeContent(controlUnit.getId());
            writer.writeEndNode();
        }
        writer.writeEndNodes();
    }

    @Override
    public SecondaryVoltageControl read(Network network, DeserializerContext context) {
        Version version = getExtensionVersionImported(context);
        SecondaryVoltageControlAdder adder = network.newExtension(SecondaryVoltageControlAdder.class);
        context.getReader().readChildNodes(elementName -> {
            if (!elementName.equals(CONTROL_ZONE_ROOT_ELEMENT)) {
                throw new PowsyblException(getExceptionMessageUnknownElement(elementName, "secondaryVoltageControl"));
            }
            readControlZone(network, context, adder, version);
        });
        return adder.add();
    }

    private static void readControlZone(Network network, DeserializerContext context, SecondaryVoltageControlAdder adder, Version version) {
        String name = context.getReader().readStringAttribute("name");
        ControlZoneAdder controlZoneAdder = adder.newControlZone()
                .withName(name);
        context.getReader().readChildNodes(elementName -> {
            switch (elementName) {
                case PILOT_POINT_ELEMENT -> readPilotPoint(network, context, controlZoneAdder, version);
                case CONTROL_UNIT_ROOT_ELEMENT -> readControlUnit(context, controlZoneAdder);
                default -> throw new PowsyblException(getExceptionMessageUnknownElement(elementName, "'controlZone'"));
            }
        });
        controlZoneAdder.add();
    }

    private static void readPilotPoint(Network network, DeserializerContext context, ControlZoneAdder controlZoneAdder, Version version) {
        double targetV = context.getReader().readDoubleAttribute("targetV");
        List<PilotPoint.BusRef> buses = new ArrayList<>();
        List<String> busbarSectionIds = new ArrayList<>();
        if (version.isGreaterThan(Version.V_1_0)) {
            context.getReader().readChildNodes(elementName -> {
                switch (elementName) {
                    case BUS_ROOT_ELEMENT -> {
                        String voltageLevelId = context.getReader().readStringAttribute(VOLTAGE_LEVEL_ATTRIBUTE);
                        buses.add(new PilotPoint.BusRef(voltageLevelId, context.getReader().readContent()));
                    }
                    case BUSBAR_SECTION_ROOT_ELEMENT -> busbarSectionIds.add(context.getReader().readContent());
                    default -> throw new PowsyblException(getExceptionMessageUnknownElement(elementName, PILOT_POINT_ELEMENT));
                }
            });
        } else {
            // Version 1.0: a flat list of busbar section or bus IDs. Each ID is resolved against the network
            // to rebuild the distinction between buses (with their voltage level) and busbar sections.
            context.getReader().readChildNodes(elementName -> {
                if (!elementName.equals(BUSBAR_SECTION_OR_BUS_ID_ROOT_ELEMENT)) {
                    throw new PowsyblException(getExceptionMessageUnknownElement(elementName, PILOT_POINT_ELEMENT));
                }
                String id = context.getReader().readContent();
                Identifiable<?> identifiable = network.getIdentifiable(id);
                if (identifiable instanceof Bus bus) {
                    buses.add(new PilotPoint.BusRef(bus.getVoltageLevel().getId(), id));
                } else {
                    busbarSectionIds.add(id);
                }
            });
        }
        controlZoneAdder.newPilotPoint()
                .withBuses(buses)
                .withBusbarSectionIds(busbarSectionIds)
                .withTargetV(targetV)
                .add();
    }

    private static void readControlUnit(DeserializerContext context, ControlZoneAdder controlZoneAdder) {
        boolean participate = context.getReader().readBooleanAttribute("participate");
        String id = context.getReader().readContent();
        controlZoneAdder.newControlUnit()
                .withId(id)
                .withParticipate(participate)
                .add();
    }

    private static String getExceptionMessageUnknownElement(String elementName, String where) {
        return "Unknown element name '" + elementName + "' in '" + where + "'";
    }
}
