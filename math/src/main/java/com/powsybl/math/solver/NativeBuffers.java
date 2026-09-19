/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.solver;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;

/**
 * Helpers for allocating direct native-order buffers passed to the JNI solvers.
 * The native side uses GetDirectBufferAddress, which requires direct allocation
 * and assumes platform byte order.
 *
 * Allocate once and reuse across calls when the dimensions are stable - that's
 * the whole point of the buffer-based API.
 *
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
public final class NativeBuffers {

    private NativeBuffers() {
    }

    public static IntBuffer allocInt(int capacity) {
        return ByteBuffer.allocateDirect(capacity * Integer.BYTES)
                .order(ByteOrder.nativeOrder()).asIntBuffer();
    }

    public static DoubleBuffer allocDouble(int capacity) {
        return ByteBuffer.allocateDirect(capacity * Double.BYTES)
                .order(ByteOrder.nativeOrder()).asDoubleBuffer();
    }

    /** Allocate + copy. Use only for one-shot setup; reuse buffers in hot paths. */
    public static IntBuffer wrap(int[] data) {
        IntBuffer buf = allocInt(data.length);
        buf.put(data);
        buf.clear();
        return buf;
    }

    /** Allocate + copy. Use only for one-shot setup; reuse buffers in hot paths. */
    public static DoubleBuffer wrap(double[] data) {
        DoubleBuffer buf = allocDouble(data.length);
        buf.put(data);
        buf.clear();
        return buf;
    }

    public static void copyFrom(IntBuffer dst, int[] src) {
        dst.clear();
        dst.put(src);
        dst.clear();
    }

    public static void copyFrom(DoubleBuffer dst, double[] src) {
        dst.clear();
        dst.put(src);
        dst.clear();
    }

    public static void copyTo(DoubleBuffer src, double[] dst) {
        src.clear();
        src.get(dst);
        src.clear();
    }
}
