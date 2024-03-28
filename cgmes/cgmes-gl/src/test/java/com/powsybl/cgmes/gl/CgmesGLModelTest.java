/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.gl;

import com.powsybl.triplestore.api.PropertyBags;
import com.powsybl.triplestore.api.QueryCatalog;
import com.powsybl.triplestore.api.TripleStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
class CgmesGLModelTest extends AbstractCgmesGLTest {

    protected TripleStore tripleStore;
    protected QueryCatalog queryCatalog;
    protected CgmesGLModel cgmesGLModel;

    @BeforeEach
    void setUp() {
        super.setUp();
        tripleStore = Mockito.mock(TripleStore.class);
        queryCatalog = Mockito.mock(QueryCatalog.class);
        setQuery(CgmesGLModel.SUBSTASTION_POSITION_QUERY_KEY, "SubstationQuery", substationsPropertyBags);
        setQuery(CgmesGLModel.LINE_POSITION_QUERY_KEY, "LineQuery", linesPropertyBags);
        cgmesGLModel = new CgmesGLModel(tripleStore, queryCatalog);
    }

    protected void setQuery(String key, String query, PropertyBags queryResults) {
        Mockito.when(queryCatalog.get(key)).thenReturn(query);
        Mockito.when(tripleStore.query(query)).thenReturn(queryResults);
    }

    protected void removeQueryCatalogKey(String key) {
        Mockito.when(queryCatalog.get(key)).thenReturn(null);
    }

    @Test
    void getSubstationsPosition() {
        assertEquals(substationsPropertyBags, cgmesGLModel.getSubstationsPosition());
        removeQueryCatalogKey(CgmesGLModel.SUBSTASTION_POSITION_QUERY_KEY);
        assertEquals(new PropertyBags(), cgmesGLModel.getSubstationsPosition());
    }

    @Test
    void getLinesPosition() {
        assertEquals(linesPropertyBags, cgmesGLModel.getLinesPositions());
        removeQueryCatalogKey(CgmesGLModel.LINE_POSITION_QUERY_KEY);
        assertEquals(new PropertyBags(), cgmesGLModel.getLinesPositions());
    }

}
