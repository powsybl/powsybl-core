/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.util;

import com.powsybl.iidm.network.ConnectableType;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;
import com.powsybl.iidm.network.util.ImmutableDanglingLine;
import com.powsybl.iidm.network.util.ImmutableGenerator;
import com.powsybl.iidm.network.util.ImmutableLoad;
import com.powsybl.iidm.network.util.ImmutableNetwork;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class ImmutableInjectionTest {

    @Test
    public void testGenerator() {
        ImmutableNetwork network = ImmutableNetwork.of(EurostagTutorialExample1Factory.create());
        Generator generator = network.getGenerator("GEN");
        assertTrue(generator instanceof ImmutableGenerator);
        assertEquals(ConnectableType.GENERATOR, generator.getType());
        Set<String> expectedInvalidMethods = new HashSet<>();
        expectedInvalidMethods.add("setMaxP");
        expectedInvalidMethods.add("setMinP");
        expectedInvalidMethods.add("setTargetP");
        expectedInvalidMethods.add("setTargetQ");
        expectedInvalidMethods.add("setTargetV");
        expectedInvalidMethods.add("setRatedS");
        expectedInvalidMethods.add("setEnergySource");
        expectedInvalidMethods.add("setVoltageRegulatorOn");
        expectedInvalidMethods.add("setRegulatingTerminal");
        expectedInvalidMethods.add("remove");
        expectedInvalidMethods.addAll(ImmutableTestHelper.NEW_REACTIVE);
        ImmutableTestHelper.testInvalidMethods(generator, expectedInvalidMethods);
    }

    @Test
    public void testLoad() {
        ImmutableNetwork network = ImmutableNetwork.of(EurostagTutorialExample1Factory.create());
        Load load = network.getLoad("LOAD");
        assertTrue(load instanceof ImmutableLoad);
        Set<String> expectedInvalidMethods = new HashSet<>();
        expectedInvalidMethods.add("setQ0");
        expectedInvalidMethods.add("setP0");
        expectedInvalidMethods.add("setLoadType");
        expectedInvalidMethods.add("remove");
        ImmutableTestHelper.testInvalidMethods(load, expectedInvalidMethods);
    }

    @Test
    public void testDanglingLine() {
        ImmutableNetwork network = ImmutableNetwork.of(NoEquipmentNetworkFactory.createWithDanglingLine());
        DanglingLine dl = network.getDanglingLine("DL");
        assertTrue(dl instanceof ImmutableDanglingLine);
        Set<String> invalidDanglingLineMethods = new HashSet<>(ImmutableTestHelper.RXGB_SETTERS);
        invalidDanglingLineMethods.add("setP0");
        invalidDanglingLineMethods.add("setQ0");
        invalidDanglingLineMethods.add("newCurrentLimits");
        invalidDanglingLineMethods.add("remove");
        ImmutableTestHelper.testInvalidMethods(dl, invalidDanglingLineMethods);

    }
}
