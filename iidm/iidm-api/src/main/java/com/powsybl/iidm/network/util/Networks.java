/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.io.table.AbstractTableFormatter;
import com.powsybl.commons.io.table.AsciiTableFormatter;
import com.powsybl.commons.io.table.Column;
import com.powsybl.commons.io.table.HorizontalAlignment;
import com.powsybl.commons.ref.RefObj;
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
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
        network.getTwoWindingsTransformerStream().forEach(TwoWindingsTransformer::applySolvedValues);
        network.getThreeWindingsTransformerStream().forEach(ThreeWindingsTransformer::applySolvedValues);
        network.getShuntCompensatorStream().forEach(ShuntCompensator::applySolvedValues);
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
     * Get all the elements of the <code>network</code> that can be reduced into an equivalent generator. The conditions for a reduction are
     * decided for each voltage level, with the following criteria:
     * <ul>
     *     <li>the voltage level must contain only reducible elements, which are {@link Generator}, {@link Load}, {@link TwoWindingsTransformer} and {@link BusbarSection}</li>
     *     <li>all breakers in the voltage level must be closed (to prevent issue where a part of the voltage level is incorrectly reduced)</li>
     *     <li>a given set of reducible elements must be linked to the voltage level above through a single two-winding transformer. There can be multiple transformer per voltage level</li>
     *     <li>there must be at least one generator per set of reducible elements for the reduction</li>
     * </ul>
     * @param network the network on which we want to get all the sets of reducible elements and their corresponding two winding transformer
     * @return a stream of {@link ReducibleTransformerData}, containing the information necessary for a reduction
     */
    public static Stream<ReducibleTransformerData> getReducibleTransformerDataStream(Network network) {
        return getReducibleTransformerDataStream(network.getVoltageLevelStream());
    }

    /**
     * Get all the elements of the <code>substation</code> that can be reduced into an equivalent generator. The conditions for a reduction are
     * decided for each voltage level, with the following criteria:
     * <ul>
     *     <li>the voltage level must contain only reducible elements, which are {@link Generator}, {@link Load}, {@link TwoWindingsTransformer} and {@link BusbarSection}</li>
     *     <li>all breakers in the voltage level must be closed (to prevent issue where a part of the voltage level is incorrectly reduced)</li>
     *     <li>a given set of reducible elements must be linked to the voltage level above through a single two-winding transformer. There can be multiple transformer per voltage level</li>
     *     <li>there must be at least one generator per set of reducible elements for the reduction</li>
     * </ul>
     * @param substation the substation on which we want to get all the sets of reducible elements and their corresponding two winding transformer
     * @return a stream of {@link ReducibleTransformerData}, containing the information necessary for a reduction
     */
    public static Stream<ReducibleTransformerData> getReducibleTransformerDataStream(Substation substation) {
        return getReducibleTransformerDataStream(substation.getVoltageLevelStream());
    }

    /**
     * Filter the voltage levels to only keep the ones that are valid, according to the description of {@link #getReducibleTransformerDataStream(Substation)} and {@link #getReducibleTransformerDataStream(Network)}
     * Then use a traverser to ensure each set of element contains at least one generator and corresponds to a single transformer
     * @param voltageLevelStream the voltage levels on which to perform the filtering and traversing
     * @return a stream of {@link ReducibleTransformerData} containing the information used for the reduction, one for each set of elements
     */
    private static Stream<ReducibleTransformerData> getReducibleTransformerDataStream(Stream<VoltageLevel> voltageLevelStream) {
        return voltageLevelStream
           .filter(v -> StreamSupport.stream(v.getConnectables().spliterator(), false).allMatch(c -> isReducibleElement(c.getType())))
           .filter(v -> StreamSupport.stream(v.getSwitches().spliterator(), false).noneMatch(s -> s.getKind() == SwitchKind.BREAKER && s.isOpen()))
           .flatMap(v -> {
               RefObj<Boolean> isValid = new RefObj<>(true);
               Stream<ReducibleTransformerData> dataStream = traverseVlTopology(isValid, v);
               return Boolean.TRUE.equals(isValid.get()) ? dataStream : null;
           })
            .filter(Objects::nonNull);
    }

    /**
     * Traverse the topology starting from each two winding transformer, to check if all the sets of reducible elements are valid (ie
     * only one two-winding transformer per set of reducible element, at least one generator per set of reducible element).
     * @param isValid used to signify to the calling process if the voltage level is valid for reduction with regard to topology
     * @param vl the voltage level on which to perform the checks
     * @return a stream of {@link ReducibleTransformerData} containing the information used for the reduction, one for each set of elements
     */
    private static Stream<ReducibleTransformerData> traverseVlTopology(RefObj<Boolean> isValid, VoltageLevel vl) {
        return vl.getTwoWindingsTransformerStream()
            .map(t -> {
                //check before starting traversal if another part of that voltage level is already not reducible (in which case the entire vl is not reducible)
                if (Boolean.FALSE.equals(isValid.get())) {
                    return null;
                }
                TwoSides vlSide = getTransformerVoltageLevelSide(t, vl);
                List<Generator> toBeDeletedGenerators = switch (vl.getTopologyKind()) {
                    case BUS_BREAKER -> getGeneratorsFromBusBreakerTopology(vl, t.getTerminal(vlSide), t.getId(), isValid);
                    case NODE_BREAKER -> getGeneratorsFromNodeBreakerTopology(vl, t.getTerminal(vlSide), t.getId(), isValid);
                    default -> throw new PowsyblException("Unknown topology kind: " + vl.getTopologyKind());
                };
                if (Boolean.TRUE.equals(isValid.get()) && !toBeDeletedGenerators.isEmpty()) {
                    return new ReducibleTransformerData(t, vlSide, toBeDeletedGenerators);
                } else {
                    //set false for the case where the topology is valid (a single 2WT for the connected buses) but there are no generators
                    isValid.set(false);
                    return null;
                }
            });
    }

    /**
     * @return the list of generators that can be accessed by a traverser starting from the given terminal
     */
    private static List<Generator> getGeneratorsFromBusBreakerTopology(VoltageLevel vl, Terminal startingTerminal, String startingTransformerId, RefObj<Boolean> isValid) {
        List<Generator> toBeDeletedGenerators = new ArrayList<>();
        Bus startingBus = startingTerminal.getBusBreakerView().getBus();
        traverseBus(startingBus, toBeDeletedGenerators, startingTransformerId, isValid);
        vl.getBusBreakerView().traverse(startingBus, (bus1, sw, bus2) -> traverseBus(bus2, toBeDeletedGenerators, startingTransformerId, isValid));
        return toBeDeletedGenerators;
    }

    /**
     * @return the list of generators that can be accessed by a traverser starting from the given terminal
     */
    private static List<Generator> getGeneratorsFromNodeBreakerTopology(VoltageLevel vl, Terminal startingTerminal, String startingTransformerId, RefObj<Boolean> isValid) {
        List<Generator> toBeDeletedGenerators = new ArrayList<>();
        int startingNode = startingTerminal.getNodeBreakerView().getNode();
        traverseNode(vl, null, startingNode, toBeDeletedGenerators, startingTransformerId, isValid);
        vl.getNodeBreakerView().traverse(startingNode, (node1, sw, node2) -> traverseNode(vl, sw, node2, toBeDeletedGenerators, startingTransformerId, isValid));
        return toBeDeletedGenerators;
    }

    /**
     * Handle bus traversal, collecting generators along the way. Stops if the set of elements is detected as invalid for reduction.
     * @param bus the bus whose elements we are currently looking at.
     * @param toBeDeletedGenerators the list of generators that would be removed from the network by a reduction.
     * @param startingTransformerId the ID of the transformer we started the traversal from. Used to identify if another transformer is encountered during the traversal.
     * @param isValid the set of elements is valid for reduction
     * @return an {@link TraverseResult} depending on the elements of the bus
     */
    private static TraverseResult traverseBus(Bus bus, List<Generator> toBeDeletedGenerators, String startingTransformerId, RefObj<Boolean> isValid) {
        if (bus == null) {
            return TraverseResult.TERMINATE_TRAVERSER;
        }
        TraverseResult result = TraverseResult.CONTINUE;
        for (Terminal terminal : bus.getConnectedTerminals()) {
            Connectable<?> connectable = terminal.getConnectable();
            result = switch (connectable.getType()) {
                case IdentifiableType.GENERATOR -> handleGenerator(connectable, toBeDeletedGenerators);
                case IdentifiableType.TWO_WINDINGS_TRANSFORMER -> handleTwoWindingTransformer(connectable, startingTransformerId, isValid);
                default -> TraverseResult.CONTINUE;
            };
            if (result == TraverseResult.TERMINATE_TRAVERSER) {
                return result;
            }
        }
        return result;
    }

    /**
     * Handle node traversal, collecting generators along the way. Stops if the set of elements is detected as invalid for reduction.
     * @param vl the voltage level of this node
     * @param sw the next switch to traverse (if it exists)
     * @param node the node we are currently looking at.
     * @param toBeDeletedGenerators the list of generators that would be removed from the network by a reduction.
     * @param startingTransformerId the ID of the transformer we started the traversal from. Used to identify if another transformer is encountered during the traversal.
     * @param isValid the set of elements is valid for reduction
     * @return an {@link TraverseResult} depending on the elements of the bus
     */
    private static TraverseResult traverseNode(VoltageLevel vl, Switch sw, int node, List<Generator> toBeDeletedGenerators, String startingTransformerId, RefObj<Boolean> isValid) {
        if (node < 0) {
            return TraverseResult.TERMINATE_TRAVERSER;
        }
        if (sw != null && sw.isOpen()) {
            //the filter already made sure there are no open breaker, that means this is an open disconnector, do not go that way
            return TraverseResult.TERMINATE_PATH;
        }
        Connectable<?> connectable = vl.getNodeBreakerView().getTerminal(node).getConnectable();
        return switch (connectable.getType()) {
            case IdentifiableType.GENERATOR -> handleGenerator(connectable, toBeDeletedGenerators);
            case IdentifiableType.TWO_WINDINGS_TRANSFORMER -> handleTwoWindingTransformer(connectable, startingTransformerId, isValid);
            default -> TraverseResult.CONTINUE;
        };
    }

    private static TraverseResult handleGenerator(Connectable<?> connectable, List<Generator> toBeDeletedGenerators) {
        toBeDeletedGenerators.add((Generator) connectable);
        return TraverseResult.CONTINUE;
    }

    private static TraverseResult handleTwoWindingTransformer(Connectable<?> connectable, String startingTransformerId, RefObj<Boolean> isValid) {
        if (!connectable.getId().equals(startingTransformerId)) {
            isValid.set(false);
            return TraverseResult.TERMINATE_TRAVERSER;
        } else {
            return TraverseResult.CONTINUE;
        }
    }

    /**
     * The voltage elements on the <code>reducibleSide</code> of the <code>transformer</code> and that transformer can be
     * reduced into a single generator equivalent to the active / reactive power on the terminal of the side opposite to <code>reducibleSide</code>.
     * If this reduction is performed, the <code>toBeReplacedGenerators</code> will be removed.
     * @param transformer
     * @param reducibleSide
     */
    public record ReducibleTransformerData(TwoWindingsTransformer transformer, TwoSides reducibleSide, List<Generator> toBeReplacedGenerators) { }

    /**
     * Which elements are acceptable in a voltage level for reduction.
     */
    private static boolean isReducibleElement(IdentifiableType type) {
        return type == IdentifiableType.TWO_WINDINGS_TRANSFORMER
            || type == IdentifiableType.GENERATOR
            || type == IdentifiableType.LOAD
            || type == IdentifiableType.BUSBAR_SECTION;
    }

    /**
     * Get the side of that transformer which is on the side of the given voltage level.
     */
    private static TwoSides getTransformerVoltageLevelSide(TwoWindingsTransformer transformer, VoltageLevel voltageLevel) {
        return transformer.getTerminal(TwoSides.ONE).getVoltageLevel().getId().equals(voltageLevel.getId()) ? TwoSides.ONE : TwoSides.TWO;
    }
}
