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
public class UcteAngleRegulationTypeTest {

    @Test
    public void test() {
        assertEquals(2, UcteAngleRegulationType.values().length);
        assertEquals(UcteAngleRegulationType.ASYM, UcteAngleRegulationType.values()[0]);
        assertEquals(UcteAngleRegulationType.SYMM, UcteAngleRegulationType.values()[1]);
    }
}
