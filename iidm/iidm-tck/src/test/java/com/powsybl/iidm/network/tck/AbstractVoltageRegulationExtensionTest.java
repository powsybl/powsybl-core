/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractVoltageRegulationExtensionTest {

    private Network network;
    private VoltageLevel voltageLevel;

    @BeforeEach
    public void initNetwork() {
        network = FictitiousSwitchFactory.create();
        voltageLevel = network.getVoltageLevel("C");
    }

    @Test
    public void testSetterGetter() {
        RegulationMode mode = RegulationMode.VOLTAGE;
        boolean regulating = true;
        double targetV = 24.1;
        Generator generator = createGeneratorAdder(10.0, false, 20.0)
            .newVoltageRegulation()
                .withMode(mode)
                .withRegulating(regulating)
                .withTargetValue(targetV)
                .add()
            .add();
        assertNotNull(generator);
        VoltageRegulation voltageRegulation = generator.getVoltageRegulation();
        assertNotNull(voltageRegulation);
        assertEquals(targetV, voltageRegulation.getTargetValue());
        assertEquals(mode, voltageRegulation.getMode());
        assertNull(voltageRegulation.getTerminal());
        assertEquals(Double.NaN, voltageRegulation.getTargetDeadband());
        assertEquals(Double.NaN, voltageRegulation.getSlope());
    }

    private GeneratorAdder createGeneratorAdder(double reactivePowerSetpoint, boolean regulatorOn, double voltageSetpoint) {
        return voltageLevel.newGenerator()
            .setId("ID")
            .setVoltageRegulatorOn(regulatorOn)
            .setEnergySource(EnergySource.HYDRO)
            .setMaxP(20.0)
            .setMinP(0.0)
            .setRatedS(40.0)
            .setTargetP(5.0)
            .setTargetQ(reactivePowerSetpoint)
            .setNode(1)
            .setTargetV(voltageSetpoint);
    }
}
