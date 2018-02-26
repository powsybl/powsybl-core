/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
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

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public final class StaticVarCompensatorsValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(StaticVarCompensatorsValidation.class);

    private StaticVarCompensatorsValidation() {
    }

    public static boolean checkSVCs(Network network, ValidationConfig config, Path file) throws IOException {
        Objects.requireNonNull(file);
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
                      .map(svc -> checkSVCs(svc, config, svcsWriter))
                      .reduce(Boolean::logicalAnd)
                      .orElse(true);
    }

    public static boolean checkSVCs(StaticVarCompensator svc, ValidationConfig config, Writer writer) {
        Objects.requireNonNull(svc);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);

        try (ValidationWriter svcsWriter = ValidationUtils.createValidationWriter(svc.getId(), config, writer, ValidationType.SVCS)) {
            return checkSVCs(svc, config, svcsWriter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean checkSVCs(StaticVarCompensator svc, ValidationConfig config, ValidationWriter svcsWriter) {
        Objects.requireNonNull(svc);
        Objects.requireNonNull(config);
        Objects.requireNonNull(svcsWriter);
        float p = svc.getTerminal().getP();
        float q = svc.getTerminal().getQ();
        Bus bus = svc.getTerminal().getBusView().getBus();
        float reactivePowerSetpoint = svc.getReactivePowerSetPoint();
        float voltageSetpoint = svc.getVoltageSetPoint();
        RegulationMode regulationMode = svc.getRegulationMode();
        float bMin = svc.getBmin();
        float bMax = svc.getBmax();
        if (bus != null && !Float.isNaN(bus.getV())) {
            float v = config.getLoadFlowParameters().isSpecificCompatibility() ? svc.getTerminal().getVoltageLevel().getNominalV() : bus.getV();
            return checkSVCs(svc.getId(), p, q, v, reactivePowerSetpoint, voltageSetpoint, regulationMode, bMin, bMax, config, svcsWriter);
        }
        try {
            svcsWriter.write(svc.getId(), p, q, Float.NaN, reactivePowerSetpoint, voltageSetpoint, svc.getTerminal().isConnected(), regulationMode, bMin, bMax, true);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return true;
    }

    public static boolean checkSVCs(String id, float p, float q, float v, float reactivePowerSetpoint, float voltageSetpoint,
                                    RegulationMode regulationMode, float bMin, float bMax, ValidationConfig config, Writer writer) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);

        try (ValidationWriter svcsWriter = ValidationUtils.createValidationWriter(id, config, writer, ValidationType.SVCS)) {
            return checkSVCs(id, p, q, v, reactivePowerSetpoint, voltageSetpoint, regulationMode, bMin, bMax, config, svcsWriter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean checkSVCs(String id, float p, float q, float v, float reactivePowerSetpoint, float voltageSetpoint,
                                    RegulationMode regulationMode, float bMin, float bMax, ValidationConfig config, ValidationWriter svcsWriter) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(config);
        Objects.requireNonNull(svcsWriter);
        boolean validated = true;
        try {
            if (Float.isNaN(p) || Float.isNaN(q)) {
                validated = checkSVCsNaNValues(id, p, q, reactivePowerSetpoint);
            } else {
                validated = checkSVCsValues(id, p, q, v, reactivePowerSetpoint, voltageSetpoint, regulationMode, bMin, bMax, config);
            }
            svcsWriter.write(id, p, q, v, reactivePowerSetpoint, voltageSetpoint, true, regulationMode, bMin, bMax, validated);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return validated;
    }

    private static boolean checkSVCsNaNValues(String id, float p, float q, float reactivePowerSetpoint) {
        // a validation error should be detected if there is a setpoint but no p or q
        if (!Float.isNaN(reactivePowerSetpoint) && reactivePowerSetpoint != 0) {
            LOGGER.warn("{} {}: {}: P={} Q={} reactivePowerSetpoint={}", ValidationType.SVCS, ValidationUtils.VALIDATION_ERROR, id, p, q, reactivePowerSetpoint);
            return false;
        }
        return true;
    }

    private static boolean checkSVCsValues(String id, float p, float q, float v, float reactivePowerSetpoint, float voltageSetpoint,
                                           RegulationMode regulationMode, float bMin, float bMax, ValidationConfig config) {
        boolean validated = true;
        // active power should be equal to 0
        if (Math.abs(p) > config.getThreshold()) {
            LOGGER.warn("{} {}: {}: P={}", ValidationType.SVCS, ValidationUtils.VALIDATION_ERROR, id, p);
            validated = false;
        }
        float qMin = bMin * v * v;
        float qMax = bMax * v * v;
        // if regulationMode = REACTIVE_POWER
        // if the setpoint is in [Qmin=bMin*V*V, Qmax=bMax*V*V] then reactive power should be equal to setpoint
        // if the setpoint is outside [Qmin=bMin*V*V, Qmax=bMax*V*V] then the reactive power is equal to the nearest bound
        if (regulationMode == RegulationMode.REACTIVE_POWER
            && (ValidationUtils.areNaN(config, reactivePowerSetpoint, qMin, qMax)
                || Math.abs(q + getSetPoint(reactivePowerSetpoint, qMin, qMax, config)) > config.getThreshold())) {
            LOGGER.warn("{} {}: {}: regulator mode={} - Q={} bMin={} bMax={} V={} reactivePowerSetpoint={}", ValidationType.SVCS, ValidationUtils.VALIDATION_ERROR, id, regulationMode, q, bMin, bMax, v, reactivePowerSetpoint);
            validated = false;
        }
        // if regulationMode = VOLTAGE then
        // either q is equal to Qmax = bMax * V * V and V is lower than voltageSetpoint
        // or q is equal to Qmin = bMin * V * V and V is higher than voltageSetpoint
        // or V at the connected bus is equal to voltageSetpoint and q is bounded within [-Qmax=bMax*V*V, -Qmin=bMin*V*V]
        if (regulationMode == RegulationMode.VOLTAGE
            && (ValidationUtils.areNaN(config, qMin, qMax, v, voltageSetpoint)
                || ((Math.abs(q + qMax) > config.getThreshold() || (v - voltageSetpoint) >= config.getThreshold())
                    && (Math.abs(q + qMin) > config.getThreshold() || (voltageSetpoint - v) >= config.getThreshold())
                    && (Math.abs(v - voltageSetpoint) > config.getThreshold() || !ValidationUtils.boundedWithin(-qMax, -qMin, q, config.getThreshold()))))) {
            LOGGER.warn("{} {}: {}: regulator mode={} - Q={} bMin={} bMax={} V={} targetV={}", ValidationType.SVCS, ValidationUtils.VALIDATION_ERROR, id, regulationMode, q, bMin, bMax, v, voltageSetpoint);
            validated = false;
        }
        // if regulationMode = OFF then reactive power should be equal to 0
        if (regulationMode == RegulationMode.OFF && Math.abs(q) > config.getThreshold()) {
            LOGGER.warn("{} {}: {}: regulator mode={} - Q={} ", ValidationType.SVCS, ValidationUtils.VALIDATION_ERROR, id, regulationMode, q);
            validated = false;
        }
        return validated;
    }

    private static float closestBound(float bound1, float bound2, float value) {
        if (Float.isNaN(value)) {
            return Float.NaN;
        }
        if (Float.isNaN(bound1)) {
            return bound2;
        }
        if (Float.isNaN(bound2)) {
            return bound1;
        }
        return Math.abs(value - bound1) < Math.abs(value - bound2) ? bound1 : bound2;
    }

    private static float getSetPoint(float reactivePowerSetpoint, float qMin, float qMax, ValidationConfig config) {
        return ValidationUtils.boundedWithin(qMin, qMax, reactivePowerSetpoint, config.getThreshold())
               ? reactivePowerSetpoint
               : closestBound(qMin, qMax, reactivePowerSetpoint);
    }
}
