/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import com.powsybl.afs.mapdb.storage.MapDbAppStorage;
import com.powsybl.afs.storage.AppStorage;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LocalTaskMonitorTest extends AbstractProjectFileTest {

    @Override
    protected List<ProjectFileExtension> getProjectFileExtensions() {
        return Collections.singletonList(new FooFileExtension());
    }

    @Override
    protected AppStorage createStorage() {
        return MapDbAppStorage.createHeap("mem");
    }

    private static final class ListenerState {
        List<ProjectFile> started = new ArrayList<>();
        List<ProjectFile> stopped = new ArrayList<>();
        List<ProjectFile> updated = new ArrayList<>();
        List<String> messages = new ArrayList<>();

        private void reset() {
            started.clear();
            stopped.clear();
            updated.clear();
            messages.clear();
        }
    }

    @Test
    public void test() {
        Project test = afs.getRootFolder().createProject("test");
        FooFile foo = test.getRootFolder().fileBuilder(FooFileBuilder.class)
                .withName("foo")
                .build();

        TaskMonitor monitor = new LocalTaskMonitor();
        ListenerState listenerState = new ListenerState();
        TaskListener listener = new TaskListener() {
            @Override
            public void taskStarted(ProjectFile projectFile) {
                listenerState.started.add(projectFile);
            }

            @Override
            public void taskStopped(ProjectFile projectFile) {
                listenerState.stopped.add(projectFile);
            }

            @Override
            public void taskMessageUpdated(ProjectFile projectFile, String message) {
                listenerState.updated.add(projectFile);
                listenerState.messages.add(message);
            }
        };
        monitor.addListener(listener);
        assertEquals(0L, monitor.takeSnapshot().getRevision());
        assertTrue(monitor.takeSnapshot().getTasks().isEmpty());

        monitor.startTask(foo);
        assertTrue(listenerState.started.size() == 1);
        listenerState.reset();
        assertEquals(1L, monitor.takeSnapshot().getRevision());
        assertEquals(Collections.singletonList(new TaskMonitor.Task(foo, 1L)), monitor.takeSnapshot().getTasks());

        monitor.updateTaskMessage(foo, "hello");
        assertTrue(listenerState.updated.size() == 1);
        assertTrue(listenerState.messages.size() == 1);
        assertEquals("hello", listenerState.messages.get(0));
        listenerState.reset();
        assertEquals(2L, monitor.takeSnapshot().getRevision());
        assertEquals(Collections.singletonList(new TaskMonitor.Task(foo, "hello", 2L)), monitor.takeSnapshot().getTasks());

        monitor.stopTask(foo);
        assertTrue(listenerState.stopped.size() == 1);
        assertEquals(3L, monitor.takeSnapshot().getRevision());
        assertTrue(monitor.takeSnapshot().getTasks().isEmpty());

        monitor.removeListener(listener);
    }
}
