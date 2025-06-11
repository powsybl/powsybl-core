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
 * Create new busbar sections and new switches between existing busbar sections in a voltage level in NODE_BREAKER topology
 * The new busbar sections can be created before the busbar sections at the first position, between
 * busbar sections, or after the busbar sections at the last position
 * All busbar sections in the voltage level must have the extension BusBarSectionPosition,
 * corresponding to the position (busbar index and section index) in the voltage level
 *
 * @author Franck Lecuyer {@literal <franck.lecuyer_externe at rte-france.com>}
 */
public class CreateVoltageLevelSections extends AbstractNetworkModification {

    private final String referenceBusbarSectionId;  // reference busbar section id

    private final boolean createTheBusbarSectionsAfterTheReferenceBusbarSection;   // create the new busbar sections after(true) or before(false) the reference busbar section

    private final boolean allBusbars;  // create the new busbar sections on all busbars(true) or only on the busbar of the reference busbar section(false)

    private final SwitchKind leftSwitchKind;  // create only a disconnector(SwitchKind.DISCONNECTOR) or a breaker surrounded by 2 disconnectors(SwitchKind.BREAKER), left to the new busbar sections created

    private final SwitchKind rightSwitchKind;  // create only a disconnector(SwitchKind.DISCONNECTOR) or a breaker surrounded by 2 disconnectors(SwitchKind.BREAKER), right to the new busbar sections created

    private final boolean leftSwitchFictitious;  // fictitious(true) or not(false) for the new switches created, left to the new busbar sections created

    private final boolean rightSwitchFictitious; // fictitious(true) or not(false) for the new switches created, right to the new busbar sections created

    CreateVoltageLevelSections(String referenceBusbarSectionId,
                               boolean createTheBusbarSectionsAfterTheReferenceBusbarSection,
                               boolean allBusbars,
                               SwitchKind leftSwitchKind,
                               SwitchKind rightSwitchKind,
                               boolean leftSwitchFictitious,
                               boolean rightSwitchFictitious) {
        this.referenceBusbarSectionId = Objects.requireNonNull(referenceBusbarSectionId, "Reference busbar section id not defined");
        this.createTheBusbarSectionsAfterTheReferenceBusbarSection = createTheBusbarSectionsAfterTheReferenceBusbarSection;
        this.allBusbars = allBusbars;
        this.leftSwitchKind = leftSwitchKind;
        this.rightSwitchKind = rightSwitchKind;
        this.leftSwitchFictitious = leftSwitchFictitious;
        this.rightSwitchFictitious = rightSwitchFictitious;
    }

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

    public boolean isAllBusbars() {
        return allBusbars;
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

        VoltageLevel vl = referenceBusbarSection.getTerminal().getVoltageLevel();
        if (vl == null) {
            return;
        }

        // check all busbar sections have the extension BusbarSectionPosition
        boolean allBusbarSectionsWithExtension = vl.getNodeBreakerView().getBusbarSectionStream().allMatch(busbarSection ->
            Objects.nonNull(busbarSection.getExtension(BusbarSectionPosition.class)));
        if (!allBusbarSectionsWithExtension) {
            busbarSectionsWithoutPositionReport(reportNode, vl.getId());
            logOrThrow(throwException, String.format("Some busbar sections have no position in voltage level (%s)", vl.getId()));
            return;
        }

        BusbarSectionPosition referenceBusbarSectionPosition = referenceBusbarSection.getExtension(BusbarSectionPosition.class);
        List<BusbarSection> busbarSectionsList = !isAllBusbars() ? List.of(referenceBusbarSection) : getParallelBusbarSections(vl, referenceBusbarSectionPosition);

        int nextSectionIndex = findNextSectionIndex(vl, referenceBusbarSectionPosition);

        busbarSectionsList.forEach(busbarSection -> {
            BusbarSectionPosition busbarSectionPosition = busbarSection.getExtension(BusbarSectionPosition.class);

            if (nextSectionIndex == -1) {  // we insert before first section or after last section
                // create new busbar section
                BusbarSection newBusbarSection = createBusbarSection(vl, namingStrategy, busbarSectionPosition);

                // create new switches between busbarSection and newBusbarSection
                createSwitchesBetweenBusbarSections(vl, busbarSection, newBusbarSection, namingStrategy);
            } else {
                // here, we insert a new busbar section and new switches between 2 existing busbar sections
                // we need to remove existing switches between busbarSection and the neighbour busbar section,
                // so we traverse the graph, starting from referenceBusbqarSection terminal with a customized Traverser,
                // in order to get these switches and this neighbour busbar section
                BusbarSectionFinderTraverser traverser = new BusbarSectionFinderTraverser(busbarSection.getId(), busbarSectionPosition.getBusbarIndex(), nextSectionIndex);
                busbarSection.getTerminal().traverse(traverser);
                BusbarSection neighbourBusbarSection = traverser.getFoundBusbarSection();
                if (neighbourBusbarSection == null) {
                    failToInsertBusbarSectionReport(reportNode, vl.getId(), busbarSection.getId());
                    logOrThrow(throwException, String.format("Can't insert a busbar section in voltage level (%s) before or after busbar section (%s) : no neighbour busbar section found to do the operation", vl.getId(), busbarSection.getId()));
                    return;
                }
                List<Switch> switchesEncountered = traverser.getSwitchesEncountered();

                // remove the switches encountered
                switchesEncountered.forEach(s -> vl.getNodeBreakerView().removeSwitch(s.getId()));

                // create new busbar section
                BusbarSection newBusbarSection = createBusbarSection(vl, namingStrategy, busbarSectionPosition);

                // create new switches between busbarSection and newBusbarSection
                createSwitchesBetweenBusbarSections(vl, busbarSection, newBusbarSection, namingStrategy);

                // create new switches between newBusbarSection and neighbourBusbarSection
                createSwitchesBetweenBusbarSections(vl, newBusbarSection, neighbourBusbarSection, namingStrategy);
            }
        });
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
                        // we found the desired busbar section
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
        BusbarSection bbs = network.getBusbarSection(getReferenceBusbarSectionId());
        if (!checkVoltageLevel(bbs)) {
            impact = NetworkModificationImpact.CANNOT_BE_APPLIED;
        }
        return impact;
    }

    private BusbarSection createBusbarSection(VoltageLevel vl,
                                              NamingStrategy namingStrategy,
                                              BusbarSectionPosition busbarSectionPosition) {
        int busbarSectionNode = vl.getNodeBreakerView().getMaximumNodeIndex() + 1;
        BusbarSection busbarSection = vl.getNodeBreakerView()
            .newBusbarSection()
            .setId(namingStrategy.getBusbarId(vl.getId(), busbarSectionNode))
            .setName(Integer.toString(busbarSectionNode))
            .setNode(busbarSectionNode)
            .add();
        busbarSection.newExtension(BusbarSectionPositionAdder.class)
            .withBusbarIndex(busbarSectionPosition.getBusbarIndex())
            .withSectionIndex(isCreateTheBusbarSectionsAfterTheReferenceBusbarSection() ? busbarSectionPosition.getSectionIndex() + 1 : busbarSectionPosition.getSectionIndex() - 1)
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

    private void createSwitchesBetweenBusbarSections(VoltageLevel vl, BusbarSection busbarSection1, BusbarSection busbarSection2, NamingStrategy namingStrategy) {
        // create new switches between busbarSection1 and busbarSection2
        boolean fictitious = isCreateTheBusbarSectionsAfterTheReferenceBusbarSection() ? isLeftSwitchFictitious() : isRightSwitchFictitious();
        int busbarSection1Node = busbarSection1.getTerminal().getNodeBreakerView().getNode();
        int busbarSection2Node = busbarSection2.getTerminal().getNodeBreakerView().getNode();
        int firstDisconnectorNode1 = isCreateTheBusbarSectionsAfterTheReferenceBusbarSection() ? busbarSection1Node : busbarSection2Node;
        int firstDisconnectorNode2 = isCreateTheBusbarSectionsAfterTheReferenceBusbarSection() ? busbarSection2Node : busbarSection1Node;
        if (getLeftSwitchKind() == SwitchKind.BREAKER || getRightSwitchKind() == SwitchKind.BREAKER) {
            firstDisconnectorNode2 = vl.getNodeBreakerView().getMaximumNodeIndex() + 1;
        }

        //      add first disconnector
        createNBDisconnector(firstDisconnectorNode1, firstDisconnectorNode2, namingStrategy.getDisconnectorBetweenChunksId(busbarSection1, vl.getId(), firstDisconnectorNode1, firstDisconnectorNode2), vl.getNodeBreakerView(), false, fictitious);

        if (getLeftSwitchKind() == SwitchKind.BREAKER || getRightSwitchKind() == SwitchKind.BREAKER) {
            //      add breaker
            int breakerNode2 = vl.getNodeBreakerView().getMaximumNodeIndex() + 1;
            createNBBreaker(firstDisconnectorNode2, breakerNode2, namingStrategy.getBreakerId(vl.getId(), firstDisconnectorNode2, breakerNode2), vl.getNodeBreakerView(), false, fictitious);

            //      add second disconnector
            int secondDisconnectorNode2 = isCreateTheBusbarSectionsAfterTheReferenceBusbarSection() ? busbarSection2Node : busbarSection1Node;
            createNBDisconnector(breakerNode2, secondDisconnectorNode2, namingStrategy.getDisconnectorBetweenChunksId(busbarSection2, vl.getId(), breakerNode2, secondDisconnectorNode2), vl.getNodeBreakerView(), false, fictitious);
        }
    }
}
