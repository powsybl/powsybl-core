/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.io;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;

public class FileUtilTest {

    FileSystem fsFoo;
    FileSystem fsBar;

    @Before
    public void setUp() {
        fsFoo = Jimfs.newFileSystem(Configuration.unix());
        fsBar = Jimfs.newFileSystem(Configuration.unix());
    }

    @Test
    public void testCopyDir() throws IOException {
        Path initPaths = fsFoo.getPath("/tmp/a/b/c");
        Files.createDirectories(initPaths);

        Path dest = fsFoo.getPath("/dest/a");
        Files.createDirectories(dest);
        Path remoteDest = fsBar.getPath("/dest/a");
        Files.createDirectories(remoteDest);

        Path source = initPaths.getParent().getParent(); // /tmp/a
        FileUtil.copyDir(source, dest);
        FileUtil.copyDir(source, remoteDest);

        assertTrue(Files.exists(fsFoo.getPath("/dest/a/b/c")));
        assertTrue(Files.exists(fsBar.getPath("/dest/a/b/c")));
    }

}
