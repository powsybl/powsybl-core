/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.validation;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.StaticVarCompensator.RegulationMode;
import com.powsybl.loadflow.validation.io.ValidationWriter;

import static com.powsybl.loadflow.validation.ValidationUtils.*;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
public final class StaticVarCompensatorsValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(StaticVarCompensatorsValidation.class);

    public static final StaticVarCompensatorsValidation INSTANCE = new StaticVarCompensatorsValidation();

    private StaticVarCompensatorsValidation() {
    }

    public boolean checkSVCs(Network network, ValidationConfig config, Path file) throws IOException {
        Objects.requireNonNull(file);
        Objects.requireNonNull(config);
        Objects.requireNonNull(file);
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            return checkSVCs(network, config, writer);
        }
    }

    public boolean checkSVCs(Network network, ValidationConfig config, Writer writer) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);
        try (ValidationWriter svcsWriter = ValidationUtils.createValidationWriter(network.getId(), config, writer, ValidationType.SVCS)) {
            return checkSVCs(network, config, svcsWriter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public boolean checkSVCs(Network network, ValidationConfig config, ValidationWriter svcsWriter) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(config);
        Objects.requireNonNull(svcsWriter);
        LOGGER.info("Checking static var compensators of network {}", network.getId());
        return network.getStaticVarCompensatorStream()
                      .sorted(Comparator.comparing(StaticVarCompensator::getId))
                      .map(svc -> checkSVCs(svc, config, svcsWriter))
                      .reduce(Boolean::logicalAnd)
                      .orElse(true);
    }

    public boolean checkSVCs(StaticVarCompensator svc, ValidationConfig config, Writer writer) {
        Objects.requireNonNull(svc);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);

        try (ValidationWriter svcsWriter = ValidationUtils.createValidationWriter(svc.getId(), config, writer, ValidationType.SVCS)) {
            return checkSVCs(svc, config, svcsWriter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public boolean checkSVCs(StaticVarCompensator svc, ValidationConfig config, ValidationWriter svcsWriter) {
        Objects.requireNonNull(svc);
        Objects.requireNonNull(config);
        Objects.requireNonNull(svcsWriter);
        double p = svc.getTerminal().getP();
        double q = svc.getTerminal().getQ();
        Bus bus = svc.getTerminal().getBusView().getBus();
        double reactivePowerSetpoint = svc.getReactivePowerSetpoint();
        double voltageSetpoint = svc.getVoltageSetpoint();
        RegulationMode regulationMode = svc.getRegulationMode();
        boolean regulating = svc.isRegulating();
        double bMin = svc.getBmin();
        double bMax = svc.getBmax();
        double nominalVcontroller = svc.getTerminal().getVoltageLevel().getNominalV();
        double vController = bus != null ? bus.getV() : Double.NaN;
        double vControlled = svc.getRegulatingTerminal() != null
                ? ValidationUtils.getTerminalState(svc.getRegulatingTerminal()).v()
                : vController;
        ValidationUtils.TerminalState terminalState = ValidationUtils.getTerminalState(svc.getTerminal());
        boolean connected = terminalState.connected();
        boolean mainComponent = terminalState.mainComponent();
        return checkSVCs(svc.getId(), p, q, vControlled, vController, nominalVcontroller, reactivePowerSetpoint, voltageSetpoint, regulationMode, regulating, bMin, bMax, connected, mainComponent, config, svcsWriter);
    }

    public boolean checkSVCs(String id, double p, double q, double vControlled, double vController, double nominalVcontroller, double reactivePowerSetpoint, double voltageSetpoint,
                                    RegulationMode regulationMode, boolean regulating, double bMin, double bMax, boolean connected, boolean mainComponent,
                                    ValidationConfig config, Writer writer) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);

        try (ValidationWriter svcsWriter = ValidationUtils.createValidationWriter(id, config, writer, ValidationType.SVCS)) {
            return checkSVCs(id, p, q, vControlled, vController, nominalVcontroller, reactivePowerSetpoint, voltageSetpoint, regulationMode, regulating, bMin, bMax, connected, mainComponent, config, svcsWriter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

     /**
     *
     * Rule1: active power (p) (within threshold) should be equal to 0 <br/>
     * Rule2: **reactivePowerSetpoint** must be undefined or equal to 0 if NO (**p** or **q**)<br/>
     * - TODO (doc states that p should be equal to 0, if so **reactivePowerSetpoint** must be undefined or equal to 0)<br/>
     * - Suggestion => check only if (q undefined or equal to 0 then **reactivePowerSetpoint** must be 0)<br/><br/>
     * Rule3: **regulationMode = REACTIVE_POWER** then same condition as generator without voltage regulation<br/>
     * - Rule3.1: => (config, reactivePowerSetpoint, qMin, qMax) not defined => OK<br/>
     * - Rule3.2: => q must match reactivePowerSetpoint (within threshold)<br/><br/>
     * Rule4: **regulationMode = VOLTAGE** then same condition as generator with voltage regulation<br/>
     * - Rule4.1: => (config, qMin, qMax, vControlled, voltageSetpoint) not defined => OK<br/>
     * - Rule4.2: => V is lower than voltageSetpoint (within threshold) AND q must match qMax (within threshold)<br/>
     * - Rule4.3: => V is higher than voltageSetpoint (within threshold) AND q must match Qmin (within threshold)<br/>
     * - Rule4.4: => V is at the controlled bus (within threshold) AND q is bounded within [Qmin=-bMax*V*V, Qmax=-bMin*V*V]<br/><br/>
     * Rule5: if regulating is false then reactive power (q) should be equal to 0<br/>
     *
     */
    public boolean checkSVCs(String id, double p, double q, double vControlled, double vController, double nominalVcontroller, double reactivePowerSetpoint, double voltageSetpoint,
                                    RegulationMode regulationMode, boolean regulating, double bMin, double bMax, boolean connected, boolean mainComponent,
                                    ValidationConfig config, ValidationWriter svcsWriter) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(config);
        Objects.requireNonNull(svcsWriter);
        boolean validated = true;

        if (connected && ValidationUtils.isMainComponent(config, mainComponent)) {
            if (Double.isNaN(p) || Double.isNaN(q)) {
                // Rule2: **reactivePowerSetpoint** must be 0 if p or q is missing (NaN)
                validated = checkSVCsNaNValues(id, p, q, reactivePowerSetpoint);
            } else {
                // Rule1, Rule3, Rule4, Rule5
                validated = checkSVCsValues(id, p, q, vControlled, vController, nominalVcontroller, reactivePowerSetpoint, voltageSetpoint, regulationMode, regulating, bMin, bMax, config);
            }
        }
        try {
            svcsWriter.write(id, p, q, vControlled, vController, nominalVcontroller, reactivePowerSetpoint, voltageSetpoint, connected, regulationMode, regulating, bMin, bMax, mainComponent, validated);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return validated;
    }

    private static boolean checkSVCsNaNValues(String id, double p, double q, double reactivePowerSetpoint) {
        // a validation error should be detected if there is a setpoint but no p or q
        if (!isUndefinedOrZero(reactivePowerSetpoint, 0.0)) {
            LOGGER.warn("{} {}: {}: P={} Q={} reactivePowerSetpoint={}", ValidationType.SVCS, ValidationUtils.VALIDATION_ERROR, id, p, q, reactivePowerSetpoint);
            return false;
        }
        return true;
    }

    private static boolean checkSVCsValues(String id, double p, double q, double vControlled, double vController,
        double nominalVcontroller, double reactivePowerSetpoint, double voltageSetpoint,
        RegulationMode regulationMode, boolean regulating, double bMin, double bMax, ValidationConfig config) {
        boolean validated = true;
        double threshold = config.getThreshold();
        // Rule1: active power should be equal to 0
        if (isOutsideTolerance(p, 0.0, threshold)) {
            LOGGER.warn("{} {}: {}: P={}", ValidationType.SVCS, ValidationUtils.VALIDATION_ERROR, id, p);
            validated = false;
        }
        // vController: voltage in bus
        // nominalVcontroller: voltage in VoltageLevel
        double vAux = voltageFrom(vController, nominalVcontroller);
        double qMin = -bMax * vAux * vAux;
        double qMax = -bMin * vAux * vAux;

        //Rule3: **regulationMode = REACTIVE_POWER**
        if (reactivePowerRegulationModeKo(regulationMode, q, qMin, qMax, reactivePowerSetpoint, config)) {
            LOGGER.warn(
                "{} {}: {}: regulator mode={} - Q={} qMin={} qMax={} bMin={} bMax={} Vcontroller={} nominalV={} reactivePowerSetpoint={}",
                ValidationType.SVCS, ValidationUtils.VALIDATION_ERROR, id, regulationMode, q, qMin, qMax, bMin, bMax,
                vController, nominalVcontroller, reactivePowerSetpoint);
            validated = false;
        }

        // Rule4: **regulationMode = VOLTAGE**
        if (voltageRegulationModeKo(regulationMode, q, qMin, qMax, vControlled, voltageSetpoint, config)) {
            LOGGER.warn(
                "{} {}: {}: regulator mode={} - Q={} qMin={} qMax={} bMin={} bMax={} Vcontroller={} Vcontrolled={} targetV={}",
                ValidationType.SVCS, ValidationUtils.VALIDATION_ERROR, id, regulationMode, q, qMin, qMax, bMin, bMax,
                vController, vControlled, voltageSetpoint);
            validated = false;
        }

        // Rule5: if regulating is false then reactive power (q) should be equal to 0
        if (notRegulatingKo(regulating, q, config)) {
            LOGGER.warn("{} {}: {}: regulator mode={} - Q={} ", ValidationType.SVCS, ValidationUtils.VALIDATION_ERROR,
                id, regulationMode, q);
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
