/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.tck.extensions;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.impl.extensions.ManualFrequencyRestorationReserveAdderImpl;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Jacques Borsenberger {literal <jacques.borsenberger at rte-france.com}
 */
class ManualFrequencyRestorationReserveTest {

    @Test
    void testTrueSetIsTrueGet() {
        Network network = EurostagTutorialExample1Factory.create();
        var generator = network.getGenerator("GEN");
        var mFRR = generator.newExtension(ManualFrequencyRestorationReserveAdderImpl.class)
                 .withParticipate(true)
                 .add();
        assertNotNull(mFRR);
        assertTrue(mFRR.isParticipate());
        mFRR.setParticipate(false);
        assertFalse(mFRR.isParticipate());
    }

}
