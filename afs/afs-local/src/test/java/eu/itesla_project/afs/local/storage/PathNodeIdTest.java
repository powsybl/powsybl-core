/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.afs.local.storage;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.google.common.testing.EqualsTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.FileSystem;

import static org.junit.Assert.assertEquals;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PathNodeIdTest {

    private FileSystem fileSystem;

    @Before
    public void setUp() throws Exception {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
    }

    @After
    public void tearDown() throws Exception {
        fileSystem.close();
    }

    @Test
    public void test() {
        PathNodeId nodeId1 = new PathNodeId(fileSystem.getPath("/test1"));
        assertEquals(fileSystem.getPath("/test1"), nodeId1.getPath());
        PathNodeId nodeId2 = new PathNodeId(fileSystem.getPath("/test1"));
        PathNodeId nodeId3 = new PathNodeId(fileSystem.getPath("/test2"));
        PathNodeId nodeId4 = new PathNodeId(fileSystem.getPath("/test2"));
        new EqualsTester()
                .addEqualityGroup(nodeId1, nodeId2)
                .addEqualityGroup(nodeId3, nodeId4)
                .testEquals();
    }
}
