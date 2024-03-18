/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.criteria;

import com.powsybl.iidm.criteria.translation.NetworkElement;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class NetworkElementIdListCriterionTest {
    private static NetworkElement networkElement1;
    private static NetworkElement networkElement2;
    private static NetworkElement networkElement3;

    @BeforeAll
    public static void init() {
        networkElement1 = createNetworkElement("id1");
        networkElement2 = createNetworkElement("id2");
        networkElement3 = createNetworkElement("id3");
    }

    @Test
    void typeTest() {
        assertEquals(NetworkElementCriterion.NetworkElementCriterionType.IDENTIFIER,
                new NetworkElementIdListCriterion(Set.of()).getNetworkElementCriterionType());
    }

    @Test
    void idListTest() {
        NetworkElementIdListCriterion criterion = new NetworkElementIdListCriterion(Set.of(networkElement1.getId(), networkElement3.getId()));
        assertTrue(criterion.accept(new NetworkElementVisitor(networkElement1)));
        assertFalse(criterion.accept(new NetworkElementVisitor(networkElement2)));
        assertTrue(criterion.accept(new NetworkElementVisitor(networkElement3)));
    }

    @Test
    void emptyListTest() {
        NetworkElementIdListCriterion criterion = new NetworkElementIdListCriterion(Set.of());
        assertFalse(criterion.accept(new NetworkElementVisitor(networkElement1)));
        assertFalse(criterion.accept(new NetworkElementVisitor(networkElement2)));
        assertFalse(criterion.accept(new NetworkElementVisitor(networkElement3)));
    }

    private static NetworkElement createNetworkElement(String id) {
        NetworkElement n = Mockito.mock(NetworkElement.class);
        when(n.getId()).thenReturn(id);
        return n;
    }
}


