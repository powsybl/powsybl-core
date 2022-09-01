/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public abstract class AbstractConvertersTest {

    @Rule
    public ExpectedException expected = ExpectedException.none();

    protected FileSystem fileSystem;
    protected Path path;
    protected Path badPath;

    protected static final String TEST_FORMAT = "TST";
    protected static final String UNSUPPORTED_FORMAT = "UNSUPPORTED";
    protected static final String EXTENSION = "tst";

    @Before
    public void setUp() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        path = fileSystem.getPath("/work/foo.tst");
        badPath = fileSystem.getPath("/work/baz.txt");
    }

    @After
    public void tearDown() throws IOException {
        fileSystem.close();
    }
}
