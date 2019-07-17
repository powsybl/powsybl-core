/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.network;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.*;

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
    private static final Supplier<Map<String, Double>> SUPPLIER =
            Suppliers.memoize(UcteVoltageLevelCode::initializeBoundaries);

    UcteVoltageLevelCode(int voltageLevel) {
        this.voltageLevel = voltageLevel;
    }

    public static UcteVoltageLevelCode voltageLevelCodeFromChar(char code) {
        if (code < '0' || code > '9') {
            throw new IllegalArgumentException("'" + code + "' doesn't refer to a voltage level");
        }
        return UcteVoltageLevelCode.values()[code - '0'];
    }

    public static UcteVoltageLevelCode voltageLevelCodeFromIidmVoltage(double nominalV) {
        Map<String, Double> voltageLevelCodeBoundary = UcteVoltageLevelCode.SUPPLIER.get();
        if (nominalV >= voltageLevelCodeBoundary.get("VL27.min") && nominalV <= voltageLevelCodeBoundary.get("VL27.max")) {
            return UcteVoltageLevelCode.VL_27;
        } else if (nominalV > voltageLevelCodeBoundary.get("VL70.min") && nominalV <= voltageLevelCodeBoundary.get("VL70.max")) {
            return UcteVoltageLevelCode.VL_70;
        } else if (nominalV > voltageLevelCodeBoundary.get("VL110.min") && nominalV <= voltageLevelCodeBoundary.get("VL110.max")) {
            return UcteVoltageLevelCode.VL_110;
        } else if (nominalV > voltageLevelCodeBoundary.get("VL120.min") && nominalV <= voltageLevelCodeBoundary.get("VL120.max")) {
            return UcteVoltageLevelCode.VL_120;
        } else if (nominalV > voltageLevelCodeBoundary.get("VL150.min") && nominalV <= voltageLevelCodeBoundary.get("VL150.max")) {
            return UcteVoltageLevelCode.VL_150;
        } else if (nominalV > voltageLevelCodeBoundary.get("VL220.min") && nominalV <= voltageLevelCodeBoundary.get("VL220.max")) {
            return UcteVoltageLevelCode.VL_220;
        } else if (nominalV > voltageLevelCodeBoundary.get("VL330.min") && nominalV <= voltageLevelCodeBoundary.get("VL330.max")) {
            return UcteVoltageLevelCode.VL_330;
        } else if (nominalV > voltageLevelCodeBoundary.get("VL380.min") && nominalV <= voltageLevelCodeBoundary.get("VL380.max")) {
            return UcteVoltageLevelCode.VL_380;
        } else if (nominalV > voltageLevelCodeBoundary.get("VL500.min") && nominalV <= voltageLevelCodeBoundary.get("VL500.max")) {
            return UcteVoltageLevelCode.VL_500;
        } else if (nominalV > voltageLevelCodeBoundary.get("VL750.min") && nominalV <= voltageLevelCodeBoundary.get("VL750.max")) {
            return UcteVoltageLevelCode.VL_750;
        } else {
            throw new IllegalArgumentException("'" + nominalV + "' doesn't refer to a voltage level");
        }
    }

    private static Map<String, Double> initializeBoundaries() {
        Properties prop = new Properties();
        try {
            ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            InputStream inputStream = Objects.requireNonNull(UcteVoltageLevelCode.class.getClassLoader()
                    .getResource("voltageConversion.properties")).openStream();
            prop.load(inputStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        HashMap<String, Double> boundaries = new HashMap<>();
        boundaries.put("VL27.min", Double.valueOf(prop.getProperty("VL27.min")));
        boundaries.put("VL27.max", Double.valueOf(prop.getProperty("VL27.max")));
        boundaries.put("VL70.min", Double.valueOf(prop.getProperty("VL70.min")));
        boundaries.put("VL70.max", Double.valueOf(prop.getProperty("VL70.max")));
        boundaries.put("VL110.min", Double.valueOf(prop.getProperty("VL110.min")));
        boundaries.put("VL110.max", Double.valueOf(prop.getProperty("VL110.max")));
        boundaries.put("VL120.min", Double.valueOf(prop.getProperty("VL120.min")));
        boundaries.put("VL120.max", Double.valueOf(prop.getProperty("VL120.max")));
        boundaries.put("VL150.min", Double.valueOf(prop.getProperty("VL150.min")));
        boundaries.put("VL150.max", Double.valueOf(prop.getProperty("VL150.max")));
        boundaries.put("VL220.min", Double.valueOf(prop.getProperty("VL220.min")));
        boundaries.put("VL220.max", Double.valueOf(prop.getProperty("VL220.max")));
        boundaries.put("VL330.min", Double.valueOf(prop.getProperty("VL330.min")));
        boundaries.put("VL330.max", Double.valueOf(prop.getProperty("VL330.max")));
        boundaries.put("VL380.min", Double.valueOf(prop.getProperty("VL380.min")));
        boundaries.put("VL380.max", Double.valueOf(prop.getProperty("VL380.max")));
        boundaries.put("VL500.min", Double.valueOf(prop.getProperty("VL500.min")));
        boundaries.put("VL500.max", Double.valueOf(prop.getProperty("VL500.max")));
        boundaries.put("VL750.min", Double.valueOf(prop.getProperty("VL750.min")));
        boundaries.put("VL750.max", Double.valueOf(prop.getProperty("VL750.max")));
        return boundaries;
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
