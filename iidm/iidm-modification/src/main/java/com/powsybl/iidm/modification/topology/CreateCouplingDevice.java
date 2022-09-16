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
import java.util.stream.Collectors;

import static com.powsybl.iidm.modification.topology.ModificationReports.*;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.*;

/**
 * Adds a coupling device between two busbar sections. If topology extensions are present, then it creates open
 * disconnectors to connect the breaker to every parallel busbar section. If there are exactly two busbar sections and
 * that they must have the same sectionIndex, then no open disconnector is created.
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class CreateCouplingDevice extends AbstractNetworkModification {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateCouplingDevice.class);

    private final String bbsId1;

    private final String bbsId2;

    CreateCouplingDevice(String bbsId1, String bbsId2) {
        this.bbsId1 = bbsId1;
        this.bbsId2 = bbsId2;
    }

    protected String getBbsId1() {
        return bbsId1;
    }

    protected String getBbsId2() {
        return bbsId2;
    }

    @Override
    public void apply(Network network, boolean throwException, ComputationManager computationManager, Reporter reporter) {
        BusbarSection bbs1 = network.getBusbarSection(bbsId1);
        if (bbs1 == null) {
            LOGGER.error("Busbar section {} not found.", bbsId1);
            notFoundBusbarSectionReport(reporter, bbsId1);
            if (throwException) {
                throw new PowsyblException(String.format("Busbar section %s not found.", bbsId1));
            }
            return;
        }

        BusbarSection bbs2 = network.getBusbarSection(bbsId2);
        if (bbs1 == null) {
            LOGGER.error("Busbar section {} not found.", bbsId2);
            notFoundBusbarSectionReport(reporter, bbsId2);
            if (throwException) {
                throw new PowsyblException(String.format("Busbar section %s not found.", bbsId2));
            }
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
        int breakerNode1 = voltageLevel1.getNodeBreakerView().getMaximumNodeIndex() + 1;
        int breakerNode2 = breakerNode1 + 1;
        int bbs1Node = bbs1.getTerminal().getNodeBreakerView().getNode();
        int bbs2Node = bbs2.getTerminal().getNodeBreakerView().getNode();

        createNBBreaker(breakerNode1, breakerNode2, "", "NEW", voltageLevel1.getNodeBreakerView(), false);
        createNBDisconnector(bbs1Node, breakerNode1, "1", "NEW", voltageLevel1.getNodeBreakerView(), false);
        createNBDisconnector(bbs2Node, breakerNode2, "2", "NEW", voltageLevel1.getNodeBreakerView(), false);

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
                if (!(bbsList1.size() == 1 && position1.getSectionIndex() == position2.getSectionIndex())) {
                    nbOpenDisconnectors = bbsList1.size() * 2;
                    createTopologyFromBusbarSectionList(voltageLevel1, breakerNode1, "NEW_COUPLING_SIDE1", bbsList1);
                    createTopologyFromBusbarSectionList(voltageLevel2, breakerNode2, "NEW_COUPLING_SIDE2", bbsList2);
                }
            } else {
                LOGGER.warn("No busbar section position extension found on {}, only one disconnector is created.", bbs2.getId());
                noBusbarSectionPositionExtensionReport(reporter, bbs2);
            }
        } else {
            LOGGER.warn("No busbar section position extension found on {}, only one disconnector is created.", bbs1.getId());
            noBusbarSectionPositionExtensionReport(reporter, bbs1);
        }

        LOGGER.info("New coupling device was added to voltage level {} between busbar sectiond {} and {}", voltageLevel1.getId(), bbs1.getId(), bbs2.getId());
        newCouplingDeviceAddedReport(reporter, voltageLevel1.getId(), bbsId1, bbsId2, nbOpenDisconnectors);
    }

}
