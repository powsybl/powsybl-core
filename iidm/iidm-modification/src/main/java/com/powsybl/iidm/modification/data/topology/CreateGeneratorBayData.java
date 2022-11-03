/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.data.topology;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.modification.data.NetworkModificationData;
import com.powsybl.iidm.modification.topology.CreateFeederBay;
import com.powsybl.iidm.modification.topology.CreateFeederBayBuilder;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ConnectablePosition;

import java.util.Objects;

import static com.powsybl.iidm.modification.data.util.TerminalUtils.getTerminal;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public class CreateGeneratorBayData implements NetworkModificationData<CreateGeneratorBayData, CreateFeederBay> {

    public static final String VERSION = "1.0";
    public static final String NAME = "createGeneratorBay";

    private String generatorId = null;
    private String generatorName = null;
    private boolean fictitious = false;

    private EnergySource energySource = EnergySource.OTHER;
    private double minP = Double.NaN;
    private double maxP = Double.NaN;
    private String regulatingConnectableId = null;
    private String regulatingSide = null;
    private Boolean voltageRegulatorOn = null;
    private double targetP = Double.NaN;
    private double targetQ = Double.NaN;
    private double targetV = Double.NaN;
    private double ratedS = Double.NaN;

    private String bbsId = null;
    private Integer positionOrder = null;
    private String feederName = null;
    private ConnectablePosition.Direction direction = ConnectablePosition.Direction.BOTTOM;

    public CreateGeneratorBayData setGeneratorId(String generatorId) {
        this.generatorId = generatorId;
        return this;
    }

    public CreateGeneratorBayData setGeneratorName(String generatorName) {
        this.generatorName = generatorName;
        return this;
    }

    public CreateGeneratorBayData setGeneratorFictitious(boolean fictitious) {
        this.fictitious = fictitious;
        return this;
    }

    public CreateGeneratorBayData setGeneratorEnergySource(EnergySource energySource) {
        this.energySource = energySource;
        return this;
    }

    public CreateGeneratorBayData setGeneratorMinP(double minP) {
        this.minP = minP;
        return this;
    }

    public CreateGeneratorBayData setGeneratorMaxP(double maxP) {
        this.maxP = maxP;
        return this;
    }

    public CreateGeneratorBayData setGeneratorRegulatingConnectableId(String regulatingConnectableId) {
        this.regulatingConnectableId = regulatingConnectableId;
        return this;
    }

    public CreateGeneratorBayData setGeneratorRegulatingSide(String regulatingSide) {
        this.regulatingSide = regulatingSide;
        return this;
    }

    public CreateGeneratorBayData setGeneratorVoltageRegulatorOn(boolean voltageRegulatorOn) {
        this.voltageRegulatorOn = voltageRegulatorOn;
        return this;
    }

    public CreateGeneratorBayData setGeneratorTargetP(double targetP) {
        this.targetP = targetP;
        return this;
    }

    public CreateGeneratorBayData setGeneratorTargetQ(double targetQ) {
        this.targetQ = targetQ;
        return this;
    }

    public CreateGeneratorBayData setGeneratorTargetV(double targetV) {
        this.targetV = targetV;
        return this;
    }

    public CreateGeneratorBayData setGeneratorRatedS(double ratedS) {
        this.ratedS = ratedS;
        return this;
    }

    public CreateGeneratorBayData setBusbarSectionId(String bbsId) {
        this.bbsId = bbsId;
        return this;
    }

    public CreateGeneratorBayData setPositionOrder(int positionOrder) {
        this.positionOrder = positionOrder;
        return this;
    }

    public CreateGeneratorBayData setFeederName(String feederName) {
        this.feederName = feederName;
        return this;
    }

    public CreateGeneratorBayData setDirection(ConnectablePosition.Direction direction) {
        this.direction = direction;
        return this;
    }

    public String getGeneratorId() {
        return generatorId;
    }

    public String getGeneratorName() {
        return generatorName;
    }

    public boolean isGeneratorFictitious() {
        return fictitious;
    }

    public EnergySource getGeneratorEnergySource() {
        return energySource;
    }

    public double getGeneratorMinP() {
        return minP;
    }

    public double getGeneratorMaxP() {
        return maxP;
    }

    public String getGeneratorRegulatingConnectableId() {
        return regulatingConnectableId;
    }

    public String getGeneratorRegulatingSide() {
        return regulatingSide;
    }

    public boolean isGeneratorVoltageRegulatorOn() {
        return voltageRegulatorOn;
    }

    public double getGeneratorTargetP() {
        return targetP;
    }

    public double getGeneratorTargetQ() {
        return targetQ;
    }

    public double getGeneratorTargetV() {
        return targetV;
    }

    public double getGeneratorRatedS() {
        return ratedS;
    }

    public String getBusbarSectionId() {
        return bbsId;
    }

    public Integer getPositionOrder() {
        return positionOrder;
    }

    public String getFeederName() {
        return feederName;
    }

    public ConnectablePosition.Direction getDirection() {
        return direction;
    }

    @Override
    public String getType() {
        return NAME;
    }

    @Override
    public void copy(CreateGeneratorBayData data) {
        setGeneratorId(data.generatorId)
                .setGeneratorName(data.generatorName)
                .setGeneratorFictitious(data.fictitious)
                .setGeneratorEnergySource(data.energySource)
                .setGeneratorMinP(data.minP)
                .setGeneratorMaxP(data.maxP)
                .setGeneratorRegulatingConnectableId(data.regulatingConnectableId)
                .setGeneratorRegulatingSide(data.regulatingSide)
                .setGeneratorVoltageRegulatorOn(data.voltageRegulatorOn)
                .setGeneratorTargetP(data.targetP)
                .setGeneratorTargetQ(data.targetQ)
                .setGeneratorTargetV(data.targetV)
                .setGeneratorRatedS(data.ratedS)
                .setBusbarSectionId(data.bbsId)
                .setPositionOrder(data.positionOrder)
                .setDirection(data.direction);
    }

    @Override
    public CreateFeederBay toModification(Network network) {
        checks();
        BusbarSection bbs = network.getBusbarSection(bbsId);
        if (bbs == null) {
            throw new PowsyblException("Busbar section " + bbsId + " does not exist in network " + network.getId());
        }
        VoltageLevel voltageLevel = bbs.getTerminal().getVoltageLevel();
        GeneratorAdder adder = voltageLevel.newGenerator()
                .setId(generatorId)
                .setName(generatorName)
                .setFictitious(fictitious)
                .setEnergySource(energySource)
                .setMinP(minP)
                .setMaxP(maxP)
                .setRegulatingTerminal(getTerminal(network, regulatingConnectableId, regulatingSide))
                .setVoltageRegulatorOn(voltageRegulatorOn)
                .setTargetP(targetP)
                .setTargetQ(targetQ)
                .setTargetV(targetV)
                .setRatedS(ratedS);
        return new CreateFeederBayBuilder()
                .withInjectionAdder(adder)
                .withBbsId(bbsId)
                .withInjectionPositionOrder(positionOrder)
                .withInjectionFeederName(feederName)
                .withInjectionDirection(direction)
                .build();
    }

    public void checks() {
        Objects.requireNonNull(generatorId, "Undefined generator ID");
        Objects.requireNonNull(energySource, "Undefined energy source");
        Objects.requireNonNull(voltageRegulatorOn, "Undefined voltage regulation status");
        Objects.requireNonNull(bbsId, "Undefined busbar section ID");
        Objects.requireNonNull(positionOrder, "Undefined position order");
        Objects.requireNonNull(direction, "Undefined direction");
    }
}
