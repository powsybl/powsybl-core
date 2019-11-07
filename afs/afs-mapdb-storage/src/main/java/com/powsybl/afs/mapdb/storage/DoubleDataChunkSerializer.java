/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.mapdb.storage;

import com.powsybl.timeseries.CompressedDoubleDataChunk;
import com.powsybl.timeseries.DoubleDataChunk;
import com.powsybl.timeseries.UncompressedDoubleDataChunk;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.Serializer;

import java.io.IOException;
import java.io.Serializable;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class DoubleDataChunkSerializer implements Serializer<DoubleDataChunk>, Serializable {

    public static final DoubleDataChunkSerializer INSTANCE = new DoubleDataChunkSerializer();

    private DoubleDataChunkSerializer() {
    }

    @Override
    public void serialize(DataOutput2 out, DoubleDataChunk chunk) throws IOException {
        out.writeInt(MapDbStorageConstants.STORAGE_VERSION);
        if (chunk instanceof UncompressedDoubleDataChunk) {
            UncompressedDoubleDataChunk uncompressedChunk = (UncompressedDoubleDataChunk) chunk;
            out.writeUTF("uncompressed");
            out.writeInt(uncompressedChunk.getOffset());
            out.writeInt(uncompressedChunk.getLength());
            for (double value : uncompressedChunk.getValues()) {
                out.writeDouble(value);
            }
        } else if (chunk instanceof CompressedDoubleDataChunk) {
            CompressedDoubleDataChunk compressedChunk = (CompressedDoubleDataChunk) chunk;
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
    public DoubleDataChunk deserialize(DataInput2 input, int available) throws IOException {
        input.readInt(); // Storage version is retrieved here
        String type = input.readUTF();
        if ("uncompressed".equals(type)) {
            int offset = input.readInt();
            int length = input.readInt();
            double[] values = new double[length];
            for (int i = 0; i < length; i++) {
                values[i] = input.readDouble();
            }
            return new UncompressedDoubleDataChunk(offset, values);
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
            return new CompressedDoubleDataChunk(offset, uncompressedLength, stepValues, stepLengths);
        } else {
            throw new AssertionError();
        }
    }
}
