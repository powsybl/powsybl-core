/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
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
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.extensions.ActivePowerControl;
import com.powsybl.iidm.network.extensions.ActivePowerControlAdder;

import javax.xml.stream.XMLStreamException;

/**
 * @author Ghiles Abdellah <ghiles.abdellah at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class ActivePowerControlXmlSerializer<T extends Injection<T>> extends AbstractExtensionXmlSerializer<T, ActivePowerControl<T>> {

    public ActivePowerControlXmlSerializer() {
        super("activePowerControl", "network", ActivePowerControl.class, false, "activePowerControl.xsd",
                "http://www.itesla_project.eu/schema/iidm/ext/active_power_control/1_0", "apc");
    }

    @Override
    public void write(ActivePowerControl activePowerControl, XmlWriterContext context) throws XMLStreamException {
        context.getWriter().writeAttribute("participate", Boolean.toString(activePowerControl.isParticipate()));
        XmlUtil.writeFloat("droop", activePowerControl.getDroop(), context.getWriter());
    }

    @Override
    public ActivePowerControl<T> read(T identifiable, XmlReaderContext context) {
        boolean participate = XmlUtil.readBoolAttribute(context.getReader(), "participate");
        float droop = XmlUtil.readFloatAttribute(context.getReader(), "droop");
        ActivePowerControlAdder<T> activePowerControlAdder = identifiable.newExtension(ActivePowerControlAdder.class);
        return activePowerControlAdder.withParticipate(participate)
                .withDroop(droop)
                .add();
    }
}
