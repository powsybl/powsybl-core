/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.model.test;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.cgmes.model.CgmesModelException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileSystem;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class MissingCaseTest {

    private FileSystem fileSystem;

    @Before
    public void setUp() throws Exception {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
    }

    @After
    public void tearDown() throws Exception {
        fileSystem.close();
    }

    @Test(expected = CgmesModelException.class)
    public void missing() throws IOException {
        TestGridModel missing = new TestGridModelPath(
                fileSystem.getPath("/thisTestCaseDoesNotExist"),
                null,
                null);
        new CgmesModelTester(missing).test();
    }
}
