/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage;

import com.google.common.collect.ImmutableMap;
import com.google.common.testing.EqualsTester;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NodeGenericMetadataTest {

    @Test
    public void test() {
        // check getters
        NodeGenericMetadata metadata = new NodeGenericMetadata()
                .setString("s1", "a")
                .setString("s2", "b")
                .setDouble("d1", 0d)
                .setInt("i1", 1)
                .setBoolean("b1", true);
        assertEquals(metadata.getString("s1"), "a");
        assertEquals(metadata.getString("s2"), "b");
        assertEquals(ImmutableMap.of("s1", "a", "s2", "b"), metadata.getStrings());
        assertEquals(metadata.getDouble("d1"), 0d, 0d);
        assertEquals(ImmutableMap.of("d1", 0d), metadata.getDoubles());
        assertEquals(metadata.getInt("i1"), 1);
        assertEquals(ImmutableMap.of("i1", 1), metadata.getInts());
        assertEquals(metadata.getBoolean("b1"), true);
        assertEquals(ImmutableMap.of("b1", true), metadata. getBooleans());

        // check metadata not found
        try {
            metadata.getString("s3");
            fail();
        } catch (IllegalArgumentException e) {
        }

        // check equality
        new EqualsTester()
                .addEqualityGroup(new NodeGenericMetadata().setString("s1", "a"),
                                 new NodeGenericMetadata().setString("s1", "a"))
                .addEqualityGroup(new NodeGenericMetadata().setDouble("d1", 3d),
                                  new NodeGenericMetadata().setDouble("d1", 3d))
                .testEquals();
    }
}