/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.model.test;

import java.io.IOException;
import java.nio.file.Paths;

import org.junit.Test;

import com.powsybl.cgmes.model.CgmesModelException;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class MissingCaseTest {
    @Test(expected = CgmesModelException.class)
    public void missing() throws IOException {
        TestGridModel missing = new TestGridModelPath(
                Paths.get("./thisTestCaseDoesNotExist"),
                "",
                null);
        new CgmesModelTester(missing).test();
    }
}
