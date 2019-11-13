/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ieeecdf.model;

import com.univocity.parsers.annotations.FixedWidth;
import com.univocity.parsers.annotations.Parsed;

/**
 * Columns  1- 3   Loss zone number  (I)
 * Columns  5-16   Loss zone name (A)
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class IeeeCdfLossZone {

    @FixedWidth(from = 0, to = 3)
    @Parsed
    private int number;

    @FixedWidth(from = 4, to = 16)
    @Parsed
    private String name;

    /**
     * Loss zone number  (I)
     */
    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    /**
     * Loss zone name (A)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
