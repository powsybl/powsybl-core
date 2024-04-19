/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util;

import com.google.common.collect.ImmutableMap;
import com.powsybl.commons.io.table.AbstractTableFormatter;
import com.powsybl.commons.io.table.AsciiTableFormatter;
import com.powsybl.commons.io.table.Column;
import com.powsybl.commons.io.table.HorizontalAlignment;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.*;
import com.powsybl.math.graph.TraverseResult;
import org.slf4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public final class Networks {

    private Networks() {
    }

    public static boolean isBusValid(int feederCount) {
        return feederCount >= 1;
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

    public static void printBalanceSummary(String title, Network network, Writer writer) throws IOException {
        Objects.requireNonNull(title);
        Objects.requireNonNull(network);
        Objects.requireNonNull(writer);

        ConnectedPower balanceMainCC = new ConnectedPower();
        ConnectedPower balanceOtherCC = new ConnectedPower();

        addBuses(network, balanceMainCC, balanceOtherCC);
        addLoads(network, balanceMainCC, balanceOtherCC);
        addDanglingLines(network, balanceMainCC, balanceOtherCC);
        addGenerators(network, balanceMainCC, balanceOtherCC);
        addShuntCompensators(network, balanceMainCC, balanceOtherCC);

        logOtherCC(writer, title, () -> writeInTable(balanceMainCC, balanceOtherCC), balanceOtherCC);
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
        for (DanglingLine dl : network.getDanglingLines(DanglingLineFilter.UNPAIRED)) {
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
            double q = sc.getB() * Math.pow(sc.getTerminal().getVoltageLevel().getNominalV(), 2);
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

    private static void logOtherCC(Writer writer, String title, Supplier<String> tableSupplier, ConnectedPower balanceOtherCC) throws IOException {
        writer.write("Active balance at step '" + title + "':" + System.lineSeparator() + tableSupplier.get());

        if (!balanceOtherCC.connectedLoads.isEmpty()) {
            writer.write("Connected loads in other CC: " + balanceOtherCC.connectedLoads + System.lineSeparator());
        }
        if (!balanceOtherCC.disconnectedLoads.isEmpty()) {
            writer.write("Disconnected loads in other CC: " + balanceOtherCC.disconnectedLoads + System.lineSeparator());
        }
        if (!balanceOtherCC.connectedGenerators.isEmpty()) {
            writer.write("Connected generators in other CC: " + balanceOtherCC.connectedGenerators + System.lineSeparator());
        }
        if (!balanceOtherCC.disconnectedGenerators.isEmpty()) {
            writer.write("Disconnected generators in other CC: " + balanceOtherCC.disconnectedGenerators + System.lineSeparator());
        }
        if (!balanceOtherCC.disconnectedShunts.isEmpty()) {
            writer.write("Disconnected shunts in other CC: " + balanceOtherCC.disconnectedShunts + System.lineSeparator());
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

    /**
     * Return the list of nodes (N/B topology) for each bus of a the Bus view
     * If a node is not associated to a bus, it is not included in any list.
     * @param voltageLevel The voltage level to traverse
     * @return the list of nodes (N/B topology) for each bus of a Bus view
     */
    public static Map<String, Set<Integer>> getNodesByBus(VoltageLevel voltageLevel) {
        checkNodeBreakerVoltageLevel(voltageLevel);

        Map<String, Set<Integer>> nodesByBus = new TreeMap<>();
        for (int i : voltageLevel.getNodeBreakerView().getNodes()) {
            Terminal terminal = voltageLevel.getNodeBreakerView().getTerminal(i);
            if (terminal != null) {
                Bus bus = terminal.getBusView().getBus();
                if (bus != null) {
                    nodesByBus.computeIfAbsent(bus.getId(), k -> new TreeSet<>()).add(i);
                }
            } else {
                // If there is no terminal for the current node, we try to find one traversing the topology
                Terminal equivalentTerminal = getEquivalentTerminal(voltageLevel, i);

                if (equivalentTerminal != null) {
                    Bus bus = equivalentTerminal.getBusView().getBus();
                    if (bus != null) {
                        nodesByBus.computeIfAbsent(bus.getId(), k -> new TreeSet<>()).add(i);
                    }
                }
            }
        }

        return nodesByBus;
    }

    public static IntStream getNodes(String busId, VoltageLevel voltageLevel, Function<Terminal, Bus> getBusFromTerminal) {
        checkNodeBreakerVoltageLevel(voltageLevel);
        Set<Integer> nodes = new TreeSet<>();
        for (int i : voltageLevel.getNodeBreakerView().getNodes()) {
            Terminal terminal = voltageLevel.getNodeBreakerView().getTerminal(i);
            if (terminal != null) {
                Bus bus = getBusFromTerminal.apply(terminal);
                if (bus != null && bus.getId().equals(busId)) {
                    nodes.add(i);
                }
            } else {
                // If there is no terminal for the current node, we try to find one traversing the topology
                Terminal equivalentTerminal = Networks.getEquivalentTerminal(voltageLevel, i);

                if (equivalentTerminal != null) {
                    Bus bus = getBusFromTerminal.apply(equivalentTerminal);
                    if (bus != null && bus.getId().equals(busId)) {
                        nodes.add(i);
                    }
                }
            }
        }
        return nodes.stream().mapToInt(Integer::intValue);
    }

    /**
     * Return a terminal for the specified node.
     * If a terminal is attached to the node, return this terminal. Otherwise, this method traverses the topology and return
     * the first equivalent terminal found.
     *
     * @param voltageLevel The voltage level to traverse
     * @param node The starting node
     * @return A terminal for the specified node or null.
     */
    public static Terminal getEquivalentTerminal(VoltageLevel voltageLevel, int node) {
        checkNodeBreakerVoltageLevel(voltageLevel);

        Terminal[] equivalentTerminal = new Terminal[1];

        VoltageLevel.NodeBreakerView.TopologyTraverser traverser = (node1, sw, node2) -> {
            if (sw != null && sw.isOpen()) {
                return TraverseResult.TERMINATE_PATH;
            }
            Terminal t = voltageLevel.getNodeBreakerView().getTerminal(node2);
            if (t != null) {
                equivalentTerminal[0] = t;
                return TraverseResult.TERMINATE_TRAVERSER;
            }
            return TraverseResult.CONTINUE;
        };

        voltageLevel.getNodeBreakerView().traverse(node, traverser);

        return equivalentTerminal[0];
    }

    private static void checkNodeBreakerVoltageLevel(VoltageLevel voltageLevel) {
        if (voltageLevel.getTopologyKind() != TopologyKind.NODE_BREAKER) {
            throw new IllegalArgumentException("The voltage level " + voltageLevel.getId() + " is not described in Node/Breaker topology");
        }
    }

    /**
     * Set a {@link ReportNode} in the reportNode context of the given network, execute a runnable then restore the reportNode context.
     *
     * @param network a network
     * @param reportNode the reportNode to use
     * @param runnable the runnable to execute
     */
    public static void executeWithReportNode(Network network, ReportNode reportNode, Runnable runnable) {
        network.getReportNodeContext().pushReportNode(reportNode);
        try {
            runnable.run();
        } finally {
            network.getReportNodeContext().popReportNode();
        }
    }

    /**
     * Returns a {@link ReportNodeContext} containing the same reportNodes as the given one,
     * but reconfigured to allow it, or not, to be accessed simultaneously by different threads.
     * When this option is activated, the reportNode context can have a different content
     * for each thread.
     *
     * @param reportNodeContext the ReportNodeContext to reconfigure
     * @param allow allow multi-thread access to the ReportNodeContext
     * @return the reconfigured ReportNodeContext
     */
    public static AbstractReportNodeContext allowReportNodeContextMultiThreadAccess(AbstractReportNodeContext reportNodeContext, boolean allow) {
        AbstractReportNodeContext newReportNodeContext = null;
        if (allow && !(reportNodeContext instanceof MultiThreadReportNodeContext)) {
            newReportNodeContext = new MultiThreadReportNodeContext(reportNodeContext);
        } else if (!allow && !(reportNodeContext instanceof SimpleReportNodeContext)) {
            newReportNodeContext = new SimpleReportNodeContext(reportNodeContext);
            if (reportNodeContext instanceof MultiThreadReportNodeContext multiThreadReportNodeContext) {
                multiThreadReportNodeContext.close(); // to avoid memory leaks
            }
        }
        return newReportNodeContext != null ? newReportNodeContext : reportNodeContext;
    }
}
