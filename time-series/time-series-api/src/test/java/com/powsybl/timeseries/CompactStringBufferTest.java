/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries;

import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CompactStringBufferTest {

    @Test
    public void test() {
        CompactStringBuffer buffer = new CompactStringBuffer(ByteBuffer::allocate, 4);
        buffer.putString(0, "aaa");
        buffer.putString(1, "aaa");
        buffer.putString(3, "bbb");

        assertEquals("aaa", buffer.getString(0));
        assertEquals("aaa", buffer.getString(1));
        assertEquals(null, buffer.getString(2));
        assertEquals("bbb", buffer.getString(3));
    }
}
