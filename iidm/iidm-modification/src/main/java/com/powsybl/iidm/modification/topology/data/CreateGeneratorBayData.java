/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.iidm.modification.NetworkModificationData;
import com.powsybl.iidm.modification.topology.CreateFeederBay;
import com.powsybl.iidm.modification.topology.CreateFeederBayBuilder;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ConnectablePosition;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public class CreateGeneratorBayData implements NetworkModificationData<CreateGeneratorBayData, CreateFeederBay> {

    public static final String VERSION = "1.0";
    public static final String NAME = "createGeneratorBay";

    private String voltageLevelId = null;

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

    public CreateGeneratorBayData setVoltageLevelId(String voltageLevelId) {
        this.voltageLevelId = voltageLevelId;
        return this;
    }

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

    public String getVoltageLevelId() {
        return voltageLevelId;
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
    public String getName() {
        return NAME;
    }

    @Override
    public void write(Path path) {
        Objects.requireNonNull(path);

        try (OutputStream outputStream = Files.newOutputStream(path)) {
            write(outputStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void write(OutputStream os) {
        Objects.requireNonNull(os);
        try {
            ObjectMapper objectMapper = JsonUtil.createObjectMapper()
                    .registerModule(new CreateGeneratorBayDataJsonModule());
            ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();
            writer.writeValue(os, this);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void update(Path path) {
        Objects.requireNonNull(path);
        try (InputStream is = Files.newInputStream(path)) {
            update(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void update(InputStream is) {
        Objects.requireNonNull(is);
        try {
            ObjectMapper objectMapper = JsonUtil.createObjectMapper()
                    .registerModule(new CreateGeneratorBayDataJsonModule());
            copy(objectMapper.readerForUpdating(this).readValue(is));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void copy(CreateGeneratorBayData data) {
        setVoltageLevelId(data.getVoltageLevelId())
                .setGeneratorId(data.generatorId)
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
    public CreateFeederBay toModification() {
        throw new PowsyblException("A network should be passed in parameters to create the adder");
    }

    @Override
    public CreateFeederBay toModification(Network network) {
        checks();
        VoltageLevel voltageLevel = network.getVoltageLevel(voltageLevelId);
        if (voltageLevel == null) {
            throw new PowsyblException("Unable to create generatorAdder for CreateGeneratorBay as voltage level " + voltageLevelId + " does not exist");
        }
        GeneratorAdder adder = voltageLevel.newGenerator()
                .setId(generatorId)
                .setName(generatorName)
                .setFictitious(fictitious)
                .setEnergySource(energySource)
                .setMinP(minP)
                .setMaxP(maxP)
                .setRegulatingTerminal(getRegulatingTerminal(network, regulatingConnectableId, regulatingSide))
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
        Objects.requireNonNull(voltageLevelId, "Undefined voltage level ID");
        Objects.requireNonNull(generatorId, "Undefined generator ID");
        Objects.requireNonNull(energySource, "Undefined energy source");
        Objects.requireNonNull(voltageRegulatorOn, "Undefined voltage regulation status");
        Objects.requireNonNull(bbsId, "Undefined busbar section ID");
        Objects.requireNonNull(positionOrder, "Undefined position order");
        Objects.requireNonNull(direction, "Undefined direction");
    }

    private static Terminal getRegulatingTerminal(Network network, String regulatingConnectableId, String regulatingSide) {
        if (regulatingConnectableId == null) {
            return null;
        }
        Connectable<?> c = network.getConnectable(regulatingConnectableId);
        if (c == null) {
            throw new PowsyblException("Given regulating connectable " + regulatingConnectableId + " does not exist");
        }
        if (c instanceof Injection) {
            return ((Injection<?>) c).getTerminal();
        } else if (c instanceof Branch) {
            if (regulatingSide == null) {
                throw new PowsyblException("Undefined side for regulation on branch");
            }
            return ((Branch<?>) c).getTerminal(Branch.Side.valueOf(regulatingSide));
        } else if (c instanceof ThreeWindingsTransformer) {
            if (regulatingSide == null) {
                throw new PowsyblException("Undefined side for regulation on three-windings transformer");
            }
            return ((ThreeWindingsTransformer) c).getTerminal(ThreeWindingsTransformer.Side.valueOf(regulatingSide));
        }
        throw new AssertionError("Unexpected type of connectable " + regulatingConnectableId);
    }
}
