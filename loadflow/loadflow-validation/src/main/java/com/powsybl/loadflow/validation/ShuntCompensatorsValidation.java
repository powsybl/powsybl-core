/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.validation;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.loadflow.validation.data.ShuntData;
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
public final class ShuntCompensatorsValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShuntCompensatorsValidation.class);

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
        return network.getShuntCompensatorStream()
                .sorted(Comparator.comparing(ShuntCompensator::getId))
                .map(shunt -> checkShunt(shunt, config, shuntsWriter))
                .reduce(Boolean::logicalAnd)
                .orElse(true);
    }

    public static boolean checkShunt(ShuntCompensator shunt, ValidationConfig config, Writer writer) {
        Objects.requireNonNull(shunt);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);

        try (ValidationWriter shuntsWriter = ValidationUtils.createValidationWriter(shunt.getId(), config, writer, ValidationType.SHUNTS)) {
            return checkShunt(shunt, config, shuntsWriter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean checkShunt(ShuntCompensator shunt, ValidationConfig config, ValidationWriter shuntsWriter) {
        Objects.requireNonNull(shunt);
        Objects.requireNonNull(config);
        Objects.requireNonNull(shuntsWriter);

        if (shunt.getModelType() == ShuntCompensatorModelType.NON_LINEAR) {
            throw new PowsyblException("non linear shunt not supported yet");
        }

        double p = shunt.getTerminal().getP();
        double q = shunt.getTerminal().getQ();
        int currentSectionCount = shunt.getSectionCount();
        int maximumSectionCount = shunt.getMaximumSectionCount();
        double bPerSection = shunt.getModel(ShuntCompensatorLinearModel.class).getBPerSection();
        double nominalV = shunt.getTerminal().getVoltageLevel().getNominalV();
        double qMax = bPerSection * maximumSectionCount * nominalV * nominalV;
        TerminalState terminalState = getTerminalState(shunt.getTerminal());
        ShuntData d = new ShuntData(shunt.getId(),
                p, q,
                currentSectionCount, maximumSectionCount,
                bPerSection,
                qMax,
                terminalState.v(), terminalState.connected(),
                nominalV,
                terminalState.mainComponent());
        return checkShunt(d, config, shuntsWriter);
    }

    public static boolean checkShunt(ShuntData d, ValidationConfig config, Writer writer) {
        Objects.requireNonNull(d);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);

        try (ValidationWriter shuntsWriter = ValidationUtils.createValidationWriter(d.shuntId(), config, writer, ValidationType.SHUNTS)) {
            return checkShunt(d, config, shuntsWriter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Rules for valid results :<br/>
     * - Rule: |p| < e <br/>
     * - Rule: q must match expectedQ <br/>
     * - Rule: if the shunt is disconnected, q should be NaN or 0
     */
    public static boolean checkShunt(ShuntData d, ValidationConfig config,
                               ValidationWriter shuntsWriter) {
        boolean validated = true;
        double threshold = config.getThreshold();
        String id = d.shuntId();

        // Rule: if the shunt is disconnected, q should be NaN or 0
        if (!d.connected() && !isNaNOrZero(d.q(), config.getThreshold())) { // if the shunt is disconnected then either “q” is not defined or “q” is 0
            LOGGER.warn("{} {}: {}: disconnected shunt Q {}", ValidationType.SHUNTS, ValidationUtils.VALIDATION_ERROR, id, d.q());
            validated = false;
        }
        if (isConnectedAndMainComponent(d.connected(), d.mainComponent(), config)) {
            // Rule: |p| < e
            if (!Double.isNaN(d.p()) && Math.abs(d.p()) > threshold) {
                LOGGER.warn("{} {}: {}: P={}", ValidationType.SHUNTS, ValidationUtils.VALIDATION_ERROR, id, d.p());
                validated = false;
            }
            // Rule: q must match expectedQ
            if (areNaN(config, d.q(), d.expectedQ()) || isOutsideTolerance(d.q(), d.expectedQ(), threshold)) {
                LOGGER.warn("{} {}: {}:  Q {} {}", ValidationType.SHUNTS, ValidationUtils.VALIDATION_ERROR, id, d.q(), d.expectedQ());
                validated = false;
            }
        }
        try {
            shuntsWriter.writeShunt(new Validated<>(d, validated));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return validated;
    }
}
