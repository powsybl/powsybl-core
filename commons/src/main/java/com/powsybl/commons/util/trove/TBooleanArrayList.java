/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.util.trove;

import gnu.trove.list.array.TByteArrayList;

/**
 * TBooleanArrayList implement (not provided by trove4j) based on TByteArrayList.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class TBooleanArrayList {

    private final TByteArrayList list;

    public TBooleanArrayList(int capacity) {
        list = new TByteArrayList(capacity);
    }

    private static byte toByte(boolean b) {
        return (byte) (b ? 1 : 0);
    }

    private static boolean fromByte(byte b) {
        return b == 1;
    }

    public boolean add(boolean val) {
        return list.add(toByte(val));
    }

    public boolean get(int offset) {
        return fromByte(list.get(offset));
    }

    public void ensureCapacity(int capacity) {
        list.ensureCapacity(capacity);
    }

    public boolean removeAt(int offset) {
        return fromByte(list.removeAt(offset));
    }

    public void remove(int offset, int length) {
        list.remove(offset, length);
    }

    public int size() {
        return list.size();
    }

    public boolean set(int offset, boolean val) {
        return fromByte(list.set(offset, toByte(val)));
    }

    public void fill(int fromIndex, int toIndex, boolean val) {
        list.fill(fromIndex, toIndex, toByte(val));
    }
}
