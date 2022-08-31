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

import java.util.Objects;

import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.LoadingLimitsBags;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.addLoadingLimits;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.attachLine;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.createLineAdder;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.createdLineReport;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.noAttachmentPointAndOrAttachedVoltageLevelReport;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.notFoundLineReport;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.removeVoltageLevelAndSubstation;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.removedLineReport;

/**
 * This method transform the action done in the AttachNewLineOnLine class into the action done in the AttachVoltageLevelOnLine class :
 * it replaces 3 existing lines (with the same attachment point at one of their side) with two new lines,
 * and eventually removes the attached voltage level, if it contains no equipments anymore, except bus or bus bar section
 *
 *    VL1 ---------- attachment point ---------- VL2                            VL1 ---------- attachment point ---------- VL2
 *         (line1Z)       |            (lineZ2)                                      (line1C)                    (lineC2)
 *                        |
 *                        | (lineZP)                       =========>
 *                        |
 *                        |
 *               attached voltage level
 *
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class LineOnLineIntoVoltageLevelOnLine implements NetworkModification {

    private String line1ZId;
    private String lineZ2Id;
    private String lineZPId;
    private String line1CId;
    private String line1CName;
    private String lineC2Id;
    private String lineC2Name;

    private static final String LINE_NOT_FOUND_REPORT_MESSAGE = "Line %s is not found";

    /**
     * Constructor.
     *
     * @param line1ZId            The non-null ID of the existing line connecting the first voltage level to the attachment point
     * @param lineZ2Id            The non-null ID of the existing line connecting the attachment point to the second voltage level
     * @param lineZPId            The non-null ID of the existing line connecting the attachment point to the attached voltage level
     * @param line1CId            The non-null ID of the new line connecting the first voltage level to the attachement point
     * @param line1CName          The optional name of the new line connecting the first voltage level to the attachment point
     * @param lineC2Id            The non-null ID of the new line connecting the second voltage level to the attachment point
     * @param lineC2Name          The optional name of the new line connecting the second voltage level to the attachment point
     */
    public LineOnLineIntoVoltageLevelOnLine(String line1ZId, String lineZ2Id, String lineZPId,
                                            String line1CId, String line1CName,
                                            String lineC2Id, String lineC2Name) {
        this.line1ZId = Objects.requireNonNull(line1ZId);
        this.lineZ2Id = Objects.requireNonNull(lineZ2Id);
        this.lineZPId = Objects.requireNonNull(lineZPId);
        this.line1CId = Objects.requireNonNull(line1CId);
        this.line1CName = line1CName;
        this.lineC2Id = Objects.requireNonNull(lineC2Id);
        this.lineC2Name = lineC2Name;
    }

    public String getLine1ZId() {
        return line1ZId;
    }

    public LineOnLineIntoVoltageLevelOnLine setLine1ZId(String line1ZId) {
        this.line1ZId = line1ZId;
        return this;
    }

    public String getLineZ2Id() {
        return lineZ2Id;
    }

    public LineOnLineIntoVoltageLevelOnLine setLineZ2Id(String lineZ2Id) {
        this.lineZ2Id = lineZ2Id;
        return this;
    }

    public String getLineZPId() {
        return lineZPId;
    }

    public LineOnLineIntoVoltageLevelOnLine setLineZPId(String lineZPId) {
        this.lineZPId = lineZPId;
        return this;
    }

    public String getLine1CId() {
        return line1CId;
    }

    public LineOnLineIntoVoltageLevelOnLine setLine1CId(String line1CId) {
        this.line1CId = line1CId;
        return this;
    }

    public String getLine1CName() {
        return line1CName;
    }

    public LineOnLineIntoVoltageLevelOnLine setLine1CName(String line1CName) {
        this.line1CName = line1CName;
        return this;
    }

    public String getLineC2Id() {
        return lineC2Id;
    }

    public LineOnLineIntoVoltageLevelOnLine setLineC2Id(String lineC2Id) {
        this.lineC2Id = lineC2Id;
        return this;
    }

    public String getLineC2Name() {
        return lineC2Name;
    }

    public LineOnLineIntoVoltageLevelOnLine setLineC2Name(String lineC2Name) {
        this.lineC2Name = lineC2Name;
        return this;
    }

    @Override
    public void apply(Network network, boolean throwException, Reporter reporter) {
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

        // Check the configuration and find the attachment point and the attached voltage level :
        // attachment point is the voltage level in common with line1Z, lineZ2 and lineZP
        // attached voltage level is the voltage level at one side of lineZP, but not at one side of line1Z or lineZ2
        VoltageLevel attachmentPoint = null;
        VoltageLevel attachedVoltageLevel = null;
        boolean configOk = false;
        Branch.Side line1ZAttachmentPointSide = null;
        Branch.Side lineZ2AttachmentPointSide = null;

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

            String attachmentPointId = line1ZVlId1.equals(lineZ2VlId1) || line1ZVlId1.equals(lineZ2VlId2) ? line1ZVlId1 : line1ZVlId2;
            attachmentPoint = network.getVoltageLevel(attachmentPointId);

            line1ZAttachmentPointSide = line1ZVlId1.equals(attachmentPointId) ? Branch.Side.ONE : Branch.Side.TWO;
            lineZ2AttachmentPointSide = lineZ2VlId1.equals(attachmentPointId) ? Branch.Side.ONE : Branch.Side.TWO;

            String attachedVoltageLevelId = lineZPVlId1.equals(lineZ2VlId1) || lineZPVlId1.equals(lineZ2VlId2) ? lineZPVlId2 : lineZPVlId1;
            attachedVoltageLevel = network.getVoltageLevel(attachedVoltageLevelId);
        }

        if (!configOk || attachmentPoint == null || attachedVoltageLevel == null) {
            noAttachmentPointAndOrAttachedVoltageLevelReport(reporter, line1ZId, lineZ2Id, lineZPId);
            if (throwException) {
                throw new PowsyblException(String.format("Unable to find the attachment point and the attached voltage level from lines %s, %s and %s", line1ZId, lineZ2Id, lineZPId));
            } else {
                return;
            }
        }

        // Set parameters of the new lines line1C and lineC2
        LineAdder line1CAdder = createLineAdder(line1CId, line1CName,
                line1Z.getTerminal(line1ZAttachmentPointSide == Branch.Side.ONE ? Branch.Side.TWO : Branch.Side.ONE).getVoltageLevel().getId(),
                attachmentPoint.getId(),
                network, line1Z, lineZP);
        LineAdder lineC2Adder = createLineAdder(lineC2Id, lineC2Name,
                attachmentPoint.getId(),
                lineZ2.getTerminal(lineZ2AttachmentPointSide == Branch.Side.ONE ? Branch.Side.TWO : Branch.Side.ONE).getVoltageLevel().getId(),
                network, lineZ2, lineZP);

        attachLine(line1Z.getTerminal(Branch.Side.ONE), line1CAdder, (bus, adder) -> adder.setConnectableBus1(bus.getId()), (bus, adder) -> adder.setBus1(bus.getId()), (node, adder) -> adder.setNode1(node));
        attachLine(line1Z.getTerminal(Branch.Side.TWO), line1CAdder, (bus, adder) -> adder.setConnectableBus2(bus.getId()), (bus, adder) -> adder.setBus2(bus.getId()), (node, adder) -> adder.setNode2(node));

        attachLine(lineZ2.getTerminal(Branch.Side.ONE), lineC2Adder, (bus, adder) -> adder.setConnectableBus1(bus.getId()), (bus, adder) -> adder.setBus1(bus.getId()), (node, adder) -> adder.setNode1(node));
        attachLine(lineZ2.getTerminal(Branch.Side.TWO), lineC2Adder, (bus, adder) -> adder.setConnectableBus2(bus.getId()), (bus, adder) -> adder.setBus2(bus.getId()), (node, adder) -> adder.setNode2(node));

        // Remove the three existing lines
        line1Z.remove();
        removedLineReport(reporter, line1ZId);
        lineZ2.remove();
        removedLineReport(reporter, lineZ2Id);
        lineZP.remove();
        removedLineReport(reporter, lineZPId);

        // Create the new lines
        Line line1C = line1CAdder.add();
        LoadingLimitsBags limits1Line1Z = new LoadingLimitsBags(line1Z::getActivePowerLimits1, line1Z::getApparentPowerLimits1, line1Z::getCurrentLimits1);
        LoadingLimitsBags limits2Line1Z = new LoadingLimitsBags(line1Z::getActivePowerLimits2, line1Z::getApparentPowerLimits2, line1Z::getCurrentLimits2);
        addLoadingLimits(line1C, limits1Line1Z, Branch.Side.ONE);
        addLoadingLimits(line1C, limits2Line1Z, Branch.Side.TWO);
        createdLineReport(reporter, line1CId);

        Line lineC2 = lineC2Adder.add();
        LoadingLimitsBags limits1LineZ2 = new LoadingLimitsBags(lineZ2::getActivePowerLimits1, lineZ2::getApparentPowerLimits1, lineZ2::getCurrentLimits1);
        LoadingLimitsBags limits2LineZ2 = new LoadingLimitsBags(lineZ2::getActivePowerLimits2, lineZ2::getApparentPowerLimits2, lineZ2::getCurrentLimits2);
        addLoadingLimits(lineC2, limits1LineZ2, Branch.Side.ONE);
        addLoadingLimits(lineC2, limits2LineZ2, Branch.Side.TWO);
        createdLineReport(reporter, lineC2Id);

        // remove attached voltage level, if necessary
        removeVoltageLevelAndSubstation(attachedVoltageLevel, reporter);
    }

    @Override
    public void apply(Network network, ComputationManager computationManager) {
        apply(network);
    }

    @Override
    public void apply(Network network) {
        apply(network, true, Reporter.NO_OP);
    }
}
