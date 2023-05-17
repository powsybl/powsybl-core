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
import com.powsybl.iidm.network.extensions.BusbarSectionPositionAdder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.powsybl.iidm.modification.topology.ModificationReports.createdNewSymmetricalTopology;
import static com.powsybl.iidm.modification.topology.ModificationReports.notFoundVoltageLevelReport;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.*;

/**
 * Creates symmetrical matrix topology in a given voltage level,
 * containing a given number of busbar with a given number of sections each.
 *
 * See {@link CreateVoltageLevelTopologyBuilder}.
 *
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public class CreateVoltageLevelTopology extends AbstractNetworkModification {

    private static final Logger LOG = LoggerFactory.getLogger(CreateVoltageLevelTopology.class);
    private static final String SEPARATOR = "_";

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
        this.lowBusOrBusbarIndex = checkCount(lowBusOrBusbarIndex, "low busbar index", 0);
        this.alignedBusesOrBusbarCount = checkCount(alignedBusesOrBusbarCount, "busBar count", 1);
        this.lowSectionIndex = checkCount(lowSectionIndex, "low section index", 0);
        this.sectionCount = checkCount(sectionCount, "section count", 1);
        this.busOrBusbarSectionPrefixId = Objects.requireNonNull(busOrBusbarSectionPrefixId, "Undefined busbar section prefix ID");
        this.switchPrefixId = Objects.requireNonNull(switchPrefixId, "Undefined switch prefix ID");
        this.switchKinds = switchKinds;
    }

    private static int checkCount(Integer count, String type, int min) {
        Objects.requireNonNull(count, "Undefined " + type);
        if (count < min) {
            throw new PowsyblException(type + " must be >= " + min);
        }
        return count;
    }

    private static List<SwitchKind> checkSwitchKinds(List<SwitchKind> switchKinds, int sectionCount) {
        Objects.requireNonNull(switchKinds, "Undefined switch kinds");
        if (switchKinds.size() != sectionCount - 1) {
            throw new PowsyblException("Unexpected switch kinds count (" + switchKinds.size() + "). Should be " + (sectionCount - 1));
        }
        if (switchKinds.contains(null)) {
            throw new PowsyblException("All switch kinds must be defined");
        }
        if (switchKinds.stream().anyMatch(kind -> kind != SwitchKind.DISCONNECTOR && kind != SwitchKind.BREAKER)) {
            throw new PowsyblException("Switch kinds must be DISCONNECTOR or BREAKER");
        }
        return new ArrayList<>(switchKinds);
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
    public void apply(Network network, boolean throwException, ComputationManager computationManager, Reporter reporter) {
        // Get the voltage level
        VoltageLevel voltageLevel = network.getVoltageLevel(voltageLevelId);
        if (voltageLevel == null) {
            LOG.error("Voltage level {} is not found", voltageLevelId);
            notFoundVoltageLevelReport(reporter, voltageLevelId);
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
            createBuses(voltageLevel);
            // Create switches between buses
            createBusBreakerSwitches(voltageLevel);
        } else {
            // Check switch kinds
            checkSwitchKinds(switchKinds, sectionCount);
            // Create busbar sections
            createBusbarSections(voltageLevel);
            // Create switches
            createSwitches(voltageLevel);
        }
        LOG.info("New symmetrical topology in voltage level {}: creation of {} bus(es) or busbar(s) with {} section(s) each.", voltageLevelId, alignedBusesOrBusbarCount, sectionCount);
        createdNewSymmetricalTopology(reporter, voltageLevelId, alignedBusesOrBusbarCount, sectionCount);
    }

    private void createBusbarSections(VoltageLevel voltageLevel) {
        int node = 0;
        for (int sectionNum = lowSectionIndex; sectionNum < lowSectionIndex + sectionCount; sectionNum++) {
            for (int busbarNum = lowBusOrBusbarIndex; busbarNum < lowBusOrBusbarIndex + alignedBusesOrBusbarCount; busbarNum++) {
                BusbarSection bbs = voltageLevel.getNodeBreakerView().newBusbarSection()
                        .setId(busOrBusbarSectionPrefixId + SEPARATOR + busbarNum + SEPARATOR + sectionNum)
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

    private void createBuses(VoltageLevel voltageLevel) {
        for (int sectionNum = lowSectionIndex; sectionNum < lowSectionIndex + sectionCount; sectionNum++) {
            for (int busNum = lowBusOrBusbarIndex; busNum < lowBusOrBusbarIndex + alignedBusesOrBusbarCount; busNum++) {
                voltageLevel.getBusBreakerView().newBus()
                        .setId(busOrBusbarSectionPrefixId + SEPARATOR + busNum + SEPARATOR + sectionNum)
                        .add();
            }
        }
    }

    private void createBusBreakerSwitches(VoltageLevel voltageLevel) {
        for (int sectionNum = lowSectionIndex; sectionNum < lowSectionIndex + sectionCount - 1; sectionNum++) {
            for (int busNum = lowBusOrBusbarIndex; busNum < lowSectionIndex + alignedBusesOrBusbarCount; busNum++) {
                String bus1Id = busOrBusbarSectionPrefixId + SEPARATOR + busNum + SEPARATOR + sectionNum;
                String bus2Id = busOrBusbarSectionPrefixId + SEPARATOR + busNum + SEPARATOR + (sectionNum + 1);
                createBusBreakerSwitch(bus1Id, bus2Id, switchPrefixId, SEPARATOR + busNum + SEPARATOR + sectionNum, voltageLevel.getBusBreakerView());
            }
        }
    }

    private void createSwitches(VoltageLevel voltageLevel) {
        for (int sectionNum = lowSectionIndex; sectionNum < lowSectionIndex + sectionCount - 1; sectionNum++) {
            SwitchKind switchKind = switchKinds.get(sectionNum - 1);
            for (int busBarNum = lowBusOrBusbarIndex; busBarNum < lowBusOrBusbarIndex + alignedBusesOrBusbarCount; busBarNum++) {
                if (switchKind == SwitchKind.BREAKER) {
                    int node1 = getNode(busBarNum, sectionNum, busOrBusbarSectionPrefixId, voltageLevel);
                    int node2 = voltageLevel.getNodeBreakerView().getMaximumNodeIndex() + 1;
                    int node3 = node2 + 1;
                    int node4 = getNode(busBarNum, sectionNum + 1, busOrBusbarSectionPrefixId, voltageLevel);
                    createNBDisconnector(node1, node2, SEPARATOR + busBarNum + SEPARATOR + sectionNum, switchPrefixId, voltageLevel.getNodeBreakerView(), false);
                    createNBBreaker(node2, node3, SEPARATOR + busBarNum + SEPARATOR + sectionNum, switchPrefixId, voltageLevel.getNodeBreakerView(), false);
                    createNBDisconnector(node3, node4, SEPARATOR + busBarNum + SEPARATOR + sectionNum, switchPrefixId, voltageLevel.getNodeBreakerView(), false);
                } else if (switchKind == SwitchKind.DISCONNECTOR) {
                    int node1 = getNode(busBarNum, sectionNum, busOrBusbarSectionPrefixId, voltageLevel);
                    int node2 = getNode(busBarNum, sectionNum + 1, busOrBusbarSectionPrefixId, voltageLevel);
                    createNBDisconnector(node1, node2, SEPARATOR + busBarNum + SEPARATOR + sectionNum, switchPrefixId, voltageLevel.getNodeBreakerView(), false);
                } // other cases cannot happen (has been checked in the constructor)
            }
        }
    }

    private static int getNode(int busBarNum, int sectionNum, String busBarSectionPrefixId, VoltageLevel voltageLevel) {
        return voltageLevel.getNodeBreakerView().getBusbarSection(busBarSectionPrefixId + SEPARATOR + busBarNum + SEPARATOR + sectionNum).getTerminal().getNodeBreakerView().getNode();
    }
}
