/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import java.util.Set;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.StaticVarCompensator.RegulationMode;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class ValidationUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationUtil.class);
    private static final String REACTIVE_POWER_SETPOINT = "reactive power setpoint";
    private static final String VOLTAGE_SETPOINT = "voltage setpoint";
    private static final String REGULATING_TERMINAL_NOT_DEFINED = "regulating terminal is not defined or is not part of the network";
    private static final String VOLTAGE_REGULATOR_OFF = "voltage regulator is off";

    private ValidationUtil() {
    }

    private static ValidationException createInvalidValueException(Validable validable, double value, String valueName) {
        return createInvalidValueException(validable, value, valueName, null);
    }

    private static ValidationException createInvalidValueException(Validable validable, double value, String valueName, String reason) {
        String r = reason == null ? "" : " (" + reason + ")";
        return new ValidationException(validable, "invalid value (" + value + ") for " + valueName + r);
    }

    public static void checkActivePowerSetpoint(Validable validable, double activePowerSetpoint) {
        if (Double.isNaN(activePowerSetpoint)) {
            throw createInvalidValueException(validable, activePowerSetpoint, "active power setpoint");
        }
    }

    private static void checkReactivePowerTarget(Validable validable, double targetQ) {
        if (Double.isNaN(targetQ)) {
            throw createInvalidValueException(validable, targetQ, REACTIVE_POWER_SETPOINT);
        }
    }

    public static void checkReactivePowerTarget(Validable validable, String validableType, double reactivePowerSetpoint, boolean voltageRegulatorOn) {
        if (!voltageRegulatorOn && !validReactivePowerSetpoint(reactivePowerSetpoint)) {
            throw new ValidationException(validable, "Unexpected value for reactive power target of " + validableType + ": " + reactivePowerSetpoint);
        }
    }

    public static void checkReactivePowerSetpoint(Validable validable, String validableType, double reactivePowerSetpoint, boolean regulationOn) {
        if (regulationOn && !validReactivePowerSetpoint(reactivePowerSetpoint)) {
            throw new ValidationException(validable, "Unexpected value for reactive power setpoint of " + validableType + ": " + reactivePowerSetpoint);
        }
    }

    public static void checkReactivePowerSetpoint(Validable validable, String validableType, double reactivePowerSetpoint, RegulationMode regulationMode) {
        if (regulationMode == StaticVarCompensator.RegulationMode.REACTIVE_POWER && !validReactivePowerSetpoint(reactivePowerSetpoint)) {
            throw new ValidationException(validable, "Unexpected value for reactive power setpoint of " + validableType + ": " + reactivePowerSetpoint);
        }
    }

    public static void checkHvdcActivePowerSetpoint(Validable validable, double activePowerSetpoint) {
        if (Double.isNaN(activePowerSetpoint)) {
            throw createInvalidValueException(validable, activePowerSetpoint, "active power setpoint");
        } else if (activePowerSetpoint < 0) {
            throw createInvalidValueException(validable, activePowerSetpoint, "active power setpoint should not be negative");
        }
    }

    public static void checkActivePowerLimits(Validable validable, double minP, double maxP) {
        if (minP > maxP) {
            throw new ValidationException(validable, "invalid active limits [" + minP + ", " + maxP + "]");
        }
    }

    /**
     * @deprecated Use {@link #checkActivePowerLimits(Validable, double, double)} instead.
     */
    @Deprecated(since = "4.3.0")
    public static void checkActivePowerLimits(Validable validable, double minP, double maxP, double p) {
        checkActivePowerLimits(validable, minP, maxP);

        if (p > maxP) {
            throw new ValidationException(validable, "invalid active power p > maxP: " + p + " > " + maxP);
        }
        if (p < minP) {
            throw new ValidationException(validable, "invalid active power p < minP: " + p + " < " + minP);
        }
    }

    public static void checkVoltageSetpoint(Validable validable, String validableType, double voltageSetpoint, boolean voltageRegulatorOn) {
        if (voltageRegulatorOn && !validVoltageSetpoint(voltageSetpoint)) {
            throw new ValidationException(validable, "Unexpected value for voltage setpoint of " + validableType + ": " + voltageSetpoint);
        }
    }

    public static void checkVoltageSetpoint(Validable validable, String validableType, double voltageSetpoint, RegulationMode regulationMode) {
        if (regulationMode == StaticVarCompensator.RegulationMode.VOLTAGE && !validVoltageSetpoint(voltageSetpoint)) {
            throw new ValidationException(validable, "Unexpected value for voltage setpoint of " + validableType + ": " + voltageSetpoint);
        }
    }

    /**
     * @deprecated
     * Use {@link #checkTargetDeadband(Validable, String, double, boolean)} instead
     */
    @Deprecated(since = "4.4.0")
    public static void checkTargetDeadband(Validable validable, String validableType, boolean regulating, double targetDeadband) {
        if (regulating && Double.isNaN(targetDeadband)) {
            throw new ValidationException(validable, "Undefined value for target deadband of regulating " + validableType);
        }
        if (targetDeadband < 0) {
            throw new ValidationException(validable, "Unexpected value for target deadband of " + validableType + ": " + targetDeadband + " < 0");
        }
    }

    public static void checkTargetDeadband(Validable validable, String validableType, double targetDeadband, boolean voltageRegulatorOn) {
        if (voltageRegulatorOn && !validDeadband(targetDeadband)) {
            throw new ValidationException(validable, "Unexpected value for target deadband of " + validableType + ": " + targetDeadband);
        }
    }

    public static void checkRatedS(Validable validable, double ratedS) {
        if (!Double.isNaN(ratedS) && ratedS <= 0) {
            throw new ValidationException(validable, "Invalid value of rated S " + ratedS);
        }
    }

    public static void checkEnergySource(Validable validable, EnergySource energySource) {
        if (energySource == null) {
            throw new ValidationException(validable, "energy source is not set");
        }
    }

    public static void checkMinP(Validable validable, double minP) {
        if (Double.isNaN(minP)) {
            throw createInvalidValueException(validable, minP, "minimum P");
        }
    }

    public static void checkMaxP(Validable validable, double maxP) {
        if (Double.isNaN(maxP)) {
            throw createInvalidValueException(validable, maxP, "maximum P");
        }
    }

    public static void checkHvdcMaxP(Validable validable, double maxP) {
        if (Double.isNaN(maxP)) {
            throw createInvalidValueException(validable, maxP, "maximum P");
        } else if (maxP < 0) {
            throw createInvalidValueException(validable, maxP, "maximum P");
        }
    }

    /**
     * @deprecated
     * Use
     *     {@link #checkRegulatingTerminal(Validable, String, Terminal, boolean, Network)} or
     *     {@link #checkRegulatingTerminal(Validable, String, Terminal, RegualtionMode, Network)} instead
     */
    @Deprecated(since = "4.4.0")
    public static void checkRegulatingTerminal(Validable validable, Terminal regulatingTerminal, Network network) {
        if (regulatingTerminal != null && regulatingTerminal.getVoltageLevel().getNetwork() != network) {
            throw new ValidationException(validable, "regulating terminal is not part of the network");
        }
    }

    public static void checkRegulatingTerminal(Validable validable, String validableType, Terminal regulatingTerminal,
        boolean voltageRegulatorOn, Network network) {
        if (voltageRegulatorOn && !validRegulatingTerminal(regulatingTerminal, network)) {
            throw new ValidationException(validable, "Unexpected value for regulating terminal of " + validableType + ": " + regulatingTerminal);
        }
    }

    public static void checkRegulatingTerminal(Validable validable, String validableType, Terminal regulatingTerminal,
        RegulationMode regulationMode, Network network) {
        if (regulationMode != StaticVarCompensator.RegulationMode.OFF && !validRegulatingTerminal(regulatingTerminal, network)) {
            throw new ValidationException(validable, "Unexpected value for regulating terminal of " + validableType + ": " + regulatingTerminal);
        }
    }

    public static void checkLoadType(Validable validable, LoadType loadType) {
        if (loadType == null) {
            throw new ValidationException(validable, "load type is null");
        }
    }

    public static void checkP0(Validable validable, double p0) {
        if (Double.isNaN(p0)) {
            throw new ValidationException(validable, "p0 is invalid");
        }
    }

    public static void checkQ0(Validable validable, double q0) {
        if (Double.isNaN(q0)) {
            throw new ValidationException(validable, "q0 is invalid");
        }
    }

    public static void checkR(Validable validable, double r) {
        if (Double.isNaN(r)) {
            throw new ValidationException(validable, "r is invalid");
        }
    }

    public static void checkX(Validable validable, double x) {
        if (Double.isNaN(x)) {
            throw new ValidationException(validable, "x is invalid");
        }
    }

    public static void checkG1(Validable validable, double g1) {
        if (Double.isNaN(g1)) {
            throw new ValidationException(validable, "g1 is invalid");
        }
    }

    public static void checkG2(Validable validable, double g2) {
        if (Double.isNaN(g2)) {
            throw new ValidationException(validable, "g2 is invalid");
        }
    }

    public static void checkB1(Validable validable, double b1) {
        if (Double.isNaN(b1)) {
            throw new ValidationException(validable, "b1 is invalid");
        }
    }

    public static void checkB2(Validable validable, double b2) {
        if (Double.isNaN(b2)) {
            throw new ValidationException(validable, "b2 is invalid");
        }
    }

    public static void checkG(Validable validable, double g) {
        if (Double.isNaN(g)) {
            throw new ValidationException(validable, "g is invalid");
        }
    }

    public static void checkB(Validable validable, double b) {
        if (Double.isNaN(b)) {
            throw new ValidationException(validable, "b is invalid");
        }
    }

    public static void checkNominalV(Validable validable, double nominalV) {
        if (Double.isNaN(nominalV) || nominalV <= 0) {
            throw new ValidationException(validable, "nominal voltage is invalid");
        }
    }

    public static void checkVoltageLimits(Validable validable, double lowVoltageLimit, double highVoltageLimit) {
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

    public static void checkTopologyKind(Validable validable, TopologyKind topologyKind) {
        if (topologyKind == null) {
            throw new ValidationException(validable, "topology kind is invalid");
        }
    }

    public static void checkCaseDate(Validable validable, DateTime caseDate) {
        if (caseDate == null) {
            throw new ValidationException(validable, "case date is invalid");
        }
    }

    public static void checkForecastDistance(Validable validable, int forecastDistance) {
        if (forecastDistance < 0) {
            throw new ValidationException(validable, "forecast distance < 0");
        }
    }

    /**
     * @deprecated
     * Use {@link #checkBPerSection(Validable, double)} instead.
     */
    @Deprecated(since = "4.2.0")
    public static void checkLinearBPerSection(Validable validable, double bPerSection) {
        checkBPerSection(validable, bPerSection);
        if (bPerSection == 0) {
            throw new ValidationException(validable, "susceptance per section is equal to zero");
        }
    }

    public static void checkBPerSection(Validable validable, double sectionB) {
        if (Double.isNaN(sectionB)) {
            throw new ValidationException(validable, "section susceptance is invalid");
        }
    }

    public static void checkMaximumSectionCount(Validable validable, int maximumSectionCount) {
        if (maximumSectionCount <= 0) {
            throw new ValidationException(validable,
                    "the maximum number of section (" + maximumSectionCount
                            + ") should be greater than 0");
        }
    }

    public static void checkSections(Validable validable, int currentSectionCount, int maximumSectionCount) {
        if (currentSectionCount < 0) {
            throw new ValidationException(validable,
                    "the current number of section (" + currentSectionCount
                            + ") should be greater than or equal to 0");
        }
        checkMaximumSectionCount(validable, maximumSectionCount);
        if (currentSectionCount > maximumSectionCount) {
            throw new ValidationException(validable,
                    "the current number (" + currentSectionCount
                            + ") of section should be lesser than the maximum number of section ("
                            + maximumSectionCount + ")");
        }
    }

    public static void checkRatedU(Validable validable, double ratedU, String num) {
        if (Double.isNaN(ratedU)) {
            throw new ValidationException(validable, "rated U" + num + " is invalid");
        }
    }

    public static void checkRatedU1(Validable validable, double ratedU1) {
        checkRatedU(validable, ratedU1, "1");
    }

    public static void checkRatedU2(Validable validable, double ratedU2) {
        checkRatedU(validable, ratedU2, "2");
    }

    public static void checkBmin(Validable validable, double bMin) {
        if (Double.isNaN(bMin)) {
            throw new ValidationException(validable, "bmin is invalid");
        }
    }

    public static void checkBmax(Validable validable, double bMax) {
        if (Double.isNaN(bMax)) {
            throw new ValidationException(validable, "bmax is invalid");
        }
    }

    private static void throwExceptionOrWarningForRtc(Validable validable, boolean loadTapChangingCapabilities, String message) {
        if (loadTapChangingCapabilities) {
            throw new ValidationException(validable, message);
        } else {
            LOGGER.warn(message);
        }
    }

    /**
     * @deprecated
     * Use {@link #checkRatioTapChangerRegulation(Validable, boolean, boolean, Terminal, double, double, Network)} instead
     */
    @Deprecated(since = "4.4.0")
    public static void checkRatioTapChangerRegulation(Validable validable, boolean regulating, boolean loadTapChangingCapabilities,
        Terminal regulationTerminal, double targetV, Network network) {
        if (regulating) {
            if (Double.isNaN(targetV)) {
                throwExceptionOrWarningForRtc(validable, loadTapChangingCapabilities, "a target voltage has to be set for a regulating ratio tap changer");
            }
            if (targetV <= 0) {
                throwExceptionOrWarningForRtc(validable, loadTapChangingCapabilities, "bad target voltage " + targetV);
            }
            if (regulationTerminal == null) {
                throwExceptionOrWarningForRtc(validable, loadTapChangingCapabilities, "a regulation terminal has to be set for a regulating ratio tap changer");
            }
            if (regulationTerminal != null && regulationTerminal.getVoltageLevel().getNetwork() != network) {
                throwExceptionOrWarningForRtc(validable, loadTapChangingCapabilities, "regulation terminal is not part of the network");
            }
        }
    }

    /**
     * @deprecated
     * Use {@link #checkRatioTapChangerRegulation(Validable, boolean, Terminal, double, double, Network)} instead
     */
    @Deprecated(since = "4.4.0")
    public static void checkRatioTapChangerRegulation(Validable validable, boolean regulating,
        Terminal regulationTerminal, double targetV, Network network) {
        checkRatioTapChangerRegulation(validable, regulating, true, regulationTerminal, targetV, network);
    }

    /**
     * @deprecated
     * Use {@link #checkPhaseTapChangerRegulation(Validable, boolean, PhaseTapChanger.RegulationMode, Terminal, double, double, Network)} instead
     */
    @Deprecated(since = "4.4.0")
    public static void checkPhaseTapChangerRegulation(Validable validable, PhaseTapChanger.RegulationMode regulationMode,
        double regulationValue, boolean regulating, Terminal regulationTerminal,
        Network network) {
        if (regulationMode == null) {
            throw new ValidationException(validable, "phase regulation mode is not set");
        }
        if (regulating) {
            if (regulationMode != PhaseTapChanger.RegulationMode.FIXED_TAP && Double.isNaN(regulationValue)) {
                throw new ValidationException(validable, "phase regulation is on and threshold/setpoint value is not set");
            }
            if (regulationMode != PhaseTapChanger.RegulationMode.FIXED_TAP && regulationTerminal == null) {
                throw new ValidationException(validable, "phase regulation is on and regulated terminal is not set");
            }
            if (regulationMode == PhaseTapChanger.RegulationMode.FIXED_TAP) {
                throw new ValidationException(validable, "phase regulation cannot be on if mode is FIXED");
            }
        }
        if (regulationTerminal != null && regulationTerminal.getVoltageLevel().getNetwork() != network) {
            throw new ValidationException(validable, "phase regulation terminal is not part of the network");
        }
    }

    public static void checkRatioTapChangerRegulation(Validable validable, boolean regulating,
        Terminal regulationTerminal, double targetV, double targetDeadband, Network network) {
        checkRatioTapChangerRegulation(validable, regulating, true, regulationTerminal, targetV, targetDeadband, network);
    }

    public static void checkRatioTapChangerRegulation(Validable validable, boolean regulating, boolean loadTapChangingCapabilities,
        Terminal regulationTerminal, double targetV, double targetDeadband, Network network) {
        if (!loadTapChangingCapabilities) {
            return;
        }
        ValidationUtil.checkRegulatingVoltageControl(validable, regulationTerminal, targetV, targetDeadband, regulating, network);
    }

    public static void checkPhaseTapChangerRegulation(Validable validable, boolean regulating,
        PhaseTapChanger.RegulationMode regulationMode, Terminal regulationTerminal, double regulationValue,
        double targetDeadband, Network network) {
        if (regulationMode == null) {
            throw new ValidationException(validable, "phase regulation mode is not set");
        }
        if (regulationMode == PhaseTapChanger.RegulationMode.FIXED_TAP && regulating) {
            throw new ValidationException(validable, "phase regulation cannot be on if mode is FIXED");
        }
        checkRegulatingCurrentOrActivePowerControl(validable, regulationTerminal, regulationValue, targetDeadband,
            regulating, network);
    }

    public static void checkCurrentOrActivePowerSetpoint(Validable validable, String validableType, double currentOrActivePowerSetpoint, boolean regulationOn) {
        if (regulationOn && !validCurrentOrActivePowerSetpoint(currentOrActivePowerSetpoint)) {
            throw new ValidationException(validable, "Unexpected value for current setpoint or active power setpoint of " + validableType + ": " + currentOrActivePowerSetpoint);
        }
    }

    public static void checkOnlyOneTapChangerRegulatingEnabled(Validable validable,
        Set<TapChanger> tapChangersNotIncludingTheModified, boolean regulating) {
        if (regulating && tapChangersNotIncludingTheModified.stream().anyMatch(TapChanger::isRegulating)) {
            throw new ValidationException(validable, "Only one regulating control enabled is allowed");
        }
    }

    public static void checkGeneratorRegulatingVoltageControl(Validable validable, Terminal regulatingTerminal,
        double voltageSetpoint, boolean voltageRegulatorOn, double targetQ, Network network) {
        checkGeneratorRegulatingVoltageControl(validable, regulatingTerminal, voltageSetpoint, voltageRegulatorOn, targetQ, network, true);
    }

    public static void checkGeneratorRegulatingVoltageControl(Validable validable, Terminal regulatingTerminal,
        double voltageSetpoint, boolean voltageRegulatorOn, double targetQ, Network network,
        boolean validateRegulatingTerminal) {
        if (!voltageRegulatorOn) {
            checkReactivePowerTarget(validable, targetQ);
        }
        checkRegulatingVoltageControl(validable, regulatingTerminal, voltageSetpoint, voltageRegulatorOn, network, validateRegulatingTerminal);
    }

    /**
     * @deprecated
     * Use {@link #checkSvcRegulatingControl(Validable, Terminal, double, double, RegulationMode, Network)}
     */
    @Deprecated(since = "4.4.0")
    public static void checkSvcRegulator(Validable validable, double voltageSetpoint, double reactivePowerSetpoint, StaticVarCompensator.RegulationMode regulationMode) {
        if (regulationMode == null) {
            throw new ValidationException(validable, "Regulation mode is invalid");
        }
        switch (regulationMode) {
            case VOLTAGE:
                if (Double.isNaN(voltageSetpoint)) {
                    throw createInvalidValueException(validable, voltageSetpoint, VOLTAGE_SETPOINT);
                }
                break;

            case REACTIVE_POWER:
                if (Double.isNaN(reactivePowerSetpoint)) {
                    throw createInvalidValueException(validable, reactivePowerSetpoint, REACTIVE_POWER_SETPOINT);
                }
                break;

            case OFF:
                // nothing to check
                break;

            default:
                throw new AssertionError();
        }

    }

    public static void checkSvcRegulatingControl(Validable validable, Terminal regulatingTerminal, double voltageSetpoint,
        double reactivePowerSetpoint, RegulationMode regulationMode, Network network) {
        checkSvcRegulatingControl(validable, regulatingTerminal, voltageSetpoint, reactivePowerSetpoint, regulationMode, network, true);
    }

    public static void checkSvcRegulatingControl(Validable validable, Terminal regulatingTerminal, double voltageSetpoint,
        double reactivePowerSetpoint, RegulationMode regulationMode, Network network, boolean validateRegulatingTerminal) {
        if (regulationMode == null) {
            throw new ValidationException(validable, "Regulation mode is invalid");
        }
        if (regulationMode == StaticVarCompensator.RegulationMode.VOLTAGE) {
            ValidationUtil.checkRegulatingVoltageControl(validable, regulatingTerminal, voltageSetpoint, true, network, validateRegulatingTerminal);
        } else if (regulationMode == StaticVarCompensator.RegulationMode.REACTIVE_POWER) {
            ValidationUtil.checkRegulatingReactivePowerControl(validable, regulatingTerminal, reactivePowerSetpoint, true, network);
        }
    }

    /**
     * @deprecated
     * Use {@link #checkRegulatingVoltageControlAndReactivePowerSetpoint(Validable, Terminal, double, double, boolean, Network)} instead
     */
    @Deprecated(since = "4.4.0")
    public static void checkVoltageControl(Validable validable, Boolean voltageRegulatorOn, double voltageSetpoint, double reactivePowerSetpoint) {
        if (checkVoltageControl(validable, voltageRegulatorOn, voltageSetpoint) && Double.isNaN(reactivePowerSetpoint)) {
            throw createInvalidValueException(validable, reactivePowerSetpoint, REACTIVE_POWER_SETPOINT, VOLTAGE_REGULATOR_OFF);
        }
    }

    /**
     * @deprecated
     * Use {@link #checkRegulatingVoltageControl(Validable, Terminal, double, boolean, Network)} instead
     */
    @Deprecated(since = "4.4.0")
    public static boolean checkVoltageControl(Validable validable, Boolean voltageRegulatorOn, double voltageSetpoint) {
        if (voltageRegulatorOn == null) {
            throw new ValidationException(validable, "voltage regulator status is not set");
        }
        boolean bVoltageRegulatorOn = voltageRegulatorOn; // make sonar happy java:S5411 Boxed "Boolean" should be avoided in boolean expressions
        if (bVoltageRegulatorOn) {
            if (Double.isNaN(voltageSetpoint) || voltageSetpoint <= 0) {
                throw createInvalidValueException(validable, voltageSetpoint, VOLTAGE_SETPOINT, "voltage regulator is on");
            }
            return false;
        }
        return true;
    }

    public static void checkRegulatingVoltageControl(Validable validable, Terminal regulatingTerminal,
        double voltageSetpoint, boolean voltageRegulatorOn, Network network) {
        checkRegulatingVoltageControl(validable, regulatingTerminal, voltageSetpoint, voltageRegulatorOn, network, true);
    }

    public static void checkRegulatingVoltageControl(Validable validable, Terminal regulatingTerminal,
        double voltageSetpoint, boolean voltageRegulatorOn, Network network, boolean validateRegulatingTerminal) {
        if (!voltageRegulatorOn) {
            return;
        }
        if (validateRegulatingTerminal && !validRegulatingTerminal(regulatingTerminal, network)) {
            throw new ValidationException(validable, REGULATING_TERMINAL_NOT_DEFINED);
        }
        if (!validVoltageSetpoint(voltageSetpoint)) {
            throw createInvalidValueException(validable, voltageSetpoint, VOLTAGE_SETPOINT);
        }
    }

    public static void checkRegulatingVoltageControl(Validable validable, Terminal regulatingTerminal,
        double voltageSetpoint, double targetDeadband, boolean voltageRegulatorOn, Network network) {
        checkRegulatingVoltageControl(validable, regulatingTerminal, voltageSetpoint, targetDeadband, voltageRegulatorOn, network, true);
    }

    public static void checkRegulatingVoltageControl(Validable validable, Terminal regulatingTerminal,
        double voltageSetpoint, double targetDeadband, boolean voltageRegulatorOn, Network network,
        boolean validateRegulatingTerminal) {
        if (!voltageRegulatorOn) {
            return;
        }
        if (validateRegulatingTerminal && !validRegulatingTerminal(regulatingTerminal, network)) {
            throw new ValidationException(validable, REGULATING_TERMINAL_NOT_DEFINED);
        }
        if (!validVoltageSetpoint(voltageSetpoint)) {
            throw createInvalidValueException(validable, voltageSetpoint, VOLTAGE_SETPOINT);
        }
        if (!validDeadband(targetDeadband)) {
            throw createInvalidValueException(validable, targetDeadband, "target deadband");
        }
    }

    private static void checkRegulatingVoltageControl(Validable validable, double voltageSetpoint, boolean voltageRegulatorOn) {
        if (!voltageRegulatorOn) {
            return;
        }
        if (!validVoltageSetpoint(voltageSetpoint)) {
            throw createInvalidValueException(validable, voltageSetpoint, VOLTAGE_SETPOINT, "voltage regulator is on");
        }
    }

    public static void checkRegulatingVoltageControlAndReactivePowerSetpoint(Validable validable,
        double voltageSetpoint, double reactivePowerSetpoint, boolean voltageRegulatorOn) {
        if (!voltageRegulatorOn && !validReactivePowerSetpoint(reactivePowerSetpoint)) {
            throw createInvalidValueException(validable, reactivePowerSetpoint, REACTIVE_POWER_SETPOINT, VOLTAGE_REGULATOR_OFF);
        }
        checkRegulatingVoltageControl(validable, voltageSetpoint, voltageRegulatorOn);
    }

    public static void checkRegulatingVoltageControlAndReactivePowerSetpoint(Validable validable,
        Terminal regulatingTerminal, double voltageSetpoint, double reactivePowerSetpoint,
        boolean voltageRegulatorOn, Network network) {
        if (!voltageRegulatorOn && !validReactivePowerSetpoint(reactivePowerSetpoint)) {
            throw createInvalidValueException(validable, reactivePowerSetpoint, REACTIVE_POWER_SETPOINT, VOLTAGE_REGULATOR_OFF);
        }
        checkRegulatingVoltageControl(validable, regulatingTerminal, voltageSetpoint, voltageRegulatorOn, network);
    }

    public static void checkRegulatingReactivePowerControl(Validable validable, Terminal regulatingTerminal,
        double reactivePowerSetpoint, boolean reactivePowerRegulatorOn, Network network) {
        if (!reactivePowerRegulatorOn) {
            return;
        }
        if (!validRegulatingTerminal(regulatingTerminal, network)) {
            throw new ValidationException(validable, REGULATING_TERMINAL_NOT_DEFINED);
        }
        if (!validReactivePowerSetpoint(reactivePowerSetpoint)) {
            throw createInvalidValueException(validable, reactivePowerSetpoint, REACTIVE_POWER_SETPOINT);
        }
    }

    public static void checkRegulatingCurrentOrActivePowerControl(Validable validable, Terminal regulatingTerminal,
        double valueSetpoint, double targetDeadband, boolean regulatorOn, Network network) {
        if (!regulatorOn) {
            return;
        }
        if (!validRegulatingTerminal(regulatingTerminal, network)) {
            throw new ValidationException(validable, REGULATING_TERMINAL_NOT_DEFINED);
        }
        if (!validCurrentOrActivePowerSetpoint(valueSetpoint)) {
            throw createInvalidValueException(validable, valueSetpoint, "current or active power setpoint");
        }
        if (!validDeadband(targetDeadband)) {
            throw createInvalidValueException(validable, targetDeadband, "target deadband");
        }
    }

    public static boolean validRegulatingVoltageControl(Terminal regulatingTerminal, double voltageSetpoint, Network network) {
        return validRegulatingTerminal(regulatingTerminal, network) && validVoltageSetpoint(voltageSetpoint);
    }

    public static boolean validRegulatingVoltageControl(Terminal regulatingTerminal, double voltageSetpoint, double targetDeadband, Network network) {
        return validRegulatingTerminal(regulatingTerminal, network) && validVoltageSetpoint(voltageSetpoint) && validDeadband(targetDeadband);
    }

    public static boolean validRegulatingReactivePowerControl(Terminal regulatingTerminal, double reactivePowerSetpoint, Network network) {
        return validRegulatingTerminal(regulatingTerminal, network) && validReactivePowerSetpoint(reactivePowerSetpoint);
    }

    public static boolean validRegulatingCurrentOrActivePowerControl(Terminal regulatingTerminal, double valueSetpoint, double targetDeadband, Network network) {
        return validRegulatingTerminal(regulatingTerminal, network) && validCurrentOrActivePowerSetpoint(valueSetpoint) && validDeadband(targetDeadband);
    }

    private static boolean validRegulatingTerminal(Terminal regulatingTerminal, Network network) {
        return regulatingTerminal != null && regulatingTerminal.getVoltageLevel().getNetwork() == network;
    }

    private static boolean validVoltageSetpoint(double voltageSetpoint) {
        return !Double.isNaN(voltageSetpoint) && voltageSetpoint > 0;
    }

    private static boolean validReactivePowerSetpoint(double reactivePowerSetpoint) {
        return !Double.isNaN(reactivePowerSetpoint);
    }

    private static boolean validCurrentOrActivePowerSetpoint(double valueSetpoint) {
        return !Double.isNaN(valueSetpoint);
    }

    private static boolean validDeadband(double deadband) {
        return !Double.isNaN(deadband) && deadband >= 0;
    }

    public static void checkConvertersMode(Validable validable, HvdcLine.ConvertersMode converterMode) {
        if (converterMode == null) {
            throw new ValidationException(validable, "converter mode is invalid");
        }
    }

    public static void checkPowerFactor(Validable validable, double powerFactor) {
        if (Double.isNaN(powerFactor)) {
            throw new ValidationException(validable, "power factor is invalid");
        } else if (Math.abs(powerFactor) > 1) {
            throw new ValidationException(validable, "power factor is invalid, it should be between -1 and 1");
        }
    }

    public static void checkConnected(Validable validable, Boolean connected) {
        if (connected == null) {
            throw new ValidationException(validable, "connection status is invalid");
        }
    }

    public static void checkPermanentLimit(Validable validable, double permanentLimit) {
        // TODO: if (Double.isNaN(permanentLimit) || permanentLimit <= 0) {
        if (permanentLimit <= 0) {
            throw new ValidationException(validable, "permanent limit must be defined and be > 0");
        }
    }

    public static void checkLossFactor(Validable validable, float lossFactor) {
        if (Double.isNaN(lossFactor)) {
            throw new ValidationException(validable, "loss factor is invalid");
        } else if (lossFactor < 0 || lossFactor > 100) {
            throw new ValidationException(validable, "loss factor must be >= 0 and <= 100");
        }
    }
}
