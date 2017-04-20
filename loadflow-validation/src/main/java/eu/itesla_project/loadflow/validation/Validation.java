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

import eu.itesla_project.commons.config.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.itesla_project.commons.io.table.Column;
import eu.itesla_project.commons.io.table.TableFormatter;
import eu.itesla_project.commons.io.table.TableFormatterConfig;
import eu.itesla_project.commons.io.table.TableFormatterFactory;
import eu.itesla_project.iidm.network.Bus;
import eu.itesla_project.iidm.network.Line;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.TwoWindingsTransformer;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class Validation {

    private static final Logger LOGGER = LoggerFactory.getLogger(Validation.class);

    private Validation() {
    }

    private static TableFormatterConfig TABLE_FORMATTER_CONFIG = TableFormatterConfig.load();
    private static Column[] COLUMNS = {
        new Column("id"),
        new Column("characteristic"),
        new Column("value")
    };

    private static TableFormatter createTableFormater(String id, CheckFlowsConfig config, Writer writer) {
        try {
            TableFormatterFactory factory = config.getTableFormatterFactory().newInstance();
            return factory.create(writer, id + " flow check", TABLE_FORMATTER_CONFIG, COLUMNS);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ConfigurationException(e);
        }
    }

    public static boolean checkFlows(String id, double r, double x, double rho1, double rho2, double u1, double u2, double theta1, double theta2, double alpha1,
                                     double alpha2, double g1, double g2, double b1, double b2, float p1, float q1, float p2, float q2,
                                     CheckFlowsConfig config, Writer writer) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);

        try (TableFormatter formatter = createTableFormater(id, config, writer)) {
            return checkFlows(id, r, x, rho1, rho2, u1, u2, theta1, theta2, alpha1, alpha2, g1, g2, b1, b2, p1, q1, p2, q2, config, formatter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean checkFlows(String id, double r, double x, double rho1, double rho2, double u1, double u2, double theta1, double theta2, double alpha1,
                                      double alpha2, double g1, double g2, double b1, double b2, float p1, float q1, float p2, float q2, CheckFlowsConfig config, 
                                      TableFormatter formatter) {
        boolean ok = true;
        try {
            if (Math.abs(x) < config.getEpsilonX() && config.applyReactanceCorrection()) {
                LOGGER.info("x " + x + " -> " + config.getEpsilonX());
                x = config.getEpsilonX();
            }
            double z = Math.hypot(r, x);
            double y = 1 / z;
            double ksi = Math.atan2(r, x);
            double p1_calc = rho1 * rho2 * u1 * u2 * y * Math.sin(theta1 - theta2 - ksi + alpha1 - alpha2) + rho1 * rho1 * u1 * u1 * (y * Math.sin(ksi) + g1);
            double q1_calc = -rho1 * rho2 * u1 * u2 * y * Math.cos(theta1 - theta2 - ksi + alpha1 - alpha2) + rho1 * rho1 * u1 * u1 * (y * Math.cos(ksi) - b1);
            double p2_calc = rho2 * rho1 * u2 * u1 * y * Math.sin(theta2 - theta1 - ksi + alpha2 - alpha1) + rho2 * rho2 * u2 * u2 * (y * Math.sin(ksi) + g2);
            double q2_calc = -rho2 * rho1 * u2 * u1 * y * Math.cos(theta2 - theta1 - ksi + alpha2 - alpha1) + rho2 * rho2 * u2 * u2 * (y * Math.cos(ksi) - b2);

            formatter.writeCell(id).writeCell("network_p1").writeCell(p1)
                    .writeCell(id).writeCell("expected_p1").writeCell(p1_calc)
                    .writeCell(id).writeCell("network_q1").writeCell(q1)
                    .writeCell(id).writeCell("expected_q1").writeCell(q1_calc)
                    .writeCell(id).writeCell("network_p2").writeCell(p2)
                    .writeCell(id).writeCell("expected_p2").writeCell(p2_calc)
                    .writeCell(id).writeCell("network_q2").writeCell(q2)
                    .writeCell(id).writeCell("expected_q2").writeCell(q2_calc);

            if (config.isVerbose()) {
                formatter.writeCell(id).writeCell("r").writeCell(r)
                        .writeCell(id).writeCell("x").writeCell(x)
                        .writeCell(id).writeCell("g1").writeCell(g1)
                        .writeCell(id).writeCell("g2").writeCell(g2)
                        .writeCell(id).writeCell("b1").writeCell(b1)
                        .writeCell(id).writeCell("b2").writeCell(b2)
                        .writeCell(id).writeCell("rho1").writeCell(rho1)
                        .writeCell(id).writeCell("rho2").writeCell(rho2)
                        .writeCell(id).writeCell("alpha1").writeCell(alpha1)
                        .writeCell(id).writeCell("alpha2").writeCell(alpha2)
                        .writeCell(id).writeCell("u1").writeCell(u1)
                        .writeCell(id).writeCell("u2").writeCell(u2)
                        .writeCell(id).writeCell("theta1").writeCell(theta1)
                        .writeCell(id).writeCell("theta2").writeCell(theta2)
                        .writeCell(id).writeCell("z").writeCell(z)
                        .writeCell(id).writeCell("y").writeCell(y)
                        .writeCell(id).writeCell("ksi").writeCell(ksi);
            }
            if (Math.abs(p1 - p1_calc) > config.getThreshold()) {
                LOGGER.warn(id + " P1 " + p1 + " " + p1_calc);
                ok = false;
            }
            if (Math.abs(q1 - q1_calc) > config.getThreshold()) {
                LOGGER.warn(id + " Q1 " + q1 + " " + q1_calc);
                ok = false;
            }
            if (Math.abs(p2 - p2_calc) > config.getThreshold()) {
                LOGGER.warn(id + " P2 " + p2 + " " + p2_calc);
                ok = false;
            }
            if (Math.abs(q2 - q2_calc) > config.getThreshold()) {
                LOGGER.warn(id + " Q2 " + q2 + " " + q2_calc);
                ok = false;
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return ok;
    }
    
    public static boolean checkFlows(Line l, CheckFlowsConfig config, Writer writer) {
        Objects.requireNonNull(l);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);

        try (TableFormatter formatter = createTableFormater(l.getId(), config, writer)) {
            return checkFlows(l, config, formatter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean checkFlows(Line l, CheckFlowsConfig config, TableFormatter formatter) {
        Objects.requireNonNull(l);
        Objects.requireNonNull(config);
        Objects.requireNonNull(formatter);

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
            return checkFlows(l.getId(), r, x, rho1, rho2, u1, u2, theta1, theta2, alpha1, alpha2, g1, g2, b1, b2, p1, q1, p2, q2, config, formatter);
        }
        return true;
    }

    public static boolean checkFlows(TwoWindingsTransformer twt, CheckFlowsConfig config, Writer writer) {
        Objects.requireNonNull(twt);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);

        try (TableFormatter formatter = createTableFormater(twt.getId(), config, writer)) {
            return checkFlows(twt, config, formatter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean checkFlows(TwoWindingsTransformer twt, CheckFlowsConfig config, TableFormatter formatter) {
        Objects.requireNonNull(twt);
        Objects.requireNonNull(config);
        Objects.requireNonNull(formatter);

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
            double b1 = twt.getB() / 2;
            double b2 = twt.getB() / 2;
            if (twt.getRatioTapChanger() != null) {
                r *= (1 + twt.getRatioTapChanger().getCurrentStep().getR() / 100);
                x *= (1 + twt.getRatioTapChanger().getCurrentStep().getX() / 100);
                g1 *= (1 + twt.getRatioTapChanger().getCurrentStep().getG() / 100);
                b1 *= (1 + twt.getRatioTapChanger().getCurrentStep().getB() / 100);
            }
            if (twt.getPhaseTapChanger() != null) {
                r *= (1 + twt.getPhaseTapChanger().getCurrentStep().getR() / 100);
                x *= (1 + twt.getPhaseTapChanger().getCurrentStep().getX() / 100);
                g1 *= (1 + twt.getPhaseTapChanger().getCurrentStep().getG() / 100);
                b1 *= (1 + twt.getPhaseTapChanger().getCurrentStep().getB() / 100);
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
            return checkFlows(twt.getId(), r, x, rho1, rho2, u1, u2, theta1, theta2, alpha1, alpha2, g1, g2, b1, b2, p1, q1, p2, q2, config, formatter);
        }
        return true;
    }
    
    public static boolean checkFlows(Network network, CheckFlowsConfig config, Writer writer) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);

        try (TableFormatter formatter = createTableFormater(network.getId(), config, writer)) {
            return checkFlows(network, config, formatter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    public static boolean checkFlows(Network network, CheckFlowsConfig config, Path file) throws IOException, InstantiationException, IllegalAccessException {
        Objects.requireNonNull(file);
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            return checkFlows(network, config, writer);
        }
    }

    public static boolean checkFlows(Network network, CheckFlowsConfig config, TableFormatter formatter) {
        LOGGER.info("Checking flows of network {}", network.getId());

        boolean linesOk = network.getLineStream()
                .sorted(Comparator.comparing(Line::getId))
                .map(l -> checkFlows(l, config, formatter))
                .reduce(Boolean::logicalAnd).orElse(true);

        boolean transformersOk = network.getTwoWindingsTransformerStream()
                .sorted(Comparator.comparing(TwoWindingsTransformer::getId))
                .map(t -> checkFlows(t, config, formatter))
                .reduce(Boolean::logicalAnd).orElse(true);

        return linesOk && transformersOk;
    }
}
