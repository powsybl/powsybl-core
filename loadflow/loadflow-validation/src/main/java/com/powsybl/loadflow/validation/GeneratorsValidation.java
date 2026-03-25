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

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.loadflow.validation.io.ValidationWriter;

import static com.powsybl.loadflow.validation.ValidationUtils.*;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
public final class GeneratorsValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeneratorsValidation.class);

    public static final GeneratorsValidation INSTANCE = new GeneratorsValidation();

    private GeneratorsValidation() {
    }

    public boolean checkGenerators(Network network, ValidationConfig config, Path file) throws IOException {
        Objects.requireNonNull(network);
        Objects.requireNonNull(config);
        Objects.requireNonNull(file);
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            return checkGenerators(network, config, writer);
        }
    }

    public boolean checkGenerators(Network network, ValidationConfig config, Writer writer) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);
        try (ValidationWriter generatorsWriter = ValidationUtils.createValidationWriter(network.getId(), config, writer, ValidationType.GENERATORS)) {
            return checkGenerators(network, config, generatorsWriter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public boolean checkGenerators(Network network, ValidationConfig config, ValidationWriter generatorsWriter) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(config);
        Objects.requireNonNull(generatorsWriter);
        LOGGER.info("Checking generators of network {}", network.getId());
        BalanceTypeGuesser guesser = new BalanceTypeGuesser(network, config.getThreshold());
        LOGGER.info("Using {} balance type", guesser.getBalanceType());
        return network.getGeneratorStream()
                      .sorted(Comparator.comparing(Generator::getId))
                      .map(gen -> checkGenerators(gen, config, generatorsWriter, guesser))
                      .reduce(Boolean::logicalAnd)
                      .orElse(true);
    }

    public boolean checkGenerators(Generator gen, ValidationConfig config, Writer writer) {
        Objects.requireNonNull(gen);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);

        try (ValidationWriter generatorsWriter = ValidationUtils.createValidationWriter(gen.getId(), config, writer, ValidationType.GENERATORS)) {
            return checkGenerators(gen, config, generatorsWriter, new BalanceTypeGuesser());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public boolean checkGenerators(Generator gen, ValidationConfig config, ValidationWriter generatorsWriter, BalanceTypeGuesser guesser) {
        Objects.requireNonNull(gen);
        Objects.requireNonNull(config);
        Objects.requireNonNull(generatorsWriter);
        double p = gen.getTerminal().getP();
        double q = gen.getTerminal().getQ();
        double targetP = gen.getTargetP();
        double targetQ = gen.getTargetQ();
        double targetV = gen.getTargetV();
        boolean voltageRegulatorOn = gen.isVoltageRegulatorOn();
        double minP = gen.getMinP();
        double maxP = gen.getMaxP();
        double minQ = gen.getReactiveLimits().getMinQ(targetP);
        double maxQ = gen.getReactiveLimits().getMaxQ(targetP);
        ValidationUtils.TerminalState terminalState = ValidationUtils.getTerminalState(gen.getTerminal());
        double v = terminalState.v();
        boolean connected = terminalState.connected();
        boolean mainComponent = terminalState.mainComponent();
        return checkGenerators(gen.getId(), p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minP, maxP, minQ, maxQ, connected,
                               mainComponent, config, generatorsWriter, guesser);
    }

    public boolean checkGenerators(String id, double p, double q, double v, double targetP, double targetQ, double targetV,
                                          boolean voltageRegulatorOn, double minP, double maxP, double minQ, double maxQ, boolean connected,
                                          boolean mainComponent, ValidationConfig config, Writer writer) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);

        try (ValidationWriter generatorsWriter = ValidationUtils.createValidationWriter(id, config, writer, ValidationType.GENERATORS)) {
            return checkGenerators(id, p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minP, maxP, minQ, maxQ, connected, mainComponent, config,
                    generatorsWriter, new BalanceTypeGuesser());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public boolean checkGenerators(String id, double p, double q, double v, double targetP, double targetQ, double targetV,
                                          boolean voltageRegulatorOn, double minP, double maxP, double minQ, double maxQ, boolean connected,
                                          boolean mainComponent, ValidationConfig config, ValidationWriter generatorsWriter, BalanceTypeGuesser guesser) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(config);
        Objects.requireNonNull(generatorsWriter);
        boolean validated = true;

        double expectedP = getExpectedP(guesser, id, p, targetP, minP, maxP, config.getThreshold());
        if (connected && ValidationUtils.isMainComponent(config, mainComponent)) {
            if (areNaN(p, q)) {
                validated = validateMissingPQRule(id, p, q, targetP, targetQ);
            } else if (isReactiveBoundInverted(minQ, maxQ, config.getThreshold(), config.isNoRequirementIfReactiveBoundInversion())) {
                validated = true;
            } else if (isSetpointOutsidePowerBounds(targetP, minP, maxP, config.getThreshold(), config.isNoRequirementIfSetpointOutsidePowerBounds())) {
                validated = true;
            } else {
                validated = checkGeneratorsValues(id, p, q, v, expectedP, targetQ, targetV, voltageRegulatorOn, minQ, maxQ, config);
            }
        }
        try {
            generatorsWriter.write(id, p, q, v, targetP, targetQ, targetV, expectedP, connected, voltageRegulatorOn, minP, maxP, minQ, maxQ, mainComponent, validated);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return validated;
    }

    private static double getExpectedP(BalanceTypeGuesser guesser, String id, double p, double targetP, double minP, double maxP, double threshold) {
        if (Math.abs(p + targetP) <= threshold) {
            return targetP;
        }
        switch (guesser.getBalanceType()) {
            case NONE:
                return id.equals(guesser.getSlack()) ? -p : targetP;
            case PROPORTIONAL_TO_GENERATION_P_MAX:
                return Math.max(Math.max(0, minP), Math.min(maxP, targetP + maxP * guesser.getKMax()));
            case PROPORTIONAL_TO_GENERATION_P:
                return Math.max(Math.max(0, minP), Math.min(maxP, targetP + targetP * guesser.getKTarget()));
            case PROPORTIONAL_TO_GENERATION_HEADROOM:
                return Math.max(Math.max(0, minP), Math.min(maxP, targetP + (maxP - targetP) * guesser.getKHeadroom()));
            default:
                throw new IllegalStateException("Unhandled Balance Type: " + guesser.getBalanceType());
        }
    }

    private static boolean validateMissingPQRule(String id, double p, double q, double targetP, double targetQ) {
        // a validation error should be detected if there is both a voltage and a target but no p or q
        if (!Double.isNaN(targetP) && targetP != 0 || !Double.isNaN(targetQ) && targetQ != 0) {
            LOGGER.warn("{} {}: {}: P={} targetP={} - Q={} targetQ={}", ValidationType.GENERATORS, ValidationUtils.VALIDATION_ERROR, id, p, targetP, q, targetQ);
            return false;
        }
        return true;
    }

    private static boolean checkGeneratorsValues(String id, double p, double q, double v, double expectedP, double targetQ, double targetV,
                                                 boolean voltageRegulatorOn, double minQ, double maxQ, ValidationConfig config) {
        boolean validated = true;
        double threshold = config.getThreshold();
        // Rule: Active power p matches expected setpoint
        if (isActivePowerKo(p, expectedP, config, threshold)) {
            LOGGER.warn("{} {}: {}: P={} expectedP={}",
                    ValidationType.GENERATORS, ValidationUtils.VALIDATION_ERROR, id, p, expectedP);
            validated = false;
        }

        //Rule: If voltage regulator is disabled, Reactive power Q matches targetQ
        if (!voltageRegulatorOn && (areNaN(config, targetQ) || isReactivePowerKo(q, targetQ, threshold))) {
            LOGGER.warn("{} {}: {}: voltage regulator off - Q={} targetQ={}", ValidationType.GENERATORS, ValidationUtils.VALIDATION_ERROR, id, q, targetQ);
            validated = false;
        }

        double qGen = -q;
        // Rule: If voltage regulator ON, Reactive power q follow V/targetV logic
        // - qGen at minQ if V > targetV + threshold
        // - qGen at maxQ if V < targetV - threshold
        // - else qGen within [minQ, maxQ])
        if (voltageRegulatorOn && (ValidationUtils.areNaN(config, minQ, maxQ, targetV) || isVoltageRegulationKo(qGen, v, targetV, minQ, maxQ, threshold))) {
            LOGGER.warn("{} {}: {}: voltage regulator on - Q={} minQ={} maxQ={} - V={} targetV={}", ValidationType.GENERATORS, ValidationUtils.VALIDATION_ERROR, id, qGen, minQ, maxQ, v, targetV);
            validated = false;
        }
        return validated;
    }
}
