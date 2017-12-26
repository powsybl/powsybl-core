/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage;

import com.powsybl.math.timeseries.DoubleArrayChunk;
import com.powsybl.math.timeseries.StringArrayChunk;
import com.powsybl.math.timeseries.TimeSeriesMetadata;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class DefaultListenableAppStorageTest {

    private ListenableAppStorage listenableStorage;

    private String methodCalled;

    @Before
    public void setUp() {
        AppStorage storage = Mockito.mock(AppStorage.class);
        Mockito.when(storage.createNode(Mockito.any(NodeId.class), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.any(NodeMetadata.class)))
                .thenReturn(new NodeInfo(new NodeIdMock("result"), "", "", "", 0, 0, 0, new NodeMetadata()));
        Mockito.when(storage.writeStringData(Mockito.any(NodeId.class), Mockito.anyString()))
                .thenReturn(new StringWriter());

        listenableStorage = new DefaultListenableAppStorage(storage);
        listenableStorage.addListener(this, new DefaultAppStorageListener() {
            @Override
            public void nodeCreated(NodeId id) {
                methodCalled = "nodeCreated";
            }

            @Override
            public void nodeRemoved(NodeId id) {
                methodCalled = "nodeRemoved";
            }

            @Override
            public void nodeDataUpdated(NodeId id, String attributeName) {
                methodCalled = "nodeDataUpdated";
            }

            @Override
            public void dependencyAdded(NodeId id, String dependencyName) {
                methodCalled = "dependencyAdded";
            }

            @Override
            public void timeSeriesCreated(NodeId id, String timeSeriesName) {
                methodCalled = "timeSeriesCreated";
            }

            @Override
            public void timeSeriesDataUpdated(NodeId id, String timeSeriesName) {
                methodCalled = "timeSeriesDataUpdated";
            }

            @Override
            public void timeSeriesRemoved(NodeId id) {
                methodCalled = "timeSeriesRemoved";
            }
        });
    }

    @After
    public void tearDown() throws Exception {
        listenableStorage.removeListeners(this);
    }

    @Test
    public void test() throws IOException {
        listenableStorage.createNode(new NodeIdMock("node1"), "node2", "file", "", 0, new NodeMetadata());
        assertEquals("nodeCreated", methodCalled);

        listenableStorage.deleteNode(new NodeIdMock("node1"));
        assertEquals("nodeRemoved", methodCalled);

        try (Writer writer = listenableStorage.writeStringData(new NodeIdMock("node1"), "attr")) {
            writer.write("hello");
        }
        assertEquals("nodeDataUpdated", methodCalled);

        listenableStorage.addDependency(new NodeIdMock("node1"), "a", new NodeIdMock("node1"));
        assertEquals("dependencyAdded", methodCalled);

        listenableStorage.createTimeSeries(new NodeIdMock("node1"), Mockito.mock(TimeSeriesMetadata.class));
        assertEquals("timeSeriesCreated", methodCalled);

        listenableStorage.addDoubleTimeSeriesData(new NodeIdMock("node1"), 1, "ts1", Collections.singletonList(Mockito.mock(DoubleArrayChunk.class)));
        assertEquals("timeSeriesDataUpdated", methodCalled);

        listenableStorage.addStringTimeSeriesData(new NodeIdMock("node1"), 1, "ts1", Collections.singletonList(Mockito.mock(StringArrayChunk.class)));
        assertEquals("timeSeriesDataUpdated", methodCalled);

        listenableStorage.removeAllTimeSeries(new NodeIdMock("node1"));
        assertEquals("timeSeriesRemoved", methodCalled);
    }
}
