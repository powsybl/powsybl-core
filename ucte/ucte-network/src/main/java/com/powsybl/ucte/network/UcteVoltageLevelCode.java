/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.Properties;

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

    public static UcteVoltageLevelCode voltageLevelCodeFromChar(char code) {
        if (code == '0') {
            return UcteVoltageLevelCode.VL_750;
        } else if (code == '1') {
            return UcteVoltageLevelCode.VL_380;
        } else if (code == '2') {
            return UcteVoltageLevelCode.VL_220;
        } else if (code == '3') {
            return UcteVoltageLevelCode.VL_150;
        } else if (code == '4') {
            return UcteVoltageLevelCode.VL_120;
        } else if (code == '5') {
            return UcteVoltageLevelCode.VL_110;
        } else if (code == '6') {
            return UcteVoltageLevelCode.VL_70;
        } else if (code == '7') {
            return UcteVoltageLevelCode.VL_27;
        } else if (code == '8') {
            return UcteVoltageLevelCode.VL_330;
        } else if (code == '9') {
            return UcteVoltageLevelCode.VL_500;
        } else {
            throw new IllegalArgumentException("'" + code + "' doesn't refer to a voltage level");
        }
    }

    public static UcteVoltageLevelCode voltageLevelCodeFromIidmVoltage(double nominalV) {
        Properties prop = new Properties();
        try {
            ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            InputStream inputStream = Objects.requireNonNull(classLoader.getResource("voltageConversion.properties")).openStream();
            prop.load(inputStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        if (nominalV >= Double.valueOf(prop.getProperty("VL27.min")) && nominalV <= Double.valueOf(prop.getProperty("VL27.max"))) {
            return UcteVoltageLevelCode.VL_27;
        } else if (nominalV > Double.valueOf(prop.getProperty("VL70.min")) && nominalV <= Double.valueOf(prop.getProperty("VL70.max"))) {
            return UcteVoltageLevelCode.VL_70;
        } else if (nominalV > Double.valueOf(prop.getProperty("VL110.min")) && nominalV <= Double.valueOf(prop.getProperty("VL110.max"))) {
            return UcteVoltageLevelCode.VL_110;
        } else if (nominalV > Double.valueOf(prop.getProperty("VL120.min")) && nominalV <= Double.valueOf(prop.getProperty("VL120.max"))) {
            return UcteVoltageLevelCode.VL_120;
        } else if (nominalV > Double.valueOf(prop.getProperty("VL150.min")) && nominalV <= Double.valueOf(prop.getProperty("VL150.max"))) {
            return UcteVoltageLevelCode.VL_150;
        } else if (nominalV > Double.valueOf(prop.getProperty("VL220.min")) && nominalV <= Double.valueOf(prop.getProperty("VL220.max"))) {
            return UcteVoltageLevelCode.VL_220;
        } else if (nominalV > Double.valueOf(prop.getProperty("VL330.min")) && nominalV <= Double.valueOf(prop.getProperty("VL330.max"))) {
            return UcteVoltageLevelCode.VL_330;
        } else if (nominalV > Double.valueOf(prop.getProperty("VL380.min")) && nominalV <= Double.valueOf(prop.getProperty("VL380.max"))) {
            return UcteVoltageLevelCode.VL_380;
        } else if (nominalV > Double.valueOf(prop.getProperty("VL500.min")) && nominalV <= Double.valueOf(prop.getProperty("VL500.max"))) {
            return UcteVoltageLevelCode.VL_500;
        } else if (nominalV > Double.valueOf(prop.getProperty("VL750.min")) && nominalV <= Double.valueOf(prop.getProperty("VL750.max"))) {
            return UcteVoltageLevelCode.VL_750;
        } else {
            throw new IllegalArgumentException("'" + nominalV + "' doesn't refer to a voltage level");
        }
    }

    public static boolean isVoltageLevel(char character) {
        return (int) character >= '0' && (int) character <= '9';
    }

    /**
     * Gets the voltage level (kV).
     * @return the voltage level (kV)
     */
    public int getVoltageLevel() {
        return voltageLevel;
    }

}
