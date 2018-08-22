/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractPoint {

    protected final int index;

    protected final long time;

    protected AbstractPoint(int index, long time) {
        if (index < 0) {
            throw new IllegalArgumentException("Bad index value " + index);
        }
        this.index = index;
        this.time = Objects.requireNonNull(time);
    }

    public int getIndex() {
        return index;
    }

    public long getTime() {
        return time;
    }
}
