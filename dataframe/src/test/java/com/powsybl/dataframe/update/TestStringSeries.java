/**
 * Copyright (c) 2021-2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dataframe.update;

import java.util.List;

/**
 * @author Sylvain Leclerc <sylvain.leclerc@rte-france.com>
 */
public class TestStringSeries implements StringSeries {

    private final List<String> values;

    public TestStringSeries(String... values) {
        this.values = List.of(values);
    }

    @Override
    public String get(int index) {
        return values.get(index);
    }
}
