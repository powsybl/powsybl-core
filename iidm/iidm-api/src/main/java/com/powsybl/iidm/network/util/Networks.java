/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

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

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.Terminal;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class Networks {

    private Networks() {
    }

    public static boolean isBusValid(int branchCount) {
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
            throw new PowsyblException(e);
        }
    }

    static class ConnectedPower {
        private int busCount = 0;

        private List<String> connectedLoads = new ArrayList<>();
        private List<String> disconnectedLoads = new ArrayList<>();
        private double connectedLoadVolume = 0.0;
        private double disconnectedLoadVolume = 0.0;

        private double connectedMaxGeneration = 0.0;
        private double disconnectedMaxGeneration = 0.0;
        private double connectedGeneration = 0.0;
        private double disconnectedGeneration = 0.0;
        private List<String> connectedGenerators = new ArrayList<>();
        private List<String> disconnectedGenerators = new ArrayList<>();

        private List<String> connectedShunts = new ArrayList<>();
        private List<String> disconnectedShunts = new ArrayList<>();
        private double connectedShuntPositiveVolume = 0.0;
        private double disconnectedShuntPositiveVolume = 0.0;
        private double connectedShuntNegativeVolume = 0.0;
        private double disconnectedShuntNegativeVolume = 0.0;
    }

    public static void printBalanceSummary(String title, Network network, Logger logger) {

        ConnectedPower balanceMainCC = new ConnectedPower();
        ConnectedPower balanceOtherCC = new ConnectedPower();

        addBuses(network, balanceMainCC, balanceOtherCC);
        addLoads(network, balanceMainCC, balanceOtherCC);
        addDanglingLines(network, balanceMainCC, balanceOtherCC);
        addGenerators(network, balanceMainCC, balanceOtherCC);
        addShuntCompensators(network, balanceMainCC, balanceOtherCC);

        Table table = writeInTable(balanceMainCC, balanceOtherCC);

        logOtherCC(logger, title, table, balanceOtherCC);
    }

    private static void addBuses(Network network, ConnectedPower balanceMainCC, ConnectedPower balanceOtherCC) {
        for (Bus b : network.getBusBreakerView().getBuses()) {
            if (b.isInMainConnectedComponent()) {
                balanceMainCC.busCount++;
            } else {
                balanceOtherCC.busCount++;
            }
        }
    }

    private static void addLoads(Network network, ConnectedPower balanceMainCC, ConnectedPower balanceOtherCC) {
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
    }

    private static void addDanglingLines(Network network, ConnectedPower balanceMainCC, ConnectedPower balanceOtherCC) {
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
    }

    private static void addGenerators(Network network, ConnectedPower balanceMainCC, ConnectedPower balanceOtherCC) {
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
    }

    private static void addShuntCompensators(Network network, ConnectedPower balanceMainCC, ConnectedPower balanceOtherCC) {
        for (ShuntCompensator sc : network.getShuntCompensators()) {
            Terminal.BusBreakerView view = sc.getTerminal().getBusBreakerView();
            double q = sc.getCurrentB() * Math.pow(sc.getTerminal().getVoltageLevel().getNominalV(), 2);
            if (view.getBus() != null) {
                addConnectedShunt(view, q, sc.getId(), balanceMainCC, balanceOtherCC);
            } else {
                addDisonnectedShunt(view, q, sc.getId(), balanceMainCC, balanceOtherCC);
            }
        }
    }

    private static void addConnectedShunt(Terminal.BusBreakerView view, double q, String shuntId, ConnectedPower balanceMainCC, ConnectedPower balanceOtherCC) {
        if (view.getBus().isInMainConnectedComponent()) {
            if (q > 0) {
                balanceMainCC.connectedShuntPositiveVolume += q;
            } else {
                balanceMainCC.connectedShuntNegativeVolume += q;
            }
            balanceMainCC.connectedShunts.add(shuntId);
        } else {
            if (q > 0) {
                balanceOtherCC.connectedShuntPositiveVolume += q;
            } else {
                balanceOtherCC.connectedShuntNegativeVolume += q;
            }
            balanceOtherCC.connectedShunts.add(shuntId);
        }
    }

    private static void addDisonnectedShunt(Terminal.BusBreakerView view, double q, String shuntId, ConnectedPower balanceMainCC, ConnectedPower balanceOtherCC) {
        if (view.getConnectableBus().isInMainConnectedComponent()) {
            if (q > 0) {
                balanceMainCC.disconnectedShuntPositiveVolume += q;
            } else {
                balanceMainCC.disconnectedShuntNegativeVolume += q;
            }
            balanceMainCC.disconnectedShunts.add(shuntId);
        } else {
            if (q > 0) {
                balanceOtherCC.disconnectedShuntPositiveVolume += q;
            } else {
                balanceOtherCC.disconnectedShuntNegativeVolume += q;
            }
            balanceOtherCC.disconnectedShunts.add(shuntId);
        }
    }


    private static Table writeInTable(ConnectedPower balanceMainCC, ConnectedPower balanceOtherCC) {
        Table table = new Table(5, BorderStyle.CLASSIC_WIDE);
        table.addCell("");
        table.addCell("Main CC connected/disconnected", 2);
        table.addCell("Others CC connected/disconnected", 2);
        table.addCell("Bus count");
        CellStyle centerStyle = new CellStyle(CellStyle.HorizontalAlign.center);
        table.addCell(Integer.toString(balanceMainCC.busCount), centerStyle, 2);
        table.addCell(Integer.toString(balanceOtherCC.busCount), centerStyle, 2);
        table.addCell("Load count");
        table.addCell(Integer.toString(balanceMainCC.connectedLoads.size()));
        table.addCell(Integer.toString(balanceMainCC.disconnectedLoads.size()));
        table.addCell(Integer.toString(balanceOtherCC.connectedLoads.size()));
        table.addCell(Integer.toString(balanceOtherCC.disconnectedLoads.size()));
        table.addCell("Load (MW)");
        table.addCell(Double.toString(balanceMainCC.connectedLoadVolume));
        table.addCell(Double.toString(balanceMainCC.disconnectedLoadVolume));
        table.addCell(Double.toString(balanceOtherCC.connectedLoadVolume));
        table.addCell(Double.toString(balanceOtherCC.disconnectedLoadVolume));
        table.addCell("Generator count");
        table.addCell(Integer.toString(balanceMainCC.connectedGenerators.size()));
        table.addCell(Integer.toString(balanceMainCC.disconnectedGenerators.size()));
        table.addCell(Integer.toString(balanceOtherCC.connectedGenerators.size()));
        table.addCell(Integer.toString(balanceOtherCC.disconnectedGenerators.size()));
        table.addCell("Max generation (MW)");
        table.addCell(Double.toString(balanceMainCC.connectedMaxGeneration));
        table.addCell(Double.toString(balanceMainCC.disconnectedMaxGeneration));
        table.addCell(Double.toString(balanceOtherCC.connectedMaxGeneration));
        table.addCell(Double.toString(balanceOtherCC.disconnectedMaxGeneration));
        table.addCell("Generation (MW)");
        table.addCell(Double.toString(balanceMainCC.connectedGeneration));
        table.addCell(Double.toString(balanceMainCC.disconnectedGeneration));
        table.addCell(Double.toString(balanceOtherCC.connectedGeneration));
        table.addCell(Double.toString(balanceOtherCC.disconnectedGeneration));
        table.addCell("Shunt at nom V (MVar)");
        table.addCell(Double.toString(balanceMainCC.connectedShuntPositiveVolume) + " " +
                Double.toString(balanceMainCC.connectedShuntNegativeVolume) +
                " (" + Integer.toString(balanceMainCC.connectedShunts.size()) + ")");
        table.addCell(Double.toString(balanceMainCC.disconnectedShuntPositiveVolume) + " " +
                Double.toString(balanceMainCC.disconnectedShuntNegativeVolume) +
                " (" + Integer.toString(balanceMainCC.disconnectedShunts.size()) + ")");
        table.addCell(Double.toString(balanceOtherCC.connectedShuntPositiveVolume) + " " +
                Double.toString(balanceOtherCC.connectedShuntNegativeVolume) +
                " (" + Integer.toString(balanceOtherCC.connectedShunts.size()) + ")");
        table.addCell(Double.toString(balanceOtherCC.disconnectedShuntPositiveVolume) + " " +
                Double.toString(balanceOtherCC.disconnectedShuntNegativeVolume) +
                " (" + Integer.toString(balanceOtherCC.disconnectedShunts.size()) + ")");
        return table;
    }

    private static void logOtherCC(Logger logger, String title, Table table, ConnectedPower balanceOtherCC) {
        if (logger.isDebugEnabled()) {
            logger.debug("Active balance at step '{}':\n{}", title, table.render());
        }

        if (!balanceOtherCC.connectedLoads.isEmpty()) {
            logger.trace("Connected loads in other CC: {}", balanceOtherCC.connectedLoads);
        }
        if (!balanceOtherCC.disconnectedLoads.isEmpty()) {
            logger.trace("Disconnected loads in other CC: {}", balanceOtherCC.disconnectedLoads);
        }
        if (!balanceOtherCC.connectedGenerators.isEmpty()) {
            logger.trace("Connected generators in other CC: {}", balanceOtherCC.connectedGenerators);
        }
        if (!balanceOtherCC.disconnectedGenerators.isEmpty()) {
            logger.trace("Disconnected generators in other CC: {}", balanceOtherCC.disconnectedGenerators);
        }
        if (!balanceOtherCC.disconnectedShunts.isEmpty()) {
            logger.trace("Disconnected shunts in other CC: {}", balanceOtherCC.disconnectedShunts);
        }
    }


    public static void printGeneratorsSetpointDiff(Network network, Logger logger) {
        for (Generator g : network.getGenerators()) {
            double dp = Math.abs(g.getTerminal().getP() + g.getTargetP());
            double dq = Math.abs(g.getTerminal().getQ() + g.getTargetQ());
            double dv = Math.abs(g.getTerminal().getBusBreakerView().getConnectableBus().getV() - g.getTargetV());
            if (dp > 1 || dq > 5 || dv > 0.1) {
                logger.warn("Generator {}: ({}, {}, {}) ({}, {}, {}) -> ({}, {}, {})", g.getId(),
                        dp, dq, dv,
                        -g.getTargetP(), -g.getTargetQ(), g.getTargetV(),
                        g.getTerminal().getP(), g.getTerminal().getQ(), g.getTerminal().getBusBreakerView().getConnectableBus().getV());
            }
        }
    }

}
