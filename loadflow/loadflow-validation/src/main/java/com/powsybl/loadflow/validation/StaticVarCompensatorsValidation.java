/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
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

    public static boolean checkSVCs(StaticVarCompensator sva, ValidationConfig config, Writer writer) {
        Objects.requireNonNull(sva);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);

        try (ValidationWriter svcsWriter = ValidationUtils.createValidationWriter(sva.getId(), config, writer, ValidationType.SVCS)) {
            return checkSVCs(sva, config, svcsWriter);
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
        float reactivePowerSetPoint = svc.getReactivePowerSetPoint();
        float voltageSetPoint = svc.getVoltageSetPoint();
        RegulationMode regulationMode = svc.getRegulationMode();
        float bMin = svc.getBmin();
        float bMax = svc.getBmax();
        if (bus != null && !Float.isNaN(bus.getV())) {
            float v = bus.getV();
            return checkSVCs(svc.getId(), p, q, v, reactivePowerSetPoint, voltageSetPoint, regulationMode, bMin, bMax, config, svcsWriter);
        }
        try {
            svcsWriter.write(svc.getId(), p, q, Float.NaN, reactivePowerSetPoint, voltageSetPoint, svc.getTerminal().isConnected(), regulationMode, bMin, bMax, true);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return true;
    }

    public static boolean checkSVCs(String id, float p, float q, float v, float reactivePowerSetPoint, float voltageSetPoint,
                                    RegulationMode regulationMode, float bMin, float bMax, ValidationConfig config, Writer writer) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);

        try (ValidationWriter svcsWriter = ValidationUtils.createValidationWriter(id, config, writer, ValidationType.SVCS)) {
            return checkSVCs(id, p, q, v, reactivePowerSetPoint, voltageSetPoint, regulationMode, bMin, bMax, config, svcsWriter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean checkSVCs(String id, float p, float q, float v, float reactivePowerSetPoint, float voltageSetPoint,
                                    RegulationMode regulationMode, float bMin, float bMax, ValidationConfig config, ValidationWriter svcsWriter) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(config);
        Objects.requireNonNull(svcsWriter);
        boolean validated = true;
        try {
            if (Float.isNaN(p) || Float.isNaN(q)) {
                validated = checkSVCsNaNValues(id, p, q, reactivePowerSetPoint);
            } else {
                validated = checkSVCsValues(id, p, q, v, reactivePowerSetPoint, voltageSetPoint, regulationMode, bMin, bMax, config);
            }
            svcsWriter.write(id, p, q, v, reactivePowerSetPoint, voltageSetPoint, true, regulationMode, bMin, bMax, validated);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return validated;
    }

    private static boolean checkSVCsNaNValues(String id, float p, float q, float reactivePowerSetPoint) {
        // a validation error should be detected if there is a set point but no p or q
        if (!Float.isNaN(reactivePowerSetPoint) && reactivePowerSetPoint != 0) {
            LOGGER.warn("{} {}: {}: P={} Q={} reactivePowerSetPoint={}", ValidationType.SVCS, ValidationUtils.VALIDATION_ERROR, id, p, q, reactivePowerSetPoint);
            return false;
        }
        return true;
    }

    private static boolean checkSVCsValues(String id, float p, float q, float v, float reactivePowerSetPoint, float voltageSetPoint,
                                           RegulationMode regulationMode, float bMin, float bMax, ValidationConfig config) {
        boolean validated = true;
        // active power should be equal to 0
        if (Math.abs(p) > config.getThreshold()) {
            LOGGER.warn("{} {}: {}: P={}", ValidationType.SVCS, ValidationUtils.VALIDATION_ERROR, id, p);
            validated = false;
        }
        // if regulationMode=VOLTAGE then reactive power should be equal to set point
        if (regulationMode == RegulationMode.VOLTAGE && ((Float.isNaN(reactivePowerSetPoint) && !config.areOkMissingValues()) || Math.abs(q + reactivePowerSetPoint) > config.getThreshold())) {
            LOGGER.warn("{} {}: {}: regulator mode={} - Q={} reactivePowerSetPoint={}", ValidationType.SVCS, ValidationUtils.VALIDATION_ERROR, id, regulationMode, q, reactivePowerSetPoint);
            validated = false;
        }
        // if regulationMode<>VOLTAGE then
        // either q is equal to bMin * V and V is lower than voltageSetPoint
        // or q is equal to bMax * V and V is higher than voltageSetPoint
        // or V at the connected bus is equal to voltageSetPoint
        if (regulationMode != RegulationMode.VOLTAGE
            && (((Float.isNaN(bMin) || Float.isNaN(bMax) || Float.isNaN(voltageSetPoint)) && !config.areOkMissingValues())
                || ((Math.abs(q + bMin * v) > config.getThreshold() || (v - voltageSetPoint) >= config.getThreshold())
                    && (Math.abs(q + bMax * v) > config.getThreshold() || (voltageSetPoint - v) >= config.getThreshold())
                    && Math.abs(v - voltageSetPoint) > config.getThreshold()))) {
            LOGGER.warn("{} {}: {}: regulator mode={} - Q={} bMin={} bMax={} - V={} targetV={}", ValidationType.SVCS, ValidationUtils.VALIDATION_ERROR, id, regulationMode, q, bMin, bMax, v, voltageSetPoint);
            validated = false;
        }
        return validated;
    }

}
