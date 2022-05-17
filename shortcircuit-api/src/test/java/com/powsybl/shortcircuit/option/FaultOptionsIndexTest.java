/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit.option;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class FaultOptionsIndexTest {

    @Test
    public void test() {
        List<FaultOptions> options = new ArrayList<>();
        options.add(new FaultOptions(new FaultContext("f00"), false, false));
        options.add(new FaultOptions(new FaultContext("f01"), false, true));
        options.add(new FaultOptions(new FaultContext("f10"), true, false));
        options.add(new FaultOptions(new FaultContext("f11"), true, true));

        FaultOptionsIndex optionIndex = new FaultOptionsIndex(options);

        FaultContext expectedCtx = new FaultContext("f00");
        FaultOptions expected = new FaultOptions(expectedCtx, false, false);
        FaultOptions actual = optionIndex.getOptions().get("f00");
        assertEquals(expected, actual);
        assertEquals(expected.hashCode(), actual.hashCode());
        assertEquals(expected.toString(), actual.toString());

        FaultContext actualCtx = optionIndex.getOptions().get("f00").getFaultContext();
        assertEquals(expectedCtx, actualCtx);
        assertEquals(expectedCtx.hashCode(), actualCtx.hashCode());
        assertEquals(expectedCtx.toString(), actualCtx.toString());

        assertEquals(new FaultOptions(new FaultContext("f01"), false, true), optionIndex.getOptions().get("f01"));
        assertEquals(new FaultOptions(new FaultContext("f10"), true, false), optionIndex.getOptions().get("f10"));
        assertEquals(new FaultOptions(new FaultContext("f11"), true, true), optionIndex.getOptions().get("f11"));
    }
}
