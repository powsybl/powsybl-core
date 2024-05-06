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
import com.powsybl.commons.extensions.AbstractExtensionSerDe;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.commons.io.TreeDataWriter;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class SecondaryVoltageControlSerDe extends AbstractExtensionSerDe<Network, SecondaryVoltageControl> {

    private static final String CONTROL_ZONE_ROOT_ELEMENT = "controlZone";
    private static final String CONTROL_ZONE_ARRAY_ELEMENT = "controlZones";
    private static final String PILOT_POINT_ELEMENT = "pilotPoint";
    private static final String BUSBAR_SECTION_OR_BUS_ID_ROOT_ELEMENT = "busbarSectionOrBusId";
    private static final String BUSBAR_SECTION_OR_BUS_ID_ARRAY_ELEMENT = "busbarSectionOrBusIds";
    private static final String CONTROL_UNIT_ROOT_ELEMENT = "controlUnit";
    private static final String CONTROL_UNIT_ARRAY_ELEMENT = "controlUnits";

    public SecondaryVoltageControlSerDe() {
        super(SecondaryVoltageControl.NAME, "network", SecondaryVoltageControl.class,
                "secondaryVoltageControl_V1_0.xsd", "http://www.powsybl.org/schema/iidm/ext/secondary_voltage_control/1_0", "svc");
    }

    @Override
    public Map<String, String> getArrayNameToSingleNameMap() {
        return Map.of(CONTROL_ZONE_ARRAY_ELEMENT, CONTROL_ZONE_ROOT_ELEMENT,
                CONTROL_UNIT_ARRAY_ELEMENT, CONTROL_UNIT_ROOT_ELEMENT,
                BUSBAR_SECTION_OR_BUS_ID_ARRAY_ELEMENT, BUSBAR_SECTION_OR_BUS_ID_ROOT_ELEMENT);
    }

    @Override
    public void write(SecondaryVoltageControl control, SerializerContext context) {
        TreeDataWriter writer = context.getWriter();
        writer.writeStartNodes();
        for (ControlZone controlZone : control.getControlZones()) {
            writer.writeStartNode(getNamespaceUri(), CONTROL_ZONE_ROOT_ELEMENT);
            writer.writeStringAttribute("name", controlZone.getName());
            writePilotPoint(controlZone, writer);
            writeControlUnits(controlZone.getControlUnits(), writer);
            writer.writeEndNode();
        }
        writer.writeEndNodes();
    }

    private void writePilotPoint(ControlZone controlZone, TreeDataWriter writer) {
        writer.writeStartNode(getNamespaceUri(), PILOT_POINT_ELEMENT);
        writer.writeDoubleAttribute("targetV", controlZone.getPilotPoint().getTargetV());
        writer.writeStartNodes();
        for (String busbarSectionOrBusId : controlZone.getPilotPoint().getBusbarSectionsOrBusesIds()) {
            writer.writeStartNode(getNamespaceUri(), BUSBAR_SECTION_OR_BUS_ID_ROOT_ELEMENT);
            writer.writeNodeContent(busbarSectionOrBusId);
            writer.writeEndNode();
        }
        writer.writeEndNodes();
        writer.writeEndNode();
    }

    private void writeControlUnits(List<ControlUnit> controlUnits, TreeDataWriter writer) {
        writer.writeStartNodes();
        for (ControlUnit controlUnit : controlUnits) {
            writer.writeStartNode(getNamespaceUri(), CONTROL_UNIT_ROOT_ELEMENT);
            writer.writeBooleanAttribute("participate", controlUnit.isParticipate());
            writer.writeNodeContent(controlUnit.getId());
            writer.writeEndNode();
        }
        writer.writeEndNodes();
    }

    @Override
    public SecondaryVoltageControl read(Network network, DeserializerContext context) {
        SecondaryVoltageControlAdder adder = network.newExtension(SecondaryVoltageControlAdder.class);
        context.getReader().readChildNodes(elementName -> {
            if (!elementName.equals(CONTROL_ZONE_ROOT_ELEMENT)) {
                throw new PowsyblException(getExceptionMessageUnknownElement(elementName, "secondaryVoltageControl"));
            }
            readControlZone(context, adder);
        });
        return adder.add();
    }

    private static void readControlZone(DeserializerContext context, SecondaryVoltageControlAdder adder) {
        String name = context.getReader().readStringAttribute("name");
        ControlZoneAdder controlZoneAdder = adder.newControlZone()
                .withName(name);
        context.getReader().readChildNodes(elementName -> {
            switch (elementName) {
                case PILOT_POINT_ELEMENT -> readPilotPoint(context, controlZoneAdder);
                case CONTROL_UNIT_ROOT_ELEMENT -> readControlUnit(context, controlZoneAdder);
                default -> throw new PowsyblException(getExceptionMessageUnknownElement(elementName, "'controlZone'"));
            }
        });
        controlZoneAdder.add();
    }

    private static void readPilotPoint(DeserializerContext context, ControlZoneAdder controlZoneAdder) {
        double targetV = context.getReader().readDoubleAttribute("targetV");
        List<String> busbarSectionsOrBusesIds = new ArrayList<>();
        context.getReader().readChildNodes(elementName -> {
            if (!elementName.equals(BUSBAR_SECTION_OR_BUS_ID_ROOT_ELEMENT)) {
                throw new PowsyblException(getExceptionMessageUnknownElement(elementName, PILOT_POINT_ELEMENT));
            }
            busbarSectionsOrBusesIds.add(context.getReader().readContent());
        });
        controlZoneAdder.newPilotPoint()
                .withBusbarSectionsOrBusesIds(busbarSectionsOrBusesIds)
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
