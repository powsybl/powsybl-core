/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import org.joda.time.DateTime;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class ValidationUtil {

    private ValidationUtil() {
    }

    private static ValidationException createInvalidValueException(Validable validable, double value, String valueName) {
        return createInvalidValueException(validable, value, valueName, null);
    }

    private static ValidationException createInvalidValueException(Validable validable, double value, String valueName, String reason) {
        String r = reason == null ? "" : " (" + reason + ")";
        return new ValidationException(validable, "invalid value (" + value + ") for " + valueName + r);
    }

    static void checkActivePowerSetpoint(Validable validable, double activePowerSetpoint) {
        if (Double.isNaN(activePowerSetpoint)) {
            throw createInvalidValueException(validable, activePowerSetpoint, "active power setpoint");
        }
    }

    static void checkActiveLimits(Validable validable, double minP, double maxP) {
        if (minP > maxP) {
            throw new ValidationException(validable,
                    "invalid active limits [" + minP + ", " + maxP + "]");
        }
    }

    static void checkVoltageControl(Validable validable, Boolean voltageRegulatorOn, double voltageSetpoint, double reactivePowerSetpoint) {
        if (voltageRegulatorOn == null) {
            throw new ValidationException(validable, "voltage regulator status is not set");
        }
        if (voltageRegulatorOn) {
            if (Double.isNaN(voltageSetpoint) || voltageSetpoint <= 0) {
                throw createInvalidValueException(validable, voltageSetpoint, "voltage setpoint", "voltage regulator is on");
            }
        } else {
            if (Double.isNaN(reactivePowerSetpoint)) {
                throw createInvalidValueException(validable, reactivePowerSetpoint, "reactive power setpoint", "voltage regulator is off");
            }
        }
    }

    static void checkRatedS(Validable validable, double ratedS) {
        if (!Double.isNaN(ratedS) && ratedS <= 0) {
            throw new ValidationException(validable, "Invalid value of rated S " + ratedS);
        }
    }

    static void checkEnergySource(Validable validable, EnergySource energySource) {
        if (energySource == null) {
            throw new ValidationException(validable, "energy source is not set");
        }
    }

    static void checkMinP(Validable validable, double minP) {
        if (Double.isNaN(minP)) {
            throw createInvalidValueException(validable, minP, "minimum P");
        }
    }

    static void checkMaxP(Validable validable, double maxP) {
        if (Double.isNaN(maxP)) {
            throw createInvalidValueException(validable, maxP, "maximum P");
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

    static void checkP0(Validable validable, double p0) {
        if (Double.isNaN(p0)) {
            throw new ValidationException(validable, "p0 is invalid");
        }
    }

    static void checkQ0(Validable validable, double q0) {
        if (Double.isNaN(q0)) {
            throw new ValidationException(validable, "q0 is invalid");
        }
    }

    static void checkR(Validable validable, double r) {
        if (Double.isNaN(r)) {
            throw new ValidationException(validable, "r is invalid");
        }
    }

    static void checkX(Validable validable, double x) {
        if (Double.isNaN(x)) {
            throw new ValidationException(validable, "x is invalid");
        }
    }

    static void checkG1(Validable validable, double g1) {
        if (Double.isNaN(g1)) {
            throw new ValidationException(validable, "g1 is invalid");
        }
    }

    static void checkG2(Validable validable, double g2) {
        if (Double.isNaN(g2)) {
            throw new ValidationException(validable, "g2 is invalid");
        }
    }

    static void checkB1(Validable validable, double b1) {
        if (Double.isNaN(b1)) {
            throw new ValidationException(validable, "b1 is invalid");
        }
    }

    static void checkB2(Validable validable, double b2) {
        if (Double.isNaN(b2)) {
            throw new ValidationException(validable, "b2 is invalid");
        }
    }

    static void checkG(Validable validable, double g) {
        if (Double.isNaN(g)) {
            throw new ValidationException(validable, "g is invalid");
        }
    }

    static void checkB(Validable validable, double b) {
        if (Double.isNaN(b)) {
            throw new ValidationException(validable, "b is invalid");
        }
    }

    static void checkCountry(Validable validable, Country country) {
        if (country == null) {
            throw new ValidationException(validable, "country is invalid");
        }
    }

    static void checkNominalV(Validable validable, double nominalV) {
        if (Double.isNaN(nominalV) || nominalV <= 0) {
            throw new ValidationException(validable, "nominal voltage is invalid");
        }
    }

    static void checkVoltageLimits(Validable validable, double lowVoltageLimit, double highVoltageLimit) {
        if (lowVoltageLimit < 0) {
            throw new ValidationException(validable, "low voltage limit is < 0");
        }
        if (highVoltageLimit < 0) {
            throw new ValidationException(validable, "high voltage limit is < 0");
        }
        if (lowVoltageLimit > highVoltageLimit) {
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

    static void checkbPerSection(Validable validable, double bPerSection) {
        if (Double.isNaN(bPerSection)) {
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

    static void checkRatedU1(Validable validable, double ratedU1) {
        if (Double.isNaN(ratedU1)) {
            throw new ValidationException(validable, "rated U1 is invalid");
        }
    }

    static void checkRatedU2(Validable validable, double ratedU2) {
        if (Double.isNaN(ratedU2)) {
            throw new ValidationException(validable, "rated U2 is invalid");
        }
    }

    static void checkSvcRegulator(Validable validable, double voltageSetpoint, double reactivePowerSetpoint, StaticVarCompensator.RegulationMode regulationMode) {
        if (regulationMode == null) {
            throw new ValidationException(validable, "Regulation mode is invalid");
        }
        switch (regulationMode) {
            case VOLTAGE:
                if (Double.isNaN(voltageSetpoint)) {
                    throw createInvalidValueException(validable, voltageSetpoint, "voltage setpoint");
                }
                break;

            case REACTIVE_POWER:
                if (Double.isNaN(reactivePowerSetpoint)) {
                    throw createInvalidValueException(validable, reactivePowerSetpoint, "reactive power setpoint");
                }
                break;

            case OFF:
                // nothing to check
                break;

            default:
                throw new AssertionError();
        }

    }

    static void checkBmin(Validable validable, double bMin) {
        if (Double.isNaN(bMin)) {
            throw new ValidationException(validable, "bmin is invalid");
        }
    }

    static void checkBmax(Validable validable, double bMax) {
        if (Double.isNaN(bMax)) {
            throw new ValidationException(validable, "bmax is invalid");
        }
    }

    static void checkRatioTapChangerRegulation(Validable validable, boolean loadTapChangingCapabilities, boolean regulating,
                                               Terminal regulationTerminal, double targetV, Network network) {
        if (loadTapChangingCapabilities && regulating) {
            if (Double.isNaN(targetV)) {
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

    static void checkPhaseTapChangerRegulation(Validable validable, PhaseTapChanger.RegulationMode regulationMode,
                                               double regulationValue, boolean regulating, Terminal regulationTerminal,
                                               Network network) {
        if (regulationMode == null) {
            throw new ValidationException(validable, "phase regulation mode is not set");
        }
        if (regulationMode != PhaseTapChanger.RegulationMode.FIXED_TAP && Double.isNaN(regulationValue)) {
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

    static void checkPowerFactor(Validable validable, double powerFactor) {
        if (Double.isNaN(powerFactor)) {
            throw new ValidationException(validable, "power factor is invalid");
        }
    }

    static void checkConnected(Validable validable, Boolean connected) {
        if (connected == null) {
            throw new ValidationException(validable, "connection status is invalid");
        }
    }

    static void checkPermanentLimit(Validable validable, double permanentLimit) {
        if (permanentLimit <= 0) {
            throw new ValidationException(validable, "permanent limit must be > 0");
        }
    }

    static void checkLossFactor(Validable validable, float lossFactor) {
        if (Double.isNaN(lossFactor)) {
            throw new ValidationException(validable, "loss factor is invalid");
        } else if (lossFactor < 0) {
            throw new ValidationException(validable, "loss factor must be >= 0");
        }
    }
}
