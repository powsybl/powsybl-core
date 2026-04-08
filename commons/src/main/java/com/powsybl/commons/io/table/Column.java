/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.io.table;

import java.text.NumberFormat;
import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class Column {

    private final String name;

    private HorizontalAlignment horizontalAlignment;

    private HorizontalAlignment titleHorizontalAlignment;

    private NumberFormat numberFormat = null;

    private int colspan = 1;

    public Column(String name) {
        this.name = Objects.requireNonNull(name);
        this.horizontalAlignment = HorizontalAlignment.LEFT;
        this.titleHorizontalAlignment = HorizontalAlignment.LEFT;
    }

    public String getName() {
        return name;
    }

    public HorizontalAlignment getHorizontalAlignment() {
        return horizontalAlignment;
    }

    public HorizontalAlignment getTitleHorizontalAlignment() {
        return titleHorizontalAlignment;
    }

    public Column setTitleHorizontalAlignment(HorizontalAlignment titleHorizontalAlignment) {
        this.titleHorizontalAlignment = Objects.requireNonNull(titleHorizontalAlignment);
        return this;
    }

    public Column setHorizontalAlignment(HorizontalAlignment horizontalAlignment) {
        this.horizontalAlignment = Objects.requireNonNull(horizontalAlignment);
        return this;
    }

    public NumberFormat getNumberFormat() {
        return numberFormat;
    }

    public Column setNumberFormat(NumberFormat numberFormat) {
        this.numberFormat = Objects.requireNonNull(numberFormat);
        return this;
    }

    public int getColspan() {
        return colspan;
    }

    public Column setColspan(int colspan) {
        this.colspan = checkColspan(colspan);
        return this;
    }

    private static int checkColspan(int colspan) {
        if (colspan < 1) {
            throw new IllegalArgumentException("colspan must be greater than 0");
        }
        return colspan;
    }
}
