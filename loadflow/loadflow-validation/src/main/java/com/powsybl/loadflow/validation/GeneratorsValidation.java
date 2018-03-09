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
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.loadflow.validation.io.ValidationWriter;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public final class GeneratorsValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeneratorsValidation.class);

    private GeneratorsValidation() {
    }

    public static boolean checkGenerators(Network network, ValidationConfig config, Path file) throws IOException {
        Objects.requireNonNull(file);
        Objects.requireNonNull(config);
        Objects.requireNonNull(file);
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            return checkGenerators(network, config, writer);
        }
    }

    public static boolean checkGenerators(Network network, ValidationConfig config, Writer writer) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);
        try (ValidationWriter generatorsWriter = ValidationUtils.createValidationWriter(network.getId(), config, writer, ValidationType.GENERATORS)) {
            return checkGenerators(network, config, generatorsWriter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean checkGenerators(Network network, ValidationConfig config, ValidationWriter generatorsWriter) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(config);
        Objects.requireNonNull(generatorsWriter);
        LOGGER.info("Checking generators of network {}", network.getId());
        return network.getGeneratorStream()
                      .sorted(Comparator.comparing(Generator::getId))
                      .map(gen -> checkGenerators(gen, config, generatorsWriter))
                      .reduce(Boolean::logicalAnd)
                      .orElse(true);
    }

    public static boolean checkGenerators(Generator gen, ValidationConfig config, Writer writer) {
        Objects.requireNonNull(gen);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);

        try (ValidationWriter generatorsWriter = ValidationUtils.createValidationWriter(gen.getId(), config, writer, ValidationType.GENERATORS)) {
            return checkGenerators(gen, config, generatorsWriter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean checkGenerators(Generator gen, ValidationConfig config, ValidationWriter generatorsWriter) {
        Objects.requireNonNull(gen);
        Objects.requireNonNull(config);
        Objects.requireNonNull(generatorsWriter);
        float p = gen.getTerminal().getP();
        float q = gen.getTerminal().getQ();
        Bus bus = gen.getTerminal().getBusView().getBus();
        float targetP = gen.getTargetP();
        float targetQ = gen.getTargetQ();
        float targetV = gen.getTargetV();
        boolean voltageRegulatorOn = gen.isVoltageRegulatorOn();
        float minQ = gen.getReactiveLimits().getMinQ(targetP);
        float maxQ = gen.getReactiveLimits().getMaxQ(targetP);
        if (bus != null && !Float.isNaN(bus.getV())) {
            float v = bus.getV();
            return checkGenerators(gen.getId(), p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minQ, maxQ, config, generatorsWriter);
        }
        try {
            generatorsWriter.write(gen.getId(), p, q, Float.NaN, targetP, targetQ, targetV, gen.getTerminal().isConnected(), voltageRegulatorOn, minQ, maxQ, true);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return true;
    }

    public static boolean checkGenerators(String id, float p, float q, float v, float targetP, float targetQ, float targetV,
                                          boolean voltageRegulatorOn, float minQ, float maxQ, ValidationConfig config, Writer writer) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);

        try (ValidationWriter generatorsWriter = ValidationUtils.createValidationWriter(id, config, writer, ValidationType.GENERATORS)) {
            return checkGenerators(id, p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minQ, maxQ, config, generatorsWriter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean checkGenerators(String id, float p, float q, float v, float targetP, float targetQ, float targetV,
                                          boolean voltageRegulatorOn, float minQ, float maxQ, ValidationConfig config, ValidationWriter generatorsWriter) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(config);
        Objects.requireNonNull(generatorsWriter);
        boolean validated = true;
        try {
            if (Float.isNaN(p) || Float.isNaN(q)) {
                validated = checkGeneratorsNaNValues(id, p, q, targetP, targetQ);
            } else if (maxQ < minQ && config.isNoRequirementIfReactiveBoundInversion()) { // when maxQ < minQ if noRequirementIfReactiveBoundInversion return true
                validated = true;
            } else {
                validated = checkGeneratorsValues(id, p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minQ, maxQ, config);
            }
            generatorsWriter.write(id, p, q, v, targetP, targetQ, targetV, true, voltageRegulatorOn, minQ, maxQ, validated);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return validated;
    }

    private static boolean checkGeneratorsNaNValues(String id, float p, float q, float targetP, float targetQ) {
        // a validation error should be detected if there is both a voltage and a target but no p or q
        if ((!Float.isNaN(targetP) && targetP != 0)
            || (!Float.isNaN(targetQ) && targetQ != 0)) {
            LOGGER.warn("{} {}: {}: P={} targetP={} - Q={} targetQ={}", ValidationType.GENERATORS, ValidationUtils.VALIDATION_ERROR, id, p, targetP, q, targetQ);
            return false;
        }
        return true;
    }

    private static boolean checkGeneratorsValues(String id, float p, float q, float v, float targetP, float targetQ, float targetV,
                                                 boolean voltageRegulatorOn, float minQ, float maxQ, ValidationConfig config) {
        boolean validated = true;
        // active power should be equal to setpoint
        if (ValidationUtils.areNaN(config, targetP) || Math.abs(p + targetP) > config.getThreshold()) {
            LOGGER.warn("{} {}: {}: P={} targetP={}", ValidationType.GENERATORS, ValidationUtils.VALIDATION_ERROR, id, p, targetP);
            validated = false;
        }
        // if voltageRegulatorOn="false" then reactive power should be equal to setpoint
        if (!voltageRegulatorOn && (ValidationUtils.areNaN(config, targetQ) || Math.abs(q + targetQ) > config.getThreshold())) {
            LOGGER.warn("{} {}: {}: voltage regulator off - Q={} targetQ={}", ValidationType.GENERATORS, ValidationUtils.VALIDATION_ERROR, id, q, targetQ);
            validated = false;
        }
        // if voltageRegulatorOn="true" then
        // either q is equal to g.getReactiveLimits().getMinQ(p) and V is higher than g.getTargetV()
        // or q is equal to g.getReactiveLimits().getMaxQ(p) and V is lower than g.getTargetV()
        // or V at the connected bus is equal to g.getTargetV() and the reactive bounds are satisfied
        float qg = -q;
        if (voltageRegulatorOn
            && (ValidationUtils.areNaN(config, minQ, maxQ, targetV)
                || ((Math.abs(qg - getMinQ(minQ, maxQ)) > config.getThreshold() || (v - targetV) < config.getThreshold())
                    && (Math.abs(qg - getMaxQ(minQ, maxQ)) > config.getThreshold() || (targetV - v) < config.getThreshold())
                    && (!ValidationUtils.boundedWithin(minQ, maxQ, -q, config.getThreshold()) || Math.abs(v - targetV) > config.getThreshold())))) {
            LOGGER.warn("{} {}: {}: voltage regulator on - Q={} minQ={} maxQ={} - V={} targetV={}", ValidationType.GENERATORS, ValidationUtils.VALIDATION_ERROR, id, q, minQ, maxQ, v, targetV);
            validated = false;
        }
        return validated;
    }

    private static float getMaxQ(float minQ, float maxQ) {
        return maxQ < minQ ? minQ : maxQ;
    }

    private static float getMinQ(float minQ, float maxQ) {
        return maxQ < minQ ? maxQ : minQ;
    }

}
