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
import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.LineAdder;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static com.powsybl.iidm.modification.topology.ModificationReports.createdLineReport;
import static com.powsybl.iidm.modification.topology.ModificationReports.noVoltageLevelInCommonReport;
import static com.powsybl.iidm.modification.topology.ModificationReports.notFoundLineReport;
import static com.powsybl.iidm.modification.topology.ModificationReports.removedLineReport;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.LoadingLimitsBags;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.addLoadingLimits;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.attachLine;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.calcMinLoadingLimitsBags;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.createLineAdder;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.removeVoltageLevelAndSubstation;

/**
 * This method reverses the action done in the ConnectVoltageLevelOnLine class :
 * it replaces 2 existing lines (with the same voltage level at one of their side) with a new line,
 * and eventually removes the voltage level in common (attachment point), if it contains no equipments anymore, except bus or bus bar section
 *
 *    VL1 ----------- attachment point ----------- VL2         =========>        VL1 ------------------------- VL2
 *          (line1)                      (line2)                                              (line)
 *
 *
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class RevertConnectVoltageLevelOnLine implements NetworkModification {

    private String line1Id;
    private String line2Id;

    private String lineId;
    private String lineName;

    /**
     * Constructor.
     *
     * @param line1Id       The non-null ID of the first line
     * @param line2Id       The non-null ID of the second line
     * @param lineId        The non-null ID of the new line to be created
     * @param lineName      The optional name of the new line to be created
     */
    public RevertConnectVoltageLevelOnLine(String line1Id, String line2Id, String lineId, String lineName) {
        this.line1Id = Objects.requireNonNull(line1Id);
        this.line2Id = Objects.requireNonNull(line2Id);
        this.lineId = Objects.requireNonNull(lineId);
        this.lineName = lineName;
    }

    public RevertConnectVoltageLevelOnLine setLine1Id(String line1Id) {
        this.line1Id = Objects.requireNonNull(line1Id);
        return this;
    }

    public RevertConnectVoltageLevelOnLine setLine2Id(String line2Id) {
        this.line2Id = Objects.requireNonNull(line2Id);
        return this;
    }

    public RevertConnectVoltageLevelOnLine setLineId(String lineId) {
        this.lineId = Objects.requireNonNull(lineId);
        return this;
    }

    public RevertConnectVoltageLevelOnLine setLineName(String lineName) {
        this.lineName = lineName;
        return this;
    }

    @Override
    public void apply(Network network, boolean throwException, Reporter reporter) {
        Line line1 = network.getLine(line1Id);
        if (line1 == null) {
            notFoundLineReport(reporter, line1Id);
            if (throwException) {
                throw new PowsyblException(String.format("Line %s is not found", line1Id));
            } else {
                return;
            }
        }

        Line line2 = network.getLine(line2Id);
        if (line2 == null) {
            notFoundLineReport(reporter, line2Id);
            if (throwException) {
                throw new PowsyblException(String.format("Line %s is not found", line2Id));
            } else {
                return;
            }
        }

        // Check and find the voltage level in common
        Set<String> vlIds = new HashSet<>();
        String line1VlId1 = line1.getTerminal1().getVoltageLevel().getId();
        String line1VlId2 = line1.getTerminal2().getVoltageLevel().getId();
        String line2VlId1 = line2.getTerminal1().getVoltageLevel().getId();
        String line2VlId2 = line2.getTerminal2().getVoltageLevel().getId();
        String commonVlId = "";

        vlIds.add(line1VlId1);
        if (!vlIds.add(line1VlId2)) {
            commonVlId = line1VlId2;
        }
        if (!vlIds.add(line2VlId1)) {
            commonVlId = line2VlId1;
        }
        if (!vlIds.add(line2VlId2)) {
            commonVlId = line2VlId2;
        }

        if (vlIds.size() != 3) {
            noVoltageLevelInCommonReport(reporter, line1Id, line2Id);
            if (throwException) {
                throw new PowsyblException(String.format("Lines %s and %s should have one and only one voltage level in common at their extremities", line1Id, line2Id));
            } else {
                return;
            }
        }

        VoltageLevel commonVl = network.getVoltageLevel(commonVlId);
        Branch.Side side1 = line1VlId1.equals(commonVlId) ? Branch.Side.TWO : Branch.Side.ONE;
        Branch.Side side2 = line2VlId1.equals(commonVlId) ? Branch.Side.TWO : Branch.Side.ONE;

        // Set parameters of the new line replacing the two existing lines
        LineAdder lineAdder = createLineAdder(lineId, lineName, side1 == Branch.Side.TWO ? line1VlId2 : line1VlId1,
                side2 == Branch.Side.TWO ? line2VlId2 : line2VlId1, network, line1, line2);

        attachLine(line1.getTerminal(side1), lineAdder, (bus, adder) -> adder.setConnectableBus1(bus.getId()), (bus, adder) -> adder.setBus1(bus.getId()), (node, adder) -> adder.setNode1(node));
        attachLine(line2.getTerminal(side2), lineAdder, (bus, adder) -> adder.setConnectableBus2(bus.getId()), (bus, adder) -> adder.setBus2(bus.getId()), (node, adder) -> adder.setNode2(node));

        // build the limits associated to the new line
        LoadingLimitsBags limits = calcMinLoadingLimitsBags(line1, line2);

        // Remove the two existing lines
        line1.remove();
        removedLineReport(reporter, line1Id);

        line2.remove();
        removedLineReport(reporter, line2Id);

        // Create the new line
        Line line = lineAdder.add();
        addLoadingLimits(line, limits, Branch.Side.ONE);
        addLoadingLimits(line, limits, Branch.Side.TWO);
        createdLineReport(reporter, lineId);

        // remove voltage level and substation in common, if necessary
        removeVoltageLevelAndSubstation(commonVl, reporter);
    }

    @Override
    public void apply(Network network, ComputationManager computationManager) {
        apply(network);
    }

    @Override
    public void apply(Network network) {
        apply(network, true, Reporter.NO_OP);
    }

    public String getLine1Id() {
        return line1Id;
    }

    public String getLine2Id() {
        return line2Id;
    }

    public String getLineId() {
        return lineId;
    }

    public String getLineName() {
        return lineName;
    }
}
