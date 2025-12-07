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

import com.powsybl.loadflow.validation.data.GeneratorData;
import com.powsybl.loadflow.validation.data.Validated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.loadflow.validation.io.ValidationWriter;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
public final class GeneratorsValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeneratorsValidation.class);

    private GeneratorsValidation() {
    }

    public static boolean checkGenerators(Network network, ValidationConfig config, Path file) throws IOException {
        Objects.requireNonNull(network);
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
        BalanceTypeGuesser guesser = new BalanceTypeGuesser(network, config.getThreshold());
        LOGGER.info("Using {} balance type", guesser.getBalanceType());
        return network.getGeneratorStream()
                      .sorted(Comparator.comparing(Generator::getId))
                      .map(gen -> checkGenerators(gen, config, generatorsWriter, guesser))
                      .reduce(Boolean::logicalAnd)
                      .orElse(true);
    }

    public static boolean checkGenerators(Generator gen, ValidationConfig config, Writer writer) {
        Objects.requireNonNull(gen);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);

        try (ValidationWriter generatorsWriter = ValidationUtils.createValidationWriter(gen.getId(), config, writer, ValidationType.GENERATORS)) {
            return checkGenerators(gen, config, generatorsWriter, BalanceTypeGuesser.NO_BALANCING_GUESSER);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean checkGenerators(Generator gen, ValidationConfig config, ValidationWriter generatorsWriter, BalanceTypeGuesser guesser) {
        Objects.requireNonNull(gen);
        Objects.requireNonNull(config);
        Objects.requireNonNull(generatorsWriter);
        Objects.requireNonNull(guesser);
        double p = gen.getTerminal().getP();
        double q = gen.getTerminal().getQ();
        Bus bus = gen.getTerminal().getBusView().getBus();
        double targetP = gen.getTargetP();
        double targetQ = gen.getTargetQ();
        double targetV = gen.getTargetV();
        boolean voltageRegulatorOn = gen.isVoltageRegulatorOn();
        double minP = gen.getMinP();
        double maxP = gen.getMaxP();
        double minQ = gen.getReactiveLimits().getMinQ(targetP);
        double maxQ = gen.getReactiveLimits().getMaxQ(targetP);
        double v = bus != null ? bus.getV() : Double.NaN;
        boolean connected = bus != null;
        Bus connectableBus = gen.getTerminal().getBusView().getConnectableBus();
        boolean connectableMainComponent = connectableBus != null && connectableBus.isInMainConnectedComponent();
        boolean mainComponent = bus != null ? bus.isInMainConnectedComponent() : connectableMainComponent;
        GeneratorData d = new GeneratorData(gen.getId(),
                p, q, v,
                targetP, targetQ, targetV,
                voltageRegulatorOn,
                minP, maxP, minQ, maxQ,
                connected, mainComponent,
                guesser);
        return checkGenerators(d, config, generatorsWriter, guesser);
    }

    public static boolean checkGenerators(GeneratorData d, ValidationConfig config, Writer writer) {
        Objects.requireNonNull(d);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);

        try (ValidationWriter generatorsWriter = ValidationUtils.createValidationWriter(d.generatorId(), config, writer, ValidationType.GENERATORS)) {
            return checkGenerators(d, config,
                    generatorsWriter, BalanceTypeGuesser.NO_BALANCING_GUESSER);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean checkGenerators(GeneratorData d, ValidationConfig config, ValidationWriter generatorsWriter, BalanceTypeGuesser guesser) {
        Objects.requireNonNull(d);
        Objects.requireNonNull(config);
        Objects.requireNonNull(generatorsWriter);
        Objects.requireNonNull(guesser);
        boolean validated = true;

        String id = d.generatorId();

        if (d.connected() && ValidationUtils.isMainComponent(config, d.mainComponent())) {
            if (Double.isNaN(d.p()) || Double.isNaN(d.q())) {
                validated = checkGeneratorsNaNValues(id, d.p(), d.q(), d.targetP(), d.targetQ());
            } else if (checkReactiveBoundInversion(d.minQ(), d.maxQ(), config)) { // when maxQ < minQ if noRequirementIfReactiveBoundInversion return true
                validated = true;
            } else if (checkSetpointOutsidePowerBounds(d.targetP(), d.minP(), d.maxP(), config)) { // when targetP < minP or targetP > maxP if noRequirementIfSetpointOutsidePowerBounds return true
                validated = true;
            } else {
                validated = checkGeneratorsValues(d, config);
            }
        }
        try {
            generatorsWriter.writeGenerator(new Validated<>(d, validated));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return validated;
    }

    private static boolean checkGeneratorsNaNValues(String id, double p, double q, double targetP, double targetQ) {
        // a validation error should be detected if there is both a voltage and a target but no p or q
        if (!Double.isNaN(targetP) && targetP != 0
                || !Double.isNaN(targetQ) && targetQ != 0) {
            LOGGER.warn("{} {}: {}: P={} targetP={} - Q={} targetQ={}", ValidationType.GENERATORS, ValidationUtils.VALIDATION_ERROR, id, p, targetP, q, targetQ);
            return false;
        }
        return true;
    }

    private static boolean checkGeneratorsValues(GeneratorData d, ValidationConfig config) {
        boolean validated = true;
        String id = d.generatorId();
        // active power should be equal to setpoint
        if (ValidationUtils.areNaN(config, d.expectedP()) || Math.abs(d.p() + d.expectedP()) > config.getThreshold()) {
            LOGGER.warn("{} {}: {}: P={} expectedP={}", ValidationType.GENERATORS, ValidationUtils.VALIDATION_ERROR, id, d.p(), d.expectedP());
            validated = false;
        }
        // if voltageRegulatorOn="false" then reactive power should be equal to setpoint
        if (!d.voltageRegulatorOn() && (ValidationUtils.areNaN(config, d.targetQ()) || Math.abs(d.q() + d.targetQ()) > config.getThreshold())) {
            LOGGER.warn("{} {}: {}: voltage regulator off - Q={} targetQ={}", ValidationType.GENERATORS, ValidationUtils.VALIDATION_ERROR, id, d.q(), d.targetQ());
            validated = false;
        }
        // if voltageRegulatorOn="true" then
        // either q is equal to g.getReactiveLimits().getMinQ(p) and V is higher than g.getTargetV()
        // or q is equal to g.getReactiveLimits().getMaxQ(p) and V is lower than g.getTargetV()
        // or V at the connected bus is equal to g.getTargetV() and the reactive bounds are satisfied
        double qGen = -d.q();
        if (d.voltageRegulatorOn()
            && (ValidationUtils.areNaN(config, d.minQ(), d.maxQ(), d.targetV())
                || d.v() > d.targetV() + config.getThreshold() && Math.abs(qGen - getMinQ(d.minQ(), d.maxQ())) > config.getThreshold()
                || d.v() < d.targetV() - config.getThreshold() && Math.abs(qGen - getMaxQ(d.minQ(), d.maxQ())) > config.getThreshold()
                || Math.abs(d.v() - d.targetV()) <= config.getThreshold() && !ValidationUtils.boundedWithin(d.minQ(), d.maxQ(), qGen, config.getThreshold()))) {
            LOGGER.warn("{} {}: {}: voltage regulator on - Q={} minQ={} maxQ={} - V={} targetV={}", ValidationType.GENERATORS, ValidationUtils.VALIDATION_ERROR, id, qGen, d.minQ(), d.maxQ(), d.v(), d.targetV());
            validated = false;
        }
        return validated;
    }

    private static double getMaxQ(double minQ, double maxQ) {
        return Math.max(maxQ, minQ);
    }

    private static double getMinQ(double minQ, double maxQ) {
        return Math.min(maxQ, minQ);
    }

    private static boolean checkReactiveBoundInversion(double minQ, double maxQ, ValidationConfig config) {
        return maxQ < minQ - config.getThreshold() && config.isNoRequirementIfReactiveBoundInversion();
    }

    private static boolean checkSetpointOutsidePowerBounds(double targetP, double minP, double maxP, ValidationConfig config) {
        return (targetP < minP - config.getThreshold() || targetP > maxP + config.getThreshold()) && config.isNoRequirementIfSetpointOutsidePowerBounds();
    }

}
