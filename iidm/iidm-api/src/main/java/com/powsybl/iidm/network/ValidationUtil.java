/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.report.TypedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public final class ValidationUtil {

    public enum ActionOnError {
        THROW_EXCEPTION,
        LOG_ERROR,
        IGNORE,
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationUtil.class);

    private static final String ACTIVE_POWER_SETPOINT = "active power setpoint";
    private static final String MAXIMUM_P = "maximum P";
    private static final String UNIQUE_REGULATING_TAP_CHANGER_MSG = "Only one regulating control enabled is allowed";
    private static final String VOLTAGE_REGULATOR_ON = "voltage regulator is on";
    private static final String VOLTAGE_SETPOINT = "voltage setpoint";

    private ValidationUtil() {
    }

    public static PowsyblException createUndefinedValueGetterException() {
        return new PowsyblException("This getter cannot be used if the value is not defined");
    }

    public static PowsyblException createUnsetMethodException() {
        return new PowsyblException("Unset method is not defined. Implement SCADA mode in order to use it");
    }

    private static ValidationException createInvalidValueException(Validable validable, double value, String valueName) {
        return createInvalidValueException(validable, value, valueName, null);
    }

    private static ValidationException createInvalidValueException(Validable validable, double value, String valueName, String reason) {
        String r = reason == null ? "" : " (" + reason + ")";
        return new ValidationException(validable, "invalid value (" + value + ") for " + valueName + r);
    }

    private static String createInvalidValueMessage(double value, String valueName, String reason) {
        return "invalid value (" + value + ") for " + valueName + (reason == null ? "" : " (" + reason + ")");
    }

    private static void logError(Validable validable, String message, ReportNode reportNode) {
        reportNode.newReportNode()
                .withMessageTemplate(validable.getMessageHeader(), message)
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
        LOGGER.error("{}{}", validable.getMessageHeader(), message);
    }

    private static void throwExceptionOrLogError(Validable validable, String message, ActionOnError actionOnError, ReportNode reportNode) {
        if (actionOnError == ActionOnError.THROW_EXCEPTION) {
            throw new ValidationException(validable, message);
        }
        if (actionOnError == ActionOnError.LOG_ERROR) {
            logError(validable, message, reportNode);
        }
    }

    private static void throwExceptionOrIgnore(Validable validable, String message, ActionOnError actionOnError) {
        if (actionOnError == ActionOnError.THROW_EXCEPTION) {
            throw new ValidationException(validable, message);
        }
    }

    public static void throwExceptionOrIgnore(Validable validable, String message, ValidationLevel validationLevel) {
        throwExceptionOrIgnore(validable, message, checkValidationActionOnError(validationLevel));
    }

    public static void throwExceptionOrLogError(Validable validable, String message, ValidationLevel validationLevel, ReportNode reportNode) {
        throwExceptionOrLogError(validable, message, validationLevel == ValidationLevel.STEADY_STATE_HYPOTHESIS ? ActionOnError.THROW_EXCEPTION : ActionOnError.LOG_ERROR, reportNode);
    }

    private static void throwExceptionOrLogErrorForInvalidValue(Validable validable, double value, String valueName, ActionOnError actionOnError, ReportNode reportNode) {
        throwExceptionOrLogErrorForInvalidValue(validable, value, valueName, null, actionOnError, reportNode);
    }

    private static void throwExceptionOrLogErrorForInvalidValue(Validable validable, double value, String valueName, String reason, ActionOnError actionOnError, ReportNode reportNode) {
        if (actionOnError == ActionOnError.THROW_EXCEPTION) {
            throw createInvalidValueException(validable, value, valueName, reason);
        }
        if (actionOnError == ActionOnError.LOG_ERROR) {
            logError(validable, createInvalidValueMessage(value, valueName, reason), reportNode);
        }
    }

    public static ValidationLevel checkActivePowerSetpoint(Validable validable, double activePowerSetpoint, ValidationLevel validationLevel, ReportNode reportNode) {
        return checkActivePowerSetpoint(validable, activePowerSetpoint, checkValidationActionOnError(validationLevel), reportNode);
    }

    private static ValidationLevel checkActivePowerSetpoint(Validable validable, double activePowerSetpoint, ActionOnError actionOnError, ReportNode reportNode) {
        if (Double.isNaN(activePowerSetpoint)) {
            throwExceptionOrLogErrorForInvalidValue(validable, activePowerSetpoint, ACTIVE_POWER_SETPOINT, actionOnError, reportNode);
            return ValidationLevel.EQUIPMENT;
        }
        return ValidationLevel.STEADY_STATE_HYPOTHESIS;
    }

    public static ValidationLevel checkHvdcActivePowerSetpoint(Validable validable, double activePowerSetpoint, ValidationLevel validationLevel, ReportNode reportNode) {
        return checkHvdcActivePowerSetpoint(validable, activePowerSetpoint, checkValidationActionOnError(validationLevel), reportNode);
    }

    private static ValidationLevel checkHvdcActivePowerSetpoint(Validable validable, double activePowerSetpoint, ActionOnError actionOnError, ReportNode reportNode) {
        if (Double.isNaN(activePowerSetpoint)) {
            throwExceptionOrLogErrorForInvalidValue(validable, activePowerSetpoint, ACTIVE_POWER_SETPOINT, actionOnError, reportNode);
            return ValidationLevel.EQUIPMENT;
        } else if (activePowerSetpoint < 0) {
            throw createInvalidValueException(validable, activePowerSetpoint, ACTIVE_POWER_SETPOINT, "active power setpoint should not be negative");
        }
        return ValidationLevel.STEADY_STATE_HYPOTHESIS;
    }

    public static void checkActivePowerLimits(Validable validable, double minP, double maxP) {
        if (minP > maxP) {
            throw new ValidationException(validable, "invalid active limits [" + minP + ", " + maxP + "]");
        }
    }

    public static ValidationLevel checkTargetDeadband(Validable validable, String validableType, boolean regulating, double targetDeadband, ValidationLevel validationLevel, ReportNode reportNode) {
        return checkTargetDeadband(validable, validableType, regulating, targetDeadband, checkValidationActionOnError(validationLevel), reportNode);
    }

    private static ValidationLevel checkTargetDeadband(Validable validable, String validableType, boolean regulating, double targetDeadband, ActionOnError actionOnError, ReportNode reportNode) {
        if (regulating && Double.isNaN(targetDeadband)) {
            throwExceptionOrLogError(validable, "Undefined value for target deadband of regulating " + validableType, actionOnError, reportNode);
            return ValidationLevel.EQUIPMENT;
        }
        if (targetDeadband < 0) {
            throw new ValidationException(validable, "Unexpected value for target deadband of " + validableType + ": " + targetDeadband + " < 0");
        }
        return ValidationLevel.STEADY_STATE_HYPOTHESIS;
    }

    public static ValidationLevel checkVoltageControl(Validable validable, boolean voltageRegulatorOn, double voltageSetpoint, ValidationLevel validationLevel, ReportNode reportNode) {
        return checkVoltageControl(validable, voltageRegulatorOn, voltageSetpoint, checkValidationActionOnError(validationLevel), reportNode);
    }

    private static ValidationLevel checkVoltageControl(Validable validable, boolean voltageRegulatorOn, double voltageSetpoint, ActionOnError actionOnError, ReportNode reportNode) {
        if (voltageRegulatorOn) {
            if (Double.isNaN(voltageSetpoint)) {
                throwExceptionOrLogErrorForInvalidValue(validable, voltageSetpoint, VOLTAGE_SETPOINT, VOLTAGE_REGULATOR_ON, actionOnError, reportNode);
                return ValidationLevel.EQUIPMENT;
            }
            if (voltageSetpoint <= 0) {
                throw createInvalidValueException(validable, voltageSetpoint, VOLTAGE_SETPOINT, VOLTAGE_REGULATOR_ON);
            }
        }
        return ValidationLevel.STEADY_STATE_HYPOTHESIS;
    }

    public static ValidationLevel checkVoltageControl(Validable validable, Boolean voltageRegulatorOn, double voltageSetpoint, double reactivePowerSetpoint, ValidationLevel validationLevel, ReportNode reportNode) {
        return checkVoltageControl(validable, voltageRegulatorOn, voltageSetpoint, reactivePowerSetpoint, checkValidationActionOnError(validationLevel), reportNode);
    }

    private static ValidationLevel checkVoltageControl(Validable validable, Boolean voltageRegulatorOn, double voltageSetpoint, double reactivePowerSetpoint, ActionOnError actionOnError, ReportNode reportNode) {
        if (voltageRegulatorOn == null) {
            throw new ValidationException(validable, "voltage regulator status is not set");
        }
        if (voltageRegulatorOn) {
            if (Double.isNaN(voltageSetpoint)) {
                throwExceptionOrLogErrorForInvalidValue(validable, voltageSetpoint, VOLTAGE_SETPOINT, VOLTAGE_REGULATOR_ON, actionOnError, reportNode);
                return ValidationLevel.EQUIPMENT;
            }
            if (voltageSetpoint <= 0) {
                throw createInvalidValueException(validable, voltageSetpoint, VOLTAGE_SETPOINT, VOLTAGE_REGULATOR_ON);
            }
        } else if (Double.isNaN(reactivePowerSetpoint)) {
            throwExceptionOrLogErrorForInvalidValue(validable, reactivePowerSetpoint, "reactive power setpoint", "voltage regulator is off", actionOnError, reportNode);
            return ValidationLevel.EQUIPMENT;
        }
        return ValidationLevel.STEADY_STATE_HYPOTHESIS;
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
            throw createInvalidValueException(validable, maxP, MAXIMUM_P);
        }
    }

    public static void checkHvdcMaxP(Validable validable, double maxP) {
        if (Double.isNaN(maxP)) {
            throw createInvalidValueException(validable, maxP, MAXIMUM_P);
        } else if (maxP < 0) {
            throw createInvalidValueException(validable, maxP, MAXIMUM_P, "maximum P should not be negative");
        }
    }

    public static void checkRegulatingTerminal(Validable validable, Terminal regulatingTerminal, Network network) {
        if (regulatingTerminal != null && regulatingTerminal.getVoltageLevel().getNetwork() != network) {
            throw new ValidationException(validable, "regulating terminal is not part of the network");
        }
    }

    public static void checkLoadType(Validable validable, LoadType loadType) {
        if (loadType == null) {
            throw new ValidationException(validable, "load type is null");
        }
    }

    public static ValidationLevel checkP0(Validable validable, double p0, ValidationLevel validationLevel, ReportNode reportNode) {
        return checkP0(validable, p0, checkValidationActionOnError(validationLevel), reportNode);
    }

    private static ValidationLevel checkP0(Validable validable, double p0, ActionOnError actionOnError, ReportNode reportNode) {
        if (Double.isNaN(p0)) {
            throwExceptionOrLogError(validable, "p0 is invalid", actionOnError, reportNode);
            return ValidationLevel.EQUIPMENT;
        }
        return ValidationLevel.STEADY_STATE_HYPOTHESIS;
    }

    public static ValidationLevel checkQ0(Validable validable, double q0, ValidationLevel validationLevel, ReportNode reportNode) {
        return checkQ0(validable, q0, checkValidationActionOnError(validationLevel), reportNode);
    }

    private static ValidationLevel checkQ0(Validable validable, double q0, ActionOnError actionOnError, ReportNode reportNode) {
        if (Double.isNaN(q0)) {
            throwExceptionOrLogError(validable, "q0 is invalid", actionOnError, reportNode);
            return ValidationLevel.EQUIPMENT;
        }
        return ValidationLevel.STEADY_STATE_HYPOTHESIS;
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

    public static void checkCaseDate(Validable validable, ZonedDateTime caseDate) {
        if (caseDate == null) {
            throw new ValidationException(validable, "case date is invalid");
        }
    }

    public static void checkForecastDistance(Validable validable, int forecastDistance) {
        if (forecastDistance < 0) {
            throw new ValidationException(validable, "forecast distance < 0");
        }
    }

    public static void checkBPerSection(Validable validable, double sectionB) {
        if (Double.isNaN(sectionB)) {
            throw new ValidationException(validable, "section susceptance is invalid");
        }
    }

    public static void checkMaximumSectionCount(Validable validable, int maximumSectionCount) {
        if (maximumSectionCount <= 0) {
            throw new ValidationException(validable, "the maximum number of section (" + maximumSectionCount
                    + ") should be greater than 0");
        }
    }

    public static ValidationLevel checkSections(Validable validable, Integer currentSectionCount, int maximumSectionCount, ValidationLevel validationLevel, ReportNode reportNode) {
        return checkSections(validable, currentSectionCount, maximumSectionCount, checkValidationActionOnError(validationLevel), reportNode);
    }

    private static ValidationLevel checkSections(Validable validable, Integer currentSectionCount, int maximumSectionCount, ActionOnError actionOnError, ReportNode reportNode) {
        checkMaximumSectionCount(validable, maximumSectionCount);
        if (currentSectionCount == null) {
            throwExceptionOrLogError(validable, "the current number of section is undefined", actionOnError, reportNode);
            return ValidationLevel.EQUIPMENT;
        } else {
            if (currentSectionCount < 0) {
                throw new ValidationException(validable, "the current number of section (" + currentSectionCount
                        + ") should be greater than or equal to 0");
            }
            if (currentSectionCount > maximumSectionCount) {
                throw new ValidationException(validable, "the current number (" + currentSectionCount
                        + ") of section should be lesser than the maximum number of section ("
                        + maximumSectionCount + ")");
            }
        }
        return ValidationLevel.STEADY_STATE_HYPOTHESIS;
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

    public static ValidationLevel checkSvcRegulator(Validable validable, double voltageSetpoint, double reactivePowerSetpoint,
                                                    StaticVarCompensator.RegulationMode regulationMode, ValidationLevel validationLevel, ReportNode reportNode) {
        return checkSvcRegulator(validable, voltageSetpoint, reactivePowerSetpoint, regulationMode, checkValidationActionOnError(validationLevel), reportNode);
    }

    private static ValidationLevel checkSvcRegulator(Validable validable, double voltageSetpoint, double reactivePowerSetpoint,
                                                     StaticVarCompensator.RegulationMode regulationMode, ActionOnError actionOnError, ReportNode reportNode) {
        if (regulationMode == null) {
            throwExceptionOrLogError(validable, "Regulation mode is invalid", actionOnError, reportNode);
            return ValidationLevel.EQUIPMENT;
        }
        switch (regulationMode) {
            case VOLTAGE -> {
                if (Double.isNaN(voltageSetpoint)) {
                    throwExceptionOrLogErrorForInvalidValue(validable, voltageSetpoint, VOLTAGE_SETPOINT, actionOnError, reportNode);
                    return ValidationLevel.EQUIPMENT;
                }
            }
            case REACTIVE_POWER -> {
                if (Double.isNaN(reactivePowerSetpoint)) {
                    throwExceptionOrLogErrorForInvalidValue(validable, reactivePowerSetpoint, "reactive power setpoint", actionOnError, reportNode);
                    return ValidationLevel.EQUIPMENT;
                }
            }
            case OFF -> {
                // nothing to check
            }
        }
        return ValidationLevel.STEADY_STATE_HYPOTHESIS;
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

    private static ValidationLevel errorOrWarningForRtc(Validable validable, boolean loadTapChangingCapabilities, String message, ActionOnError actionOnError, ReportNode reportNode) {
        if (loadTapChangingCapabilities) {
            throwExceptionOrLogError(validable, message, actionOnError, reportNode);
            return ValidationLevel.EQUIPMENT;
        }
        reportNode.newReportNode()
                .withMessageTemplate(validable.getMessageHeader(), message)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
        LOGGER.warn("{}{}", validable.getMessageHeader(), message);
        return ValidationLevel.STEADY_STATE_HYPOTHESIS;
    }

    public static ValidationLevel checkRatioTapChangerRegulation(Validable validable, boolean regulating, boolean loadTapChangingCapabilities,
                                                                 Terminal regulationTerminal, RatioTapChanger.RegulationMode regulationMode,
                                                                 double regulationValue, Network network, ValidationLevel validationLevel, ReportNode reportNode) {
        return checkRatioTapChangerRegulation(validable, regulating, loadTapChangingCapabilities, regulationTerminal, regulationMode, regulationValue, network, checkValidationActionOnError(validationLevel), reportNode);
    }

    private static ValidationLevel checkRatioTapChangerRegulation(Validable validable, boolean regulating, boolean loadTapChangingCapabilities,
                                                                 Terminal regulationTerminal, RatioTapChanger.RegulationMode regulationMode,
                                                                 double regulationValue, Network network, ActionOnError actionOnError,
                                                                 ReportNode reportNode) {
        ValidationLevel validationLevel = ValidationLevel.STEADY_STATE_HYPOTHESIS;
        if (regulating) {
            if (Objects.isNull(regulationMode)) {
                validationLevel = ValidationLevel.min(validationLevel, errorOrWarningForRtc(validable, loadTapChangingCapabilities, "regulation mode of regulating ratio tap changer must be given", actionOnError, reportNode));
            }
            if (Double.isNaN(regulationValue)) {
                validationLevel = ValidationLevel.min(validationLevel, errorOrWarningForRtc(validable, loadTapChangingCapabilities, "a regulation value has to be set for a regulating ratio tap changer", actionOnError, reportNode));
            }
            if (regulationMode == RatioTapChanger.RegulationMode.VOLTAGE && regulationValue <= 0) {
                throw new ValidationException(validable, "bad target voltage " + regulationValue);
            }
            if (regulationTerminal == null) {
                validationLevel = ValidationLevel.min(validationLevel, errorOrWarningForRtc(validable, loadTapChangingCapabilities, "a regulation terminal has to be set for a regulating ratio tap changer", actionOnError, reportNode));
            }
        }
        if (regulationTerminal != null && regulationTerminal.getVoltageLevel().getNetwork() != network) {
            throw new ValidationException(validable, "regulation terminal is not part of the network");
        }
        return validationLevel;
    }

    public static ValidationLevel checkPhaseTapChangerRegulation(Validable validable, PhaseTapChanger.RegulationMode regulationMode,
                                                                 double regulationValue, boolean regulating, Terminal regulationTerminal,
                                                                 Network network, ValidationLevel validationLevel, ReportNode reportNode) {
        return checkPhaseTapChangerRegulation(validable, regulationMode, regulationValue, regulating, regulationTerminal,
                network, checkValidationActionOnError(validationLevel), reportNode);
    }

    private static ValidationLevel checkPhaseTapChangerRegulation(Validable validable, PhaseTapChanger.RegulationMode regulationMode,
                                                                  double regulationValue, boolean regulating, Terminal regulationTerminal,
                                                                  Network network, ActionOnError actionOnError, ReportNode reportNode) {
        ValidationLevel validationLevel = ValidationLevel.STEADY_STATE_HYPOTHESIS;
        if (regulationMode == null) {
            throwExceptionOrLogError(validable, "phase regulation mode is not set", actionOnError, reportNode);
            validationLevel = ValidationLevel.min(validationLevel, ValidationLevel.EQUIPMENT);
        }
        if (regulating && regulationMode != null) {
            validationLevel = checkRegulatingPhaseTapChanger(validable, regulationMode, regulationValue, regulationTerminal, actionOnError, reportNode);
        }
        if (regulationTerminal != null && regulationTerminal.getVoltageLevel().getNetwork() != network) {
            throw new ValidationException(validable, "phase regulation terminal is not part of the network");
        }
        return validationLevel;
    }

    private static ValidationLevel checkRegulatingPhaseTapChanger(Validable validable,
            PhaseTapChanger.RegulationMode regulationMode,
            double regulationValue, Terminal regulationTerminal,
            ActionOnError actionOnError, ReportNode reportNode) {
        ValidationLevel validationLevel = ValidationLevel.STEADY_STATE_HYPOTHESIS;
        if (regulationMode != PhaseTapChanger.RegulationMode.FIXED_TAP && Double.isNaN(regulationValue)) {
            throwExceptionOrLogError(validable, "phase regulation is on and threshold/setpoint value is not set",
                    actionOnError, reportNode);
            validationLevel = ValidationLevel.min(validationLevel, ValidationLevel.EQUIPMENT);
        }
        if (regulationMode != PhaseTapChanger.RegulationMode.FIXED_TAP && regulationTerminal == null) {
            throwExceptionOrLogError(validable, "phase regulation is on and regulated terminal is not set",
                    actionOnError, reportNode);
            validationLevel = ValidationLevel.min(validationLevel, ValidationLevel.EQUIPMENT);
        }
        if (regulationMode == PhaseTapChanger.RegulationMode.FIXED_TAP) {
            throwExceptionOrLogError(validable, "phase regulation cannot be on if mode is FIXED", actionOnError,
                    reportNode);
            validationLevel = ValidationLevel.min(validationLevel, ValidationLevel.EQUIPMENT);
        }
        if (regulationMode == PhaseTapChanger.RegulationMode.CURRENT_LIMITER && regulationValue < 0) {
            throwExceptionOrLogError(validable,
                    "phase tap changer in CURRENT_LIMITER mode must have a non-negative regulation value",
                    actionOnError, reportNode);
            validationLevel = ValidationLevel.min(validationLevel, ValidationLevel.EQUIPMENT);
        }
        return validationLevel;
    }

    public static ValidationLevel checkOnlyOneTapChangerRegulatingEnabled(Validable validable, Set<TapChanger<?, ?, ?, ?>> tapChangersNotIncludingTheModified,
                                                                           boolean regulating, ValidationLevel validationLevel, ReportNode reportNode) {
        return checkOnlyOneTapChangerRegulatingEnabled(validable, tapChangersNotIncludingTheModified, regulating, checkValidationActionOnError(validationLevel), reportNode);
    }

    private static ValidationLevel checkOnlyOneTapChangerRegulatingEnabled(Validable validable, Set<TapChanger<?, ?, ?, ?>> tapChangersNotIncludingTheModified,
                                                                           boolean regulating, ActionOnError actionOnError, ReportNode reportNode) {
        if (regulating && tapChangersNotIncludingTheModified.stream().anyMatch(TapChanger::isRegulating)) {
            throwExceptionOrLogError(validable, UNIQUE_REGULATING_TAP_CHANGER_MSG, actionOnError, reportNode);
            return ValidationLevel.EQUIPMENT;
        }
        return ValidationLevel.STEADY_STATE_HYPOTHESIS;
    }

    public static ValidationLevel checkConvertersMode(Validable validable, HvdcLine.ConvertersMode converterMode,
                                                      ValidationLevel validationLevel, ReportNode reportNode) {
        return checkConvertersMode(validable, converterMode, checkValidationActionOnError(validationLevel), reportNode);
    }

    private static ValidationLevel checkConvertersMode(Validable validable, HvdcLine.ConvertersMode converterMode,
                                                       ActionOnError actionOnError, ReportNode reportNode) {
        if (converterMode == null) {
            throwExceptionOrLogError(validable, "converter mode is invalid", actionOnError, reportNode);
            return ValidationLevel.EQUIPMENT;
        }
        return ValidationLevel.STEADY_STATE_HYPOTHESIS;
    }

    public static void checkPowerFactor(Validable validable, double powerFactor) {
        if (Double.isNaN(powerFactor)) {
            throw new ValidationException(validable, "power factor is invalid");
        } else if (Math.abs(powerFactor) > 1) {
            throw new ValidationException(validable, "power factor is invalid, it should be between -1 and 1");
        }
    }

    public static ValidationLevel checkLoadingLimits(Validable validable, double permanentLimit, Collection<LoadingLimits.TemporaryLimit> temporaryLimits,
                                                     ValidationLevel validationLevel, ReportNode reportNode) {
        return checkLoadingLimits(validable, permanentLimit, temporaryLimits, checkValidationActionOnError(validationLevel), reportNode);
    }

    private static ValidationLevel checkLoadingLimits(Validable validable, double permanentLimit, Collection<LoadingLimits.TemporaryLimit> temporaryLimits,
                                                      ActionOnError actionOnError, ReportNode reportNode) {
        ValidationLevel validationLevel = ValidationUtil.checkPermanentLimit(validable, permanentLimit, temporaryLimits, actionOnError, reportNode);
        ValidationUtil.checkTemporaryLimits(validable, permanentLimit, temporaryLimits);
        return validationLevel;
    }

    public static ValidationLevel checkPermanentLimit(Validable validable, double permanentLimit, Collection<LoadingLimits.TemporaryLimit> temporaryLimits,
                                                      ValidationLevel validationLevel, ReportNode reportNode) {
        return checkPermanentLimit(validable, permanentLimit, temporaryLimits, checkValidationActionOnError(validationLevel), reportNode);
    }

    private static ValidationLevel checkPermanentLimit(Validable validable, double permanentLimit, Collection<LoadingLimits.TemporaryLimit> temporaryLimits,
                                                       ActionOnError actionOnError, ReportNode reportNode) {
        ValidationLevel validationLevel = ValidationLevel.STEADY_STATE_HYPOTHESIS;
        if (Double.isNaN(permanentLimit) && !temporaryLimits.isEmpty()) {
            throwExceptionOrLogError(validable, "permanent limit must be defined if temporary limits are present", actionOnError, reportNode);
            validationLevel = ValidationLevel.min(validationLevel, ValidationLevel.EQUIPMENT);
        }
        if (permanentLimit < 0) {
            // because it is forbidden for SSH and EQ validation levels.
            throw new ValidationException(validable, "permanent limit must be >= 0");
        }
        if (permanentLimit == 0) {
            // log if zero
            LOGGER.info("{}permanent limit is set to 0", validable.getMessageHeader());
        }

        return validationLevel;
    }

    private static void checkTemporaryLimits(Validable validable, double permanentLimit, Collection<LoadingLimits.TemporaryLimit> temporaryLimits) {
        // check temporary limits are consistent with permanent
        if (LOGGER.isDebugEnabled()) {
            double previousLimit = Double.NaN;
            boolean wrongOrderMessageAlreadyLogged = false;
            for (LoadingLimits.TemporaryLimit tl : temporaryLimits) { // iterate in ascending order
                if (tl.getValue() <= permanentLimit) {
                    LOGGER.debug("{}, temporary limit should be greater than permanent limit", validable.getMessageHeader());
                }
                if (!wrongOrderMessageAlreadyLogged && !Double.isNaN(previousLimit) && tl.getValue() <= previousLimit) {
                    LOGGER.debug("{} : temporary limits should be in ascending value order", validable.getMessageHeader());
                    wrongOrderMessageAlreadyLogged = true;
                }
                previousLimit = tl.getValue();
            }
        }
    }

    public static ValidationLevel checkLossFactor(Validable validable, float lossFactor, ValidationLevel validationLevel, ReportNode reportNode) {
        return checkLossFactor(validable, lossFactor, checkValidationActionOnError(validationLevel), reportNode);
    }

    private static ValidationLevel checkLossFactor(Validable validable, float lossFactor, ActionOnError actionOnError, ReportNode reportNode) {
        ValidationLevel validationLevel = ValidationLevel.STEADY_STATE_HYPOTHESIS;
        if (Double.isNaN(lossFactor)) {
            throwExceptionOrLogError(validable, "loss factor is invalid is undefined", actionOnError, reportNode);
            validationLevel = ValidationLevel.min(validationLevel, ValidationLevel.EQUIPMENT);
        } else if (lossFactor < 0 || lossFactor > 100) {
            throw new ValidationException(validable, "loss factor must be >= 0 and <= 100");
        }
        return validationLevel;
    }

    private static ValidationLevel checkRtc(Validable validable, RatioTapChanger rtc, Network network, ActionOnError actionOnError, ReportNode reportNode) {
        ValidationLevel validationLevel = ValidationLevel.STEADY_STATE_HYPOTHESIS;
        if (rtc.findTapPosition().isEmpty()) {
            throwExceptionOrLogError(validable, "tap position is not set", actionOnError, reportNode);
            validationLevel = ValidationLevel.min(validationLevel, ValidationLevel.EQUIPMENT);
        }
        validationLevel = ValidationLevel.min(validationLevel, checkRatioTapChangerRegulation(validable, rtc.isRegulating(), rtc.hasLoadTapChangingCapabilities(), rtc.getRegulationTerminal(), rtc.getRegulationMode(), rtc.getRegulationValue(), network, actionOnError, reportNode));
        validationLevel = ValidationLevel.min(validationLevel, checkTargetDeadband(validable, "ratio tap changer", rtc.isRegulating(), rtc.getTargetDeadband(), actionOnError, reportNode));
        return validationLevel;
    }

    private static ValidationLevel checkPtc(Validable validable, PhaseTapChanger ptc, Network network, ActionOnError actionOnError, ReportNode reportNode) {
        ValidationLevel validationLevel = ValidationLevel.STEADY_STATE_HYPOTHESIS;
        if (ptc.findTapPosition().isEmpty()) {
            throwExceptionOrLogError(validable, "tap position is not set", actionOnError, reportNode);
            validationLevel = ValidationLevel.min(validationLevel, ValidationLevel.EQUIPMENT);
        }
        validationLevel = ValidationLevel.min(validationLevel, checkPhaseTapChangerRegulation(validable, ptc.getRegulationMode(), ptc.getRegulationValue(), ptc.isRegulating(), ptc.getRegulationTerminal(), network, actionOnError, reportNode));
        validationLevel = ValidationLevel.min(validationLevel, checkTargetDeadband(validable, "phase tap changer", ptc.isRegulating(), ptc.getTargetDeadband(), actionOnError, reportNode));
        return validationLevel;
    }

    private static ValidationLevel checkThreeWindingsTransformer(Validable validable, ThreeWindingsTransformer twt, ActionOnError actionOnError, ReportNode reportNode) {
        ValidationLevel validationLevel = ValidationLevel.STEADY_STATE_HYPOTHESIS;
        for (ThreeWindingsTransformer.Leg leg : twt.getLegs()) {
            if (leg.hasRatioTapChanger()) {
                validationLevel = ValidationLevel.min(validationLevel, checkRtc(validable, leg.getRatioTapChanger(), twt.getNetwork(), actionOnError, reportNode));
            }
            if (leg.hasPhaseTapChanger()) {
                validationLevel = ValidationLevel.min(validationLevel, checkPtc(validable, leg.getPhaseTapChanger(), twt.getNetwork(), actionOnError, reportNode));
            }
        }
        long regulatingTc = twt.getLegStream()
                .map(ThreeWindingsTransformer.Leg::getRatioTapChanger)
                .filter(Objects::nonNull)
                .filter(TapChanger::isRegulating)
                .count()
                + twt.getLegStream()
                .map(ThreeWindingsTransformer.Leg::getPhaseTapChanger)
                .filter(Objects::nonNull)
                .filter(TapChanger::isRegulating)
                .count();
        if (regulatingTc > 1) {
            throw new ValidationException(validable, UNIQUE_REGULATING_TAP_CHANGER_MSG);
        }
        validationLevel = checkOperationalLimitsGroups(validable, twt.getLeg1().getOperationalLimitsGroups(), validationLevel, actionOnError, reportNode);
        validationLevel = checkOperationalLimitsGroups(validable, twt.getLeg2().getOperationalLimitsGroups(), validationLevel, actionOnError, reportNode);
        validationLevel = checkOperationalLimitsGroups(validable, twt.getLeg3().getOperationalLimitsGroups(), validationLevel, actionOnError, reportNode);
        return validationLevel;
    }

    private static ValidationLevel checkTwoWindingsTransformer(Validable validable, TwoWindingsTransformer twt, ActionOnError actionOnError, ReportNode reportNode) {
        ValidationLevel validationLevel = ValidationLevel.STEADY_STATE_HYPOTHESIS;
        if (twt.hasRatioTapChanger()) {
            validationLevel = ValidationLevel.min(validationLevel, checkRtc(validable, twt.getRatioTapChanger(), twt.getNetwork(), actionOnError, reportNode));
        }
        if (twt.hasPhaseTapChanger()) {
            validationLevel = ValidationLevel.min(validationLevel, checkPtc(validable, twt.getPhaseTapChanger(), twt.getNetwork(), actionOnError, reportNode));
        }
        if (twt.getOptionalRatioTapChanger().map(TapChanger::isRegulating).orElse(false)
                && twt.getOptionalPhaseTapChanger().map(TapChanger::isRegulating).orElse(false)) {
            throw new ValidationException(validable, UNIQUE_REGULATING_TAP_CHANGER_MSG);
        }
        validationLevel = checkOperationalLimitsGroups(validable, twt.getOperationalLimitsGroups1(), validationLevel, actionOnError, reportNode);
        validationLevel = checkOperationalLimitsGroups(validable, twt.getOperationalLimitsGroups2(), validationLevel, actionOnError, reportNode);
        return validationLevel;
    }

    private static ValidationLevel checkIdentifiable(Identifiable<?> identifiable, ValidationLevel previous, ActionOnError actionOnError, ReportNode reportNode) {
        ValidationLevel validationLevel = previous;
        if (identifiable instanceof Validable validable) {
            if (identifiable instanceof Battery battery) {
                validationLevel = ValidationLevel.min(validationLevel, checkP0(validable, battery.getTargetP(), actionOnError, reportNode));
                validationLevel = ValidationLevel.min(validationLevel, checkQ0(validable, battery.getTargetQ(), actionOnError, reportNode));
            } else if (identifiable instanceof DanglingLine danglingLine) {
                validationLevel = ValidationLevel.min(validationLevel, checkP0(validable, danglingLine.getP0(), actionOnError, reportNode));
                validationLevel = ValidationLevel.min(validationLevel, checkQ0(validable, danglingLine.getQ0(), actionOnError, reportNode));
                validationLevel = checkGenerationOnDanglingLine(validationLevel, validable, danglingLine, actionOnError, reportNode);
                validationLevel = checkOperationalLimitsGroups(validable, danglingLine.getOperationalLimitsGroups(), validationLevel, actionOnError, reportNode);
            } else if (identifiable instanceof Generator generator) {
                validationLevel = ValidationLevel.min(validationLevel, checkActivePowerSetpoint(validable, generator.getTargetP(), actionOnError, reportNode));
                validationLevel = ValidationLevel.min(validationLevel, checkVoltageControl(validable, generator.isVoltageRegulatorOn(), generator.getTargetV(), generator.getTargetQ(), actionOnError, reportNode));
            } else if (identifiable instanceof HvdcLine hvdcLine) {
                validationLevel = ValidationLevel.min(validationLevel, checkConvertersMode(validable, hvdcLine.getConvertersMode(), actionOnError, reportNode));
                validationLevel = ValidationLevel.min(validationLevel, checkHvdcActivePowerSetpoint(validable, hvdcLine.getActivePowerSetpoint(), actionOnError, reportNode));
            } else if (identifiable instanceof Load load) {
                validationLevel = ValidationLevel.min(validationLevel, checkP0(validable, load.getP0(), actionOnError, reportNode));
                validationLevel = ValidationLevel.min(validationLevel, checkQ0(validable, load.getQ0(), actionOnError, reportNode));
            } else if (identifiable instanceof ShuntCompensator shunt) {
                validationLevel = ValidationLevel.min(validationLevel, checkVoltageControl(validable, shunt.isVoltageRegulatorOn(), shunt.getTargetV(), actionOnError, reportNode));
                validationLevel = ValidationLevel.min(validationLevel, checkTargetDeadband(validable, "shunt compensator", shunt.isVoltageRegulatorOn(), shunt.getTargetDeadband(), actionOnError, reportNode));
                validationLevel = ValidationLevel.min(validationLevel, checkSections(validable, getSectionCount(shunt), shunt.getMaximumSectionCount(), actionOnError, reportNode));
            } else if (identifiable instanceof StaticVarCompensator svc) {
                validationLevel = ValidationLevel.min(validationLevel, checkSvcRegulator(validable, svc.getVoltageSetpoint(), svc.getReactivePowerSetpoint(), svc.getRegulationMode(), actionOnError, reportNode));
            } else if (identifiable instanceof ThreeWindingsTransformer twt) {
                validationLevel = ValidationLevel.min(validationLevel, checkThreeWindingsTransformer(validable, twt, actionOnError, reportNode));
            } else if (identifiable instanceof TwoWindingsTransformer twt) {
                validationLevel = ValidationLevel.min(validationLevel, checkTwoWindingsTransformer(validable, twt, actionOnError, reportNode));
            } else if (identifiable instanceof VscConverterStation converterStation) {
                validationLevel = ValidationLevel.min(validationLevel, checkVoltageControl(validable, converterStation.isVoltageRegulatorOn(), converterStation.getVoltageSetpoint(), converterStation.getReactivePowerSetpoint(), actionOnError, reportNode));
            } else if (identifiable instanceof Branch<?> branch) {
                validationLevel = checkOperationalLimitsGroups(validable, branch.getOperationalLimitsGroups1(), validationLevel, actionOnError, reportNode);
                validationLevel = checkOperationalLimitsGroups(validable, branch.getOperationalLimitsGroups2(), validationLevel, actionOnError, reportNode);
            }
        }
        return validationLevel;
    }

    private static ValidationLevel checkGenerationOnDanglingLine(ValidationLevel previous, Validable validable, DanglingLine danglingLine, ActionOnError actionOnError, ReportNode reportNode) {
        ValidationLevel validationLevel = previous;
        DanglingLine.Generation generation = danglingLine.getGeneration();
        if (generation != null) {
            validationLevel = ValidationLevel.min(validationLevel, checkActivePowerSetpoint(validable, generation.getTargetP(), actionOnError, reportNode));
            validationLevel = ValidationLevel.min(validationLevel, checkVoltageControl(validable, generation.isVoltageRegulationOn(), generation.getTargetV(), generation.getTargetQ(), actionOnError, reportNode));
        }
        return validationLevel;
    }

    private static Integer getSectionCount(ShuntCompensator shunt) {
        return shunt.findSectionCount().isPresent() ? shunt.getSectionCount() : null;
    }

    private static ValidationLevel checkOperationalLimitsGroups(Validable validable, Collection<OperationalLimitsGroup> operationalLimitsGroupCollection, ValidationLevel previous, ActionOnError actionOnError, ReportNode reportNode) {
        ValidationLevel validationLevel = previous;
        for (OperationalLimitsGroup group : operationalLimitsGroupCollection) {
            validationLevel = checkOperationalLimitsGroup(validable, group, validationLevel, actionOnError, reportNode);
        }
        return validationLevel;
    }

    private static ValidationLevel checkOperationalLimitsGroup(Validable validable, OperationalLimitsGroup operationalLimitsGroup, ValidationLevel previous, ActionOnError actionOnError, ReportNode reportNode) {
        ValidationLevel[] validationLevel = new ValidationLevel[1];
        validationLevel[0] = previous;
        operationalLimitsGroup.getCurrentLimits().ifPresent(l -> validationLevel[0] = checkLoadingLimits(validable, l, validationLevel[0], actionOnError, reportNode));
        operationalLimitsGroup.getApparentPowerLimits().ifPresent(l -> validationLevel[0] = checkLoadingLimits(validable, l, validationLevel[0], actionOnError, reportNode));
        operationalLimitsGroup.getActivePowerLimits().ifPresent(l -> validationLevel[0] = checkLoadingLimits(validable, l, validationLevel[0], actionOnError, reportNode));
        return validationLevel[0];
    }

    private static ValidationLevel checkLoadingLimits(Validable validable, LoadingLimits limits, ValidationLevel validationLevel, ActionOnError actionOnError, ReportNode reportNode) {
        return ValidationLevel.min(validationLevel, checkLoadingLimits(validable, limits.getPermanentLimit(), limits.getTemporaryLimits(), actionOnError, reportNode));
    }

    public static ValidationLevel validate(Collection<Identifiable<?>> identifiables, boolean allChecks, ActionOnError actionOnError, ValidationLevel previous, ReportNode reportNode) {
        Objects.requireNonNull(identifiables);
        Objects.requireNonNull(previous);
        Objects.requireNonNull(reportNode);
        if (checkValidationLevel(previous)) {
            return previous;
        }
        ValidationLevel validationLevel = ValidationLevel.STEADY_STATE_HYPOTHESIS;
        for (Identifiable<?> identifiable : identifiables) {
            validationLevel = checkIdentifiable(identifiable, validationLevel, actionOnError, reportNode);
            if (!allChecks && validationLevel == ValidationLevel.MINIMUM_VALUE) {
                return validationLevel;
            }
        }
        return validationLevel;
    }

    private static boolean checkValidationLevel(ValidationLevel validationLevel) {
        return validationLevel.compareTo(ValidationLevel.STEADY_STATE_HYPOTHESIS) >= 0;
    }

    private static ActionOnError checkValidationActionOnError(ValidationLevel validationLevel) {
        return validationLevel.compareTo(ValidationLevel.STEADY_STATE_HYPOTHESIS) >= 0 ? ActionOnError.THROW_EXCEPTION : ActionOnError.IGNORE;
    }

}
