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
        boolean generatorsValidated = network.getGeneratorStream()
                                             .sorted(Comparator.comparing(Generator::getId))
                                             .map(gen -> checkGenerators(gen, config, generatorsWriter))
                                             .reduce(Boolean::logicalAnd)
                                             .orElse(true);
        return generatorsValidated;
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
            // a validation error should be detected if there is both a voltage and a target but no p or q
            if (Float.isNaN(p) || Float.isNaN(q)) {
                if ((!Float.isNaN(targetP) && targetP != 0)
                    || (!Float.isNaN(targetQ) && targetQ != 0)) {
                    LOGGER.warn(ValidationType.GENERATORS + " " + ValidationUtils.VALIDATION_ERROR + ": " + id + ": P=" + p + " targetP=" + targetP + " - Q=" + q + " targetQ=" + targetQ);
                    validated = false;
                } else {
                    validated = true;
                }
            } else {
                // active power should be equal to set point
                if ((Float.isNaN(targetP) && !config.areOkMissingValues()) || Math.abs(p + targetP) > config.getThreshold()) {
                    LOGGER.warn(ValidationType.GENERATORS + " " + ValidationUtils.VALIDATION_ERROR + ": " + id + ": P=" + p + " targetP=" + targetP);
                    validated = false;
                }
                // if voltageRegulatorOn="false" then reactive power should be equal to set point
                if (!voltageRegulatorOn && ((Float.isNaN(targetQ) && !config.areOkMissingValues()) || Math.abs(q + targetQ) > config.getThreshold())) {
                    LOGGER.warn(ValidationType.GENERATORS + " " + ValidationUtils.VALIDATION_ERROR + ": " + id + ": voltage regulator off - Q=" + q + " targetQ=" + targetQ);
                    validated = false;
                }
                // if voltageRegulatorOn="true" then
                // either q is equal to g.getReactiveLimits().getMinQ(p) and V is lower than g.getTargetV()
                // or q is equal to g.getReactiveLimits().getMaxQ(p) and V is higher than g.getTargetV()
                // or V at the connected bus is equal to g.getTargetV()
                if (voltageRegulatorOn
                    && (((Float.isNaN(minQ) || Float.isNaN(maxQ) || Float.isNaN(targetV)) && !config.areOkMissingValues())
                        || ((Math.abs(q + minQ) > config.getThreshold() || (v - targetV) >= config.getThreshold())
                            && (Math.abs(q + maxQ) > config.getThreshold() || (targetV - v) >= config.getThreshold())
                            && Math.abs(v - targetV) > config.getThreshold()))) {
                    LOGGER.warn(ValidationType.GENERATORS + " " + ValidationUtils.VALIDATION_ERROR + ": " + id + ": voltage regulator on - Q=" + q + " minQ=" + minQ + " maxQ=" + maxQ + " - V=" + v + " targetV=" + targetV);
                    validated = false;
                }
            }
            generatorsWriter.write(id, p, q, v, targetP, targetQ, targetV, true, voltageRegulatorOn, minQ, maxQ, validated);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return validated;
    }

}
