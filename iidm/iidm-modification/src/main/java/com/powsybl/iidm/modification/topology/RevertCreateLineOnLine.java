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
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.LineAdder;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import static com.powsybl.iidm.modification.topology.ModificationReports.createdLineReport;
import static com.powsybl.iidm.modification.topology.ModificationReports.noTeePointAndOrAttachedVoltageLevelReport;
import static com.powsybl.iidm.modification.topology.ModificationReports.notFoundLineReport;
import static com.powsybl.iidm.modification.topology.ModificationReports.removedLineReport;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.LoadingLimitsBags;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.addLoadingLimits;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.attachLine;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.createLineAdder;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.removeVoltageLevelAndSubstation;

/**
 * This method reverses the action done in the CreateLineOnLine class :
 * it replaces 3 existing lines (with the same voltage level at one of their side) with a new line,
 * and eventually removes the existing voltage levels (tee point and attached voltage level), if they contain no equipments
 * anymore, except bus or bus bar section
 *
 *  *    VL1 ---------- tee point ---------- VL2                            VL1 ----------------------------- VL2
 *  *         (lineAZ)       |     (lineBZ)                                                (line)
 *  *                        |
 *  *                        | (lineCZ)                       =========>
 *  *                        |
 *  *                        |
 *  *               attached voltage level
 *  *
 *
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class RevertCreateLineOnLine extends AbstractNetworkModification {

    private static final Logger LOG = LoggerFactory.getLogger(RevertCreateLineOnLine.class);

    private String lineAZId;
    private String lineBZId;
    private String lineCZId;

    private String lineId;
    private String lineName;

    private static final String LINE_NOT_FOUND_REPORT_MESSAGE = "Line %s is not found";
    private static final String LINE_REMOVED_MESSAGE = "Line {} removed";

    /**
     * Constructor.
     *
     * NB: This constructor is package-private, Please use {@link RevertCreateLineOnLineBuilder} instead.
     */
    RevertCreateLineOnLine(String lineAZId, String lineBZId, String lineCZId, String lineId, String lineName) {
        this.lineAZId = Objects.requireNonNull(lineAZId);
        this.lineBZId = Objects.requireNonNull(lineBZId);
        this.lineCZId = Objects.requireNonNull(lineCZId);
        this.lineId = Objects.requireNonNull(lineId);
        this.lineName = lineName;
    }

    public RevertCreateLineOnLine setLineAZId(String lineAZId) {
        this.lineAZId = Objects.requireNonNull(lineAZId);
        return this;
    }

    public RevertCreateLineOnLine setLineBZId(String lineBZId) {
        this.lineBZId = Objects.requireNonNull(lineBZId);
        return this;
    }

    public RevertCreateLineOnLine setLineCZId(String lineCZId) {
        this.lineCZId = Objects.requireNonNull(lineCZId);
        return this;
    }

    public RevertCreateLineOnLine setLineId(String lineId) {
        this.lineId = Objects.requireNonNull(lineId);
        return this;
    }

    public RevertCreateLineOnLine setLineName(String lineName) {
        this.lineName = lineName;
        return this;
    }

    private static Line checkAndGetLine(Network network, String lineId, Reporter reporter, boolean throwException) {
        Line line = network.getLine(lineId);
        if (line == null) {
            notFoundLineReport(reporter, lineId);
            LOG.error("Line {} is not found", lineId);
            if (throwException) {
                throw new PowsyblException(String.format(LINE_NOT_FOUND_REPORT_MESSAGE, lineId));
            }
        }
        return line;
    }

    @Override
    public void apply(Network network, boolean throwException,
                      ComputationManager computationManager, Reporter reporter) {
        Line lineAZ = checkAndGetLine(network, lineAZId, reporter, throwException);
        Line lineBZ = checkAndGetLine(network, lineBZId, reporter, throwException);
        Line lineCZ = checkAndGetLine(network, lineCZId, reporter, throwException);
        if (lineAZ == null || lineBZ == null || lineCZ == null) {
            return;
        }

        // Check the configuration and find the tee point and the attached voltage level :
        // tee point is the voltage level in common with lineAZ and lineBZ
        // attached voltage level is the voltage level of lineCZ, not in common with lineAZ or lineBZ
        VoltageLevel teePoint = null;
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

            String teePointId = lineAZVlId1.equals(lineBZVlId1) || lineAZVlId1.equals(lineBZVlId2) ? lineAZVlId1 : lineAZVlId2;
            teePoint = network.getVoltageLevel(teePointId);

            newLineSide1 = lineAZVlId1.equals(teePointId) ? Branch.Side.TWO : Branch.Side.ONE;
            newLineSide2 = lineBZVlId1.equals(teePointId) ? Branch.Side.TWO : Branch.Side.ONE;

            String attachedVoltageLevelId = lineCZVlId1.equals(lineBZVlId1) || lineCZVlId1.equals(lineBZVlId2) ? lineCZVlId2 : lineCZVlId1;
            attachedVoltageLevel = network.getVoltageLevel(attachedVoltageLevelId);
        }

        if (!configOk || teePoint == null || attachedVoltageLevel == null) {
            noTeePointAndOrAttachedVoltageLevelReport(reporter, lineAZId, lineBZId, lineCZId);
            LOG.error("Unable to find the tee point and the attached voltage level from lines {}, {} and {}", lineAZId, lineBZId, lineCZId);
            if (throwException) {
                throw new PowsyblException(String.format("Unable to find the attachment point and the attached voltage level from lines %s, %s and %s", lineAZId, lineBZId, lineCZId));
            } else {
                return;
            }
        }

        // Set parameters of the new line replacing the three existing lines
        LineAdder lineAdder = createLineAdder(lineId, lineName, newLineSide1 == Branch.Side.TWO ? lineAZVlId2 : lineAZVlId1,
                newLineSide2 == Branch.Side.TWO ? lineBZVlId2 : lineBZVlId1, network, lineAZ, lineBZ);

        attachLine(lineAZ.getTerminal(newLineSide1), lineAdder, (bus, adder) -> adder.setConnectableBus1(bus.getId()), (bus, adder) -> adder.setBus1(bus.getId()), (node, adder) -> adder.setNode1(node));
        attachLine(lineBZ.getTerminal(newLineSide2), lineAdder, (bus, adder) -> adder.setConnectableBus2(bus.getId()), (bus, adder) -> adder.setBus2(bus.getId()), (node, adder) -> adder.setNode2(node));

        // get lineAZ limits on newLineSide1
        Branch.Side limitsLineAZSide = newLineSide1;
        LoadingLimitsBags limitsLineAZ = new LoadingLimitsBags(() -> lineAZ.getActivePowerLimits(limitsLineAZSide), () -> lineAZ.getApparentPowerLimits(limitsLineAZSide), () -> lineAZ.getCurrentLimits(limitsLineAZSide));

        // get lineBZ limits on newLineSide2
        Branch.Side limitsLineBZSide = newLineSide2;
        LoadingLimitsBags limitsLineBZ = new LoadingLimitsBags(() -> lineBZ.getActivePowerLimits(limitsLineBZSide), () -> lineBZ.getApparentPowerLimits(limitsLineBZSide), () -> lineBZ.getCurrentLimits(limitsLineBZSide));

        // Remove the three existing lines
        lineAZ.remove();
        removedLineReport(reporter, lineAZId);
        LOG.info(LINE_REMOVED_MESSAGE, lineAZId);

        lineBZ.remove();
        removedLineReport(reporter, lineBZId);
        LOG.info(LINE_REMOVED_MESSAGE, lineBZId);

        lineCZ.remove();
        removedLineReport(reporter, lineCZId);
        LOG.info(LINE_REMOVED_MESSAGE, lineCZId);

        // Create the new line
        Line line = lineAdder.add();
        addLoadingLimits(line, limitsLineAZ, Branch.Side.ONE);
        addLoadingLimits(line, limitsLineBZ, Branch.Side.TWO);
        createdLineReport(reporter, lineId);
        LOG.info("New line {} created, replacing lines {}, {} and {}", lineId, lineAZId, lineBZId, lineCZId);

        // remove attachment point and attachment point substation, if necessary
        removeVoltageLevelAndSubstation(teePoint, reporter);

        // remove attached voltage level and attached substation, if necessary
        removeVoltageLevelAndSubstation(attachedVoltageLevel, reporter);
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
