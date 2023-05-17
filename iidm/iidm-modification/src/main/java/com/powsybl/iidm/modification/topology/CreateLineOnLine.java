/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.reporter.Report;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.commons.reporter.TypedValue;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.*;

/**
 * Connect an existing voltage level (in practice a voltage level where we have some loads or generations) to an
 * existing line through a tee point.
 * This method cuts an existing line in two, creating a fictitious voltage level between them (the tee point). Then it links an existing voltage level to
 * this fictitious voltage level in creating a new line from a given line adder.
 *
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public class CreateLineOnLine extends AbstractLineConnectionModification<CreateLineOnLine> {

    private static final Logger LOG = LoggerFactory.getLogger(CreateLineOnLine.class);

    private final LineAdder lineAdder;

    private String fictitiousVlId;
    private String fictitiousVlName;

    private boolean createFictSubstation;
    private String fictitiousSubstationId;
    private String fictitiousSubstationName;

    /**
     * Constructor.
     *
     * @param positionPercent                  When the existing line is cut, percent is equal to the ratio between the parameters of the first line
     *                                 and the parameters of the line that is cut multiplied by 100. 100 minus percent is equal to the ratio
     *                                 between the parameters of the second line and the parameters of the line that is cut multiplied by 100.
     * @param bbsOrBusId               The ID of the existing bus or bus bar section of the voltage level voltageLevelId where we want to connect the line
     *                                 that will be between this voltage level and the fictitious voltage level.
     *                                 Please note that there will be switches between this bus or bus bar section and the connection point of the line.
     * @param fictitiousVlId           ID of the created voltage level at the attachment point of the initial line. Please note that this voltage level is fictitious.
     * @param fictitiousVlName         Name of the created voltage level at the attachment point of the initial line. Please note that this voltage level is fictitious.
     * @param createFictSubstation     If true, a fictitious substation at the attachment point will be created. Else, the created voltage level
     *                                 will be contained directly in the network.
     * @param fictitiousSubstationId   If createFictSubstation is true, the fictitious substation is given a non-null given ID.
     * @param fictitiousSubstationName If createdFictSubstation is true, the fictitious substation is given a given name.
     * @param line1Id                  When the initial line is cut, the line segment at side 1 has a non-null given ID.
     * @param line1Name                When the initial line is cut, the line segment at side 1 has a given name.
     * @param line2Id                  When the initial line is cut, the line segment at side 2 has a non-null given ID.
     * @param line2Name                When the initial line is cut, the line segment at side 2 has a given name.
     * @param line                     The initial line to be cut.
     * @param lineAdder                The line adder from which the line between the fictitious voltage level and the voltage level voltageLevelId is created.
     * <p>
     * NB: This constructor is package-private, please use {@link CreateLineOnLineBuilder} instead.
     */
    CreateLineOnLine(double positionPercent, String bbsOrBusId, String fictitiousVlId, String fictitiousVlName,
                            boolean createFictSubstation, String fictitiousSubstationId, String fictitiousSubstationName,
                            String line1Id, String line1Name, String line2Id, String line2Name, Line line, LineAdder lineAdder) {
        super(positionPercent, bbsOrBusId, line1Id, line1Name, line2Id, line2Name, line);
        this.fictitiousVlId = Objects.requireNonNull(fictitiousVlId);
        this.fictitiousVlName = fictitiousVlName;
        this.createFictSubstation = createFictSubstation;
        this.fictitiousSubstationId = checkFictitiousSubstationId(createFictSubstation, fictitiousSubstationId);
        this.fictitiousSubstationName = fictitiousSubstationName;
        this.lineAdder = Objects.requireNonNull(lineAdder);
    }

    private static String checkFictitiousSubstationId(boolean createFictSubstation, String fictitiousSubstationId) {
        if (createFictSubstation && fictitiousSubstationId == null) {
            throw new PowsyblException("Fictitious substation ID must be defined if a fictitious substation is to be created");
        }
        return fictitiousSubstationId;
    }

    public CreateLineOnLine setFictitiousVlId(String fictitiousVlId) {
        this.fictitiousVlId = Objects.requireNonNull(fictitiousVlId);
        return this;
    }

    public CreateLineOnLine setFictitiousVlName(String fictitiousVlName) {
        this.fictitiousVlName = fictitiousVlName;
        return this;
    }

    public CreateLineOnLine setCreateFictSubstation(boolean createFictSubstation) {
        checkFictitiousSubstationId(createFictSubstation, fictitiousSubstationId);
        this.createFictSubstation = createFictSubstation;
        return this;
    }

    public CreateLineOnLine setFictitiousSubstationId(String fictitiousSubstationId) {
        this.fictitiousSubstationId = checkFictitiousSubstationId(createFictSubstation, fictitiousSubstationId);
        return this;
    }

    public CreateLineOnLine setFictitiousSubstationName(String fictitiousSubstationName) {
        this.fictitiousSubstationName = fictitiousSubstationName;
        return this;
    }

    @Override
    public void apply(Network network, boolean throwException,
                      ComputationManager computationManager, Reporter reporter) {
        // Checks
        if (failChecks(network, throwException, reporter, LOG)) {
            return;
        }

        // Create the fictitious voltage Level at the attachment point
        VoltageLevel fictitiousVl;
        if (createFictSubstation) {
            fictitiousVl = network.newSubstation()
                    .setId(fictitiousSubstationId)
                    .setName(fictitiousSubstationName)
                    .setFictitious(true)
                    .add()
                    .newVoltageLevel()
                    .setId(fictitiousVlId)
                    .setName(fictitiousVlName)
                    .setFictitious(true)
                    .setNominalV(line.getTerminal1().getVoltageLevel().getNominalV())
                    .setTopologyKind(TopologyKind.NODE_BREAKER)
                    .add();
        } else {
            fictitiousVl = network.newVoltageLevel()
                    .setId(fictitiousVlId)
                    .setName(fictitiousVlName)
                    .setFictitious(true)
                    .setNominalV(line.getTerminal1().getVoltageLevel().getNominalV())
                    .setTopologyKind(TopologyKind.NODE_BREAKER).add();
        }

        // Create the two lines replacing the existing line
        LineAdder adder1 = createLineAdder(positionPercent, line1Id, line1Name, line.getTerminal1().getVoltageLevel().getId(), fictitiousVlId, network, line);
        LineAdder adder2 = createLineAdder(100 - positionPercent, line2Id, line2Name, fictitiousVlId, line.getTerminal2().getVoltageLevel().getId(), network, line);
        attachLine(line.getTerminal1(), adder1, (bus, adder) -> adder.setConnectableBus1(bus.getId()), (bus, adder) -> adder.setBus1(bus.getId()), (node, adder) -> adder.setNode1(node));
        attachLine(line.getTerminal2(), adder2, (bus, adder) -> adder.setConnectableBus2(bus.getId()), (bus, adder) -> adder.setBus2(bus.getId()), (node, adder) -> adder.setNode2(node));
        LoadingLimitsBags limits1 = new LoadingLimitsBags(line::getActivePowerLimits1, line::getApparentPowerLimits1, line::getCurrentLimits1);
        LoadingLimitsBags limits2 = new LoadingLimitsBags(line::getActivePowerLimits2, line::getApparentPowerLimits2, line::getCurrentLimits2);

        // Remove the existing line
        String originalLineId = line.getId();
        line.remove();

        Line line1 = adder1.setNode2(0).add();
        Line line2 = adder2.setNode1(2).add();
        addLoadingLimits(line1, limits1, Branch.Side.ONE);
        addLoadingLimits(line2, limits2, Branch.Side.TWO);

        // Create the topology inside the fictitious voltage level
        fictitiousVl.getNodeBreakerView()
                .newInternalConnection()
                .setNode1(0)
                .setNode2(1)
                .add();
        fictitiousVl.getNodeBreakerView()
                .newInternalConnection()
                .setNode1(1)
                .setNode2(2)
                .add();
        fictitiousVl.getNodeBreakerView()
                .newInternalConnection()
                .setNode1(1)
                .setNode2(3)
                .add();

        // Set the end points of the new line
        lineAdder.setNode1(3).setVoltageLevel1(fictitiousVlId).setVoltageLevel2(voltageLevel.getId());

        // Create topology in the existing voltage level
        TopologyKind topologyKind = voltageLevel.getTopologyKind();
        if (topologyKind == TopologyKind.BUS_BREAKER) {
            Bus bus = network.getBusBreakerView().getBus(bbsOrBusId);
            Bus bus1 = voltageLevel.getBusBreakerView()
                    .newBus()
                    .setId(originalLineId + "_BUS")
                    .add();
            lineAdder.setBus2(bus1.getId());
            voltageLevel.getBusBreakerView().newSwitch()
                    .setId(originalLineId + "_SW")
                    .setOpen(false)
                    .setBus1(bus1.getId())
                    .setBus2(bus.getId())
                    .add();
        } else if (topologyKind == TopologyKind.NODE_BREAKER) {
            BusbarSection bbs = network.getBusbarSection(bbsOrBusId);
            int bbsNode = bbs.getTerminal().getNodeBreakerView().getNode();
            int firstAvailableNode = voltageLevel.getNodeBreakerView().getMaximumNodeIndex() + 1;
            lineAdder.setNode2(firstAvailableNode);
            createNodeBreakerSwitches(firstAvailableNode, firstAvailableNode + 1, bbsNode, originalLineId, voltageLevel.getNodeBreakerView());
        } else {
            throw new IllegalStateException();
        }

        // Create the new line
        Line newLine = lineAdder.add();
        LOG.info("New line {} was created and connected on a tee point to lines {} and {} replacing line {}", newLine.getId(), line1Id, line2Id, originalLineId);
        reporter.report(Report.builder()
                .withKey("newLineOnLineCreated")
                .withDefaultMessage("New line ${newLineId} was created and connected on a tee point to lines ${line1Id} and ${line2Id} replacing line ${originalLineId}.")
                .withValue("newLineId", newLine.getId())
                .withValue("line1Id", line1Id)
                .withValue("line2Id", line2Id)
                .withValue("originalLineId", originalLineId)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .build());
    }

    public LineAdder getLineAdder() {
        return lineAdder;
    }

    public String getFictitiousVlId() {
        return fictitiousVlId;
    }

    public String getFictitiousVlName() {
        return fictitiousVlName;
    }

    public boolean isCreateFictSubstation() {
        return createFictSubstation;
    }

    public String getFictitiousSubstationId() {
        return fictitiousSubstationId;
    }

    public String getFictitiousSubstationName() {
        return fictitiousSubstationName;
    }
}
