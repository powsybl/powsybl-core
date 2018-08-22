/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.mapdb.storage;

import com.powsybl.timeseries.CompressedStringArrayChunk;
import com.powsybl.timeseries.StringArrayChunk;
import com.powsybl.timeseries.UncompressedStringArrayChunk;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.Serializer;

import java.io.IOException;
import java.io.Serializable;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class StringArrayChunkSerializer implements Serializer<StringArrayChunk>, Serializable {

    public static final StringArrayChunkSerializer INSTANCE = new StringArrayChunkSerializer();

    private StringArrayChunkSerializer() {
    }

    @Override
    public void serialize(DataOutput2 out, StringArrayChunk chunk) throws IOException {
        if (chunk instanceof UncompressedStringArrayChunk) {
            UncompressedStringArrayChunk uncompressedChunk = (UncompressedStringArrayChunk) chunk;
            out.writeUTF("uncompressed");
            out.writeInt(uncompressedChunk.getOffset());
            out.writeInt(uncompressedChunk.getLength());
            for (String value : uncompressedChunk.getValues()) {
                out.writeUTF(value);
            }
        } else if (chunk instanceof CompressedStringArrayChunk) {
            CompressedStringArrayChunk compressedChunk = (CompressedStringArrayChunk) chunk;
            out.writeUTF("compressed");
            out.writeInt(compressedChunk.getOffset());
            out.writeInt(compressedChunk.getUncompressedLength());
            out.writeInt(compressedChunk.getStepLengths().length);
            for (int value : compressedChunk.getStepLengths()) {
                out.writeInt(value);
            }
            out.writeInt(compressedChunk.getStepValues().length);
            for (String value : compressedChunk.getStepValues()) {
                out.writeUTF(value);
            }
        } else {
            throw new AssertionError();
        }
    }

    @Override
    public StringArrayChunk deserialize(DataInput2 input, int available) throws IOException {
        String type = input.readUTF();
        if ("uncompressed".equals(type)) {
            int offset = input.readInt();
            int length = input.readInt();
            String[] values = new String[length];
            for (int i = 0; i < length; i++) {
                values[i] = input.readUTF();
            }
            return new UncompressedStringArrayChunk(offset, values);
        } else if ("compressed".equals(type)) {
            int offset = input.readInt();
            int uncompressedLength = input.readInt();
            int stepLengthsLength = input.readInt();
            int[] stepLengths = new int[stepLengthsLength];
            for (int i = 0; i < stepLengthsLength; i++) {
                stepLengths[i] = input.readInt();
            }
            int stepValuesLength = input.readInt();
            String[] stepValues = new String[stepValuesLength];
            for (int i = 0; i < stepValuesLength; i++) {
                stepValues[i] = input.readUTF();
            }
            return new CompressedStringArrayChunk(offset, uncompressedLength, stepValues, stepLengths);
        } else {
            throw new AssertionError();
        }
    }
}
