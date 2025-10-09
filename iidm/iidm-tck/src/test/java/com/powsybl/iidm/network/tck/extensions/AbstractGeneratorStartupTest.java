/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck.extensions;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.GeneratorStartup;
import com.powsybl.iidm.network.extensions.GeneratorStartupAdder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
public abstract class AbstractGeneratorStartupTest {

    @Test
    public void test() {
        Network network = EurostagTutorialExample1Factory.create();
        Generator generator = network.getGenerator("GEN");
        GeneratorStartup startup = generator.newExtension(GeneratorStartupAdder.class)
                .withPlannedActivePowerSetpoint(600.0)
                .withStartupCost(5.0)
                .withMarginalCost(10.0)
                .withPlannedOutageRate(0.8)
                .withForcedOutageRate(0.7)
                .add();
        assertEquals(600.0, startup.getPlannedActivePowerSetpoint(), 0.0);
        assertEquals(5.0, startup.getStartupCost(), 0.0);
        assertEquals(10.0, startup.getMarginalCost(), 0.0);
        assertEquals(0.8, startup.getPlannedOutageRate(), 0.0);
        assertEquals(0.7, startup.getForcedOutageRate(), 0.0);
        startup.setPlannedActivePowerSetpoint(610.0).setStartupCost(4.0).setMarginalCost(12.0).setPlannedOutageRate(0.7).setForcedOutageRate(0.8);
        assertEquals(610.0, startup.getPlannedActivePowerSetpoint(), 0.0);
        assertEquals(4.0, startup.getStartupCost(), 0.0);
        assertEquals(12.0, startup.getMarginalCost(), 0.0);
        assertEquals(0.7, startup.getPlannedOutageRate(), 0.0);
        assertEquals(0.8, startup.getForcedOutageRate(), 0.0);
    }
}
