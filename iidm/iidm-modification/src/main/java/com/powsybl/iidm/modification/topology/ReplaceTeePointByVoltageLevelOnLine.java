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
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

import static com.powsybl.iidm.modification.util.ModificationReports.*;
import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.*;

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
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 */
public class ReplaceTeePointByVoltageLevelOnLine extends AbstractNetworkModification {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReplaceTeePointByVoltageLevelOnLine.class);

    private final String teePointLine1Id;
    private final String teePointLine2Id;
    private final String teePointLineToRemoveId;
    private final String bbsOrBusId;
    private final String newLine1Id;
    private final String newLine1Name;
    private final String newLine2Id;
    private final String newLine2Name;

    private static final String LINE_NOT_FOUND_REPORT_MESSAGE = "Line %s is not found";
    private static final String LINE_NOT_FOUND_LOG_MESSAGE = "Line {} is not found";
    private static final String LINE_REMOVED_LOG_MESSAGE = "Line {} removed";

    /**
     * Constructor.
     *
     * @param teePointLine1Id        The non-null ID of the existing line connecting the tee point to the first voltage level
     * @param teePointLine2Id        The non-null ID of the existing line connecting the tee point to the second voltage level
     * @param teePointLineToRemoveId The non-null ID of the existing line connecting the tee point to the tapped voltage level
     * @param bbsOrBusId             The non-null ID of the existing bus or bus bar section in the tapped voltage level, where we want to connect the new lines newLine1 and newLine2
     * @param newLine1Id             The non-null ID of the new line connecting the first voltage level to the formerly tapped voltage level
     * @param newLine1Name           The optional name of the new line connecting the first voltage level to the formerly tapped voltage level
     * @param newLine2Id             The non-null ID of the new line connecting the second voltage level to the formerly tapped voltage level
     * @param newLine2Name           The optional name of the new line connecting the second voltage level to the formerly tapped voltage level
     * <p>
     * NB: This constructor is package-private, Please use {@link ReplaceTeePointByVoltageLevelOnLineBuilder} instead.
     */
    ReplaceTeePointByVoltageLevelOnLine(String teePointLine1Id, String teePointLine2Id, String teePointLineToRemoveId, String bbsOrBusId,
                                        String newLine1Id, String newLine1Name, String newLine2Id, String newLine2Name) {
        this.teePointLine1Id = Objects.requireNonNull(teePointLine1Id);
        this.teePointLine2Id = Objects.requireNonNull(teePointLine2Id);
        this.teePointLineToRemoveId = Objects.requireNonNull(teePointLineToRemoveId);
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
        Line tpLine1 = network.getLine(teePointLine1Id);
        if (tpLine1 == null) {
            notFoundLineReport(reporter, teePointLine1Id);
            LOGGER.error(LINE_NOT_FOUND_LOG_MESSAGE, teePointLine1Id);
            if (throwException) {
                throw new PowsyblException(String.format(LINE_NOT_FOUND_REPORT_MESSAGE, teePointLine1Id));
            } else {
                return;
            }
        }

        Line tpLine2 = network.getLine(teePointLine2Id);
        if (tpLine2 == null) {
            notFoundLineReport(reporter, teePointLine2Id);
            LOGGER.error(LINE_NOT_FOUND_LOG_MESSAGE, teePointLine2Id);
            if (throwException) {
                throw new PowsyblException(String.format(LINE_NOT_FOUND_REPORT_MESSAGE, teePointLine2Id));
            } else {
                return;
            }
        }

        Line tpLineToRemove = network.getLine(teePointLineToRemoveId);
        if (tpLineToRemove == null) {
            notFoundLineReport(reporter, teePointLineToRemoveId);
            LOGGER.error(LINE_NOT_FOUND_LOG_MESSAGE, teePointLineToRemoveId);
            if (throwException) {
                throw new PowsyblException(String.format(LINE_NOT_FOUND_REPORT_MESSAGE, teePointLineToRemoveId));
            } else {
                return;
            }
        }

        // tee point is the voltage level in common with tpLine1, tpLine2 and tpLineToRemove
        VoltageLevel teePoint = TopologyModificationUtils.findTeePoint(tpLine1, tpLine2, tpLineToRemove);
        if (teePoint == null) {
            noTeePointAndOrTappedVoltageLevelReport(reporter, teePointLine1Id, teePointLine2Id, teePointLineToRemoveId);
            LOGGER.error("Unable to find the tee point and the tapped voltage level from lines {}, {} and {}", teePointLine1Id, teePointLine2Id, teePointLineToRemoveId);
            if (throwException) {
                throw new PowsyblException(String.format("Unable to find the tee point and the tapped voltage level from lines %s, %s and %s", teePointLine1Id, teePointLine2Id, teePointLineToRemoveId));
            } else {
                return;
            }
        }

        // tapped voltage level is the voltage level of tpLineToRemove, at the opposite side of the tee point
        VoltageLevel tappedVoltageLevel = tpLineToRemove.getTerminal1().getVoltageLevel() == teePoint
                ? tpLineToRemove.getTerminal2().getVoltageLevel()
                : tpLineToRemove.getTerminal1().getVoltageLevel();

        TwoSides tpLine1OtherVlSide = tpLine1.getTerminal1().getVoltageLevel() == teePoint ? TwoSides.TWO : TwoSides.ONE;
        TwoSides tpLine2OtherVlSide = tpLine2.getTerminal1().getVoltageLevel() == teePoint ? TwoSides.TWO : TwoSides.ONE;

        // Set parameters of the new lines newLine1 and newLine2
        LineAdder newLine1Adder = createLineAdder(newLine1Id, newLine1Name, tpLine1.getTerminal(tpLine1OtherVlSide).getVoltageLevel().getId(), tappedVoltageLevel.getId(), network, tpLine1, tpLineToRemove);
        LineAdder newLine2Adder = createLineAdder(newLine2Id, newLine2Name, tappedVoltageLevel.getId(), tpLine2.getTerminal(tpLine2OtherVlSide).getVoltageLevel().getId(), network, tpLine2, tpLineToRemove);

        // Create the topology inside the existing tapped voltage level and attach lines newLine1 and newLine2
        attachLine(tpLine1.getTerminal(tpLine1OtherVlSide), newLine1Adder, (bus, adder) -> adder.setConnectableBus1(bus.getId()), (bus, adder) -> adder.setBus1(bus.getId()), (node, adder) -> adder.setNode1(node));
        attachLine(tpLine2.getTerminal(tpLine2OtherVlSide), newLine2Adder, (bus, adder) -> adder.setConnectableBus2(bus.getId()), (bus, adder) -> adder.setBus2(bus.getId()), (node, adder) -> adder.setNode2(node));

        // Create the breaker topology
        if (!createTopology(newLine1Adder, newLine2Adder, tappedVoltageLevel, reporter, throwException)) {
            return;
        }

        // get line tpLine1 limits
        TwoSides tpLine1Limits1Side = tpLine1OtherVlSide;
        TwoSides tpLine1Limits2Side = tpLine1OtherVlSide == TwoSides.ONE ? TwoSides.TWO : TwoSides.ONE;
        LoadingLimitsBags limits1TpLine1 = new LoadingLimitsBags(() -> tpLine1.getActivePowerLimits(tpLine1Limits1Side),
            () -> tpLine1.getApparentPowerLimits(tpLine1Limits1Side),
            () -> tpLine1.getCurrentLimits(tpLine1Limits1Side));
        LoadingLimitsBags limits2TpLine1 = new LoadingLimitsBags(() -> tpLine1.getActivePowerLimits(tpLine1Limits2Side),
            () -> tpLine1.getApparentPowerLimits(tpLine1Limits2Side),
            () -> tpLine1.getCurrentLimits(tpLine1Limits2Side));

        // get line tpLine2 limits
        TwoSides tpLine2Limits1Side = tpLine2OtherVlSide == TwoSides.ONE ? TwoSides.TWO : TwoSides.ONE;
        TwoSides tpLine2Limits2Side = tpLine2OtherVlSide;

        LoadingLimitsBags limits1TpLine2 = new LoadingLimitsBags(() -> tpLine2.getActivePowerLimits(tpLine2Limits1Side),
            () -> tpLine2.getApparentPowerLimits(tpLine2Limits1Side),
            () -> tpLine2.getCurrentLimits(tpLine2Limits1Side));
        LoadingLimitsBags limits2TpLine2 = new LoadingLimitsBags(() -> tpLine2.getActivePowerLimits(tpLine2Limits2Side),
            () -> tpLine2.getApparentPowerLimits(tpLine2Limits2Side),
            () -> tpLine2.getCurrentLimits(tpLine2Limits2Side));

        // Remove the three existing lines
        tpLine1.remove();
        removedLineReport(reporter, teePointLine1Id);
        LOGGER.info(LINE_REMOVED_LOG_MESSAGE, teePointLine1Id);
        tpLine2.remove();
        removedLineReport(reporter, teePointLine2Id);
        LOGGER.info(LINE_REMOVED_LOG_MESSAGE, teePointLine2Id);
        new RemoveFeederBay(tpLineToRemove.getId()).apply(network, throwException, computationManager, reporter);
        removedLineReport(reporter, teePointLineToRemoveId);
        LOGGER.info(LINE_REMOVED_LOG_MESSAGE, teePointLineToRemoveId);

        // Create the two new lines
        Line newLine1 = newLine1Adder.add();
        addLoadingLimits(newLine1, limits1TpLine1, TwoSides.ONE);
        addLoadingLimits(newLine1, limits2TpLine1, TwoSides.TWO);
        createdLineReport(reporter, newLine1Id);
        LOGGER.info("Line {} created", newLine1Id);

        Line newLine2 = newLine2Adder.add();
        addLoadingLimits(newLine2, limits1TpLine2, TwoSides.ONE);
        addLoadingLimits(newLine2, limits2TpLine2, TwoSides.TWO);
        createdLineReport(reporter, newLine2Id);
        LOGGER.info("Line {} created", newLine2Id);

        // remove tee point
        removeVoltageLevelAndSubstation(teePoint, reporter);
    }

    private boolean createTopology(LineAdder newLine1Adder, LineAdder newLine2Adder, VoltageLevel tappedVoltageLevel, Reporter reporter, boolean throwException) {
        TopologyKind topologyKind = tappedVoltageLevel.getTopologyKind();
        if (topologyKind == TopologyKind.BUS_BREAKER) {
            Bus bus = tappedVoltageLevel.getBusBreakerView().getBus(bbsOrBusId);
            if (bus == null) {
                return errorWhenBusNull(reporter, tappedVoltageLevel, throwException);
            }
            Bus bus1 = tappedVoltageLevel.getBusBreakerView()
                .newBus()
                .setId(newLine1Id + "_BUS_1")
                .add();
            Bus bus2 = tappedVoltageLevel.getBusBreakerView()
                .newBus()
                .setId(newLine2Id + "_BUS_2")
                .add();
            createBusBreakerSwitches(bus1.getId(), bus.getId(), bus2.getId(), bbsOrBusId, tappedVoltageLevel.getBusBreakerView());
            newLine1Adder.setBus2(bus1.getId());
            newLine1Adder.setConnectableBus2(bus1.getId());
            newLine2Adder.setBus1(bus2.getId());
            newLine2Adder.setConnectableBus1(bus2.getId());
        } else if (topologyKind == TopologyKind.NODE_BREAKER) {
            BusbarSection bbs = tappedVoltageLevel.getNodeBreakerView().getBusbarSection(bbsOrBusId);
            if (bbs == null) {
                return errorWhenBusbarSectionNull(reporter, tappedVoltageLevel, throwException);
            }
            // New node
            int firstAvailableNode = tappedVoltageLevel.getNodeBreakerView().getMaximumNodeIndex() + 1;
            newLine1Adder.setNode2(firstAvailableNode);
            newLine2Adder.setNode1(firstAvailableNode + 3);

            // Busbar section properties
            BusbarSectionPosition position = bbs.getExtension(BusbarSectionPosition.class);

            // Topology creation
            if (position == null) {
                // No position extension is present so only one disconnector is needed
                createNodeBreakerSwitchesTopology(tappedVoltageLevel, firstAvailableNode, firstAvailableNode + 1, newLine1Id, bbs);
                createNodeBreakerSwitchesTopology(tappedVoltageLevel, firstAvailableNode + 3, firstAvailableNode + 2, newLine2Id, bbs);
                LOGGER.warn("No busbar section position extension found on {}, only one disconnector is created.", bbs.getId());
                noBusbarSectionPositionExtensionReport(reporter, bbs);
            } else {
                List<BusbarSection> bbsList = getParallelBusbarSections(tappedVoltageLevel, position);
                createNodeBreakerSwitchesTopology(tappedVoltageLevel, firstAvailableNode, firstAvailableNode + 1, newLine1Id, bbsList, bbs);
                createNodeBreakerSwitchesTopology(tappedVoltageLevel, firstAvailableNode + 3, firstAvailableNode + 2, newLine2Id, bbsList, bbs);
            }
        }
        return true;
    }

    private boolean errorWhenBusNull(Reporter reporter, VoltageLevel voltageLevel, boolean throwException) {
        notFoundBusInVoltageLevelReport(reporter, bbsOrBusId, voltageLevel.getId());
        LOGGER.error("Bus {} is not found in voltage level {}", bbsOrBusId, voltageLevel.getId());
        if (throwException) {
            throw new PowsyblException(String.format("Bus %s is not found in voltage level %s", bbsOrBusId, voltageLevel.getId()));
        } else {
            return false;
        }
    }

    private boolean errorWhenBusbarSectionNull(Reporter reporter, VoltageLevel voltageLevel, boolean throwException) {
        notFoundBusbarSectionInVoltageLevelReport(reporter, bbsOrBusId, voltageLevel.getId());
        LOGGER.error("Busbar section {} is not found in voltage level {}", bbsOrBusId, voltageLevel.getId());
        if (throwException) {
            throw new PowsyblException(String.format("Busbar section %s is not found in voltage level %s", bbsOrBusId, voltageLevel.getId()));
        } else {
            return false;
        }
    }
}
