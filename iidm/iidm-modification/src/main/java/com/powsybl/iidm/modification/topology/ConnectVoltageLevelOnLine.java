/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.report.TypedValue;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.*;
import static com.powsybl.iidm.modification.util.ModificationReports.noBusbarSectionPositionExtensionReport;

/**
 * This method cuts an existing line in two lines that will be created and connected to an existing voltage level
 * (also called switching voltage level). The voltage level should be added to the network just before calling this method, and should contains
 * at least a configured bus in bus/breaker topology or a bus bar section in node/breaker topology.
 *
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
public class ConnectVoltageLevelOnLine extends AbstractLineConnectionModification<ConnectVoltageLevelOnLine> {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectVoltageLevelOnLine.class);

    /**
     * Constructor.
     * <br/>
     * NB: This constructor will eventually be package-private, please use {@link CreateLineOnLineBuilder} instead.
     *
     * @param positionPercent        When the existing line is cut, percent is equal to the ratio between the parameters of the first line
     *                       and the parameters of the line that is cut multiplied by 100. 100 minus percent is equal to the ratio
     *                       between the parameters of the second line and the parameters of the line that is cut multiplied by 100.
     * @param bbsOrBusId     The ID of the configured bus or bus bar section to which the lines will be linked to at the attachment point.
     * @param line1Id        The non-null ID of the line segment at side 1.
     * @param line1Name      The name of the line segment at side 1.
     * @param line2Id        The non-null ID of the line segment at side 2.
     * @param line2Name      The name of the line segment at side 2.
     * @param line           The line on which the voltage level is to be attached.
     */
    ConnectVoltageLevelOnLine(double positionPercent, String bbsOrBusId, String line1Id, String line1Name,
                              String line2Id, String line2Name, Line line) {
        super(positionPercent, bbsOrBusId, line1Id, line1Name, line2Id, line2Name, line);
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException,
                      ComputationManager computationManager, ReportNode reportNode) {
        // Checks
        if (failChecks(network, throwException, reportNode, LOG)) {
            return;
        }

        // Set parameters of the two lines replacing the existing line
        LineAdder adder1 = createLineAdder(positionPercent, line1Id, line1Name, line.getTerminal1().getVoltageLevel().getId(), voltageLevel.getId(), network, line);
        LineAdder adder2 = createLineAdder(100 - positionPercent, line2Id, line2Name, voltageLevel.getId(), line.getTerminal2().getVoltageLevel().getId(), network, line);
        attachLine(line.getTerminal1(), adder1, (bus, adder) -> adder.setConnectableBus1(bus.getId()), (bus, adder) -> adder.setBus1(bus.getId()), (node, adder) -> adder.setNode1(node));
        attachLine(line.getTerminal2(), adder2, (bus, adder) -> adder.setConnectableBus2(bus.getId()), (bus, adder) -> adder.setBus2(bus.getId()), (node, adder) -> adder.setNode2(node));
        LoadingLimitsBags limits1 = new LoadingLimitsBags(line::getActivePowerLimits1, line::getApparentPowerLimits1, line::getCurrentLimits1);
        LoadingLimitsBags limits2 = new LoadingLimitsBags(line::getActivePowerLimits2, line::getApparentPowerLimits2, line::getCurrentLimits2);

        // Create the topology inside the existing voltage level
        TopologyKind topologyKind = voltageLevel.getTopologyKind();
        if (topologyKind == TopologyKind.BUS_BREAKER) {
            Bus bus = network.getBusBreakerView().getBus(bbsOrBusId);
            Bus bus1 = voltageLevel.getBusBreakerView()
                    .newBus()
                    .setId(namingStrategy.getBusId(line1Id))
                    .add();
            Bus bus2 = voltageLevel.getBusBreakerView()
                    .newBus()
                    .setId(namingStrategy.getBusId(line2Id))
                    .add();
            createBusBreakerSwitch(bus1.getId(), bus.getId(), namingStrategy.getSwitchId(line1Id, 1), voltageLevel.getBusBreakerView());
            createBusBreakerSwitch(bus.getId(), bus2.getId(), namingStrategy.getSwitchId(line2Id, 2), voltageLevel.getBusBreakerView());
            adder1.setBus2(bus1.getId());
            adder2.setBus1(bus2.getId());
        } else if (topologyKind == TopologyKind.NODE_BREAKER) {
            // New node
            int firstAvailableNode = voltageLevel.getNodeBreakerView().getMaximumNodeIndex() + 1;
            adder1.setNode2(firstAvailableNode);
            adder2.setNode1(firstAvailableNode + 3);

            // Busbar section properties
            BusbarSection bbs = network.getBusbarSection(bbsOrBusId);
            BusbarSectionPosition position = bbs.getExtension(BusbarSectionPosition.class);

            // Topology creation
            if (position == null) {
                // No position extension is present so the line is connected only on the required busbar section
                createNodeBreakerSwitchesTopology(voltageLevel, firstAvailableNode, firstAvailableNode + 1, namingStrategy, line1Id, bbs);
                createNodeBreakerSwitchesTopology(voltageLevel, firstAvailableNode + 3, firstAvailableNode + 2, namingStrategy, line2Id, bbs);
                LOG.warn("No busbar section position extension found on {}, only one disconnector is created.", bbs.getId());
                noBusbarSectionPositionExtensionReport(reportNode, bbs);
            } else {
                List<BusbarSection> bbsList = getParallelBusbarSections(voltageLevel, position);
                createNodeBreakerSwitchesTopology(voltageLevel, firstAvailableNode, firstAvailableNode + 1, namingStrategy, line1Id, bbsList, bbs);
                createNodeBreakerSwitchesTopology(voltageLevel, firstAvailableNode + 3, firstAvailableNode + 2, namingStrategy, line2Id, bbsList, bbs);
            }
        } else {
            throw new IllegalStateException();
        }

        // Remove the existing line
        String originalLineId = line.getId();
        line.remove();

        // Create the two lines
        Line line1 = adder1.add();
        Line line2 = adder2.add();
        addLoadingLimits(line1, limits1, TwoSides.ONE);
        addLoadingLimits(line2, limits2, TwoSides.TWO);
        LOG.info("Voltage level {} connected to lines {} and {} replacing line {}.", voltageLevel.getId(), line1Id, line2Id, originalLineId);
        reportNode.newReportNode()
                .withMessageTemplate("voltageConnectedOnLine", "Voltage level ${voltageLevelId} connected to lines ${line1Id} and ${line2Id} replacing line ${originalLineId}.")
                .withUntypedValue("voltageLevelId", voltageLevel.getId())
                .withUntypedValue("line1Id", line1Id)
                .withUntypedValue("line2Id", line2Id)
                .withUntypedValue("originalLineId", originalLineId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .add();
    }
}
