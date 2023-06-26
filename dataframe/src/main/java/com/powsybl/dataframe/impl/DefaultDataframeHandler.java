/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dataframe.impl;

import com.powsybl.dataframe.DataframeHandler;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Writes series data to POJOs.
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class DefaultDataframeHandler implements DataframeHandler {

    private final Consumer<Series> seriesConsumer;

    public DefaultDataframeHandler(Consumer<Series> seriesConsumer) {
        this.seriesConsumer = Objects.requireNonNull(seriesConsumer);
    }

    @Override
    public void allocate(int seriesCount) {
        //Nothing to do
    }

    @Override
    public StringSeriesWriter newStringIndex(String name, int size) {
        String[] values = new String[size];
        seriesConsumer.accept(Series.index(name, values));
        return (i, s) -> values[i] = s;
    }

    @Override
    public IntSeriesWriter newIntIndex(String name, int size) {
        int[] values = new int[size];
        seriesConsumer.accept(Series.index(name, values));
        return (i, s) -> values[i] = s;
    }

    @Override
    public StringSeriesWriter newStringSeries(String name, int size) {
        String[] values = new String[size];
        seriesConsumer.accept(new Series(name, values));
        return (i, s) -> values[i] = s;
    }

    @Override
    public IntSeriesWriter newIntSeries(String name, int size) {
        int[] values = new int[size];
        seriesConsumer.accept(new Series(name, values));
        return (i, s) -> values[i] = s;
    }

    @Override
    public BooleanSeriesWriter newBooleanSeries(String name, int size) {
        boolean[] values = new boolean[size];
        seriesConsumer.accept(new Series(name, values));
        return (i, s) -> values[i] = s;
    }

    @Override
    public DoubleSeriesWriter newDoubleSeries(String name, int size) {
        double[] values = new double[size];
        seriesConsumer.accept(new Series(name, values));
        return (i, s) -> values[i] = s;
    }
}
