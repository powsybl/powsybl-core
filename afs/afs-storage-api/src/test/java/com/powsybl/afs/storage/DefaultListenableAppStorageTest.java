/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage;

import com.powsybl.afs.storage.events.*;
import com.powsybl.timeseries.DoubleDataChunk;
import com.powsybl.timeseries.StringDataChunk;
import com.powsybl.timeseries.TimeSeriesMetadata;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class DefaultListenableAppStorageTest {

    private ListenableAppStorage listenableStorage;

    private NodeEventList lastEventList;

    private AppStorageListener l = eventList -> lastEventList = eventList;

    private AtomicReference<byte[]> data = new AtomicReference<>();

    @Before
    public void setUp() {
        AppStorage storage = Mockito.mock(AppStorage.class);
        Mockito.when(storage.createNode(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.any(NodeGenericMetadata.class), Mockito.any(NodeAccessRights.class)))
                .thenReturn(new NodeInfo("node2", "nodeName", "", "", 0, 0, 0, new NodeGenericMetadata(), new NodeAccessRights()));
        Mockito.when(storage.deleteNode("node2")).thenReturn("node1");
        Mockito.when(storage.writeBinaryData(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(new ByteArrayOutputStream() {
                    @Override
                    public void close() throws IOException {
                        super.close();
                        data.set(toByteArray());
                    }
                });
        Mockito.when(storage.readBinaryData(Mockito.anyString(), Mockito.anyString()))
                .thenAnswer(i -> new ByteArrayInputStream(data.get()));

        Mockito.when(storage.dataExists(Mockito.anyString(), Mockito.anyString())).thenAnswer(i -> data.get() != null);

        Mockito.when(storage.isConsistent("node1")).thenReturn(true);

        listenableStorage = new DefaultListenableAppStorage(storage);
        listenableStorage.addListener(l);
    }

    @After
    public void tearDown() {
        listenableStorage.removeListeners();
    }

    @Test
    public void test() throws IOException {
        listenableStorage.createNode("node1", "node2", "file", "", 0, new NodeGenericMetadata(), new NodeAccessRights());
        listenableStorage.flush();
        assertEquals(new NodeEventList(new NodeCreated("node2", "node1")), lastEventList);

        listenableStorage.deleteNode("node2");
        listenableStorage.flush();
        assertEquals(new NodeEventList(new NodeRemoved("node2", "node1")), lastEventList);

        try (OutputStream os = listenableStorage.writeBinaryData("node1", "attr")) {
            os.write("hello".getBytes(StandardCharsets.UTF_8));
        }
        listenableStorage.flush();
        assertEquals(new NodeEventList(new NodeDataUpdated("node1", "attr")), lastEventList);

        listenableStorage.addDependency("node1", "a", "node2");
        listenableStorage.flush();
        assertEquals(new NodeEventList(new DependencyAdded("node1", "a"), new BackwardDependencyAdded("node2", "a")), lastEventList);

        listenableStorage.removeDependency("node1", "a", "node2");
        listenableStorage.flush();
        assertEquals(new NodeEventList(new DependencyRemoved("node1", "a"), new BackwardDependencyRemoved("node2", "a")), lastEventList);

        TimeSeriesMetadata metadata = Mockito.mock(TimeSeriesMetadata.class);
        Mockito.when(metadata.getName()).thenReturn("ts1");
        listenableStorage.createTimeSeries("node1", metadata);
        listenableStorage.flush();
        assertEquals(new NodeEventList(new TimeSeriesCreated("node1", "ts1")), lastEventList);

        listenableStorage.addDoubleTimeSeriesData("node1", 1, "ts1", Collections.singletonList(Mockito.mock(DoubleDataChunk.class)));
        listenableStorage.flush();
        assertEquals(new NodeEventList(new TimeSeriesDataUpdated("node1", "ts1")), lastEventList);

        listenableStorage.addStringTimeSeriesData("node1", 1, "ts1", Collections.singletonList(Mockito.mock(StringDataChunk.class)));
        listenableStorage.flush();
        assertEquals(new NodeEventList(new TimeSeriesDataUpdated("node1", "ts1")), lastEventList);

        listenableStorage.clearTimeSeries("node1");
        listenableStorage.flush();
        assertEquals(new NodeEventList(new TimeSeriesCleared("node1")), lastEventList);

        listenableStorage.setConsistent("node1");
        assertTrue(listenableStorage.isConsistent("node1"));

    }

    // data updated event should only be raised once write is finished
    @Test
    public void testDataUpdated() throws IOException {
        AtomicBoolean dataUpdated = new AtomicBoolean(false);
        AppStorageListener dataListener = eventList -> eventList.getEvents().stream()
                .filter(e -> e.getType() == NodeEventType.NODE_DATA_UPDATED)
                .forEach(e -> dataUpdated.set(true));

        listenableStorage.addListener(dataListener);

        try (OutputStream os = listenableStorage.writeBinaryData("node1", "attr")) {
            listenableStorage.flush();
            assertFalse(dataUpdated.get());
            os.write("hello".getBytes(StandardCharsets.UTF_8));
            listenableStorage.flush();
            assertFalse(dataUpdated.get());
        }

        listenableStorage.flush();
        assertTrue(dataUpdated.get());
        assertTrue(data.get() != null);
        assertEquals("hello", new String(data.get(), StandardCharsets.UTF_8));
        assertTrue(listenableStorage.dataExists("node1", "attr"));
    }
}
