/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.testing.EqualsTester;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NodeEventTest {

    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws Exception {
        objectMapper = new ObjectMapper();
    }

    @Test
    public void dependencyAddedTest() throws IOException {
        DependencyAdded added = new DependencyAdded("a", "b");
        assertEquals("a", added.getId());
        assertEquals(NodeEventType.DEPENDENCY_ADDED, added.getType());
        assertEquals("b", added.getDependencyName());

        DependencyAdded added2 = objectMapper.readValue(objectMapper.writeValueAsString(added), DependencyAdded.class);
        assertEquals(added, added2);

        new EqualsTester()
                .addEqualityGroup(new DependencyAdded("a", "b"), new DependencyAdded("a", "b"))
                .addEqualityGroup(new DependencyAdded("c", "d"), new DependencyAdded("c", "d"))
                .testEquals();
    }

    @Test
    public void dependencyRemovedTest() throws IOException {
        DependencyRemoved removed = new DependencyRemoved("a", "b");
        assertEquals("a", removed.getId());
        assertEquals(NodeEventType.DEPENDENCY_REMOVED, removed.getType());
        assertEquals("b", removed.getDependencyName());

        DependencyRemoved removed2 = objectMapper.readValue(objectMapper.writeValueAsString(removed), DependencyRemoved.class);
        assertEquals(removed, removed2);

        new EqualsTester()
                .addEqualityGroup(new DependencyRemoved("a", "b"), new DependencyRemoved("a", "b"))
                .addEqualityGroup(new DependencyRemoved("c", "d"), new DependencyRemoved("c", "d"))
                .testEquals();
    }

    @Test
    public void nodeCreatedTest() throws IOException {
        NodeCreated created = new NodeCreated("a");
        assertEquals("a", created.getId());
        assertEquals(NodeEventType.NODE_CREATED, created.getType());

        NodeCreated created2 = objectMapper.readValue(objectMapper.writeValueAsString(created), NodeCreated.class);
        assertEquals(created, created2);

        new EqualsTester()
                .addEqualityGroup(new NodeCreated("a"), new NodeCreated("a"))
                .addEqualityGroup(new NodeCreated("b"), new NodeCreated("b"))
                .testEquals();
    }

    @Test
    public void nodeDataUpdatedTest() throws IOException {
        NodeDataUpdated updated = new NodeDataUpdated("a", "b");
        assertEquals("a", updated.getId());
        assertEquals(NodeEventType.NODE_DATA_UPDATED, updated.getType());
        assertEquals("b", updated.getDataName());

        NodeDataUpdated updated2 = objectMapper.readValue(objectMapper.writeValueAsString(updated), NodeDataUpdated.class);
        assertEquals(updated, updated2);

        new EqualsTester()
                .addEqualityGroup(new NodeDataUpdated("a", "b"), new NodeDataUpdated("a", "b"))
                .addEqualityGroup(new NodeDataUpdated("c", "d"), new NodeDataUpdated("c", "d"))
                .testEquals();
    }

    @Test
    public void nodeDescriptionUpdatedTest() throws IOException {
        NodeDescriptionUpdated updated = new NodeDescriptionUpdated("a", "b");
        assertEquals("a", updated.getId());
        assertEquals(NodeEventType.NODE_DESCRIPTION_UPDATED, updated.getType());
        assertEquals("b", updated.getDescription());

        NodeDescriptionUpdated updated2 = objectMapper.readValue(objectMapper.writeValueAsString(updated), NodeDescriptionUpdated.class);
        assertEquals(updated, updated2);

        new EqualsTester()
                .addEqualityGroup(new NodeDescriptionUpdated("a", "b"), new NodeDescriptionUpdated("a", "b"))
                .addEqualityGroup(new NodeDescriptionUpdated("c", "d"), new NodeDescriptionUpdated("c", "d"))
                .testEquals();
    }

    @Test
    public void nodeRemovedTest() throws IOException {
        NodeRemoved removed = new NodeRemoved("a");
        assertEquals("a", removed.getId());
        assertEquals(NodeEventType.NODE_REMOVED, removed.getType());

        NodeRemoved removed2 = objectMapper.readValue(objectMapper.writeValueAsString(removed), NodeRemoved.class);
        assertEquals(removed, removed2);

        new EqualsTester()
                .addEqualityGroup(new NodeRemoved("a"), new NodeRemoved("a"))
                .addEqualityGroup(new NodeRemoved("b"), new NodeRemoved("b"))
                .testEquals();
    }

    @Test
    public void parentChangedTest() throws IOException {
        ParentChanged changed = new ParentChanged("a");
        assertEquals("a", changed.getId());
        assertEquals(NodeEventType.PARENT_CHANGED, changed.getType());

        ParentChanged changed2 = objectMapper.readValue(objectMapper.writeValueAsString(changed), ParentChanged.class);
        assertEquals(changed, changed2);

        new EqualsTester()
                .addEqualityGroup(new ParentChanged("a"), new ParentChanged("a"))
                .addEqualityGroup(new ParentChanged("b"), new ParentChanged("b"))
                .testEquals();
    }


    @Test
    public void timeSeriesAllRemovedTest() throws IOException {
        TimeSeriesAllRemoved removed = new TimeSeriesAllRemoved("a");
        assertEquals("a", removed.getId());
        assertEquals(NodeEventType.TIME_SERIES_ALL_REMOVED, removed.getType());

        TimeSeriesAllRemoved removed2 = objectMapper.readValue(objectMapper.writeValueAsString(removed), TimeSeriesAllRemoved.class);
        assertEquals(removed, removed2);

        new EqualsTester()
                .addEqualityGroup(new TimeSeriesAllRemoved("a"), new TimeSeriesAllRemoved("a"))
                .addEqualityGroup(new TimeSeriesAllRemoved("b"), new TimeSeriesAllRemoved("b"))
                .testEquals();
    }

    @Test
    public void timeSeriesCreatedTest() throws IOException {
        TimeSeriesCreated created = new TimeSeriesCreated("a", "b");
        assertEquals("a", created.getId());
        assertEquals(NodeEventType.TIME_SERIES_CREATED, created.getType());
        assertEquals("b", created.getTimeSeriesName());

        TimeSeriesCreated created2 = objectMapper.readValue(objectMapper.writeValueAsString(created), TimeSeriesCreated.class);
        assertEquals(created, created2);

        new EqualsTester()
                .addEqualityGroup(new TimeSeriesCreated("a", "b"), new TimeSeriesCreated("a", "b"))
                .addEqualityGroup(new TimeSeriesCreated("c", "d"), new TimeSeriesCreated("c", "d"))
                .testEquals();
    }

    @Test
    public void timeSeriesDataUpdatedTest() throws IOException {
        TimeSeriesDataUpdated updated = new TimeSeriesDataUpdated("a", "b");
        assertEquals("a", updated.getId());
        assertEquals(NodeEventType.TIME_SERIES_DATA_UPDATED, updated.getType());
        assertEquals("b", updated.getTimeSeriesName());

        TimeSeriesDataUpdated updated2 = objectMapper.readValue(objectMapper.writeValueAsString(updated), TimeSeriesDataUpdated.class);
        assertEquals(updated, updated2);

        new EqualsTester()
                .addEqualityGroup(new TimeSeriesDataUpdated("a", "b"), new TimeSeriesDataUpdated("a", "b"))
                .addEqualityGroup(new TimeSeriesDataUpdated("c", "d"), new TimeSeriesDataUpdated("c", "d"))
                .testEquals();
    }
}
