/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class BranchDataTest extends AbstractLoadFlowResultsCompletionTest {

    @Test
    public void testLine() {
        BranchData lineData = new BranchData(line, 0.1f, false);
        assertEquals(lineP1, lineData.getComputedP1(), 0.0001f);
        assertEquals(lineQ1, lineData.getComputedQ1(), 0.0001f);
        assertEquals(lineP2, lineData.getComputedP2(), 0.0001f);
        assertEquals(lineQ2, lineData.getComputedQ2(), 0.0001f);
    }

    @Test
    public void testTransformer() {
        BranchData twtData = new BranchData(transformer, 0.1f, false, true);
        assertEquals(twtP1, twtData.getComputedP1(), 0.0001f);
        assertEquals(twtQ1, twtData.getComputedQ1(), 0.0001f);
        assertEquals(twtP2, twtData.getComputedP2(), 0.0001f);
        assertEquals(twtQ2, twtData.getComputedQ2(), 0.0001f);
    }

}
