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
        double p = svc.getTerminal().getP();
        double q = svc.getTerminal().getQ();
        Bus bus = svc.getTerminal().getBusView().getBus();
        double reactivePowerSetpoint = svc.getReactivePowerSetPoint();
        double voltageSetpoint = svc.getVoltageSetPoint();
        RegulationMode regulationMode = svc.getRegulationMode();
        double bMin = svc.getBmin();
        double bMax = svc.getBmax();
        double nominalV = svc.getTerminal().getVoltageLevel().getNominalV();
        double v = bus != null ? bus.getV() : Double.NaN;
        boolean connected = bus != null;
        Bus connectableBus = svc.getTerminal().getBusView().getConnectableBus();
        boolean connectableMainComponent = connectableBus != null && connectableBus.isInMainConnectedComponent();
        boolean mainComponent = bus != null ? bus.isInMainConnectedComponent() : connectableMainComponent;
        return checkSVCs(svc.getId(), p, q, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, bMin, bMax, connected, mainComponent, config, svcsWriter);
    }

    public static boolean checkSVCs(String id, double p, double q, double v, double nominalV, double reactivePowerSetpoint, double voltageSetpoint,
                                    RegulationMode regulationMode, double bMin, double bMax, boolean connected, boolean mainComponent,
                                    ValidationConfig config, Writer writer) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);

        try (ValidationWriter svcsWriter = ValidationUtils.createValidationWriter(id, config, writer, ValidationType.SVCS)) {
            return checkSVCs(id, p, q, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, bMin, bMax, connected, mainComponent, config, svcsWriter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean checkSVCs(String id, double p, double q, double v, double nominalV, double reactivePowerSetpoint, double voltageSetpoint,
                                    RegulationMode regulationMode, double bMin, double bMax, boolean connected, boolean mainComponent,
                                    ValidationConfig config, ValidationWriter svcsWriter) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(config);
        Objects.requireNonNull(svcsWriter);
        boolean validated = true;

        if (connected && ValidationUtils.isMainComponent(config, mainComponent)) {
            if (Double.isNaN(p) || Double.isNaN(q)) {
                validated = checkSVCsNaNValues(id, p, q, reactivePowerSetpoint);
            } else {
                validated = checkSVCsValues(id, p, q, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, bMin, bMax, config);
            }
        }
        try {
            svcsWriter.write(id, p, q, v, nominalV, reactivePowerSetpoint, voltageSetpoint, connected, regulationMode, bMin, bMax, mainComponent, validated);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return validated;
    }

    private static boolean checkSVCsNaNValues(String id, double p, double q, double reactivePowerSetpoint) {
        // a validation error should be detected if there is a setpoint but no p or q
        if (!Double.isNaN(reactivePowerSetpoint) && reactivePowerSetpoint != 0) {
            LOGGER.warn("{} {}: {}: P={} Q={} reactivePowerSetpoint={}", ValidationType.SVCS, ValidationUtils.VALIDATION_ERROR, id, p, q, reactivePowerSetpoint);
            return false;
        }
        return true;
    }

    private static boolean checkSVCsValues(String id, double p, double q, double v, double nominalV, double reactivePowerSetpoint, double voltageSetpoint,
                                           RegulationMode regulationMode, double bMin, double bMax, ValidationConfig config) {
        boolean validated = true;
        // active power should be equal to 0
        if (Math.abs(p) > config.getThreshold()) {
            LOGGER.warn("{} {}: {}: P={}", ValidationType.SVCS, ValidationUtils.VALIDATION_ERROR, id, p);
            validated = false;
        }
        // for the computation of Qmin and Qmax use either the nominal voltage or the voltage of the  bus (depending on the SpecificCompatibility loadflow parameter)
        double vAux = config.getLoadFlowParameters().isSpecificCompatibility() ? nominalV : v;
        double qMin = bMin * vAux * vAux;
        double qMax = bMax * vAux * vAux;
        // if regulationMode = REACTIVE_POWER
        // if the setpoint is in [Qmin=bMin*V*V, Qmax=bMax*V*V] then reactive power should be equal to setpoint
        // if the setpoint is outside [Qmin=bMin*V*V, Qmax=bMax*V*V] then the reactive power is equal to the nearest bound
        if (regulationMode == RegulationMode.REACTIVE_POWER
            && (ValidationUtils.areNaN(config, reactivePowerSetpoint, qMin, qMax)
                || Math.abs(q + getReactivePowerSetpoint(reactivePowerSetpoint, qMin, qMax, config)) > config.getThreshold())) {
            LOGGER.warn("{} {}: {}: regulator mode={} - Q={} bMin={} bMax={} V={} nominalV={} reactivePowerSetpoint={}", ValidationType.SVCS, ValidationUtils.VALIDATION_ERROR, id, regulationMode, q, bMin, bMax, v, nominalV, reactivePowerSetpoint);
            validated = false;
        }
        // if regulationMode = VOLTAGE then
        // either q is equal to Qmax = bMax * V * V and V is lower than voltageSetpoint
        // or q is equal to Qmin = bMin * V * V and V is higher than voltageSetpoint
        // or V at the connected bus is equal to voltageSetpoint and q is bounded within [-Qmax=bMax*V*V, -Qmin=bMin*V*V]
        double qSvc = -q;
        if (regulationMode == RegulationMode.VOLTAGE
            && (ValidationUtils.areNaN(config, qMin, qMax, v, voltageSetpoint)
                || (v < voltageSetpoint - config.getThreshold() && Math.abs(qSvc - qMax) > config.getThreshold())
                || (v > voltageSetpoint + config.getThreshold() && Math.abs(qSvc - qMin) > config.getThreshold())
                || (Math.abs(v - voltageSetpoint) < config.getThreshold()) && !ValidationUtils.boundedWithin(qMin, qMax, qSvc, config.getThreshold()))) {
            LOGGER.warn("{} {}: {}: regulator mode={} - Q={} bMin={} bMax={} V={} nominalV={} targetV={}", ValidationType.SVCS, ValidationUtils.VALIDATION_ERROR, id, regulationMode, qSvc, bMin, bMax, v, nominalV, voltageSetpoint);
            validated = false;
        }
        // if regulationMode = OFF then reactive power should be equal to 0
        if (regulationMode == RegulationMode.OFF && Math.abs(q) > config.getThreshold()) {
            LOGGER.warn("{} {}: {}: regulator mode={} - Q={} ", ValidationType.SVCS, ValidationUtils.VALIDATION_ERROR, id, regulationMode, q);
            validated = false;
        }
        return validated;
    }

    private static double closestBound(double bound1, double bound2, double value) {
        if (Double.isNaN(value)) {
            return Double.NaN;
        }
        if (Double.isNaN(bound1)) {
            return bound2;
        }
        if (Double.isNaN(bound2)) {
            return bound1;
        }
        return Math.abs(value - bound1) < Math.abs(value - bound2) ? bound1 : bound2;
    }

    private static double getReactivePowerSetpoint(double reactivePowerSetpoint, double qMin, double qMax, ValidationConfig config) {
        return ValidationUtils.boundedWithin(qMin, qMax, reactivePowerSetpoint, config.getThreshold())
               ? reactivePowerSetpoint
               : closestBound(qMin, qMax, reactivePowerSetpoint);
    }
}
