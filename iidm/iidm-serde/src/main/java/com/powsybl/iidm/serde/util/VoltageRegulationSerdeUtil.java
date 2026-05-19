/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.util;

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
        double voltageSetpoint = voltageRegulationHolder.getRegulatingTargetV();
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_16, context, () -> context.getWriter().writeDoubleAttribute("voltageSetpoint", voltageSetpoint));
    }

    public static void writeReactivePowerSetpoint(VoltageRegulationHolder voltageRegulationHolder, NetworkSerializerContext context) {
        double reactivePowerSetpoint = voltageRegulationHolder.getRegulatingTargetQ();
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_16, context, () -> context.getWriter().writeDoubleAttribute("reactivePowerSetpoint", reactivePowerSetpoint));
    }
}
