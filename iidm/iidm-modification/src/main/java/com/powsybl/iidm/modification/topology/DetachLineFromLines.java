/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.google.common.collect.Iterables;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.reporter.Report;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.commons.reporter.TypedValue;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.LineAdder;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.VoltageLevel;

import java.util.Objects;
import java.util.Optional;

import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.LoadingLimitsBags;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.addLoadingLimits;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.attachLine;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.calcMinLoadingLimitsBags;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.createLineAdder;

/**
 * This method reverses the action done in the AttachNewLineOnLine class :
 * it replaces 3 existing lines (with the same voltage level at one of their side) with a new line,
 * and eventually removes the existing voltage levels, if they contain no equipments anymore, except bus or bus bar section
 *
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class DetachLineFromLines implements NetworkModification {

    private String lineAZId;
    private String lineBZId;
    private String lineCZId;

    private String lineId;
    private String lineName;

    private static final String LINE_NOT_FOUND_REPORT_KEY = "lineNotFound";
    private static final String LINE_NOT_FOUND_REPORT_MESSAGE = "Line %s is not found";

    /**
     * Constructor.
     *
     * @param lineAZId     The non-null ID of the first line
     * @param lineBZId     The non-null ID of the second line
     * @param lineCZId     The non-null ID of the third line (connecting attachment point to attached voltage level)
     * @param lineId       The non-null ID of the new line to be created
     * @param lineName     The optional name of the new line to be created
     */
    public DetachLineFromLines(String lineAZId, String lineBZId, String lineCZId, String lineId, String lineName) {
        this.lineAZId = Objects.requireNonNull(lineAZId);
        this.lineBZId = Objects.requireNonNull(lineBZId);
        this.lineCZId = Objects.requireNonNull(lineCZId);
        this.lineId = Objects.requireNonNull(lineId);
        this.lineName = lineName;
    }

    private void report(String message, String key, TypedValue typedValue, Reporter reporter) {
        reporter.report(Report.builder()
                .withKey(key)
                .withDefaultMessage(message)
                .withSeverity(typedValue)
                .build());
    }

    public DetachLineFromLines setLineAZId(String lineAZId) {
        this.lineAZId = Objects.requireNonNull(lineAZId);
        return this;
    }

    public DetachLineFromLines setLineBZId(String lineBZId) {
        this.lineBZId = Objects.requireNonNull(lineBZId);
        return this;
    }

    public DetachLineFromLines setLineCZId(String lineCZId) {
        this.lineCZId = Objects.requireNonNull(lineCZId);
        return this;
    }

    public DetachLineFromLines setLineId(String lineId) {
        this.lineId = Objects.requireNonNull(lineId);
        return this;
    }

    public DetachLineFromLines setLineName(String lineName) {
        this.lineName = lineName;
        return this;
    }

    @Override
    public void apply(Network network, boolean throwException, Reporter reporter) {
        Line lineAZ = network.getLine(lineAZId);
        if (lineAZ == null) {
            String message = String.format(LINE_NOT_FOUND_REPORT_MESSAGE, lineAZId);
            report(message, LINE_NOT_FOUND_REPORT_KEY, TypedValue.ERROR_SEVERITY, reporter);
            if (throwException) {
                throw new PowsyblException(message);
            } else {
                return;
            }
        }

        Line lineBZ = network.getLine(lineBZId);
        if (lineBZ == null) {
            String message = String.format(LINE_NOT_FOUND_REPORT_MESSAGE, lineBZId);
            report(message, LINE_NOT_FOUND_REPORT_KEY, TypedValue.ERROR_SEVERITY, reporter);
            if (throwException) {
                throw new PowsyblException(message);
            } else {
                return;
            }
        }

        Line lineCZ = network.getLine(lineCZId);
        if (lineCZ == null) {
            String message = String.format(LINE_NOT_FOUND_REPORT_MESSAGE, lineCZId);
            report(message, LINE_NOT_FOUND_REPORT_KEY, TypedValue.ERROR_SEVERITY, reporter);
            if (throwException) {
                throw new PowsyblException(message);
            } else {
                return;
            }
        }

        // Check the configuration and find the attachment point and the attached voltage level :
        // attachment point is the voltage level in common with lineAZ and lineBZ
        // attached voltage level is the voltage level of lineCZ, not in common with lineAZ or lineBZ
        VoltageLevel attachmentPoint = null;
        VoltageLevel attachedVoltageLevel = null;
        boolean configOk = false;
        Branch.Side newLineSide1 = null;
        Branch.Side newLineSide2 = null;

        String lineAZVlId1 = lineAZ.getTerminal1().getVoltageLevel().getId();
        String lineAZVlId2 = lineAZ.getTerminal2().getVoltageLevel().getId();
        String lineBZVlId1 = lineBZ.getTerminal1().getVoltageLevel().getId();
        String lineBZVlId2 = lineBZ.getTerminal2().getVoltageLevel().getId();
        String lineCZVlId1 = lineCZ.getTerminal1().getVoltageLevel().getId();
        String lineCZVlId2 = lineCZ.getTerminal2().getVoltageLevel().getId();

        if ((lineAZVlId1.equals(lineBZVlId1) || lineAZVlId1.equals(lineBZVlId2) ||
                lineAZVlId2.equals(lineBZVlId1) || lineAZVlId2.equals(lineBZVlId2)) &&
                (lineBZVlId1.equals(lineCZVlId1) || lineBZVlId1.equals(lineCZVlId2) ||
                        lineBZVlId2.equals(lineCZVlId1) || lineBZVlId2.equals(lineCZVlId2)) &&
                (lineAZVlId1.equals(lineCZVlId1) || lineAZVlId1.equals(lineCZVlId2) ||
                        lineAZVlId2.equals(lineCZVlId1) || lineAZVlId2.equals(lineCZVlId2))) {
            configOk = true;

            String attachmentPointId = lineAZVlId1.equals(lineBZVlId1) || lineAZVlId1.equals(lineBZVlId2) ? lineAZVlId1 : lineAZVlId2;
            attachmentPoint = network.getVoltageLevel(attachmentPointId);

            newLineSide1 = lineAZVlId1.equals(attachmentPointId) ? Branch.Side.TWO : Branch.Side.ONE;
            newLineSide2 = lineBZVlId1.equals(attachmentPointId) ? Branch.Side.TWO : Branch.Side.ONE;

            String attachedVoltageLevelId = lineCZVlId1.equals(lineBZVlId1) || lineCZVlId1.equals(lineBZVlId2) ? lineCZVlId2 : lineCZVlId1;
            attachedVoltageLevel = network.getVoltageLevel(attachedVoltageLevelId);
        }

        if (!configOk || attachmentPoint == null || attachedVoltageLevel == null) {
            String message = String.format("Unable to find the attachment point and the attached voltage level from lines %s, %s and %s", lineAZId, lineBZId, lineCZId);
            report(message, "noAttachmentPointAndOrAttachedVoltageLevel", TypedValue.ERROR_SEVERITY, reporter);
            if (throwException) {
                throw new PowsyblException(message);
            } else {
                return;
            }
        }

        // Set parameters of the new line replacing the three existing lines
        LineAdder lineAdder = createLineAdder(lineId, lineName, newLineSide1 == Branch.Side.TWO ? lineAZVlId2 : lineAZVlId1,
                newLineSide2 == Branch.Side.TWO ? lineBZVlId2 : lineBZVlId1, network, lineAZ, lineBZ);

        attachLine(lineAZ.getTerminal(newLineSide1), lineAdder, (bus, adder) -> adder.setConnectableBus1(bus.getId()), (bus, adder) -> adder.setBus1(bus.getId()), (node, adder) -> adder.setNode1(node));
        attachLine(lineBZ.getTerminal(newLineSide2), lineAdder, (bus, adder) -> adder.setConnectableBus2(bus.getId()), (bus, adder) -> adder.setBus2(bus.getId()), (node, adder) -> adder.setNode2(node));

        // build the limits associated to the new line
        LoadingLimitsBags limits = calcMinLoadingLimitsBags(lineAZ, lineBZ);

        // Remove the three existing lines
        lineAZ.remove();
        report(String.format("Line %s removed", lineAZId), "lineRemoved", TypedValue.INFO_SEVERITY, reporter);

        lineBZ.remove();
        report(String.format("Line %s removed", lineBZId), "lineRemoved", TypedValue.INFO_SEVERITY, reporter);

        lineCZ.remove();
        report(String.format("Line %s removed", lineCZId), "lineRemoved", TypedValue.INFO_SEVERITY, reporter);

        // Create the new line
        Line line = lineAdder.add();
        addLoadingLimits(line, limits, Branch.Side.ONE);
        addLoadingLimits(line, limits, Branch.Side.TWO);
        report(String.format("Line %s created", lineId), "lineCreated", TypedValue.INFO_SEVERITY, reporter);

        // remove attachment point and attachment point substation, if necessary
        Optional<Substation> attachmentPointSubstation = attachmentPoint.getSubstation();
        if (attachmentPoint.getConnectableStream().noneMatch(c -> c.getType() != IdentifiableType.BUSBAR_SECTION && c.getType() != IdentifiableType.BUS)) {
            attachmentPoint.remove();
            report(String.format("Voltage level %s removed", attachmentPoint.getId()), "voltageLevelRemoved", TypedValue.INFO_SEVERITY, reporter);
        }
        attachmentPointSubstation.ifPresent(s -> {
            if (Iterables.isEmpty(s.getVoltageLevels())) {
                s.remove();
                report(String.format("Substation %s removed", s.getId()), "substationRemoved", TypedValue.INFO_SEVERITY, reporter);
            }
        });

        // remove attached voltage level and attached substation, if necessary
        Optional<Substation> attachedSubstation = attachedVoltageLevel.getSubstation();
        if (attachedVoltageLevel.getConnectableStream().noneMatch(c -> c.getType() != IdentifiableType.BUSBAR_SECTION && c.getType() != IdentifiableType.BUS)) {
            attachedVoltageLevel.remove();
            report(String.format("Voltage level %s removed", attachedVoltageLevel.getId()), "voltageLevelRemoved", TypedValue.INFO_SEVERITY, reporter);
        }
        attachedSubstation.ifPresent(s -> {
            if (Iterables.isEmpty(s.getVoltageLevels())) {
                s.remove();
                report(String.format("Substation %s removed", s.getId()), "substationRemoved", TypedValue.INFO_SEVERITY, reporter);
            }
        });
    }

    @Override
    public void apply(Network network, ComputationManager computationManager) {
        apply(network);
    }

    @Override
    public void apply(Network network) {
        apply(network, true, Reporter.NO_OP);
    }

    public String getLineAZId() {
        return lineAZId;
    }

    public String getLineBZId() {
        return lineBZId;
    }

    public String getLineCZId() {
        return lineCZId;
    }

    public String getLineId() {
        return lineId;
    }

    public String getLineName() {
        return lineName;
    }
}
