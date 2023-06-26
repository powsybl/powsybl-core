/**
 * Copyright (c) 2021-2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dataframe.update;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Sylvain Leclerc <sylvain.leclerc@rte-france.com>
 */
public class TestDoubleSeries implements DoubleSeries {

    private final List<Double> values;

    public TestDoubleSeries(double... values) {
        this.values = Arrays.stream(values).boxed().collect(Collectors.toList());
    }

    @Override
    public double get(int index) {
        return values.get(index);
    }
}
