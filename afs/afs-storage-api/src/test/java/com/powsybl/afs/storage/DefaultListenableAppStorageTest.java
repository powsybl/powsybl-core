/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage;

import com.powsybl.afs.storage.events.*;
import com.powsybl.math.timeseries.DoubleArrayChunk;
import com.powsybl.math.timeseries.StringArrayChunk;
import com.powsybl.math.timeseries.TimeSeriesMetadata;
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

    private NodeEvent lastEvent;

    @Before
    public void setUp() {
        AppStorage storage = Mockito.mock(AppStorage.class);
        Mockito.when(storage.createNode(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.any(NodeGenericMetadata.class)))
                .thenReturn(new NodeInfo("node2", "", "", "", 0, 0, 0, new NodeGenericMetadata()));
        Mockito.when(storage.writeBinaryData(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(new ByteArrayOutputStream());

        listenableStorage = new DefaultListenableAppStorage(storage);
        listenableStorage.addListener(this, event -> lastEvent = event);
    }

    @After
    public void tearDown() throws Exception {
        listenableStorage.removeListeners(this);
    }

    @Test
    public void test() throws IOException {
        listenableStorage.createNode("node1", "node2", "file", "", 0, new NodeGenericMetadata());
        assertEquals(new NodeCreated("node2"), lastEvent);

        listenableStorage.deleteNode("node1");
        assertEquals(new NodeRemoved("node1"), lastEvent);

        try (OutputStream os = listenableStorage.writeBinaryData("node1", "attr")) {
            os.write("hello".getBytes(StandardCharsets.UTF_8));
        }
        assertEquals(new NodeDataUpdated("node1", "attr"), lastEvent);

        listenableStorage.addDependency("node1", "a", "node1");
        assertEquals(new DependencyAdded("node1", "a"), lastEvent);

        listenableStorage.removeDependency("node1", "a", "node1");
        assertEquals(new DependencyRemoved("node1", "a"), lastEvent);

        TimeSeriesMetadata metadata = Mockito.mock(TimeSeriesMetadata.class);
        Mockito.when(metadata.getName()).thenReturn("ts1");
        listenableStorage.createTimeSeries("node1", metadata);
        assertEquals(new TimeSeriesCreated("node1", "ts1"), lastEvent);

        listenableStorage.addDoubleTimeSeriesData("node1", 1, "ts1", Collections.singletonList(Mockito.mock(DoubleArrayChunk.class)));
        assertEquals(new TimeSeriesDataUpdated("node1", "ts1"), lastEvent);

        listenableStorage.addStringTimeSeriesData("node1", 1, "ts1", Collections.singletonList(Mockito.mock(StringArrayChunk.class)));
        assertEquals(new TimeSeriesDataUpdated("node1", "ts1"), lastEvent);

        listenableStorage.clearTimeSeries("node1");
        assertEquals(new TimeSeriesCleared("node1"), lastEvent);
    }
}
