/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
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
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.extensions.VoltagePerReactivePowerControl;
import com.powsybl.iidm.network.extensions.VoltagePerReactivePowerControlAdder;

import javax.xml.stream.XMLStreamException;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class VoltagePerReactivePowerControlXmlSerializer extends AbstractExtensionXmlSerializer<StaticVarCompensator, VoltagePerReactivePowerControl> {

    public VoltagePerReactivePowerControlXmlSerializer() {
        super("voltagePerReactivePowerControl", "network", VoltagePerReactivePowerControl.class, false, "voltagePerReactivePowerControl.xsd",
                "http://www.powsybl.org/schema/iidm/ext/voltage_per_reactive_power_control/1_0", "vprpc");
    }

    @Override
    public void write(VoltagePerReactivePowerControl control, XmlWriterContext context) throws XMLStreamException {
        XmlUtil.writeDouble("slope", control.getSlope(), context.getWriter());
    }

    @Override
    public VoltagePerReactivePowerControl read(StaticVarCompensator svc, XmlReaderContext context) {
        double slope = XmlUtil.readDoubleAttribute(context.getReader(), "slope");
        svc.newExtension(VoltagePerReactivePowerControlAdder.class)
                .withSlope(slope)
                .add();
        return svc.getExtension(VoltagePerReactivePowerControl.class);
    }
}
