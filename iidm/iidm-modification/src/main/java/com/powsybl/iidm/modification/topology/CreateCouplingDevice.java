/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
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
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.powsybl.iidm.modification.util.ModificationLogs.busOrBbsDoesNotExist;
import static com.powsybl.iidm.modification.util.ModificationReports.*;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.*;

/**
 * Adds a coupling device between two busbar sections. If topology extensions are present, then it creates open
 * disconnectors to connect the breaker to every parallel busbar section, else does not create them.
 * If there are exactly two busbar sections and
 * that they must have the same sectionIndex, then no open disconnector is created.
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public class CreateCouplingDevice extends AbstractNetworkModification {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateCouplingDevice.class);
    private static final String NETWORK_MODIFICATION_NAME = "CreateCouplingDevice";

    private final String busOrBbsId1;

    private final String busOrBbsId2;

    private String switchPrefixId;

    CreateCouplingDevice(String busOrBbsId1, String busOrBbsId2, String switchPrefixId) {
        this.busOrBbsId1 = Objects.requireNonNull(busOrBbsId1, "Busbar section 1 not defined");
        this.busOrBbsId2 = Objects.requireNonNull(busOrBbsId2, "Busbar section 2 not defined");
        this.switchPrefixId = switchPrefixId;
    }

    public String getBusOrBbsId1() {
        return busOrBbsId1;
    }

    /**
     * @deprecated Use {@link #getBusOrBbsId1()} instead.
     */
    @Deprecated(since = "5.2.0")
    public String getBbsId1() {
        return getBusOrBbsId1();
    }

    public String getBusOrBbsId2() {
        return busOrBbsId2;
    }

    /**
     * @deprecated Use {@link #getBusOrBbsId2()} instead.
     */
    @Deprecated(since = "5.2.0")
    public String getBbsId2() {
        return getBusOrBbsId2();
    }

    public Optional<String> getSwitchPrefixId() {
        return Optional.ofNullable(switchPrefixId);
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException, ComputationManager computationManager, ReportNode reportNode) {
        Identifiable<?> busOrBbs1 = network.getIdentifiable(busOrBbsId1);
        Identifiable<?> busOrBbs2 = network.getIdentifiable(busOrBbsId2);
        if (failBbs(busOrBbs1, busOrBbs2, reportNode, throwException)) {
            return;
        }

        VoltageLevel voltageLevel1 = getVoltageLevel(busOrBbs1, reportNode, throwException);
        VoltageLevel voltageLevel2 = getVoltageLevel(busOrBbs2, reportNode, throwException);
        if (voltageLevel1 == null || voltageLevel2 == null) {
            LOGGER.error("Voltage level associated to {} or {} not found.", busOrBbs1, busOrBbs2);
            notFoundBusOrBusbarSectionVoltageLevelReport(reportNode, busOrBbsId1, busOrBbsId2);
            if (throwException) {
                throw new PowsyblException(String.format("Voltage level associated to %s or %s not found.", busOrBbs1, busOrBbs2));
            }
            return;
        }
        if (voltageLevel1 != voltageLevel2) {
            LOGGER.error("{} and {} are in two different voltage levels.", busOrBbsId1, busOrBbsId2);
            unexpectedDifferentVoltageLevels(reportNode, busOrBbsId1, busOrBbsId2);
            if (throwException) {
                throw new PowsyblException(String.format("%s and %s are in two different voltage levels.", busOrBbsId1, busOrBbsId2));
            }
            return;
        }
        if (busOrBbs1 instanceof Bus && busOrBbs2 instanceof Bus) {
            if (switchPrefixId == null) {
                switchPrefixId = voltageLevel1.getId();
            }
            // buses are identifiable: voltage level is BUS_BREAKER
            createBusBreakerSwitch(busOrBbsId1, busOrBbsId2, namingStrategy.getSwitchId(switchPrefixId), voltageLevel1.getBusBreakerView());
        } else if (busOrBbs1 instanceof BusbarSection bbs1 && busOrBbs2 instanceof BusbarSection bbs2) {
            // busbar sections exist: voltage level is NODE_BREAKER
            applyOnBusbarSections(voltageLevel1, voltageLevel2, bbs1, bbs2, namingStrategy, reportNode);
        }
        LOGGER.info("New coupling device was added to voltage level {} between {} and {}", voltageLevel1.getId(), busOrBbs1, busOrBbs2);
        newCouplingDeviceAddedReport(reportNode, voltageLevel1.getId(), busOrBbsId1, busOrBbsId2);
    }

    @Override
    protected boolean applyDryRun(Network network, NamingStrategy namingStrategy, ComputationManager computationManager, ReportNode reportNode) {
        Identifiable<?> busOrBbs1 = network.getIdentifiable(busOrBbsId1);
        Identifiable<?> busOrBbs2 = network.getIdentifiable(busOrBbsId2);
        if (busOrBbs1 == null) {
            dryRunConclusive = false;
            reportOnInconclusiveDryRun(reportNode,
                NETWORK_MODIFICATION_NAME,
                String.format("Bus or busbar section %s not found", busOrBbsId1));
        }
        if (busOrBbs2 == null) {
            dryRunConclusive = false;
            reportOnInconclusiveDryRun(reportNode,
                NETWORK_MODIFICATION_NAME,
                String.format("Bus or busbar section %s not found", busOrBbsId2));
        } else if (busOrBbs2 == busOrBbs1) {
            dryRunConclusive = false;
            reportOnInconclusiveDryRun(reportNode,
                NETWORK_MODIFICATION_NAME,
                String.format("No coupling device can be created on a same bus or busbar section (%s)", busOrBbsId1));
        }
        dryRunConclusive = checkVoltageLevel(busOrBbs1, reportNode, NETWORK_MODIFICATION_NAME, dryRunConclusive);
        dryRunConclusive = checkVoltageLevel(busOrBbs2, reportNode, NETWORK_MODIFICATION_NAME, dryRunConclusive);
        return dryRunConclusive;
    }

    @Override
    public boolean isLocalDryRunPossible() {
        return true;
    }

    /**
     * Apply the modification on the two specified busbar sections
     */
    private void applyOnBusbarSections(VoltageLevel voltageLevel1, VoltageLevel voltageLevel2, BusbarSection bbs1, BusbarSection bbs2, NamingStrategy namingStrategy, ReportNode reportNode) {
        if (switchPrefixId == null) {
            switchPrefixId = namingStrategy.getSwitchBaseId(voltageLevel1, bbs1, bbs2);
        }
        // busbar sections exist: voltage level is NODE_BREAKER
        int breakerNode1 = voltageLevel1.getNodeBreakerView().getMaximumNodeIndex() + 1;
        int breakerNode2 = breakerNode1 + 1;
        int nbOpenDisconnectors = 0;

        // Breaker
        createNBBreaker(breakerNode1, breakerNode2, namingStrategy.getBreakerId(switchPrefixId), voltageLevel1.getNodeBreakerView(), false);

        // Positions
        BusbarSectionPosition position1 = bbs1.getExtension(BusbarSectionPosition.class);
        BusbarSectionPosition position2 = bbs2.getExtension(BusbarSectionPosition.class);
        boolean bbsOnSameSection = position1 != null && position2 != null && position1.getSectionIndex() == position2.getSectionIndex();

        // If the positions are defined and on the same section, check if the first side is on the first bar or the second side on the last bar.
        // If true, the last bar will not be connected on the first side nor the first bar on the second side.
        // If false, it will be the opposite.
        boolean avoidLastBarOnFirstSide = bbsOnSameSection && checkSides(voltageLevel1, position1, position2);

        // Disconnectors
        if (position1 != null) {

            // List of the bars for the first section and creation of the topology
            List<BusbarSection> bbsList1 = computeBbsListAndCreateTopology(voltageLevel1, bbs1, namingStrategy, bbsOnSameSection, breakerNode1, avoidLastBarOnFirstSide, 1);

            nbOpenDisconnectors += bbsList1.size() - 1;
        } else {
            createDisconnectorTopology(voltageLevel1, breakerNode1, namingStrategy, switchPrefixId, List.of(bbs1), bbs1);
            LOGGER.warn("No busbar section position extension found on {}, only one disconnector is created.", bbs1.getId());
            noBusbarSectionPositionExtensionReport(reportNode, bbs1);
        }
        if (position2 != null) {
            // List of the bars for the second section and creation of the topology
            List<BusbarSection> bbsList2 = computeBbsListAndCreateTopology(voltageLevel2, bbs2, namingStrategy, bbsOnSameSection, breakerNode2, !avoidLastBarOnFirstSide, 2);

            nbOpenDisconnectors += bbsList2.size() - 1;
        } else {
            createDisconnectorTopology(voltageLevel2, breakerNode2, namingStrategy, switchPrefixId, List.of(bbs2), bbs2);
            LOGGER.warn("No busbar section position extension found on {}, only one disconnector is created.", bbs2.getId());
            noBusbarSectionPositionExtensionReport(reportNode, bbs2);
        }

        if (nbOpenDisconnectors > 0) {
            LOGGER.info("{} open disconnectors created on parallel busbar section in voltage level {}", nbOpenDisconnectors, voltageLevel1.getId());
            openDisconnectorsAddedReport(reportNode, voltageLevel1.getId(), nbOpenDisconnectors);
        }
    }

    /**
     * Check if the first side is on the first bar or the second side on the last bar
     * @param voltageLevel Voltage level in which the busbar sections are located
     * @param position1 Position on the first side
     * @param position2 Position on the second side
     * @return True if the first side is on the first bar or the second side is on the last bar, else False
     */
    private boolean checkSides(VoltageLevel voltageLevel, BusbarSectionPosition position1, BusbarSectionPosition position2) {
        // Check if the first side is on the first bar or the second side on the last bar
        OptionalInt minBbsIndex = getParallelBusbarSections(voltageLevel, position1).stream()
            .mapToInt(b -> b.getExtension(BusbarSectionPosition.class).getBusbarIndex()).min();
        OptionalInt maxBbsIndex = getParallelBusbarSections(voltageLevel, position2).stream()
            .mapToInt(b -> b.getExtension(BusbarSectionPosition.class).getBusbarIndex()).max();
        return position1.getBusbarIndex() == minBbsIndex.orElseThrow()
            || position2.getBusbarIndex() == maxBbsIndex.orElseThrow();
    }

    /**
     * Computes the list of the bars on which to connect the coupling device for the current side and creates the
     * disconnectors for the different parallel busbar sections.
     * @param voltageLevel Voltage level in which the busbar sections are located
     * @param bbs Current bar
     * @param namingStrategy Naming strategy used to name the disconnectors created
     * @param bbsOnSameSection True if the two busbar sections are located on the same section
     * @param breakerNode Node on the current site of the breaker
     * @param avoidLastBar If true, the last bar will not be connected, if false the first one will not be connected.
     * @param side Side of the coupling device on which to connect the bars
     * @return List of the busbar sections on which a connection was made
     */
    private List<BusbarSection> computeBbsListAndCreateTopology(VoltageLevel voltageLevel, BusbarSection bbs, NamingStrategy namingStrategy, boolean bbsOnSameSection, int breakerNode, boolean avoidLastBar, int side) {

        // Position of the bar
        BusbarSectionPosition position = bbs.getExtension(BusbarSectionPosition.class);
        // List of the bars parallel to the given position
        List<BusbarSection> bbsList = getParallelBusbarSections(voltageLevel, position);

        // If the two busbarsections are in the same section, avoid the last bar if avoidLastBar is true, else the first one
        if (bbsOnSameSection) {
            if (avoidLastBar) {
                int maxBbsIndex = bbsList.stream().mapToInt(b -> b.getExtension(BusbarSectionPosition.class).getBusbarIndex()).max().orElseThrow();
                bbsList = bbsList.stream().filter(b -> b.getExtension(BusbarSectionPosition.class).getBusbarIndex() != maxBbsIndex).toList();
            } else {
                int minBbsIndex = bbsList.stream().mapToInt(b -> b.getExtension(BusbarSectionPosition.class).getBusbarIndex()).min().orElseThrow();
                bbsList = bbsList.stream().filter(b -> b.getExtension(BusbarSectionPosition.class).getBusbarIndex() != minBbsIndex).toList();
            }
        }

        // Disconnectors
        createDisconnectorTopology(voltageLevel, breakerNode, namingStrategy, switchPrefixId, bbsList, bbs, bbsOnSameSection ? side : 0);
        return bbsList;
    }

    private boolean failBbs(Identifiable<?> bbs1, Identifiable<?> bbs2, ReportNode reportNode, boolean throwException) {
        if (bbs1 == null) {
            busOrBbsDoesNotExist(busOrBbsId1, reportNode, throwException);
            return true;
        }
        if (bbs2 == null) {
            busOrBbsDoesNotExist(busOrBbsId2, reportNode, throwException);
            return true;
        }
        if (bbs1 == bbs2) {
            LOGGER.error("No coupling device can be created on a same busbar section or bus ({})", busOrBbsId1);
            noCouplingDeviceOnSameBusOrBusbarSection(reportNode, busOrBbsId1);
            if (throwException) {
                throw new PowsyblException(String.format("No coupling device can be created on a same bus or busbar section (%s)", busOrBbsId1));
            }
            return true;
        }
        return false;
    }

    private static VoltageLevel getVoltageLevel(Identifiable<?> identifiable, ReportNode reportNode, boolean throwException) {
        if (identifiable instanceof Bus bus) {
            return bus.getVoltageLevel();
        }
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
}
