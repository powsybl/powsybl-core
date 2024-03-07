/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.AbstractNetworkModification;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BusbarSectionPositionAdder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.*;
import static com.powsybl.iidm.modification.util.ModificationReports.*;

/**
 * Creates symmetrical matrix topology in a given voltage level,
 * containing a given number of busbar with a given number of sections each.<br/>
 * See {@link CreateVoltageLevelTopologyBuilder}.
 *
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
public class CreateVoltageLevelTopology extends AbstractNetworkModification {

    private static final Logger LOG = LoggerFactory.getLogger(CreateVoltageLevelTopology.class);

    private final String voltageLevelId;

    private final int lowBusOrBusbarIndex;
    private final int alignedBusesOrBusbarCount;
    private final int lowSectionIndex;
    private final int sectionCount;

    private final String busOrBusbarSectionPrefixId;
    private final String switchPrefixId;

    private final List<SwitchKind> switchKinds;

    CreateVoltageLevelTopology(String voltageLevelId, int lowBusOrBusbarIndex, Integer alignedBusesOrBusbarCount,
                               int lowSectionIndex, Integer sectionCount,
                               String busOrBusbarSectionPrefixId, String switchPrefixId, List<SwitchKind> switchKinds) {
        this.voltageLevelId = Objects.requireNonNull(voltageLevelId, "Undefined voltage level ID");
        this.lowBusOrBusbarIndex = lowBusOrBusbarIndex;
        this.alignedBusesOrBusbarCount = Objects.requireNonNull(alignedBusesOrBusbarCount, "Undefined aligned buses or busbars count");
        this.lowSectionIndex = lowSectionIndex;
        this.sectionCount = Objects.requireNonNull(sectionCount, "Undefined section count");
        this.busOrBusbarSectionPrefixId = Objects.requireNonNull(busOrBusbarSectionPrefixId, "Undefined busbar section prefix ID");
        this.switchPrefixId = Objects.requireNonNull(switchPrefixId, "Undefined switch prefix ID");
        this.switchKinds = switchKinds;
    }

    private static boolean checkCountAttributes(Integer count, String type, int min, ReportNode reportNode, boolean throwException) {
        if (count < min) {
            LOG.error("{} must be >= {}", type, min);
            countLowerThanMin(reportNode, type, min);
            if (throwException) {
                throw new PowsyblException(type + " must be >= " + min);
            }
            return false;
        }
        return true;
    }

    private boolean checkCountAttributes(int lowBusOrBusbarIndex, int alignedBusesOrBusbarCount, int lowSectionIndex,
                                         int sectionCount, boolean throwException, ReportNode reportNode) {
        return checkCountAttributes(lowBusOrBusbarIndex, "low busbar index", 0, reportNode, throwException) &&
        checkCountAttributes(alignedBusesOrBusbarCount, "busbar count", 1, reportNode, throwException) &&
        checkCountAttributes(lowSectionIndex, "low section index", 0, reportNode, throwException) &&
        checkCountAttributes(sectionCount, "section count", 1, reportNode, throwException);
    }

    private static boolean checkSwitchKinds(List<SwitchKind> switchKinds, int sectionCount, ReportNode reportNode, boolean throwException) {
        Objects.requireNonNull(switchKinds, "Undefined switch kinds");
        if (switchKinds.size() != sectionCount - 1) {
            LOG.error("Unexpected switch kinds count ({}). Should be {}", switchKinds.size(), sectionCount - 1);
            unexpectedSwitchKindsCount(reportNode, switchKinds.size(), sectionCount - 1);
            if (throwException) {
                throw new PowsyblException("Unexpected switch kinds count (" + switchKinds.size() + "). Should be " + (sectionCount - 1));
            }
            return false;
        }
        if (switchKinds.contains(null)) {
            LOG.error("All switch kinds must be defined");
            undefinedSwitchKind(reportNode);
            if (throwException) {
                throw new PowsyblException("All switch kinds must be defined");
            }
            return false;
        }
        if (switchKinds.stream().anyMatch(kind -> kind != SwitchKind.DISCONNECTOR && kind != SwitchKind.BREAKER)) {
            LOG.error("Switch kinds must be DISCONNECTOR or BREAKER");
            wrongSwitchKind(reportNode);
            if (throwException) {
                throw new PowsyblException("Switch kinds must be DISCONNECTOR or BREAKER");
            }
            return false;
        }
        return true;
    }

    public String getVoltageLevelId() {
        return voltageLevelId;
    }

    public int getLowBusOrBusbarIndex() {
        return lowBusOrBusbarIndex;
    }

    public int getAlignedBusesOrBusbarCount() {
        return alignedBusesOrBusbarCount;
    }

    public int getLowSectionIndex() {
        return lowSectionIndex;
    }

    public int getSectionCount() {
        return sectionCount;
    }

    public List<SwitchKind> getSwitchKinds() {
        return Collections.unmodifiableList(switchKinds);
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException, ComputationManager computationManager, ReportNode reportNode) {
        //checks
        if (!checkCountAttributes(lowBusOrBusbarIndex, alignedBusesOrBusbarCount, lowSectionIndex, sectionCount, throwException, reportNode)) {
            return;
        }

        // Get the voltage level
        VoltageLevel voltageLevel = network.getVoltageLevel(voltageLevelId);
        if (voltageLevel == null) {
            LOG.error("Voltage level {} is not found", voltageLevelId);
            notFoundVoltageLevelReport(reportNode, voltageLevelId);
            if (throwException) {
                throw new PowsyblException(String.format("Voltage level %s is not found", voltageLevelId));
            }
            return;
        }
        TopologyKind topologyKind = voltageLevel.getTopologyKind();
        if (topologyKind == TopologyKind.BUS_BREAKER) {
            if (!switchKinds.isEmpty()) {
                LOG.warn("Voltage level {} is BUS_BREAKER. Switchkinds is ignored.", voltageLevelId);
            }
            // Create buses
            createBuses(voltageLevel, namingStrategy);
            // Create switches between buses
            createBusBreakerSwitches(voltageLevel, namingStrategy);
        } else {
            // Check switch kinds
            if (!checkSwitchKinds(switchKinds, sectionCount, reportNode, throwException)) {
                return;
            }
            // Create busbar sections
            createBusbarSections(voltageLevel, namingStrategy);
            // Create switches
            createSwitches(voltageLevel, namingStrategy);
        }
        LOG.info("New symmetrical topology in voltage level {}: creation of {} bus(es) or busbar(s) with {} section(s) each.", voltageLevelId, alignedBusesOrBusbarCount, sectionCount);
        createdNewSymmetricalTopology(reportNode, voltageLevelId, alignedBusesOrBusbarCount, sectionCount);
    }

    private void createBusbarSections(VoltageLevel voltageLevel, NamingStrategy namingStrategy) {
        int node = 0;
        for (int sectionNum = lowSectionIndex; sectionNum < lowSectionIndex + sectionCount; sectionNum++) {
            for (int busbarNum = lowBusOrBusbarIndex; busbarNum < lowBusOrBusbarIndex + alignedBusesOrBusbarCount; busbarNum++) {
                BusbarSection bbs = voltageLevel.getNodeBreakerView().newBusbarSection()
                        .setId(namingStrategy.getBusbarId(busOrBusbarSectionPrefixId, switchKinds, busbarNum, sectionNum))
                        .setNode(node)
                        .add();
                bbs.newExtension(BusbarSectionPositionAdder.class)
                        .withBusbarIndex(busbarNum)
                        .withSectionIndex(sectionNum)
                        .add();
                node++;
            }
        }
    }

    private void createBuses(VoltageLevel voltageLevel, NamingStrategy namingStrategy) {
        for (int sectionNum = lowSectionIndex; sectionNum < lowSectionIndex + sectionCount; sectionNum++) {
            for (int busNum = lowBusOrBusbarIndex; busNum < lowBusOrBusbarIndex + alignedBusesOrBusbarCount; busNum++) {
                voltageLevel.getBusBreakerView().newBus()
                        .setId(namingStrategy.getBusbarId(busOrBusbarSectionPrefixId, busNum, sectionNum))
                        .add();
            }
        }
    }

    private void createBusBreakerSwitches(VoltageLevel voltageLevel, NamingStrategy namingStrategy) {
        for (int sectionNum = lowSectionIndex; sectionNum < lowSectionIndex + sectionCount - 1; sectionNum++) {
            for (int busNum = lowBusOrBusbarIndex; busNum < lowSectionIndex + alignedBusesOrBusbarCount; busNum++) {
                String bus1Id = namingStrategy.getBusbarId(busOrBusbarSectionPrefixId, busNum, sectionNum);
                String bus2Id = namingStrategy.getBusbarId(busOrBusbarSectionPrefixId, busNum, sectionNum + 1);
                createBusBreakerSwitch(bus1Id, bus2Id, namingStrategy.getSwitchId(switchPrefixId, busNum, sectionNum), voltageLevel.getBusBreakerView());
            }
        }
    }

    private void createSwitches(VoltageLevel voltageLevel, NamingStrategy namingStrategy) {
        for (int sectionNum = lowSectionIndex; sectionNum < lowSectionIndex + sectionCount - 1; sectionNum++) {
            SwitchKind switchKind = switchKinds.get(sectionNum - 1);
            for (int busBarNum = lowBusOrBusbarIndex; busBarNum < lowBusOrBusbarIndex + alignedBusesOrBusbarCount; busBarNum++) {
                // Busbarsections on which to connect the disconnectors
                BusbarSection bbs1 = voltageLevel.getNodeBreakerView().getBusbarSection(namingStrategy.getBusbarId(busOrBusbarSectionPrefixId, switchKinds, busBarNum, sectionNum));
                BusbarSection bbs2 = voltageLevel.getNodeBreakerView().getBusbarSection(namingStrategy.getBusbarId(busOrBusbarSectionPrefixId, switchKinds, busBarNum, sectionNum + 1));

                // Nodes
                int node1 = getNode(bbs1.getId(), voltageLevel);
                int node2 = getNode(bbs2.getId(), voltageLevel);

                if (switchKind == SwitchKind.BREAKER) {
                    // New nodes
                    int newNode1 = voltageLevel.getNodeBreakerView().getMaximumNodeIndex() + 1;
                    int newNode2 = newNode1 + 1;

                    // Prefix
                    String chunkingPrefixId = namingStrategy.getChunkPrefix(switchPrefixId, switchKinds, busBarNum, sectionNum, sectionNum + 1);

                    // Breaker and disconnectors creation
                    createNBDisconnector(node1, newNode1, namingStrategy.getDisconnectorBetweenChunksId(bbs1, chunkingPrefixId, node1, newNode1), voltageLevel.getNodeBreakerView(), false);
                    createNBBreaker(newNode1, newNode2, namingStrategy.getBreakerId(chunkingPrefixId, busBarNum, sectionNum), voltageLevel.getNodeBreakerView(), false);
                    createNBDisconnector(newNode2, node2, namingStrategy.getDisconnectorBetweenChunksId(bbs2, chunkingPrefixId, newNode2, node2), voltageLevel.getNodeBreakerView(), false);
                } else if (switchKind == SwitchKind.DISCONNECTOR) {
                    // Prefix
                    String sectioningPrefix = namingStrategy.getSectioningPrefix(switchPrefixId, bbs1, busBarNum, sectionNum, sectionNum + 1);

                    // Disconnector creation
                    createNBDisconnector(node1, node2, namingStrategy.getDisconnectorId(sectioningPrefix, node1, node2), voltageLevel.getNodeBreakerView(), false);
                } // other cases cannot happen (has been checked in the constructor)
            }
        }
    }

    private static int getNode(String busBarSectionId, VoltageLevel voltageLevel) {
        return voltageLevel.getNodeBreakerView().getBusbarSection(busBarSectionId).getTerminal().getNodeBreakerView().getNode();
    }
}
