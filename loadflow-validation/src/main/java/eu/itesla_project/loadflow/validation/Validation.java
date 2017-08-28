/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.loadflow.validation;

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

import eu.itesla_project.commons.config.ConfigurationException;
import eu.itesla_project.iidm.network.Bus;
import eu.itesla_project.iidm.network.Generator;
import eu.itesla_project.iidm.network.Line;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.TwoWindingsTransformer;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class Validation {

    private static final Logger LOGGER = LoggerFactory.getLogger(Validation.class);

    private Validation() {
    }

    private static ValidationWriter createValidationWriter(String id, ValidationConfig config, Writer writer, ValidationType validationType) {
        try {
            ValidationWriterFactory factory = config.getValidationOutputWriter().getValidationWriterFactory().newInstance();
            return factory.create(id, config.getTableFormatterFactory(), writer, config.isVerbose(), validationType);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ConfigurationException(e);
        }
    }

    public static boolean checkFlows(String id, double r, double x, double rho1, double rho2, double u1, double u2, double theta1, double theta2, double alpha1,
                                     double alpha2, double g1, double g2, double b1, double b2, float p1, float q1, float p2, float q2,
                                     ValidationConfig config, Writer writer) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);

        try (ValidationWriter flowsWriter = createValidationWriter(id, config, writer, ValidationType.FLOWS)) {
            return checkFlows(id, r, x, rho1, rho2, u1, u2, theta1, theta2, alpha1, alpha2, g1, g2, b1, b2, p1, q1, p2, q2, config, flowsWriter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean checkFlows(String id, double r, double x, double rho1, double rho2, double u1, double u2, double theta1, double theta2, double alpha1,
                                      double alpha2, double g1, double g2, double b1, double b2, float p1, float q1, float p2, float q2, ValidationConfig config, 
                                      ValidationWriter flowsWriter) {
        boolean ok = true;
        try {
            double fixedX = x;
            if (Math.abs(fixedX) < config.getEpsilonX() && config.applyReactanceCorrection()) {
                LOGGER.info("x " + fixedX + " -> " + config.getEpsilonX());
                fixedX = config.getEpsilonX();
            }
            double z = Math.hypot(r, fixedX);
            double y = 1 / z;
            double ksi = Math.atan2(r, fixedX);
            double p1Calc = rho1 * rho2 * u1 * u2 * y * Math.sin(theta1 - theta2 - ksi + alpha1 - alpha2) + rho1 * rho1 * u1 * u1 * (y * Math.sin(ksi) + g1);
            double q1Calc = -rho1 * rho2 * u1 * u2 * y * Math.cos(theta1 - theta2 - ksi + alpha1 - alpha2) + rho1 * rho1 * u1 * u1 * (y * Math.cos(ksi) - b1);
            double p2Calc = rho2 * rho1 * u2 * u1 * y * Math.sin(theta2 - theta1 - ksi + alpha2 - alpha1) + rho2 * rho2 * u2 * u2 * (y * Math.sin(ksi) + g2);
            double q2Calc = -rho2 * rho1 * u2 * u1 * y * Math.cos(theta2 - theta1 - ksi + alpha2 - alpha1) + rho2 * rho2 * u2 * u2 * (y * Math.cos(ksi) - b2);

            flowsWriter.write(id, p1, p1Calc, q1, q1Calc, p2, p2Calc, q2, q2Calc, r, x, g1, g2, b1, b2, rho1, rho2, alpha1, alpha2, u1, u2, theta1, theta2, z, y, ksi);

            if (Math.abs(p1 - p1Calc) > config.getThreshold()) {
                LOGGER.warn(id + " P1 " + p1 + " " + p1Calc);
                ok = false;
            }
            if (Math.abs(q1 - q1Calc) > config.getThreshold()) {
                LOGGER.warn(id + " Q1 " + q1 + " " + q1Calc);
                ok = false;
            }
            if (Math.abs(p2 - p2Calc) > config.getThreshold()) {
                LOGGER.warn(id + " P2 " + p2 + " " + p2Calc);
                ok = false;
            }
            if (Math.abs(q2 - q2Calc) > config.getThreshold()) {
                LOGGER.warn(id + " Q2 " + q2 + " " + q2Calc);
                ok = false;
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return ok;
    }

    public static boolean checkFlows(Line l, ValidationConfig config, Writer writer) {
        Objects.requireNonNull(l);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);

        try (ValidationWriter flowsWriter = createValidationWriter(l.getId(), config, writer, ValidationType.FLOWS)) {
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
        if (bus1 != null && bus2 != null && !Float.isNaN(p1) && !Float.isNaN(p2) && !Float.isNaN(q1) && !Float.isNaN(q2)) {
            double r = l.getR();
            double x = l.getX();
            double rho1 = 1f;
            double rho2 = 1f;
            double u1 = bus1.getV();
            double u2 = bus2.getV();
            double theta1 = Math.toRadians(bus1.getAngle());
            double theta2 = Math.toRadians(bus2.getAngle());
            double alpha1 = 0f;
            double alpha2 = 0f;
            double g1 = l.getG1();
            double g2 = l.getG2();
            double b1 = l.getB1();
            double b2 = l.getB2();
            return checkFlows(l.getId(), r, x, rho1, rho2, u1, u2, theta1, theta2, alpha1, alpha2, g1, g2, b1, b2, p1, q1, p2, q2, config, flowsWriter);
        }
        return true;
    }

    public static boolean checkFlows(TwoWindingsTransformer twt, ValidationConfig config, Writer writer) {
        Objects.requireNonNull(twt);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);

        try (ValidationWriter flowsWriter = createValidationWriter(twt.getId(), config, writer, ValidationType.FLOWS)) {
            return checkFlows(twt, config, flowsWriter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean checkFlows(TwoWindingsTransformer twt, ValidationConfig config, ValidationWriter flowsWriter) {
        Objects.requireNonNull(twt);
        Objects.requireNonNull(config);
        Objects.requireNonNull(flowsWriter);

        if (twt.getRatioTapChanger() != null && twt.getPhaseTapChanger() != null) {
            throw new AssertionError();
        }
        float p1 = twt.getTerminal1().getP();
        float q1 = twt.getTerminal1().getQ();
        float p2 = twt.getTerminal2().getP();
        float q2 = twt.getTerminal2().getQ();
        Bus bus1 = twt.getTerminal1().getBusView().getBus();
        Bus bus2 = twt.getTerminal2().getBusView().getBus();
        if (bus1 != null && bus2 != null && !Float.isNaN(p1) && !Float.isNaN(p2) && !Float.isNaN(q1) && !Float.isNaN(q2)) {
            float r = twt.getR();
            float x = twt.getX();
            double g1 = twt.getG();
            double g2 = 0f;
            double b1 = twt.getB();
            double b2 = 0f;
            if (config.getLoadFlowParameters().isSpecificCompatibility()) {
                g1 = twt.getG() / 2;
                g2 = twt.getG() / 2;
                b1 = twt.getB() / 2;
                b2 = twt.getB() / 2;
            }
            if (twt.getRatioTapChanger() != null) {
                r *= 1 + twt.getRatioTapChanger().getCurrentStep().getR() / 100;
                x *= 1 + twt.getRatioTapChanger().getCurrentStep().getX() / 100;
                g1 *= 1 + twt.getRatioTapChanger().getCurrentStep().getG() / 100;
                b1 *= 1 + twt.getRatioTapChanger().getCurrentStep().getB() / 100;
            }
            if (twt.getPhaseTapChanger() != null) {
                r *= 1 + twt.getPhaseTapChanger().getCurrentStep().getR() / 100;
                x *= 1 + twt.getPhaseTapChanger().getCurrentStep().getX() / 100;
                g1 *= 1 + twt.getPhaseTapChanger().getCurrentStep().getG() / 100;
                b1 *= 1 + twt.getPhaseTapChanger().getCurrentStep().getB() / 100;
            }

            double rho1 = twt.getRatedU2() / twt.getRatedU1();
            if (twt.getRatioTapChanger() != null) {
                rho1 *= twt.getRatioTapChanger().getCurrentStep().getRho();
            }
            if (twt.getPhaseTapChanger() != null) {
                rho1 *= twt.getPhaseTapChanger().getCurrentStep().getRho();
            }
            double rho2 = 1f;
            double u1 = bus1.getV();
            double u2 = bus2.getV();
            double theta1 = Math.toRadians(bus1.getAngle());
            double theta2 = Math.toRadians(bus2.getAngle());
            double alpha1 = twt.getPhaseTapChanger() != null ? Math.toRadians(twt.getPhaseTapChanger().getCurrentStep().getAlpha()) : 0f;
            double alpha2 = 0f;
            return checkFlows(twt.getId(), r, x, rho1, rho2, u1, u2, theta1, theta2, alpha1, alpha2, g1, g2, b1, b2, p1, q1, p2, q2, config, flowsWriter);
        }
        return true;
    }

    public static boolean checkFlows(Network network, ValidationConfig config, Writer writer) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);

        try (ValidationWriter flowsWriter = createValidationWriter(network.getId(), config, writer, ValidationType.FLOWS)) {
            return checkFlows(network, config, flowsWriter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean checkFlows(Network network, ValidationConfig config, Path file) throws IOException {
        Objects.requireNonNull(file);
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            return checkFlows(network, config, writer);
        }
    }

    public static boolean checkFlows(Network network, ValidationConfig config, ValidationWriter flowsWriter) {
        LOGGER.info("Checking flows of network {}", network.getId());

        boolean linesOk = network.getLineStream()
                .sorted(Comparator.comparing(Line::getId))
                .map(l -> checkFlows(l, config, flowsWriter))
                .reduce(Boolean::logicalAnd).orElse(true);

        boolean transformersOk = network.getTwoWindingsTransformerStream()
                .sorted(Comparator.comparing(TwoWindingsTransformer::getId))
                .map(t -> checkFlows(t, config, flowsWriter))
                .reduce(Boolean::logicalAnd).orElse(true);

        return linesOk && transformersOk;
    }
    
    public static boolean checkGenerators(Network network, ValidationConfig config, Path file) throws IOException {
        Objects.requireNonNull(file);
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            return checkGenerators(network, config, writer);
        }
    }

    public static boolean checkGenerators(Network network, ValidationConfig config, Writer writer) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);
        try (ValidationWriter generatorsWriter = createValidationWriter(network.getId(), config, writer, ValidationType.GENERATORS)) {
            return checkGenerators(network, config, generatorsWriter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean checkGenerators(Network network, ValidationConfig config, ValidationWriter generatorsWriter) {
        LOGGER.info("Checking generators of network {}", network.getId());
        boolean generatorsOk = network.getGeneratorStream()
                .sorted(Comparator.comparing(Generator::getId))
                .map(gen -> checkGenerators(gen, config, generatorsWriter))
                .reduce(Boolean::logicalAnd).orElse(true);
        return generatorsOk;
    }
    
    public static boolean checkGenerators(Generator gen, ValidationConfig config, Writer writer) {
        Objects.requireNonNull(gen);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);

        try (ValidationWriter generatorsWriter = createValidationWriter(gen.getId(), config, writer, ValidationType.GENERATORS)) {
            return checkGenerators(gen, config, generatorsWriter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean checkGenerators(Generator gen, ValidationConfig config, ValidationWriter generatorsWriter) {
        Objects.requireNonNull(gen);
        Objects.requireNonNull(config);
        Objects.requireNonNull(generatorsWriter);
        float p = gen.getTerminal().getP();
        float q = gen.getTerminal().getQ();
        Bus bus = gen.getTerminal().getBusView().getBus();
        float targetP = gen.getTargetP();
        float targetQ = gen.getTargetQ();
        float targetV = gen.getTargetV();
        boolean voltageRegulatorOn = gen.isVoltageRegulatorOn();
        float minQ = gen.getReactiveLimits().getMinQ(p);
        float maxQ = gen.getReactiveLimits().getMaxQ(p);
        if (bus != null && !Float.isNaN(bus.getV())) {
            float v = bus.getV();
            return checkGenerators(gen.getId(), p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minQ, maxQ, config, generatorsWriter);
        }
        try {
            generatorsWriter.write(gen.getId(), p, q, Float.NaN, targetP, targetQ, targetV, gen.getTerminal().isConnected(), voltageRegulatorOn, minQ, maxQ);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return true;
    }
    
    public static boolean checkGenerators(String id, float p, float q, float v, float targetP, float targetQ, float targetV,
                                          boolean voltageRegulatorOn, float minQ, float maxQ, ValidationConfig config, Writer writer) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);

        try (ValidationWriter generatorsWriter = createValidationWriter(id, config, writer, ValidationType.GENERATORS)) {
            return checkGenerators(id, p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minQ, maxQ, config, generatorsWriter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean checkGenerators(String id, float p, float q, float v, float targetP, float targetQ, float targetV,
                                          boolean voltageRegulatorOn, float minQ, float maxQ, ValidationConfig config, ValidationWriter generatorsWriter) {
        boolean ok = true;
        try {
            generatorsWriter.write(id, p, q, v, targetP, targetQ, targetV, true, voltageRegulatorOn, minQ, maxQ);
            // a validation error should be detected if there is both a voltage and a target but no p or q
            if (Float.isNaN(p) || Float.isNaN(q)) {
                if ((!Float.isNaN(targetP) && targetP != 0) 
                    || (!Float.isNaN(targetQ) && targetQ != 0)) {
                    LOGGER.warn(id + ": P=" + p + " targetP=" + targetP + " - Q=" + q + " targetP=" + targetQ);
                    return false;
                } else {
                    return true;
                }
            }
            // active power should be equal to set point
            if (Math.abs(p + targetP) > config.getThreshold()) {
                LOGGER.warn(id + ": P=" + p + " targetP=" + targetP);
                ok = false;
            }
            // if voltageRegulatorOn="false" then reactive power should be equal to set point
            if (!voltageRegulatorOn && Math.abs(q + targetQ) > config.getThreshold()) {
                LOGGER.warn(id + ": voltage regulator off - Q=" + q + " targetQ=" + targetQ);
                ok = false;
            }
            // if voltageRegulatorOn="true" then 
            // either q is equal to g.getReactiveLimits().getMinQ(p) and V is lower than g.getTargetV()
            // or q is equal to g.getReactiveLimits().getMaxQ(p) and V is higher than g.getTargetV()
            // or V at the connected bus is equal to g.getTargetV()
            if (voltageRegulatorOn 
                && (Math.abs(q + minQ) > config.getThreshold() || (v - targetV) >= config.getThreshold()) 
                && (Math.abs(q + maxQ) > config.getThreshold() || (targetV - v) >= config.getThreshold()) 
                && Math.abs(v - targetV) > config.getThreshold()) {
                LOGGER.warn(id + ": voltage regulator on - Q=" + q + " minQ=" + minQ + " maxQ=" + maxQ + " - V=" + v + " targetV=" + targetV);
                ok = false;
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return ok;
    }

}
