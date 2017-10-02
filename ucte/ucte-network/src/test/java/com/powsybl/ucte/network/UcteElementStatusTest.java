/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.network;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class UcteElementStatusTest {

    @Test
    public void test() {
        int[] status = {0, 8, 1, 9, 2, 7};

        assertEquals(6, UcteElementStatus.values().length);
        for (int i = 0; i < UcteElementStatus.values().length; ++i) {
            UcteElementStatus elementStatus = UcteElementStatus.values()[i];
            assertEquals(status[i], elementStatus.getCode());
            assertEquals(elementStatus, UcteElementStatus.fromCode(status[i]));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void unknownElementStatus() {
        UcteElementStatus.fromCode(10);
    }
}
