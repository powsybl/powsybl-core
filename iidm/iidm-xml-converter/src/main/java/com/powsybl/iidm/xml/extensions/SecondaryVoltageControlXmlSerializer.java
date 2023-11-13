/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.xml.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtensionXmlSerializer;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.extensions.XmlReaderContext;
import com.powsybl.commons.extensions.XmlWriterContext;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.SecondaryVoltageControl;
import com.powsybl.iidm.network.extensions.SecondaryVoltageControl.ControlUnit;
import com.powsybl.iidm.network.extensions.SecondaryVoltageControl.ControlZone;
import com.powsybl.iidm.network.extensions.SecondaryVoltageControl.PilotPoint;
import com.powsybl.iidm.network.extensions.SecondaryVoltageControlAdder;
import org.apache.commons.lang3.mutable.MutableDouble;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
@AutoService(ExtensionXmlSerializer.class)
public class SecondaryVoltageControlXmlSerializer extends AbstractExtensionXmlSerializer<Network, SecondaryVoltageControl> {

    private static final String CONTROL_ZONE_ELEMENT = "controlZone";
    private static final String PILOT_POINT_ELEMENT = "pilotPoint";
    private static final String BUSBAR_SECTION_OR_BUS_ID_ELEMENT = "busbarSectionOrBusId";
    private static final String CONTROL_UNIT_ELEMENT = "controlUnit";

    public SecondaryVoltageControlXmlSerializer() {
        super(SecondaryVoltageControl.NAME, "network", SecondaryVoltageControl.class,
                "secondaryVoltageControl_V1_0.xsd", "http://www.powsybl.org/schema/iidm/ext/secondary_voltage_control/1_0", "svc");
    }

    @Override
    public void write(SecondaryVoltageControl control, XmlWriterContext context) {
        for (SecondaryVoltageControl.ControlZone controlZone : control.getControlZones()) {
            context.getWriter().writeStartNode(getNamespaceUri(), CONTROL_ZONE_ELEMENT);
            context.getWriter().writeStringAttribute("name", controlZone.getName());
            context.getWriter().writeStartNode(getNamespaceUri(), PILOT_POINT_ELEMENT);
            context.getWriter().writeDoubleAttribute("targetV", controlZone.getPilotPoint().getTargetV());
            for (String busbarSectionOrBusId : controlZone.getPilotPoint().getBusbarSectionsOrBusesIds()) {
                context.getWriter().writeStartNode(getNamespaceUri(), BUSBAR_SECTION_OR_BUS_ID_ELEMENT);
                context.getWriter().writeNodeContent(busbarSectionOrBusId);
                context.getWriter().writeEndNode();
            }
            context.getWriter().writeEndNode();
            for (ControlUnit controlUnit : controlZone.getControlUnits()) {
                context.getWriter().writeStartNode(getNamespaceUri(), CONTROL_UNIT_ELEMENT);
                context.getWriter().writeBooleanAttribute("participate", controlUnit.isParticipate());
                context.getWriter().writeNodeContent(controlUnit.getId());
                context.getWriter().writeEndNode();
            }
            context.getWriter().writeEndNode();
        }
    }

    @Override
    public SecondaryVoltageControl read(Network network, XmlReaderContext context) {
        SecondaryVoltageControlAdder adder = network.newExtension(SecondaryVoltageControlAdder.class);
        context.getReader().readChildNodes(elementName -> {
            if (!elementName.equals(CONTROL_ZONE_ELEMENT)) {
                throw new IllegalStateException("Unexpected element " + elementName);
            }
            readControlZone(context, adder);
        });
        return adder.add();
    }

    private static void readControlZone(XmlReaderContext context, SecondaryVoltageControlAdder adder) {
        String name = context.getReader().readStringAttribute("name");
        MutableDouble targetV = new MutableDouble(Double.NaN);
        List<String> busbarSectionsOrBusesIds = new ArrayList<>();
        List<ControlUnit> controlUnits = new ArrayList<>();
        context.getReader().readChildNodes(elementName2 -> {
            switch (elementName2) {
                case PILOT_POINT_ELEMENT -> readPilotPoint(context, targetV, busbarSectionsOrBusesIds);
                case CONTROL_UNIT_ELEMENT -> readControlUnit(context, controlUnits);
                default -> throw new IllegalStateException("Unexpected element " + elementName2);
            }
        });
        PilotPoint pilotPoint = new PilotPoint(busbarSectionsOrBusesIds, targetV.getValue());
        adder.addControlZone(new ControlZone(name, pilotPoint, controlUnits));
    }

    private static void readPilotPoint(XmlReaderContext context, MutableDouble targetV, List<String> busbarSectionsOrBusesIds) {
        targetV.setValue(context.getReader().readDoubleAttribute("targetV"));
        context.getReader().readChildNodes(elementName -> {
            if (!elementName.equals(BUSBAR_SECTION_OR_BUS_ID_ELEMENT)) {
                throw new IllegalStateException("Unexpected element " + elementName);
            }
            busbarSectionsOrBusesIds.add(context.getReader().readContent());
        });
    }

    private static void readControlUnit(XmlReaderContext context, List<ControlUnit> controlUnits) {
        boolean participate = context.getReader().readBooleanAttribute("participate");
        String id = context.getReader().readContent();
        controlUnits.add(new ControlUnit(id, participate));
    }
}
