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
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.loadflow.validation.io.ValidationWriter;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class FlowsValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlowsValidation.class);

    private FlowsValidation() {
    }

    public static boolean checkFlows(String id, double r, double x, double rho1, double rho2, double u1, double u2, double theta1, double theta2, double alpha1,
                                     double alpha2, double g1, double g2, double b1, double b2, float p1, float q1, float p2, float q2, boolean connected1,
                                     boolean connected2, boolean mainComponent1, boolean mainComponent2, ValidationConfig config, Writer writer) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);

        try (ValidationWriter flowsWriter = ValidationUtils.createValidationWriter(id, config, writer, ValidationType.FLOWS)) {
            return checkFlows(id, r, x, rho1, rho2, u1, u2, theta1, theta2, alpha1, alpha2, g1, g2, b1, b2, p1, q1, p2, q2, connected1, connected2,
                              mainComponent1, mainComponent2, config, flowsWriter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean checkFlows(String id, double r, double x, double rho1, double rho2, double u1, double u2, double theta1, double theta2, double alpha1,
                                      double alpha2, double g1, double g2, double b1, double b2, float p1, float q1, float p2, float q2, boolean connected1,
                                      boolean connected2, boolean mainComponent1, boolean mainComponent2, ValidationConfig config, ValidationWriter flowsWriter) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(config);
        Objects.requireNonNull(flowsWriter);
        boolean validated = true;

        double fixedX = x;
        if (Math.abs(fixedX) < config.getEpsilonX() && config.applyReactanceCorrection()) {
            LOGGER.info("x {} -> {}", fixedX, config.getEpsilonX());
            fixedX = config.getEpsilonX();
        }
        double z = Math.hypot(r, fixedX);
        double y = 1 / z;
        double ksi = Math.atan2(r, fixedX);
        double computedU1 = computeU(connected1, connected2, u1, u2, rho1, rho2, g1, b1, y, ksi);
        double computedU2 = computeU(connected2, connected1, u2, u1, rho2, rho1, g2, b2, y, ksi);
        double computedTheta1 = computeTheta(connected1, connected2, theta1, theta2, alpha1, alpha2, computedU1, computedU2, rho1, rho2, g1, b1, y, ksi);
        double computedTheta2 = computeTheta(connected2, connected1, theta2, theta1, alpha2, alpha1, computedU2, computedU1, rho2, rho1, g2, b2, y, ksi);
        double p1Calc = connected1 ? rho1 * rho2 * computedU1 * computedU2 * y * Math.sin(computedTheta1 - computedTheta2 - ksi + alpha1 - alpha2) + rho1 * rho1 * computedU1 * computedU1 * (y * Math.sin(ksi) + g1) : Float.NaN;
        double q1Calc = connected1 ? -rho1 * rho2 * computedU1 * computedU2 * y * Math.cos(computedTheta1 - computedTheta2 - ksi + alpha1 - alpha2) + rho1 * rho1 * computedU1 * computedU1 * (y * Math.cos(ksi) - b1) : Float.NaN;
        double p2Calc = connected2 ? rho2 * rho1 * computedU2 * computedU1 * y * Math.sin(computedTheta2 - computedTheta1 - ksi + alpha2 - alpha1) + rho2 * rho2 * computedU2 * computedU2 * (y * Math.sin(ksi) + g2) : Float.NaN;
        double q2Calc = connected2 ? -rho2 * rho1 * computedU2 * computedU1 * y * Math.cos(computedTheta2 - computedTheta1 - ksi + alpha2 - alpha1) + rho2 * rho2 * computedU2 * computedU2 * (y * Math.cos(ksi) - b2) : Float.NaN;

        if (!connected1) {
            validated &= checkDisconnectedTerminal(id, "1", p1, p1Calc, q1, q1Calc, config);
        }
        if (!connected2) {
            validated &= checkDisconnectedTerminal(id, "2", p2, p2Calc, q2, q2Calc, config);
        }
        if (connected1 && ValidationUtils.isMainComponent(config, mainComponent1)) {
            validated &= checkConnectedTerminal(id, "1", p1, p1Calc, q1, q1Calc, config);
        }
        if (connected2 && ValidationUtils.isMainComponent(config, mainComponent2)) {
            validated &= checkConnectedTerminal(id, "2", p2, p2Calc, q2, q2Calc, config);
        }
        try {
            flowsWriter.write(id, p1, p1Calc, q1, q1Calc, p2, p2Calc, q2, q2Calc, r, x, g1, g2, b1, b2, rho1, rho2, alpha1, alpha2, u1, u2, theta1, theta2, z, y, ksi,
                              connected1, connected2, mainComponent1, mainComponent2, validated);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return validated;
    }

    private static double computeU(boolean connected, boolean otherConnected, double u, double otherU, double rho, double otherRho, double g, double b, double y, double ksi) {
        if (connected) {
            return u;
        }
        if (!otherConnected) {
            return u;
        }
        double z1 = y * Math.sin(ksi) + g;
        double z2 = y * Math.cos(ksi) - b;
        return 1 / Math.sqrt(z1 * z1 + z2 * z2) * (otherRho / rho) * y * otherU;
    }

    private static double computeTheta(boolean connected, boolean otherConnected, double theta, double otherTheta, double alpha, double otherAlpha, double u, double otherU,
                                       double rho, double otherRho, double g, double b, double y, double ksi) {
        if (connected) {
            return theta;
        }
        if (!otherConnected) {
            return theta;
        }
        double z1 = y * Math.sin(ksi) + g;
        double z2 = y * Math.cos(ksi) - b;
        double phi = -(-otherTheta - ksi + alpha - otherAlpha);
        double cosTheatMinusPhi = rho / otherRho * u / otherU * (Math.cos(ksi) - b / y);
        return Math.atan(-z1 / z2) + phi + (cosTheatMinusPhi < 0 ? Math.PI : 0);
    }

    private static boolean checkDisconnectedTerminal(String id, String terminalNumber, float p, double pCalc, float q, double qCalc, ValidationConfig config) {
        boolean validated = true;
        if (!Float.isNaN(p) && Math.abs(p) > config.getThreshold()) {
            LOGGER.warn("{} {}: {} disconnected P{} {} {}", ValidationType.FLOWS, ValidationUtils.VALIDATION_ERROR, id, terminalNumber, p, pCalc);
            validated = false;
        }
        if (!Float.isNaN(q) && Math.abs(q) > config.getThreshold()) {
            LOGGER.warn("{} {}: {} disconnected Q{} {} {}", ValidationType.FLOWS, ValidationUtils.VALIDATION_ERROR, id, terminalNumber, q, qCalc);
            validated = false;
        }
        return validated;
    }

    private static boolean checkConnectedTerminal(String id, String terminalNumber, float p, double pCalc, float q, double qCalc, ValidationConfig config) {
        boolean validated = true;
        if (ValidationUtils.areNaN(config, pCalc) || Math.abs(p - pCalc) > config.getThreshold()) {
            LOGGER.warn("{} {}: {} P{} {} {}", ValidationType.FLOWS, ValidationUtils.VALIDATION_ERROR, id, terminalNumber, p, pCalc);
            validated = false;
        }
        if (ValidationUtils.areNaN(config, qCalc) || Math.abs(q - qCalc) > config.getThreshold()) {
            LOGGER.warn("{} {}: {} Q{} {} {}", ValidationType.FLOWS, ValidationUtils.VALIDATION_ERROR, id, terminalNumber, q, qCalc);
            validated = false;
        }
        return validated;
    }

    public static boolean checkFlows(Line l, ValidationConfig config, Writer writer) {
        Objects.requireNonNull(l);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);

        try (ValidationWriter flowsWriter = ValidationUtils.createValidationWriter(l.getId(), config, writer, ValidationType.FLOWS)) {
            return checkFlows(l, config, flowsWriter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean checkFlows(Line l, ValidationConfig config, ValidationWriter flowsWriter) {
        Objects.requireNonNull(l);
        Objects.requireNonNull(config);
        Objects.requireNonNull(flowsWriter);

        float p1 = l.getTerminal1().getP();
        float q1 = l.getTerminal1().getQ();
        float p2 = l.getTerminal2().getP();
        float q2 = l.getTerminal2().getQ();
        Bus bus1 = l.getTerminal1().getBusView().getBus();
        Bus bus2 = l.getTerminal2().getBusView().getBus();
        double r = l.getR();
        double x = l.getX();
        double rho1 = 1f;
        double rho2 = 1f;
        double u1 = bus1 != null ? bus1.getV() : Double.NaN;
        double u2 = bus2 != null ? bus2.getV() : Double.NaN;
        double theta1 = bus1 != null ? Math.toRadians(bus1.getAngle()) : Double.NaN;
        double theta2 = bus2 != null ? Math.toRadians(bus2.getAngle()) : Double.NaN;
        double alpha1 = 0f;
        double alpha2 = 0f;
        double g1 = l.getG1();
        double g2 = l.getG2();
        double b1 = l.getB1();
        double b2 = l.getB2();
        boolean connected1 = bus1 != null ? true : false;
        boolean connected2 = bus2 != null ? true : false;
        Bus connectableBus1 = l.getTerminal1().getBusView().getConnectableBus();
        Bus connectableBus2 = l.getTerminal2().getBusView().getConnectableBus();
        boolean connectableMainComponent1 = connectableBus1 != null ? connectableBus1.isInMainConnectedComponent() : false;
        boolean connectableMainComponent2 = connectableBus2 != null ? connectableBus2.isInMainConnectedComponent() : false;
        boolean mainComponent1 = bus1 != null ? bus1.isInMainConnectedComponent() : connectableMainComponent1;
        boolean mainComponent2 = bus2 != null ? bus2.isInMainConnectedComponent() : connectableMainComponent2;
        return checkFlows(l.getId(), r, x, rho1, rho2, u1, u2, theta1, theta2, alpha1, alpha2, g1, g2, b1, b2, p1, q1, p2, q2, connected1, connected2,
                          mainComponent1, mainComponent2, config, flowsWriter);
    }

    public static boolean checkFlows(TwoWindingsTransformer twt, ValidationConfig config, Writer writer) {
        Objects.requireNonNull(twt);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);

        try (ValidationWriter flowsWriter = ValidationUtils.createValidationWriter(twt.getId(), config, writer, ValidationType.FLOWS)) {
            return checkFlows(twt, config, flowsWriter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean checkFlows(TwoWindingsTransformer twt, ValidationConfig config, ValidationWriter flowsWriter) {
        Objects.requireNonNull(twt);
        Objects.requireNonNull(config);
        Objects.requireNonNull(flowsWriter);

        float p1 = twt.getTerminal1().getP();
        float q1 = twt.getTerminal1().getQ();
        float p2 = twt.getTerminal2().getP();
        float q2 = twt.getTerminal2().getQ();
        Bus bus1 = twt.getTerminal1().getBusView().getBus();
        Bus bus2 = twt.getTerminal2().getBusView().getBus();
        float r = (float) getR(twt);
        float x = (float) getX(twt);
        double g1 = getG1(twt, config);
        double g2 = config.getLoadFlowParameters().isSpecificCompatibility() ? twt.getG() / 2 : 0f;
        double b1 = getB1(twt, config);
        double b2 = config.getLoadFlowParameters().isSpecificCompatibility() ? twt.getB() / 2 : 0f;
        double rho1 = getRho1(twt);
        double rho2 = 1f;
        double u1 = bus1 != null ? bus1.getV() : Double.NaN;
        double u2 = bus2 != null ? bus2.getV() : Double.NaN;
        double theta1 = bus1 != null ? Math.toRadians(bus1.getAngle()) : Double.NaN;
        double theta2 = bus2 != null ? Math.toRadians(bus2.getAngle()) : Double.NaN;
        double alpha1 = twt.getPhaseTapChanger() != null ? Math.toRadians(twt.getPhaseTapChanger().getCurrentStep().getAlpha()) : 0f;
        double alpha2 = 0f;
        boolean connected1 = bus1 != null ? true : false;
        boolean connected2 = bus2 != null ? true : false;
        Bus connectableBus1 = twt.getTerminal1().getBusView().getConnectableBus();
        Bus connectableBus2 = twt.getTerminal2().getBusView().getConnectableBus();
        boolean connectableMainComponent1 = connectableBus1 != null ? connectableBus1.isInMainConnectedComponent() : false;
        boolean connectableMainComponent2 = connectableBus2 != null ? connectableBus2.isInMainConnectedComponent() : false;
        boolean mainComponent1 = bus1 != null ? bus1.isInMainConnectedComponent() : connectableMainComponent1;
        boolean mainComponent2 = bus2 != null ? bus2.isInMainConnectedComponent() : connectableMainComponent2;
        return checkFlows(twt.getId(), r, x, rho1, rho2, u1, u2, theta1, theta2, alpha1, alpha2, g1, g2, b1, b2, p1, q1, p2, q2, connected1, connected2,
                          mainComponent1, mainComponent2, config, flowsWriter);
    }

    private static double getValue(float initialValue, float rtcStepValue, float ptcStepValue) {
        return initialValue * (1 + rtcStepValue / 100) * (1 + ptcStepValue / 100);
    }

    private static double getR(TwoWindingsTransformer twt) {
        return getValue(twt.getR(),
                twt.getRatioTapChanger() != null ? twt.getRatioTapChanger().getCurrentStep().getR() : 0,
                twt.getPhaseTapChanger() != null ? twt.getPhaseTapChanger().getCurrentStep().getR() : 0);
    }

    private static double getX(TwoWindingsTransformer twt) {
        return getValue(twt.getX(),
                twt.getRatioTapChanger() != null ? twt.getRatioTapChanger().getCurrentStep().getX() : 0,
                twt.getPhaseTapChanger() != null ? twt.getPhaseTapChanger().getCurrentStep().getX() : 0);
    }

    private static double getG1(TwoWindingsTransformer twt, ValidationConfig config) {
        return getValue(config.getLoadFlowParameters().isSpecificCompatibility() ? twt.getG() / 2 : twt.getG(),
                twt.getRatioTapChanger() != null ? twt.getRatioTapChanger().getCurrentStep().getG() : 0,
                twt.getPhaseTapChanger() != null ? twt.getPhaseTapChanger().getCurrentStep().getG() : 0);
    }

    private static double getB1(TwoWindingsTransformer twt, ValidationConfig config) {
        return getValue(config.getLoadFlowParameters().isSpecificCompatibility() ? twt.getB() / 2 : twt.getB(),
                twt.getRatioTapChanger() != null ? twt.getRatioTapChanger().getCurrentStep().getB() : 0,
                twt.getPhaseTapChanger() != null ? twt.getPhaseTapChanger().getCurrentStep().getB() : 0);
    }

    private static double getRho1(TwoWindingsTransformer twt) {
        double rho1 = twt.getRatedU2() / twt.getRatedU1();
        if (twt.getRatioTapChanger() != null) {
            rho1 *= twt.getRatioTapChanger().getCurrentStep().getRho();
        }
        if (twt.getPhaseTapChanger() != null) {
            rho1 *= twt.getPhaseTapChanger().getCurrentStep().getRho();
        }
        return rho1;
    }

    public static boolean checkFlows(Network network, ValidationConfig config, Writer writer) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);

        try (ValidationWriter flowsWriter = ValidationUtils.createValidationWriter(network.getId(), config, writer, ValidationType.FLOWS)) {
            return checkFlows(network, config, flowsWriter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean checkFlows(Network network, ValidationConfig config, Path file) throws IOException {
        Objects.requireNonNull(network);
        Objects.requireNonNull(config);
        Objects.requireNonNull(file);
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            return checkFlows(network, config, writer);
        }
    }

    public static boolean checkFlows(Network network, ValidationConfig config, ValidationWriter flowsWriter) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(config);
        Objects.requireNonNull(flowsWriter);
        LOGGER.info("Checking flows of network {}", network.getId());

        boolean linesValidated = network.getLineStream()
                .sorted(Comparator.comparing(Line::getId))
                .map(l -> checkFlows(l, config, flowsWriter))
                .reduce(Boolean::logicalAnd).orElse(true);

        boolean transformersValidated = network.getTwoWindingsTransformerStream()
                .sorted(Comparator.comparing(TwoWindingsTransformer::getId))
                .map(t -> checkFlows(t, config, flowsWriter))
                .reduce(Boolean::logicalAnd).orElse(true);

        return linesValidated && transformersValidated;
    }

}
