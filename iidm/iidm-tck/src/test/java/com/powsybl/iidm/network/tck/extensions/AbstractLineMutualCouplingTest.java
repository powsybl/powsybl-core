/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck.extensions;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.LineMutualCoupling;
import com.powsybl.iidm.network.extensions.LineMutualCouplingAdder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public abstract class AbstractLineMutualCouplingTest {

    @Test
    public void test() {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(ZonedDateTime.parse("2016-12-07T11:18:52.881+01:00"));
        network.getLine("NHV1_NHV2_1").newExtension(LineMutualCouplingAdder.class)
            .withR(0.5)
            .withX(1.0)
            .add();

        LineMutualCoupling lineMutualCoupling = network.getLine("NHV1_NHV2_1").getExtension(LineMutualCoupling.class);
        assertEquals(0.5, lineMutualCoupling.getR());
        assertEquals(1.0, lineMutualCoupling.getX());

        lineMutualCoupling.setR(0.52);
        lineMutualCoupling.setX(1.05);

        assertEquals(0.52, lineMutualCoupling.getR());
        assertEquals(1.05, lineMutualCoupling.getX());
    }
}
