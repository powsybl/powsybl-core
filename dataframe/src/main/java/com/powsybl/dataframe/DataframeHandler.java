/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dataframe;

/**
 * Receives series data, is in charge of doing something with it,
 * typically writing to a data structure.
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public interface DataframeHandler {

    @FunctionalInterface
    interface IntSeriesWriter {
        void set(int index, int value);
    }

    @FunctionalInterface
    interface DoubleSeriesWriter {
        void set(int index, double value);
    }

    @FunctionalInterface
    interface BooleanSeriesWriter {
        void set(int index, boolean value);
    }

    @FunctionalInterface
    interface StringSeriesWriter {
        void set(int index, String value);
    }

    void allocate(int seriesCount);

    StringSeriesWriter newStringIndex(String name, int size);

    IntSeriesWriter newIntIndex(String name, int size);

    StringSeriesWriter newStringSeries(String name, int size);

    IntSeriesWriter newIntSeries(String name, int size);

    BooleanSeriesWriter newBooleanSeries(String name, int size);

    DoubleSeriesWriter newDoubleSeries(String name, int size);

}
