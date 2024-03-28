/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.tck;

import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.tck.AbstractTopologyTraverserTest;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import com.powsybl.math.graph.TraverseResult;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TopologyTraverserTest extends AbstractTopologyTraverserTest {

    @Test
    void testTerminateTraverser() {
        Network network = createMixedNodeBreakerBusBreakerNetwork();
        Terminal startGNbv = network.getGenerator("G").getTerminal();
        List<Pair<String, Integer>> visited1 = getVisitedList(startGNbv, s -> s != null && s.getId().equals("BR2") ? TraverseResult.TERMINATE_TRAVERSER : TraverseResult.CONTINUE);
        assertEquals(List.of(Pair.of("G", 0), Pair.of("BBS1", 0)), visited1);

        List<Pair<String, Integer>>visited2 = getVisitedList(startGNbv, s -> TraverseResult.CONTINUE, t -> TraverseResult.TERMINATE_TRAVERSER);
        assertEquals(List.of(Pair.of("G", 0)), visited2);

        List<Pair<String, Integer>>visited3 = getVisitedList(startGNbv, s -> TraverseResult.CONTINUE,
            t -> t.getConnectable() instanceof BusbarSection ? TraverseResult.TERMINATE_TRAVERSER : TraverseResult.CONTINUE);
        assertEquals(List.of(Pair.of("G", 0), Pair.of("BBS1", 0)), visited3);

        Terminal startLBbv = network.getLoad("LD2").getTerminal();
        List<Pair<String, Integer>>visited4 = getVisitedList(startLBbv, s -> TraverseResult.CONTINUE, t -> TraverseResult.TERMINATE_TRAVERSER);
        assertEquals(List.of(Pair.of("LD2", 0)), visited4);

        List<Pair<String, Integer>>visited5 = getVisitedList(startLBbv, s -> TraverseResult.CONTINUE,
            t -> t.getConnectable().getId().equals("L2") ? TraverseResult.TERMINATE_TRAVERSER : TraverseResult.CONTINUE);
        assertEquals(List.of(Pair.of("LD2", 0), Pair.of("L2", 1)), visited5);
    }

    @Test
    void testTraversalOrder() {
        Network network = FictitiousSwitchFactory.create();
        List<Pair<String, Integer>> visited = getVisitedList(network.getGenerator("CB").getTerminal(), s -> TraverseResult.CONTINUE);
        assertEquals(List.of(Pair.of("CB", 0), Pair.of("O", 0), Pair.of("P", 0), Pair.of("CF", 0),
                        Pair.of("CH", 0), Pair.of("CC", 0), Pair.of("CD", 0), Pair.of("CE", 0),
                        Pair.of("CJ", 1), Pair.of("CI", 1), Pair.of("CG", 0), Pair.of("CJ", 0),
                        Pair.of("D", 0), Pair.of("CI", 0)),
                visited);
    }

}
