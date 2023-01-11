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
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.SecondaryVoltageControl;
import com.powsybl.iidm.network.extensions.SecondaryVoltageControl.ControlUnit;
import com.powsybl.iidm.network.extensions.SecondaryVoltageControl.ControlZone;
import com.powsybl.iidm.network.extensions.SecondaryVoltageControl.PilotPoint;
import com.powsybl.iidm.network.extensions.SecondaryVoltageControlAdder;
import org.apache.commons.lang3.mutable.MutableDouble;

import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class SecondaryVoltageControlXmlSerializer extends AbstractExtensionXmlSerializer<Network, SecondaryVoltageControl> {

    private static final String CONTROL_ZONE_ELEMENT = "controlZone";
    private static final String PILOT_POINT_ELEMENT = "pilotPoint";
    private static final String BUSBAR_SECTION_OR_BUS_ID_ELEMENT = "busbarSectionOrBusId";
    private static final String CONTROL_UNIT_ELEMENT = "controlUnit";

    public SecondaryVoltageControlXmlSerializer() {
        super(SecondaryVoltageControl.NAME, "network", SecondaryVoltageControl.class, true,
                "secondaryVoltageControl_V1_0.xsd", "http://www.powsybl.org/schema/iidm/ext/secondary_voltage_control/1_0", "svc");
    }

    @Override
    public void write(SecondaryVoltageControl control, XmlWriterContext context) throws XMLStreamException {
        for (SecondaryVoltageControl.ControlZone controlZone : control.getControlZones()) {
            context.getWriter().writeStartElement(getNamespaceUri(), CONTROL_ZONE_ELEMENT);
            context.getWriter().writeAttribute("name", controlZone.getName());
            context.getWriter().writeStartElement(getNamespaceUri(), PILOT_POINT_ELEMENT);
            XmlUtil.writeDouble("targetV", controlZone.getPilotPoint().getTargetV(), context.getWriter());
            for (String busbarSectionOrBusId : controlZone.getPilotPoint().getBusbarSectionsOrBusesIds()) {
                context.getWriter().writeStartElement(getNamespaceUri(), BUSBAR_SECTION_OR_BUS_ID_ELEMENT);
                context.getWriter().writeCharacters(busbarSectionOrBusId);
                context.getWriter().writeEndElement();
            }
            context.getWriter().writeEndElement();
            for (ControlUnit controlUnit : controlZone.getControlUnits()) {
                context.getWriter().writeStartElement(getNamespaceUri(), CONTROL_UNIT_ELEMENT);
                context.getWriter().writeAttribute("participate", Boolean.toString(controlUnit.isParticipate()));
                context.getWriter().writeCharacters(controlUnit.getId());
                context.getWriter().writeEndElement();
            }
            context.getWriter().writeEndElement();
        }
    }

    @Override
    public SecondaryVoltageControl read(Network network, XmlReaderContext context) throws XMLStreamException {
        SecondaryVoltageControlAdder adder = network.newExtension(SecondaryVoltageControlAdder.class);
        XmlUtil.readUntilEndElement(getExtensionName(), context.getReader(), () -> {
            if (context.getReader().getLocalName().equals(CONTROL_ZONE_ELEMENT)) {
                String name = context.getReader().getAttributeValue(null, "name");
                MutableDouble targetV = new MutableDouble(Double.NaN);
                List<String> busbarSectionsOrBusesIds = new ArrayList<>();
                List<ControlUnit> controlUnits = new ArrayList<>();
                XmlUtil.readUntilEndElement(CONTROL_ZONE_ELEMENT, context.getReader(), () -> {
                    if (context.getReader().getLocalName().equals(PILOT_POINT_ELEMENT)) {
                        targetV.setValue(XmlUtil.readDoubleAttribute(context.getReader(), "targetV"));
                    } else if (context.getReader().getLocalName().equals(BUSBAR_SECTION_OR_BUS_ID_ELEMENT)) {
                        busbarSectionsOrBusesIds.add(XmlUtil.readText(BUSBAR_SECTION_OR_BUS_ID_ELEMENT, context.getReader()));
                    } else if (context.getReader().getLocalName().equals(CONTROL_UNIT_ELEMENT)) {
                        boolean participate = Boolean.parseBoolean(context.getReader().getAttributeValue(null, "participate"));
                        String id = XmlUtil.readText(CONTROL_UNIT_ELEMENT, context.getReader());
                        controlUnits.add(new ControlUnit(id, participate));
                    } else {
                        throw new IllegalStateException("Unexpected element " + context.getReader().getLocalName());
                    }
                });
                PilotPoint pilotPoint = new PilotPoint(busbarSectionsOrBusesIds, targetV.getValue());
                adder.addControlZone(new ControlZone(name, pilotPoint, controlUnits));
            }
        });
        return adder.add();
    }
}
