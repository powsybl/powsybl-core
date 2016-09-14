/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.util;

import com.google.common.collect.ImmutableMap;
import eu.itesla_project.commons.ITeslaException;
import eu.itesla_project.iidm.network.*;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.*;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class Networks {

    private static final Logger LOGGER = LoggerFactory.getLogger(Networks.class);

    private Networks() {
    }

    public static boolean isBusValid(int feederCount, int branchCount) {
        return branchCount >=1;
    }

    public static Map<String, String> getExecutionTags(Network network) {
        return ImmutableMap.of("state", network.getStateManager().getWorkingStateId());
    }

    public static void dumpStateId(Path workingDir, String stateId) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(workingDir.resolve("state.txt"), StandardCharsets.UTF_8)) {
            writer.write(stateId);
            writer.newLine();
        }
    }

    public static void dumpStateId(Path workingDir, Network network) throws IOException {
        dumpStateId(workingDir, network.getStateManager().getWorkingStateId());
    }

    public static void runScript(Network network, Reader reader, Writer out) {
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine jsEngine = mgr.getEngineByName("js");
        try {
            ScriptContext context = new SimpleScriptContext();
            context.setAttribute("network", network, ScriptContext.ENGINE_SCOPE);
            if (out != null) {
                context.setWriter(out);
            }
            jsEngine.eval(reader, context);
        } catch (ScriptException e) {
            throw new ITeslaException(e);
        }
    }

    public static void printBalanceSummary(String title, Network network, Logger logger) {

        class ConnectedPower {
            int busCount = 0;

            List<String> connectedLoads = new ArrayList<>();
            List<String> disconnectedLoads = new ArrayList<>();
            float connectedLoadVolume = 0f;
            float disconnectedLoadVolume = 0f;

            float connectedMaxGeneration = 0f;
            float disconnectedMaxGeneration = 0f;
            float connectedGeneration = 0f;
            float disconnectedGeneration = 0f;
            List<String> connectedGenerators = new ArrayList<>();
            List<String> disconnectedGenerators = new ArrayList<>();

            List<String> connectedShunts = new ArrayList<>();
            List<String> disconnectedShunts = new ArrayList<>();
            float connectedShuntPositiveVolume = 0f;
            float disconnectedShuntPositiveVolume = 0f;
            float connectedShuntNegativeVolume = 0f;
            float disconnectedShuntNegativeVolume = 0f;
        }

        ConnectedPower balanceMainCC = new ConnectedPower();
        ConnectedPower balanceOtherCC = new ConnectedPower();

        for (Bus b : network.getBusBreakerView().getBuses()) {
            if (b.isInMainConnectedComponent()) {
                balanceMainCC.busCount++;
            } else {
                balanceOtherCC.busCount++;
            }
        }

        for (Load l : network.getLoads()) {
            Terminal.BusBreakerView view = l.getTerminal().getBusBreakerView();
            if (view.getBus() != null) {
                if (view.getBus().isInMainConnectedComponent()) {
                    balanceMainCC.connectedLoads.add(l.getId());
                    balanceMainCC.connectedLoadVolume += l.getP0();
                } else {
                    balanceOtherCC.connectedLoads.add(l.getId());
                    balanceOtherCC.connectedLoadVolume += l.getP0();
                }
            } else {
                if (view.getConnectableBus().isInMainConnectedComponent()) {
                    balanceMainCC.disconnectedLoads.add(l.getId());
                    balanceMainCC.disconnectedLoadVolume += l.getP0();
                } else {
                    balanceOtherCC.disconnectedLoads.add(l.getId());
                    balanceOtherCC.disconnectedLoadVolume += l.getP0();
                }
            }
        }
        for (DanglingLine dl : network.getDanglingLines()) {
            Terminal.BusBreakerView view = dl.getTerminal().getBusBreakerView();
            if (view.getBus() != null) {
                if (view.getBus().isInMainConnectedComponent()) {
                    balanceMainCC.connectedLoads.add(dl.getId());
                    balanceMainCC.connectedLoadVolume += dl.getP0();
                } else {
                    balanceOtherCC.connectedLoads.add(dl.getId());
                    balanceOtherCC.connectedLoadVolume += dl.getP0();
                }
            } else {
                if (view.getConnectableBus().isInMainConnectedComponent()) {
                    balanceMainCC.disconnectedLoads.add(dl.getId());
                    balanceMainCC.disconnectedLoadVolume += dl.getP0();
                } else {
                    balanceOtherCC.disconnectedLoads.add(dl.getId());
                    balanceOtherCC.disconnectedLoadVolume += dl.getP0();
                }
            }
        }
        for (Generator g : network.getGenerators()) {
            Terminal.BusBreakerView view = g.getTerminal().getBusBreakerView();
            if (view.getBus() != null) {
                if (view.getBus().isInMainConnectedComponent()) {
                    balanceMainCC.connectedMaxGeneration += g.getMaxP();
                    balanceMainCC.connectedGeneration += g.getTargetP();
                    balanceMainCC.connectedGenerators.add(g.getId());
                } else {
                    balanceOtherCC.connectedMaxGeneration += g.getMaxP();
                    balanceOtherCC.connectedGeneration += g.getTargetP();
                    balanceOtherCC.connectedGenerators.add(g.getId());
                }
            } else {
                if (view.getConnectableBus().isInMainConnectedComponent()) {
                    balanceMainCC.disconnectedMaxGeneration += g.getMaxP();
                    balanceMainCC.disconnectedGeneration += g.getTargetP();
                    balanceMainCC.disconnectedGenerators.add(g.getId());
                } else {
                    balanceOtherCC.disconnectedMaxGeneration += g.getMaxP();
                    balanceOtherCC.disconnectedGeneration += g.getTargetP();
                    balanceOtherCC.disconnectedGenerators.add(g.getId());
                }
            }
        }
        for (ShuntCompensator sc : network.getShunts()) {
            Terminal.BusBreakerView view = sc.getTerminal().getBusBreakerView();
            double q = sc.getCurrentB() * Math.pow(sc.getTerminal().getVoltageLevel().getNominalV(), 2);
            if (view.getBus() != null) {
                if (view.getBus().isInMainConnectedComponent()) {
                    if (q > 0) {
                        balanceMainCC.connectedShuntPositiveVolume += q;
                    } else {
                        balanceMainCC.connectedShuntNegativeVolume += q;
                    }
                    balanceMainCC.connectedShunts.add(sc.getId());
                } else {
                    if (q > 0) {
                        balanceOtherCC.connectedShuntPositiveVolume += q;
                    } else {
                        balanceOtherCC.connectedShuntNegativeVolume += q;
                    }
                    balanceOtherCC.connectedShunts.add(sc.getId());
                }
            } else {
                if (view.getConnectableBus().isInMainConnectedComponent()) {
                    if (q > 0) {
                        balanceMainCC.disconnectedShuntPositiveVolume += q;
                    } else {
                        balanceMainCC.disconnectedShuntNegativeVolume += q;
                    }
                    balanceMainCC.disconnectedShunts.add(sc.getId());
                } else {
                    if (q > 0) {
                        balanceOtherCC.disconnectedShuntPositiveVolume += q;
                    } else {
                        balanceOtherCC.disconnectedShuntNegativeVolume += q;
                    }
                    balanceOtherCC.disconnectedShunts.add(sc.getId());
                }
            }
        }
        Table table = new Table(5, BorderStyle.CLASSIC_WIDE);
        table.addCell("");
        table.addCell("Main CC connected/disconnected", 2);
        table.addCell("Others CC connected/disconnected", 2);
        table.addCell("Bus count");
        CellStyle centerStyle = new CellStyle(CellStyle.HorizontalAlign.center);
        table.addCell(Integer.toString(balanceMainCC.busCount), centerStyle, 2);
        table.addCell(Integer.toString(balanceOtherCC.busCount), centerStyle, 2);
        table.addCell("Load count");
        table.addCell("" + balanceMainCC.connectedLoads.size());
        table.addCell("" + balanceMainCC.disconnectedLoads.size());
        table.addCell("" + balanceOtherCC.connectedLoads.size());
        table.addCell("" + balanceOtherCC.disconnectedLoads.size());
        table.addCell("Load (MW)");
        table.addCell("" + balanceMainCC.connectedLoadVolume);
        table.addCell("" + balanceMainCC.disconnectedLoadVolume);
        table.addCell("" + balanceOtherCC.connectedLoadVolume);
        table.addCell("" + balanceOtherCC.disconnectedLoadVolume);
        table.addCell("Generator count");
        table.addCell("" + balanceMainCC.connectedGenerators.size());
        table.addCell("" + balanceMainCC.disconnectedGenerators.size());
        table.addCell("" + balanceOtherCC.connectedGenerators.size());
        table.addCell("" + balanceOtherCC.disconnectedGenerators.size());
        table.addCell("Max generation (MW)");
        table.addCell("" + balanceMainCC.connectedMaxGeneration);
        table.addCell("" + balanceMainCC.disconnectedMaxGeneration);
        table.addCell("" + balanceOtherCC.connectedMaxGeneration);
        table.addCell("" + balanceOtherCC.disconnectedMaxGeneration);
        table.addCell("Generation (MW)");
        table.addCell("" + balanceMainCC.connectedGeneration);
        table.addCell("" + balanceMainCC.disconnectedGeneration);
        table.addCell("" + balanceOtherCC.connectedGeneration);
        table.addCell("" + balanceOtherCC.disconnectedGeneration);
        table.addCell("Shunt at nom V (MVar)");
        table.addCell("" + balanceMainCC.connectedShuntPositiveVolume + " " + balanceMainCC.connectedShuntNegativeVolume + " (" + balanceMainCC.connectedShunts.size() + ")");
        table.addCell("" + balanceMainCC.disconnectedShuntPositiveVolume + " " + balanceMainCC.disconnectedShuntNegativeVolume + " (" + balanceMainCC.disconnectedShunts.size() + ")");
        table.addCell("" + balanceOtherCC.connectedShuntPositiveVolume + " " + balanceOtherCC.connectedShuntNegativeVolume + " (" + balanceOtherCC.connectedShunts.size() + ")");
        table.addCell("" + balanceOtherCC.disconnectedShuntPositiveVolume + " " + balanceOtherCC.disconnectedShuntNegativeVolume + " (" + balanceOtherCC.disconnectedShunts.size() + ")");

        logger.debug("Active balance at step '{}':\n{}", title, table.render());

        if (balanceOtherCC.connectedLoads.size() > 0) {
            logger.trace("Connected loads in other CC: {}", balanceOtherCC.connectedLoads);
        }
        if (balanceOtherCC.disconnectedLoads.size() > 0) {
            logger.trace("Disconnected loads in other CC: {}", balanceOtherCC.disconnectedLoads);
        }
        if (balanceOtherCC.connectedGenerators.size() > 0) {
            logger.trace("Connected generators in other CC: {}", balanceOtherCC.connectedGenerators);
        }
        if (balanceOtherCC.disconnectedGenerators.size() > 0) {
            logger.trace("Disconnected generators in other CC: {}", balanceOtherCC.disconnectedGenerators);
        }
        if (balanceOtherCC.disconnectedShunts.size() > 0) {
            logger.trace("Disconnected shunts in other CC: {}", balanceOtherCC.disconnectedShunts);
        }
    }


    public static void printGeneratorsSetpointDiff(Network network, Logger logger) {
        for (Generator g : network.getGenerators()) {
            float dp = Math.abs(g.getTerminal().getP() + g.getTargetP());
            float dq = Math.abs(g.getTerminal().getQ() + g.getTargetQ());
            float dv = Math.abs(g.getTerminal().getBusBreakerView().getConnectableBus().getV() - g.getTargetV());
            if (dp > 1 || dq > 5 || dv > 0.1) {
                logger.warn("Generator {}: ({}, {}, {}) ({}, {}, {}) -> ({}, {}, {})", g.getId(),
                        dp, dq, dv,
                        -g.getTargetP(), -g.getTargetQ(), g.getTargetV(),
                        g.getTerminal().getP(), g.getTerminal().getQ(), g.getTerminal().getBusBreakerView().getConnectableBus().getV());
            }
        }
    }

    private static double EPSILON_X = 0.01;

    public static void checkFlows(String id, double r, double x, double rho1, double rho2, double u1, double u2, double theta1, double theta2, double alpha1,
                                  double alpha2, double g1, double g2, double b1, double b2, float p1, float q1, float p2, float q2) {
        if (Math.abs(x) < EPSILON_X) {
            // System.out.println("x " + x + " -> " + EPSILON_X);
            // x = EPSILON_X;
        }
        double z = Math.hypot(r, x);
        double y = 1 / z;
        double ksi = Math.atan2(r, x);
        double p1_calc = rho1 * rho2 * u1 * u2 * y * Math.sin(theta1 - theta2 - ksi + alpha1 - alpha2) + rho1 * rho1 * u1 * u1 * (y * Math.sin(ksi) + g1);
        double q1_calc = - rho1 * rho2 * u1 * u2 * y * Math.cos(theta1 - theta2 - ksi + alpha1 - alpha2) + rho1 * rho1 * u1 * u1 * (y * Math.cos(ksi) - b1);
        double p2_calc = rho2 * rho1 * u2 * u1 * y * Math.sin(theta2 - theta1 - ksi + alpha2 - alpha1) + rho2 * rho2 * u2 * u2 * (y * Math.sin(ksi) + g2);
        double q2_calc = - rho2 * rho1 * u2 * u1 * y * Math.cos(theta2 - theta1 - ksi + alpha2 - alpha1) + rho2 * rho2 * u2 * u2 * (y * Math.cos(ksi) - b2);

        boolean debug = false;
        if (debug) {
            System.out.println("r=" + r);
            System.out.println("x=" + x);
            System.out.println("g1=" + g1);
            System.out.println("g2=" + g2);
            System.out.println("b1=" + b1);
            System.out.println("b2=" + b2);
            System.out.println("rho1=" + rho1);
            System.out.println("rho2=" + rho2);
            System.out.println("alpha1=" + alpha1);
            System.out.println("alpha2=" + alpha2);
            System.out.println("u1=" + u1);
            System.out.println("u2=" + u2);
            System.out.println("theta1=" + theta1);
            System.out.println("theta2=" + theta2);
            System.out.println("z=" + z);
            System.out.println("y=" + y);
            System.out.println("ksi=" + ksi);
            System.out.println("p1=" + p1);
            System.out.println("q1=" + q1);
            System.out.println("p2=" + p2);
            System.out.println("q2=" + q2);
        }
        float seuil = 0.1f;
        if (Math.abs(p1 - p1_calc) > seuil) {
            System.out.println(id + " P1 " + p1 + " " + p1_calc);
        }
        if (Math.abs(q1 - q1_calc) > seuil) {
            System.out.println(id + " Q1 " + q1 + " " + q1_calc);
        }
        if (Math.abs(p2 - p2_calc) > seuil) {
            System.out.println(id + " P2 " + p2 + " " + p2_calc);
        }
        if (Math.abs(q2 - q2_calc) > seuil) {
            System.out.println(id + " Q2 " + q2 + " " + q2_calc);
        }
    }

    public static void checkFlows(Line l) {
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
            checkFlows(l.getId(), r, x, rho1, rho2, u1, u2, theta1, theta2, alpha1, alpha2, g1, g2, b1, b2, p1, q1, p2, q2);
        }
    }

    public static void checkFlows(TwoWindingsTransformer twt) {
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
            checkFlows(twt.getId(), r, x, rho1, rho2, u1, u2, theta1, theta2, alpha1, alpha2, g1, g2, b1, b2, p1, q1, p2, q2);
        }
    }

    public static void checkFlows(Network network) {
        for (Line l : network.getLines()) {
            checkFlows(l);
        }
        for (TwoWindingsTransformer twt : network.getTwoWindingsTransformers()) {
            checkFlows(twt);
        }
    }

}
