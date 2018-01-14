/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.math.timeseries;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CompactStringBuffer {

    private final IntBuffer buffer;

    private final BiList<String> dict = new BiList<>();

    public CompactStringBuffer(int size) {
        this.buffer = createIntBuffer(size);
        for (int i = 0; i < size; i++) {
            buffer.put(-1);
        }
    }

    private static IntBuffer createIntBuffer(int size) {
        return ByteBuffer.allocateDirect(size * Integer.BYTES).asIntBuffer();
    }

    public void putString(int index, String value) {
        int num;
        if (value == null) {
            num = -1;
        } else {
            num = dict.indexOf(value);
            if (num == -1) {
                // create a new entry
                num = dict.add(value);
            }
        }
        buffer.put(index, num);
    }

    public String getString(int index) {
        int num = buffer.get(index);
        if (num == -1) {
            return null;
        } else {
            return dict.get(num);
        }
    }

    public int capacity() {
        return buffer.capacity();
    }

    public String[] toArray() {
        String[] array = new String[buffer.capacity()];
        for (int i = 0; i < buffer.capacity(); i++) {
            array[i] = getString(i);
        }
        return array;
    }
}
