/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Alice Caron {@literal <alice.caron at rte-france.com>}
 */
public abstract class AbstractIdentifiableTest {

    @Test
    public void test() {
        Network network = EurostagTutorialExample1Factory.create();

        // Test getSortIndex is implemented
        assertEquals(11, network.getLine("NHV1_NHV2_1").getSortIndex());
        assertEquals(12, network.getLine("NHV1_NHV2_2").getSortIndex());
        assertEquals(13, network.getTwoWindingsTransformer("NGEN_NHV1").getSortIndex());
        assertEquals(14, network.getTwoWindingsTransformer("NHV2_NLOAD").getSortIndex());
    }
}
