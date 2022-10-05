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

import java.util.*;

import static com.powsybl.iidm.modification.topology.ModificationReports.*;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.createNBBreaker;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.createNBDisconnector;

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

    private final int lowBusbarIndex;
    private final int busbarCount;
    private final int lowSectionIndex;
    private final int sectionCount;

    private final String busbarSectionPrefixId;
    private final String switchPrefixId;

    private final List<SwitchKind> switchKinds;

    CreateVoltageLevelTopology(String voltageLevelId, int lowBusbarIndex, Integer busbarCount,
                               int lowSectionIndex, Integer sectionCount,
                               String busbarSectionPrefixId, String switchPrefixId, List<SwitchKind> switchKinds) {
        this.voltageLevelId = Objects.requireNonNull(voltageLevelId, "Undefined voltage level ID");
        this.lowBusbarIndex = checkCount(lowBusbarIndex, "low busbar index");
        this.busbarCount = checkCount(busbarCount, "busBar count");
        this.lowSectionIndex = checkCount(lowSectionIndex, "low section index");
        this.sectionCount = checkCount(sectionCount, "section count");
        this.busbarSectionPrefixId = Objects.requireNonNull(busbarSectionPrefixId, "Undefined busbar section prefix ID");
        this.switchPrefixId = Objects.requireNonNull(switchPrefixId, "Undefined switch prefix ID");
        this.switchKinds = checkSwitchKinds(switchKinds, sectionCount);
    }

    private static int checkCount(Integer count, String type) {
        Objects.requireNonNull(count, "Undefined " + type);
        if (count < 0) {
            throw new PowsyblException(type + " must be >= 0");
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

    public int getLowBusbarIndex() {
        return lowBusbarIndex;
    }

    public int getBusbarCount() {
        return busbarCount;
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
        // Check voltage level is NODE_BREAKER
        TopologyKind topologyKind = voltageLevel.getTopologyKind();
        if (topologyKind != TopologyKind.NODE_BREAKER) {
            LOG.error("Voltage Level {} has an unsupported topology {}. Should be {}", voltageLevelId, topologyKind, TopologyKind.NODE_BREAKER);
            unsupportedVoltageLevelTopologyKind(reporter, voltageLevelId, TopologyKind.NODE_BREAKER, topologyKind);
            if (throwException) {
                throw new PowsyblException(String.format("Voltage Level %s has an unsupported topology %s. Should be %s",
                        voltageLevelId, topologyKind.name(), TopologyKind.NODE_BREAKER.name()));
            }
            return;
        }
        // Create busbar sections
        createBusBarSections(voltageLevel);
        // Create switches
        createSwitches(voltageLevel);
        LOG.info("New symmetrical topology in voltage level {}: creation of {} busbar(s) with {} section(s) each.", voltageLevelId, busbarCount, sectionCount);
        createdNewSymmetricalTopology(reporter, voltageLevelId, busbarCount, sectionCount);
    }

    private void createBusBarSections(VoltageLevel voltageLevel) {
        int node = 0;
        for (int sectionNum = lowSectionIndex; sectionNum < lowSectionIndex + sectionCount; sectionNum++) {
            for (int busBarNum = lowBusbarIndex; busBarNum < lowBusbarIndex + busbarCount; busBarNum++) {
                BusbarSection bbs = voltageLevel.getNodeBreakerView().newBusbarSection()
                        .setId(busbarSectionPrefixId + SEPARATOR + busBarNum + SEPARATOR + sectionNum)
                        .setNode(node)
                        .add();
                bbs.newExtension(BusbarSectionPositionAdder.class)
                        .withBusbarIndex(busBarNum)
                        .withSectionIndex(sectionNum)
                        .add();
                node++;
            }
        }
    }

    private void createSwitches(VoltageLevel voltageLevel) {
        for (int sectionNum = lowSectionIndex; sectionNum < lowSectionIndex + sectionCount - 1; sectionNum++) {
            SwitchKind switchKind = switchKinds.get(sectionNum - 1);
            for (int busBarNum = lowBusbarIndex; busBarNum < lowBusbarIndex + busbarCount; busBarNum++) {
                if (switchKind == SwitchKind.BREAKER) {
                    int node1 = getNode(busBarNum, sectionNum, busbarSectionPrefixId, voltageLevel);
                    int node2 = voltageLevel.getNodeBreakerView().getMaximumNodeIndex() + 1;
                    int node3 = node2 + 1;
                    int node4 = getNode(busBarNum, sectionNum + 1, busbarSectionPrefixId, voltageLevel);
                    createNBDisconnector(node1, node2, SEPARATOR + busBarNum + SEPARATOR + sectionNum, switchPrefixId, voltageLevel.getNodeBreakerView(), false);
                    createNBBreaker(node2, node3, SEPARATOR + busBarNum + SEPARATOR + sectionNum, switchPrefixId, voltageLevel.getNodeBreakerView(), false);
                    createNBDisconnector(node3, node4, SEPARATOR + busBarNum + SEPARATOR + sectionNum, switchPrefixId, voltageLevel.getNodeBreakerView(), false);
                } else if (switchKind == SwitchKind.DISCONNECTOR) {
                    int node1 = getNode(busBarNum, sectionNum, busbarSectionPrefixId, voltageLevel);
                    int node2 = getNode(busBarNum, sectionNum + 1, busbarSectionPrefixId, voltageLevel);
                    createNBDisconnector(node1, node2, SEPARATOR + busBarNum + SEPARATOR + sectionNum, switchPrefixId, voltageLevel.getNodeBreakerView(), false);
                } // other cases cannot happen (has been checked in the constructor)
            }
        }
    }

    private static int getNode(int busBarNum, int sectionNum, String busBarSectionPrefixId, VoltageLevel voltageLevel) {
        return voltageLevel.getNodeBreakerView().getBusbarSection(busBarSectionPrefixId + SEPARATOR + busBarNum + SEPARATOR + sectionNum).getTerminal().getNodeBreakerView().getNode();
    }
}
