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

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.loadflow.validation.data.ShuntData;
import com.powsybl.loadflow.validation.data.Validated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.loadflow.validation.io.ValidationWriter;

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
        Bus bus = shunt.getTerminal().getBusView().getBus();
        double v = bus != null ? bus.getV() : Double.NaN;
        boolean connected = bus != null;
        Bus connectableBus = shunt.getTerminal().getBusView().getConnectableBus();
        boolean connectableMainComponent = connectableBus != null && connectableBus.isInMainConnectedComponent();
        boolean mainComponent = bus != null ? bus.isInMainConnectedComponent() : connectableMainComponent;
        ShuntData d = new ShuntData(shunt.getId(),
                p, q,
                currentSectionCount, maximumSectionCount,
                bPerSection,
                qMax,
                v, connected,
                nominalV,
                mainComponent);
        return checkShunts(d, config, shuntsWriter);
    }

    public static boolean checkShunts(ShuntData d, ValidationConfig config,
                               Writer writer) {
        Objects.requireNonNull(d);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);

        try (ValidationWriter shuntsWriter = ValidationUtils.createValidationWriter(d.shuntId(), config, writer, ValidationType.SHUNTS)) {
            return checkShunts(d, config, shuntsWriter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean checkShunts(ShuntData d, ValidationConfig config,
                               ValidationWriter shuntsWriter) {
        boolean validated = true;
        String id = d.shuntId();

        if (!d.connected() && !Double.isNaN(d.q()) && d.q() != 0) { // if the shunt is disconnected then either “q” is not defined or “q” is 0
            LOGGER.warn("{} {}: {}: disconnected shunt Q {}", ValidationType.SHUNTS, ValidationUtils.VALIDATION_ERROR, id, d.q());
            validated = false;
        }
        if (d.connected() && ValidationUtils.isMainComponent(config, d.mainComponent())) {
            // “p” is always NaN
            if (!Double.isNaN(d.p())) {
                LOGGER.warn("{} {}: {}: P={}", ValidationType.SHUNTS, ValidationUtils.VALIDATION_ERROR, id, d.p());
                validated = false;
            }
            if (ValidationUtils.areNaN(config, d.q(), d.expectedQ()) || Math.abs(d.q() - d.expectedQ()) > config.getThreshold()) {
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
