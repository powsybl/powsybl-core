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
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.LineAdder;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;

import java.util.Objects;

import static com.powsybl.iidm.modification.topology.ModificationReports.createdLineReport;
import static com.powsybl.iidm.modification.topology.ModificationReports.noTeePointAndOrAttachedVoltageLevelReport;
import static com.powsybl.iidm.modification.topology.ModificationReports.notFoundBusInVoltageLevelReport;
import static com.powsybl.iidm.modification.topology.ModificationReports.notFoundBusbarSectionInVoltageLevelReport;
import static com.powsybl.iidm.modification.topology.ModificationReports.notFoundLineReport;
import static com.powsybl.iidm.modification.topology.ModificationReports.notFoundVoltageLevelReport;
import static com.powsybl.iidm.modification.topology.ModificationReports.removedLineReport;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.LoadingLimitsBags;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.addLoadingLimits;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.attachLine;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.createBusBreakerSwitches;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.createLineAdder;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.createNodeBreakerSwitches;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.removeVoltageLevelAndSubstation;

/**
 * This method transform the action done in the CreateLineOnLine class into the action done in the ConnectVoltageLevelOnLine class :
 * it replaces 3 existing lines (with the same voltage level at one of their side (tee point)) with two new lines,
 * and removes the tee point
 *
 *    VL1 ---------- tee point ---------- VL2                            VL1 ---------- attached voltage level ---------- VL2
 *         (line1Z)       |     (lineZ2)                                      (line1C)                          (lineC2)
 *                        |
 *                        | (lineZP)                       =========>
 *                        |
 *                        |
 *               attached voltage level (voltageLevelId)
 *                  (contains bbsOrBusId)
 *
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class ReplaceTeePointByVoltageLevelOnLine extends AbstractNetworkModification {

    private String line1ZId;
    private String lineZ2Id;
    private String lineZPId;
    private String voltageLevelId;
    private String bbsOrBusId;
    private String line1CId;
    private String line1CName;
    private String lineC2Id;
    private String lineC2Name;

    private static final String LINE_NOT_FOUND_REPORT_MESSAGE = "Line %s is not found";

    /**
     * Constructor.
     *
     * @param line1ZId            The non-null ID of the existing line connecting the first voltage level to the tee point
     * @param lineZ2Id            The non-null ID of the existing line connecting the tee point to the second voltage level
     * @param lineZPId            The non-null ID of the existing line connecting the tee point to the attached voltage level
     * @param voltageLevelId      The non-null ID of the existing attached voltage level
     * @param bbsOrBusId          The non-null ID of the existing bus or bus bar section in the attached voltage level voltageLevelId,
     *                            where we want to connect the new lines line1C and lineC2
     * @param line1CId            The non-null ID of the new line connecting the first voltage level to the attached voltage level
     * @param line1CName          The optional name of the new line connecting the first voltage level to the attached voltage level
     * @param lineC2Id            The non-null ID of the new line connecting the second voltage level to the attached voltage level
     * @param lineC2Name          The optional name of the new line connecting the second voltage level to the attached voltage level
     */
    ReplaceTeePointByVoltageLevelOnLine(String line1ZId, String lineZ2Id, String lineZPId,
                                        String voltageLevelId, String bbsOrBusId,
                                        String line1CId, String line1CName,
                                        String lineC2Id, String lineC2Name) {
        this.line1ZId = Objects.requireNonNull(line1ZId);
        this.lineZ2Id = Objects.requireNonNull(lineZ2Id);
        this.lineZPId = Objects.requireNonNull(lineZPId);
        this.voltageLevelId = Objects.requireNonNull(voltageLevelId);
        this.bbsOrBusId = Objects.requireNonNull(bbsOrBusId);
        this.line1CId = Objects.requireNonNull(line1CId);
        this.line1CName = line1CName;
        this.lineC2Id = Objects.requireNonNull(lineC2Id);
        this.lineC2Name = lineC2Name;
    }

    public String getLine1ZId() {
        return line1ZId;
    }

    public ReplaceTeePointByVoltageLevelOnLine setLine1ZId(String line1ZId) {
        this.line1ZId = line1ZId;
        return this;
    }

    public String getLineZ2Id() {
        return lineZ2Id;
    }

    public ReplaceTeePointByVoltageLevelOnLine setLineZ2Id(String lineZ2Id) {
        this.lineZ2Id = lineZ2Id;
        return this;
    }

    public String getLineZPId() {
        return lineZPId;
    }

    public ReplaceTeePointByVoltageLevelOnLine setLineZPId(String lineZPId) {
        this.lineZPId = lineZPId;
        return this;
    }

    public String getVoltageLevelId() {
        return voltageLevelId;
    }

    public ReplaceTeePointByVoltageLevelOnLine setVoltageLevelId(String voltageLevelId) {
        this.voltageLevelId = voltageLevelId;
        return this;
    }

    public String getBbsOrBusId() {
        return bbsOrBusId;
    }

    public ReplaceTeePointByVoltageLevelOnLine setBbsOrBusId(String bbsOrBusId) {
        this.bbsOrBusId = bbsOrBusId;
        return this;
    }

    public String getLine1CId() {
        return line1CId;
    }

    public ReplaceTeePointByVoltageLevelOnLine setLine1CId(String line1CId) {
        this.line1CId = line1CId;
        return this;
    }

    public String getLine1CName() {
        return line1CName;
    }

    public ReplaceTeePointByVoltageLevelOnLine setLine1CName(String line1CName) {
        this.line1CName = line1CName;
        return this;
    }

    public String getLineC2Id() {
        return lineC2Id;
    }

    public ReplaceTeePointByVoltageLevelOnLine setLineC2Id(String lineC2Id) {
        this.lineC2Id = lineC2Id;
        return this;
    }

    public String getLineC2Name() {
        return lineC2Name;
    }

    public ReplaceTeePointByVoltageLevelOnLine setLineC2Name(String lineC2Name) {
        this.lineC2Name = lineC2Name;
        return this;
    }

    @Override
    public void apply(Network network, boolean throwException,
                      ComputationManager computationManager, Reporter reporter) {
        Line line1Z = network.getLine(line1ZId);
        if (line1Z == null) {
            notFoundLineReport(reporter, line1ZId);
            if (throwException) {
                throw new PowsyblException(String.format(LINE_NOT_FOUND_REPORT_MESSAGE, line1ZId));
            } else {
                return;
            }
        }

        Line lineZ2 = network.getLine(lineZ2Id);
        if (lineZ2 == null) {
            notFoundLineReport(reporter, lineZ2Id);
            if (throwException) {
                throw new PowsyblException(String.format(LINE_NOT_FOUND_REPORT_MESSAGE, lineZ2Id));
            } else {
                return;
            }
        }

        Line lineZP = network.getLine(lineZPId);
        if (lineZP == null) {
            notFoundLineReport(reporter, lineZPId);
            if (throwException) {
                throw new PowsyblException(String.format(LINE_NOT_FOUND_REPORT_MESSAGE, lineZPId));
            } else {
                return;
            }
        }

        VoltageLevel attachedVoltageLevel = network.getVoltageLevel(voltageLevelId);
        if (attachedVoltageLevel == null) {
            notFoundVoltageLevelReport(reporter, voltageLevelId);
            if (throwException) {
                throw new PowsyblException(String.format("Voltage level %s is not found", voltageLevelId));
            } else {
                return;
            }
        }

        // Check the configuration and find the tee point :
        // tee point is the voltage level in common with line1Z, lineZ2 and lineZP
        VoltageLevel teePoint = null;
        Branch.Side line1ZOtherVlSide = null;
        Branch.Side lineZ2OtherVlSide = null;
        boolean configOk = false;

        String line1ZVlId1 = line1Z.getTerminal1().getVoltageLevel().getId();
        String line1ZVlId2 = line1Z.getTerminal2().getVoltageLevel().getId();
        String lineZ2VlId1 = lineZ2.getTerminal1().getVoltageLevel().getId();
        String lineZ2VlId2 = lineZ2.getTerminal2().getVoltageLevel().getId();
        String lineZPVlId1 = lineZP.getTerminal1().getVoltageLevel().getId();
        String lineZPVlId2 = lineZP.getTerminal2().getVoltageLevel().getId();

        if ((line1ZVlId1.equals(lineZ2VlId1) || line1ZVlId1.equals(lineZ2VlId2) ||
                line1ZVlId2.equals(lineZ2VlId1) || line1ZVlId2.equals(lineZ2VlId2)) &&
                (lineZ2VlId1.equals(lineZPVlId1) || lineZ2VlId1.equals(lineZPVlId2) ||
                        lineZ2VlId2.equals(lineZPVlId1) || lineZ2VlId2.equals(lineZPVlId2)) &&
                (line1ZVlId1.equals(lineZPVlId1) || line1ZVlId1.equals(lineZPVlId2) ||
                        line1ZVlId2.equals(lineZPVlId1) || line1ZVlId2.equals(lineZPVlId2))) {
            configOk = true;

            String teePointId = line1ZVlId1.equals(lineZ2VlId1) || line1ZVlId1.equals(lineZ2VlId2) ? line1ZVlId1 : line1ZVlId2;
            teePoint = network.getVoltageLevel(teePointId);

            Branch.Side line1ZTeePointSide = line1ZVlId1.equals(teePointId) ? Branch.Side.ONE : Branch.Side.TWO;
            line1ZOtherVlSide = line1ZTeePointSide == Branch.Side.ONE ? Branch.Side.TWO : Branch.Side.ONE;
            Branch.Side lineZ2TeePointSide = lineZ2VlId1.equals(teePointId) ? Branch.Side.ONE : Branch.Side.TWO;
            lineZ2OtherVlSide = lineZ2TeePointSide == Branch.Side.ONE ? Branch.Side.TWO : Branch.Side.ONE;
        }

        if (!configOk || teePoint == null || attachedVoltageLevel == null) {
            noTeePointAndOrAttachedVoltageLevelReport(reporter, line1ZId, lineZ2Id, lineZPId);
            if (throwException) {
                throw new PowsyblException(String.format("Unable to find the tee point and the attached voltage level from lines %s, %s and %s", line1ZId, lineZ2Id, lineZPId));
            } else {
                return;
            }
        }

        // Set parameters of the new lines line1C and lineC2
        LineAdder line1CAdder = createLineAdder(line1CId, line1CName, line1Z.getTerminal(line1ZOtherVlSide).getVoltageLevel().getId(), attachedVoltageLevel.getId(), network, line1Z, lineZP);
        LineAdder lineC2Adder = createLineAdder(lineC2Id, lineC2Name, attachedVoltageLevel.getId(), lineZ2.getTerminal(lineZ2OtherVlSide).getVoltageLevel().getId(), network, lineZ2, lineZP);

        // Create the topology inside the existing attached voltage level and attach lines line1C and lineC2
        attachLine(line1Z.getTerminal(line1ZOtherVlSide), line1CAdder, (bus, adder) -> adder.setConnectableBus1(bus.getId()), (bus, adder) -> adder.setBus1(bus.getId()), (node, adder) -> adder.setNode1(node));
        attachLine(lineZ2.getTerminal(lineZ2OtherVlSide), lineC2Adder, (bus, adder) -> adder.setConnectableBus2(bus.getId()), (bus, adder) -> adder.setBus2(bus.getId()), (node, adder) -> adder.setNode2(node));

        TopologyKind topologyKind = attachedVoltageLevel.getTopologyKind();
        if (topologyKind == TopologyKind.BUS_BREAKER) {
            Bus bus = network.getBusBreakerView().getBus(bbsOrBusId);
            if (bus == null) {
                notFoundBusInVoltageLevelReport(reporter, bbsOrBusId, voltageLevelId);
                if (throwException) {
                    throw new PowsyblException(String.format("Bus %s is not found in voltage level %s", bbsOrBusId, voltageLevelId));
                } else {
                    return;
                }
            }
            Bus bus1 = attachedVoltageLevel.getBusBreakerView()
                    .newBus()
                    .setId(line1CId + "_BUS_1")
                    .add();
            Bus bus2 = attachedVoltageLevel.getBusBreakerView()
                    .newBus()
                    .setId(lineC2Id + "_BUS_2")
                    .add();
            createBusBreakerSwitches(bus1.getId(), bus.getId(), bus2.getId(), bbsOrBusId, attachedVoltageLevel.getBusBreakerView());
            line1CAdder.setBus2(bus1.getId());
            line1CAdder.setConnectableBus2(bus1.getId());
            lineC2Adder.setBus1(bus2.getId());
            lineC2Adder.setConnectableBus1(bus2.getId());
        } else if (topologyKind == TopologyKind.NODE_BREAKER) {
            BusbarSection bbs = network.getBusbarSection(bbsOrBusId);
            if (bbs == null) {
                notFoundBusbarSectionInVoltageLevelReport(reporter, bbsOrBusId, voltageLevelId);
                if (throwException) {
                    throw new PowsyblException(String.format("Busbar section %s is not found in voltage level %s", bbsOrBusId, voltageLevelId));
                } else {
                    return;
                }
            }
            int bbsNode = bbs.getTerminal().getNodeBreakerView().getNode();
            int firstAvailableNode = attachedVoltageLevel.getNodeBreakerView().getMaximumNodeIndex() + 1;
            createNodeBreakerSwitches(firstAvailableNode, firstAvailableNode + 1, bbsNode, "_1", line1CId, attachedVoltageLevel.getNodeBreakerView());
            createNodeBreakerSwitches(firstAvailableNode + 3, firstAvailableNode + 2, bbsNode, "_2", lineC2Id, attachedVoltageLevel.getNodeBreakerView());
            line1CAdder.setNode2(firstAvailableNode);
            lineC2Adder.setNode1(firstAvailableNode + 3);
        }

        // get line line1Z limits
        Branch.Side line1ZLimits1Side = line1ZOtherVlSide;
        Branch.Side line1ZLimits2Side = line1ZOtherVlSide == Branch.Side.ONE ? Branch.Side.TWO : Branch.Side.ONE;
        LoadingLimitsBags limits1Line1Z = new LoadingLimitsBags(() -> line1Z.getActivePowerLimits(line1ZLimits1Side),
            () -> line1Z.getApparentPowerLimits(line1ZLimits1Side),
            () -> line1Z.getCurrentLimits(line1ZLimits1Side));
        LoadingLimitsBags limits2Line1Z = new LoadingLimitsBags(() -> line1Z.getActivePowerLimits(line1ZLimits2Side),
            () -> line1Z.getApparentPowerLimits(line1ZLimits2Side),
            () -> line1Z.getCurrentLimits(line1ZLimits2Side));

        // get line lineZ2 limits
        Branch.Side lineZ2Limits1Side = lineZ2OtherVlSide == Branch.Side.ONE ? Branch.Side.TWO : Branch.Side.ONE;
        Branch.Side lineZ2Limits2Side = lineZ2OtherVlSide;

        LoadingLimitsBags limits1LineZ2 = new LoadingLimitsBags(() -> lineZ2.getActivePowerLimits(lineZ2Limits1Side),
            () -> lineZ2.getApparentPowerLimits(lineZ2Limits1Side),
            () -> lineZ2.getCurrentLimits(lineZ2Limits1Side));
        LoadingLimitsBags limits2LineZ2 = new LoadingLimitsBags(() -> lineZ2.getActivePowerLimits(lineZ2Limits2Side),
            () -> lineZ2.getApparentPowerLimits(lineZ2Limits2Side),
            () -> lineZ2.getCurrentLimits(lineZ2Limits2Side));

        // Remove the three existing lines
        line1Z.remove();
        removedLineReport(reporter, line1ZId);
        lineZ2.remove();
        removedLineReport(reporter, lineZ2Id);
        lineZP.remove();
        removedLineReport(reporter, lineZPId);

        // Create the two new lines
        Line line1C = line1CAdder.add();
        addLoadingLimits(line1C, limits1Line1Z, Branch.Side.ONE);
        addLoadingLimits(line1C, limits2Line1Z, Branch.Side.TWO);
        createdLineReport(reporter, line1CId);

        Line lineC2 = lineC2Adder.add();
        addLoadingLimits(lineC2, limits1LineZ2, Branch.Side.ONE);
        addLoadingLimits(lineC2, limits2LineZ2, Branch.Side.TWO);
        createdLineReport(reporter, lineC2Id);

        // remove tee point
        removeVoltageLevelAndSubstation(teePoint, reporter);
    }
}
