/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
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
import com.powsybl.iidm.network.extensions.SecondaryVoltageControl.PilotPoint;
import com.powsybl.iidm.network.extensions.SecondaryVoltageControl.Zone;
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

    public static final String GENERATOR_OR_VSC_ID_ELEMENT = "generatorOrVscId";
    public static final String ZONE_ELEMENT = "zone";
    public static final String PILOT_POINT_ELEMENT = "pilotPoint";
    private static final String BUSBAR_SECTION_OR_BUS_ID_ELEMENT = "busbarSectionOrBusId";

    public SecondaryVoltageControlXmlSerializer() {
        super(SecondaryVoltageControl.NAME, "network", SecondaryVoltageControl.class, true,
                "secondaryVoltageControl_V1_0.xsd", "http://www.powsybl.org/schema/iidm/ext/secondary_voltage_control/1_0", "svc");
    }

    @Override
    public void write(SecondaryVoltageControl control, XmlWriterContext context) throws XMLStreamException {
        for (Zone zone : control.getZones()) {
            context.getWriter().writeStartElement(getNamespaceUri(), ZONE_ELEMENT);
            context.getWriter().writeAttribute("name", zone.getName());
            context.getWriter().writeStartElement(getNamespaceUri(), PILOT_POINT_ELEMENT);
            XmlUtil.writeDouble("targetV", zone.getPilotPoint().getTargetV(), context.getWriter());
            for (String busbarSectionOrBusId : zone.getPilotPoint().getBusbarSectionsOrBusesIds()) {
                context.getWriter().writeStartElement(getNamespaceUri(), BUSBAR_SECTION_OR_BUS_ID_ELEMENT);
                context.getWriter().writeCharacters(busbarSectionOrBusId);
                context.getWriter().writeEndElement();
            }
            context.getWriter().writeEndElement();
            for (String generatorOdVscId : zone.getGeneratorsOrVscsIds()) {
                context.getWriter().writeStartElement(getNamespaceUri(), GENERATOR_OR_VSC_ID_ELEMENT);
                context.getWriter().writeCharacters(generatorOdVscId);
                context.getWriter().writeEndElement();
            }
            context.getWriter().writeEndElement();
        }
    }

    @Override
    public SecondaryVoltageControl read(Network network, XmlReaderContext context) throws XMLStreamException {
        SecondaryVoltageControlAdder adder = network.newExtension(SecondaryVoltageControlAdder.class);
        XmlUtil.readUntilEndElement(getExtensionName(), context.getReader(), () -> {
            if (context.getReader().getLocalName().equals(ZONE_ELEMENT)) {
                String name = context.getReader().getAttributeValue(null, "name");
                MutableDouble targetV = new MutableDouble(Double.NaN);
                List<String> busbarSectionsOrBusesIds = new ArrayList<>();
                List<String> generatorsOrVscsIds = new ArrayList<>();
                XmlUtil.readUntilEndElement(ZONE_ELEMENT, context.getReader(), () -> {
                    if (context.getReader().getLocalName().equals(PILOT_POINT_ELEMENT)) {
                        targetV.setValue(XmlUtil.readDoubleAttribute(context.getReader(), "targetV"));
                    } else if (context.getReader().getLocalName().equals(BUSBAR_SECTION_OR_BUS_ID_ELEMENT)) {
                        busbarSectionsOrBusesIds.add(XmlUtil.readText(BUSBAR_SECTION_OR_BUS_ID_ELEMENT, context.getReader()));
                    } else if (context.getReader().getLocalName().equals(GENERATOR_OR_VSC_ID_ELEMENT)) {
                        generatorsOrVscsIds.add(XmlUtil.readText(GENERATOR_OR_VSC_ID_ELEMENT, context.getReader()));
                    } else {
                        throw new IllegalStateException("Unexpected element " + context.getReader().getLocalName());
                    }
                });
                PilotPoint pilotPoint = new PilotPoint(busbarSectionsOrBusesIds, targetV.getValue());
                adder.addZone(new Zone(name, pilotPoint, generatorsOrVscsIds));
            }
        });
        return adder.add();
    }
}
