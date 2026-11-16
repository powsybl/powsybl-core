/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.ref;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
class RefTest {

    @Test
    void testRefObj() {
        Object object0 = new Object();
        Object object1 = new Object();

        RefObj<Object> ref = new RefObj<>(object0);
        assertEquals(object0, ref.get());

        ref.set(object1);
        assertEquals(object1, ref.get());
    }

    @Test
    void testRefChain() {
        Object object1 = new Object();
        Object object2 = new Object();

        RefObj<Object> ref1 = new RefObj<>(object1);
        RefObj<Object> ref2 = new RefObj<>(object1);
        RefChain<Object> refChain = new RefChain<>(ref1);
        assertEquals(object1, refChain.get());

        refChain.setRef(ref2);
        ref1.set(object2);
        assertEquals(object1, refChain.get());

        ref2.set(object2);
        assertEquals(object2, refChain.get());
    }
}
