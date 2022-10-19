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
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.mergeLimits;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.removeVoltageLevelAndSubstation;

/**
 * This method reverses the action done in the CreateLineOnLine class :
 * it replaces 3 existing lines (with the same voltage level at one of their side) with a new line,
 * and eventually removes the existing voltage levels (tee point and attached voltage level), if they contain no equipments
 * anymore, except bus or bus bar section <br/><br/>
 * <pre>
 *  *    VL1 --------------------- tee point -------------------- VL2                          VL1 ----------------------------- VL2
 *  *         (lineToBeMerged1Id)     |     (lineToBeMerged2Id)                                            (mergedLineId)
 *  *                                 |
 *  *                                 | (lineToBeDeletedId)                   =========>
 *  *                                 |
 *  *                                 |
 *  *                        attached voltage level
 *  *
 * </pre>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class RevertCreateLineOnLine extends AbstractNetworkModification {

    private static final Logger LOG = LoggerFactory.getLogger(RevertCreateLineOnLine.class);

    private String lineToBeMerged1Id;
    private String lineToBeMerged2Id;
    private String lineToBeDeletedId;

    private String mergedLineId;
    private String mergedLineName;

    private static final String LINE_NOT_FOUND_REPORT_MESSAGE = "Line %s is not found";
    private static final String LINE_REMOVED_MESSAGE = "Line {} removed";

    /**
     * Constructor.
     *
     * NB: This constructor is package-private, Please use {@link RevertCreateLineOnLineBuilder} instead.
     */
    RevertCreateLineOnLine(String lineToBeMerged1Id, String lineToBeMerged2Id, String lineToBeDeletedId, String mergedLineId, String mergedLineName) {
        this.lineToBeMerged1Id = Objects.requireNonNull(lineToBeMerged1Id);
        this.lineToBeMerged2Id = Objects.requireNonNull(lineToBeMerged2Id);
        this.lineToBeDeletedId = Objects.requireNonNull(lineToBeDeletedId);
        this.mergedLineId = Objects.requireNonNull(mergedLineId);
        this.mergedLineName = mergedLineName;
    }

    public RevertCreateLineOnLine setLineToBeMerged1Id(String lineToBeMerged1Id) {
        this.lineToBeMerged1Id = Objects.requireNonNull(lineToBeMerged1Id);
        return this;
    }

    public RevertCreateLineOnLine setLineToBeMerged2Id(String lineToBeMerged2Id) {
        this.lineToBeMerged2Id = Objects.requireNonNull(lineToBeMerged2Id);
        return this;
    }

    public RevertCreateLineOnLine setLineToBeDeletedId(String lineToBeDeletedId) {
        this.lineToBeDeletedId = Objects.requireNonNull(lineToBeDeletedId);
        return this;
    }

    public RevertCreateLineOnLine setMergedLineId(String mergedLineId) {
        this.mergedLineId = Objects.requireNonNull(mergedLineId);
        return this;
    }

    public RevertCreateLineOnLine setMergedLineName(String mergedLineName) {
        this.mergedLineName = mergedLineName;
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
        Line lineToBeMerged1 = checkAndGetLine(network, lineToBeMerged1Id, reporter, throwException);
        Line lineToBeMerged2 = checkAndGetLine(network, lineToBeMerged2Id, reporter, throwException);
        Line lineToBeDeleted = checkAndGetLine(network, lineToBeDeletedId, reporter, throwException);
        if (lineToBeMerged1 == null || lineToBeMerged2 == null || lineToBeDeleted == null) {
            return;
        }

        // Check the configuration and find the tee point and the attached voltage level :
        // tee point is the voltage level in common with lineToBeMerged1 and lineToBeMerged2
        // attached voltage level is the voltage level of lineToBeDeleted, not in common with lineToBeMerged1 or lineToBeMerged2
        VoltageLevel teePoint = null;
        VoltageLevel attachedVoltageLevel = null;
        boolean configOk = false;
        Branch.Side newLineSide1 = null;
        Branch.Side newLineSide2 = null;

        String lineToBeMerged1VlId1 = lineToBeMerged1.getTerminal1().getVoltageLevel().getId();
        String lineToBeMerged1VlId2 = lineToBeMerged1.getTerminal2().getVoltageLevel().getId();
        String lineToBeMerged2VlId1 = lineToBeMerged2.getTerminal1().getVoltageLevel().getId();
        String lineToBeMerged2VlId2 = lineToBeMerged2.getTerminal2().getVoltageLevel().getId();
        String lineToBeDeletedVlId1 = lineToBeDeleted.getTerminal1().getVoltageLevel().getId();
        String lineToBeDeletedVlId2 = lineToBeDeleted.getTerminal2().getVoltageLevel().getId();

        if ((lineToBeMerged1VlId1.equals(lineToBeMerged2VlId1) || lineToBeMerged1VlId1.equals(lineToBeMerged2VlId2) ||
                lineToBeMerged1VlId2.equals(lineToBeMerged2VlId1) || lineToBeMerged1VlId2.equals(lineToBeMerged2VlId2)) &&
                (lineToBeMerged2VlId1.equals(lineToBeDeletedVlId1) || lineToBeMerged2VlId1.equals(lineToBeDeletedVlId2) ||
                        lineToBeMerged2VlId2.equals(lineToBeDeletedVlId1) || lineToBeMerged2VlId2.equals(lineToBeDeletedVlId2)) &&
                (lineToBeMerged1VlId1.equals(lineToBeDeletedVlId1) || lineToBeMerged1VlId1.equals(lineToBeDeletedVlId2) ||
                        lineToBeMerged1VlId2.equals(lineToBeDeletedVlId1) || lineToBeMerged1VlId2.equals(lineToBeDeletedVlId2))) {
            configOk = true;

            String teePointId = lineToBeMerged1VlId1.equals(lineToBeMerged2VlId1) || lineToBeMerged1VlId1.equals(lineToBeMerged2VlId2) ? lineToBeMerged1VlId1 : lineToBeMerged1VlId2;
            teePoint = network.getVoltageLevel(teePointId);

            newLineSide1 = lineToBeMerged1VlId1.equals(teePointId) ? Branch.Side.TWO : Branch.Side.ONE;
            newLineSide2 = lineToBeMerged2VlId1.equals(teePointId) ? Branch.Side.TWO : Branch.Side.ONE;

            String attachedVoltageLevelId = lineToBeDeletedVlId1.equals(lineToBeMerged2VlId1) || lineToBeDeletedVlId1.equals(lineToBeMerged2VlId2) ? lineToBeDeletedVlId2 : lineToBeDeletedVlId1;
            attachedVoltageLevel = network.getVoltageLevel(attachedVoltageLevelId);
        }

        if (!configOk || teePoint == null || attachedVoltageLevel == null) {
            noTeePointAndOrAttachedVoltageLevelReport(reporter, lineToBeMerged1Id, lineToBeMerged2Id, lineToBeDeletedId);
            LOG.error("Unable to find the tee point and the attached voltage level from lines {}, {} and {}", lineToBeMerged1Id, lineToBeMerged2Id, lineToBeDeletedId);
            if (throwException) {
                throw new PowsyblException(String.format("Unable to find the attachment point and the attached voltage level from lines %s, %s and %s", lineToBeMerged1Id, lineToBeMerged2Id, lineToBeDeletedId));
            } else {
                return;
            }
        }

        // Set parameters of the new line replacing the three existing lines
        LineAdder lineAdder = createLineAdder(mergedLineId, mergedLineName, newLineSide1 == Branch.Side.TWO ? lineToBeMerged1VlId2 : lineToBeMerged1VlId1,
                newLineSide2 == Branch.Side.TWO ? lineToBeMerged2VlId2 : lineToBeMerged2VlId1, network, lineToBeMerged1, lineToBeMerged2);

        attachLine(lineToBeMerged1.getTerminal(newLineSide1), lineAdder, (bus, adder) -> adder.setConnectableBus1(bus.getId()), (bus, adder) -> adder.setBus1(bus.getId()), (node, adder) -> adder.setNode1(node));
        attachLine(lineToBeMerged2.getTerminal(newLineSide2), lineAdder, (bus, adder) -> adder.setConnectableBus2(bus.getId()), (bus, adder) -> adder.setBus2(bus.getId()), (node, adder) -> adder.setNode2(node));

        // get lineToBeMerged1 limits
        Branch.Side lineToBeMerged1Side1 = newLineSide1;
        Branch.Side lineToBeMerged1Side2 = newLineSide1 == Branch.Side.ONE ? Branch.Side.TWO : Branch.Side.ONE;
        LoadingLimitsBags limitsLineToBeMerged1Side1 = new LoadingLimitsBags(() -> lineToBeMerged1.getActivePowerLimits(lineToBeMerged1Side1), () -> lineToBeMerged1.getApparentPowerLimits(lineToBeMerged1Side1), () -> lineToBeMerged1.getCurrentLimits(lineToBeMerged1Side1));
        LoadingLimitsBags limitsLineToBeMerged1Side2 = new LoadingLimitsBags(() -> lineToBeMerged1.getActivePowerLimits(lineToBeMerged1Side2), () -> lineToBeMerged1.getApparentPowerLimits(lineToBeMerged1Side2), () -> lineToBeMerged1.getCurrentLimits(lineToBeMerged1Side2));

        // get lineToBeMerged2 limits
        Branch.Side lineToBeMerged2Side2 = newLineSide2;
        Branch.Side lineToBeMerged2Side1 = newLineSide2 == Branch.Side.ONE ? Branch.Side.TWO : Branch.Side.ONE;
        LoadingLimitsBags limitsLineToBeMerged2Side1 = new LoadingLimitsBags(() -> lineToBeMerged2.getActivePowerLimits(lineToBeMerged2Side1), () -> lineToBeMerged2.getApparentPowerLimits(lineToBeMerged2Side1), () -> lineToBeMerged2.getCurrentLimits(lineToBeMerged2Side1));
        LoadingLimitsBags limitsLineToBeMerged2Side2 = new LoadingLimitsBags(() -> lineToBeMerged2.getActivePowerLimits(lineToBeMerged2Side2), () -> lineToBeMerged2.getApparentPowerLimits(lineToBeMerged2Side2), () -> lineToBeMerged2.getCurrentLimits(lineToBeMerged2Side2));

        // Remove the three existing lines
        lineToBeMerged1.remove();
        removedLineReport(reporter, lineToBeMerged1Id);
        LOG.info(LINE_REMOVED_MESSAGE, lineToBeMerged1Id);

        lineToBeMerged2.remove();
        removedLineReport(reporter, lineToBeMerged2Id);
        LOG.info(LINE_REMOVED_MESSAGE, lineToBeMerged2Id);

        lineToBeDeleted.remove(true);
        removedLineReport(reporter, lineToBeDeletedId);
        LOG.info(LINE_REMOVED_MESSAGE, lineToBeDeletedId);

        // Create the new line
        Line line = lineAdder.add();
        LoadingLimitsBags limitsSide1 = mergeLimits(lineToBeMerged1Id, limitsLineToBeMerged1Side1, limitsLineToBeMerged1Side2, reporter);
        LoadingLimitsBags limitsSide2 = mergeLimits(lineToBeMerged2Id, limitsLineToBeMerged2Side2, limitsLineToBeMerged2Side1, reporter);
        addLoadingLimits(line, limitsSide1, Branch.Side.ONE);
        addLoadingLimits(line, limitsSide2, Branch.Side.TWO);
        createdLineReport(reporter, mergedLineId);
        LOG.info("New line {} created, replacing lines {}, {} and {}", mergedLineId, lineToBeMerged1Id, lineToBeMerged2Id, lineToBeDeletedId);

        // remove attachment point and attachment point substation, if necessary
        removeVoltageLevelAndSubstation(teePoint, reporter);

        // remove attached voltage level and attached substation, if necessary
        removeVoltageLevelAndSubstation(attachedVoltageLevel, reporter);
    }

    public String getLineToBeMerged1Id() {
        return lineToBeMerged1Id;
    }

    public String getLineToBeMerged2Id() {
        return lineToBeMerged2Id;
    }

    public String getLineToBeDeletedId() {
        return lineToBeDeletedId;
    }

    public String getMergedLineId() {
        return mergedLineId;
    }

    public String getMergedLineName() {
        return mergedLineName;
    }
}
