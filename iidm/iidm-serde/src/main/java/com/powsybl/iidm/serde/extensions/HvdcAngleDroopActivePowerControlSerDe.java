/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */
package com.powsybl.iidm.serde.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtensionSerDe;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.extensions.HvdcAngleDroopActivePowerControl;
import com.powsybl.iidm.network.extensions.HvdcAngleDroopActivePowerControlAdder;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 * @author Paul Bui-Quang {@literal <paul.buiquang at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class HvdcAngleDroopActivePowerControlSerDe extends AbstractExtensionSerDe<HvdcLine, HvdcAngleDroopActivePowerControl> {

    public HvdcAngleDroopActivePowerControlSerDe() {
        super("hvdcAngleDroopActivePowerControl", "network", HvdcAngleDroopActivePowerControl.class,
                "hvdcAngleDroopActivePowerControl.xsd",
                "http://www.itesla_project.eu/schema/iidm/ext/hvdc_angle_droop_active_power_control/1_0", "hapc");
    }

    @Override
    public void write(HvdcAngleDroopActivePowerControl extension, SerializerContext context) {
        context.getWriter().writeFloatAttribute("p0", extension.getP0());
        context.getWriter().writeFloatAttribute("droop", extension.getDroop());
        context.getWriter().writeBooleanAttribute("enabled", extension.isEnabled());
    }

    @Override
    public HvdcAngleDroopActivePowerControl read(HvdcLine hvdcLine, DeserializerContext context) {
        float p0 = context.getReader().readFloatAttribute("p0");
        float droop = context.getReader().readFloatAttribute("droop");
        boolean enabled = context.getReader().readBooleanAttribute("enabled");
        context.getReader().readEndNode();

        return hvdcLine.newExtension(HvdcAngleDroopActivePowerControlAdder.class)
                .withP0(p0)
                .withDroop(droop)
                .withEnabled(enabled)
                .add();
    }
}
