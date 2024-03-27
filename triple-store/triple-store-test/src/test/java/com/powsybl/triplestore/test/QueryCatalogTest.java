/**
 * Copyright (c) 2018, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.triplestore.test;

import com.powsybl.triplestore.api.QueryCatalog;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class QueryCatalogTest {

    @Test
    void testHashCodeEquals() {
        QueryCatalog q1 = new QueryCatalog("foaf/foaf-graphs.sparql");
        QueryCatalog q2 = new QueryCatalog("foaf/foaf-graphs.sparql");
        QueryCatalog q3 = new QueryCatalog("foaf/foaf-graphs-copy.sparql");
        QueryCatalog q4 = new QueryCatalog("foaf/foaf-optionals.sparql");
        assertEquals(q1.hashCode(), q2.hashCode());
        assertNotEquals(q1.hashCode(), q3.hashCode());
        assertNotEquals(q1.hashCode(), q4.hashCode());
        assertEquals(q1, q2);
        assertNotEquals(q1, q3);
        assertNotEquals(q1, q4);
    }
}
