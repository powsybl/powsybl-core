/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.ImmutableMap;
import com.powsybl.afs.storage.json.AppStorageJsonModule;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NodeInfoTest {

    private AppStorage storage;

    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws Exception {
        storage = Mockito.mock(AppStorage.class);
        Mockito.when(storage.fromString(Mockito.anyString()))
                .then(invocationOnMock -> new NodeIdMock(invocationOnMock.getArguments()[0].toString()));

        objectMapper = new ObjectMapper()
                .registerModule(new AppStorageJsonModule());
        // add storage as an attribute du deserialization context
        objectMapper.setConfig(objectMapper.getDeserializationConfig().withAttribute("storage", this.storage));
    }

    @Test
    public void nodeIdTest() throws IOException {
        List<NodeId> idList = Arrays.asList(new NodeIdMock("a"), new NodeIdMock("b"));
        List<NodeId> idList2 = objectMapper.readValue(objectMapper.writeValueAsString(idList),
                                                      TypeFactory.defaultInstance().constructCollectionType(List.class, NodeId.class));
        assertEquals(idList, idList2);
    }

    @Test
    public void nodeInfoTest() throws IOException {
        NodeInfo info = new NodeInfo(new NodeIdMock("a"), "b", "c", "d", 1000000, 1000001, 0, ImmutableMap.of("s1", "s1"),
                ImmutableMap.of("d1", 1d), ImmutableMap.of("i1", 2), ImmutableMap.of("b1", true));
        NodeInfo info2 = objectMapper.readValue(objectMapper.writeValueAsString(info), NodeInfo.class);
        assertEquals(info, info2);
    }
}
