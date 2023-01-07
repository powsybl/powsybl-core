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
import com.powsybl.iidm.network.extensions.SecondaryVoltageControl.Zone;
import com.powsybl.iidm.network.extensions.SecondaryVoltageControlAdder;
import org.apache.commons.lang3.mutable.MutableObject;

import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class SecondaryVoltageControlXmlSerializer extends AbstractExtensionXmlSerializer<Network, SecondaryVoltageControl> {

    public static final String GENERATOR_ELEMENT = "generator";
    public static final String ZONE_ELEMENT = "zone";
    public static final String PILOT_POINT_ELEMENT = "pilotPoint";

    public SecondaryVoltageControlXmlSerializer() {
        super(SecondaryVoltageControl.NAME, "network", SecondaryVoltageControl.class, true,
                "secondaryVoltageControl_V1_0.xsd", "http://www.powsybl.org/schema/iidm/ext/secondary_voltage_control/1_0", "svc");
    }

    @Override
    public void write(SecondaryVoltageControl control, XmlWriterContext context) throws XMLStreamException {
        for (Zone zone : control.getZones()) {
            context.getWriter().writeStartElement(getNamespaceUri(), ZONE_ELEMENT);
            context.getWriter().writeAttribute("name", zone.getName());
            context.getWriter().writeEmptyElement(getNamespaceUri(), PILOT_POINT_ELEMENT);
            context.getWriter().writeAttribute("busbarSectionOrBusId", zone.getPilotPoint().getBusbarSectionOrBusId());
            XmlUtil.writeDouble("targetV", zone.getPilotPoint().getTargetV(), context.getWriter());
            for (String generatorId : zone.getGeneratorsIds()) {
                context.getWriter().writeStartElement(getNamespaceUri(), GENERATOR_ELEMENT);
                context.getWriter().writeCharacters(generatorId);
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
                MutableObject<SecondaryVoltageControl.PilotPoint> pilotPoint = new MutableObject<>();
                List<String> generatorsIds = new ArrayList<>();
                XmlUtil.readUntilEndElement(ZONE_ELEMENT, context.getReader(), () -> {
                    if (context.getReader().getLocalName().equals(PILOT_POINT_ELEMENT)) {
                        String busbarSectionOrBusId = context.getReader().getAttributeValue(null, "busbarSectionOrBusId");
                        double targetV = XmlUtil.readDoubleAttribute(context.getReader(), "targetV");
                        pilotPoint.setValue(new SecondaryVoltageControl.PilotPoint(busbarSectionOrBusId, targetV));
                    } else if (context.getReader().getLocalName().equals(GENERATOR_ELEMENT)) {
                        generatorsIds.add(XmlUtil.readText(GENERATOR_ELEMENT, context.getReader()));
                    } else {
                        throw new IllegalStateException("Unexpected element " + context.getReader().getLocalName());
                    }
                });
                adder.addZone(new Zone(name, pilotPoint.getValue(), generatorsIds));
            }
        });
        return adder.add();
    }
}
