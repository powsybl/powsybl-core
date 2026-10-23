/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util;

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
import java.util.stream.Collectors;
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
        return Map.of("variant", network.getVariantManager().getWorkingVariantId());
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

        private final List<String> connectedLoads = new ArrayList<>();
        private final List<String> disconnectedLoads = new ArrayList<>();
        private double connectedLoadVolume = 0.0;
        private double disconnectedLoadVolume = 0.0;

        private double connectedMaxGeneration = 0.0;
        private double disconnectedMaxGeneration = 0.0;
        private double connectedGeneration = 0.0;
        private double disconnectedGeneration = 0.0;
        private final List<String> connectedGenerators = new ArrayList<>();
        private final List<String> disconnectedGenerators = new ArrayList<>();

        private final List<String> connectedShunts = new ArrayList<>();
        private final List<String> disconnectedShunts = new ArrayList<>();
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
        addBoundaryLines(network, balanceMainCC, balanceOtherCC);
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

    private static void addBoundaryLines(Network network, ConnectedPower balanceMainCC, ConnectedPower balanceOtherCC) {
        for (BoundaryLine dl : network.getBoundaryLines(BoundaryLineFilter.UNPAIRED)) {
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
                    .writeCell(balanceMainCC.connectedShuntPositiveVolume + " " +
                        balanceMainCC.connectedShuntNegativeVolume +
                            " (" + balanceMainCC.connectedShunts.size() + ")")
                    .writeCell(balanceMainCC.disconnectedShuntPositiveVolume + " " +
                        balanceMainCC.disconnectedShuntNegativeVolume +
                            " (" + balanceMainCC.disconnectedShunts.size() + ")")
                    .writeCell(balanceOtherCC.connectedShuntPositiveVolume + " " +
                        balanceOtherCC.connectedShuntNegativeVolume +
                            " (" + balanceOtherCC.connectedShunts.size() + ")")
                    .writeCell(balanceOtherCC.disconnectedShuntPositiveVolume + " " +
                        balanceOtherCC.disconnectedShuntNegativeVolume +
                            " (" + balanceOtherCC.disconnectedShunts.size() + ")");
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
            double dq = Math.abs(g.getTerminal().getQ() + g.getLocalTargetQ());
            double dv = Math.abs(g.getTerminal().getBusBreakerView().getConnectableBus().getV() - g.getLocalTargetV());
            if (dp > 1 || dq > 5 || dv > 0.1) {
                logger.warn("Generator {}: ({}, {}, {}) ({}, {}, {}) -> ({}, {}, {})", g.getId(),
                        dp, dq, dv,
                        -g.getTargetP(), -g.getLocalTargetQ(), g.getLocalTargetV(),
                        g.getTerminal().getP(), g.getTerminal().getQ(), g.getTerminal().getBusBreakerView().getConnectableBus().getV());
            }
        }
    }

    /**
     * Return the list of nodes (N/B topology) for each bus of a the Bus view
     * If a node is not associated to a bus, it is not included in any list.
     * @param voltageLevel The voltage level to traverse
     * @return a map with the list of nodes (N/B topology) for each bus of a Bus view
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

    private static void addBusFromTerminal(String busId, Terminal terminal, Function<Terminal, Bus> getBusFromTerminal, Set<Integer> nodes, int node) {
        Bus bus = getBusFromTerminal.apply(terminal);
        if (bus != null && bus.getId().equals(busId)) {
            nodes.add(node);
        }
    }

    public static IntStream getNodes(String busId, VoltageLevel voltageLevel, Function<Terminal, Bus> getBusFromTerminal) {
        checkNodeBreakerVoltageLevel(voltageLevel);
        Set<Integer> nodes = new TreeSet<>();
        for (int i : voltageLevel.getNodeBreakerView().getNodes()) {
            Terminal terminal = voltageLevel.getNodeBreakerView().getTerminal(i);
            if (terminal != null) {
                addBusFromTerminal(busId, terminal, getBusFromTerminal, nodes, i);
            } else {
                // If there is no terminal for the current node, we try to find one traversing the topology
                Terminal equivalentTerminal = Networks.getEquivalentTerminal(voltageLevel, i);
                if (equivalentTerminal != null) {
                    addBusFromTerminal(busId, equivalentTerminal, getBusFromTerminal, nodes, i);
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

    /**
     * This method replaces the "input" values used for load flow calculation by their solved values returned by the
     * load flow calculation. This includes the tap position of tap changers, the section count of shunt compensators,
     * the active and reactive power flow on generators, batteries, loads and on the generation part of a boundary line
     * and the voltage on generators and boundary lines.
     */
    public static void applySolvedValues(Network network) {
        applySolvedTapPositionAndSolvedSectionCount(network);
        network.getGeneratorStream().forEach(Generator::applySolvedValues);
        network.getBatteryStream().forEach(Battery::applySolvedValues);
        network.getLoadStream().forEach(Load::applySolvedValues);
        network.getBoundaryLineStream().forEach(BoundaryLine::applySolvedValues);
    }

    /**
     * This method replaces the "input" status of tap changer position and shunt section count by their solved values if they are present.
     */
    public static void applySolvedTapPositionAndSolvedSectionCount(Network network) {
        network.getTwoWindingsTransformerStream().forEach(TwoWindingsTransformer::applySolvedValues);
        network.getThreeWindingsTransformerStream().forEach(ThreeWindingsTransformer::applySolvedValues);
        network.getShuntCompensatorStream().forEach(ShuntCompensator::applySolvedValues);
    }

    /**
     * This method replaces all the solved values in the network with NaN values. It includes all terminals P and Q (for AC)
     * or I (for DC), buses voltage magnitude (and angle for AC), solved shunt/compensator section count, and tap
     * changers tap position.
     */
    public static void unsetSolvedValues(Network network) {
        network.getBusView().getBusStream().forEach(Bus::unsetSolvedValues);
        network.getConnectableStream().forEach(Connectable::unsetSolvedValues);
        network.getDcBusStream().forEach(bus -> bus.setV(Double.NaN));
        network.getDcConnectableStream().forEach(DcConnectable::unsetSolvedValues);
    }

    public static final String ALIAS_TYPE_ORIGINAL_ID = "original-id";

    public record IdentifierChangeSkipped(String id, String name, IdentifierChangeSkippedReason reason) {

        public enum IdentifierChangeSkippedReason {
            NETWORK,
            NO_NAME,
            NAME_EQUALS_ID,
            DUPLICATED_NAME,
            UNIQUE_NAME_WAS_ID,
            ORIGINAL_ID_ALIAS_ALREADY_PRESENT
        }

        private static IdentifierChangeSkippedReason reason(Identifiable<?> i, String name, Map<String, Long> nameCount, Network network) {
            if (i instanceof Network) {
                return IdentifierChangeSkippedReason.NETWORK;
            }
            if (i.getAliasFromType(ALIAS_TYPE_ORIGINAL_ID).isPresent()) {
                return IdentifierChangeSkippedReason.ORIGINAL_ID_ALIAS_ALREADY_PRESENT;
            }
            if (name == null) {
                return IdentifierChangeSkippedReason.NO_NAME;
            }
            if (name.equals(i.getId())) {
                return IdentifierChangeSkippedReason.NAME_EQUALS_ID;
            }
            if (nameCount.getOrDefault(name, 0L) != 1) {
                return IdentifierChangeSkippedReason.DUPLICATED_NAME;
            }
            if (network.getIdentifiable(name) != null) {
                return IdentifierChangeSkippedReason.UNIQUE_NAME_WAS_ID;
            }
            return null;
        }
    }

    /**
     * Replaces the id of every named identifiable by its name, keeping the original id
     * reachable as an alias of type {@link #ALIAS_TYPE_ORIGINAL_ID}.
     * <p>
     * An identifiable id is changed only when its name can be used as a unique identifier in the network.
     * Conditions that are checked to skip changes:
     * <ul>
     *   <li>The name must not be empty.</li>
     *   <li>The name and id of the identifiable must be different.</li>
     *   <li>The name must be unique for all identifiables considered.
     *       Otherwise, the first one processed would take the name and the others would be
     *       left out, making the result dependent on the iteration order.</li>
     *   <li>The name must not already be in use as an id or as an alias of another identifiable.
     *       {@code Network.getIdentifiable} resolves aliases, so a single lookup covers both.
     *       This condition fires for example when fictitious switches created for disconnected CGMES terminals.
     *       Fictitious switches are named after their terminal , and that identifier is already registered as an
     *       alias of the equipment owning the terminal.</li>
     *   <li>The alias {@link #ALIAS_TYPE_ORIGINAL_ID} must be available.</li>
     * </ul>
     * <p>
     * Identifiables that cannot be changed keep their original id and are returned.
     * <p>
     * Notes on the implementation:
     * <ul>
     *   <li>The identifiables are collected into a list first: {@code setId} mutates the
     *       network index, and {@code getIdentifiables()} is a live view of it.</li>
     *   <li>{@code Network} instances (including subnetworks) are excluded. Changing their ids is out of scope.</li>
     *   <li>{@code setId} must be called before {@code addAlias}: if the object still has
     *       the original id, {@code NetworkIndex.addAlias} would silently ignore the request.</li>
     * </ul>
     *
     * @param network the network whose identifiables are changed in place
     * @return A list of identifiable id-names that could not be changed, with the reason for rejection
     */
    public static List<IdentifierChangeSkipped> replaceIdsWithNames(Network network) {
        Map<String, Long> nameCount = network.getIdentifiables().stream()
                .filter(i -> !(i instanceof Network))
                .map(i -> i.getOptionalName().orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(n -> n, Collectors.counting()));
        List<IdentifierChangeSkipped> skipped = new ArrayList<>();
        for (Identifiable<?> i : network.getIdentifiables().stream().toList()) {
            String name = i.getOptionalName().orElse(null);
            IdentifierChangeSkipped.IdentifierChangeSkippedReason reason;
            reason = IdentifierChangeSkipped.reason(i, name, nameCount, network);
            if (reason != null) {
                skipped.add(new IdentifierChangeSkipped(i.getId(), name, reason));
                continue;
            }
            String originalId = i.getId();
            i.setId(name);
            i.addAlias(originalId, ALIAS_TYPE_ORIGINAL_ID);
        }
        return skipped;
    }
}
