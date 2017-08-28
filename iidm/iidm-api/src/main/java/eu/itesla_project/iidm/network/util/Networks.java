/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.util;

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

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.Table;
import org.slf4j.Logger;

import com.google.common.collect.ImmutableMap;

import eu.itesla_project.commons.ITeslaException;
import eu.itesla_project.iidm.network.Bus;
import eu.itesla_project.iidm.network.DanglingLine;
import eu.itesla_project.iidm.network.Generator;
import eu.itesla_project.iidm.network.Load;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.ShuntCompensator;
import eu.itesla_project.iidm.network.Terminal;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class Networks {

    private Networks() {
    }

    public static boolean isBusValid(int feederCount, int branchCount) {
        return branchCount >= 1;
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

}
