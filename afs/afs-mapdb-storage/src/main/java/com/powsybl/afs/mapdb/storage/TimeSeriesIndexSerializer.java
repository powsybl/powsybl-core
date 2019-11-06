/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.mapdb.storage;

import com.powsybl.timeseries.RegularTimeSeriesIndex;
import com.powsybl.timeseries.TimeSeriesIndex;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.Serializer;

import java.io.IOException;
import java.io.Serializable;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class TimeSeriesIndexSerializer implements Serializer<TimeSeriesIndex>, Serializable {

    public static final TimeSeriesIndexSerializer INSTANCE = new TimeSeriesIndexSerializer();

    private TimeSeriesIndexSerializer() {
    }

    @Override
    public void serialize(DataOutput2 out, TimeSeriesIndex index) throws IOException {
        out.writeInt(MapDbStorageConstants.STORAGE_VERSION);
        if (index instanceof RegularTimeSeriesIndex) {
            RegularTimeSeriesIndex regularIndex = (RegularTimeSeriesIndex) index;
            out.writeUTF("regularIndex");
            out.writeLong(regularIndex.getStartTime());
            out.writeLong(regularIndex.getEndTime());
            out.writeLong(regularIndex.getSpacing());
        } else {
            throw new AssertionError();
        }
    }

    @Override
    public TimeSeriesIndex deserialize(DataInput2 input, int available) throws IOException {
        input.readInt(); // Storage version is retrieved here
        String indexType = input.readUTF();
        long startTime = input.readLong();
        long endTime = input.readLong();
        long spacing = input.readLong();
        if ("regularIndex".equals(indexType)) {
            return new RegularTimeSeriesIndex(startTime, endTime, spacing);
        } else {
            throw new AssertionError();
        }
    }
}
