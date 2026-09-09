/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck.voltage.regulation.backward.compatibility;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolder;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
public abstract class AbstractVoltageRegulationBackwardCompatibilityCommon {

    public static final String LOCAL_BUS = "NGEN";
    public Network network;
    public VoltageLevel voltageLevel;
    public Terminal remoteTerminal;

    @BeforeEach
    void initNetwork() {
        network = BatteryNetworkFactory.create();
        voltageLevel = network.getVoltageLevel("VLGEN");
        remoteTerminal = network.getBattery("BAT").getTerminal();
    }

    public <T extends VoltageRegulationHolder<T>> void checkVoltageRegulationAttributes(VoltageRegulationAttributesToCheck expected, T actual) {
        assertNotNull(expected);
        assertNotNull(actual);

        assertEquals(expected.mode() != null, actual.isWithMode(expected.mode()));

        if (expected.terminal() != null) {
            assertEquals(expected.terminal().getConnectable().getId(), actual.getRegulatingTerminal().getConnectable().getId());
        } else {
            if (actual.getVoltageRegulation() != null) {
                assertNull(actual.getVoltageRegulation().getTerminal());
            }
        }

        assertEquals(expected.localTargetQ(), actual.getLocalTargetQ());
        assertEquals(expected.localTargetV(), actual.getLocalTargetV());
        if (expected.withVoltageRegulationPresent) {
            assertEquals(expected.targetValue(), actual.getVoltageRegulation().getTargetValue());
        } else {
            assertNull(actual.getVoltageRegulation());
        }

        assertEquals(expected.isRegulating(), actual.isRegulating());
        assertEquals(expected.terminal() != null, actual.isRemoteRegulating());

        if (actual.getVoltageRegulation() != null) {
            assertEquals(expected.targetDeadband(), actual.getVoltageRegulation().getTargetDeadband());

            assertEquals(expected.slope(), actual.getVoltageRegulation().getSlope());
        }
    }

    public record VoltageRegulationAttributesToCheck(
        double localTargetV,
        double localTargetQ,
        double targetValue,
        double targetDeadband,
        double slope,
        RegulationMode mode,
        boolean isRegulating,
        Terminal terminal,
        boolean withVoltageRegulationPresent
    ) {
    }

}
