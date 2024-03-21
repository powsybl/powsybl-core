/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.AbstractNetworkModification;
import com.powsybl.iidm.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import static com.powsybl.iidm.modification.util.ModificationReports.*;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.*;

/**
 * This method reverses the action done in the {@link CreateLineOnLine} class :
 * it replaces 3 existing lines (with the same voltage level at one of their side) with a new line,
 * and eventually removes the existing voltage levels (tee point and tapped voltage level), if they contain no equipments
 * anymore, except bus or bus bar section <br/><br/>
 * Before modification:
 * <pre>
 * VL1 --------------------- tee point -------------------- VL2
 *      (lineToBeMerged1Id)     |     (lineToBeMerged2Id)
 *                              |
 *                              | (lineToBeDeletedId)
 *                              |
 *                     tapped voltage level</pre>
 * After modification:
 * <pre>
 * VL1 ----------------------------- VL2
 *             (mergedLineId)</pre>
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
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
     * <p>
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

    private static Line checkAndGetLine(Network network, String lineId, ReportNode reportNode, boolean throwException) {
        Line line = network.getLine(lineId);
        if (line == null) {
            notFoundLineReport(reportNode, lineId);
            LOG.error("Line {} is not found", lineId);
            if (throwException) {
                throw new PowsyblException(String.format(LINE_NOT_FOUND_REPORT_MESSAGE, lineId));
            }
        }
        return line;
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException,
                      ComputationManager computationManager, ReportNode reportNode) {
        Line lineToBeMerged1 = checkAndGetLine(network, lineToBeMerged1Id, reportNode, throwException);
        Line lineToBeMerged2 = checkAndGetLine(network, lineToBeMerged2Id, reportNode, throwException);
        Line lineToBeDeleted = checkAndGetLine(network, lineToBeDeletedId, reportNode, throwException);
        if (lineToBeMerged1 == null || lineToBeMerged2 == null || lineToBeDeleted == null) {
            return;
        }

        // tee point is the voltage level in common with lineToBeMerged1 and lineToBeMerged2
        VoltageLevel teePoint = TopologyModificationUtils.findTeePoint(lineToBeMerged1, lineToBeMerged2, lineToBeDeleted);
        if (teePoint == null) {
            noTeePointAndOrTappedVoltageLevelReport(reportNode, lineToBeMerged1Id, lineToBeMerged2Id, lineToBeDeletedId);
            LOG.error("Unable to find the tee point and the tapped voltage level from lines {}, {} and {}", lineToBeMerged1Id, lineToBeMerged2Id, lineToBeDeletedId);
            if (throwException) {
                throw new PowsyblException(String.format("Unable to find the attachment point and the tapped voltage level from lines %s, %s and %s", lineToBeMerged1Id, lineToBeMerged2Id, lineToBeDeletedId));
            } else {
                return;
            }
        }

        // tapped voltage level is the voltage level of lineToBeDeleted, at the opposite side of the tee point
        VoltageLevel tappedVoltageLevel = lineToBeDeleted.getTerminal1().getVoltageLevel() == teePoint
                ? lineToBeDeleted.getTerminal2().getVoltageLevel()
                : lineToBeDeleted.getTerminal1().getVoltageLevel();

        // Set parameters of the new line replacing the three existing lines
        TwoSides newLineSide1 = lineToBeMerged1.getTerminal1().getVoltageLevel() == teePoint ? TwoSides.TWO : TwoSides.ONE;
        TwoSides newLineSide2 = lineToBeMerged2.getTerminal1().getVoltageLevel() == teePoint ? TwoSides.TWO : TwoSides.ONE;
        LineAdder lineAdder = createLineAdder(mergedLineId, mergedLineName,
                lineToBeMerged1.getTerminal(newLineSide1).getVoltageLevel().getId(),
                lineToBeMerged2.getTerminal(newLineSide2).getVoltageLevel().getId(),
                network, lineToBeMerged1, lineToBeMerged2);

        attachLine(lineToBeMerged1.getTerminal(newLineSide1), lineAdder, (bus, adder) -> adder.setConnectableBus1(bus.getId()), (bus, adder) -> adder.setBus1(bus.getId()), (node, adder) -> adder.setNode1(node));
        attachLine(lineToBeMerged2.getTerminal(newLineSide2), lineAdder, (bus, adder) -> adder.setConnectableBus2(bus.getId()), (bus, adder) -> adder.setBus2(bus.getId()), (node, adder) -> adder.setNode2(node));

        // get lineToBeMerged1 limits
        TwoSides lineToBeMerged1Side1 = newLineSide1;
        TwoSides lineToBeMerged1Side2 = newLineSide1 == TwoSides.ONE ? TwoSides.TWO : TwoSides.ONE;
        LoadingLimitsBags limitsLineToBeMerged1Side1 = new LoadingLimitsBags(() -> lineToBeMerged1.getActivePowerLimits(lineToBeMerged1Side1), () -> lineToBeMerged1.getApparentPowerLimits(lineToBeMerged1Side1), () -> lineToBeMerged1.getCurrentLimits(lineToBeMerged1Side1));
        LoadingLimitsBags limitsLineToBeMerged1Side2 = new LoadingLimitsBags(() -> lineToBeMerged1.getActivePowerLimits(lineToBeMerged1Side2), () -> lineToBeMerged1.getApparentPowerLimits(lineToBeMerged1Side2), () -> lineToBeMerged1.getCurrentLimits(lineToBeMerged1Side2));

        // get lineToBeMerged2 limits
        TwoSides lineToBeMerged2Side2 = newLineSide2;
        TwoSides lineToBeMerged2Side1 = newLineSide2 == TwoSides.ONE ? TwoSides.TWO : TwoSides.ONE;
        LoadingLimitsBags limitsLineToBeMerged2Side1 = new LoadingLimitsBags(() -> lineToBeMerged2.getActivePowerLimits(lineToBeMerged2Side1), () -> lineToBeMerged2.getApparentPowerLimits(lineToBeMerged2Side1), () -> lineToBeMerged2.getCurrentLimits(lineToBeMerged2Side1));
        LoadingLimitsBags limitsLineToBeMerged2Side2 = new LoadingLimitsBags(() -> lineToBeMerged2.getActivePowerLimits(lineToBeMerged2Side2), () -> lineToBeMerged2.getApparentPowerLimits(lineToBeMerged2Side2), () -> lineToBeMerged2.getCurrentLimits(lineToBeMerged2Side2));

        // Remove the three existing lines
        lineToBeMerged1.remove();
        removedLineReport(reportNode, lineToBeMerged1Id);
        LOG.info(LINE_REMOVED_MESSAGE, lineToBeMerged1Id);

        lineToBeMerged2.remove();
        removedLineReport(reportNode, lineToBeMerged2Id);
        LOG.info(LINE_REMOVED_MESSAGE, lineToBeMerged2Id);

        lineToBeDeleted.remove();
        removedLineReport(reportNode, lineToBeDeletedId);
        LOG.info(LINE_REMOVED_MESSAGE, lineToBeDeletedId);

        // Create the new line
        Line line = lineAdder.add();
        LoadingLimitsBags limitsSide1 = mergeLimits(lineToBeMerged1Id, limitsLineToBeMerged1Side1, limitsLineToBeMerged1Side2, reportNode);
        LoadingLimitsBags limitsSide2 = mergeLimits(lineToBeMerged2Id, limitsLineToBeMerged2Side2, limitsLineToBeMerged2Side1, reportNode);
        addLoadingLimits(line, limitsSide1, TwoSides.ONE);
        addLoadingLimits(line, limitsSide2, TwoSides.TWO);
        createdLineReport(reportNode, mergedLineId);
        LOG.info("New line {} created, replacing lines {}, {} and {}", mergedLineId, lineToBeMerged1Id, lineToBeMerged2Id, lineToBeDeletedId);

        // remove attachment point and attachment point substation, if necessary
        removeVoltageLevelAndSubstation(teePoint, reportNode);

        // remove attached voltage level and attached substation, if necessary
        removeVoltageLevelAndSubstation(tappedVoltageLevel, reportNode);
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
