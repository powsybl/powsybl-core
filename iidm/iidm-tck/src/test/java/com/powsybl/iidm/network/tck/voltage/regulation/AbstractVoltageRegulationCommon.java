/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck.voltage.regulation;

import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolder;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.assertEquals;

// TODO MSA Complete me with some extra tests like removeTerminal on multiVariants
/**
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
abstract class AbstractVoltageRegulationCommon<T extends VoltageRegulationHolder<T>> {

    VoltageLevel voltageLevel;
    Terminal remoteTerminal;
    Network network;

    @BeforeEach
    void initNetwork() {
        network = BatteryNetworkFactory.create();
        voltageLevel = network.getVoltageLevel("VLGEN");
        remoteTerminal = network.getBattery("BAT").getTerminal();
    }

    public void changeTerminalTest(T holder) {
        var vlgen = network.getVoltageLevel("VLGEN");
        Bus ngen = vlgen.getBusBreakerView().getBus("NGEN");

        var battery2 = network.getBattery("BAT2");
        Terminal remoteTerminal2 = battery2.getTerminal();

        Battery battery3 = vlgen.newBattery()
            .setId("BAT3")
            .setBus(ngen.getId())
            .setConnectableBus(ngen.getId())
            .setTargetP(9999.99)
            .setLocalTargetQ(9999.99)
            .setMinP(-9999.99)
            .setMaxP(9999.99)
            .add();
        battery3.newMinMaxReactiveLimits()
            .setMinQ(-9999.99)
            .setMaxQ(9999.99)
            .add();
        battery3.getTerminal().setP(-605);
        battery3.getTerminal().setQ(-225);
        Terminal remoteTerminal3 = battery3.getTerminal();

        double localTargetV = 25.0;
        double targetValue = 50.0;
        double newTargetValue = 120.0;
        VoltageRegulation voltageRegulation = holder.newVoltageRegulation()
            .withTerminal(remoteTerminal2)
            .withMode(RegulationMode.VOLTAGE)
            .withTargetValue(targetValue)
            .withTargetDeadband(15.0)
            .build();

        assertEquals(remoteTerminal2, voltageRegulation.getTerminal());
        assertEquals(targetValue, holder.getRegulatingTargetV());
        voltageRegulation.setTerminal(remoteTerminal3, newTargetValue);
        assertEquals(newTargetValue, holder.getRegulatingTargetV());
        assertEquals(remoteTerminal3, voltageRegulation.getTerminal());
        // Removing battery 2 should not change the regulating terminal
        battery2.remove();
        assertEquals(newTargetValue, holder.getRegulatingTargetV());
        assertEquals(remoteTerminal3, voltageRegulation.getTerminal());
        // Removing battery 3 should change the regulating terminal to the local one (fallback)
        battery3.remove();
        assertEquals(newTargetValue, holder.getRegulatingTargetV());
        assertEquals(holder.getTerminal(), holder.getRegulatingTerminal());
        // Switch to local regulation (this was already the case)
        holder.setLocalTargetV(localTargetV);
        voltageRegulation.setTerminal(null, Double.NaN);
        assertEquals(localTargetV, holder.getRegulatingTargetV());
        assertEquals(holder.getTerminal(), holder.getRegulatingTerminal());
    }

}
