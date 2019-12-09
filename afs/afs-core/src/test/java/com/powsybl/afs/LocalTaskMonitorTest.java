/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.testing.EqualsTester;
import com.powsybl.afs.mapdb.storage.MapDbAppStorage;
import com.powsybl.afs.storage.AppStorage;
import com.powsybl.afs.storage.InMemoryEventsBus;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.computation.CompletableFutureTask;
import org.junit.Test;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.Assert.*;

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
        return MapDbAppStorage.createMem("mem", new InMemoryEventsBus());
    }

    @Test
    public void test() throws IOException, TaskMonitor.NotACancellableTaskMonitor, InterruptedException {
        Project test = afs.getRootFolder().createProject("test");
        FooFile foo = test.getRootFolder().fileBuilder(FooFileBuilder.class)
                .withName("foo")
                .build();

        try (TaskMonitor monitor = new LocalTaskMonitor()) {
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
            assertEquals(0L, monitor.takeSnapshot(null).getRevision());
            assertTrue(monitor.takeSnapshot(null).getTasks().isEmpty());

            TaskMonitor.Task task = monitor.startTask(foo);
            assertEquals("foo", task.getName());
            assertEquals(1, events.size());
            assertEquals(new StartTaskEvent(task.getId(), 1L, "foo"), events.pop());

            assertEquals(1L, monitor.takeSnapshot(null).getRevision());
            assertEquals(1, monitor.takeSnapshot(null).getTasks().size());
            assertEquals(task.getId(), monitor.takeSnapshot(null).getTasks().get(0).getId());
            assertEquals(1L, monitor.takeSnapshot(null).getTasks().get(0).getRevision());

            // test Snapshot -> json -> Snapshot
            ObjectMapper objectMapper = JsonUtil.createObjectMapper();
            TaskMonitor.Snapshot snapshotRef = monitor.takeSnapshot(null);
            String snJsonRef = objectMapper.writeValueAsString(snapshotRef);
            TaskMonitor.Snapshot snapshotConverted = objectMapper.readValue(snJsonRef, TaskMonitor.Snapshot.class);
            assertEquals(snapshotRef, snapshotConverted);

            monitor.updateTaskMessage(task.getId(), "hello");
            assertEquals(1, events.size());
            assertEquals(new UpdateTaskMessageEvent(task.getId(), 2L, "hello"), events.pop());

            assertEquals(2L, monitor.takeSnapshot(null).getRevision());
            assertEquals(1, monitor.takeSnapshot(null).getTasks().size());
            assertEquals(task.getId(), monitor.takeSnapshot(null).getTasks().get(0).getId());
            assertEquals("hello", monitor.takeSnapshot(null).getTasks().get(0).getMessage());
            assertEquals(2L, monitor.takeSnapshot(null).getTasks().get(0).getRevision());

            try {
                monitor.updateTaskMessage(new UUID(0L, 0L), "");
                fail();
            } catch (IllegalArgumentException ignored) {
            }

            monitor.updateTaskFuture(task.getId(), null);
            assertEquals(1, events.size());
            assertEquals(new TaskCancellableStatusChangeEvent(task.getId(), 3L, false), events.pop());

            CountDownLatch waitForStart = new CountDownLatch(1);
            CountDownLatch waitIndefinitely = new CountDownLatch(1);
            CountDownLatch waitForInterruption = new CountDownLatch(1);

            AtomicBoolean interrupted = new AtomicBoolean(false);
            CompletableFutureTask<Void> dummyTaskProcess = CompletableFutureTask.runAsync(() -> {
                waitForStart.countDown();
                try {
                    waitIndefinitely.await();
                    fail();
                } catch (InterruptedException exc) {
                    waitForInterruption.countDown();
                    interrupted.set(true);
                }
                return null;
            }, Executors.newSingleThreadExecutor());

            //Cancel after task has actually started
            waitForStart.await();
            monitor.updateTaskFuture(task.getId(), dummyTaskProcess);
            assertEquals(1, events.size());
            assertEquals(new TaskCancellableStatusChangeEvent(task.getId(), 4L, true), events.pop());
            boolean success = monitor.cancelTaskComputation(task.getId());
            assertThat(success).isTrue();
            assertThat(dummyTaskProcess.isCancelled()).isTrue();
            assertThatCode(dummyTaskProcess::get).isInstanceOf(CancellationException.class);
            waitForInterruption.await();
            assertThat(waitForInterruption.getCount()).isEqualTo(0);
            assertThat(interrupted.get()).isTrue();
            assertThat(waitIndefinitely.getCount()).isEqualTo(1);

            monitor.stopTask(task.getId());
            assertEquals(1, events.size());
            assertEquals(new StopTaskEvent(task.getId(), 5L), events.pop());

            assertEquals(5L, monitor.takeSnapshot(null).getRevision());
            assertTrue(monitor.takeSnapshot(null).getTasks().isEmpty());

            try {
                monitor.stopTask(new UUID(0L, 0L));
                fail();
            } catch (IllegalArgumentException ignored) {
            }

            monitor.removeListener(listener);
        }
    }

    @Test
    public void startTaskEventTest() throws IOException {
        TaskEvent event = new StartTaskEvent(new UUID(0L, 0L), 0L, "e1");
        assertEquals("StartTaskEvent(taskId=00000000-0000-0000-0000-000000000000, revision=0, name=e1)", event.toString());
        ObjectMapper objectMapper = JsonUtil.createObjectMapper();
        String json = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(event);
        String jsonRef = String.join(System.lineSeparator(),
                "{",
                "  \"@c\" : \".StartTaskEvent\",",
                "  \"taskId\" : \"00000000-0000-0000-0000-000000000000\",",
                "  \"revision\" : 0,",
                "  \"name\" : \"e1\"",
                "}");
        assertEquals(jsonRef, json);
        TaskEvent event2 = objectMapper.readValue(json, TaskEvent.class);
        assertEquals(event, event2);

        new EqualsTester()
                .addEqualityGroup(new StartTaskEvent(new UUID(0L, 0L), 0L, "e1"), new StartTaskEvent(new UUID(0L, 0L), 0L, "e1"))
                .addEqualityGroup(new StartTaskEvent(new UUID(0L, 1L), 1L, "e2"), new StartTaskEvent(new UUID(0L, 1L), 1L, "e2"))
                .testEquals();
    }

    @Test
    public void stopTaskEventTest() throws IOException {
        TaskEvent event = new StopTaskEvent(new UUID(0L, 1L), 1L);
        assertEquals("StopTaskEvent(taskId=00000000-0000-0000-0000-000000000001, revision=1)", event.toString());
        ObjectMapper objectMapper = JsonUtil.createObjectMapper();
        String json = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(event);
        String jsonRef = String.join(System.lineSeparator(),
                "{",
                "  \"@c\" : \".StopTaskEvent\",",
                "  \"taskId\" : \"00000000-0000-0000-0000-000000000001\",",
                "  \"revision\" : 1",
                "}");
        assertEquals(jsonRef, json);
        TaskEvent event2 = objectMapper.readValue(json, TaskEvent.class);
        assertEquals(event, event2);

        new EqualsTester()
                .addEqualityGroup(new StopTaskEvent(new UUID(0L, 0L), 0L), new StopTaskEvent(new UUID(0L, 0L), 0L))
                .addEqualityGroup(new StopTaskEvent(new UUID(0L, 1L), 1L), new StopTaskEvent(new UUID(0L, 1L), 1L))
                .testEquals();
    }

    @Test
    public void updateTaskMessageEventTest() throws IOException {
        TaskEvent event = new UpdateTaskMessageEvent(new UUID(0L, 2L), 2L, "hello");
        assertEquals("UpdateTaskMessageEvent(taskId=00000000-0000-0000-0000-000000000002, revision=2, message=hello)", event.toString());
        ObjectMapper objectMapper = JsonUtil.createObjectMapper();
        String json = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(event);
        String jsonRef = String.join(System.lineSeparator(),
                "{",
                "  \"@c\" : \".UpdateTaskMessageEvent\",",
                "  \"taskId\" : \"00000000-0000-0000-0000-000000000002\",",
                "  \"revision\" : 2,",
                "  \"message\" : \"hello\"",
                "}");
        assertEquals(jsonRef, json);
        TaskEvent event2 = objectMapper.readValue(json, TaskEvent.class);
        assertEquals(event, event2);

        new EqualsTester()
                .addEqualityGroup(new UpdateTaskMessageEvent(new UUID(0L, 0L), 0L, "hello"), new UpdateTaskMessageEvent(new UUID(0L, 0L), 0L, "hello"))
                .addEqualityGroup(new UpdateTaskMessageEvent(new UUID(0L, 1L), 1L, "bye"), new UpdateTaskMessageEvent(new UUID(0L, 1L), 1L, "bye"))
                .testEquals();
    }
}
