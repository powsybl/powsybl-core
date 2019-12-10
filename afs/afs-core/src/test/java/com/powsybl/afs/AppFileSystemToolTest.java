/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import com.powsybl.afs.mapdb.storage.MapDbAppStorage;
import com.powsybl.afs.storage.AppStorage;
import com.powsybl.afs.storage.NodeGenericMetadata;
import com.powsybl.computation.ComputationManager;
import com.powsybl.tools.AbstractToolTest;
import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolRunningContext;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class AppFileSystemToolTest extends AbstractToolTest {

    private AppFileSystemTool tool;

    private static final String FOLDER_PSEUDO_CLASS = "folder";

    public AppFileSystemToolTest() {
        ComputationManager computationManager = Mockito.mock(ComputationManager.class);
        tool = new AppFileSystemTool() {
            @Override
            protected AppData createAppData(ToolRunningContext context) {
                AppData appData = new AppData(computationManager, computationManager, Collections.emptyList(),
                        Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
                AppStorage storage = MapDbAppStorage.createMem("mem", appData.getEventsBus());
                AppFileSystem afs = new AppFileSystem("mem", false, storage);
                appData.addFileSystem(afs);
                afs.getRootFolder().createProject("test_project1");
                afs.getRootFolder().createProject("test_project2");
                storage.createNode(afs.getRootFolder().getId(), "test", FOLDER_PSEUDO_CLASS, "", 0,
                        new NodeGenericMetadata().setString("k", "v"));
                storage.flush();
                return appData;
            }
        };
    }

    @Override
    protected Iterable<Tool> getTools() {
        return Collections.singleton(tool);
    }

    @Override
    public void assertCommand() {
        Command command = tool.getCommand();
        assertCommand(command, "afs", 7, 0);
        assertOption(command.getOptions(), "ls", false, true);
        assertOption(command.getOptions(), "archive", false, true);
        assertOption(command.getOptions(), "unarchive", false, true);
        assertOption(command.getOptions(), "ls-inconsistent-nodes", false, true);
        assertOption(command.getOptions(), "fix-inconsistent-nodes", false, true);
        assertOption(command.getOptions(), "rm-inconsistent-nodes", false, true);

        assertEquals("Application file system", command.getTheme());
        assertEquals("application file system command line tool", command.getDescription());
        assertNull(command.getUsageFooter());
    }

    @Test
    public void testLs() throws IOException {
        assertCommand(new String[] {"afs", "--ls"}, 0, "mem" + System.lineSeparator(), "");
        assertCommand(new String[] {"afs", "--ls", "mem:/"}, 0, String.join(System.lineSeparator(), "test_project1", "test_project2"), "");
    }

    @Test
    public void testLsInconsistentNodes() throws IOException {
        assertCommand(new String[] {"afs", "--ls-inconsistent-nodes", "mem"}, 0, "mem:"
                + System.lineSeparator() + "[a-z0-9-]+" + System.lineSeparator(), "");
        assertCommand(new String[] {"afs", "--ls-inconsistent-nodes"}, 0, "mem:"
                + System.lineSeparator() + "[a-z0-9-]+" + System.lineSeparator(), "");
    }

    @Test
    public void testFixInconsistentNodes() throws IOException {
        assertCommand(new String[] {"afs", "--fix-inconsistent-nodes", "mem"}, 0, "mem:"
                + System.lineSeparator() + "[a-z0-9-]+ fixed", "");
        assertCommand(new String[] {"afs", "--fix-inconsistent-nodes"}, 3, "", "IllegalArgumentException");
        assertCommand(new String[] {"afs", "--ls-inconsistent-nodes", "mem", "nodeId"}, 0, "mem:"
                + System.lineSeparator() + "[a-z0-9-]+" + System.lineSeparator(), "");
    }

    @Test
    public void testRemoveInconsistentNodes() throws IOException {
        assertCommand(new String[] {"afs", "--rm-inconsistent-nodes", "mem"}, 0, "mem:"
                + System.lineSeparator() + "[a-z0-9-]+ cleaned", "");
        assertCommand(new String[] {"afs", "--rm-inconsistent-nodes"}, 3, "", "IllegalArgumentException");
    }

}
