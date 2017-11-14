/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.math.timeseries;

import java.util.Arrays;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class StoredDoubleTimeSeries extends AbstractTimeSeries<DoublePoint, DoubleArrayChunk> implements DoubleTimeSeries {

    private static final double[] NAN_ARRAY = new double[] {Double.NaN};

    public StoredDoubleTimeSeries(TimeSeriesMetadata metadata, DoubleArrayChunk... chunks) {
        super(metadata, chunks);
    }

    public StoredDoubleTimeSeries(TimeSeriesMetadata metadata, List<DoubleArrayChunk> chunks) {
        super(metadata, chunks);
    }

    protected CompressedDoubleArrayChunk createGapFillingChunk(int i, int length) {
        return new CompressedDoubleArrayChunk(i, length, NAN_ARRAY, new int[] {length});
    }

    @Override
    public double[] toArray() {
        double[] array = new double[metadata.getIndex().getPointCount()];
        Arrays.fill(array, Double.NaN);
        chunks.forEach(chunk -> chunk.fillArray(array));
        return array;
    }
}
