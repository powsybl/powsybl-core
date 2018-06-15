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
    LV_750(750),

    /**
     * 1    380kV.
     */
    LV_380(380),

    /**
     * 2    220kV.
     */
    LV_220(220),

    /**
     * 3    150kV.
     */
    LV_150(150),

    /**
     * 4    120kV.
     */
    LV_120(120),

    /**
     * 5    110kV.
     */
    LV_110(110),

    /**
     * 6    70kV.
     */
    LV_70(70),

    /**
     * 7    27kV.
     */
    LV_27(27),

    /**
     * 8    330kV.
     */
    LV_330(330),

    /**
     * 9    500kV.
     */
    LV_500(500);

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
