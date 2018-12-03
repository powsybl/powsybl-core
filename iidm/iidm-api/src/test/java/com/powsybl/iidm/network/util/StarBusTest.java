/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class StarBusTest extends AbstractTwtDataTest {

    @Test
    public void test() {
        StarBus starBus = new StarBus(twt);

        assertEquals(412.66200701692287, starBus.getU(), .0001);
        assertEquals(-7.353686938578365, Math.toDegrees(starBus.getTheta()), .0001);
    }

}
