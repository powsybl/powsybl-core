/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.afs.core;

import eu.itesla_project.afs.storage.AppFileSystemStorage;
import eu.itesla_project.afs.mapdb.storage.MapDbAppFileSystemStorage;
import eu.itesla_project.commons.tools.AbstractToolTest;
import eu.itesla_project.commons.tools.Command;
import eu.itesla_project.commons.tools.Tool;
import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.iidm.import_.ImportersLoader;
import eu.itesla_project.iidm.import_.ImportersLoaderList;
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

    public AppFileSystemToolTest() {
        ImportersLoader importersLoader = new ImportersLoaderList(Collections.emptyList(), Collections.emptyList());
        ComputationManager computationManager = Mockito.mock(ComputationManager.class);
        tool = new AppFileSystemTool() {
            @Override
            protected AppData createAppData() {
                AppFileSystemStorage storage = MapDbAppFileSystemStorage.createHeap("mem");
                AppFileSystem afs = new AppFileSystem("mem", false, storage);
                AppData appData = new AppData(computationManager, importersLoader, Collections.singletonList(computationManager1 -> Collections.singletonList(afs)),
                        Collections.emptyList(), Collections.emptyList());
                afs.getRootFolder().createProject("test_project1", "");
                afs.getRootFolder().createProject("test_project2", "");
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
        assertCommand(command, "afs", 1, 0);
        assertOption(command.getOptions(), "ls", false, true);
        assertEquals("Application file system", command.getTheme());
        assertEquals("application file system command line tool", command.getDescription());
        assertNull(command.getUsageFooter());
    }

    @Test
    public void testLs() throws IOException {
        assertCommand(new String[] { "afs", "--ls" }, 0, "mem" + System.lineSeparator(), "");
        assertCommand(new String[] { "afs", "--ls", "mem:/" }, 0, String.join(System.lineSeparator(), "test_project1", "test_project2"), "");
    }
}