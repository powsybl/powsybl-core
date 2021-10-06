/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;

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
        List<String> traversed = new ArrayList<>();
        start.traverse(new Terminal.TopologyTraverser() {
            @Override
            public boolean traverse(Terminal terminal, boolean connected) {
                traversed.add(terminal.getConnectable().getId());
                return true;
            }

            @Override
            public boolean traverse(Switch aSwitch) {
                return switchPredicate.test(aSwitch);
            }
        });
        return traversed;
    }

}
