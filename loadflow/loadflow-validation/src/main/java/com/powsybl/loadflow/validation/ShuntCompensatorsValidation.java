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
        return network.getShuntCompensatorStream()
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

        double p = shunt.getTerminal().getP();
        double q = shunt.getTerminal().getQ();
        int currentSectionCount = shunt.getCurrentSectionCount();
        int maximumSectionCount = shunt.getMaximumSectionCount();
        double bPerSection = shunt.getbPerSection();
        double nominalV = shunt.getTerminal().getVoltageLevel().getNominalV();
        double qMax = bPerSection * maximumSectionCount * nominalV * nominalV;
        Bus bus = shunt.getTerminal().getBusView().getBus();
        double v = bus != null ? bus.getV() : Double.NaN;
        boolean connected = bus != null;
        Bus connectableBus = shunt.getTerminal().getBusView().getConnectableBus();
        boolean connectableMainComponent = connectableBus != null && connectableBus.isInMainConnectedComponent();
        boolean mainComponent = bus != null ? bus.isInMainConnectedComponent() : connectableMainComponent;
        return checkShunts(shunt.getId(), p, q, currentSectionCount, maximumSectionCount, bPerSection, v, qMax, nominalV, connected, mainComponent, config, shuntsWriter);
    }

    public static boolean checkShunts(String id, double p, double q, int currentSectionCount, int maximumSectionCount, double bPerSection,
                                      double v, double qMax, double nominalV, boolean connected, boolean mainComponent, ValidationConfig config,
                                      Writer writer) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);

        try (ValidationWriter shuntsWriter = ValidationUtils.createValidationWriter(id, config, writer, ValidationType.SHUNTS)) {
            return checkShunts(id, p, q, currentSectionCount, maximumSectionCount, bPerSection, v, qMax, nominalV, connected, mainComponent, config, shuntsWriter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean checkShunts(String id, double p, double q, int currentSectionCount, int maximumSectionCount, double bPerSection,
                                      double v, double qMax, double nominalV, boolean connected, boolean mainComponent, ValidationConfig config,
                                      ValidationWriter shuntsWriter) {
        boolean validated = true;

        if (!connected && !Double.isNaN(q) && q != 0) { // if the shunt is disconnected then either “q” is not defined or “q” is 0
            LOGGER.warn("{} {}: {}: disconnected shunt Q {}", ValidationType.SHUNTS, ValidationUtils.VALIDATION_ERROR, id, q);
            validated = false;
        }
        // “q” = - bPerSection * currentSectionCount * v^2
        double expectedQ = -bPerSection * currentSectionCount * v * v;
        if (connected && ValidationUtils.isMainComponent(config, mainComponent)) {
            // “p” is always NaN
            if (!Double.isNaN(p)) {
                LOGGER.warn("{} {}: {}: P={}", ValidationType.SHUNTS, ValidationUtils.VALIDATION_ERROR, id, p);
                validated = false;
            }
            if (ValidationUtils.areNaN(config, q, expectedQ) || Math.abs(q - expectedQ) > config.getThreshold()) {
                LOGGER.warn("{} {}: {}:  Q {} {}", ValidationType.SHUNTS, ValidationUtils.VALIDATION_ERROR, id, q, expectedQ);
                validated = false;
            }
        }
        try {
            shuntsWriter.write(id, q, expectedQ, p, currentSectionCount, maximumSectionCount, bPerSection, v, connected, qMax, nominalV, mainComponent, validated);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return validated;
    }
}
