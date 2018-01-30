/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.afs.storage.json.AppStorageJsonModule;
import com.powsybl.commons.json.JsonUtil;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NodeDependencyTest {

    @Test
    public void test() throws IOException {
        NodeInfo info = new NodeInfo("a", "b", "c", "d", 1000000, 1000001, 0, new NodeGenericMetadata());
        NodeDependency dependency = new NodeDependency("l", info);
        assertEquals("l", dependency.getName());
        assertEquals(info, dependency.getNodeInfo());
        assertEquals("NodeDependency(name=l, nodeInfo=NodeInfo(id=a, name=b, pseudoClass=c, description=d, creationTime=1000000, modificationTime=1000001, version=0, genericMetadata=NodeGenericMetadata(stringMetadata={}, doubleMetadata={}, intMetadata={}, booleanMetadata={})))", dependency.toString());

        ObjectMapper objectMapper = JsonUtil.createObjectMapper()
                .registerModule(new AppStorageJsonModule());

        NodeDependency dependency2 = objectMapper.readValue(objectMapper.writeValueAsString(dependency), NodeDependency.class);
        assertEquals(dependency, dependency2);
    }
}
