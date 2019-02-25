/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.google.common.collect.ImmutableMap;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.io.table.AbstractTableFormatter;
import com.powsybl.commons.io.table.AsciiTableFormatter;
import com.powsybl.commons.io.table.Column;
import com.powsybl.commons.io.table.HorizontalAlignment;
import com.powsybl.iidm.network.*;
import org.slf4j.Logger;

import javax.script.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

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
        return ImmutableMap.of("variant", network.getVariantManager().getWorkingVariantId());
    }

    public static void dumpVariantId(Path workingDir, String variantId) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(workingDir.resolve("variant.txt"), StandardCharsets.UTF_8)) {
            writer.write(variantId);
            writer.newLine();
        }
    }

    public static void dumpVariantId(Path workingDir, Network network) throws IOException {
        dumpVariantId(workingDir, network.getVariantManager().getWorkingVariantId());
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

        logOtherCC(logger, title, () -> writeInTable(balanceMainCC, balanceOtherCC), balanceOtherCC);
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

    private static String writeInTable(ConnectedPower balanceMainCC, ConnectedPower balanceOtherCC) {
        Writer writer = new StringWriter();
        try (AbstractTableFormatter formatter = new AsciiTableFormatter(writer, null,
                     new Column("")
                             .setTitleHorizontalAlignment(HorizontalAlignment.CENTER),
                     new Column("Main CC connected/disconnected")
                             .setColspan(2)
                             .setTitleHorizontalAlignment(HorizontalAlignment.CENTER),
                     new Column("Others CC connected/disconnected")
                             .setColspan(2)
                             .setTitleHorizontalAlignment(HorizontalAlignment.CENTER))) {
            formatter.writeCell("Bus count")
                    .writeCell(Integer.toString(balanceMainCC.busCount), 2)
                    .writeCell(Integer.toString(balanceOtherCC.busCount), 2);
            formatter.writeCell("Load count")
                    .writeCell(Integer.toString(balanceMainCC.connectedLoads.size()))
                    .writeCell(Integer.toString(balanceMainCC.disconnectedLoads.size()))
                    .writeCell(Integer.toString(balanceOtherCC.connectedLoads.size()))
                    .writeCell(Integer.toString(balanceOtherCC.disconnectedLoads.size()));
            formatter.writeCell("Load (MW)")
                    .writeCell(Double.toString(balanceMainCC.connectedLoadVolume))
                    .writeCell(Double.toString(balanceMainCC.disconnectedLoadVolume))
                    .writeCell(Double.toString(balanceOtherCC.connectedLoadVolume))
                    .writeCell(Double.toString(balanceOtherCC.disconnectedLoadVolume));
            formatter.writeCell("Generator count")
                    .writeCell(Integer.toString(balanceMainCC.connectedGenerators.size()))
                    .writeCell(Integer.toString(balanceMainCC.disconnectedGenerators.size()))
                    .writeCell(Integer.toString(balanceOtherCC.connectedGenerators.size()))
                    .writeCell(Integer.toString(balanceOtherCC.disconnectedGenerators.size()));
            formatter.writeCell("Max generation (MW)")
                    .writeCell(Double.toString(balanceMainCC.connectedMaxGeneration))
                    .writeCell(Double.toString(balanceMainCC.disconnectedMaxGeneration))
                    .writeCell(Double.toString(balanceOtherCC.connectedMaxGeneration))
                    .writeCell(Double.toString(balanceOtherCC.disconnectedMaxGeneration));
            formatter.writeCell("Generation (MW)")
                    .writeCell(Double.toString(balanceMainCC.connectedGeneration))
                    .writeCell(Double.toString(balanceMainCC.disconnectedGeneration))
                    .writeCell(Double.toString(balanceOtherCC.connectedGeneration))
                    .writeCell(Double.toString(balanceOtherCC.disconnectedGeneration));
            formatter.writeCell("Shunt at nom V (MVar)")
                    .writeCell(Double.toString(balanceMainCC.connectedShuntPositiveVolume) + " " +
                            Double.toString(balanceMainCC.connectedShuntNegativeVolume) +
                            " (" + Integer.toString(balanceMainCC.connectedShunts.size()) + ")")
                    .writeCell(Double.toString(balanceMainCC.disconnectedShuntPositiveVolume) + " " +
                            Double.toString(balanceMainCC.disconnectedShuntNegativeVolume) +
                            " (" + Integer.toString(balanceMainCC.disconnectedShunts.size()) + ")")
                    .writeCell(Double.toString(balanceOtherCC.connectedShuntPositiveVolume) + " " +
                            Double.toString(balanceOtherCC.connectedShuntNegativeVolume) +
                            " (" + Integer.toString(balanceOtherCC.connectedShunts.size()) + ")")
                    .writeCell(Double.toString(balanceOtherCC.disconnectedShuntPositiveVolume) + " " +
                            Double.toString(balanceOtherCC.disconnectedShuntNegativeVolume) +
                            " (" + Integer.toString(balanceOtherCC.disconnectedShunts.size()) + ")");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return writer.toString();
    }

    private static void logOtherCC(Logger logger, String title, Supplier<String> tableSupplier, ConnectedPower balanceOtherCC) {
        if (logger.isDebugEnabled()) {
            logger.debug("Active balance at step '{}':\n{}", title, tableSupplier.get());
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
