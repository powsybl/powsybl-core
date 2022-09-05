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
public class CreateLineOnLine extends AbstractNetworkModification {

    private final String voltageLevelId;
    private final String bbsOrBusId;

    private final Line line;
    private final LineAdder lineAdder;

    private double percent;

    private String fictitiousVlId;
    private String fictitiousVlName;

    private boolean createFictSubstation;
    private String fictitiousSubstationId;
    private String fictitiousSubstationName;

    private String line1Id;
    private String line1Name;
    private String line2Id;
    private String line2Name;

    /**
     * Constructor. <br>
     * <p>
     * percent is 50. <br>
     * fictitiousVlId is line.getId() + "_VL". <br>
     * line1Id is line.getId() + "_1" <br>
     * line2Id is line.getId() + "_2". <br>
     * @deprecated Use {@link CreateLineOnLineBuilder} instead.
     */
    @Deprecated(since = "4.10.0")
    public CreateLineOnLine(String voltageLevelId, String bbsOrBusId, Line line, LineAdder lineAdder) {
        this(50, voltageLevelId, bbsOrBusId, line.getId() + "_VL", line.getId() + "_1",
                line.getId() + "_2", line, lineAdder);
    }

    /**
     * Constructor. <br>
     * <p>
     * fictitiousVlName is null <br>
     * createFictitiousSubstation is false. <br>
     * fictitiousSubstationId is line.getId() + "_S" <br>
     * fictitiousSubstationName is null. <br>
     * line1Name is null. <br>
     * line2Name is null. <br>
     *
     * @deprecated Use {@link CreateLineOnLineBuilder} instead.
     */
    @Deprecated(since = "4.10.0")
    public CreateLineOnLine(double percent, String voltageLevelId, String bbsOrBusId, String fictitiousVlId, String line1Id,
                            String line2Id, Line line, LineAdder lineAdder) {
        this(percent, voltageLevelId, bbsOrBusId, fictitiousVlId, null, false,
                line.getId() + "_S", null, line1Id, null, line2Id, null, line,
                lineAdder);
    }

    /**
     * Constructor.
     *
     * @param percent                  When the existing line is cut, percent is equal to the ratio between the parameters of the first line
     *                                 and the parameters of the line that is cut multiplied by 100. 100 minus percent is equal to the ratio
     *                                 between the parameters of the second line and the parameters of the line that is cut multiplied by 100.
     * @param voltageLevelId           The voltage level with the given ID that we want to connect to the initial line.
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
     *
     * NB: This constructor will eventually be package-private, please use {@link CreateLineOnLineBuilder} instead.
     */
    public CreateLineOnLine(double percent, String voltageLevelId, String bbsOrBusId, String fictitiousVlId, String fictitiousVlName,
                            boolean createFictSubstation, String fictitiousSubstationId, String fictitiousSubstationName,
                            String line1Id, String line1Name, String line2Id, String line2Name, Line line, LineAdder lineAdder) {
        this.percent = checkPercent(percent);
        this.voltageLevelId = Objects.requireNonNull(voltageLevelId);
        this.bbsOrBusId = Objects.requireNonNull(bbsOrBusId);
        this.fictitiousVlId = Objects.requireNonNull(fictitiousVlId);
        this.fictitiousVlName = fictitiousVlName;
        this.createFictSubstation = createFictSubstation;
        this.fictitiousSubstationId = checkFictitiousSubstationId(createFictSubstation, fictitiousSubstationId);
        this.fictitiousSubstationName = fictitiousSubstationName;
        this.line1Id = Objects.requireNonNull(line1Id);
        this.line1Name = line1Name;
        this.line2Id = Objects.requireNonNull(line2Id);
        this.line2Name = line2Name;
        this.line = Objects.requireNonNull(line);
        this.lineAdder = Objects.requireNonNull(lineAdder);
    }

    private static String checkFictitiousSubstationId(boolean createFictSubstation, String fictitiousSubstationId) {
        if (createFictSubstation && fictitiousSubstationId == null) {
            throw new PowsyblException("Fictitious substation ID must be defined if a fictitious substation is to be created");
        }
        return fictitiousSubstationId;
    }

    public CreateLineOnLine setPercent(double percent) {
        this.percent = checkPercent(percent);
        return this;
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

    public CreateLineOnLine setLine1Id(String line1Id) {
        this.line1Id = Objects.requireNonNull(line1Id);
        return this;
    }

    public CreateLineOnLine setLine1Name(String line1Name) {
        this.line1Name = line1Name;
        return this;
    }

    public CreateLineOnLine setLine2Id(String line2Id) {
        this.line2Id = Objects.requireNonNull(line2Id);
        return this;
    }

    public CreateLineOnLine setLine2Name(String line2Name) {
        this.line2Name = line2Name;
        return this;
    }

    @Override
    public void apply(Network network, boolean throwException,
                      ComputationManager computationManager, Reporter reporter) {
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
        LineAdder adder1 = createLineAdder(percent, line1Id, line1Name, line.getTerminal1().getVoltageLevel().getId(), fictitiousVlId, network, line);
        LineAdder adder2 = createLineAdder(100 - percent, line2Id, line2Name, fictitiousVlId, line.getTerminal2().getVoltageLevel().getId(), network, line);
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
        lineAdder.setNode1(3).setVoltageLevel1(fictitiousVlId).setVoltageLevel2(voltageLevelId);

        // Create topology in the existing voltage level
        VoltageLevel voltageLevel = network.getVoltageLevel(voltageLevelId);
        if (voltageLevel == null) {
            throw new PowsyblException(String.format("Voltage level %s is not found", voltageLevelId));
        }
        TopologyKind topologyKind = voltageLevel.getTopologyKind();
        if (topologyKind == TopologyKind.BUS_BREAKER) {
            Bus bus = network.getBusBreakerView().getBus(bbsOrBusId);
            if (bus == null) {
                throw new PowsyblException(String.format("Bus %s is not found", bbsOrBusId));
            }
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
            if (bbs == null) {
                throw new PowsyblException(String.format("Busbar section %s is not found", bbsOrBusId));
            }
            int bbsNode = bbs.getTerminal().getNodeBreakerView().getNode();
            int firstAvailableNode = voltageLevel.getNodeBreakerView().getMaximumNodeIndex() + 1;
            lineAdder.setNode2(firstAvailableNode);
            createNodeBreakerSwitches(firstAvailableNode, firstAvailableNode + 1, bbsNode, originalLineId, voltageLevel.getNodeBreakerView());
        } else {
            throw new AssertionError();
        }

        // Create the new line
        lineAdder.add();
    }

    public String getVoltageLevelId() {
        return voltageLevelId;
    }

    public String getBbsOrBusId() {
        return bbsOrBusId;
    }

    public Line getLine() {
        return line;
    }

    public LineAdder getLineAdder() {
        return lineAdder;
    }

    public double getPercent() {
        return percent;
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

    public String getLine1Id() {
        return line1Id;
    }

    public String getLine1Name() {
        return line1Name;
    }

    public String getLine2Id() {
        return line2Id;
    }

    public String getLine2Name() {
        return line2Name;
    }
}
