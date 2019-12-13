/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.testing.EqualsTester;
import com.powsybl.commons.json.JsonUtil;
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
        objectMapper = JsonUtil.createObjectMapper();
    }

    @Test
    public void dependencyAddedTest() throws IOException {
        DependencyAdded added = new DependencyAdded("a", "b");
        assertEquals("a", added.getId());
        assertEquals(DependencyAdded.TYPE, added.getType());
        assertEquals("b", added.getDependencyName());

        DependencyAdded added2 = objectMapper.readValue(objectMapper.writeValueAsString(added), DependencyAdded.class);
        assertEquals(added, added2);

        new EqualsTester()
                .addEqualityGroup(new DependencyAdded("a", "b"), new DependencyAdded("a", "b"))
                .addEqualityGroup(new DependencyAdded("c", "d"), new DependencyAdded("c", "d"))
                .testEquals();
    }

    @Test
    public void backwardDependencyAddedTest() throws IOException {
        BackwardDependencyAdded added = new BackwardDependencyAdded("a", "b");
        assertEquals("a", added.getId());
        assertEquals(BackwardDependencyAdded.TYPE, added.getType());
        assertEquals("b", added.getDependencyName());

        BackwardDependencyAdded added2 = objectMapper.readValue(objectMapper.writeValueAsString(added), BackwardDependencyAdded.class);
        assertEquals(added, added2);

        new EqualsTester()
                .addEqualityGroup(new BackwardDependencyAdded("a", "b"), new BackwardDependencyAdded("a", "b"))
                .addEqualityGroup(new BackwardDependencyAdded("c", "d"), new BackwardDependencyAdded("c", "d"))
                .testEquals();
    }

    @Test
    public void dependencyRemovedTest() throws IOException {
        DependencyRemoved removed = new DependencyRemoved("a", "b");
        assertEquals("a", removed.getId());
        assertEquals(DependencyRemoved.TYPE, removed.getType());
        assertEquals("b", removed.getDependencyName());

        DependencyRemoved removed2 = objectMapper.readValue(objectMapper.writeValueAsString(removed), DependencyRemoved.class);
        assertEquals(removed, removed2);

        new EqualsTester()
                .addEqualityGroup(new DependencyRemoved("a", "b"), new DependencyRemoved("a", "b"))
                .addEqualityGroup(new DependencyRemoved("c", "d"), new DependencyRemoved("c", "d"))
                .testEquals();
    }

    @Test
    public void backwardDependencyRemovedTest() throws IOException {
        BackwardDependencyRemoved removed = new BackwardDependencyRemoved("a", "b");
        assertEquals("a", removed.getId());
        assertEquals(BackwardDependencyRemoved.TYPE, removed.getType());
        assertEquals("b", removed.getDependencyName());

        BackwardDependencyRemoved removed2 = objectMapper.readValue(objectMapper.writeValueAsString(removed), BackwardDependencyRemoved.class);
        assertEquals(removed, removed2);

        new EqualsTester()
                .addEqualityGroup(new BackwardDependencyRemoved("a", "b"), new BackwardDependencyRemoved("a", "b"))
                .addEqualityGroup(new BackwardDependencyRemoved("c", "d"), new BackwardDependencyRemoved("c", "d"))
                .testEquals();
    }

    @Test
    public void nodeCreatedTest() throws IOException {
        NodeCreated created = new NodeCreated("a", "b");
        assertEquals("a", created.getId());
        assertEquals("b", created.getParentId());
        assertEquals(NodeCreated.TYPE, created.getType());

        NodeCreated created2 = objectMapper.readValue(objectMapper.writeValueAsString(created), NodeCreated.class);
        assertEquals(created, created2);

        new EqualsTester()
                .addEqualityGroup(new NodeCreated("a", "b"), new NodeCreated("a", "b"))
                .addEqualityGroup(new NodeCreated("b", null), new NodeCreated("b", null))
                .testEquals();
    }

    @Test
    public void nodeConsistent() throws IOException {
        NodeConsistent nodeConsistent = new NodeConsistent("a");
        assertEquals("a", nodeConsistent.getId());
        assertEquals("NodeConsistent(id=a)", nodeConsistent.toString());
        assertEquals(NodeConsistent.TYPE, nodeConsistent.getType());

        NodeConsistent nodeConsistent1 = objectMapper.readValue(objectMapper.writeValueAsString(nodeConsistent), NodeConsistent.class);
        assertEquals(nodeConsistent, nodeConsistent1);
    }

    @Test
    public void nodeDataUpdatedTest() throws IOException {
        NodeDataUpdated updated = new NodeDataUpdated("a", "b");
        assertEquals("a", updated.getId());
        assertEquals(NodeDataUpdated.TYPE, updated.getType());
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
        assertEquals(NodeDescriptionUpdated.TYPE, updated.getType());
        assertEquals("b", updated.getDescription());

        NodeDescriptionUpdated updated2 = objectMapper.readValue(objectMapper.writeValueAsString(updated), NodeDescriptionUpdated.class);
        assertEquals(updated, updated2);

        new EqualsTester()
                .addEqualityGroup(new NodeDescriptionUpdated("a", "b"), new NodeDescriptionUpdated("a", "b"))
                .addEqualityGroup(new NodeDescriptionUpdated("c", "d"), new NodeDescriptionUpdated("c", "d"))
                .testEquals();
    }

    @Test
    public void nodeNameUpdatedTest() throws IOException {
        NodeNameUpdated updated = new NodeNameUpdated("a", "b");
        assertEquals("a", updated.getId());
        assertEquals(NodeNameUpdated.TYPE, updated.getType());
        assertEquals("b", updated.getName());

        NodeNameUpdated updated2 = objectMapper.readValue(objectMapper.writeValueAsString(updated), NodeNameUpdated.class);
        assertEquals(updated, updated2);

        new EqualsTester()
                .addEqualityGroup(new NodeNameUpdated("a", "b"), new NodeNameUpdated("a", "b"))
                .addEqualityGroup(new NodeNameUpdated("c", "d"), new NodeNameUpdated("c", "d"))
                .testEquals();
    }

    @Test
    public void nodeRemovedTest() throws IOException {
        NodeRemoved removed = new NodeRemoved("a", "b");
        assertEquals("a", removed.getId());
        assertEquals("b", removed.getParentId());
        assertEquals(NodeRemoved.TYPE, removed.getType());

        NodeRemoved removed2 = objectMapper.readValue(objectMapper.writeValueAsString(removed), NodeRemoved.class);
        assertEquals(removed, removed2);

        new EqualsTester()
                .addEqualityGroup(new NodeRemoved("a", "b"), new NodeRemoved("a", "b"))
                .addEqualityGroup(new NodeRemoved("b", "c"), new NodeRemoved("b", "c"))
                .testEquals();
    }

    @Test
    public void parentChangedTest() throws IOException {
        ParentChanged changed = new ParentChanged("a");
        assertEquals("a", changed.getId());
        assertEquals(ParentChanged.TYPE, changed.getType());

        ParentChanged changed2 = objectMapper.readValue(objectMapper.writeValueAsString(changed), ParentChanged.class);
        assertEquals(changed, changed2);

        new EqualsTester()
                .addEqualityGroup(new ParentChanged("a"), new ParentChanged("a"))
                .addEqualityGroup(new ParentChanged("b"), new ParentChanged("b"))
                .testEquals();
    }

    @Test
    public void timeSeriesClearedTest() throws IOException {
        TimeSeriesCleared cleared = new TimeSeriesCleared("a");
        assertEquals("a", cleared.getId());
        assertEquals(TimeSeriesCleared.TYPE, cleared.getType());

        TimeSeriesCleared cleared2 = objectMapper.readValue(objectMapper.writeValueAsString(cleared), TimeSeriesCleared.class);
        assertEquals(cleared, cleared2);

        new EqualsTester()
                .addEqualityGroup(new TimeSeriesCleared("a"), new TimeSeriesCleared("a"))
                .addEqualityGroup(new TimeSeriesCleared("b"), new TimeSeriesCleared("b"))
                .testEquals();
    }

    @Test
    public void timeSeriesCreatedTest() throws IOException {
        TimeSeriesCreated created = new TimeSeriesCreated("a", "b");
        assertEquals("a", created.getId());
        assertEquals(TimeSeriesCreated.TYPE, created.getType());
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
        assertEquals(TimeSeriesDataUpdated.TYPE, updated.getType());
        assertEquals("b", updated.getTimeSeriesName());

        TimeSeriesDataUpdated updated2 = objectMapper.readValue(objectMapper.writeValueAsString(updated), TimeSeriesDataUpdated.class);
        assertEquals(updated, updated2);

        new EqualsTester()
                .addEqualityGroup(new TimeSeriesDataUpdated("a", "b"), new TimeSeriesDataUpdated("a", "b"))
                .addEqualityGroup(new TimeSeriesDataUpdated("c", "d"), new TimeSeriesDataUpdated("c", "d"))
                .testEquals();
    }

    @Test
    public void eventListTest() throws IOException {
        NodeEventList eventList = new NodeEventList(new NodeCreated("a", "c"), new NodeRemoved("b", "d"));
        assertEquals("[NodeCreated(id=a, parentId=c), NodeRemoved(id=b, parentId=d)]", eventList.toString());

        NodeEventList eventList2 = objectMapper.readValue(objectMapper.writeValueAsString(eventList), NodeEventList.class);
        assertEquals(eventList, eventList2);
    }
}
