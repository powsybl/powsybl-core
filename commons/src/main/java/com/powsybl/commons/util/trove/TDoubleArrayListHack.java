/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.util.trove;

import gnu.trove.list.array.TDoubleArrayList;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TDoubleArrayListHack extends TDoubleArrayList {

    public TDoubleArrayListHack() {
    }

    public TDoubleArrayListHack(int capacity) {
        super(capacity);
    }

    public TDoubleArrayListHack(double[] values) {
        super(values);
    }

    public double[] getData() {
        return _data;
    }
}
