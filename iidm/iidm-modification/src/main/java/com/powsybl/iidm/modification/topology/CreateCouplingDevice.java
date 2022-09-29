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
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.powsybl.iidm.modification.topology.ModificationReports.*;
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

    private final String bbsId1;

    private final String bbsId2;

    private String switchPrefixId;

    CreateCouplingDevice(String bbsId1, String bbsId2, String switchPrefixId) {
        this.bbsId1 = Objects.requireNonNull(bbsId1, "Busbar section 1 not defined");
        this.bbsId2 = Objects.requireNonNull(bbsId2, "Busbar section 2 not defined");
        this.switchPrefixId = switchPrefixId;
    }

    public String getBbsId1() {
        return bbsId1;
    }

    public String getBbsId2() {
        return bbsId2;
    }

    public Optional<String> getSwitchPrefixId() {
        return Optional.ofNullable(switchPrefixId);
    }

    @Override
    public void apply(Network network, boolean throwException, ComputationManager computationManager, Reporter reporter) {
        BusbarSection bbs1 = network.getBusbarSection(bbsId1);
        BusbarSection bbs2 = network.getBusbarSection(bbsId2);
        if (failBbs(bbs1, bbs2, reporter, throwException)) {
            return;
        }

        VoltageLevel voltageLevel1 = bbs1.getTerminal().getVoltageLevel();
        VoltageLevel voltageLevel2 = bbs2.getTerminal().getVoltageLevel();
        if (voltageLevel1 != voltageLevel2) {
            LOGGER.error("Busbar sections {} and {} are in two different voltage levels.", bbsId1, bbsId2);
            busbarsInDifferentVoltageLevels(reporter, bbsId1, bbsId2);
            if (throwException) {
                throw new PowsyblException(String.format("Busbar sections %s and %s are in two different voltage levels.", bbsId1, bbsId2));
            }
            return;
        }
        if (switchPrefixId == null) {
            switchPrefixId = voltageLevel1.getId();
        }
        int breakerNode1 = voltageLevel1.getNodeBreakerView().getMaximumNodeIndex() + 1;
        int breakerNode2 = breakerNode1 + 1;
        int bbs1Node = bbs1.getTerminal().getNodeBreakerView().getNode();
        int bbs2Node = bbs2.getTerminal().getNodeBreakerView().getNode();

        createNBBreaker(breakerNode1, breakerNode2, "", switchPrefixId, voltageLevel1.getNodeBreakerView(), false);
        createNBDisconnector(bbs1Node, breakerNode1, "_" + bbs1Node, switchPrefixId, voltageLevel1.getNodeBreakerView(), false);
        createNBDisconnector(bbs2Node, breakerNode2, "_" + bbs2Node, switchPrefixId, voltageLevel1.getNodeBreakerView(), false);

        BusbarSectionPosition position1 = bbs1.getExtension(BusbarSectionPosition.class);
        BusbarSectionPosition position2 = bbs2.getExtension(BusbarSectionPosition.class);
        int nbOpenDisconnectors = 0;
        if (position1 != null) {
            if (position2 != null) {
                List<BusbarSection> bbsList1 = voltageLevel1.getNodeBreakerView().getBusbarSectionStream()
                        .filter(b -> b.getExtension(BusbarSectionPosition.class) != null)
                        .filter(b -> b.getExtension(BusbarSectionPosition.class).getSectionIndex() == position1.getSectionIndex())
                        .filter(b -> !b.getId().equals(bbsId1)).collect(Collectors.toList());
                List<BusbarSection> bbsList2 = voltageLevel2.getNodeBreakerView().getBusbarSectionStream()
                        .filter(b -> b.getExtension(BusbarSectionPosition.class) != null)
                        .filter(b -> b.getExtension(BusbarSectionPosition.class).getSectionIndex() == position2.getSectionIndex())
                        .filter(b -> !b.getId().equals(bbsId2)).collect(Collectors.toList());
                if (bbsList1.size() != 1 || position1.getSectionIndex() != position2.getSectionIndex()) { // if both busbar sections not in same section or in same section with other busbar sections
                    nbOpenDisconnectors = bbsList1.size() * 2;
                    createTopologyFromBusbarSectionList(voltageLevel1, breakerNode1, switchPrefixId, bbsList1);
                    createTopologyFromBusbarSectionList(voltageLevel2, breakerNode2, switchPrefixId, bbsList2);
                }
            } else {
                LOGGER.warn("No busbar section position extension found on {}, only one disconnector is created.", bbs2.getId());
                noBusbarSectionPositionExtensionReport(reporter, bbs2);
            }
        } else {
            LOGGER.warn("No busbar section position extension found on {}, only one disconnector is created.", bbs1.getId());
            noBusbarSectionPositionExtensionReport(reporter, bbs1);
        }

        LOGGER.info("New coupling device was added to voltage level {} between busbar sections {} and {}", voltageLevel1.getId(), bbs1.getId(), bbs2.getId());
        newCouplingDeviceAddedReport(reporter, voltageLevel1.getId(), bbsId1, bbsId2, nbOpenDisconnectors);
    }

    private boolean failBbs(BusbarSection bbs1, BusbarSection bbs2, Reporter reporter, boolean throwException) {
        if (bbs1 == null) {
            bbsDoesNotExist(bbsId1, reporter, throwException);
            return true;
        }
        if (bbs2 == null) {
            bbsDoesNotExist(bbsId2, reporter, throwException);
            return true;
        }
        if (bbs1 == bbs2) {
            LOGGER.error("No coupling device can be created on a same busbar section ({})", bbsId1);
            noCouplingDeviceOnSameBusbarSection(reporter, bbsId1);
            if (throwException) {
                throw new PowsyblException(String.format("No coupling device can be created on a same busbar section (%s)", bbsId1));
            }
            return true;
        }
        return false;
    }

    private static void bbsDoesNotExist(String bbsId, Reporter reporter, boolean throwException) {
        LOGGER.error("Busbar section {} not found.", bbsId);
        notFoundBusbarSectionReport(reporter, bbsId);
        if (throwException) {
            throw new PowsyblException(String.format("Busbar section %s not found.", bbsId));
        }
    }

}
