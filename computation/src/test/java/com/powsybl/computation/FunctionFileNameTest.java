/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation;

import com.powsybl.commons.PowsyblException;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class FunctionFileNameTest {

    @Test
    public void test() {
        FunctionFileName fileName = new FunctionFileName(executionNumber -> "file-" + executionNumber, null);
        assertEquals("file-0", fileName.getName(0));
        assertTrue(fileName.dependsOnExecutionNumber());
    }

    @Test(expected = RuntimeException.class)
    public void checkTest() {
        FunctionFileName fileName = new FunctionFileName(executionNumber -> "file-" + executionNumber,
            s -> {
                throw new PowsyblException("error");
            }
        );
        fileName.getName(0);
    }
}
