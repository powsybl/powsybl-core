/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.network;

import java.util.Arrays;
import java.util.Comparator;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
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

    public static UcteVoltageLevelCode voltageLevelCodeFromChar(char code) {
        if (code < '0' || code > '9') {
            throw new IllegalArgumentException("'" + code + "' doesn't refer to a voltage level");
        }
        return UcteVoltageLevelCode.values()[code - '0'];
    }

    public static char voltageLevelCodeFromVoltage(double voltage) {
        if (voltage < UcteVoltageLevelCode.VL_27.getVoltageLevel()) {
            return '7';
        }
        if (voltage > UcteVoltageLevelCode.VL_750.getVoltageLevel()) {
            return '0';
        }

        return Arrays.stream(UcteVoltageLevelCode.values())
                .min(Comparator.comparingDouble(code ->
                        Math.abs(voltage - code.getVoltageLevel())))
                .map(code -> (char) ('0' + code.ordinal()))
                .orElseThrow();
    }

    public static boolean isVoltageLevelCode(char character) {
        return character >= '0' && character <= '9';
    }

    /**
     * Gets the voltage level (kV).
     * @return the voltage level (kV)
     */
    public int getVoltageLevel() {
        return voltageLevel;
    }

}
