/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.network;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public enum UcteVoltageLevelCode {
    /**
     * 0    750kV.
     */
    VL_750(750),

    /**
     * 1    380kV.
     */
    VL_380(380),

    /**
     * 2    220kV.
     */
    VL_220(220),

    /**
     * 3    150kV.
     */
    VL_150(150),

    /**
     * 4    120kV.
     */
    VL_120(120),

    /**
     * 5    110kV.
     */
    VL_110(110),

    /**
     * 6    70kV.
     */
    VL_70(70),

    /**
     * 7    27kV.
     */
    VL_27(27),

    /**
     * 8    330kV.
     */
    VL_330(330),

    /**
     * 9    500kV.
     */
    VL_500(500);

    private final int voltageLevel;

    UcteVoltageLevelCode(int voltageLevel) {
        this.voltageLevel = voltageLevel;
    }

    /**
     * Gets the voltage level (kV).
     * @return the voltage level (kV)
     */
    public int getVoltageLevel() {
        return voltageLevel;
    }

}
