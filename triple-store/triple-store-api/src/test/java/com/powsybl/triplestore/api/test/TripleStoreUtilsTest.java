/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.triplestore.api.test;

import com.powsybl.triplestore.api.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public class TripleStoreUtilsTest {

    private final TripleStore tripleStore = Mockito.mock(TripleStore.class);
    private final QueryCatalog queryCatalog = Mockito.mock(QueryCatalog.class);

    @Before
    public void setUp() {
        Mockito.when(queryCatalog.get(Mockito.anyString())).thenReturn(null);
        Mockito.when(queryCatalog.get("test")).thenReturn("test");
        PropertyBags bags = new PropertyBags();
        PropertyBag p = new PropertyBag(List.of("test"));
        p.put("test", "test");
        bags.add(p);
        Mockito.when(tripleStore.query("test")).thenReturn(bags);

    }

    @Test
    public void test() {
        PropertyBags result = TripleStoreUtils.queryTripleStore("test", queryCatalog, tripleStore);
        assertEquals(1, result.size());
        PropertyBag p = result.get(0);
        assertEquals(1, p.size());
        assertEquals("test", p.get("test"));
    }

    @Test
    public void notExistingQueryTest() {
        PropertyBags result = TripleStoreUtils.queryTripleStore("empty", queryCatalog, tripleStore);
        assertTrue(result.isEmpty());
    }
}
