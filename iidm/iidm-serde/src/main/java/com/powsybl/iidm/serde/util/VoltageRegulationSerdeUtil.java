/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.util;

import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolder;
import com.powsybl.iidm.serde.IidmVersion;
import com.powsybl.iidm.serde.NetworkSerializerContext;

/**
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
public final class VoltageRegulationSerdeUtil {
    private VoltageRegulationSerdeUtil() {
        /* This utility class should not be instantiated */
    }

    public static void writeVoltageSetpoint(VoltageRegulationHolder voltageRegulationHolder, NetworkSerializerContext context) {
        double voltageSetpoint;
        if (voltageRegulationHolder.isWithMode(RegulationMode.REACTIVE_POWER)) {
            voltageSetpoint = voltageRegulationHolder.getTargetV();
        } else {
            voltageSetpoint = voltageRegulationHolder.getVoltageRegulation() != null ? voltageRegulationHolder.getVoltageRegulation().getTargetValue() : Double.NaN;
        }
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_15, context, () -> context.getWriter().writeDoubleAttribute("voltageSetpoint", voltageSetpoint));
    }

    public static void writeReactivePowerSetpoint(VoltageRegulationHolder voltageRegulationHolder, NetworkSerializerContext context) {
        double reactivePowerSetpoint;
        if (voltageRegulationHolder.isWithMode(RegulationMode.VOLTAGE)) {
            reactivePowerSetpoint = voltageRegulationHolder.getTargetQ();
        } else {
            reactivePowerSetpoint = voltageRegulationHolder.getVoltageRegulation() != null ? voltageRegulationHolder.getVoltageRegulation().getTargetValue() : Double.NaN;
        }
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_15, context, () -> context.getWriter().writeDoubleAttribute("reactivePowerSetpoint", reactivePowerSetpoint));
    }
}
