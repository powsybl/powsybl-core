/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */
package com.powsybl.iidm.xml.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtensionXmlSerializer;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.extensions.HvdcAngleDroopActivePowerControl;
import com.powsybl.iidm.network.extensions.HvdcAngleDroopActivePowerControlAdder;

import javax.xml.stream.XMLStreamException;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 * @author Paul Bui-Quang <paul.buiquang at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class HvdcAngleDroopActivePowerControlXmlSerializer extends AbstractExtensionXmlSerializer<HvdcLine, HvdcAngleDroopActivePowerControl> {

    public HvdcAngleDroopActivePowerControlXmlSerializer() {
        super("hvdcAngleDroopActivePowerControl", "network", HvdcAngleDroopActivePowerControl.class,
                false, "hvdcAngleDroopActivePowerControl.xsd",
                "http://www.itesla_project.eu/schema/iidm/ext/hvdc_angle_droop_active_power_control/1_0", "hapc");
    }

    @Override
    public void write(HvdcAngleDroopActivePowerControl extension, XmlWriterContext context) throws XMLStreamException {
        context.getWriter().writeAttribute("p0", Float.toString(extension.getP0()));
        context.getWriter().writeAttribute("droop", Float.toString(extension.getDroop()));
        context.getWriter().writeAttribute("enabled", Boolean.toString(extension.isEnabled()));
    }

    @Override
    public HvdcAngleDroopActivePowerControl read(HvdcLine hvdcLine, XmlReaderContext context) throws XMLStreamException {
        float p0 = XmlUtil.readFloatAttribute(context.getReader(), "p0");
        float droop = XmlUtil.readFloatAttribute(context.getReader(), "droop");
        boolean enabled = XmlUtil.readBoolAttribute(context.getReader(), "enabled");

        return hvdcLine.newExtension(HvdcAngleDroopActivePowerControlAdder.class)
                .withP0(p0)
                .withDroop(droop)
                .withEnabled(enabled)
                .add();
    }
}
