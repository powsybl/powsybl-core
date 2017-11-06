/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.network;

import java.util.Objects;

import static com.powsybl.ucte.network.UcteElementStatus.REAL_ELEMENT_OUT_OF_OPERATION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
abstract class AbstractUcteElementTest {

    protected void testElement(UcteElement element) {
        Objects.requireNonNull(element);

        element.setStatus(REAL_ELEMENT_OUT_OF_OPERATION);
        assertEquals(REAL_ELEMENT_OUT_OF_OPERATION, element.getStatus());

        element.setResistance(1.1f);
        assertEquals(1.1f, element.getResistance(), 0.0f);

        element.setReactance(2.1f);
        assertEquals(2.1f, element.getReactance(), 0.0f);

        element.setSusceptance(3.1f);
        assertEquals(3.1f, element.getSusceptance(), 0.0f);

        element.setCurrentLimit(2000);
        assertEquals(Integer.valueOf(2000), element.getCurrentLimit());
        element.setCurrentLimit(null);
        assertNull(element.getCurrentLimit());

        element.setElementName("ELEMENT_NAME");
        assertEquals("ELEMENT_NAME", element.getElementName());
        element.setElementName(null);
        assertNull(element.getElementName());
    }
}
