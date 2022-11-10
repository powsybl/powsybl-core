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
 * <p>
 * Before modification:
 * <pre>
 * VL1 ----------------- tee point ----------------- VL2
 *      (teePointLine1)     |       (teePointLine2)
 *                          |
 *                          | (teePointLineToRemove)
 *                          |
 *                      VL3 tapped
 *                (contains bbsOrBusId)</pre>
 * After modification:
 * <pre>
 * VL1 ------------ VL3 switching ------------ VL2
 *      (newLine1)                (newLine2)</pre>
 *
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class ReplaceTeePointByVoltageLevelOnLine extends AbstractNetworkModification {

    private String teePointLine1Id;
    private String teePointLine2Id;
    private String teePointLineToRemoveId;
    private String voltageLevelId;
    private String bbsOrBusId;
    private String newLine1Id;
    private String newLine1Name;
    private String newLine2Id;
    private String newLine2Name;

    private static final String LINE_NOT_FOUND_REPORT_MESSAGE = "Line %s is not found";

    /**
     * Constructor.
     *
     * @param teePointLine1Id        The non-null ID of the existing line connecting the tee point to the first voltage level
     * @param teePointLine2Id        The non-null ID of the existing line connecting the tee point to the second voltage level
     * @param teePointLineToRemoveId The non-null ID of the existing line connecting the tee point to the tapped voltage level
     * @param voltageLevelId         The non-null ID of the existing tapped voltage level
     * @param bbsOrBusId             The non-null ID of the existing bus or bus bar section in the tapped voltage level voltageLevelId,
     *                               where we want to connect the new lines newLine1 and newLine2
     * @param newLine1Id             The non-null ID of the new line connecting the first voltage level to the formerly tapped voltage level
     * @param newLine1Name           The optional name of the new line connecting the first voltage level to the formerly tapped voltage level
     * @param newLine2Id             The non-null ID of the new line connecting the second voltage level to the formerly tapped voltage level
     * @param newLine2Name           The optional name of the new line connecting the second voltage level to the formerly tapped voltage level
     */
    ReplaceTeePointByVoltageLevelOnLine(String teePointLine1Id, String teePointLine2Id, String teePointLineToRemoveId,
                                        String voltageLevelId, String bbsOrBusId,
                                        String newLine1Id, String newLine1Name,
                                        String newLine2Id, String newLine2Name) {
        this.teePointLine1Id = Objects.requireNonNull(teePointLine1Id);
        this.teePointLine2Id = Objects.requireNonNull(teePointLine2Id);
        this.teePointLineToRemoveId = Objects.requireNonNull(teePointLineToRemoveId);
        this.voltageLevelId = Objects.requireNonNull(voltageLevelId);
        this.bbsOrBusId = Objects.requireNonNull(bbsOrBusId);
        this.newLine1Id = Objects.requireNonNull(newLine1Id);
        this.newLine1Name = newLine1Name;
        this.newLine2Id = Objects.requireNonNull(newLine2Id);
        this.newLine2Name = newLine2Name;
    }

    public String getTeePointLine1Id() {
        return teePointLine1Id;
    }

    public String getTeePointLine2Id() {
        return teePointLine2Id;
    }

    public String getTeePointLineToRemoveId() {
        return teePointLineToRemoveId;
    }

    public String getVoltageLevelId() {
        return voltageLevelId;
    }

    public String getBbsOrBusId() {
        return bbsOrBusId;
    }

    public String getNewLine1Id() {
        return newLine1Id;
    }

    public String getNewLine1Name() {
        return newLine1Name;
    }

    public String getNewLine2Id() {
        return newLine2Id;
    }

    public String getNewLine2Name() {
        return newLine2Name;
    }

    @Override
    public void apply(Network network, boolean throwException,
                      ComputationManager computationManager, Reporter reporter) {
        Line line1Z = network.getLine(teePointLine1Id);
        if (line1Z == null) {
            notFoundLineReport(reporter, teePointLine1Id);
            if (throwException) {
                throw new PowsyblException(String.format(LINE_NOT_FOUND_REPORT_MESSAGE, teePointLine1Id));
            } else {
                return;
            }
        }

        Line lineZ2 = network.getLine(teePointLine2Id);
        if (lineZ2 == null) {
            notFoundLineReport(reporter, teePointLine2Id);
            if (throwException) {
                throw new PowsyblException(String.format(LINE_NOT_FOUND_REPORT_MESSAGE, teePointLine2Id));
            } else {
                return;
            }
        }

        Line lineZP = network.getLine(teePointLineToRemoveId);
        if (lineZP == null) {
            notFoundLineReport(reporter, teePointLineToRemoveId);
            if (throwException) {
                throw new PowsyblException(String.format(LINE_NOT_FOUND_REPORT_MESSAGE, teePointLineToRemoveId));
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
            noTeePointAndOrAttachedVoltageLevelReport(reporter, teePointLine1Id, teePointLine2Id, teePointLineToRemoveId);
            if (throwException) {
                throw new PowsyblException(String.format("Unable to find the tee point and the attached voltage level from lines %s, %s and %s", teePointLine1Id, teePointLine2Id, teePointLineToRemoveId));
            } else {
                return;
            }
        }

        // Set parameters of the new lines line1C and lineC2
        LineAdder line1CAdder = createLineAdder(newLine1Id, newLine1Name, line1Z.getTerminal(line1ZOtherVlSide).getVoltageLevel().getId(), attachedVoltageLevel.getId(), network, line1Z, lineZP);
        LineAdder lineC2Adder = createLineAdder(newLine2Id, newLine2Name, attachedVoltageLevel.getId(), lineZ2.getTerminal(lineZ2OtherVlSide).getVoltageLevel().getId(), network, lineZ2, lineZP);

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
                    .setId(newLine1Id + "_BUS_1")
                    .add();
            Bus bus2 = attachedVoltageLevel.getBusBreakerView()
                    .newBus()
                    .setId(newLine2Id + "_BUS_2")
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
            createNodeBreakerSwitches(firstAvailableNode, firstAvailableNode + 1, bbsNode, "_1", newLine1Id, attachedVoltageLevel.getNodeBreakerView());
            createNodeBreakerSwitches(firstAvailableNode + 3, firstAvailableNode + 2, bbsNode, "_2", newLine2Id, attachedVoltageLevel.getNodeBreakerView());
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
        removedLineReport(reporter, teePointLine1Id);
        lineZ2.remove();
        removedLineReport(reporter, teePointLine2Id);
        lineZP.remove();
        removedLineReport(reporter, teePointLineToRemoveId);

        // Create the two new lines
        Line line1C = line1CAdder.add();
        addLoadingLimits(line1C, limits1Line1Z, Branch.Side.ONE);
        addLoadingLimits(line1C, limits2Line1Z, Branch.Side.TWO);
        createdLineReport(reporter, newLine1Id);

        Line lineC2 = lineC2Adder.add();
        addLoadingLimits(lineC2, limits1LineZ2, Branch.Side.ONE);
        addLoadingLimits(lineC2, limits2LineZ2, Branch.Side.TWO);
        createdLineReport(reporter, newLine2Id);

        // remove tee point
        removeVoltageLevelAndSubstation(teePoint, reporter);
    }
}
