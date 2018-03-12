/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
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
import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.TwoTerminalsConnectable.Side;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.loadflow.validation.io.ValidationWriter;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public final class TransformersValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransformersValidation.class);

    private TransformersValidation() {
    }

    public static boolean checkTransformers(Network network, ValidationConfig config, Path file) throws IOException {
        Objects.requireNonNull(file);
        Objects.requireNonNull(config);
        Objects.requireNonNull(file);

        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            return checkTransformers(network, config, writer);
        }
    }

    public static boolean checkTransformers(Network network, ValidationConfig config, Writer writer) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);

        try (ValidationWriter twtsWriter = ValidationUtils.createValidationWriter(network.getId(), config, writer, ValidationType.TWTS)) {
            return checkTransformers(network, config, twtsWriter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean checkTransformers(Network network, ValidationConfig config, ValidationWriter twtsWriter) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(config);
        Objects.requireNonNull(twtsWriter);

        LOGGER.info("Checking transformers of network {}", network.getId());
        return network.getTwoWindingsTransformerStream()
                      .filter(TransformersValidation::filterTwt)
                      .sorted(Comparator.comparing(TwoWindingsTransformer::getId))
                      .map(twt -> checkTransformers(twt, config, twtsWriter))
                      .reduce(Boolean::logicalAnd)
                      .orElse(true);
    }

    private static boolean filterTwt(TwoWindingsTransformer twt) {
        return twt.getRatioTapChanger() != null && twt.getRatioTapChanger().isRegulating();
    }

    public static boolean checkTransformers(TwoWindingsTransformer twt, ValidationConfig config, Writer writer) {
        Objects.requireNonNull(twt);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);

        try (ValidationWriter twtsWriter = ValidationUtils.createValidationWriter(twt.getId(), config, writer, ValidationType.TWTS)) {
            return checkTransformers(twt, config, twtsWriter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean checkTransformers(TwoWindingsTransformer twt, ValidationConfig config, ValidationWriter twtsWriter) {
        Objects.requireNonNull(twt);
        Objects.requireNonNull(config);
        Objects.requireNonNull(twtsWriter);

        RatioTapChanger ratioTapChanger = twt.getRatioTapChanger();
        int tapPosition = ratioTapChanger.getTapPosition();
        int lowTapPosition = ratioTapChanger.getLowTapPosition();
        int highTapPosition = ratioTapChanger.getHighTapPosition();
        float rho = ratioTapChanger.getCurrentStep().getRho();
        float rhoPreviousStep = tapPosition == lowTapPosition ? Float.NaN : ratioTapChanger.getStep(tapPosition - 1).getRho();
        float rhoNextStep = tapPosition == highTapPosition ? Float.NaN : ratioTapChanger.getStep(tapPosition + 1).getRho();
        float targetV = ratioTapChanger.getTargetV();
        Side regulatedSide = twt.getTerminal1().equals(ratioTapChanger.getRegulationTerminal()) ? Side.ONE : Side.TWO;
        Bus bus = ratioTapChanger.getRegulationTerminal().getBusView().getBus();
        float v = bus != null ? bus.getV() : Float.NaN;
        boolean connected = bus != null ? true : false;
        Bus connectableBus = ratioTapChanger.getRegulationTerminal().getBusView().getConnectableBus();
        boolean connectableMainComponent = connectableBus != null ? connectableBus.isInMainConnectedComponent() : false;
        boolean mainComponent = bus != null ? bus.isInMainConnectedComponent() : connectableMainComponent;
        return checkTransformers(twt.getId(), rho, rhoPreviousStep, rhoNextStep, tapPosition, lowTapPosition, highTapPosition,
                                 targetV, regulatedSide, v, connected, mainComponent, config, twtsWriter);
    }

    public static boolean checkTransformers(String id, float rho, float rhoPreviousStep, float rhoNextStep, int tapPosition,
                                            int lowTapPosition, int highTapPosition, float targetV, Side regulatedSide, float v,
                                            boolean connected, boolean mainComponent, ValidationConfig config, Writer writer) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);

        try (ValidationWriter twtsWriter = ValidationUtils.createValidationWriter(id, config, writer, ValidationType.TWTS)) {
            return checkTransformers(id, rho, rhoPreviousStep, rhoNextStep, tapPosition, lowTapPosition, highTapPosition,
                                     targetV, regulatedSide, v, connected, mainComponent, config, twtsWriter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean checkTransformers(String id, float rho, float rhoPreviousStep, float rhoNextStep, int tapPosition,
                                            int lowTapPosition, int highTapPosition, float targetV, Side regulatedSide, float v,
                                            boolean connected, boolean mainComponent, ValidationConfig config, ValidationWriter twtsWriter) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(config);
        Objects.requireNonNull(twtsWriter);

        boolean validated = true;
        float error = (v - targetV) / targetV;
        float upIncrement = getUpIncrement(regulatedSide, rho, rhoPreviousStep, rhoNextStep, tapPosition, highTapPosition);
        float downIncrement = getDownIncrement(regulatedSide, rho, rhoPreviousStep, rhoNextStep, tapPosition, lowTapPosition);
        if (connected && mainComponent) {
            validated = checkTransformerSide(id, regulatedSide, error, upIncrement, downIncrement, tapPosition, lowTapPosition, highTapPosition, config);
        }
        try {
            twtsWriter.write(id, error, upIncrement, downIncrement, rho, rhoPreviousStep, rhoNextStep, tapPosition, lowTapPosition,
                             highTapPosition, targetV, regulatedSide, v, connected, mainComponent, validated);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return validated;
    }

    private static float getUpIncrement(Side regulatedSide, float rho, float rhoPreviousStep, float rhoNextStep,
                                        int tapPosition, int highTapPosition) {
        switch (regulatedSide) {
            case ONE:
                return tapPosition == highTapPosition ? Float.NaN : 1 / rhoNextStep - 1 / rho;
            case TWO:
                return tapPosition == highTapPosition ? Float.NaN : rhoNextStep - rho;
            default:
                throw new AssertionError("Unexpected Side value: " + regulatedSide);
        }
    }

    private static float getDownIncrement(Side regulatedSide, float rho, float rhoPreviousStep, float rhoNextStep,
                                          int tapPosition, int lowTapPosition) {
        switch (regulatedSide) {
            case ONE:
                return tapPosition == lowTapPosition ? Float.NaN : 1 / rhoPreviousStep - 1 / rho;
            case TWO:
                return tapPosition == lowTapPosition ? Float.NaN : rhoPreviousStep - rho;
            default:
                throw new AssertionError("Unexpected Side value: " + regulatedSide);
        }
    }

    private static boolean checkTransformerSide(String id, Side side, float error, float upIncrement, float downIncrement, int tapPosition,
                                                int lowTapPosition, int highTapPosition, ValidationConfig config) {
        boolean validated = true;
        float maxIncrement = getMaxIncrement(upIncrement, downIncrement, tapPosition, lowTapPosition, highTapPosition);
        float minIncrement = getMinIncrement(upIncrement, downIncrement, tapPosition, lowTapPosition, highTapPosition);
        if (ValidationUtils.areNaN(config, error)) {
            LOGGER.warn("{} {}: {} side {}: error {}", ValidationType.TWTS, ValidationUtils.VALIDATION_ERROR, id, side, error);
            return false;
        }
        // Error >= -Max(UpIncrement, DownIncrement) - to be tested only if Max(UpIncrement, DownIncrement) > 0
        if (maxIncrement > 0 && (error + maxIncrement) < -config.getThreshold()) {
            LOGGER.warn("{} {}: {} side {}: error {} upIncrement {} downIncrement {}",
                        ValidationType.TWTS, ValidationUtils.VALIDATION_ERROR, id, side, error, upIncrement, downIncrement);
            validated = false;
        }
        // Error <= -Min(UpIncrement, DownIncrement) - to be tested only if Min(UpIncrement, DownIncrement) < 0
        if (minIncrement < 0 && (error + minIncrement) > config.getThreshold()) {
            LOGGER.warn("{} {}: {} side {}: error {} upIncrement {} downIncrement {}",
                        ValidationType.TWTS, ValidationUtils.VALIDATION_ERROR, id, side, error, upIncrement, downIncrement);
            validated = false;
        }
        return validated;
    }

    private static float getMaxIncrement(float upIncrement, float downIncrement, int tapPosition, int lowTapPosition, int highTapPosition) {
        if (tapPosition == highTapPosition) {
            return Math.max(0, downIncrement);
        }
        if (tapPosition == lowTapPosition) {
            return Math.max(upIncrement, 0);
        }
        return Math.max(upIncrement, downIncrement);
    }

    private static float getMinIncrement(float upIncrement, float downIncrement, int tapPosition, int lowTapPosition, int highTapPosition) {
        if (tapPosition == highTapPosition) {
            return Math.min(downIncrement, 0);
        }
        if (tapPosition == lowTapPosition) {
            return Math.min(upIncrement, 0);
        }
        return Math.min(upIncrement, downIncrement);
    }

}
