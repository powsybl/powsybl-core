/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.*;
import org.junit.Test;

import java.util.*;
import java.util.function.Predicate;
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
        List<String> traversed1 = recordTraversed(start1, s -> true);
        assertEquals(Arrays.asList("LOAD1", "BBS1", "DL1 + DL2"), traversed1);

        List<String> traversed1bis = recordTraversed(start1, aSwitch -> !aSwitch.isOpen() && aSwitch.getKind() != SwitchKind.BREAKER);
        assertEquals(Collections.singletonList("LOAD1"), traversed1bis);

        Terminal start2 = cgm.getLoad("LOAD2").getTerminal();
        List<String> traversed2 = recordTraversed(start2, s -> true);
        assertEquals(Arrays.asList("LOAD2", "DL1 + DL2"), traversed2);
    }

    private List<String> recordTraversed(Terminal start, Predicate<Switch> switchPredicate) {
        Set<Terminal> traversed = new LinkedHashSet<>();
        start.traverse(new VoltageLevel.TopologyTraverser() {
            @Override
            public boolean traverse(Terminal terminal, boolean connected) {
                if (!traversed.add(terminal)) {
                    fail();
                }
                return true;
            }

            @Override
            public boolean traverse(Switch aSwitch) {
                return switchPredicate.test(aSwitch);
            }
        });
        return traversed.stream().map(t -> t.getConnectable().getId()).collect(Collectors.toList());
    }

}
