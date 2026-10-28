/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class LoadingLimitsBugTest extends AbstractIidmSerDeTest {

    @Test
    void test() throws IOException {
        var network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(ZonedDateTime.parse("2020-07-16T10:08:48.321+02:00"));
        // to reproduce the bug we need a 2 windings transformer without any data
        // that could create a sub element in the XML except apparent and active power limits
        // (tap changer, properties, current limits)
        var twt = network.getSubstation("P1").newTwoWindingsTransformer()
                .setId("TWT")
                .setVoltageLevel1("VLGEN")
                .setBus1("NGEN")
                .setConnectableBus1("NGEN")
                .setRatedU1(24.0)
                .setVoltageLevel2("VLHV1")
                .setBus2("NHV1")
                .setConnectableBus2("NHV1")
                .setRatedU2(400.0)
                .setR(1)
                .setX(1)
                .setG(0.0)
                .setB(0.0)
                .add();
        twt.getOrCreateSelectedOperationalLimitsGroup1().newApparentPowerLimits()
                .setPermanentLimit(100)
                .add();
        // check that XIIDM 1.5 is not ill-formed
        allFormatsRoundTripTest(network, "/loading-limits-bug.xml",
                new ExportOptions().setVersion(IidmVersion.V_1_5.toString(".")));
    }
}
