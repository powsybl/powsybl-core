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

    @Deprecated
    static void checkTargetP(Validable validable, float targetP) {
        checkActivePowerSetPoint(validable, targetP);
    }

    static void checkActivePowerSetPoint(Validable validable, float activePowerSetPoint) {
        if (Float.isNaN(activePowerSetPoint)) {
            throw new ValidationException(validable, "invalid value (" + activePowerSetPoint
                    + ") for active power set point");
        }
    }

    static void checkActiveLimits(Validable validable, float minP, float maxP) {
        if (minP > maxP) {
            throw new ValidationException(validable,
                    "invalid active limits [" + minP + ", " + maxP + "]");
        }
    }

    static void checkVoltageControl(Validable validable, Boolean voltageRegulatorOn, float voltageSetPoint, float reactivePowerSetPoint) {
        if (voltageRegulatorOn == null) {
            throw new ValidationException(validable, "voltage regulator status is not set");
        }
        if (voltageRegulatorOn) {
            if (Float.isNaN(voltageSetPoint) || voltageSetPoint <= 0) {
                throw new ValidationException(validable,
                        "invalid value (" + voltageSetPoint + ") for voltage set point (voltage regulator is on)");
            }
        } else {
            if (Float.isNaN(reactivePowerSetPoint)) {
                throw new ValidationException(validable, "invalid value (" + reactivePowerSetPoint
                        + ") for reactive power set point (voltage regulator is off)");
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

    static void checkVoltageLimits(Validable validable, float lowVoltageLimit, float highVoltageLimit) {
        if (lowVoltageLimit < 0) {
            throw new ValidationException(validable, "low voltage limit is < 0");
        }
        if (highVoltageLimit < 0) {
            throw new ValidationException(validable, "high voltage limit is < 0");
        }
        if (lowVoltageLimit >= highVoltageLimit) {
            throw new ValidationException(validable, "Inconsistent voltage limit range ["
                    + lowVoltageLimit + ", " + highVoltageLimit + "]");
        }
    }

    static void checkTopologyKind(Validable validable, TopologyKind topologyKind) {
        if (topologyKind == null) {
            throw new ValidationException(validable, "topology kind is invalid");
        }
    }

    static void checkCaseDate(Validable validable, DateTime caseDate) {
        if (caseDate == null) {
            throw new ValidationException(validable, "case date is invalid");
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

    static void checkSvcRegulator(Validable validable, float voltageSetPoint, float reactivePowerSetPoint, StaticVarCompensator.RegulationMode regulationMode) {
        if (regulationMode == null) {
            throw new ValidationException(validable, "Regulation mode is invalid");
        }
        switch (regulationMode) {
            case VOLTAGE:
                if (Float.isNaN(voltageSetPoint)) {
                    throw new ValidationException(validable, "invalid value (" + voltageSetPoint
                            + ") for voltage set point");
                }
                break;

            case REACTIVE_POWER:
                if (Float.isNaN(reactivePowerSetPoint)) {
                    throw new ValidationException(validable, "invalid value (" + reactivePowerSetPoint
                            + ") for reactive power set point");
                }
                break;

            case OFF:
                // nothing to check
                break;

            default:
                throw new AssertionError();
        }

    }

    static void checkBmin(Validable validable, float bMin) {
        if (Float.isNaN(bMin)) {
            throw new ValidationException(validable, "bmin is invalid");
        }
    }

    static void checkBmax(Validable validable, float bMax) {
        if (Float.isNaN(bMax)) {
            throw new ValidationException(validable, "bmax is invalid");
        }
    }

    static void checkRatioTapChangerRegulation(Validable validable, boolean loadTapChangingCapabilities, boolean regulating,
                                               Terminal regulationTerminal, float targetV, Network network) {
        if (loadTapChangingCapabilities) {
            if (regulating) {
                if (Float.isNaN(targetV)) {
                    throw new ValidationException(validable,
                            "a target voltage has to be set for a regulating ratio tap changer");
                }
                if (targetV <= 0) {
                    throw new ValidationException(validable, "bad target voltage " + targetV);
                }
                if (regulationTerminal == null) {
                    throw new ValidationException(validable,
                            "a regulation terminal has to be set for a regulating ratio tap changer");
                }
                if (regulationTerminal.getVoltageLevel().getSubstation().getNetwork() != network) {
                    throw new ValidationException(validable, "regulation terminal is not part of the network");
                }
            }
        }
    }

    static void checkPhaseTapChangerRegulation(Validable validable, PhaseTapChanger.RegulationMode regulationMode,
                                               float regulationValue, boolean regulating, Terminal regulationTerminal,
                                               Network network) {
        if (regulationMode == null) {
            throw new ValidationException(validable, "phase regulation mode is not set");
        }
        if (regulationMode != PhaseTapChanger.RegulationMode.FIXED_TAP && Float.isNaN(regulationValue)) {
            throw new ValidationException(validable, "phase regulation is on and threshold/setpoint value is not set");
        }
        if (regulationMode != PhaseTapChanger.RegulationMode.FIXED_TAP && regulationTerminal == null) {
            throw new ValidationException(validable, "phase regulation is on and regulated terminal is not set");
        }
        if (regulationTerminal != null && regulationTerminal.getVoltageLevel().getSubstation().getNetwork() != network) {
            throw new ValidationException(validable, "phase regulation terminal is not part of the network");
        }
        if (regulationMode == PhaseTapChanger.RegulationMode.FIXED_TAP && regulating) {
            throw new ValidationException(validable, "phase regulation cannot be on if mode is FIXED");
        }
    }

    static void checkConvertersMode(Validable validable, HvdcLine.ConvertersMode converterMode) {
        if (converterMode == null) {
            throw new ValidationException(validable, "converter mode is invalid");
        }
    }

    static void checkPowerFactor(Validable validable, float powerFactor) {
        if (Float.isNaN(powerFactor)) {
            throw new ValidationException(validable, "power factor is invalid");
        }
    }

    static void checkConnected(Validable validable, Boolean connected) {
        if (connected == null) {
            throw new ValidationException(validable, "connection status is invalid");
        }
    }
}
