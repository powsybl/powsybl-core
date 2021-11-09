/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import com.powsybl.commons.reporter.Report;
import com.powsybl.commons.reporter.Reporter;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class ValidationUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationUtil.class);

    private static final String ACTIVE_POWER_SETPOINT = "active power setpoint";
    private static final String MAXIMUM_P = "maximum P";
    private static final String UNIQUE_REGULATING_TAP_CHANGER_MSG = "Only one regulating control enabled is allowed";

    private ValidationUtil() {
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

    private static void logError(Validable validable, String message, Reporter reporter) {
        reporter.report(Report.builder()
                .withKey(validable.getMessageHeader())
                .withDefaultMessage(message)
                .withSeverity(IidmReportConstants.ERROR_SEVERITY)
                .build());
        LOGGER.error("{}{}", validable.getMessageHeader(), message);
    }

    public static void throwExceptionOrLogError(Validable validable, String message, boolean throwException) {
        throwExceptionOrLogError(validable, message, throwException, Reporter.NO_OP);
    }

    public static void throwExceptionOrLogError(Validable validable, String message, boolean throwException, Reporter reporter) {
        if (throwException) {
            throw new ValidationException(validable, message);
        }
        logError(validable, message, reporter);
    }

    private static void throwExceptionOrLogErrorForInvalidValue(Validable validable, double value, String valueName, boolean throwException, Reporter reporter) {
        throwExceptionOrLogErrorForInvalidValue(validable, value, valueName, null, throwException, reporter);
    }

    private static void throwExceptionOrLogErrorForInvalidValue(Validable validable, double value, String valueName, String reason, boolean throwException, Reporter reporter) {
        if (throwException) {
            throw createInvalidValueException(validable, value, valueName, reason);
        }
        logError(validable, createInvalidValueMessage(value, valueName, reason), reporter);
    }

    public static void checkActivePowerSetpoint(Validable validable, double activePowerSetpoint) {
        checkActivePowerSetpoint(validable, activePowerSetpoint, true);
    }

    public static ValidationLevel checkActivePowerSetpoint(Validable validable, double activePowerSetpoint, boolean throwException) {
        return checkActivePowerSetpoint(validable, activePowerSetpoint, throwException, Reporter.NO_OP);
    }

    public static ValidationLevel checkActivePowerSetpoint(Validable validable, double activePowerSetpoint, boolean throwException, Reporter reporter) {
        if (Double.isNaN(activePowerSetpoint)) {
            throwExceptionOrLogErrorForInvalidValue(validable, activePowerSetpoint, ACTIVE_POWER_SETPOINT, throwException, reporter);
            return ValidationLevel.STATE_ESTIMATION;
        }
        return ValidationLevel.LOADFLOW;
    }

    public static void checkHvdcActivePowerSetpoint(Validable validable, double activePowerSetpoint) {
        checkHvdcActivePowerSetpoint(validable, activePowerSetpoint, true);
    }

    public static ValidationLevel checkHvdcActivePowerSetpoint(Validable validable, double activePowerSetpoint, boolean throwException) {
        return checkHvdcActivePowerSetpoint(validable, activePowerSetpoint, throwException, Reporter.NO_OP);
    }

    public static ValidationLevel checkHvdcActivePowerSetpoint(Validable validable, double activePowerSetpoint, boolean throwException, Reporter reporter) {
        if (Double.isNaN(activePowerSetpoint)) {
            throwExceptionOrLogErrorForInvalidValue(validable, activePowerSetpoint, ACTIVE_POWER_SETPOINT, throwException, reporter);
            return ValidationLevel.STATE_ESTIMATION;
        } else if (activePowerSetpoint < 0) {
            throwExceptionOrLogErrorForInvalidValue(validable, activePowerSetpoint, ACTIVE_POWER_SETPOINT, "active power setpoint should not be negative", throwException, reporter);
            return ValidationLevel.STATE_ESTIMATION;
        }
        return ValidationLevel.LOADFLOW;
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

    public static void checkTargetDeadband(Validable validable, String validableType, boolean regulating, double targetDeadband) {
        checkTargetDeadband(validable, validableType, regulating, targetDeadband, true);
    }

    public static ValidationLevel checkTargetDeadband(Validable validable, String validableType, boolean regulating, double targetDeadband, boolean throwException) {
        return checkTargetDeadband(validable, validableType, regulating, targetDeadband, throwException, Reporter.NO_OP);
    }

    public static ValidationLevel checkTargetDeadband(Validable validable, String validableType, boolean regulating, double targetDeadband, boolean throwException, Reporter reporter) {
        ValidationLevel validationLevel = ValidationLevel.LOADFLOW;
        if (regulating && Double.isNaN(targetDeadband)) {
            throwExceptionOrLogError(validable, "Undefined value for target deadband of regulating " + validableType, throwException, reporter);
            validationLevel = ValidationLevel.min(validationLevel, ValidationLevel.STATE_ESTIMATION);
        }
        if (targetDeadband < 0) {
            throwExceptionOrLogError(validable, "Unexpected value for target deadband of " + validableType + ": " + targetDeadband + " < 0", throwException, reporter);
            validationLevel = ValidationLevel.min(validationLevel, ValidationLevel.STATE_ESTIMATION);
        }
        return ValidationLevel.LOADFLOW;
    }

    public static ValidationLevel checkVoltageControl(Validable validable, Boolean voltageRegulatorOn, double voltageSetpoint, boolean throwException) {
        return checkVoltageControl(validable, voltageRegulatorOn, voltageSetpoint, throwException, Reporter.NO_OP);
    }

    public static ValidationLevel checkVoltageControl(Validable validable, Boolean voltageRegulatorOn, double voltageSetpoint, boolean throwException, Reporter reporter) {
        if (voltageRegulatorOn == null) {
            throwExceptionOrLogError(validable, "voltage regulator status is not set", throwException, reporter);
            return ValidationLevel.STATE_ESTIMATION;
        }
        if (voltageRegulatorOn) {
            if (Double.isNaN(voltageSetpoint) || voltageSetpoint <= 0) {
                throwExceptionOrLogErrorForInvalidValue(validable, voltageSetpoint, "voltage setpoint", "voltage regulator is on", throwException, reporter);
                return ValidationLevel.STATE_ESTIMATION;
            }
        }
        return ValidationLevel.LOADFLOW;
    }

    public static void checkVoltageControl(Validable validable, Boolean voltageRegulatorOn, double voltageSetpoint, double reactivePowerSetpoint) {
        checkVoltageControl(validable, voltageRegulatorOn, voltageSetpoint, reactivePowerSetpoint, true);
    }

    public static ValidationLevel checkVoltageControl(Validable validable, Boolean voltageRegulatorOn, double voltageSetpoint, double reactivePowerSetpoint, boolean throwException) {
        return checkVoltageControl(validable, voltageRegulatorOn, voltageSetpoint, reactivePowerSetpoint, throwException, Reporter.NO_OP);
    }

    public static ValidationLevel checkVoltageControl(Validable validable, Boolean voltageRegulatorOn, double voltageSetpoint, double reactivePowerSetpoint, boolean throwException, Reporter reporter) {
        if (voltageRegulatorOn == null) {
            throwExceptionOrLogError(validable, "voltage regulator status is not set", throwException, reporter);
            return ValidationLevel.STATE_ESTIMATION;
        }
        if (voltageRegulatorOn) {
            if (Double.isNaN(voltageSetpoint) || voltageSetpoint <= 0) {
                throwExceptionOrLogErrorForInvalidValue(validable, voltageSetpoint, "voltage setpoint", "voltage regulator is on", throwException, reporter);
                return ValidationLevel.STATE_ESTIMATION;
            }
        } else if (Double.isNaN(reactivePowerSetpoint)) {
            throwExceptionOrLogErrorForInvalidValue(validable, reactivePowerSetpoint, "reactive power setpoint", "voltage regulator is off", throwException, reporter);
            return ValidationLevel.STATE_ESTIMATION;
        }
        return ValidationLevel.LOADFLOW;
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

    public static void checkP0(Validable validable, double p0) {
        checkP0(validable, p0, true);
    }

    public static ValidationLevel checkP0(Validable validable, double p0, boolean throwException) {
        return checkP0(validable, p0, throwException, Reporter.NO_OP);
    }

    public static ValidationLevel checkP0(Validable validable, double p0, boolean throwException, Reporter reporter) {
        if (Double.isNaN(p0)) {
            throwExceptionOrLogError(validable, "p0 is invalid", throwException, reporter);
            return ValidationLevel.STATE_ESTIMATION;
        }
        return ValidationLevel.LOADFLOW;
    }

    public static void checkQ0(Validable validable, double q0) {
        checkQ0(validable, q0, true);
    }

    public static ValidationLevel checkQ0(Validable validable, double q0, boolean throwException) {
        return checkQ0(validable, q0, throwException, Reporter.NO_OP);
    }

    public static ValidationLevel checkQ0(Validable validable, double q0, boolean throwException, Reporter reporter) {
        if (Double.isNaN(q0)) {
            throwExceptionOrLogError(validable, "q0 is invalid", throwException, reporter);
            return ValidationLevel.STATE_ESTIMATION;
        }
        return ValidationLevel.LOADFLOW;
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
            throw new ValidationException(validable, "the maximum number of section (" + maximumSectionCount
                    + ") should be greater than 0");
        }
    }

    public static void checkSections(Validable validable, int currentSectionCount, int maximumSectionCount) {
        checkSections(validable, currentSectionCount, maximumSectionCount, true);
    }

    public static ValidationLevel checkSections(Validable validable, int currentSectionCount, int maximumSectionCount, boolean throwException) {
        return checkSections(validable, currentSectionCount, maximumSectionCount, throwException, Reporter.NO_OP);
    }

    public static ValidationLevel checkSections(Validable validable, int currentSectionCount, int maximumSectionCount, boolean throwException, Reporter reporter) {
        ValidationLevel validationLevel = ValidationLevel.LOADFLOW;
        if (currentSectionCount < 0) {
            throwExceptionOrLogError(validable, "the current number of section (" + currentSectionCount
                    + ") should be greater than or equal to 0", throwException, reporter);
            validationLevel = ValidationLevel.min(validationLevel, ValidationLevel.STATE_ESTIMATION);
        }
        checkMaximumSectionCount(validable, maximumSectionCount);
        if (currentSectionCount > maximumSectionCount) {
            throwExceptionOrLogError(validable,
                    "the current number (" + currentSectionCount
                            + ") of section should be lesser than the maximum number of section ("
                            + maximumSectionCount + ")", throwException, reporter);
            validationLevel = ValidationLevel.min(validationLevel, ValidationLevel.STATE_ESTIMATION);
        }
        return validationLevel;
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

    public static void checkSvcRegulator(Validable validable, double voltageSetpoint, double reactivePowerSetpoint, StaticVarCompensator.RegulationMode regulationMode) {
        checkSvcRegulator(validable, voltageSetpoint, reactivePowerSetpoint, regulationMode, true);
    }

    public static ValidationLevel checkSvcRegulator(Validable validable, double voltageSetpoint, double reactivePowerSetpoint, StaticVarCompensator.RegulationMode regulationMode, boolean throwException) {
        return checkSvcRegulator(validable, voltageSetpoint, reactivePowerSetpoint, regulationMode, throwException, Reporter.NO_OP);
    }

    public static ValidationLevel checkSvcRegulator(Validable validable, double voltageSetpoint, double reactivePowerSetpoint,
                                         StaticVarCompensator.RegulationMode regulationMode, boolean throwException, Reporter reporter) {
        if (regulationMode == null) {
            throwExceptionOrLogError(validable, "Regulation mode is invalid", throwException, reporter);
            return ValidationLevel.STATE_ESTIMATION;
        }
        switch (regulationMode) {
            case VOLTAGE:
                if (Double.isNaN(voltageSetpoint)) {
                    throwExceptionOrLogErrorForInvalidValue(validable, voltageSetpoint, "voltage setpoint", throwException, reporter);
                    return ValidationLevel.STATE_ESTIMATION;
                }
                break;

            case REACTIVE_POWER:
                if (Double.isNaN(reactivePowerSetpoint)) {
                    throwExceptionOrLogErrorForInvalidValue(validable, reactivePowerSetpoint, "reactive power setpoint", throwException, reporter);
                    return ValidationLevel.STATE_ESTIMATION;
                }
                break;

            case OFF:
                // nothing to check
                break;

            default:
                throw new AssertionError();
        }
        return ValidationLevel.LOADFLOW;
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

    private static ValidationLevel errorOrWarningForRtc(Validable validable, boolean loadTapChangingCapabilities, String message, boolean throwException, Reporter reporter) {
        if (loadTapChangingCapabilities) {
            throwExceptionOrLogError(validable, message, throwException, reporter);
            return ValidationLevel.STATE_ESTIMATION;
        } else {
            reporter.report(Report.builder()
                    .withKey(validable.getMessageHeader())
                    .withDefaultMessage(message)
                    .withSeverity(IidmReportConstants.WARN_SEVERITY)
                    .build());
            LOGGER.warn("{}{}", validable.getMessageHeader(), message);
        }
        return ValidationLevel.LOADFLOW;
    }

    public static void checkRatioTapChangerRegulation(Validable validable, boolean regulating, boolean loadTapChangingCapabilities,
                                                      Terminal regulationTerminal, double targetV, Network network) {
        checkRatioTapChangerRegulation(validable, regulating, loadTapChangingCapabilities, regulationTerminal, targetV, network, true);
    }

    public static ValidationLevel checkRatioTapChangerRegulation(Validable validable, boolean regulating, boolean loadTapChangingCapabilities,
                                                      Terminal regulationTerminal, double targetV, Network network, boolean throwException) {
        return checkRatioTapChangerRegulation(validable, regulating, loadTapChangingCapabilities, regulationTerminal, targetV, network, throwException, Reporter.NO_OP);
    }

    public static ValidationLevel checkRatioTapChangerRegulation(Validable validable, boolean regulating, boolean loadTapChangingCapabilities,
                                                      Terminal regulationTerminal, double targetV, Network network, boolean throwException,
                                                      Reporter reporter) {
        ValidationLevel validationLevel = ValidationLevel.LOADFLOW;
        if (regulating) {
            if (Double.isNaN(targetV)) {
                validationLevel = ValidationLevel.min(validationLevel, errorOrWarningForRtc(validable, loadTapChangingCapabilities, "a target voltage has to be set for a regulating ratio tap changer", throwException, reporter));
            }
            if (targetV <= 0) {
                validationLevel = ValidationLevel.min(validationLevel, errorOrWarningForRtc(validable, loadTapChangingCapabilities, "bad target voltage " + targetV, throwException, reporter));
            }
            if (regulationTerminal == null) {
                validationLevel = ValidationLevel.min(validationLevel, errorOrWarningForRtc(validable, loadTapChangingCapabilities, "a regulation terminal has to be set for a regulating ratio tap changer", throwException, reporter));
            }
            if (regulationTerminal != null && regulationTerminal.getVoltageLevel().getNetwork() != network) {
                validationLevel = ValidationLevel.min(validationLevel, errorOrWarningForRtc(validable, loadTapChangingCapabilities, "regulation terminal is not part of the network", throwException, reporter));
            }
        }
        return validationLevel;
    }

    public static void checkRatioTapChangerRegulation(Validable validable, boolean regulating,
                                                      Terminal regulationTerminal, double targetV, Network network) {
        checkRatioTapChangerRegulation(validable, regulating, true, regulationTerminal, targetV, network);
    }

    public static void checkPhaseTapChangerRegulation(Validable validable, PhaseTapChanger.RegulationMode regulationMode,
                                                      double regulationValue, boolean regulating, Terminal regulationTerminal,
                                                      Network network) {
        checkPhaseTapChangerRegulation(validable, regulationMode, regulationValue, regulating, regulationTerminal, network, true);
    }

    public static ValidationLevel checkPhaseTapChangerRegulation(Validable validable, PhaseTapChanger.RegulationMode regulationMode,
                                                      double regulationValue, boolean regulating, Terminal regulationTerminal,
                                                      Network network, boolean throwException) {
        return checkPhaseTapChangerRegulation(validable, regulationMode, regulationValue, regulating, regulationTerminal, network, throwException, Reporter.NO_OP);
    }

    public static ValidationLevel checkPhaseTapChangerRegulation(Validable validable, PhaseTapChanger.RegulationMode regulationMode,
                                                      double regulationValue, boolean regulating, Terminal regulationTerminal,
                                                      Network network, boolean throwException, Reporter reporter) {
        ValidationLevel validationLevel = ValidationLevel.LOADFLOW;
        if (regulationMode == null) {
            throwExceptionOrLogError(validable, "phase regulation mode is not set", throwException, reporter);
            validationLevel = ValidationLevel.min(validationLevel, ValidationLevel.STATE_ESTIMATION);
        }
        if (regulating && regulationMode != null) {
            if (regulationMode != PhaseTapChanger.RegulationMode.FIXED_TAP && Double.isNaN(regulationValue)) {
                throwExceptionOrLogError(validable, "phase regulation is on and threshold/setpoint value is not set", throwException, reporter);
                validationLevel = ValidationLevel.min(validationLevel, ValidationLevel.STATE_ESTIMATION);
            }
            if (regulationMode != PhaseTapChanger.RegulationMode.FIXED_TAP && regulationTerminal == null) {
                throwExceptionOrLogError(validable, "phase regulation is on and regulated terminal is not set", throwException, reporter);
                validationLevel = ValidationLevel.min(validationLevel, ValidationLevel.STATE_ESTIMATION);
            }
            if (regulationMode == PhaseTapChanger.RegulationMode.FIXED_TAP) {
                throwExceptionOrLogError(validable, "phase regulation cannot be on if mode is FIXED", throwException, reporter);
                validationLevel = ValidationLevel.min(validationLevel, ValidationLevel.STATE_ESTIMATION);
            }
        }
        if (regulationTerminal != null && regulationTerminal.getVoltageLevel().getNetwork() != network) {
            throw new ValidationException(validable, "phase regulation terminal is not part of the network");
        }
        return validationLevel;
    }

    public static void checkOnlyOneTapChangerRegulatingEnabled(Validable validable,
                                                               Set<TapChanger> tapChangersNotIncludingTheModified, boolean regulating) {
        checkOnlyOneTapChangerRegulatingEnabled(validable, tapChangersNotIncludingTheModified, regulating, true);
    }

    public static ValidationLevel checkOnlyOneTapChangerRegulatingEnabled(Validable validable,
                                                               Set<TapChanger> tapChangersNotIncludingTheModified, boolean regulating, boolean throwException) {
        return checkOnlyOneTapChangerRegulatingEnabled(validable, tapChangersNotIncludingTheModified, regulating, throwException, Reporter.NO_OP);
    }

    public static ValidationLevel checkOnlyOneTapChangerRegulatingEnabled(Validable validable, Set<TapChanger> tapChangersNotIncludingTheModified,
                                                               boolean regulating, boolean throwException, Reporter reporter) {
        if (regulating && tapChangersNotIncludingTheModified.stream().anyMatch(TapChanger::isRegulating)) {
            throwExceptionOrLogError(validable, UNIQUE_REGULATING_TAP_CHANGER_MSG, throwException, reporter);
            return ValidationLevel.STATE_ESTIMATION;
        }
        return ValidationLevel.LOADFLOW;
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

    private static ValidationLevel checkRtc(Validable validable, RatioTapChanger rtc, Network network, boolean throwException, Reporter reporter) {
        ValidationLevel validationLevel = ValidationLevel.LOADFLOW;
        if (rtc.getTapPositionAsInteger() == null) {
            throwExceptionOrLogError(validable, "tap position is not set", throwException, reporter);
            validationLevel = ValidationLevel.min(validationLevel, ValidationLevel.STATE_ESTIMATION);
        }
        if (rtc.getTapPositionAsInteger() != null) {
            int highTapPosition = rtc.getLowTapPosition() + rtc.getAllSteps().size() - 1;
            if (rtc.getTapPosition() < rtc.getLowTapPosition() || rtc.getTapPosition() > highTapPosition) {
                throwExceptionOrLogError(validable, "incorrect tap position "
                        + rtc.getTapPosition() + " [" + rtc.getLowTapPosition() + ", "
                        + highTapPosition + "]", throwException, reporter);
                validationLevel = ValidationLevel.min(validationLevel, ValidationLevel.STATE_ESTIMATION);
            }
        }
        validationLevel = ValidationLevel.min(validationLevel, checkRatioTapChangerRegulation(validable, rtc.isRegulating(), rtc.hasLoadTapChangingCapabilities(), rtc.getRegulationTerminal(), rtc.getTargetV(), network, throwException, reporter));
        validationLevel = ValidationLevel.min(validationLevel, checkTargetDeadband(validable, "ratio tap changer", rtc.isRegulating(), rtc.getTargetDeadband(), throwException, reporter));
        return validationLevel;
    }

    private static ValidationLevel checkPtc(Validable validable, PhaseTapChanger ptc, Network network, boolean throwException, Reporter reporter) {
        ValidationLevel validationLevel = ValidationLevel.LOADFLOW;
        if (ptc.getTapPositionAsInteger() == null) {
            throwExceptionOrLogError(validable, "tap position is not set", throwException, reporter);
            validationLevel = ValidationLevel.min(validationLevel, ValidationLevel.STATE_ESTIMATION);
        }
        if (ptc.getTapPositionAsInteger() != null) {
            int highTapPosition = ptc.getLowTapPosition() + ptc.getAllSteps().size() - 1;
            if (ptc.getTapPosition() < ptc.getLowTapPosition() || ptc.getTapPosition() > highTapPosition) {
                throwExceptionOrLogError(validable, "incorrect tap position "
                        + ptc.getTapPosition() + " [" + ptc.getLowTapPosition() + ", "
                        + highTapPosition + "]", throwException, reporter);
                validationLevel = ValidationLevel.min(validationLevel, ValidationLevel.STATE_ESTIMATION);
            }
        }
        validationLevel = ValidationLevel.min(validationLevel, checkPhaseTapChangerRegulation(validable, ptc.getRegulationMode(), ptc.getRegulationValue(), ptc.isRegulating(), ptc.getRegulationTerminal(), network, throwException, reporter));
        validationLevel = ValidationLevel.min(validationLevel, checkTargetDeadband(validable, "phase tap changer", ptc.isRegulating(), ptc.getTargetDeadband(), throwException, reporter));
        return validationLevel;
    }

    public static ValidationLevel validate(Collection<Identifiable<?>> identifiables, boolean allChecks, boolean throwException, ValidationLevel previous, Reporter reporter) {
        Objects.requireNonNull(identifiables);
        Objects.requireNonNull(previous);
        Objects.requireNonNull(reporter);
        if (previous.compareTo(ValidationLevel.LOADFLOW) >= 0) {
            return previous;
        }
        ValidationLevel validationLevel = ValidationLevel.LOADFLOW;
        for (Identifiable<?> identifiable : identifiables) {
            if (identifiable instanceof Validable) {
                Validable validable = (Validable) identifiable;
                if (identifiable instanceof Battery) {
                    Battery battery = (Battery) identifiable;
                    validationLevel = ValidationLevel.min(validationLevel, checkP0(validable, battery.getP0(), throwException, reporter));
                    validationLevel = ValidationLevel.min(validationLevel, checkQ0(validable, battery.getQ0(), throwException, reporter));
                } else if (identifiable instanceof DanglingLine) {
                    DanglingLine danglingLine = (DanglingLine) identifiable;
                    validationLevel = ValidationLevel.min(validationLevel, checkP0(validable, danglingLine.getP0(), throwException, reporter));
                    validationLevel = ValidationLevel.min(validationLevel, checkQ0(validable, danglingLine.getQ0(), throwException, reporter));
                    DanglingLine.Generation generation = danglingLine.getGeneration();
                    validationLevel = ValidationLevel.min(validationLevel, checkActivePowerSetpoint(validable, generation.getTargetP(), throwException, reporter));
                    validationLevel = ValidationLevel.min(validationLevel, checkVoltageControl(validable, generation.isVoltageRegulationOn(), generation.getTargetV(), generation.getTargetQ(), throwException, reporter));
                } else if (identifiable instanceof Generator) {
                    Generator generator = (Generator) identifiable;
                    validationLevel = ValidationLevel.min(validationLevel, checkActivePowerSetpoint(validable, generator.getTargetP(), throwException, reporter));
                    validationLevel = ValidationLevel.min(validationLevel, checkVoltageControl(validable, generator.isVoltageRegulatorOn(), generator.getTargetV(), generator.getTargetQ(), throwException, reporter));
                } else if (identifiable instanceof HvdcLine) {
                    HvdcLine hvdcLine = (HvdcLine) identifiable;
                    validationLevel = ValidationLevel.min(validationLevel, checkHvdcActivePowerSetpoint(validable, hvdcLine.getActivePowerSetpoint(), throwException, reporter));
                } else if (identifiable instanceof Load) {
                    Load load = (Load) identifiable;
                    validationLevel = ValidationLevel.min(validationLevel, checkP0(validable, load.getP0(), throwException, reporter));
                    validationLevel = ValidationLevel.min(validationLevel, checkQ0(validable, load.getQ0(), throwException, reporter));
                } else if (identifiable instanceof ShuntCompensator) {
                    ShuntCompensator shunt = (ShuntCompensator) identifiable;
                    validationLevel = ValidationLevel.min(validationLevel, checkVoltageControl(validable, shunt.isVoltageRegulatorOn(), shunt.getTargetV(), throwException, reporter));
                    validationLevel = ValidationLevel.min(validationLevel, checkTargetDeadband(validable, "shunt compensator", shunt.isVoltageRegulatorOn(), shunt.getTargetDeadband(), throwException, reporter));
                    validationLevel = ValidationLevel.min(validationLevel, checkSections(validable, shunt.getSectionCount(), shunt.getMaximumSectionCount(), throwException, reporter));
                } else if (identifiable instanceof StaticVarCompensator) {
                    StaticVarCompensator svc = (StaticVarCompensator) identifiable;
                    validationLevel = ValidationLevel.min(validationLevel, checkSvcRegulator(validable, svc.getVoltageSetpoint(), svc.getReactivePowerSetpoint(), svc.getRegulationMode(), throwException, reporter));
                } else if (identifiable instanceof ThreeWindingsTransformer) {
                    ThreeWindingsTransformer twt = (ThreeWindingsTransformer) identifiable;
                    for (ThreeWindingsTransformer.Leg leg : twt.getLegs()) {
                        if (leg.getRatioTapChanger() != null) {
                            validationLevel = ValidationLevel.min(validationLevel, checkRtc(validable, leg.getRatioTapChanger(), twt.getNetwork(), throwException, reporter));
                        }
                        if (leg.getPhaseTapChanger() != null) {
                            validationLevel = ValidationLevel.min(validationLevel, checkPtc(validable, leg.getPhaseTapChanger(), twt.getNetwork(), throwException, reporter));
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
                        throwExceptionOrLogError(validable, UNIQUE_REGULATING_TAP_CHANGER_MSG, throwException, reporter);
                    }
                } else if (identifiable instanceof TwoWindingsTransformer) {
                    TwoWindingsTransformer twt = (TwoWindingsTransformer) identifiable;
                    twt.getOptionalRatioTapChanger().ifPresent(rtc -> checkRtc(validable, rtc, twt.getNetwork(), throwException, reporter));
                    twt.getOptionalPhaseTapChanger().ifPresent(ptc -> checkPtc(validable, ptc, twt.getNetwork(), throwException, reporter));
                    if (twt.getOptionalRatioTapChanger().map(RatioTapChanger::isRegulating).orElse(false)
                            && twt.getOptionalPhaseTapChanger().map(PhaseTapChanger::isRegulating).orElse(false)) {
                        throwExceptionOrLogError(validable, UNIQUE_REGULATING_TAP_CHANGER_MSG, throwException, reporter);
                    }
                } else if (identifiable instanceof VscConverterStation) {
                    VscConverterStation converterStation = (VscConverterStation) identifiable;
                    checkVoltageControl(validable, converterStation.isVoltageRegulatorOn(), converterStation.getVoltageSetpoint(), converterStation.getReactivePowerSetpoint(), throwException, reporter);
                }
                if (!allChecks && validationLevel.compareTo(ValidationLevel.LOADFLOW) < 0) {
                    return validationLevel;
                }
            }
        }
        return validationLevel;
    }
}
