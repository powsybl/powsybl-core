/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtensionSerDe;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.extensions.VoltagePerReactivePowerControl;
import com.powsybl.iidm.network.extensions.VoltagePerReactivePowerControlAdder;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
@AutoService(ExtensionSerDe.class)
public class VoltagePerReactivePowerControlSerDe extends AbstractExtensionSerDe<StaticVarCompensator, VoltagePerReactivePowerControl> {

    public VoltagePerReactivePowerControlSerDe() {
        super("voltagePerReactivePowerControl", "network", VoltagePerReactivePowerControl.class, "voltagePerReactivePowerControl.xsd",
                "http://www.powsybl.org/schema/iidm/ext/voltage_per_reactive_power_control/1_0", "vprpc");
    }

    @Override
    public void write(VoltagePerReactivePowerControl control, SerializerContext context) {
        context.getWriter().writeDoubleAttribute("slope", control.getSlope());
    }

    @Override
    public VoltagePerReactivePowerControl read(StaticVarCompensator svc, DeserializerContext context) {
        double slope = context.getReader().readDoubleAttribute("slope");
        context.getReader().readEndNode();
        return svc.newExtension(VoltagePerReactivePowerControlAdder.class)
                .withSlope(slope)
                .add();
    }
}
