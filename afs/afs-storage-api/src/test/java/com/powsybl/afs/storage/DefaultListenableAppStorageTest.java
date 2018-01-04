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

    private String methodCalled;

    @Before
    public void setUp() {
        AppStorage storage = Mockito.mock(AppStorage.class);
        Mockito.when(storage.createNode(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.any(NodeGenericMetadata.class)))
                .thenReturn(new NodeInfo("result", "", "", "", 0, 0, 0, new NodeGenericMetadata()));
        Mockito.when(storage.writeBinaryData(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(new ByteArrayOutputStream());

        listenableStorage = new DefaultListenableAppStorage(storage);
        listenableStorage.addListener(this, new DefaultAppStorageListener() {
            @Override
            public void nodeCreated(String id) {
                methodCalled = "nodeCreated";
            }

            @Override
            public void nodeRemoved(String id) {
                methodCalled = "nodeRemoved";
            }

            @Override
            public void nodeDataUpdated(String id, String dataName) {
                methodCalled = "nodeDataUpdated";
            }

            @Override
            public void dependencyAdded(String id, String dependencyName) {
                methodCalled = "dependencyAdded";
            }

            @Override
            public void timeSeriesCreated(String id, String timeSeriesName) {
                methodCalled = "timeSeriesCreated";
            }

            @Override
            public void timeSeriesDataUpdated(String id, String timeSeriesName) {
                methodCalled = "timeSeriesDataUpdated";
            }

            @Override
            public void timeSeriesRemoved(String id) {
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
        listenableStorage.createNode("node1", "node2", "file", "", 0, new NodeGenericMetadata());
        assertEquals("nodeCreated", methodCalled);

        listenableStorage.deleteNode("node1");
        assertEquals("nodeRemoved", methodCalled);

        try (OutputStream os = listenableStorage.writeBinaryData("node1", "attr")) {
            os.write("hello".getBytes(StandardCharsets.UTF_8));
        }
        assertEquals("nodeDataUpdated", methodCalled);

        listenableStorage.addDependency("node1", "a", "node1");
        assertEquals("dependencyAdded", methodCalled);

        listenableStorage.createTimeSeries("node1", Mockito.mock(TimeSeriesMetadata.class));
        assertEquals("timeSeriesCreated", methodCalled);

        listenableStorage.addDoubleTimeSeriesData("node1", 1, "ts1", Collections.singletonList(Mockito.mock(DoubleArrayChunk.class)));
        assertEquals("timeSeriesDataUpdated", methodCalled);

        listenableStorage.addStringTimeSeriesData("node1", 1, "ts1", Collections.singletonList(Mockito.mock(StringArrayChunk.class)));
        assertEquals("timeSeriesDataUpdated", methodCalled);

        listenableStorage.removeAllTimeSeries("node1");
        assertEquals("timeSeriesRemoved", methodCalled);
    }
}
