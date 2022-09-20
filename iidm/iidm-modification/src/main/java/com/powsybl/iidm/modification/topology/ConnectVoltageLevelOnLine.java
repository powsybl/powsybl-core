/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.reporter.Report;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.commons.reporter.TypedValue;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.AbstractNetworkModification;
import com.powsybl.iidm.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.*;

/**
 * This method cuts an existing line in two lines that will be created and connected to an existing voltage level
 * (also called switching voltage level). The voltage level should be added to the network just before calling this method, and should contains
 * at least a configured bus in bus/breaker topology or a bus bar section in node/breaker topology.
 *
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public class ConnectVoltageLevelOnLine extends AbstractNetworkModification {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectVoltageLevelOnLine.class);

    private final String bbsOrBusId;

    private final Line line;

    private double positionPercent;

    private String line1Id;
    private String line1Name;
    private String line2Id;
    private String line2Name;

    /**
     * Constructor.
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
     *
     * NB: This constructor will eventually be package-private, please use {@link CreateLineOnLineBuilder} instead.
     */
    ConnectVoltageLevelOnLine(double positionPercent, String bbsOrBusId, String line1Id, String line1Name,
                              String line2Id, String line2Name, Line line) {
        this.positionPercent = checkPercent(positionPercent);
        this.bbsOrBusId = Objects.requireNonNull(bbsOrBusId);
        this.line1Id = Objects.requireNonNull(line1Id);
        this.line1Name = line1Name;
        this.line2Id = Objects.requireNonNull(line2Id);
        this.line2Name = line2Name;
        this.line = Objects.requireNonNull(line);
    }

    public ConnectVoltageLevelOnLine setPositionPercent(double positionPercent) {
        this.positionPercent = checkPercent(positionPercent);
        return this;
    }

    public ConnectVoltageLevelOnLine setLine1Id(String line1Id) {
        this.line1Id = Objects.requireNonNull(line1Id);
        return this;
    }

    public ConnectVoltageLevelOnLine setLine1Name(String line1Name) {
        this.line1Name = line1Name;
        return this;
    }

    public ConnectVoltageLevelOnLine setLine2Id(String line2Id) {
        this.line2Id = Objects.requireNonNull(line2Id);
        return this;
    }

    public ConnectVoltageLevelOnLine setLine2Name(String line2Name) {
        this.line2Name = line2Name;
        return this;
    }

    @Override
    public void apply(Network network, boolean throwException,
                      ComputationManager computationManager, Reporter reporter) {
        // Checks
        Identifiable<?> identifiable = checkIdentifiable(bbsOrBusId, network, throwException, reporter, LOG);
        if (identifiable == null) {
            return;
        }
        VoltageLevel voltageLevel = getVoltageLevel(identifiable, throwException, reporter, LOG);
        if (voltageLevel == null) {
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
                    .setId(line.getId() + "_BUS_1")
                    .add();
            Bus bus2 = voltageLevel.getBusBreakerView()
                    .newBus()
                    .setId(line.getId() + "_BUS_2")
                    .add();
            createBusBreakerSwitches(bus1.getId(), bus.getId(), bus2.getId(), line.getId(), voltageLevel.getBusBreakerView());
            adder1.setBus2(bus1.getId());
            adder2.setBus1(bus2.getId());
        } else if (topologyKind == TopologyKind.NODE_BREAKER) {
            BusbarSection bbs = network.getBusbarSection(bbsOrBusId);
            int bbsNode = bbs.getTerminal().getNodeBreakerView().getNode();
            int firstAvailableNode = voltageLevel.getNodeBreakerView().getMaximumNodeIndex() + 1;
            createNodeBreakerSwitches(firstAvailableNode, firstAvailableNode + 1, bbsNode, "_1", line.getId(), voltageLevel.getNodeBreakerView());
            createNodeBreakerSwitches(firstAvailableNode + 3, firstAvailableNode + 2, bbsNode, "_2", line.getId(), voltageLevel.getNodeBreakerView());
            adder1.setNode2(firstAvailableNode);
            adder2.setNode1(firstAvailableNode + 3);
        } else {
            throw new AssertionError();
        }

        // Remove the existing line
        String originalLineId = line.getId();
        line.remove();

        // Create the two lines
        Line line1 = adder1.add();
        Line line2 = adder2.add();
        addLoadingLimits(line1, limits1, Branch.Side.ONE);
        addLoadingLimits(line2, limits2, Branch.Side.TWO);
        LOG.info("Voltage level {} connected to lines {} and {} replacing line {}.", voltageLevel.getId(), line1Id, line2Id, originalLineId);
        reporter.report(Report.builder()
                .withKey("voltageConnectedOnLine")
                .withDefaultMessage("Voltage level ${voltageLevelId} connected to lines ${line1Id} and ${line2Id} replacing line ${originalLineId}.")
                .withValue("voltageLevelId", voltageLevel.getId())
                .withValue("line1Id", line1Id)
                .withValue("line2Id", line2Id)
                .withValue("originalLineId", originalLineId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .build());
    }

    public String getBbsOrBusId() {
        return bbsOrBusId;
    }

    public Line getLine() {
        return line;
    }

    public double getPositionPercent() {
        return positionPercent;
    }

    public String getLine1Id() {
        return line1Id;
    }

    public String getLine1Name() {
        return line1Name;
    }

    public String getLine2Id() {
        return line2Id;
    }

    public String getLine2Name() {
        return line2Name;
    }
}
