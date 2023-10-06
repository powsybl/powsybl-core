/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.AbstractNetworkModification;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static com.powsybl.iidm.modification.util.ModificationLogs.busOrBbsDoesNotExist;
import static com.powsybl.iidm.modification.util.ModificationReports.*;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.*;

/**
 * Adds a coupling device between two busbar sections. If topology extensions are present, then it creates open
 * disconnectors to connect the breaker to every parallel busbar section, else does not create them.
 * If there are exactly two busbar sections and
 * that they must have the same sectionIndex, then no open disconnector is created.
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class CreateCouplingDevice extends AbstractNetworkModification {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateCouplingDevice.class);

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
    public void apply(Network network, boolean throwException, ComputationManager computationManager, Reporter reporter) {
        Identifiable<?> busOrBbs1 = network.getIdentifiable(busOrBbsId1);
        Identifiable<?> busOrBbs2 = network.getIdentifiable(busOrBbsId2);
        if (failBbs(busOrBbs1, busOrBbs2, reporter, throwException)) {
            return;
        }

        VoltageLevel voltageLevel1 = getVoltageLevel(busOrBbs1, reporter, throwException);
        VoltageLevel voltageLevel2 = getVoltageLevel(busOrBbs2, reporter, throwException);
        if (voltageLevel1 == null || voltageLevel2 == null) {
            LOGGER.error("Voltage level associated to {} or {} not found.", busOrBbs1, busOrBbs2);
            notFoundBusOrBusbarSectionVoltageLevelReport(reporter, busOrBbsId1, busOrBbsId2);
            if (throwException) {
                throw new PowsyblException(String.format("Voltage level associated to %s or %s not found.", busOrBbs1, busOrBbs2));
            }
            return;
        }
        if (voltageLevel1 != voltageLevel2) {
            LOGGER.error("{} and {} are in two different voltage levels.", busOrBbsId1, busOrBbsId2);
            unexpectedDifferentVoltageLevels(reporter, busOrBbsId1, busOrBbsId2);
            if (throwException) {
                throw new PowsyblException(String.format("%s and %s are in two different voltage levels.", busOrBbsId1, busOrBbsId2));
            }
            return;
        }
        if (switchPrefixId == null) {
            switchPrefixId = voltageLevel1.getId();
        }
        if (busOrBbs1 instanceof Bus && busOrBbs2 instanceof Bus) {
            // buses are identifiable: voltage level is BUS_BREAKER
            createBusBreakerSwitch(busOrBbsId1, busOrBbsId2, switchPrefixId, "", voltageLevel1.getBusBreakerView());
        } else if (busOrBbs1 instanceof BusbarSection bbs1 && busOrBbs2 instanceof BusbarSection bbs2) {
            // busbar sections exist: voltage level is NODE_BREAKER
            applyOnBusbarSections(voltageLevel1, voltageLevel2, bbs1, bbs2, reporter);
        }
        LOGGER.info("New coupling device was added to voltage level {} between {} and {}", voltageLevel1.getId(), busOrBbs1, busOrBbs2);
        newCouplingDeviceAddedReport(reporter, voltageLevel1.getId(), busOrBbsId1, busOrBbsId2);
    }

    /**
     * Apply the modification on the two specified busbar sections
     */
    private void applyOnBusbarSections(VoltageLevel voltageLevel1, VoltageLevel voltageLevel2, BusbarSection bbs1, BusbarSection bbs2, Reporter reporter) {
        // busbar sections exist: voltage level is NODE_BREAKER
        int breakerNode1 = voltageLevel1.getNodeBreakerView().getMaximumNodeIndex() + 1;
        int breakerNode2 = breakerNode1 + 1;
        int nbOpenDisconnectors = 0;

        // Breaker
        createNBBreaker(breakerNode1, breakerNode2, "", switchPrefixId, voltageLevel1.getNodeBreakerView(), false);

        // Disconnectors
        BusbarSectionPosition position1 = bbs1.getExtension(BusbarSectionPosition.class);
        BusbarSectionPosition position2 = bbs2.getExtension(BusbarSectionPosition.class);
        boolean bbsOnSameSection = position1 != null && position2 != null && position1.getSectionIndex() == position2.getSectionIndex();
        boolean invertSides = false;
        if (position1 != null) {
            // Check if the first side is on the first bar or not and invert the logic if it is not the case
            invertSides = bbsOnSameSection && checkSides(voltageLevel1, position1, position2);

            // List of the bars for the first section and creation of the topology
            List<BusbarSection> bbsList1 = computeBbsListAndCreateTopology(voltageLevel1, position1, bbs1, bbsOnSameSection, breakerNode1, !invertSides);

            nbOpenDisconnectors += bbsList1.size() - 1;
        } else {
            createDisconnectorTopologyFromBusbarSectionList(voltageLevel1, breakerNode1, switchPrefixId, Collections.singletonList(bbs1), bbs1);
            LOGGER.warn("No busbar section position extension found on {}, only one disconnector is created.", bbs1.getId());
            noBusbarSectionPositionExtensionReport(reporter, bbs1);
        }
        if (position2 != null) {
            // List of the bars for the second section and creation of the topology
            List<BusbarSection> bbsList2 = computeBbsListAndCreateTopology(voltageLevel2, position2, bbs2, bbsOnSameSection, breakerNode2, invertSides);

            nbOpenDisconnectors += bbsList2.size() - 1;
        } else {
            createDisconnectorTopologyFromBusbarSectionList(voltageLevel2, breakerNode2, switchPrefixId, Collections.singletonList(bbs2), bbs2);
            LOGGER.warn("No busbar section position extension found on {}, only one disconnector is created.", bbs2.getId());
            noBusbarSectionPositionExtensionReport(reporter, bbs2);
        }

        if (nbOpenDisconnectors > 0) {
            LOGGER.info("{} open disconnectors created on parallel busbar section in voltage level {}", nbOpenDisconnectors, voltageLevel1.getId());
            openDisconnectorsAddedReport(reporter, voltageLevel1.getId(), nbOpenDisconnectors);
        }
    }

    /**
     * Check if the first side is on the first bar and the second side on the last bar
     * @param voltageLevel Voltage level in which the busbar sections are located
     * @param position1 Position on the first side
     * @param position2 Position on the second side
     * @return True if the first side is not on the first bar and the second side is not on the last bar, else False
     */
    private boolean checkSides(VoltageLevel voltageLevel, BusbarSectionPosition position1, BusbarSectionPosition position2) {
        // Check if the first side is on the first bar or the second side on the last bar
        OptionalInt minBbsIndex = voltageLevel.getNodeBreakerView().getBusbarSectionStream()
            .filter(b -> b.getExtension(BusbarSectionPosition.class) != null)
            .mapToInt(b -> b.getExtension(BusbarSectionPosition.class).getBusbarIndex()).min();
        OptionalInt maxBbsIndex = voltageLevel.getNodeBreakerView().getBusbarSectionStream()
            .filter(b -> b.getExtension(BusbarSectionPosition.class) != null)
            .mapToInt(b -> b.getExtension(BusbarSectionPosition.class).getBusbarIndex()).max();
        return position1.getBusbarIndex() != minBbsIndex.orElse(Integer.MIN_VALUE)
            && position2.getBusbarIndex() != maxBbsIndex.orElse(Integer.MAX_VALUE);
    }

    /**
     * Computes the list of the bars on which to connect the coupling device for the current side and creates the
     * disconnectors for the different parallel busbar sections.
     * @param voltageLevel Voltage level in which the busbar sections are located
     * @param position Position of the current bar
     * @param bbs Current bar
     * @param bbsOnSameSection True if the two busbar sections are located on the same section
     * @param breakerNode Node on the current site of the breaker
     * @param avoidFirstBar If true, the first bar should not be connected
     * @return List of the busbar sections on which a connection was made
     */
    private List<BusbarSection> computeBbsListAndCreateTopology(VoltageLevel voltageLevel, BusbarSectionPosition position, BusbarSection bbs, boolean bbsOnSameSection, int breakerNode, boolean avoidFirstBar) {

        // List of the bars for the second section
        List<BusbarSection> bbsList = voltageLevel.getNodeBreakerView().getBusbarSectionStream()
            .filter(b -> b.getExtension(BusbarSectionPosition.class) != null)
            .filter(b -> b.getExtension(BusbarSectionPosition.class).getSectionIndex() == position.getSectionIndex()).collect(Collectors.toList());

        // If the two busbarsections are in the same section, filter the second one if there are two busbarsections in the list
        if (bbsOnSameSection) {
            if (avoidFirstBar) {
                OptionalInt maxBbsIndex = bbsList.stream()
                    .mapToInt(b -> b.getExtension(BusbarSectionPosition.class).getBusbarIndex()).max();
                bbsList = bbsList.stream().filter(b -> b.getExtension(BusbarSectionPosition.class).getBusbarIndex() != maxBbsIndex.getAsInt()).collect(Collectors.toList());
            } else {
                OptionalInt minBbsIndex = bbsList.stream()
                    .mapToInt(b -> b.getExtension(BusbarSectionPosition.class).getBusbarIndex()).min();
                bbsList = bbsList.stream().filter(b -> b.getExtension(BusbarSectionPosition.class).getBusbarIndex() != minBbsIndex.getAsInt()).collect(Collectors.toList());
            }
        }

        // Disconnectors on side 2
        createDisconnectorTopologyFromBusbarSectionList(voltageLevel, breakerNode, switchPrefixId, bbsList, bbs);
        return bbsList;
    }

    private boolean failBbs(Identifiable<?> bbs1, Identifiable<?> bbs2, Reporter reporter, boolean throwException) {
        if (bbs1 == null) {
            busOrBbsDoesNotExist(busOrBbsId1, reporter, throwException);
            return true;
        }
        if (bbs2 == null) {
            busOrBbsDoesNotExist(busOrBbsId2, reporter, throwException);
            return true;
        }
        if (bbs1 == bbs2) {
            LOGGER.error("No coupling device can be created on a same busbar section or bus ({})", busOrBbsId1);
            noCouplingDeviceOnSameBusOrBusbarSection(reporter, busOrBbsId1);
            if (throwException) {
                throw new PowsyblException(String.format("No coupling device can be created on a same bus or busbar section (%s)", busOrBbsId1));
            }
            return true;
        }
        return false;
    }

    private static VoltageLevel getVoltageLevel(Identifiable<?> identifiable, Reporter reporter, boolean throwException) {
        if (identifiable instanceof Bus bus) {
            return bus.getVoltageLevel();
        }
        if (identifiable instanceof BusbarSection bbs) {
            return bbs.getTerminal().getVoltageLevel();
        }
        LOGGER.error("Unexpected type of identifiable {}: {}", identifiable.getId(), identifiable.getType());
        unexpectedIdentifiableType(reporter, identifiable);
        if (throwException) {
            throw new PowsyblException("Unexpected type of identifiable " + identifiable.getId() + ": " + identifiable.getType());
        }
        return null;
    }
}
