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
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.loadflow.validation.io.ValidationWriter;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public final class ShuntCompensatorsValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(StaticVarCompensatorsValidation.class);

    private ShuntCompensatorsValidation() {
    }

    public static boolean checkShunts(Network network, ValidationConfig config, Path file) throws IOException {
        Objects.requireNonNull(file);
        Objects.requireNonNull(config);
        Objects.requireNonNull(file);
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            return checkShunts(network, config, writer);
        }
    }

    public static boolean checkShunts(Network network, ValidationConfig config, Writer writer) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);
        try (ValidationWriter shuntsWriter = ValidationUtils.createValidationWriter(network.getId(), config, writer, ValidationType.SHUNTS)) {
            return checkShunts(network, config, shuntsWriter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean checkShunts(Network network, ValidationConfig config, ValidationWriter shuntsWriter) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(config);
        Objects.requireNonNull(shuntsWriter);
        LOGGER.info("Checking shunt compensators of network {}", network.getId());
        return network.getShuntStream()
                      .sorted(Comparator.comparing(ShuntCompensator::getId))
                      .map(shunt -> checkShunts(shunt, config, shuntsWriter))
                      .reduce(Boolean::logicalAnd)
                      .orElse(true);
    }

    public static boolean checkShunts(ShuntCompensator shunt, ValidationConfig config, Writer writer) {
        Objects.requireNonNull(shunt);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);

        try (ValidationWriter shuntsWriter = ValidationUtils.createValidationWriter(shunt.getId(), config, writer, ValidationType.SHUNTS)) {
            return checkShunts(shunt, config, shuntsWriter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean checkShunts(ShuntCompensator shunt, ValidationConfig config, ValidationWriter shuntsWriter) {
        Objects.requireNonNull(shunt);
        Objects.requireNonNull(config);
        Objects.requireNonNull(shuntsWriter);

        boolean validated = true;
        float p = shunt.getTerminal().getP();
        float q = shunt.getTerminal().getQ();
        int currentSectionCount = shunt.getCurrentSectionCount();
        int maximumSectionCount = shunt.getMaximumSectionCount();
        float bPerSection = shunt.getbPerSection();
        float nominalV = shunt.getTerminal().getVoltageLevel().getNominalV();
        float qMax = bPerSection * maximumSectionCount * nominalV * nominalV;
        Bus bus = shunt.getTerminal().getBusView().getBus();
        if (bus != null && !Float.isNaN(bus.getV())) {
            float v = bus.getV();
            return checkShunts(shunt.getId(), p, q, currentSectionCount, maximumSectionCount, bPerSection, v, qMax, nominalV, config, shuntsWriter);
        } else if (!Float.isNaN(q) && q != 0) { // if the shunt is disconnected then either “q” is not defined or “q” is 0
            LOGGER.warn("{} {}: {}: disconnected shunt Q {}", ValidationType.SHUNTS, ValidationUtils.VALIDATION_ERROR, shunt.getId(), q);
            validated = false;
        }
        try {
            shuntsWriter.write(shunt.getId(), q, Float.NaN, p, currentSectionCount, maximumSectionCount, bPerSection, Float.NaN, shunt.getTerminal().isConnected(), qMax, nominalV, validated);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return validated;
    }

    public static boolean checkShunts(String id, float p, float q, int currentSectionCount, int maximumSectionCount, float bPerSection,
                                      float v, float qMax, float nominalV, ValidationConfig config, Writer writer) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);

        try (ValidationWriter shuntsWriter = ValidationUtils.createValidationWriter(id, config, writer, ValidationType.SHUNTS)) {
            return checkShunts(id, p, q, currentSectionCount, maximumSectionCount, bPerSection, v, qMax, nominalV, config, shuntsWriter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean checkShunts(String id, float p, float q, int currentSectionCount, int maximumSectionCount, float bPerSection,
                                      float v, float qMax, float nominalV, ValidationConfig config, ValidationWriter shuntsWriter) {
        boolean validated = true;
        // “p” is always NaN
        if (!Float.isNaN(p)) {
            LOGGER.warn("{} {}: {}: P={}", ValidationType.SHUNTS, ValidationUtils.VALIDATION_ERROR, id, p);
            validated = false;
        }
        // “q” = - bPerSection * currentSectionCount * v^2
        float expectedQ = -bPerSection * currentSectionCount * v * v;
        if (((Float.isNaN(q) || Float.isNaN(expectedQ)) && !config.areOkMissingValues())
            || Math.abs(q - expectedQ) > config.getThreshold()) {
            LOGGER.warn("{} {}: {}:  Q {} {}", ValidationType.SHUNTS, ValidationUtils.VALIDATION_ERROR, id, q, expectedQ);
            validated = false;
        }
        try {
            shuntsWriter.write(id, q, expectedQ, p, currentSectionCount, maximumSectionCount, bPerSection, v, true, qMax, nominalV, validated);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return validated;
    }
}
