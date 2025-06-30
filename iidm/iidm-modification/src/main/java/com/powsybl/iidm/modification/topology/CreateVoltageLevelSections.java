/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.AbstractNetworkModification;
import com.powsybl.iidm.modification.NetworkModificationImpact;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import com.powsybl.iidm.network.extensions.BusbarSectionPositionAdder;
import com.powsybl.math.graph.TraverseResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.createNBBreaker;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.createNBDisconnector;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.getParallelBusbarSections;
import static com.powsybl.iidm.modification.util.ModificationLogs.busbarSectionDoesNotExist;
import static com.powsybl.iidm.modification.util.ModificationLogs.logOrThrow;
import static com.powsybl.iidm.modification.util.ModificationReports.busbarSectionsWithoutPositionReport;
import static com.powsybl.iidm.modification.util.ModificationReports.failToInsertBusbarSectionReport;
import static java.lang.Math.abs;

/**
 * Create new busbar sections and new switches between existing busbar sections in a voltage level in NODE_BREAKER topology.
 * <p>
 *     The new busbar sections can be created before the busbar sections at the first position, between
 *     busbar sections, or after the busbar sections at the last position.
 * </p>
 * <p>
 *     All busbar sections in the voltage level must have the extension BusBarSectionPosition,
 *     corresponding to the position (busbar index and section index) in the voltage level.
 * </p>
 * @author Franck Lecuyer {@literal <franck.lecuyer_externe at rte-france.com>}
 */
public class CreateVoltageLevelSections extends AbstractNetworkModification {

    private final String referenceBusbarSectionId;  // Reference busbar section id

    private final boolean createTheBusbarSectionsAfterTheReferenceBusbarSection;   // create the new busbar sections after(true) or before(false) the reference busbar section

    private final boolean createOnAllParallelBusbars;  // Create the new busbar sections on all busbars(true) or only on the busbar of the reference busbar section(false)

    private final SwitchKind leftSwitchKind;  // Create only a disconnector(SwitchKind.DISCONNECTOR) or a breaker surrounded by 2 disconnectors(SwitchKind.BREAKER), left to the new busbar sections created

    private final SwitchKind rightSwitchKind;  // Create only a disconnector(SwitchKind.DISCONNECTOR) or a breaker surrounded by 2 disconnectors(SwitchKind.BREAKER), right to the new busbar sections created

    private final boolean leftSwitchFictitious;  // Fictitious(true) or not(false) for the new switches created, left to the new busbar sections created

    private final boolean rightSwitchFictitious; // Fictitious(true) or not(false) for the new switches created, right to the new busbar sections created

    private final String switchPrefixId;

    private final String busbarSectionPrefixId;

    CreateVoltageLevelSections(String referenceBusbarSectionId,
                               boolean createTheBusbarSectionsAfterTheReferenceBusbarSection,
                               boolean createOnAllParallelBusbars,
                               SwitchParameters leftSwitchParameters,
                               SwitchParameters rightSwitchParameters,
                               String switchPrefixId,
                               String busbarSectionPrefixId) {
        this.referenceBusbarSectionId = Objects.requireNonNull(referenceBusbarSectionId, "Reference busbar section id not defined");
        this.createTheBusbarSectionsAfterTheReferenceBusbarSection = createTheBusbarSectionsAfterTheReferenceBusbarSection;
        this.createOnAllParallelBusbars = createOnAllParallelBusbars;
        this.leftSwitchKind = leftSwitchParameters.switchKind;
        this.rightSwitchKind = rightSwitchParameters.switchKind;
        this.leftSwitchFictitious = leftSwitchParameters.fictitious;
        this.rightSwitchFictitious = rightSwitchParameters.fictitious;
        this.switchPrefixId = Objects.requireNonNull(switchPrefixId, "Undefined switch prefix ID");
        this.busbarSectionPrefixId = Objects.requireNonNull(busbarSectionPrefixId, "Undefined busbar section prefix ID");
    }

    record SwitchParameters(SwitchKind switchKind, boolean fictitious) { }

    @Override
    public String getName() {
        return "CreateVoltageLevelSections";
    }

    public String getReferenceBusbarSectionId() {
        return referenceBusbarSectionId;
    }

    public boolean isCreateTheBusbarSectionsAfterTheReferenceBusbarSection() {
        return createTheBusbarSectionsAfterTheReferenceBusbarSection;
    }

    public boolean isCreateOnAllParallelBusbars() {
        return createOnAllParallelBusbars;
    }

    public SwitchKind getLeftSwitchKind() {
        return leftSwitchKind;
    }

    public SwitchKind getRightSwitchKind() {
        return rightSwitchKind;
    }

    public boolean isLeftSwitchFictitious() {
        return leftSwitchFictitious;
    }

    public boolean isRightSwitchFictitious() {
        return rightSwitchFictitious;
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException, ComputationManager computationManager, ReportNode reportNode) {
        BusbarSection referenceBusbarSection = network.getBusbarSection(getReferenceBusbarSectionId());
        if (referenceBusbarSection == null) {
            busbarSectionDoesNotExist(getReferenceBusbarSectionId(), reportNode, throwException);
            return;
        }

        VoltageLevel voltageLevel = referenceBusbarSection.getTerminal().getVoltageLevel();
        if (voltageLevel == null) {
            return;
        }

        // Check that all busbar sections have the extension BusbarSectionPosition
        boolean allBusbarSectionsWithExtension = voltageLevel.getNodeBreakerView().getBusbarSectionStream().allMatch(busbarSection ->
            Objects.nonNull(busbarSection.getExtension(BusbarSectionPosition.class)));
        if (!allBusbarSectionsWithExtension) {
            busbarSectionsWithoutPositionReport(reportNode, voltageLevel.getId());
            logOrThrow(throwException, String.format("Some busbar sections have no position in voltage level (%s)", voltageLevel.getId()));
            return;
        }

        BusbarSectionPosition referenceBusbarSectionPosition = referenceBusbarSection.getExtension(BusbarSectionPosition.class);
        List<BusbarSection> busbarSections = !isCreateOnAllParallelBusbars() ? List.of(referenceBusbarSection) : getParallelBusbarSections(voltageLevel, referenceBusbarSectionPosition);

        int nextSectionIndex = findNextSectionIndex(voltageLevel, referenceBusbarSectionPosition);
        final SwitchKind switchKind1 = isCreateTheBusbarSectionsAfterTheReferenceBusbarSection() ? getLeftSwitchKind() : getRightSwitchKind();
        final boolean switchFictitious1 = isCreateTheBusbarSectionsAfterTheReferenceBusbarSection() ? isLeftSwitchFictitious() : isRightSwitchFictitious();
        final SwitchKind switchKind2 = isCreateTheBusbarSectionsAfterTheReferenceBusbarSection() ? getRightSwitchKind() : getLeftSwitchKind();
        final boolean switchFictitious2 = isCreateTheBusbarSectionsAfterTheReferenceBusbarSection() ? isRightSwitchFictitious() : isLeftSwitchFictitious();

        busbarSections.forEach(busbarSection ->
            createSection(new CreateSectionParameters(
                    busbarSection,
                    voltageLevel,
                    nextSectionIndex,
                    switchKind1,
                    switchFictitious1,
                    switchKind2,
                    switchFictitious2,
                    namingStrategy,
                    reportNode,
                    throwException))
        );
    }

    private record CreateSectionParameters(BusbarSection busbarSection,
                                           VoltageLevel voltageLevel,
                                           int nextSectionIndex,
                                           SwitchKind switchKind1,
                                           boolean switchFictitious1,
                                           SwitchKind switchKind2,
                                           boolean switchFictitious2,
                                           NamingStrategy namingStrategy,
                                           ReportNode reportNode,
                                           boolean throwException) { }

    private void createSection(CreateSectionParameters createSectionParameters) {
        BusbarSection busbarSection = createSectionParameters.busbarSection;
        VoltageLevel voltageLevel = createSectionParameters.voltageLevel;
        int nextSectionIndex = createSectionParameters.nextSectionIndex;
        SwitchKind switchKind1 = createSectionParameters.switchKind1;
        boolean switchFictitious1 = createSectionParameters.switchFictitious1;
        SwitchKind switchKind2 = createSectionParameters.switchKind2;
        boolean switchFictitious2 = createSectionParameters.switchFictitious2;
        NamingStrategy namingStrategy = createSectionParameters.namingStrategy;
        ReportNode reportNode = createSectionParameters.reportNode;
        boolean throwException = createSectionParameters.throwException;

        BusbarSectionPosition busbarSectionPosition = busbarSection.getExtension(BusbarSectionPosition.class);

        if (nextSectionIndex == -1) {
            // Insert the busbar section before the first section or after the last

            // Create a new busbar section
            BusbarSection newBusbarSection = createBusbarSection(voltageLevel, namingStrategy, busbarSectionPosition);

            // Create new switches between busbarSection and newBusbarSection
            createSwitchesBetweenBusbarSections(voltageLevel, busbarSection, newBusbarSection, namingStrategy, switchKind1, switchFictitious1);
        } else {
            // Insert a new busbar section and new switches between two existing busbar sections.
            // Existing switches between the busbarSection and the neighbor busbar section must be removed.
            // Therefore, the graph is traversed, starting from the referenceBusbarSection terminal with a
            // customized Traverser to get these switches and this neighbor busbar section
            BusbarSectionFinderTraverser traverser = new BusbarSectionFinderTraverser(busbarSection.getId(), busbarSectionPosition.getBusbarIndex(), nextSectionIndex);
            busbarSection.getTerminal().traverse(traverser);
            BusbarSection neighbourBusbarSection = traverser.getFoundBusbarSection();
            if (neighbourBusbarSection == null) {
                failToInsertBusbarSectionReport(reportNode, voltageLevel.getId(), busbarSection.getId());
                String message = String.format("Can't insert a busbar section in voltage level (%s) before or after busbar section (%s) : no neighbour busbar section found to do the operation",
                        voltageLevel.getId(), busbarSection.getId());
                logOrThrow(throwException, message);
                return;
            }
            List<Switch> switchesEncountered = traverser.getSwitchesEncountered();

            // Remove the switches encountered
            switchesEncountered.forEach(s -> voltageLevel.getNodeBreakerView().removeSwitch(s.getId()));

            // Create a new busbar section
            BusbarSection newBusbarSection = createBusbarSection(voltageLevel, namingStrategy, busbarSectionPosition);

            // Create new switches between busbarSection and newBusbarSection
            createSwitchesBetweenBusbarSections(voltageLevel, busbarSection, newBusbarSection, namingStrategy, switchKind1, switchFictitious1);

            // Create new switches between newBusbarSection and neighbourBusbarSection
            createSwitchesBetweenBusbarSections(voltageLevel, newBusbarSection, neighbourBusbarSection, namingStrategy, switchKind2, switchFictitious2);
        }
    }

    private int findNextSectionIndex(VoltageLevel vl, BusbarSectionPosition referenceBusbarSectionPosition) {
        int nextSectionIndex = isCreateTheBusbarSectionsAfterTheReferenceBusbarSection()
            ? getMinimalPositionAfter(vl, referenceBusbarSectionPosition.getSectionIndex())
            : getMaximalPositionBefore(vl, referenceBusbarSectionPosition.getSectionIndex());

        // If no position is available before or after referenceBusbarSectionPosition.sectionIndex,
        // give space by incrementing the section index of all busbar sections after referenceBusbarSection.
        // Same when inserting before the first busbar section with index=1, which could lead to diagram issues
        boolean indexesShouldBeIncremented = nextSectionIndex == -1 ?
            !isCreateTheBusbarSectionsAfterTheReferenceBusbarSection() && referenceBusbarSectionPosition.getSectionIndex() == 1 :
            abs(referenceBusbarSectionPosition.getSectionIndex() - nextSectionIndex) == 1;
        if (indexesShouldBeIncremented) {
            incrementSectionIndexes(vl, referenceBusbarSectionPosition.getSectionIndex());
            if (isCreateTheBusbarSectionsAfterTheReferenceBusbarSection()) {
                nextSectionIndex += 1;
            }
        }
        return nextSectionIndex;
    }

    private static class BusbarSectionFinderTraverser implements Terminal.TopologyTraverser {
        private final String startingBusBarSectionId;
        private final int busbarIndex;
        private final int sectionIndex;
        private BusbarSection foundBusbarSection;
        private final List<Switch> switchesEncountered = new ArrayList<>();

        public BusbarSectionFinderTraverser(String startingBusBarSectionId, int busbarIndex, int sectionIndex) {
            this.startingBusBarSectionId = startingBusBarSectionId;
            this.busbarIndex = busbarIndex;
            this.sectionIndex = sectionIndex;
        }

        @Override
        public TraverseResult traverse(Terminal terminal, boolean connected) {
            if (terminal.getConnectable().getType() == IdentifiableType.BUSBAR_SECTION) {
                BusbarSection busbarSection = (BusbarSection) terminal.getConnectable();
                if (busbarSection.getId().equals(startingBusBarSectionId)) {
                    return TraverseResult.CONTINUE;
                }
                BusbarSectionPosition busbarSectionPosition = busbarSection.getExtension(BusbarSectionPosition.class);
                if (busbarSectionPosition != null) {
                    if (busbarSectionPosition.getBusbarIndex() == busbarIndex &&
                        busbarSectionPosition.getSectionIndex() == sectionIndex) {
                        // We found the desired busbar section
                        foundBusbarSection = busbarSection;
                        return TraverseResult.TERMINATE_TRAVERSER;
                    } else {
                        switchesEncountered.clear();
                        return TraverseResult.TERMINATE_PATH;
                    }
                } else {
                    switchesEncountered.clear();
                    return TraverseResult.TERMINATE_PATH;
                }
            } else {
                switchesEncountered.clear();
                return TraverseResult.TERMINATE_PATH;
            }
        }

        @Override
        public TraverseResult traverse(Switch aSwitch) {
            switchesEncountered.add(aSwitch);
            return TraverseResult.CONTINUE;
        }

        public BusbarSection getFoundBusbarSection() {
            return foundBusbarSection;
        }

        public List<Switch> getSwitchesEncountered() {
            return switchesEncountered;
        }
    }

    @Override
    public NetworkModificationImpact hasImpactOnNetwork(Network network) {
        impact = DEFAULT_IMPACT;
        BusbarSection busbarSection = network.getBusbarSection(getReferenceBusbarSectionId());
        if (!checkVoltageLevel(busbarSection)) {
            impact = NetworkModificationImpact.CANNOT_BE_APPLIED;
        }
        return impact;
    }

    private BusbarSection createBusbarSection(VoltageLevel vl,
                                              NamingStrategy namingStrategy,
                                              BusbarSectionPosition busbarSectionPosition) {
        int busbarSectionNode = vl.getNodeBreakerView().getMaximumNodeIndex() + 1;
        int sectionNum = isCreateTheBusbarSectionsAfterTheReferenceBusbarSection() ? busbarSectionPosition.getSectionIndex() + 1 : busbarSectionPosition.getSectionIndex() - 1;
        int busbarNum = busbarSectionPosition.getBusbarIndex();
        BusbarSection busbarSection = vl.getNodeBreakerView()
            .newBusbarSection()
            .setId(namingStrategy.getBusbarId(busbarSectionPrefixId, busbarNum, sectionNum))
            .setName(Integer.toString(busbarSectionNode))
            .setNode(busbarSectionNode)
            .add();
        busbarSection.newExtension(BusbarSectionPositionAdder.class)
            .withBusbarIndex(busbarNum)
            .withSectionIndex(sectionNum)
            .add();
        return busbarSection;
    }

    private int getMinimalPositionAfter(VoltageLevel vl, int referenceSectionIndex) {
        return vl.getNodeBreakerView().getBusbarSectionStream()
            .filter(busbarSection -> busbarSection.getExtension(BusbarSectionPosition.class).getSectionIndex() > referenceSectionIndex)
            .min(Comparator.comparing(busbarSection -> busbarSection.getExtension(BusbarSectionPosition.class).getSectionIndex()))
            .map(busbarSection -> busbarSection.getExtension(BusbarSectionPosition.class).getSectionIndex())
            .orElse(-1);
    }

    private int getMaximalPositionBefore(VoltageLevel vl, int referenceSectionIndex) {
        return vl.getNodeBreakerView().getBusbarSectionStream()
            .filter(busbarSection -> busbarSection.getExtension(BusbarSectionPosition.class).getSectionIndex() < referenceSectionIndex)
            .max(Comparator.comparing(busbarSection -> busbarSection.getExtension(BusbarSectionPosition.class).getSectionIndex()))
            .map(busbarSection -> busbarSection.getExtension(BusbarSectionPosition.class).getSectionIndex())
            .orElse(-1);
    }

    private void incrementSectionIndexes(VoltageLevel vl, int referenceSectionIndex) {
        vl.getNodeBreakerView().getBusbarSectionStream().forEach(busbarSection -> {
            int sIndex = busbarSection.getExtension(BusbarSectionPosition.class).getSectionIndex();
            int sIndexToCompare = isCreateTheBusbarSectionsAfterTheReferenceBusbarSection() ? referenceSectionIndex + 1 : referenceSectionIndex;
            if (sIndex >= sIndexToCompare) {
                busbarSection.getExtension(BusbarSectionPosition.class).setSectionIndex(sIndex + 1);
            }
        });
    }

    private void createSwitchesBetweenBusbarSections(VoltageLevel vl, BusbarSection busbarSection1, BusbarSection busbarSection2,
                                                     NamingStrategy namingStrategy,
                                                     SwitchKind switchKind,
                                                     boolean fictitious) {
        // Create new switches between busbarSection1 and busbarSection2
        int busbarSection1Node = busbarSection1.getTerminal().getNodeBreakerView().getNode();
        int busbarSection2Node = busbarSection2.getTerminal().getNodeBreakerView().getNode();
        int firstDisconnectorNode2 = switchKind == SwitchKind.BREAKER ? vl.getNodeBreakerView().getMaximumNodeIndex() + 1 : busbarSection2Node;
        int busbarNum = busbarSection1.getExtension(BusbarSectionPosition.class).getBusbarIndex();
        int busbarSection1Num = busbarSection1.getExtension(BusbarSectionPosition.class).getSectionIndex();
        int busbarSection2Num = busbarSection2.getExtension(BusbarSectionPosition.class).getSectionIndex();

        // Prefix
        String chunkingPrefixId = namingStrategy.getChunkPrefix(switchPrefixId, List.of(switchKind), busbarNum, busbarSection1Num, busbarSection2Num);

        // Add the first disconnector
        createNBDisconnector(busbarSection1Node, firstDisconnectorNode2, namingStrategy.getDisconnectorBetweenChunksId(busbarSection1, chunkingPrefixId, busbarSection1Node, firstDisconnectorNode2), vl.getNodeBreakerView(), false, fictitious);

        if (switchKind == SwitchKind.BREAKER) {
            // Add a breaker
            int breakerNode2 = vl.getNodeBreakerView().getMaximumNodeIndex() + 1;
            createNBBreaker(firstDisconnectorNode2, breakerNode2, namingStrategy.getBreakerId(chunkingPrefixId, firstDisconnectorNode2, breakerNode2), vl.getNodeBreakerView(), false, fictitious);

            // Add a second disconnector
            createNBDisconnector(breakerNode2, busbarSection2Node, namingStrategy.getDisconnectorBetweenChunksId(busbarSection2, chunkingPrefixId, breakerNode2, busbarSection2Node), vl.getNodeBreakerView(), false, fictitious);
        }
    }
}
