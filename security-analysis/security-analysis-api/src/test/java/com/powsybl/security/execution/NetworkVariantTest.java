/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.execution;

import com.google.common.collect.ImmutableList;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VariantManagerConstants;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
class NetworkVariantTest {

    @Test
    void test() {
        Network network = EurostagTutorialExample1Factory.create();

        network.getVariantManager().cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, ImmutableList.of("var1", "var2"));

        NetworkVariant variant2 = new NetworkVariant(network, "var2");

        assertSame(network, variant2.getNetwork());
        assertEquals("var2", variant2.getVariantId());
        assertEquals(VariantManagerConstants.INITIAL_VARIANT_ID, network.getVariantManager().getWorkingVariantId());

        assertSame(network, variant2.getVariant());
        assertEquals("var2", network.getVariantManager().getWorkingVariantId());
    }

}
