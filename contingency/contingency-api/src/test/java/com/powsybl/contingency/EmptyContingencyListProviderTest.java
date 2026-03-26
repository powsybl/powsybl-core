/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency;

import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class EmptyContingencyListProviderTest {

    @Test
    void test() {
        ContingenciesProviderFactory factory = new EmptyContingencyListProviderFactory();
        ContingenciesProvider provider = factory.create();

        assertInstanceOf(EmptyContingencyListProvider.class, provider);
        assertEquals(0, provider.getContingencies(null).size());
        assertEquals(0, provider.getContingencies(null, new HashMap<>()).size());
    }
}
