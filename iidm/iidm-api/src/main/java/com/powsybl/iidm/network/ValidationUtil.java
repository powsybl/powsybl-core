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
 * Some methods of this class are used for validating regulation controls.
 * <p>
 * Check methods for regulation controls are organized in two sets: Complete check methods and Individual check methods.
 * <p>
 * Complete check methods. <br>
 * Complete check methods verify all the attributes associated with regulation controls. <br>
 * These methods are used in the adder, where the equipment is created and every time
 * that the regulation control is enabled. Regulation control will only change to enable if all their
 * attributes are defined and valid.<br>
 * Complete methods are:
 * <p>
 * {@link #checkVoltageControlAndTargetQ(Validable, Terminal, double, double, boolean, Network)} for voltage control and targetQ validation.<br>
 * {@link #checkVoltageControlAndTargetQ(Validable, Terminal, double, double, boolean, Network, boolean)} for voltage control and targetQ validation.<br>
 * {@link #checkSvcRegulation(Validable, Terminal, double, double, RegulationMode, Network)} for static Var compensator controls.<br>
 * {@link #checkSvcRegulation(Validable, Terminal, double, double, RegulationMode, Network, boolean)} for static Var compensator controls.<br>
 * {@link #checkDiscreteVoltageControl(Validable, Terminal, double, double, boolean, Network)} for discrete voltage control.<br>
 * {@link #checkDiscreteVoltageControl(Validable, Terminal, double, double, boolean, Network, boolean)} for discrete voltage control.<br>
 * {@link #checkVoltageControlAndTargetQ(Validable, double, double, boolean)} for voltage control without regulating terminal and targetQ validation.<br>
 * {@link #checkReactivePowerControl(Validable, Terminal, double, boolean, Network)} for reactive power controls.<br>
 * {@link #checkRatioTapChangerRegulation(Validable, boolean, boolean, Terminal, double, double, Network)} for ratio tapChanger controls.<br>
 * {@link #checkPhaseTapChangerRegulation(Validable, boolean, PhaseTapChanger.RegulationMode, Terminal, double, double, Network)} for phase tapChanger controls.<br>
 * <p>
 *
 * Validation of regulating terminal checks that the terminal is not null and its network is the same as the object being added.
 * The network for the terminal is obtained from its voltage level and terminal voltage level is not set until the terminal is attached.
 * Currently, the terminal is attached after the validation is performed so the regulating terminal is only checked if it is remote.
 * For local regulation (useLocalRegulation) we assume the terminal will be ok since it will be the one of the equipment and it is set automatically by the adder method.
 * <p>
 * Individual check methods.<br>
 * Individual methods are used when one attribute is changed by the corresponding set method
 * after the equipment object was created with adder method.
 * The verification of each attribute depends always on the enabled attribute.
 * If the control is enabled only valid attributes are allowed, but when is not enabled the verification is not done.
 * That allows to define controls after the equipment has been created with the adder method. Usually, controls must be filled
 * at the end of the conversion process when all remote regulating terminals have previously been defined.
 * When only the EQ file is imported the control could be created disabled with not defined and valid attributes. <br>
 * Individual check methods are:
 * <p>
 * {@link #checkVoltageSetpoint(Validable, String, double, boolean)} for validating voltage setpoint.<br>
 * {@link #checkVoltageSetpoint(Validable, String, double, RegulationMode)} for validating voltage setpoint.<br>
 * {@link #checkVoltageSetpoint(Validable, String, double, boolean, boolean)} for validating voltage setpoint.<br>
 * {@link #checkTargetDeadband(Validable, String, double, boolean)} for validating target deadband.<br>
 * {@link #checkTargetDeadband(Validable, String, double, boolean, boolean)} for validating target deadband.<br>
 * {@link #checkRegulatingTerminal(Validable, String, Terminal, boolean, Network)} for validating regulating terminal.<br>
 * {@link #checkRegulatingTerminal(Validable, String, Terminal, RegulationMode, Network)} for validating regulating terminal.<br>
 * {@link #checkRegulatingTerminal(Validable, String, Terminal, RegulationMode, Network, boolean)} for validating regulating terminal.<br>
 * {@link #checkCurrentOrActivePowerSetpoint(Validable, String, double, boolean)} for validating current or active power setpoint.<br>
 * {@link #checkReactivePowerSetpoint(Validable, String, double, boolean)} for validating reactive power setpoint.<br>
 * {@link #checkReactivePowerSetpoint(Validable, String, double, RegulationMode)} for validating reactive power setpoint.<br>
 * {@link #checkReactivePowerTarget(Validable, String, double, boolean)} for validating reactive power setpoint.<br>
 *
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */

public final class ValidationUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationUtil.class);
    private static final String REACTIVE_POWER_SETPOINT = "reactive power setpoint";
    private static final String VOLTAGE_SETPOINT = "voltage setpoint";
    private static final String REACTIVE_POWER_TARGET = "reactive power target";
    private static final String TARGET_DEADBAND = "target deadband";
    private static final String REGULATING_TERMINAL = "regulating terminal";
    private static final String CURRENT_OR_ACTIVE_POWER_SETPOINT = "current or active power setpoint";
    private static final String VOLTAGE = "voltage";
    private static final String REACTIVE_POWER = "reactive power";
    private static final String RATIO_TAP_CHANGER = "ratio tap changer";
    private static final String PHASE_TAP_CHANGER = "phase tap changer";
    private static final String VOLTAGE_REGULATOR_OFF = "voltage regulator is off";
    private static final String MUST_BE_GREATER_THAN_ZERO = "must be > 0.0";
    private static final String MUST_BE_GREATER_THAN_OR_EQUAL_TO_ZERO = "must be >= 0.0";
    private static final String MUST_BE_PART_OF_THE_NETWORK = "must be part of the network";

    private ValidationUtil() {
    }

    private static ValidationException createInvalidValueException(Validable validable, double value, String valueName) {
        return createInvalidValueException(validable, value, valueName, null);
    }

    private static ValidationException createInvalidValueException(Validable validable, double value, String valueName, String reason) {
        String r = reason == null ? "" : " (" + reason + ")";
        return new ValidationException(validable, "invalid value (" + value + ") for " + valueName + r);
    }

    private static ValidationException createInvalidValueException(Validable validable, String controlType, String value, String valueName) {
        return new ValidationException(validable, "invalid value (" + value + ") for " + valueName + " of " + controlType + " control");
    }

    private static ValidationException createInvalidValueException(Validable validable, String controlType, String value, String valueName, String reason) {
        String r = reason == null ? "" : " (" + reason + ")";
        return new ValidationException(validable, "invalid value (" + value + ") for " + valueName + " of " + controlType + " control" + r);
    }

    private static ValidationException createNoSetValueException(Validable validable, String controlType, String valueName) {
        return new ValidationException(validable, valueName + " has to be set for the " + controlType + " control");
    }

    private static void logNoSetValueWarning(String controlType, String valueName) {
        LOGGER.warn("{} has to be set for the {} control", valueName, controlType);
    }

    private static void logInvalidValueWarning(String controlType, String value, String valueName, String reason) {
        String r = reason == null ? "" : "(" + reason + ")";
        LOGGER.warn("invalid value ({}) for {} of {} control {}", value, valueName, controlType, r);
    }

    private static String regulatingTerminalId(Terminal terminal) {
        if (terminal == null) {
            return "Null";
        }
        if (terminal.getBusBreakerView() != null && terminal.getBusBreakerView().getBus() != null) {
            return terminal.getBusBreakerView().getBus().getId();
        }
        return terminal.toString();
    }

    private static void createNotSetValueValueExceptionOrLogWarning(Validable validable, String controlType,
        String valueName, boolean loadTapChangingCapabilities) {
        if (loadTapChangingCapabilities) {
            throw createNoSetValueException(validable, controlType, valueName);
        } else {
            logNoSetValueWarning(controlType, valueName);
        }
    }

    private static void createInvavlidValueValueExceptionOrLogWarning(Validable validable, String controlType,
        String value, String valueName, String reason, boolean loadTapChangingCapabilities) {
        if (loadTapChangingCapabilities) {
            throw createInvalidValueException(validable, controlType, value, valueName, reason);
        } else {
            logInvalidValueWarning(controlType, value, valueName, reason);
        }
    }

    public static void checkActivePowerSetpoint(Validable validable, double activePowerSetpoint) {
        if (Double.isNaN(activePowerSetpoint)) {
            throw createInvalidValueException(validable, activePowerSetpoint, "active power setpoint");
        }
    }

    /**
     * For validating reactive power target.
     * If control is disabled reactive power target must be defined and valid (Not NaN).
     * If control is enabled any value is allowed for reactive power target.
     */
    public static void checkReactivePowerTarget(Validable validable, String controlType, double reactivePowerSetpoint,
        boolean voltageRegulatorOn) {
        if (voltageRegulatorOn) {
            if (isSet(reactivePowerSetpoint) && !validReactivePowerSetpoint(reactivePowerSetpoint)) {
                throw createInvalidValueException(validable, controlType, String.valueOf(reactivePowerSetpoint), REACTIVE_POWER_TARGET);
            }
        } else {
            if (!isSet(reactivePowerSetpoint)) {
                throw createNoSetValueException(validable, controlType, REACTIVE_POWER_TARGET);
            }
            if (!validReactivePowerSetpoint(reactivePowerSetpoint)) {
                throw createInvalidValueException(validable, controlType, String.valueOf(reactivePowerSetpoint), REACTIVE_POWER_TARGET);
            }
        }
    }

    /**
     * For validating reactive power setpoint.
     * If control is enabled reactive power setpoint must be defined and valid (Not NaN).
     */
    public static void checkReactivePowerSetpoint(Validable validable, String controlType, double reactivePowerSetpoint, boolean regulationOn) {
        if (regulationOn) {
            if (!isSet(reactivePowerSetpoint)) {
                throw createNoSetValueException(validable, controlType, REACTIVE_POWER_SETPOINT);
            }
            if (!validReactivePowerSetpoint(reactivePowerSetpoint)) {
                throw createInvalidValueException(validable, controlType, String.valueOf(reactivePowerSetpoint), REACTIVE_POWER_SETPOINT);
            }
        } else {
            if (isSet(reactivePowerSetpoint) && !validReactivePowerSetpoint(reactivePowerSetpoint)) {
                throw createInvalidValueException(validable, controlType, String.valueOf(reactivePowerSetpoint), REACTIVE_POWER_SETPOINT);
            }
        }
    }

    /**
     * For validating reactive power setpoint.
     * If regulationMode is equal to REACTIVE_POWER reactive power setpoint must be defined and valid (Not NaN).
     */
    public static void checkReactivePowerSetpoint(Validable validable, String controlType, double reactivePowerSetpoint, RegulationMode regulationMode) {
        boolean regulationOn = regulationMode == StaticVarCompensator.RegulationMode.REACTIVE_POWER;
        checkReactivePowerSetpoint(validable, controlType, reactivePowerSetpoint, regulationOn);
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

    /**
     * For validating voltage setpoint.
     * If control is enabled voltage setpoint must be defined and valid (> 0.0).
     * If control is disabled and voltage setpoint is set only valid values are allowed.
     */
    public static void checkVoltageSetpoint(Validable validable, String controlType, double voltageSetpoint, boolean voltageRegulatorOn) {
        if (voltageRegulatorOn) {
            if (!isSet(voltageSetpoint)) {
                throw createNoSetValueException(validable, controlType, VOLTAGE_SETPOINT);
            }
            if (!validVoltageSetpoint(voltageSetpoint)) {
                throw createInvalidValueException(validable, controlType, String.valueOf(voltageSetpoint), VOLTAGE_SETPOINT, MUST_BE_GREATER_THAN_ZERO);
            }
        } else {
            if (isSet(voltageSetpoint) && !validVoltageSetpointDisabled(voltageSetpoint)) {
                throw createInvalidValueException(validable, controlType, String.valueOf(voltageSetpoint), VOLTAGE_SETPOINT, MUST_BE_GREATER_THAN_ZERO);
            }
        }
    }

    /**
     * For validating voltage setpoint of ratio tap changer.
     * If control is enabled and load tap changing capabilities is on voltage setpoint must be defined and valid (> 0.0).
     * If control is enabled and load tap changing capabilities is off only a warning message is logged.
     * If control is disabled and voltage setpoint is set only valid values are allowed.
     */
    public static void checkVoltageSetpoint(Validable validable, String controlType, double voltageSetpoint, boolean voltageRegulatorOn, boolean loadTapChangingCapabilities) {
        if (voltageRegulatorOn) {
            if (!isSet(voltageSetpoint)) {
                createNotSetValueValueExceptionOrLogWarning(validable, controlType, VOLTAGE_SETPOINT, loadTapChangingCapabilities);
            }
            if (!validVoltageSetpoint(voltageSetpoint)) {
                createInvavlidValueValueExceptionOrLogWarning(validable, controlType, String.valueOf(voltageSetpoint), VOLTAGE_SETPOINT, MUST_BE_GREATER_THAN_ZERO, loadTapChangingCapabilities);
            }
        } else {
            if (isSet(voltageSetpoint) && !validVoltageSetpointDisabled(voltageSetpoint)) {
                throw createInvalidValueException(validable, controlType, String.valueOf(voltageSetpoint), VOLTAGE_SETPOINT, MUST_BE_GREATER_THAN_ZERO);
            }
        }
    }

    /**
     * For validating voltage setpoint.
     * If regulationMode is equal to VOLTAGE voltage setpoint must be defined and valid (> 0.0).
     * If control is disabled and voltage setpoint is set only valid values are allowed.
     */
    public static void checkVoltageSetpoint(Validable validable, String controlType, double voltageSetpoint, RegulationMode regulationMode) {
        boolean voltageRegulatorOn = regulationMode == StaticVarCompensator.RegulationMode.VOLTAGE;
        checkVoltageSetpoint(validable, controlType, voltageSetpoint, voltageRegulatorOn);
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

    /**
     * For validating target deadband.
     * If control is enabled target deadband must be defined and valid (>= 0.0).
     * If control is disabled and target deadband is set only valid values are allowed.
     */
    public static void checkTargetDeadband(Validable validable, String controlType, double targetDeadband, boolean voltageRegulatorOn) {
        if (voltageRegulatorOn) {
            if (!isSet(targetDeadband)) {
                throw createNoSetValueException(validable, controlType, TARGET_DEADBAND);
            }
            if (!validDeadband(targetDeadband)) {
                throw createInvalidValueException(validable, controlType, String.valueOf(targetDeadband), TARGET_DEADBAND, MUST_BE_GREATER_THAN_OR_EQUAL_TO_ZERO);
            }
        } else {
            if (isSet(targetDeadband) && !validDeadband(targetDeadband)) {
                throw createInvalidValueException(validable, controlType, String.valueOf(targetDeadband), TARGET_DEADBAND, MUST_BE_GREATER_THAN_OR_EQUAL_TO_ZERO);
            }
        }
    }

    /**
     * For validating target deadband.
     * If control is enabled and load tap changing capabilities is on target deadband must be defined and valid (>= 0.0).
     * If control is enabled and load tap changing capabilities is off only a warning message is logged.
     * If control is disabled and target deadband is set only valid values are allowed.
     */
    public static void checkTargetDeadband(Validable validable, String controlType, double targetDeadband, boolean voltageRegulatorOn, boolean loadTapChangingCapabilities) {
        if (voltageRegulatorOn) {
            if (!isSet(targetDeadband)) {
                createNotSetValueValueExceptionOrLogWarning(validable, controlType, TARGET_DEADBAND, loadTapChangingCapabilities);
            }
            if (!validDeadband(targetDeadband)) {
                createInvavlidValueValueExceptionOrLogWarning(validable, controlType, String.valueOf(targetDeadband), TARGET_DEADBAND, MUST_BE_GREATER_THAN_OR_EQUAL_TO_ZERO, loadTapChangingCapabilities);
            }
        } else {
            if (isSet(targetDeadband) && !validDeadband(targetDeadband)) {
                throw createInvalidValueException(validable, controlType, String.valueOf(targetDeadband), TARGET_DEADBAND, MUST_BE_GREATER_THAN_OR_EQUAL_TO_ZERO);
            }
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
     *     {@link #checkRegulatingTerminal(Validable, String, Terminal, RegulationMode, Network)} instead
     */
    @Deprecated(since = "4.4.0")
    public static void checkRegulatingTerminal(Validable validable, Terminal regulatingTerminal, Network network) {
        if (regulatingTerminal != null && regulatingTerminal.getVoltageLevel().getNetwork() != network) {
            throw new ValidationException(validable, "regulating terminal is not part of the network");
        }
    }

    /**
     * For validating regulating terminal.
     * If control is enabled regulating terminal must be defined and valid (Network associated with it should be ok).
     */
    public static void checkRegulatingTerminal(Validable validable, String controlType, Terminal regulatingTerminal,
        boolean regulationOn, Network network) {
        if (regulationOn) {
            if (!isSet(regulatingTerminal)) {
                throw createNoSetValueException(validable, controlType, REGULATING_TERMINAL);
            }
            if (!validRegulatingTerminal(regulatingTerminal, network)) {
                throw createInvalidValueException(validable, controlType, regulatingTerminalId(regulatingTerminal), REGULATING_TERMINAL, MUST_BE_PART_OF_THE_NETWORK);
            }
        } else {
            if (isSet(regulatingTerminal) && !validRegulatingTerminal(regulatingTerminal, network)) {
                throw createInvalidValueException(validable, controlType, regulatingTerminalId(regulatingTerminal), REGULATING_TERMINAL, MUST_BE_PART_OF_THE_NETWORK);
            }
        }
    }

    /**
     * For validating target regulating terminal.
     * If control is enabled regulating terminal must be defined and valid (Network associated with it should be ok).
     */
    public static void checkRegulatingTerminal(Validable validable, String controlType, Terminal regulatingTerminal,
        RegulationMode regulationMode, Network network) {
        boolean regulationOn = regulationMode != StaticVarCompensator.RegulationMode.OFF;
        checkRegulatingTerminal(validable, controlType, regulatingTerminal, regulationOn, network);
    }

    /**
     * For validating regulating terminal.
     * If control is enabled and load tap changing capabilities is on regulating terminal must be defined and valid (Network associated with it should be ok).
     * If control is disabled and voltage setpoint is set only valid values are allowed.
     */
    public static void checkRegulatingTerminal(Validable validable, String controlType, Terminal regulatingTerminal,
        boolean regulationOn, Network network, boolean loadTapChangingCapabilities) {
        if (regulationOn) {
            if (!isSet(regulatingTerminal)) {
                createNotSetValueValueExceptionOrLogWarning(validable, controlType, REGULATING_TERMINAL, loadTapChangingCapabilities);
            }
            if (!validRegulatingTerminal(regulatingTerminal, network)) {
                createInvavlidValueValueExceptionOrLogWarning(validable, controlType, regulatingTerminalId(regulatingTerminal), REGULATING_TERMINAL, MUST_BE_PART_OF_THE_NETWORK, loadTapChangingCapabilities);
            }
        } else {
            if (isSet(regulatingTerminal) && !validRegulatingTerminal(regulatingTerminal, network)) {
                throw createInvalidValueException(validable, controlType, regulatingTerminalId(regulatingTerminal), REGULATING_TERMINAL, MUST_BE_PART_OF_THE_NETWORK);
            }
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
     * For validating ratio tapChanger controls.
     * If the control is enabled voltage setpoint, target deadband and regulating terminal must be defined and valid.
     */
    public static void checkRatioTapChangerRegulation(Validable validable, boolean regulating, boolean loadTapChangingCapabilities,
        Terminal regulationTerminal, double targetV, double targetDeadband, Network network) {

        checkRegulatingTerminal(validable, RATIO_TAP_CHANGER, regulationTerminal, regulating, network, loadTapChangingCapabilities);
        checkVoltageSetpoint(validable, RATIO_TAP_CHANGER, targetV, regulating, loadTapChangingCapabilities);
        checkTargetDeadband(validable, RATIO_TAP_CHANGER, targetDeadband, regulating, loadTapChangingCapabilities);
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

    /**
     * For validating phase tapChanger controls.
     * If the control is enabled current or active power setpoint, target deadband and regulating terminal must be defined and valid.
     */
    public static void checkPhaseTapChangerRegulation(Validable validable, boolean regulating,
        PhaseTapChanger.RegulationMode regulationMode, Terminal regulationTerminal, double regulationValue,
        double targetDeadband, Network network) {
        if (regulationMode == null) {
            throw new ValidationException(validable, "phase regulation mode is not set");
        }
        if (regulationMode == PhaseTapChanger.RegulationMode.FIXED_TAP && regulating) {
            throw new ValidationException(validable, "phase regulation cannot be on if mode is FIXED");
        }
        checkRegulatingTerminal(validable, PHASE_TAP_CHANGER, regulationTerminal, regulating, network);
        checkCurrentOrActivePowerSetpoint(validable, PHASE_TAP_CHANGER, regulationValue, regulating);
        checkTargetDeadband(validable, PHASE_TAP_CHANGER, targetDeadband, regulating);
    }

    /**
     * For validating current or active power setpoint.
     * If control is enabled current or active power setpoint must be defined and valid (Not NaN).
     */
    public static void checkCurrentOrActivePowerSetpoint(Validable validable, String controlType, double currentOrActivePowerSetpoint, boolean regulationOn) {
        if (regulationOn) {
            if (!isSet(currentOrActivePowerSetpoint)) {
                throw createNoSetValueException(validable, controlType, CURRENT_OR_ACTIVE_POWER_SETPOINT);
            }
            if (!validCurrentOrActivePowerSetpoint(currentOrActivePowerSetpoint)) {
                throw createInvalidValueException(validable, controlType, String.valueOf(currentOrActivePowerSetpoint), CURRENT_OR_ACTIVE_POWER_SETPOINT);
            }
        } else {
            if (isSet(currentOrActivePowerSetpoint) && !validCurrentOrActivePowerSetpoint(currentOrActivePowerSetpoint)) {
                throw createInvalidValueException(validable, controlType, String.valueOf(currentOrActivePowerSetpoint), CURRENT_OR_ACTIVE_POWER_SETPOINT);
            }
        }
    }

    public static void checkOnlyOneTapChangerRegulatingEnabled(Validable validable,
        Set<TapChanger> tapChangersNotIncludingTheModified, boolean regulating) {
        if (regulating && tapChangersNotIncludingTheModified.stream().anyMatch(TapChanger::isRegulating)) {
            throw new ValidationException(validable, "Only one regulating control enabled is allowed");
        }
    }

    /**
     * @deprecated
     * Use {@link #checkSvcRegulation(Validable, Terminal, double, double, RegulationMode, Network)}
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

    /**
     * For validating static Var compensator control.
     * Regulating Terminal is forced to be validated.
     */
    public static void checkSvcRegulation(Validable validable, Terminal regulatingTerminal, double voltageSetpoint,
        double reactivePowerSetpoint, RegulationMode regulationMode, Network network) {
        checkSvcRegulation(validable, regulatingTerminal, voltageSetpoint, reactivePowerSetpoint, regulationMode, network, true);
    }

    /**
     * For validating static Var compensator control.
     * User can select if regulating terminal should be validated.
     * Depending on RegulationMode (OFF, VOLTAGE, REACTIVE_POWER) static Var compensator will control voltage or reactive power.
     * When RegulationMode is OFF there is no validation.
     * When RegulationMode is VOLTAGE, voltage setpoint and regulating terminal must be defined and valid.
     * When RegulationMode is REACTIVE_POWER reactive power setpoint and regulating terminal must be defined and valid.
     */
    public static void checkSvcRegulation(Validable validable, Terminal regulatingTerminal, double voltageSetpoint,
        double reactivePowerSetpoint, RegulationMode regulationMode, Network network, boolean validateRegulatingTerminal) {
        if (regulationMode == null) {
            throw new ValidationException(validable, "Regulation mode is invalid");
        }
        if (regulationMode == StaticVarCompensator.RegulationMode.VOLTAGE) {
            checkContinuousVoltageControl(validable, regulatingTerminal, voltageSetpoint, true, network, validateRegulatingTerminal);
        } else if (regulationMode == StaticVarCompensator.RegulationMode.REACTIVE_POWER) {
            checkReactivePowerControl(validable, regulatingTerminal, reactivePowerSetpoint, true, network);
        }
    }

    /**
     * @deprecated
     * Use {@link #checkVoltageControlAndTargetQ(Validable, Terminal, double, double, boolean, Network)} instead
     */
    @Deprecated(since = "4.4.0")
    public static void checkVoltageControl(Validable validable, Boolean voltageRegulatorOn, double voltageSetpoint, double reactivePowerSetpoint) {
        if (checkVoltageControl(validable, voltageRegulatorOn, voltageSetpoint) && Double.isNaN(reactivePowerSetpoint)) {
            throw createInvalidValueException(validable, reactivePowerSetpoint, REACTIVE_POWER_SETPOINT, VOLTAGE_REGULATOR_OFF);
        }
    }

    /**
     * For validating voltage control and targetQ.
     * Regulating Terminal is forced to be validated.
     */
    public static void checkVoltageControlAndTargetQ(Validable validable, Terminal regulatingTerminal,
        double voltageSetpoint, double targetQ, boolean voltageRegulatorOn, Network network) {
        checkVoltageControlAndTargetQ(validable, regulatingTerminal, voltageSetpoint, targetQ, voltageRegulatorOn, network, true);
    }

    /**
     * For validating voltage control ant targetQ.
     * User can select if regulating terminal should be validated.
     * If the voltage control is disabled targetQ must be defined and valid.
     * If the voltage control is enabled voltage setpoint and regulating terminal must be defined and valid.
     */
    public static void checkVoltageControlAndTargetQ(Validable validable, Terminal regulatingTerminal,
        double voltageSetpoint, double targetQ, boolean voltageRegulatorOn, Network network,
        boolean validateRegulatingTerminal) {

        checkReactivePowerTarget(validable, VOLTAGE, targetQ, voltageRegulatorOn);
        checkContinuousVoltageControl(validable, regulatingTerminal, voltageSetpoint, voltageRegulatorOn, network, validateRegulatingTerminal);
    }

    /**
     * For validating voltage control (without regulating terminal) and targetQ.
     * If the voltage control is disabled targetQ must be defined and valid.
     * If the voltage control is enabled voltage setpoint must be defined and valid.
     */
    public static void checkVoltageControlAndTargetQ(Validable validable,
        double voltageSetpoint, double reactivePowerSetpoint, boolean voltageRegulatorOn) {

        checkReactivePowerTarget(validable, VOLTAGE, reactivePowerSetpoint, voltageRegulatorOn);
        checkVoltageSetpoint(validable, VOLTAGE, voltageSetpoint, voltageRegulatorOn);
    }

    /**
     * For validating discrete voltage controls.
     * Regulating Terminal is forced to be validated.
     */
    public static void checkDiscreteVoltageControl(Validable validable, Terminal regulatingTerminal,
        double voltageSetpoint, double targetDeadband, boolean voltageRegulatorOn, Network network) {
        checkDiscreteVoltageControl(validable, regulatingTerminal, voltageSetpoint, targetDeadband, voltageRegulatorOn,
            network, true);
    }

    /**
    * For validating discrete voltage controls.
    * User can select if regulating terminal should be validated.
    * If the voltage regulation control is enabled voltage setpoint, target deadband and regulating terminal must be defined and valid.
    */
    public static void checkDiscreteVoltageControl(Validable validable, Terminal regulatingTerminal,
        double voltageSetpoint, double targetDeadband, boolean voltageRegulatorOn, Network network,
        boolean validateRegulatingTerminal) {

        if (validateRegulatingTerminal) {
            checkRegulatingTerminal(validable, VOLTAGE, regulatingTerminal, voltageRegulatorOn, network);
        }
        checkVoltageSetpoint(validable, VOLTAGE, voltageSetpoint, voltageRegulatorOn);
        checkTargetDeadband(validable, VOLTAGE, targetDeadband, voltageRegulatorOn);
    }

    /**
    * For validating reactive power controls.
    * If the control is enabled reactive power setpoint and regulating terminal must be defined and valid.
    */
    public static void checkReactivePowerControl(Validable validable, Terminal regulatingTerminal,
        double reactivePowerSetpoint, boolean reactivePowerRegulatorOn, Network network) {

        checkRegulatingTerminal(validable, REACTIVE_POWER, regulatingTerminal, reactivePowerRegulatorOn, network);
        checkReactivePowerSetpoint(validable, REACTIVE_POWER, reactivePowerSetpoint, reactivePowerRegulatorOn);
    }

    private static void checkContinuousVoltageControl(Validable validable, Terminal regulatingTerminal,
        double voltageSetpoint, boolean voltageRegulatorOn, Network network, boolean validateRegulatingTerminal) {

        if (validateRegulatingTerminal) {
            checkRegulatingTerminal(validable, VOLTAGE, regulatingTerminal, voltageRegulatorOn, network);
        }
        checkVoltageSetpoint(validable, VOLTAGE, voltageSetpoint, voltageRegulatorOn);
    }

    /**
     * @deprecated
     * Use {@link #checkVoltageSetpoint(Validable, String, double, boolean)} instead
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

    public static boolean validRegulatingVoltageControl(Terminal regulatingTerminal, double voltageSetpoint, Network network) {
        return validRegulatingTerminal(regulatingTerminal, network) && validVoltageSetpoint(voltageSetpoint);
    }

    public static boolean validRegulatingVoltageControl(Terminal regulatingTerminal, double voltageSetpoint, double targetDeadband, Network network) {
        return validRegulatingTerminal(regulatingTerminal, network) && validVoltageSetpoint(voltageSetpoint) && validDeadband(targetDeadband);
    }

    public static boolean validRegulatingVoltageControl(double voltageSetpoint) {
        return validVoltageSetpoint(voltageSetpoint);
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

    // In some conformity cases (CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2)
    // we receive targetV = 0.0 and regulating off
    // To support this cases when regulating is disabled we allow voltageSetpoint zero
    private static boolean validVoltageSetpointDisabled(double voltageSetpoint) {
        return !Double.isNaN(voltageSetpoint) && voltageSetpoint >= 0;
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

    private static boolean isSet(double value) {
        return !Double.isNaN(value);
    }

    private static boolean isSet(Terminal terminal) {
        return terminal != null;
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
