/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.afs.storage.json.AppStorageJsonModule;
import com.powsybl.commons.json.JsonUtil;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NodeInfoTest {

    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws Exception {
        objectMapper = JsonUtil.createObjectMapper()
                .registerModule(new AppStorageJsonModule());
    }

    @Test
    public void nodeInfoTest() throws IOException {
        NodeInfo info = new NodeInfo("a", "b", "c", "d", 1000000, 1000001, 0,
                new NodeGenericMetadata().setString("s1", "s1")
                                         .setDouble("d1", 1d)
                                         .setInt("i1", 2)
                                         .setBoolean("b1", true));
        NodeInfo info2 = objectMapper.readValue(objectMapper.writeValueAsString(info), NodeInfo.class);
        assertEquals(info, info2);
        info.setVersion(1);
        assertEquals(1, info.getVersion());
        assertNotEquals("A non NodeInfo object", info);
    }
}
