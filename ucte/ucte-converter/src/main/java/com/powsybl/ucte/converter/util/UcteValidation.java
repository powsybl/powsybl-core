/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.converter.util;

import com.powsybl.iidm.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Anne Tilloy {@literal <anne.tilloy at rte-france.com>}
 */
public final class UcteValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(UcteValidation.class);

    private static final double LOW_VOLTAGE_FACTOR = 0.8;
    private static final double HIGH_VOLTAGE_FACTOR = 1.2;
    private static final double LOW_NOMINAL_VOLTAGE = 110;

    private static String errorFormat = "{} - {} ({})";
    private static String errorMessage = "Wrong value for reference voltage";

    private UcteValidation() {
    }

    public static void run(Network network) {
        checkGenerators(network);
        checkTransformers(network);
    }

    // Data: generator targetV
    private static void checkGenerators(Network network) {
        for (Generator generator : network.getGenerators()) {
            if (generator.isVoltageRegulatorOn()) {
                double targetVoltage = generator.getTargetV();
                double nominalVoltage = generator.getRegulatingTerminal().getVoltageLevel().getNominalV();
                if (nominalVoltage > LOW_NOMINAL_VOLTAGE && (targetVoltage < LOW_VOLTAGE_FACTOR * nominalVoltage
                        || targetVoltage > HIGH_VOLTAGE_FACTOR * nominalVoltage)) {
                    LOGGER.warn(errorFormat, generator.getId(), errorMessage, targetVoltage + " kV");
                }
            }
        }
    }

    // Data: transformer targetV
    private static void checkTransformers(Network network) {
        for (TwoWindingsTransformer twt : network.getTwoWindingsTransformers()) {
            RatioTapChanger rtc = twt.getRatioTapChanger();
            if (rtc != null && rtc.isRegulating()) {
                double targetVoltage = rtc.getTargetV();
                double nominalVoltage = rtc.getRegulationTerminal().getVoltageLevel().getNominalV();
                if (nominalVoltage > LOW_NOMINAL_VOLTAGE && (targetVoltage < 0.8 * nominalVoltage || targetVoltage > 1.2 * nominalVoltage)) {
                    LOGGER.error(errorFormat, twt.getId(), errorMessage, nominalVoltage + "kV");
                }
            }
        }
    }

}
