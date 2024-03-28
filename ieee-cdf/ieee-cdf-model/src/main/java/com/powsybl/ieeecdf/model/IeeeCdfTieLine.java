/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ieeecdf.model;

import com.univocity.parsers.annotations.FixedWidth;
import com.univocity.parsers.annotations.Parsed;

/**
 * <p>
 * @see <a href="https://labs.ece.uw.edu/pstca/formats/cdf.txt">https://labs.ece.uw.edu/pstca/formats/cdf.txt</a>
 * </p>
 *
 * <pre>
 * Columns  1- 4   Metered bus number (I)
 * Columns  7-8    Metered area number (I)
 * Columns  11-14  Non-metered bus number (I)
 * Columns  17-18  Non-metered area number (I)
 * Column   21     Circuit number
 * </pre>
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class IeeeCdfTieLine {

    /**
     * Metered bus number
     */
    @FixedWidth(from = 0, to = 4)
    @Parsed
    private int meteredBusNumber;

    /**
     * Metered area number
     */
    @FixedWidth(from = 6, to = 8)
    @Parsed
    private int meteredAreaNumber;

    /**
     * Non-metered bus number
     */
    @FixedWidth(from = 10, to = 14)
    @Parsed
    private int nonMeteredBusNumber;

    /**
     * Non-metered area number
     */
    @FixedWidth(from = 16, to = 18)
    @Parsed
    private int nonMeteredAreaNumber;

    /**
     * Circuit number
     */
    @FixedWidth(from = 20, to = 21)
    @Parsed
    private int circuitNumber;

    public int getMeteredBusNumber() {
        return meteredBusNumber;
    }

    public void setMeteredBusNumber(int meteredBusNumber) {
        this.meteredBusNumber = meteredBusNumber;
    }

    public int getMeteredAreaNumber() {
        return meteredAreaNumber;
    }

    public void setMeteredAreaNumber(int meteredAreaNumber) {
        this.meteredAreaNumber = meteredAreaNumber;
    }

    public int getNonMeteredBusNumber() {
        return nonMeteredBusNumber;
    }

    public void setNonMeteredBusNumber(int nonMeteredBusNumber) {
        this.nonMeteredBusNumber = nonMeteredBusNumber;
    }

    public int getNonMeteredAreaNumber() {
        return nonMeteredAreaNumber;
    }

    public void setNonMeteredAreaNumber(int nonMeteredAreaNumber) {
        this.nonMeteredAreaNumber = nonMeteredAreaNumber;
    }

    public int getCircuitNumber() {
        return circuitNumber;
    }

    public void setCircuitNumber(int circuitNumber) {
        this.circuitNumber = circuitNumber;
    }
}
