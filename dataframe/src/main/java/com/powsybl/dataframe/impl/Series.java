/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dataframe.impl;

import com.powsybl.commons.PowsyblException;

import java.util.Optional;

/**
 * POJO representation of a series.
 * Using a "union type" for now (only one type of array is used).
 */
public class Series {

    private final boolean index;
    private final String name;
    private final double[] doubles;
    private final int[] ints;
    private final boolean[] booleans;
    private final String[] strings;

    public Series(String name, double[] values) {
        this(false, name, values, null, null, null);
    }

    public Series(String name, String[] values) {
        this(false, name, null, null, null, values);
    }

    public Series(String name, int[] values) {
        this(false, name, null, values, null, null);
    }

    public Series(String name, boolean[] values) {
        this(false, name, null, null, values, null);
    }

    public Series(boolean index, String name, double[] doubles, int[] ints, boolean[] booleans, String[] strings) {
        this.index = index;
        this.name = name;
        this.doubles = doubles;
        this.ints = ints;
        this.booleans = booleans;
        this.strings = strings;
    }

    public static Series index(String name, String[] values) {
        return new Series(true, name, null, null, null, values);
    }

    public static Series index(String name, int[] values) {
        return new Series(true, name, null, values, null, null);
    }

    public boolean isIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public double[] getDoubles() {
        return Optional.ofNullable(doubles)
            .orElseThrow(() -> new PowsyblException("Series " + getName() + " is not of type double"));
    }

    public int[] getInts() {
        return Optional.ofNullable(ints)
            .orElseThrow(() -> new PowsyblException("Series " + getName() + " is not of type int"));
    }

    public boolean[] getBooleans() {
        return Optional.ofNullable(booleans)
            .orElseThrow(() -> new PowsyblException("Series " + getName() + " is not of type boolean"));
    }

    public String[] getStrings() {
        return Optional.ofNullable(strings)
            .orElseThrow(() -> new PowsyblException("Series " + getName() + " is not of type string"));
    }
}
