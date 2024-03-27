/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class EmptyContingencyListProviderTest {

    @Test
    void test() {
        ContingenciesProviderFactory factory = new EmptyContingencyListProviderFactory();
        ContingenciesProvider provider = factory.create();

        assertTrue(provider instanceof EmptyContingencyListProvider);
        assertEquals(0, provider.getContingencies(null).size());
    }
}
