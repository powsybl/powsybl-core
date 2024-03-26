/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class ContingencyContextTest {

    @Test
    void test() {
        ContingencyContext context = new ContingencyContext("c1", ContingencyContextType.SPECIFIC);
        assertEquals("ContingencyContext(contingencyIds=[c1], contextType=SPECIFIC)", context.toString());
    }

    @Test
    void testOnlyContingencies() {
        ContingencyContext context = ContingencyContext.create(Collections.emptyList(), ContingencyContextType.ONLY_CONTINGENCIES);
        assertEquals("ContingencyContext(contingencyIds=[], contextType=ONLY_CONTINGENCIES)", context.toString());
    }

    @Test
    void testSeveralContingencies() {
        List<String> contingenciesIds = new ArrayList<>();
        contingenciesIds.add("c1");
        contingenciesIds.add("c2");
        contingenciesIds.add("c3");
        ContingencyContext context = new ContingencyContext(contingenciesIds, ContingencyContextType.SPECIFIC);
        assertEquals("ContingencyContext(contingencyIds=[c1, c2, c3], contextType=SPECIFIC)", context.toString());
    }
}
