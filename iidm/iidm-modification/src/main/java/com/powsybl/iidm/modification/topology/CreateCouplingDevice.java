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

import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException, ComputationManager computationManager, Reporter reporter) {
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
        if (busOrBbs1 instanceof Bus && busOrBbs2 instanceof Bus) {
            if (switchPrefixId == null) {
                switchPrefixId = voltageLevel1.getId();
            }
            // buses are identifiable: voltage level is BUS_BREAKER
            createBusBreakerSwitch(busOrBbsId1, busOrBbsId2, namingStrategy.getSwitchId(switchPrefixId), voltageLevel1.getBusBreakerView());
        } else if (busOrBbs1 instanceof BusbarSection bbs1 && busOrBbs2 instanceof BusbarSection bbs2) {
            applyOnBusbarSections(voltageLevel1, voltageLevel2, bbs1, bbs2, namingStrategy, reporter);
        }
        LOGGER.info("New coupling device was added to voltage level {} between {} and {}", voltageLevel1.getId(), busOrBbs1, busOrBbs2);
        newCouplingDeviceAddedReport(reporter, voltageLevel1.getId(), busOrBbsId1, busOrBbsId2);
    }

    /**
     * Apply the modification on the two specified busbar sections
     */
    private void applyOnBusbarSections(VoltageLevel voltageLevel1, VoltageLevel voltageLevel2, BusbarSection bbs1, BusbarSection bbs2, NamingStrategy namingStrategy, Reporter reporter) {
        if (switchPrefixId == null) {
            switchPrefixId = namingStrategy.getSwitchBaseId(voltageLevel1, bbs1, bbs2);
        }
        // busbar sections exist: voltage level is NODE_BREAKER
        int breakerNode1 = voltageLevel1.getNodeBreakerView().getMaximumNodeIndex() + 1;
        int breakerNode2 = breakerNode1 + 1;
        int nbOpenDisconnectors = 0;

        // Breaker
        createNBBreaker(breakerNode1, breakerNode2, namingStrategy.getBreakerId(switchPrefixId), voltageLevel1.getNodeBreakerView(), false);

        // Disconnectors
        BusbarSectionPosition position1 = bbs1.getExtension(BusbarSectionPosition.class);
        BusbarSectionPosition position2 = bbs2.getExtension(BusbarSectionPosition.class);
        boolean checkAndFilterOtherBbs = position1 != null && position2 != null && position1.getSectionIndex() == position2.getSectionIndex();
        if (position1 != null) {
            // List of the bars for the first section
            List<BusbarSection> bbsList1 = voltageLevel1.getNodeBreakerView().getBusbarSectionStream()
                .filter(b -> b.getExtension(BusbarSectionPosition.class) != null)
                .filter(b -> b.getExtension(BusbarSectionPosition.class).getSectionIndex() == position1.getSectionIndex()).collect(Collectors.toList());

            // If the two busbarsections are in the same section, filter the second one if there are two busbarsections in the list
            if (checkAndFilterOtherBbs && bbsList1.size() == 2) {
                bbsList1 = bbsList1.stream().filter(b -> !b.getId().equals(busOrBbsId2)).collect(Collectors.toList());
            }

            // Disconnectors on side 1
            createDisconnectorTopologyFromBusbarSectionList(voltageLevel1, breakerNode1, namingStrategy, switchPrefixId, bbsList1, bbs1, checkAndFilterOtherBbs, 1);
            nbOpenDisconnectors += bbsList1.size() - 1;
        } else {
            createDisconnectorTopologyFromBusbarSectionList(voltageLevel1, breakerNode1, namingStrategy, switchPrefixId, bbs1);
            LOGGER.warn("No busbar section position extension found on {}, only one disconnector is created.", bbs1.getId());
            noBusbarSectionPositionExtensionReport(reporter, bbs1);
        }
        if (position2 != null) {
            // List of the bars for the second section
            List<BusbarSection> bbsList2 = voltageLevel2.getNodeBreakerView().getBusbarSectionStream()
                .filter(b -> b.getExtension(BusbarSectionPosition.class) != null)
                .filter(b -> b.getExtension(BusbarSectionPosition.class).getSectionIndex() == position2.getSectionIndex()).collect(Collectors.toList());

            // If the two busbarsections are in the same section, filter the second one if there are two busbarsections in the list
            if (checkAndFilterOtherBbs && bbsList2.size() == 2) {
                bbsList2 = bbsList2.stream().filter(b -> !b.getId().equals(busOrBbsId1)).collect(Collectors.toList());
            }

            // Disconnectors on side 2
            createDisconnectorTopologyFromBusbarSectionList(voltageLevel2, breakerNode2, namingStrategy, switchPrefixId, bbsList2, bbs2, checkAndFilterOtherBbs, 2);
            nbOpenDisconnectors += bbsList2.size() - 1;
        } else {
            createDisconnectorTopologyFromBusbarSectionList(voltageLevel2, breakerNode2, namingStrategy, switchPrefixId, bbs2);
            LOGGER.warn("No busbar section position extension found on {}, only one disconnector is created.", bbs2.getId());
            noBusbarSectionPositionExtensionReport(reporter, bbs2);
        }

        if (nbOpenDisconnectors > 0) {
            LOGGER.info("{} open disconnectors created on parallel busbar section in voltage level {}", nbOpenDisconnectors, voltageLevel1.getId());
            openDisconnectorsAddedReport(reporter, voltageLevel1.getId(), nbOpenDisconnectors);
        }
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
