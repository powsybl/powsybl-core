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
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.extensions.removed.VoltagePerReactivePowerControl;
import com.powsybl.iidm.serde.IidmVersion;
import com.powsybl.iidm.serde.NetworkSerializerContext;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
@AutoService({ExtensionSerDe.class, ExtinctExtensionSerDe.class})
public class VoltagePerReactivePowerControlSerDe extends AbstractExtensionSerDe<StaticVarCompensator, VoltagePerReactivePowerControl>
        implements ExtinctExtensionSerDe<StaticVarCompensator, VoltagePerReactivePowerControl> {

    public static final IidmVersion LAST_SUPPORTED_VERSION = IidmVersion.V_1_16;

    public VoltagePerReactivePowerControlSerDe() {
        super(VoltagePerReactivePowerControl.NAME, "network", VoltagePerReactivePowerControl.class, "voltagePerReactivePowerControl.xsd",
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
        //if (svc.isWithMode(RegulationMode.VOLTAGE_PER_REACTIVE_POWER)) { //TODO OPE is this check expected?
        svc.getVoltageRegulation().setSlope(slope);
        //}
        return null;
    }

    @Override
    public IidmVersion getLastSupportedVersion() {
        return LAST_SUPPORTED_VERSION;
    }

    @Override
    public boolean isExtensionNeeded(Network n) {
        return n.getStaticVarCompensatorStream().anyMatch(VoltagePerReactivePowerControlSerDe::isExtensionNeeded);
    }

    private static boolean isExtensionNeeded(StaticVarCompensator svc) {
        return svc.getVoltageRegulation() != null
                && !Double.isNaN(svc.getVoltageRegulation().getSlope());
                //&& svc.getVoltageRegulation().getMode() == RegulationMode.VOLTAGE_PER_REACTIVE_POWER; //TODO OPE is this expected?
    }

    public static boolean isExtensionNeededAndExportable(StaticVarCompensator svc, NetworkSerializerContext context) {
        return ExtinctExtensionSerDe.isExtensionExportable(context.getOptions(), VoltagePerReactivePowerControl.NAME, LAST_SUPPORTED_VERSION)
                && isExtensionNeeded(svc);
    }
}
