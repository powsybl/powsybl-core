/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.test;

import com.powsybl.iidm.network.*;

import java.util.Map;
import java.util.Objects;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public final class DcDetailedNetworkFactory {

    private DcDetailedNetworkFactory() {
    }

    /**
     * Creates a simple one bus AC (sub)network with dangling lines.
     * <br/>
     * Example with FR and one xNode where FR exports 200 MW:
     * <pre>
     *     var net = createSimpleAcNetworkWithDanglingLines(networkFactory, Country.FR, Map.of("xNode1", 200.));
     * </pre>
     *
     * <pre>
     *  targetP = 2000 MW / maxP = 4000 MW
     *  GEN-FR
     *    |
     *  (BUS-FR)-----(DLAC-FR-xNode1)  P0 = 200 MW
     *    |
     *  LOAD-FR
     *  P0 = 2000 MW - 200 MW = 1800 MW
     * </pre>
     */
    private static Network createSimpleAcNetworkWithDanglingLines(NetworkFactory networkFactory, Country country, Map<String, Double> xNodes) {
        Objects.requireNonNull(networkFactory);
        Network network = networkFactory.createNetwork(country.name(), "test");
        Substation s = network.newSubstation()
                .setId("S-" + country.name())
                .setCountry(country)
                .add();
        VoltageLevel vl = s.newVoltageLevel()
                .setId("VL-" + country.name())
                .setNominalV(400.0)
                .setLowVoltageLimit(380.0)
                .setHighVoltageLimit(420.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        Bus b = vl.getBusBreakerView().newBus()
                .setId("BUS-" + country.name())
                .add();
        vl.newGenerator()
                .setId("GEN-" + country.name())
                .setMinP(0.0)
                .setMaxP(4000.0)
                .setVoltageRegulatorOn(true)
                .setTargetV(400.0)
                .setTargetP(2000.0)
                .setTargetQ(0.0)
                .setBus(b.getId())
                .add();
        Load load = vl.newLoad()
                .setId("LOAD-" + country.name())
                .setP0(2000.0)
                .setQ0(0.0)
                .setBus(b.getId())
                .add();
        xNodes.forEach((xNode, v) -> {
            load.setP0(load.getP0() - v);
            vl.newDanglingLine()
                    .setId("DLAC-" + country.name() + "-" + xNode)
                    .setBus(b.getId())
                    .setR(0.3)
                    .setX(3.0)
                    .setB(0.0)
                    .setG(0.0)
                    .setP0(v)
                    .setQ0(0.0)
                    .setPairingKey(xNode)
                    .add();
        });
        return network;
    }

    enum Mode {
        ONE_T2WT,
        TWO_T2WT,
        T3WT
    }

    private static void addDcAcElements(Network network, Country country, String xNode, double exchange, Mode mode) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(country);
        Objects.requireNonNull(xNode);
        Objects.requireNonNull(mode);
        Substation s = network.newSubstation()
                .setId("SDC-" + country.name() + "-" + xNode)
                .add();
        VoltageLevel vldc400 = s.newVoltageLevel()
                .setId("VLDC-" + country.name() + "-" + xNode + "-400")
                .setNominalV(400.0)
                .setLowVoltageLimit(380.0)
                .setHighVoltageLimit(420.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        Bus bDc400 = vldc400.getBusBreakerView().newBus()
                .setId("BUSDC-" + country.name() + "-" + xNode + "-400")
                .add();
        vldc400.newDanglingLine()
                .setId("DLDC-" + country.name() + "-" + xNode)
                .setBus(bDc400.getId())
                .setR(0.3)
                .setX(3.0)
                .setB(0.0)
                .setG(0.0)
                .setP0(exchange)
                .setQ0(0.0)
                .setPairingKey(xNode)
                .add();
        VoltageLevel vldc150 = s.newVoltageLevel()
                .setId("VLDC-" + country.name() + "-" + xNode + "-150")
                .setNominalV(150.0)
                .setLowVoltageLimit(120.0)
                .setHighVoltageLimit(180.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        if (mode == Mode.ONE_T2WT) {
            Bus bDc1501 = vldc150.getBusBreakerView().newBus()
                    .setId("BUSDC-" + country.name() + "-" + xNode + "-150")
                    .add();
            s.newTwoWindingsTransformer()
                    .setId("TRDC-" + country.name() + "-" + xNode)
                    .setVoltageLevel1(vldc400.getId())
                    .setBus1(bDc400.getId())
                    .setConnectableBus1(bDc400.getId())
                    .setRatedU1(400.0)
                    .setVoltageLevel2(vldc150.getId())
                    .setBus2(bDc1501.getId())
                    .setConnectableBus2(bDc1501.getId())
                    .setRatedU2(150)
                    .setR(0.1)
                    .setX(5.0)
                    .setG(0.0)
                    .setB(0.0)
                    .add();
        } else if (mode == Mode.TWO_T2WT) {
            Bus bDc1501 = vldc150.getBusBreakerView().newBus()
                    .setId("BUSDC-" + country.name() + "-" + xNode + "-150-1")
                    .add();
            Bus bDc1502 = vldc150.getBusBreakerView().newBus()
                    .setId("BUSDC-" + country.name() + "-" + xNode + "-150-2")
                    .add();
            Bus bDc400i = vldc400.getBusBreakerView().newBus()
                    .setId("BUSDC-" + country.name() + "-" + xNode + "-400-I")
                    .add();
            network.newLine()
                    .setId("LINEDC-" + country.name() + "-" + xNode + "-400-I")
                    .setVoltageLevel1(vldc400.getId())
                    .setBus1(bDc400.getId())
                    .setConnectableBus1(bDc400.getId())
                    .setVoltageLevel2(vldc400.getId())
                    .setBus2(bDc400i.getId())
                    .setConnectableBus2(bDc400i.getId())
                    .setR(0.3)
                    .setX(3.0)
                    .setG1(0.0)
                    .setB1(0.0)
                    .setG2(0.0)
                    .setB2(0.0)
                    .add();
            s.newTwoWindingsTransformer()
                    .setId("TRDC-" + country.name() + "-" + xNode + "-1")
                    .setVoltageLevel1(vldc400.getId())
                    .setBus1(bDc400i.getId())
                    .setConnectableBus1(bDc400i.getId())
                    .setRatedU1(400.0)
                    .setVoltageLevel2(vldc150.getId())
                    .setBus2(bDc1501.getId())
                    .setConnectableBus2(bDc1501.getId())
                    .setRatedU2(150)
                    .setR(0.1)
                    .setX(5.0)
                    .setG(0.0)
                    .setB(0.0)
                    .add();
            s.newTwoWindingsTransformer()
                    .setId("TRDC-" + country.name() + "-" + xNode + "-2")
                    .setVoltageLevel1(vldc400.getId())
                    .setBus1(bDc400i.getId())
                    .setConnectableBus1(bDc400i.getId())
                    .setRatedU1(400.0)
                    .setVoltageLevel2(vldc150.getId())
                    .setBus2(bDc1502.getId())
                    .setConnectableBus2(bDc1502.getId())
                    .setRatedU2(150)
                    .setR(0.1)
                    .setX(5.0)
                    .setG(0.0)
                    .setB(0.0)
                    .add();

        } else if (mode == Mode.T3WT) {
            Bus bDc1501 = vldc150.getBusBreakerView().newBus()
                    .setId("BUSDC-" + country.name() + "-" + xNode + "-150-1")
                    .add();
            Bus bDc1502 = vldc150.getBusBreakerView().newBus()
                    .setId("BUSDC-" + country.name() + "-" + xNode + "-150-2")
                    .add();
            s.newThreeWindingsTransformer()
                    .setId("TRDC-" + country.name() + "-" + xNode)
                    .setRatedU0(400.0)
                    .newLeg1()
                    .setR(0.1)
                    .setX(0.0)
                    .setG(0.0)
                    .setB(0.0)
                    .setRatedU(400.0)
                    .setVoltageLevel(vldc400.getId())
                    .setBus(bDc400.getId())
                    .add()
                    .newLeg2()
                    .setR(0.1)
                    .setX(5.0)
                    .setG(0.0)
                    .setB(0.0)
                    .setRatedU(150.0)
                    .setVoltageLevel(vldc150.getId())
                    .setBus(bDc1501.getId())
                    .add()
                    .newLeg3()
                    .setR(0.1)
                    .setX(5.0)
                    .setG(0.0)
                    .setB(0.0)
                    .setRatedU(150.0)
                    .setVoltageLevel(vldc150.getId())
                    .setBus(bDc1502.getId())
                    .add()
                    .add();
        }
    }

    private static Network createLccMonopoleBase(NetworkFactory networkFactory, String dcNetworkId) {
        Objects.requireNonNull(networkFactory);
        Objects.requireNonNull(dcNetworkId);

        Network dcNetwork = networkFactory.createNetwork(dcNetworkId, "test");
        Network fr = createSimpleAcNetworkWithDanglingLines(networkFactory, Country.FR, Map.of("xNodeDc1fr", 200.));
        Network gb = createSimpleAcNetworkWithDanglingLines(networkFactory, Country.GB, Map.of("xNodeDc1gb", -200.));
        addDcAcElements(dcNetwork, Country.FR, "xNodeDc1fr", -200., Mode.TWO_T2WT);
        addDcAcElements(dcNetwork, Country.GB, "xNodeDc1gb", 200., Mode.T3WT);

        DcNode dcNodeFrPos = dcNetwork.newDcNode()
                .setId("dcNodeFrPos")
                .setNominalV(500.)
                .add();
        DcNode dcNodeFrNeg = dcNetwork.newDcNode()
                .setId("dcNodeFrNeg")
                .setNominalV(1.)
                .add();
        DcNode dcNodeGbPos = dcNetwork.newDcNode()
                .setId("dcNodeGbPos")
                .setNominalV(500.)
                .add();
        DcNode dcNodeGbNeg = dcNetwork.newDcNode()
                .setId("dcNodeGbNeg")
                .setNominalV(1.)
                .add();
        dcNetwork.newDcGround()
                .setId("dcGroundFr")
                .setDcNodeId(dcNodeFrNeg.getId())
                .setConnected(true)
                .setR(0.0)
                .add();
        dcNetwork.newDcGround()
                .setId("dcGroundGb")
                .setDcNodeId(dcNodeGbNeg.getId())
                .setConnected(true)
                .setR(0.0)
                .add();
        dcNetwork.newDcLine()
                .setId("dcLine1")
                .setDcNode1Id(dcNodeFrPos.getId())
                .setConnected1(true)
                .setDcNode2Id(dcNodeGbPos.getId())
                .setConnected2(true)
                .setR(5.0)
                .add();
        dcNetwork.getVoltageLevel("VLDC-FR-xNodeDc1fr-150").newDcLineCommutatedConverter()
                .setId("CsFr")
                .setBus1("BUSDC-FR-xNodeDc1fr-150-1")
                .setBus2("BUSDC-FR-xNodeDc1fr-150-2")
                .setDcNode1Id(dcNodeFrNeg.getId())
                .setDcNode2Id(dcNodeFrPos.getId())
                .setControlMode(DcConverter.ControlMode.V_DC)
                .setPccTerminal(dcNetwork.getLine("LINEDC-FR-xNodeDc1fr-400-I").getTerminal1())
                .setTargetVdc(500.)
                .setTargetP(200.)
                .add();
        dcNetwork.getVoltageLevel("VLDC-GB-xNodeDc1gb-150").newDcLineCommutatedConverter()
                .setId("CsGb")
                .setBus1("BUSDC-GB-xNodeDc1gb-150-1")
                .setBus2("BUSDC-GB-xNodeDc1gb-150-2")
                .setDcNode1Id(dcNodeGbNeg.getId())
                .setDcNode2Id(dcNodeGbPos.getId())
                .setControlMode(DcConverter.ControlMode.P_PCC)
                .setPccTerminal(dcNetwork.getThreeWindingsTransformer("TRDC-GB-xNodeDc1gb").getLeg1().getTerminal())
                .setTargetVdc(500.)
                .setTargetP(-200.)
                .add();
        return Network.merge(dcNetwork, fr, gb);
    }

    public static Network createLccMonopoleGroundReturn() {
        return createLccMonopoleGroundReturn(NetworkFactory.findDefault());
    }

    public static Network createLccMonopoleGroundReturn(NetworkFactory networkFactory) {
        return createLccMonopoleBase(networkFactory, "LccMonopoleGroundReturn");
    }

    public static Network createLccMonopoleMetallicReturn() {
        return createLccMonopoleMetallicReturn(NetworkFactory.findDefault());
    }

    public static Network createLccMonopoleMetallicReturn(NetworkFactory networkFactory) {
        Network network = createLccMonopoleBase(networkFactory, "LccMonopoleMetallicReturn");
        network.getDcGround("dcGroundGb").getDcTerminal().setConnected(false);
        network.getSubnetwork("LccMonopoleMetallicReturn")
                .newDcLine()
                .setId("dcLine2")
                .setDcNode1Id("dcNodeFrNeg")
                .setConnected1(true)
                .setDcNode2Id("dcNodeGbNeg")
                .setConnected2(true)
                .setR(5.0)
                .add();
        return network;
    }

    private static Network createVscMonopoleBase(NetworkFactory networkFactory, String dcNetworkId) {
        Objects.requireNonNull(networkFactory);
        Objects.requireNonNull(dcNetworkId);

        Network dcNetwork = networkFactory.createNetwork(dcNetworkId, "test");
        Network fr = createSimpleAcNetworkWithDanglingLines(networkFactory, Country.FR, Map.of("xNodeDc1fr", 200.));
        Network gb = createSimpleAcNetworkWithDanglingLines(networkFactory, Country.GB, Map.of("xNodeDc1gb", -200.));
        addDcAcElements(dcNetwork, Country.FR, "xNodeDc1fr", -200., Mode.ONE_T2WT);
        addDcAcElements(dcNetwork, Country.GB, "xNodeDc1gb", 200., Mode.ONE_T2WT);

        DcNode dcNodeFrPos = dcNetwork.newDcNode()
                .setId("dcNodeFrPos")
                .setNominalV(250.)
                .add();
        DcNode dcNodeFrNeg = dcNetwork.newDcNode()
                .setId("dcNodeFrNeg")
                .setNominalV(250.)
                .add();
        DcNode dcNodeGbPos = dcNetwork.newDcNode()
                .setId("dcNodeGbPos")
                .setNominalV(250.)
                .add();
        DcNode dcNodeGbNeg = dcNetwork.newDcNode()
                .setId("dcNodeGbNeg")
                .setNominalV(250.)
                .add();
        dcNetwork.newDcLine()
                .setId("dcLinePos")
                .setDcNode1Id(dcNodeFrPos.getId())
                .setDcNode2Id(dcNodeGbPos.getId())
                .setR(5.0)
                .add();
        dcNetwork.newDcLine()
                .setId("dcLineNeg")
                .setDcNode1Id(dcNodeFrNeg.getId())
                .setDcNode2Id(dcNodeGbNeg.getId())
                .setR(5.0)
                .add();
        Terminal frPccTerminal = dcNetwork.getTwoWindingsTransformer("TRDC-FR-xNodeDc1fr").getTerminal1();
        dcNetwork.getVoltageLevel("VLDC-FR-xNodeDc1fr-150").newDcVoltageSourceConverter()
                .setId("VsFr")
                .setBus1("BUSDC-FR-xNodeDc1fr-150")
                .setDcNode1Id(dcNodeFrNeg.getId())
                .setDcNode2Id(dcNodeFrPos.getId())
                .setControlMode(DcConverter.ControlMode.V_DC)
                .setPccTerminal(frPccTerminal)
                .setTargetVdc(500.)
                .setTargetP(200.)
                .setVoltageRegulatorOn(false)
                .setReactivePowerSetpoint(0.0)
                .setVoltageSetpoint(400.)
                .setRegulatingTerminal(frPccTerminal)
                .add();
        Terminal gbPccTerminal = dcNetwork.getTwoWindingsTransformer("TRDC-GB-xNodeDc1gb").getTerminal1();
        dcNetwork.getVoltageLevel("VLDC-GB-xNodeDc1gb-150").newDcVoltageSourceConverter()
                .setId("VsGb")
                .setBus1("BUSDC-GB-xNodeDc1gb-150")
                .setDcNode1Id(dcNodeGbNeg.getId())
                .setDcNode2Id(dcNodeGbPos.getId())
                .setControlMode(DcConverter.ControlMode.P_PCC)
                .setPccTerminal(gbPccTerminal)
                .setTargetVdc(500.)
                .setTargetP(-200.)
                .setVoltageRegulatorOn(false)
                .setReactivePowerSetpoint(0.0)
                .setVoltageSetpoint(400.)
                .setRegulatingTerminal(gbPccTerminal)
                .add();
        return Network.merge(dcNetwork, fr, gb);
    }

    public static Network createVscSymmetricalMonopole() {
        return createVscSymmetricalMonopole(NetworkFactory.findDefault());
    }

    public static Network createVscSymmetricalMonopole(NetworkFactory networkFactory) {
        return createVscMonopoleBase(networkFactory, "VscSymmetricalMonopole");
    }

    public static Network createVscAsymmetricalMonopole() {
        return createVscAsymmetricalMonopole(NetworkFactory.findDefault());
    }

    public static Network createVscAsymmetricalMonopole(NetworkFactory networkFactory) {
        Network network = createVscMonopoleBase(networkFactory, "VscAsymmetricalMonopole");
        Network dcNetwork = network.getSubnetwork("VscAsymmetricalMonopole");
        dcNetwork.getDcLine("dcLineNeg").remove();
        dcNetwork.getDcNode("dcNodeFrPos").setNominalV(500.);
        dcNetwork.getDcNode("dcNodeGbPos").setNominalV(500.);
        dcNetwork.getDcNode("dcNodeFrNeg").setNominalV(1.);
        dcNetwork.getDcNode("dcNodeGbNeg").setNominalV(1.);
        dcNetwork.newDcGround()
                .setId("dcGroundFr")
                .setDcNodeId("dcNodeFrNeg")
                .setConnected(true)
                .setR(0.0)
                .add();
        dcNetwork.newDcGround()
                .setId("dcGroundGb")
                .setDcNodeId("dcNodeGbNeg")
                .setConnected(true)
                .setR(0.0)
                .add();
        return network;
    }
}
