/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.factors;

import com.powsybl.sensitivity.factors.functions.BranchFlow;
import com.powsybl.sensitivity.factors.variables.LinearGlsk;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertSame;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class BranchFlowPerLinearGlskTest {

    private BranchFlow branchFlow;
    private LinearGlsk glsk;
    private BranchFlowPerLinearGlsk factor;

    @Before
    public void setUp() {
        branchFlow = Mockito.mock(BranchFlow.class);
        Map<String, Float> map = new HashMap<>();
        map.put("Generator", 70f);
        map.put("Load", 30f);
        glsk = new LinearGlsk("GLSK id", "GLSK name", map);
        factor = new BranchFlowPerLinearGlsk(branchFlow, glsk);
    }

    @Test
    public void getFunction() {
        assertSame(branchFlow, factor.getFunction());
    }

    @Test
    public void getVariable() {
        assertSame(glsk, factor.getVariable());
    }

}
