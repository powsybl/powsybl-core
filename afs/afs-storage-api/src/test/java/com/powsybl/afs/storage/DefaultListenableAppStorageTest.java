/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage;

import com.powsybl.afs.storage.events.*;
import com.powsybl.timeseries.DoubleArrayChunk;
import com.powsybl.timeseries.StringArrayChunk;
import com.powsybl.timeseries.TimeSeriesMetadata;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class DefaultListenableAppStorageTest {

    private ListenableAppStorage listenableStorage;

    private NodeEventList lastEventList;

    private AppStorageListener l = eventList -> lastEventList = eventList;

    @Before
    public void setUp() {
        AppStorage storage = Mockito.mock(AppStorage.class);
        Mockito.when(storage.createNode(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.any(NodeGenericMetadata.class)))
                .thenReturn(new NodeInfo("node2", "nodeName", "", "", 0, 0, 0, new NodeGenericMetadata()));
        Mockito.when(storage.deleteNode("node2")).thenReturn("node1");
        Mockito.when(storage.writeBinaryData(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(new ByteArrayOutputStream());

        listenableStorage = new DefaultListenableAppStorage(storage);
        listenableStorage.addListener(l);
    }

    @After
    public void tearDown() {
        listenableStorage.removeListeners();
    }

    @Test
    public void test() throws IOException {
        listenableStorage.createNode("node1", "node2", "file", "", 0, new NodeGenericMetadata());
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

        listenableStorage.addDoubleTimeSeriesData("node1", 1, "ts1", Collections.singletonList(Mockito.mock(DoubleArrayChunk.class)));
        listenableStorage.flush();
        assertEquals(new NodeEventList(new TimeSeriesDataUpdated("node1", "ts1")), lastEventList);

        listenableStorage.addStringTimeSeriesData("node1", 1, "ts1", Collections.singletonList(Mockito.mock(StringArrayChunk.class)));
        listenableStorage.flush();
        assertEquals(new NodeEventList(new TimeSeriesDataUpdated("node1", "ts1")), lastEventList);

        listenableStorage.clearTimeSeries("node1");
        listenableStorage.flush();
        assertEquals(new NodeEventList(new TimeSeriesCleared("node1")), lastEventList);
    }
}
