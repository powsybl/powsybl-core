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

import com.powsybl.iidm.network.Branch.Side;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.loadflow.validation.io.ValidationWriter;

/**
 * Tries to validate that transformers regulating voltage have been correclty simulated.
 *
 * We check that the voltage deviation from the target voltage stays inside a deadband around the target voltage,
 * taken equal to the maximum possible voltage increase/decrease for a one-tap change.
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public final class TransformersValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransformersValidation.class);

    private TransformersValidation() {
    }

    public static boolean checkTransformers(Network network, ValidationConfig config, Path file) throws IOException {
        Objects.requireNonNull(network);
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
                      .map(twt -> checkTransformer(twt, config, twtsWriter))
                      .reduce(Boolean::logicalAnd)
                      .orElse(true);
    }

    private static boolean filterTwt(TwoWindingsTransformer twt) {
        return twt.getRatioTapChanger() != null && twt.getRatioTapChanger().isRegulating();
    }

    public static boolean checkTransformer(TwoWindingsTransformer twt, ValidationConfig config, Writer writer) {
        Objects.requireNonNull(twt);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);

        try (ValidationWriter twtsWriter = ValidationUtils.createValidationWriter(twt.getId(), config, writer, ValidationType.TWTS)) {
            return checkTransformer(twt, config, twtsWriter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean checkTransformer(TwoWindingsTransformer twt, ValidationConfig config, ValidationWriter twtsWriter) {
        Objects.requireNonNull(twt);
        Objects.requireNonNull(config);
        Objects.requireNonNull(twtsWriter);

        RatioTapChanger ratioTapChanger = twt.getRatioTapChanger();
        int tapPosition = ratioTapChanger.getTapPosition();
        int lowTapPosition = ratioTapChanger.getLowTapPosition();
        int highTapPosition = ratioTapChanger.getHighTapPosition();
        double rho = ratioTapChanger.getCurrentStep().getRho();
        double rhoPreviousStep = tapPosition == lowTapPosition ? Double.NaN : ratioTapChanger.getStep(tapPosition - 1).getRho();
        double rhoNextStep = tapPosition == highTapPosition ? Double.NaN : ratioTapChanger.getStep(tapPosition + 1).getRho();
        double targetV = ratioTapChanger.getTargetV();
        Side regulatedSide;
        if (twt.getTerminal1().equals(ratioTapChanger.getRegulationTerminal())) {
            regulatedSide = Side.ONE;
        } else if (twt.getTerminal2().equals(ratioTapChanger.getRegulationTerminal())) {
            regulatedSide = Side.TWO;
        } else {
            LOGGER.warn("{} {}: {}: Unexpected regulation terminal (side 1 or 2 of transformer is expected), skipping validation",
                        ValidationType.TWTS, ValidationUtils.VALIDATION_WARNING, twt.getId());
            try {
                twtsWriter.write(twt.getId(), Float.NaN, Float.NaN, Float.NaN, rho, rhoPreviousStep, rhoNextStep, tapPosition, lowTapPosition,
                                 highTapPosition, targetV, null, Float.NaN, false, false, true);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            return true;
        }
        Bus bus = ratioTapChanger.getRegulationTerminal().getBusView().getBus();
        double v = bus != null ? bus.getV() : Double.NaN;
        boolean connected = bus != null;
        Bus connectableBus = ratioTapChanger.getRegulationTerminal().getBusView().getConnectableBus();
        boolean connectableMainComponent = connectableBus != null && connectableBus.isInMainConnectedComponent();
        boolean mainComponent = bus != null ? bus.isInMainConnectedComponent() : connectableMainComponent;
        return checkTransformer(twt.getId(), rho, rhoPreviousStep, rhoNextStep, tapPosition, lowTapPosition, highTapPosition,
                                 targetV, regulatedSide, v, connected, mainComponent, config, twtsWriter);
    }

    public static boolean checkTransformer(String id, double rho, double rhoPreviousStep, double rhoNextStep, int tapPosition,
                                           int lowTapPosition, int highTapPosition, double targetV, Side regulatedSide, double v,
                                           boolean connected, boolean mainComponent, ValidationConfig config, Writer writer) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);

        try (ValidationWriter twtsWriter = ValidationUtils.createValidationWriter(id, config, writer, ValidationType.TWTS)) {
            return checkTransformer(id, rho, rhoPreviousStep, rhoNextStep, tapPosition, lowTapPosition, highTapPosition,
                                     targetV, regulatedSide, v, connected, mainComponent, config, twtsWriter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean checkTransformer(String id, double rho, double rhoPreviousStep, double rhoNextStep, int tapPosition,
                                           int lowTapPosition, int highTapPosition, double targetV, Side regulatedSide, double v,
                                           boolean connected, boolean mainComponent, ValidationConfig config, ValidationWriter twtsWriter) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(config);
        Objects.requireNonNull(twtsWriter);

        boolean validated = true;
        double error = v - targetV;
        double upIncrement = Double.isNaN(rhoNextStep) ? Double.NaN : evaluateVoltage(regulatedSide, v, rho, rhoNextStep) - v;
        double downIncrement = Double.isNaN(rhoPreviousStep) ? Double.NaN : evaluateVoltage(regulatedSide, v, rho, rhoPreviousStep) - v;
        if (connected && ValidationUtils.isMainComponent(config, mainComponent)) {
            validated = checkTransformerSide(id, regulatedSide, error, upIncrement, downIncrement, config);
        }
        try {
            twtsWriter.write(id, error, upIncrement, downIncrement, rho, rhoPreviousStep, rhoNextStep, tapPosition, lowTapPosition,
                             highTapPosition, targetV, regulatedSide, v, connected, mainComponent, validated);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return validated;
    }

    /**
     *  Evaluates the voltage value for a transformation ratio different from the current ratio,
     *  assuming "nothing else changes": voltage on the other side is kept constant,
     *  voltage decrease through the impedance is kept constant (perfect transformer approximation).
     */
    private static double evaluateVoltage(Side regulatedSide, double voltage, double ratio, double nextRatio) {
        switch (regulatedSide) {
            case ONE:
                return voltage * ratio / nextRatio;
            case TWO:
                return voltage * nextRatio / ratio;
            default:
                throw new AssertionError("Unexpected Side value: " + regulatedSide);
        }
    }

    /**
     * Checks that the voltage deviation from the target voltage stays inside a deadband around the target voltage,
     * taken equal to the maximum possible voltage increase/decrease for a one-tap change.
     */
    private static boolean checkTransformerSide(String id, Side side, double error, double upIncrement, double downIncrement, ValidationConfig config) {
        boolean validated = true;
        double maxIncrease = getMaxVoltageIncrease(upIncrement, downIncrement);
        double maxDecrease = getMaxVoltageDecrease(upIncrement, downIncrement);
        if (ValidationUtils.areNaN(config, error)) {
            LOGGER.warn("{} {}: {} side {}: error {}", ValidationType.TWTS, ValidationUtils.VALIDATION_ERROR, id, side, error);
            return false;
        }
        // if error is negative, i.e if voltage is lower than target, and an increase is possible,
        // check that voltage is inside the downward deadband, taken equal to the possible increase
        if (error < 0 && !Double.isNaN(maxIncrease)) {
            double downDeadband = maxIncrease;

            if (error + downDeadband < -config.getThreshold()) {
                LOGGER.warn("{} {}: {} side {}: error {} upIncrement {} downIncrement {}",
                        ValidationType.TWTS, ValidationUtils.VALIDATION_ERROR, id, side, error, upIncrement, downIncrement);
                validated = false;
            }
        }

        // if error is positive, i.e if voltage is higher than target, and a voltage decrease is possible,
        // check that voltage is inside the upward deadband, taken equal to the possible decrease
        if (error > 0 && !Double.isNaN(maxDecrease)) {
            double upDeadband = -maxDecrease;

            if (error - upDeadband > config.getThreshold()) {
                LOGGER.warn("{} {}: {} side {}: error {} upIncrement {} downIncrement {}",
                        ValidationType.TWTS, ValidationUtils.VALIDATION_ERROR, id, side, error, upIncrement, downIncrement);
                validated = false;
            }
        }
        return validated;
    }

    /**
     *  Being given both increments corresponding to taps -1 and +1, returns the maximum voltage increase,
     *  or NaN if no increase is possible.
     */
    private static double getMaxVoltageIncrease(double upIncrement, double downIncrement) {

        if (Double.isNaN(downIncrement) && Double.isNaN(upIncrement)) {
            return Double.NaN;
        }
        if (Double.isNaN(upIncrement)) {
            return downIncrement > 0 ? downIncrement : Double.NaN;
        }
        if (Double.isNaN(downIncrement)) {
            return upIncrement > 0 ? upIncrement : Double.NaN;
        }
        return Math.max(upIncrement, downIncrement);
    }

    /**
     *  Being given both increments corresponding to taps -1 and +1, returns the maximum voltage decrease (as a negative number),
     *  or NaN if no decrease is possible.
     */
    private static double getMaxVoltageDecrease(double upIncrement, double downIncrement) {
        if (Double.isNaN(downIncrement) && Double.isNaN(upIncrement)) {
            return Double.NaN;
        }
        if (Double.isNaN(upIncrement)) {
            return downIncrement < 0 ? downIncrement : Double.NaN;
        }
        if (Double.isNaN(downIncrement)) {
            return upIncrement < 0 ? upIncrement : Double.NaN;
        }
        return Math.min(upIncrement, downIncrement);
    }

}
