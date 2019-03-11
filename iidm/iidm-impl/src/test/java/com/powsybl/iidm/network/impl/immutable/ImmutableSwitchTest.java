/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.immutable;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import com.powsybl.iidm.network.immutable.ImmutableNetwork;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class ImmutableSwitchTest {

    @Test
    public void testSw() {
        Network n = FictitiousSwitchFactory.create();
        Switch pppp1 = n.getSwitch("pppp");
        ImmutableNetwork network = ImmutableNetwork.of(n);
        Switch aSwitch = network.getSwitch("AB");
        Set<String> invalidMethods = new HashSet<>();
        invalidMethods.add("setOpen");
        invalidMethods.add("setRetained");
        invalidMethods.add("setFictitious");
        ImmutableTestHelper.testInvalidMethods(aSwitch, invalidMethods);

        Switch pppp = network.getSwitch("pppp");
    }
}
