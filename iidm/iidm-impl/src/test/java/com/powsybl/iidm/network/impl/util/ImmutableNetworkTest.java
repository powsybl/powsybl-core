/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.util;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.util.ImmutableNetwork;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static com.powsybl.iidm.network.impl.util.ImmutableTestHelper.testInvalidMethods;

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
    }
}
