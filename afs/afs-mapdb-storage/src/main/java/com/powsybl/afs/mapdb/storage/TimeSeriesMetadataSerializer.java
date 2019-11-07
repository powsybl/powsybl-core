/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.mapdb.storage;

import com.powsybl.timeseries.TimeSeriesDataType;
import com.powsybl.timeseries.TimeSeriesIndex;
import com.powsybl.timeseries.TimeSeriesMetadata;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.Serializer;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class TimeSeriesMetadataSerializer implements Serializer<TimeSeriesMetadata>, Serializable {

    public static final TimeSeriesMetadataSerializer INSTANCE = new TimeSeriesMetadataSerializer();

    private TimeSeriesMetadataSerializer() {
    }

    @Override
    public void serialize(DataOutput2 out, TimeSeriesMetadata metadata) throws IOException {
        out.writeInt(MapDbStorageConstants.STORAGE_VERSION);
        out.writeUTF(metadata.getName());
        out.writeUTF(metadata.getDataType().name());
        out.writeInt(metadata.getTags().size());
        for (Map.Entry<String, String> e : metadata.getTags().entrySet()) {
            out.writeUTF(e.getKey());
            out.writeUTF(e.getValue());
        }
        TimeSeriesIndexSerializer.INSTANCE.serialize(out, metadata.getIndex());
    }

    @Override
    public TimeSeriesMetadata deserialize(DataInput2 input, int available) throws IOException {
        input.readInt(); // Storage version is retrieved here
        String name = input.readUTF();
        TimeSeriesDataType dataType = TimeSeriesDataType.valueOf(input.readUTF());
        Map<String, String> tags = new HashMap<>();
        int size = input.readInt();
        for (int i = 0; i < size; i++) {
            String key = input.readUTF();
            String value = input.readUTF();
            tags.put(key, value);
        }
        TimeSeriesIndex index = TimeSeriesIndexSerializer.INSTANCE.deserialize(input, available);
        return new TimeSeriesMetadata(name, dataType, tags, index);
    }
}
