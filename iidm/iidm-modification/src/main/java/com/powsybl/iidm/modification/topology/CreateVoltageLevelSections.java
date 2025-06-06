/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.AbstractNetworkModification;
import com.powsybl.iidm.modification.NetworkModificationImpact;
import com.powsybl.iidm.modification.util.ModificationReports;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import com.powsybl.iidm.network.extensions.BusbarSectionPositionAdder;
import com.powsybl.math.graph.TraverseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.getParallelBusbarSections;
import static com.powsybl.iidm.modification.util.ModificationLogs.bbsDoesNotExist;
import static com.powsybl.iidm.modification.util.ModificationLogs.logOrThrow;
import static com.powsybl.iidm.modification.util.ModificationReports.unexpectedIdentifiableType;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateVoltageLevelSections.class);

    private final String bbsId;  // reference busbar section

    private final boolean after;   // create the new busbar sections after(true) or before(false) the reference busbar section

    private final boolean busbarOnly;  // create the new busbar sections only on the busbar of the reference busbar section(true) or on all busbars(false)

    private final SwitchKind leftSwitchKind;  // create only a disconnector(SwitchKind.DISCONNECTOR) or a breaker surrounded by 2 disconnectors(SwitchKind.BREAKER), left to the new busbar sections created

    private final SwitchKind rightSwitchKind;  // create only a disconnector(SwitchKind.DISCONNECTOR) or a breaker surrounded by 2 disconnectors(SwitchKind.BREAKER), right to the new busbar sections created

    private final boolean leftSwitchFictitious;  // fictitious(true) or not(false) for the new switches created, left to the new busbar sections created

    private final boolean rightSwitchFictitious; // fictitious(true) or not(false) for the new switches created, right to the new busbar sections created

    CreateVoltageLevelSections(String bbsId, boolean after, boolean busbarOnly, SwitchKind leftSwitchKind, SwitchKind rightSwitchKind, boolean leftSwitchFictitious, boolean rightSwitchFictitious) {
        this.bbsId = Objects.requireNonNull(bbsId, "Busbar section id not defined");
        this.after = after;
        this.busbarOnly = busbarOnly;
        this.leftSwitchKind = leftSwitchKind;
        this.rightSwitchKind = rightSwitchKind;
        this.leftSwitchFictitious = leftSwitchFictitious;
        this.rightSwitchFictitious = rightSwitchFictitious;
    }

    @Override
    public String getName() {
        return "CreateVoltageLevelSections";
    }

    public String getBbsId() {
        return bbsId;
    }

    public boolean isAfter() {
        return after;
    }

    public boolean isBusbarOnly() {
        return busbarOnly;
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
        Identifiable<?> bbs = network.getIdentifiable(getBbsId());
        if (failBbs(bbs, reportNode, throwException)) {
            return;
        }

        VoltageLevel vl = getVoltageLevel(bbs, reportNode, throwException);
        if (vl == null) {
            return;
        }

        BusbarSection referenceBbs = (BusbarSection) bbs;

        // check all busbar sections have the extension BusbarSectionPosition
        boolean allBbsWithExtension = vl.getNodeBreakerView().getBusbarSectionStream().allMatch(busbarSection ->
            Objects.nonNull(busbarSection.getExtension(BusbarSectionPosition.class)));
        if (!allBbsWithExtension) {
            ModificationReports.busbarSectionsWithoutPositionReport(reportNode, vl.getId());
            logOrThrow(throwException, String.format("Some busbar sections have no position in voltage level (%s)", vl.getId()));
            return;
        }

        BusbarSectionPosition referenceBbsPosition = referenceBbs.getExtension(BusbarSectionPosition.class);
        List<BusbarSection> bbsList = new ArrayList<>();
        if (isBusbarOnly()) {
            bbsList.add(referenceBbs);
        } else {
            bbsList = getParallelBusbarSections(vl, referenceBbsPosition);
        }

        int sectionIndexToFind = isAfter()
            ? getMinimalPositionAfter(vl, referenceBbsPosition.getSectionIndex())
            : getMaximalPositionBefore(vl, referenceBbsPosition.getSectionIndex());

        // if no position available before or after referenceBbsPosition.sectionIndex
        // give space by incrementing the section index of all busbar sections after referenceBbs
        // Same when inserting before first busbar section with index=1, which could lead to diagram pb
        boolean noSpaceToInsertBetween = sectionIndexToFind != -1 && abs(referenceBbsPosition.getSectionIndex() - sectionIndexToFind) == 1;
        boolean ensureNoSectionIndexZeroWhenInsertingBefore = !isAfter() && sectionIndexToFind == -1 && referenceBbsPosition.getSectionIndex() == 1;
        if (noSpaceToInsertBetween || ensureNoSectionIndexZeroWhenInsertingBefore) {
            incrementSectionIndexes(vl, referenceBbsPosition.getSectionIndex());
            if (isAfter()) {
                sectionIndexToFind += 1;
            }
        }

        int finalSectionIndexToFind = sectionIndexToFind;
        bbsList.forEach(busbarSection -> {
            BusbarSectionPosition busbarSectionPosition = busbarSection.getExtension(BusbarSectionPosition.class);

            if (finalSectionIndexToFind == -1) {  // we insert before first section or after last section
                // create new busbar section
                int newBbsNode = vl.getNodeBreakerView().getMaximumNodeIndex() + 1;
                BusbarSection newBbs = createBusbarSection(vl, namingStrategy.getBusbarId(vl.getId(), newBbsNode),
                    newBbsNode,
                    busbarSectionPosition.getBusbarIndex(),
                    isAfter() ? busbarSectionPosition.getSectionIndex() + 1 : busbarSectionPosition.getSectionIndex() - 1);
                // create new switches between busbarSection and newBbs
                createSwitchesBetweenBusbarSections(vl, busbarSection, newBbs, namingStrategy);
            } else {
                // here, we insert a new busbar section and new switches between 2 existing busbar sections
                // we need to remove existing switches between busbarSection and the neighbour busbar section,
                // so we traverse the graph, starting from referenceBbs terminal with a customized Traverser,
                // in order to get these switches and this neighbour busbar section
                BusbarSectionFinderTraverser traverser = new BusbarSectionFinderTraverser(busbarSection.getId(), busbarSectionPosition.getBusbarIndex(), finalSectionIndexToFind);
                busbarSection.getTerminal().traverse(traverser);
                BusbarSection neighbourBbs = traverser.getFoundBbs();
                if (neighbourBbs == null) {
                    ModificationReports.failToInsertBusbarSectionReport(reportNode, vl.getId(), busbarSection.getId());
                    logOrThrow(throwException, String.format("Can't insert a busbar section in voltage level (%s) before or after busbar section (%s)", vl.getId(), busbarSection.getId()));
                    return;
                }
                List<Switch> switchesEncountered = traverser.getSwitchesEncountered();

                // remove the switches encountered
                switchesEncountered.forEach(s -> vl.getNodeBreakerView().removeSwitch(s.getId()));

                // create new busbar section
                int newBbsNode = vl.getNodeBreakerView().getMaximumNodeIndex() + 1;
                BusbarSection newBbs = createBusbarSection(vl, namingStrategy.getBusbarId(vl.getId(), newBbsNode),
                    newBbsNode,
                    busbarSectionPosition.getBusbarIndex(),
                    isAfter() ? busbarSectionPosition.getSectionIndex() + 1 : busbarSectionPosition.getSectionIndex() - 1);

                // create new switches between busbarSection and newBbs
                createSwitchesBetweenBusbarSections(vl, busbarSection, newBbs, namingStrategy);

                // create new switches between newBbs and neighbourBbs
                createSwitchesBetweenBusbarSections(vl, newBbs, neighbourBbs, namingStrategy);
            }
        });
    }

    private static class BusbarSectionFinderTraverser implements Terminal.TopologyTraverser {
        private final String startingBusBarSectionId;
        private final int busbarIndex;
        private final int sectionIndex;
        private BusbarSection foundBbs;
        private final List<Switch> switchesEncountered = new ArrayList<>();

        public BusbarSectionFinderTraverser(String startingBusBarSectionId, int busbarIndex, int sectionIndex) {
            this.startingBusBarSectionId = startingBusBarSectionId;
            this.busbarIndex = busbarIndex;
            this.sectionIndex = sectionIndex;
        }

        @Override
        public TraverseResult traverse(Terminal terminal, boolean connected) {
            if (terminal.getConnectable().getType() == IdentifiableType.BUSBAR_SECTION) {
                BusbarSection bbs = (BusbarSection) terminal.getConnectable();
                if (bbs.getId().equals(startingBusBarSectionId)) {
                    return TraverseResult.CONTINUE;
                }
                BusbarSectionPosition bbsPos = bbs.getExtension(BusbarSectionPosition.class);
                if (bbsPos != null) {
                    if (bbsPos.getBusbarIndex() == busbarIndex &&
                        bbsPos.getSectionIndex() == sectionIndex) {
                        // we found the desired busbar section
                        foundBbs = bbs;
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

        public BusbarSection getFoundBbs() {
            return foundBbs;
        }

        public List<Switch> getSwitchesEncountered() {
            return switchesEncountered;
        }
    }

    @Override
    public NetworkModificationImpact hasImpactOnNetwork(Network network) {
        impact = DEFAULT_IMPACT;
        Identifiable<?> bbs = network.getIdentifiable(getBbsId());
        if (!checkVoltageLevel(bbs)) {
            impact = NetworkModificationImpact.CANNOT_BE_APPLIED;
        }
        return impact;
    }

    private boolean failBbs(Identifiable<?> bbs, ReportNode reportNode, boolean throwException) {
        if (bbs == null) {
            bbsDoesNotExist(getBbsId(), reportNode, throwException);
            return true;
        }
        return false;
    }

    private static VoltageLevel getVoltageLevel(Identifiable<?> identifiable, ReportNode reportNode, boolean throwException) {
        if (identifiable instanceof BusbarSection bbs) {
            return bbs.getTerminal().getVoltageLevel();
        }
        LOGGER.error("Unexpected type of identifiable {}: {}", identifiable.getId(), identifiable.getType());
        unexpectedIdentifiableType(reportNode, identifiable);
        if (throwException) {
            throw new PowsyblException("Unexpected type of identifiable " + identifiable.getId() + ": " + identifiable.getType());
        }
        return null;
    }

    private void createDisconnector(VoltageLevel vl, String id, int node1, int node2, boolean fictitious) {
        vl.getNodeBreakerView().newDisconnector()
            .setId(id)
            .setNode1(node1)
            .setNode2(node2)
            .setOpen(false)
            .setFictitious(fictitious)
            .add();
    }

    private void createBreaker(VoltageLevel vl, String id, int node1, int node2, boolean fictitious) {
        vl.getNodeBreakerView().newBreaker()
            .setId(id)
            .setNode1(node1)
            .setNode2(node2)
            .setOpen(false)
            .setRetained(true)
            .setFictitious(fictitious)
            .add();
    }

    private BusbarSection createBusbarSection(VoltageLevel vl, String id, int node, int busbarIndex, int sectionIndex) {
        BusbarSection bbs = vl.getNodeBreakerView()
            .newBusbarSection()
            .setId(id)
            .setName(Integer.toString(node))
            .setNode(node)
            .add();
        bbs.newExtension(BusbarSectionPositionAdder.class)
            .withBusbarIndex(busbarIndex)
            .withSectionIndex(sectionIndex)
            .add();
        return bbs;
    }

    private int getMinimalPositionAfter(VoltageLevel vl, int referenceSectionIndex) {
        return vl.getNodeBreakerView().getBusbarSectionStream()
            .filter(bbs -> bbs.getExtension(BusbarSectionPosition.class).getSectionIndex() > referenceSectionIndex)
            .min(Comparator.comparing(b -> b.getExtension(BusbarSectionPosition.class).getSectionIndex()))
            .map(b -> b.getExtension(BusbarSectionPosition.class).getSectionIndex())
            .orElse(-1);
    }

    private int getMaximalPositionBefore(VoltageLevel vl, int referenceSectionIndex) {
        return vl.getNodeBreakerView().getBusbarSectionStream()
            .filter(bbs -> bbs.getExtension(BusbarSectionPosition.class).getSectionIndex() < referenceSectionIndex)
            .max(Comparator.comparing(b -> b.getExtension(BusbarSectionPosition.class).getSectionIndex()))
            .map(b -> b.getExtension(BusbarSectionPosition.class).getSectionIndex())
            .orElse(-1);
    }

    private void incrementSectionIndexes(VoltageLevel vl, int referenceSectionIndex) {
        vl.getNodeBreakerView().getBusbarSectionStream().forEach(bbs -> {
            int sIndex = bbs.getExtension(BusbarSectionPosition.class).getSectionIndex();
            int sIndexToCompare = isAfter() ? referenceSectionIndex + 1 : referenceSectionIndex;
            if (sIndex >= sIndexToCompare) {
                bbs.getExtension(BusbarSectionPosition.class).setSectionIndex(sIndex + 1);
            }
        });
    }

    private void createSwitchesBetweenBusbarSections(VoltageLevel vl, BusbarSection bbs1, BusbarSection bbs2, NamingStrategy namingStrategy) {
        // create new switches between bbs1 and bbs2
        boolean fictitious = isAfter() ? isLeftSwitchFictitious() : isRightSwitchFictitious();
        int bbs1Node = bbs1.getTerminal().getNodeBreakerView().getNode();
        int bbs2Node = bbs2.getTerminal().getNodeBreakerView().getNode();
        int firstDisconnectorNode1 = isAfter() ? bbs1Node : bbs2Node;
        int firstDisconnectorNode2 = isAfter() ? bbs2Node : bbs1Node;
        if (getLeftSwitchKind() == SwitchKind.BREAKER || getRightSwitchKind() == SwitchKind.BREAKER) {
            firstDisconnectorNode2 = vl.getNodeBreakerView().getMaximumNodeIndex() + 1;
        }
        //      add first disconnector
        createDisconnector(vl, namingStrategy.getDisconnectorId(vl.getId(), firstDisconnectorNode1, firstDisconnectorNode2), firstDisconnectorNode1, firstDisconnectorNode2, fictitious);
        if (getLeftSwitchKind() == SwitchKind.BREAKER || getRightSwitchKind() == SwitchKind.BREAKER) {
            //      add breaker
            int breakerNode2 = vl.getNodeBreakerView().getMaximumNodeIndex() + 1;
            createBreaker(vl, namingStrategy.getBreakerId(vl.getId(), firstDisconnectorNode2, breakerNode2), firstDisconnectorNode2, breakerNode2, fictitious);
            //      add second disconnector
            int secondDisconnectorNode2 = isAfter() ? bbs2Node : bbs1Node;
            createDisconnector(vl, namingStrategy.getDisconnectorId(vl.getId(), breakerNode2, secondDisconnectorNode2), breakerNode2, secondDisconnectorNode2, fictitious);
        }
    }
}
