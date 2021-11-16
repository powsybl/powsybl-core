/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.math.graph.TraverseResult;
import org.junit.Test;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class TopologyTraverserAdapterTest {

    @Test
    public void test() {
        MergingView cgm = MergingViewFactory.createCGM(null);
        Terminal start1 = cgm.getLoad("LOAD1").getTerminal();
        List<String> visited1 = recordVisited(start1, s -> TraverseResult.CONTINUE);
        assertEquals(Arrays.asList("LOAD1", "BBS1", "DL1 + DL2"), visited1);

        List<String> visited1bis = recordVisited(start1,
            aSwitch -> !aSwitch.isOpen() && aSwitch.getKind() != SwitchKind.BREAKER ? TraverseResult.CONTINUE : TraverseResult.TERMINATE_PATH);
        assertEquals(Collections.singletonList("LOAD1"), visited1bis);

        Terminal start2 = cgm.getLoad("LOAD2").getTerminal();
        List<String> visited2 = recordVisited(start2, s -> TraverseResult.CONTINUE);

        assertEquals(Arrays.asList("LOAD2", "DL1 + DL2"), visited2);

        List<String> visited3 = recordVisited(start2, s -> TraverseResult.CONTINUE,
            t -> t.getConnectable().getId().equals("DL1 + DL2") ? TraverseResult.TERMINATE_TRAVERSER : TraverseResult.CONTINUE);
        assertEquals(Arrays.asList("LOAD2", "DL1 + DL2"), visited3);
    }

    private List<String> recordVisited(Terminal start, Function<Switch, TraverseResult> switchPredicate) {
        return recordVisited(start, switchPredicate, t -> TraverseResult.CONTINUE);
    }

    private List<String> recordVisited(Terminal start, Function<Switch, TraverseResult> switchTest, Function<Terminal, TraverseResult> terminalTest) {
        Set<Terminal> visited = new LinkedHashSet<>();
        start.traverse(new Terminal.TopologyTraverser() {
            @Override
            public TraverseResult traverse(Terminal terminal, boolean connected) {
                if (!visited.add(terminal)) {
                    fail("Visiting an already visited terminal");
                }
                return terminalTest.apply(terminal);
            }

            @Override
            public TraverseResult traverse(Switch aSwitch) {
                return switchTest.apply(aSwitch);
            }
        });
        return visited.stream().map(t -> t.getConnectable().getId()).collect(Collectors.toList());
    }

}
