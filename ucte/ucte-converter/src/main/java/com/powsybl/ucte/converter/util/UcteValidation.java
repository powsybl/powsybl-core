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
            if (voltage < 0.8 * nominalVoltage || voltage > 1.2 * nominalVoltage) {
                LOGGER.warn("Wrong value for reference voltage");
            }
        }
    }

    private static void checkGenerators(Network network) {
        for (Generator generator : network.getGenerators()) {
            if (generator.isVoltageRegulatorOn()) {
                double targetVoltage = generator.getTargetV();
                double nominalVoltage = generator.getRegulatingTerminal().getVoltageLevel().getNominalV();
                if (nominalVoltage > 110 && (targetVoltage < 0.8 * nominalVoltage || targetVoltage > 1.2 * nominalVoltage)) {
                    LOGGER.warn("Wrong value for reference voltage");
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
                if (nominalVoltage > 110 && (targetVoltage < 0.8 * nominalVoltage || targetVoltage > 1.2 * nominalVoltage)) {
                    LOGGER.warn("Wrong value for reference voltage");
                }
            }
        }
    }

}
