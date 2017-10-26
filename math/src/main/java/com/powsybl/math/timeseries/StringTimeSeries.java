/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.math.timeseries;

import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class StringTimeSeries extends AbstractTimeSeries<StringPoint, StringArrayChunk> implements TimeSeries<StringPoint, StringArrayChunk> {

    private static final String[] NULL_ARRAY = new String[] {null};

    public StringTimeSeries(TimeSeriesMetadata metadata, StringArrayChunk... chunks) {
        super(metadata, chunks);
    }

    public StringTimeSeries(TimeSeriesMetadata metadata, List<StringArrayChunk> chunks) {
        super(metadata, chunks);
    }

    protected CompressedStringArrayChunk createGapFillingChunk(int i, int length) {
        return new CompressedStringArrayChunk(i, length, NULL_ARRAY, new int[] {length});
    }

    public String[] toArray() {
        String[] array = new String[metadata.getIndex().getPointCount()];
        chunks.forEach(chunk -> chunk.fillArray(array));
        return array;
    }
}
