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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class TopologyTraverserAdapterTest {

    @Test
    public void test() {
        MergingView cgm = MergingViewFactory.createCGM(null);
        Terminal start1 = cgm.getLoad("LOAD1").getTerminal();
        List<String> traversed1 = recordTraversed(start1, s -> TraverseResult.CONTINUE);
        assertEquals(Arrays.asList("LOAD1", "BBS1", "DL1 + DL2"), traversed1);

        List<String> traversed1bis = recordTraversed(start1,
            aSwitch -> !aSwitch.isOpen() && aSwitch.getKind() != SwitchKind.BREAKER ? TraverseResult.CONTINUE : TraverseResult.TERMINATE_PATH);
        assertEquals(Collections.singletonList("LOAD1"), traversed1bis);

        Terminal start2 = cgm.getLoad("LOAD2").getTerminal();
        List<String> traversed2 = recordTraversed(start2, s -> TraverseResult.CONTINUE);
        assertEquals(Arrays.asList("LOAD2", "DL1 + DL2"), traversed2);

        List<String> traversed3 = recordTraversed(start2, s -> TraverseResult.CONTINUE,
            t -> t.getConnectable().getId().equals("DL1 + DL2") ? TraverseResult.TERMINATE_TRAVERSER : TraverseResult.CONTINUE);
        assertEquals(Arrays.asList("LOAD2", "DL1 + DL2"), traversed3);
    }

    private List<String> recordTraversed(Terminal start, Function<Switch, TraverseResult> switchPredicate) {
        return recordTraversed(start, switchPredicate, t -> TraverseResult.CONTINUE);
    }

    private List<String> recordTraversed(Terminal start, Function<Switch, TraverseResult> switchTest, Function<Terminal, TraverseResult> terminalTest) {
        List<String> traversed = new ArrayList<>();
        start.traverse(new Terminal.TopologyTraverser() {
            @Override
            public TraverseResult traverse(Terminal terminal, boolean connected) {
                traversed.add(terminal.getConnectable().getId());
                return terminalTest.apply(terminal);
            }

            @Override
            public TraverseResult traverse(Switch aSwitch) {
                return switchTest.apply(aSwitch);
            }
        });
        return traversed;
    }

}
