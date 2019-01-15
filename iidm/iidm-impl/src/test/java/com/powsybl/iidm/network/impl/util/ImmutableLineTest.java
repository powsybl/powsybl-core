/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.util;

import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TieLine;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;
import com.powsybl.iidm.network.util.ImmutableLine;
import com.powsybl.iidm.network.util.ImmutableNetwork;
import com.powsybl.iidm.network.util.ImmutableTieLine;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class ImmutableLineTest {

    private static final Set<String> INVALID_LINE_METHODS = new HashSet<>();
    static {
        INVALID_LINE_METHODS.add("setR");
        INVALID_LINE_METHODS.add("setX");
        INVALID_LINE_METHODS.add("setG1");
        INVALID_LINE_METHODS.add("setB1");
        INVALID_LINE_METHODS.add("setG2");
        INVALID_LINE_METHODS.add("setB2");
        INVALID_LINE_METHODS.add("newCurrentLimits1");
        INVALID_LINE_METHODS.add("newCurrentLimits2");
        INVALID_LINE_METHODS.add("remove");
    }

    @Test
    public void testImmutableLine() {
        Network network = ImmutableNetwork.of(EurostagTutorialExample1Factory.createWithCurrentLimits());
        Line line = network.getLine("NHV1_NHV2_1");
        assertTrue(line instanceof ImmutableLine);
        ImmutableTestHelper.testInvalidMethods(line, INVALID_LINE_METHODS);
    }

    @Test
    public void testImmutableTieLine() {
        Network network = ImmutableNetwork.of(NoEquipmentNetworkFactory.createWithTieLine());
        Line line = network.getLine("testTie");
        assertTrue(line.isTieLine());
        assertTrue(line instanceof ImmutableTieLine);
        TieLine tieLine = (TieLine) line;
        ImmutableTestHelper.testInvalidMethods(tieLine, INVALID_LINE_METHODS);

        TieLine.HalfLine half = tieLine.getHalf1();
        Set<String> invalidHalfLineMethods = new HashSet<>();
        invalidHalfLineMethods.add("setR");
        invalidHalfLineMethods.add("setX");
        invalidHalfLineMethods.add("setG1");
        invalidHalfLineMethods.add("setB1");
        invalidHalfLineMethods.add("setG2");
        invalidHalfLineMethods.add("setB2");
        invalidHalfLineMethods.add("setXnodeP");
        invalidHalfLineMethods.add("setXnodeQ");
        ImmutableTestHelper.testInvalidMethods(half, invalidHalfLineMethods);
    }

    @Test
    public void testImmutableDanglingLine() {

    }
}
