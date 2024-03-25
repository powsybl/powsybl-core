/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.comparator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.powsybl.security.PostContingencyComputationStatus;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.powsybl.contingency.Contingency;
import com.powsybl.security.results.PostContingencyResult;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
class PostContingencyResultComparatorTest {

    @Test
    void compare() {
        Contingency contingency1 = Mockito.mock(Contingency.class);
        Mockito.when(contingency1.getId()).thenReturn("contingency1");
        PostContingencyResult result1 = new PostContingencyResult(contingency1, PostContingencyComputationStatus.CONVERGED, Collections.emptyList());
        Contingency contingency2 = Mockito.mock(Contingency.class);
        Mockito.when(contingency2.getId()).thenReturn("contingency2");
        PostContingencyResult result2 = new PostContingencyResult(contingency2, PostContingencyComputationStatus.CONVERGED, Collections.emptyList());
        Contingency contingency3 = Mockito.mock(Contingency.class);
        Mockito.when(contingency3.getId()).thenReturn("contingency3");
        PostContingencyResult result3 = new PostContingencyResult(contingency3, PostContingencyComputationStatus.CONVERGED, Collections.emptyList());

        List<PostContingencyResult> results = Arrays.asList(result3, result1, result2);
        Collections.sort(results, new PostContingencyResultComparator());

        assertEquals(result1, results.get(0));
        assertEquals(result2, results.get(1));
        assertEquals(result3, results.get(2));
    }

}
