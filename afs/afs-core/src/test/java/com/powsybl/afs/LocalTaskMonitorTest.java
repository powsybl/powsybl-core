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

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
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

    @Test
    public void test() {
        Project test = afs.getRootFolder().createProject("test");
        FooFile foo = test.getRootFolder().fileBuilder(FooFileBuilder.class)
                .withName("foo")
                .build();

        TaskMonitor monitor = new LocalTaskMonitor();
        Deque<TaskEvent> events = new ArrayDeque<>();
        TaskListener listener = new TaskListener() {
            @Override
            public String getProjectId() {
                return test.getId();
            }

            @Override
            public void onEvent(TaskEvent event) {
                events.add(event);
            }
        };
        monitor.addListener(listener);
        assertEquals(0L, monitor.takeSnapshot().getRevision());
        assertTrue(monitor.takeSnapshot().getTasks().isEmpty());

        TaskMonitor.Task task = monitor.startTask(foo);
        assertEquals(1, events.size());
        assertEquals(new StartTaskEvent(task.getId(), 1L, "foo"), events.pop());

        assertEquals(1L, monitor.takeSnapshot().getRevision());
        assertEquals(1, monitor.takeSnapshot().getTasks().size());
        assertEquals(task.getId(), monitor.takeSnapshot().getTasks().get(0).getId());
        assertEquals(1L, monitor.takeSnapshot().getTasks().get(0).getRevision());

        monitor.updateTaskMessage(task.getId(), "hello");
        assertEquals(1, events.size());
        assertEquals(new UpdateTaskMessageEvent(task.getId(), 2L, "hello"), events.pop());

        assertEquals(2L, monitor.takeSnapshot().getRevision());
        assertEquals(1, monitor.takeSnapshot().getTasks().size());
        assertEquals(task.getId(), monitor.takeSnapshot().getTasks().get(0).getId());
        assertEquals("hello", monitor.takeSnapshot().getTasks().get(0).getMessage());
        assertEquals(2L, monitor.takeSnapshot().getTasks().get(0).getRevision());

        monitor.stopTask(task.getId());
        assertEquals(1, events.size());
        assertEquals(new StopTaskEvent(task.getId(), 3L), events.pop());

        assertEquals(3L, monitor.takeSnapshot().getRevision());
        assertTrue(monitor.takeSnapshot().getTasks().isEmpty());

        monitor.removeListener(listener);
    }
}
