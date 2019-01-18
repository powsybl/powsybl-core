/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.util;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.util.*;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static com.powsybl.iidm.network.impl.util.ImmutableTestHelper.testInvalidMethods;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ImmutableNetworkTest {

    @Test
    public void test() {
        Network network = ImmutableNetwork.of(EurostagTutorialExample1Factory.create());
        Set<String> expectedInvalidMethods = new HashSet<>();
        expectedInvalidMethods.add("setForecastDistance");
        expectedInvalidMethods.add("setCaseDate");
        expectedInvalidMethods.add("newSubstation");
        expectedInvalidMethods.add("newLine");
        expectedInvalidMethods.add("newTieLine");
        expectedInvalidMethods.add("newHvdcLine");
        testInvalidMethods(network, expectedInvalidMethods);

        Substation sub = network.getSubstation("P1");
        assertTrue(sub instanceof ImmutableSubstation);
        assertSame(network, sub.getNetwork());
        Set<String> invalidSubMethods = new HashSet<>();
        invalidSubMethods.add("setTso");
        invalidSubMethods.add("setCountry");
        invalidSubMethods.add("newVoltageLevel");
        invalidSubMethods.add("newTwoWindingsTransformer");
        invalidSubMethods.add("newThreeWindingsTransformer");
        invalidSubMethods.add("addGeographicalTag");
        testInvalidMethods(sub, invalidSubMethods);

        VoltageLevel vl = network.getVoltageLevel("VLGEN");
        assertTrue(vl instanceof ImmutableVoltageLevel);
        assertSame(sub, vl.getSubstation());
        Set<String> invalidVlMethods = new HashSet<>();
        invalidVlMethods.add("setNominalV");
        invalidVlMethods.add("setLowVoltageLimit");
        invalidVlMethods.add("setHighVoltageLimit");
        invalidVlMethods.add("newGenerator");
        invalidVlMethods.add("newLoad");
        invalidVlMethods.add("newShuntCompensator");
        invalidVlMethods.add("newDanglingLine");
        invalidVlMethods.add("newStaticVarCompensator");
        invalidVlMethods.add("newVscConverterStation");
        invalidVlMethods.add("newLccConverterStation");
        testInvalidMethods(vl, invalidVlMethods);

    }

    @Test
    public void testImmutableTerminalAndBus() {
        Network n = EurostagTutorialExample1Factory.createWithCurrentLimits();
        Terminal t = n.getLine("NHV1_NHV2_1").getTerminal1();
        Bus b = t.getBusView().getBus();
        Component c = b.getConnectedComponent();

        Network immutableNetwork = ImmutableNetwork.of(n);
        Terminal immutableTerminal = immutableNetwork.getLine("NHV1_NHV2_1").getTerminal1();
        assertTrue(immutableTerminal instanceof ImmutableTerminal);
        Bus immutableBus = immutableTerminal.getBusView().getBus();
        assertTrue(immutableBus instanceof ImmutableBus);
        Component immutableComponent = immutableBus.getConnectedComponent();
        assertTrue(immutableComponent instanceof ImmutableComponent);
        assertTrue(immutableBus.getVoltageLevel() instanceof ImmutableVoltageLevel);

        Set<String> invalidsTerminalMethods = new HashSet<>();
        invalidsTerminalMethods.add("setP");
        invalidsTerminalMethods.add("setQ");
        invalidsTerminalMethods.add("connect");
        invalidsTerminalMethods.add("disconnect");
        ImmutableTestHelper.testInvalidMethods(immutableTerminal, invalidsTerminalMethods);

        Set<String> invalidsBusMethods = new HashSet<>();
        invalidsBusMethods.add("setV");
        invalidsBusMethods.add("setAngle");
        ImmutableTestHelper.testInvalidMethods(immutableBus, invalidsBusMethods);

        assertTrue(immutableTerminal.getBusView().getBus() instanceof ImmutableBus);
        assertTrue(immutableTerminal.getBusView().getConnectableBus() instanceof ImmutableBus);

    }
}
