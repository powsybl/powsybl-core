/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.mapdb.storage;

import com.powsybl.timeseries.CompressedDoubleArrayChunk;
import com.powsybl.timeseries.DoubleArrayChunk;
import com.powsybl.timeseries.UncompressedDoubleArrayChunk;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.Serializer;

import java.io.IOException;
import java.io.Serializable;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class DoubleArrayChunkSerializer implements Serializer<DoubleArrayChunk>, Serializable {

    public static final DoubleArrayChunkSerializer INSTANCE = new DoubleArrayChunkSerializer();

    private DoubleArrayChunkSerializer() {
    }

    @Override
    public void serialize(DataOutput2 out, DoubleArrayChunk chunk) throws IOException {
        if (chunk instanceof UncompressedDoubleArrayChunk) {
            UncompressedDoubleArrayChunk uncompressedChunk = (UncompressedDoubleArrayChunk) chunk;
            out.writeUTF("uncompressed");
            out.writeInt(uncompressedChunk.getOffset());
            out.writeInt(uncompressedChunk.getLength());
            for (double value : uncompressedChunk.getValues()) {
                out.writeDouble(value);
            }
        } else if (chunk instanceof CompressedDoubleArrayChunk) {
            CompressedDoubleArrayChunk compressedChunk = (CompressedDoubleArrayChunk) chunk;
            out.writeUTF("compressed");
            out.writeInt(compressedChunk.getOffset());
            out.writeInt(compressedChunk.getUncompressedLength());
            out.writeInt(compressedChunk.getStepLengths().length);
            for (int value : compressedChunk.getStepLengths()) {
                out.writeInt(value);
            }
            out.writeInt(compressedChunk.getStepValues().length);
            for (double value : compressedChunk.getStepValues()) {
                out.writeDouble(value);
            }
        } else {
            throw new AssertionError();
        }
    }

    @Override
    public DoubleArrayChunk deserialize(DataInput2 input, int available) throws IOException {
        String type = input.readUTF();
        if ("uncompressed".equals(type)) {
            int offset = input.readInt();
            int length = input.readInt();
            double[] values = new double[length];
            for (int i = 0; i < length; i++) {
                values[i] = input.readDouble();
            }
            return new UncompressedDoubleArrayChunk(offset, values);
        } else if ("compressed".equals(type)) {
            int offset = input.readInt();
            int uncompressedLength = input.readInt();
            int stepLengthsLength = input.readInt();
            int[] stepLengths = new int[stepLengthsLength];
            for (int i = 0; i < stepLengthsLength; i++) {
                stepLengths[i] = input.readInt();
            }
            int stepValuesLength = input.readInt();
            double[] stepValues = new double[stepValuesLength];
            for (int i = 0; i < stepValuesLength; i++) {
                stepValues[i] = input.readDouble();
            }
            return new CompressedDoubleArrayChunk(offset, uncompressedLength, stepValues, stepLengths);
        } else {
            throw new AssertionError();
        }
    }
}
