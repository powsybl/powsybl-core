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

    private static double LOW_VOLTAGE_FACTOR = 0.8;
    private static double HIGH_VOLTAGE_FACTOR = 1.2;
    private static double LOW_NOMINAL_VOLTAGE = 110;

    private UcteValidation() {
    }

    public static void run(Network network) {
        checkNodesReferenceVoltage(network);
        checkGenerators(network);
        checkTransformers(network);
    }

    // Data: node voltage
    private static void checkNodesReferenceVoltage(Network network) {
        double nominalVoltage;
        double voltage;
        for (Bus bus : network.getBusView().getBuses()) {
            nominalVoltage = bus.getVoltageLevel().getNominalV();
            voltage = bus.getV();
            if (voltage < LOW_VOLTAGE_FACTOR * nominalVoltage || voltage > HIGH_VOLTAGE_FACTOR * nominalVoltage) {
                LOGGER.warn("{} - {} ({})", bus.getId(), "Wrong value for reference voltage", voltage + " kV");
            }
        }
    }

    private static void checkGenerators(Network network) {
        for (Generator generator : network.getGenerators()) {
            if (generator.isVoltageRegulatorOn()) {
                double targetVoltage = generator.getTargetV();
                double nominalVoltage = generator.getRegulatingTerminal().getVoltageLevel().getNominalV();
                if (nominalVoltage > LOW_NOMINAL_VOLTAGE && (targetVoltage < LOW_VOLTAGE_FACTOR * nominalVoltage
                        || targetVoltage > HIGH_VOLTAGE_FACTOR * nominalVoltage)) {
                    LOGGER.warn("{} - {} ({})", generator.getId(), "Wrong value for reference voltage", targetVoltage + " kV");
                }
            }
        }
    }

    // Data: transformer voltage
    private static void checkTransformers(Network network) {
        for (TwoWindingsTransformer twt : network.getTwoWindingsTransformers()) {
            RatioTapChanger rtc = twt.getRatioTapChanger();
            if (rtc != null && rtc.isRegulating()) {
                double targetVoltage = rtc.getTargetV();
                double nominalVoltage = rtc.getRegulationTerminal().getVoltageLevel().getNominalV();
                if (nominalVoltage > LOW_NOMINAL_VOLTAGE && (targetVoltage < 0.8 * nominalVoltage || targetVoltage > 1.2 * nominalVoltage)) {
                    LOGGER.error("{} - {} ({})", twt.getId(), "Wrong value for reference voltage", nominalVoltage + "kV");
                }
            }
        }
    }

}
