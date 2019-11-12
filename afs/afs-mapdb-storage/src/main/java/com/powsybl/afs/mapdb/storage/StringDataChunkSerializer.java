/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.mapdb.storage;

import com.powsybl.timeseries.CompressedStringDataChunk;
import com.powsybl.timeseries.StringDataChunk;
import com.powsybl.timeseries.UncompressedStringDataChunk;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.Serializer;

import java.io.IOException;
import java.io.Serializable;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class StringDataChunkSerializer implements Serializer<StringDataChunk>, Serializable {

    public static final StringDataChunkSerializer INSTANCE = new StringDataChunkSerializer();

    private StringDataChunkSerializer() {
    }

    @Override
    public void serialize(DataOutput2 out, StringDataChunk chunk) throws IOException {
        out.writeInt(MapDbStorageConstants.STORAGE_VERSION);
        if (chunk instanceof UncompressedStringDataChunk) {
            UncompressedStringDataChunk uncompressedChunk = (UncompressedStringDataChunk) chunk;
            out.writeUTF("uncompressed");
            out.writeInt(uncompressedChunk.getOffset());
            out.writeInt(uncompressedChunk.getLength());
            for (String value : uncompressedChunk.getValues()) {
                out.writeUTF(value);
            }
        } else if (chunk instanceof CompressedStringDataChunk) {
            CompressedStringDataChunk compressedChunk = (CompressedStringDataChunk) chunk;
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
    public StringDataChunk deserialize(DataInput2 input, int available) throws IOException {
        input.readInt(); // Storage version is retrieved here
        String type = input.readUTF();
        if ("uncompressed".equals(type)) {
            int offset = input.readInt();
            int length = input.readInt();
            String[] values = new String[length];
            for (int i = 0; i < length; i++) {
                values[i] = input.readUTF();
            }
            return new UncompressedStringDataChunk(offset, values);
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
            return new CompressedStringDataChunk(offset, uncompressedLength, stepValues, stepLengths);
        } else {
            throw new AssertionError();
        }
    }
}
