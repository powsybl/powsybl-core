/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.util;

import com.powsybl.iidm.network.HvdcConverterStation;
import com.powsybl.iidm.network.LccConverterStation;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VscConverterStation;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import com.powsybl.iidm.network.util.ImmutableLccConverterStation;
import com.powsybl.iidm.network.util.ImmutableNetwork;
import com.powsybl.iidm.network.util.ImmutableVscConverterStation;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.Assert.assertTrue;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class ImmutableConverterStationTest {

    @Test
    public void testVsc() {
        Network network = new ImmutableNetwork(HvdcTestNetwork.createVsc());
        VscConverterStation c1 = network.getVscConverterStation("C1");
        assertTrue(c1 instanceof ImmutableVscConverterStation);
        Set<String> invalidVscMethods = new HashSet<>();
        invalidVscMethods.add("setLossFactor"); // HvdcConverterStation
        // vsc
        invalidVscMethods.add("setVoltageRegulatorOn");
        invalidVscMethods.add("setVoltageSetpoint");
        invalidVscMethods.add("setReactivePowerSetpoint");

        invalidVscMethods.addAll(ImmutableTestHelper.NEW_REACTIVE);
        invalidVscMethods.add("remove");
        ImmutableTestHelper.testInvalidMethods(c1, invalidVscMethods);

        Stream<HvdcConverterStation<?>> csStream = network.getHvdcConverterStationStream();
        csStream.forEach(cs -> assertTrue(cs instanceof ImmutableLccConverterStation || cs instanceof ImmutableVscConverterStation));
    }

    @Test
    public void testLcc() {
        Network network = new ImmutableNetwork(HvdcTestNetwork.createLcc());
        LccConverterStation c1 = network.getLccConverterStation("C1");
        assertTrue(c1 instanceof ImmutableLccConverterStation);
        Set<String> invalidLccMethods = new HashSet<>();
        invalidLccMethods.add("setLossFactor");
        invalidLccMethods.add("setPowerFactor");
        invalidLccMethods.add("remove");
        ImmutableTestHelper.testInvalidMethods(c1, invalidLccMethods);
    }

}
