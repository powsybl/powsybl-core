/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.validation;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.loadflow.validation.data.SvcData;
import com.powsybl.loadflow.validation.data.Validated;
import com.powsybl.loadflow.validation.io.ValidationWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Objects;

import static com.powsybl.loadflow.validation.ValidationUtils.*;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
public final class StaticVarCompensatorsValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(StaticVarCompensatorsValidation.class);

    private StaticVarCompensatorsValidation() {
    }

    public static boolean checkSVCs(Network network, ValidationConfig config, Path file) throws IOException {
        Objects.requireNonNull(network);
        Objects.requireNonNull(config);
        Objects.requireNonNull(file);
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            return checkSVCs(network, config, writer);
        }
    }

    public static boolean checkSVCs(Network network, ValidationConfig config, Writer writer) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);
        try (ValidationWriter svcsWriter = ValidationUtils.createValidationWriter(network.getId(), config, writer, ValidationType.SVCS)) {
            return checkSVCs(network, config, svcsWriter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean checkSVCs(Network network, ValidationConfig config, ValidationWriter svcsWriter) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(config);
        Objects.requireNonNull(svcsWriter);
        LOGGER.info("Checking static var compensators of network {}", network.getId());
        return network.getStaticVarCompensatorStream()
                .sorted(Comparator.comparing(StaticVarCompensator::getId))
                .map(svc -> checkSVC(svc, config, svcsWriter))
                .reduce(Boolean::logicalAnd)
                .orElse(true);
    }

    public static boolean checkSVC(StaticVarCompensator svc, ValidationConfig config, Writer writer) {
        Objects.requireNonNull(svc);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);

        try (ValidationWriter svcsWriter = ValidationUtils.createValidationWriter(svc.getId(), config, writer, ValidationType.SVCS)) {
            return checkSVC(svc, config, svcsWriter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean checkSVC(StaticVarCompensator svc, ValidationConfig config, ValidationWriter svcsWriter) {
        Objects.requireNonNull(svc);
        Objects.requireNonNull(config);
        Objects.requireNonNull(svcsWriter);
        double p = svc.getTerminal().getP();
        double q = svc.getTerminal().getQ();
        double reactivePowerSetpoint = svc.getRegulatingTargetQ();
        double voltageSetpoint = svc.getRegulatingTargetV();
        RegulationMode regulationMode = RegulationMode.REACTIVE_POWER;
        boolean regulating = false;
        if (svc.getVoltageRegulation() != null) {
            regulationMode = svc.getVoltageRegulation().getMode();
            regulating = svc.getVoltageRegulation().isRegulating();
        }
        double bMin = svc.getBmin();
        double bMax = svc.getBmax();
        double nominalVcontroller = svc.getTerminal().getVoltageLevel().getNominalV();
        TerminalState terminalState = getTerminalState(svc.getTerminal());
        double vController = terminalState.v();
        double vControlled = svc.getRegulatingTerminal() != null
                ? getTerminalState(svc.getRegulatingTerminal()).v()
                : vController;
        SvcData d = new SvcData(svc.getId(),
                p, q,
                vControlled, vController,
                nominalVcontroller,
                reactivePowerSetpoint, voltageSetpoint,
                regulationMode, regulating,
                bMin, bMax,
                terminalState.connected(), terminalState.mainComponent());
        return checkSVC(d, config, svcsWriter);
    }

    public static boolean checkSVC(SvcData d, ValidationConfig config, Writer writer) {
        Objects.requireNonNull(d);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);

        try (ValidationWriter svcsWriter = ValidationUtils.createValidationWriter(d.svcId(), config, writer, ValidationType.SVCS)) {
            return checkSVC(d, config, svcsWriter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Rules for valid results:<br/>
     * Rule: active power should be equal to 0 <br/>
     * Rule: reactivePowerSetpoint must be 0 if p or q is missing <br/>
     * Rule: regulationMode = REACTIVE_POWER, q must match reactivePowerSetpoint <br/>
     * Rule: regulationMode = VOLTAGE <br/>
     *    - V is lower than voltageSetpoint (within threshold) AND q must match qMax (within threshold) <br/>
     *    - V is higher than voltageSetpoint (within threshold) AND q must match Qmin (within threshold) <br/>
     *    - V is at the controlled bus (within threshold) AND q is bounded within [Qmin=-bMax*V*V, Qmax=-bMin*V*V] <br/>
     * Rule: if regulating is false then reactive power (q) should be equal to 0
     */
    public static boolean checkSVC(SvcData d, ValidationConfig config, ValidationWriter svcsWriter) {
        Objects.requireNonNull(d);
        Objects.requireNonNull(config);
        Objects.requireNonNull(svcsWriter);
        boolean validated = true;
        String id = d.svcId();

        if (isConnectedAndMainComponent(d.connected(), d.mainComponent(), config)) {
            // Rule: reactivePowerSetpoint must be 0 if p or q is missing
            if (Double.isNaN(d.p()) || Double.isNaN(d.q())) {
                if (!isNaNOrZero(d.reactivePowerSetpoint(), 0.0)) {
                    LOGGER.warn("{} {}: {}: P={} Q={} reactivePowerSetpoint={}", ValidationType.SVCS, ValidationUtils.VALIDATION_ERROR, id, d.p(), d.q(), d.reactivePowerSetpoint());
                    validated = false;
                }
            } else {
                validated = checkSVCValues(d, config);
            }
        }
        try {
            svcsWriter.writeSvc(new Validated<>(d, validated));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return validated;
    }

    private static boolean checkSVCValues(SvcData d, ValidationConfig config) {
        boolean validated = true;
        String id = d.svcId();
        double threshold = config.getThreshold();
        // Rule: active power should be equal to 0
        if (isOutsideTolerance(d.p(), 0.0, threshold)) {
            LOGGER.warn("{} {}: {}: P={}", ValidationType.SVCS, ValidationUtils.VALIDATION_ERROR, id, d.p());
            validated = false;
        }
        double vAux = (Double.isNaN(d.vController()) || d.vController() == 0.0) ? d.nominalVcontroller() : d.vController();
        double qMin = -d.bMax() * vAux * vAux;
        double qMax = -d.bMin() * vAux * vAux;

        // Rule: regulationMode = REACTIVE_POWER, q must match reactivePowerSetpoint
        if (reactivePowerRegulationModeKo(d.regulationMode(), d.q(), qMin, qMax, d.reactivePowerSetpoint(), config)) {
            LOGGER.warn(
                    "{} {}: {}: regulator mode={} - Q={} qMin={} qMax={} bMin={} bMax={} Vcontroller={} nominalV={} reactivePowerSetpoint={}",
                    ValidationType.SVCS, ValidationUtils.VALIDATION_ERROR, id, d.regulationMode(), d.q(), qMin, qMax, d.bMin(), d.bMax(),
                    d.vController(), d.nominalVcontroller(), d.reactivePowerSetpoint());
            validated = false;
        }

        // Rule: regulationMode = VOLTAGE
        if (voltageRegulationModeKo(d.regulationMode(), d.q(), qMin, qMax, d.vControlled(), d.voltageSetpoint(), config)) {
            LOGGER.warn(
                    "{} {}: {}: regulator mode={} - Q={} qMin={} qMax={} bMin={} bMax={} Vcontroller={} Vcontrolled={} targetV={}",
                    ValidationType.SVCS, ValidationUtils.VALIDATION_ERROR, id, d.regulationMode(), d.q(), qMin, qMax, d.bMin(), d.bMax(),
                    d.vController(), d.vControlled(), d.voltageSetpoint());
            validated = false;
        }
        // Rule: if regulating is false then reactive power (q) should be equal to 0
        if (notRegulatingKo(d.regulating(), d.q(), config)) {
            LOGGER.warn("{} {}: {}: regulator mode={} - Q={} ", ValidationType.SVCS, ValidationUtils.VALIDATION_ERROR, id, d.regulationMode(), d.q());
            validated = false;
        }
        return validated;
    }

    private static boolean reactivePowerRegulationModeKo(RegulationMode regulationMode, double q, double qMin,
                                                         double qMax, double reactivePowerSetpoint, ValidationConfig config) {
        // if regulationMode = REACTIVE_POWER, the reactive power must be equal to setpoint
        if (regulationMode != RegulationMode.REACTIVE_POWER) {
            return false;
        }
        if (ValidationUtils.areNaN(config, reactivePowerSetpoint, qMin, qMax)) {
            return true;
        }
        return isOutsideTolerance(q, reactivePowerSetpoint, config.getThreshold());
    }

    private static boolean voltageRegulationModeKo(RegulationMode regulationMode, double q, double qMin,
                                                   double qMax, double vControlled, double voltageSetpoint, ValidationConfig config) {
        // if regulationMode = VOLTAGE then
        // either q is equal to Qmax = -bMin * V * V and V is lower than voltageSetpoint
        // or q is equal to Qmin = -bMax * V * V and V is higher than voltageSetpoint
        // or V at the controlled bus is equal to voltageSetpoint and q is bounded
        // within [Qmin=-bMax*V*V, Qmax=-bMin*V*V]
        double threshold = config.getThreshold();
        if (regulationMode != RegulationMode.VOLTAGE) {
            return false;
        }
        if (ValidationUtils.areNaN(config, qMin, qMax, vControlled, voltageSetpoint)) {
            return true;
        }
        if (vControlled < voltageSetpoint - threshold && isOutsideTolerance(q, qMax, threshold)) {
            return true;
        }
        if (vControlled > voltageSetpoint + threshold && isOutsideTolerance(q, qMin, threshold)) {
            return true;
        }
        boolean voltageAtSetpoint = Math.abs(vControlled - voltageSetpoint) < threshold;
        return voltageAtSetpoint && !boundedWithin(qMin, qMax, q, threshold);
    }

    private static boolean notRegulatingKo(boolean regulating, double q, ValidationConfig config) {
        // if regulating is false then reactive power should be equal to 0
        return !regulating && Math.abs(q) > config.getThreshold();
    }
}
