/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.*;
import org.joda.time.DateTime;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ValidationUtil {

    private ValidationUtil() {
    }

    static void checkTargetP(Validable validable, float targetP) {
        if (Float.isNaN(targetP)) {
            throw new ValidationException(validable, "invalid value (" + targetP
                    + ") for target P");
        }
    }

    static void checkActiveLimits(Validable validable, float minP, float maxP) {
        if (minP > maxP) {
            throw new ValidationException(validable,
                    "invalid active limits [" + minP + ", " + maxP + "]");
        }
    }

    static void checkVoltageControl(Validable validable, Boolean voltageRegulatorOn, float targetV, float targetQ) {
        if (voltageRegulatorOn == null) {
            throw new ValidationException(validable, "voltage regulator status is not set");
        }
        if (voltageRegulatorOn) {
            if (Float.isNaN(targetV) || targetV <= 0) {
                throw new ValidationException(validable,
                        "invalid value (" + targetV + ") for target V (voltage regulator is on)");
            }
        } else {
            if (Float.isNaN(targetQ)) {
                throw new ValidationException(validable, "invalid value (" + targetQ
                        + ") for target Q (voltage regulator is off)");
            }
        }
    }

    static void checkRatedS(Validable validable, float ratedS) {
        if (!Float.isNaN(ratedS) && ratedS <= 0) {
            throw new ValidationException(validable, "Invalid value of rated S " + ratedS);
        }
    }

    static void checkEnergySource(Validable validable, EnergySource energySource) {
        if (energySource == null) {
            throw new ValidationException(validable, "energy source is not set");
        }
    }

    static void checkMinP(Validable validable, float minP) {
        if (Float.isNaN(minP)) {
            throw new ValidationException(validable, "invalid value (" + minP
                    + ") for minimum P");
        }
    }

    static void checkMaxP(Validable validable, float maxP) {
        if (Float.isNaN(maxP)) {
            throw new ValidationException(validable, "invalid value (" + maxP
                    + ") for maximum P");
        }
    }

    static void checkRegulatingTerminal(Validable validable, TerminalExt regulatingTerminal, NetworkImpl network) {
        if (regulatingTerminal != null && regulatingTerminal.getVoltageLevel().getNetwork() != network) {
            throw new ValidationException(validable, "regulating terminal is not part of the network");
        }
    }


    static void checkLoadType(Validable validable, LoadType loadType) {
        if (loadType == null) {
            throw new ValidationException(validable, "load type is null");
        }
    }

    static void checkP0(Validable validable, float p0) {
        if (Float.isNaN(p0)) {
            throw new ValidationException(validable, "p0 is invalid");
        }
    }

    static void checkQ0(Validable validable, float q0) {
        if (Float.isNaN(q0)) {
            throw new ValidationException(validable, "q0 is invalid");
        }
    }

    static void checkR(Validable validable, float r) {
        if (Float.isNaN(r)) {
            throw new ValidationException(validable, "r is invalid");
        }
    }

    static void checkX(Validable validable, float x) {
        if (Float.isNaN(x)) {
            throw new ValidationException(validable, "x is invalid");
        }
    }

    static void checkG1(Validable validable, float g1) {
        if (Float.isNaN(g1)) {
            throw new ValidationException(validable, "g1 is invalid");
        }
    }

    static void checkG2(Validable validable, float g2) {
        if (Float.isNaN(g2)) {
            throw new ValidationException(validable, "g2 is invalid");
        }
    }

    static void checkB1(Validable validable, float b1) {
        if (Float.isNaN(b1)) {
            throw new ValidationException(validable, "b1 is invalid");
        }
    }

    static void checkB2(Validable validable, float b2) {
        if (Float.isNaN(b2)) {
            throw new ValidationException(validable, "b2 is invalid");
        }
    }

    static void checkG(Validable validable, float g) {
        if (Float.isNaN(g)) {
            throw new ValidationException(validable, "g is invalid");
        }
    }

    static void checkB(Validable validable, float b) {
        if (Float.isNaN(b)) {
            throw new ValidationException(validable, "b is invalid");
        }
    }

    static void checkCountry(Validable validable, Country country) {
        if (country == null) {
            throw new ValidationException(validable, "country is invalid");
        }
    }

    static void checkNominalV(Validable validable, float nominalV) {
        if (Float.isNaN(nominalV) || nominalV <= 0) {
            throw new ValidationException(validable, "nominal voltage is invalid");
        }
    }

    static void checkLowVoltageLimit(Validable validable, float lowVoltageLimit) {
        if (lowVoltageLimit <= 0) {
            throw new ValidationException(validable, "low voltage limit is <= 0");
        }
    }

    static void checkHighVoltageLimit(Validable validable, float highVoltageLimit) {
        if (highVoltageLimit <= 0) {
            throw new ValidationException(validable, "high voltage limit is <= 0");
        }
    }

    static void checkTopologyKind(Validable validable, TopologyKind topologyKind) {
        if (topologyKind == null) {
            throw new ValidationException(validable, "topology kind is invalid");
        }
    }

    static void checkDate(Validable validable, DateTime date) {
        if (date == null) {
            throw new ValidationException(validable, "date is invalid");
        }
    }

    static void checkHorizon(Validable validable, Horizon horizon) {
        if (horizon == null) {
            throw new ValidationException(validable, "horizon is invalid");
        }
    }

    static void checkForecastDistance(Validable validable, int forecastDistance) {
        if (forecastDistance < 0) {
            throw new ValidationException(validable, "forecast distance < 0");
        }
    }

    static void checkbPerSection(Validable validable, float bPerSection) {
        if (Float.isNaN(bPerSection)) {
            throw new ValidationException(validable, "susceptance per section is invalid");
        }
        if (bPerSection == 0) {
            throw new ValidationException(validable, "susceptance per section is equal to zero");
        }
    }

    static void checkSections(Validable validable, int currentSectionCount, int maximumSectionCount) {
        if (currentSectionCount < 0) {
            throw new ValidationException(validable,
                    "the current number of section (" + currentSectionCount
                            + ") should be greater than or equal to 0");
        }
        if (maximumSectionCount <= 0) {
            throw new ValidationException(validable,
                    "the maximum number of section (" + maximumSectionCount
                            + ")should be greater than 0");
        }
        if (currentSectionCount > maximumSectionCount) {
            throw new ValidationException(validable,
                    "the current number (" + currentSectionCount
                            + ") of section should be lesser than the maximum number of section ("
                            + maximumSectionCount + ")");
        }
    }

    static void checkRatedU1(Validable validable, float ratedU1) {
        if (Float.isNaN(ratedU1)) {
            throw new ValidationException(validable, "rated U1 is invalid");
        }
    }

    static void checkRatedU2(Validable validable, float ratedU2) {
        if (Float.isNaN(ratedU2)) {
            throw new ValidationException(validable, "rated U2 is invalid");
        }
    }

}
