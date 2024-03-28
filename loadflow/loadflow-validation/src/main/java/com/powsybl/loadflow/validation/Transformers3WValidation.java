/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.validation;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;

import java.util.Comparator;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerPhaseAngleClock;
import com.powsybl.iidm.network.util.TwtData;
import com.powsybl.loadflow.validation.io.ValidationWriter;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
public final class Transformers3WValidation extends AbstractTransformersValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(Transformers3WValidation.class);

    public static final Transformers3WValidation INSTANCE = new Transformers3WValidation();

    private Transformers3WValidation() {
    }

    @Override
    public boolean checkTransformers(Network network, ValidationConfig config, ValidationWriter twtsWriter) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(config);
        Objects.requireNonNull(twtsWriter);

        LOGGER.info("Checking 3W transformers of network {}", network.getId());
        return network.getThreeWindingsTransformerStream()
                .sorted(Comparator.comparing(ThreeWindingsTransformer::getId))
                .map(twt -> checkTransformer(twt, config, twtsWriter))
                .reduce(Boolean::logicalAnd)
                .orElse(true);
    }

    public boolean checkTransformer(ThreeWindingsTransformer twt, ValidationConfig config, Writer writer) {
        Objects.requireNonNull(twt);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);

        try (ValidationWriter twtsWriter = ValidationUtils.createValidationWriter(twt.getId(), config, writer, ValidationType.TWTS3W)) {
            return checkTransformer(twt, config, twtsWriter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public boolean checkTransformer(ThreeWindingsTransformer twt, ValidationConfig config, ValidationWriter twtsWriter) {
        Objects.requireNonNull(twt);
        Objects.requireNonNull(config);
        Objects.requireNonNull(twtsWriter);

        int phaseAngleClock2 = 0;
        int phaseAngleClock3 = 0;
        ThreeWindingsTransformerPhaseAngleClock phaseAngleClockExtension = twt.getExtension(ThreeWindingsTransformerPhaseAngleClock.class);
        if (phaseAngleClockExtension != null) {
            phaseAngleClock2 = phaseAngleClockExtension.getPhaseAngleClockLeg2();
            phaseAngleClock3 = phaseAngleClockExtension.getPhaseAngleClockLeg3();
        }

        boolean validated = true;
        TwtData twtData = new TwtData(twt, phaseAngleClock2, phaseAngleClock3, config.getEpsilonX(),
            config.applyReactanceCorrection(), config.getLoadFlowParameters().isTwtSplitShuntAdmittance());
        validated &= checkLeg(twtData, ThreeSides.ONE, config);
        validated &= checkLeg(twtData, ThreeSides.TWO, config);
        validated &= checkLeg(twtData, ThreeSides.THREE, config);

        try {
            twtsWriter.write(twt.getId(), twtData, validated);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return validated;
    }

    private static boolean checkLeg(TwtData twtData, ThreeSides side, ValidationConfig config) {
        boolean validated = true;
        if (twtData.isConnected(side) && twtData.isMainComponent(side)) {
            if (ValidationUtils.areNaN(config, twtData.getP(side), twtData.getComputedP(side)) || Math.abs(twtData.getP(side) - twtData.getComputedP(side)) > config.getThreshold()) {
                LOGGER.warn("{} {}: {} side {}, P {} {}", ValidationType.TWTS3W, ValidationUtils.VALIDATION_ERROR, twtData.getId(), side, twtData.getP(side), twtData.getComputedP(side));
                validated = false;
            }
            if (ValidationUtils.areNaN(config, twtData.getQ(side), twtData.getComputedQ(side)) || Math.abs(twtData.getQ(side) - twtData.getComputedQ(side)) > config.getThreshold()) {
                LOGGER.warn("{} {}: {} side {}, Q {} {}", ValidationType.TWTS3W, ValidationUtils.VALIDATION_ERROR, twtData.getId(), side, twtData.getQ(side), twtData.getComputedQ(side));
                validated = false;
            }
        }
        return validated;
    }

}
