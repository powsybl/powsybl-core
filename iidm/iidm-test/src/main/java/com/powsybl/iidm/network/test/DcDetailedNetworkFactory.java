/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.test;

import com.powsybl.iidm.network.*;

import java.util.Objects;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public final class DcDetailedNetworkFactory {

    private DcDetailedNetworkFactory() {
    }

    private static Network createBase2xnodes(NetworkFactory networkFactory, String networkId) {
        Objects.requireNonNull(networkFactory);

        Network dcNetwork = networkFactory.createNetwork(networkId, "test");
        Network fr = networkFactory.createNetwork("FR", "test");
        Substation sfr = fr.newSubstation()
                .setId("SFR")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vlfr = sfr.newVoltageLevel()
                .setId("VLFR")
                .setNominalV(400.0)
                .setLowVoltageLimit(380.0)
                .setHighVoltageLimit(420.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        Bus bfr = vlfr.getBusBreakerView().newBus()
                .setId("BFR")
                .add();
        vlfr.newGenerator()
                .setId("GFR")
                .setMinP(0.0)
                .setMaxP(4000.0)
                .setVoltageRegulatorOn(true)
                .setTargetV(400.0)
                .setTargetP(2000.0)
                .setTargetQ(0.0)
                .setBus(bfr.getId())
                .add();
        vlfr.newLoad()
                .setId("LFR")
                .setP0(1500.0)
                .setQ0(0.0)
                .setBus(bfr.getId())
                .add();
        vlfr.newDanglingLine()
                .setId("DLFR1")
                .setBus(bfr.getId())
                .setR(0.3)
                .setX(3.0)
                .setB(0.0)
                .setG(0.0)
                .setP0(500.0)
                .setQ0(0.0)
                .setPairingKey("XnodeDc1Fr")
                .add();

        Network gb = networkFactory.createNetwork("GB", "test");
        Substation sgb = gb.newSubstation()
                .setId("SGB")
                .setCountry(Country.GB)
                .add();
        VoltageLevel vlgb = sgb.newVoltageLevel()
                .setId("VLGB")
                .setNominalV(400.0)
                .setLowVoltageLimit(380.0)
                .setHighVoltageLimit(420.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        Bus bgb = vlgb.getBusBreakerView().newBus()
                .setId("BGB")
                .add();
        vlgb.newGenerator()
                .setId("GGB")
                .setMinP(0.0)
                .setMaxP(4000.0)
                .setVoltageRegulatorOn(true)
                .setTargetV(400.0)
                .setTargetP(2000.0)
                .setTargetQ(0.0)
                .setBus(bgb.getId())
                .add();
        vlgb.newLoad()
                .setId("LGB")
                .setP0(2500.0)
                .setQ0(0.0)
                .setBus("BGB")
                .add();
        vlgb.newDanglingLine()
                .setId("DLGB1")
                .setBus(bgb.getId())
                .setR(0.3)
                .setX(3.0)
                .setB(0.0)
                .setG(0.0)
                .setP0(-500.0)
                .setQ0(0.0)
                .setPairingKey("XnodeDc1Gb")
                .add();

        Substation sDcFr = dcNetwork.newSubstation()
                .setId("SDCFR")
                .add();
        VoltageLevel vldcfr400 = sDcFr.newVoltageLevel()
                .setId("VLDCFR400")
                .setNominalV(400.0)
                .setLowVoltageLimit(380.0)
                .setHighVoltageLimit(420.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        Bus bDcFr400 = vldcfr400.getBusBreakerView().newBus()
                .setId("BDCFR400")
                .add();
        vldcfr400.newDanglingLine()
                .setId("DLFR2")
                .setBus(bDcFr400.getId())
                .setR(0.3)
                .setX(3.0)
                .setB(0.0)
                .setG(0.0)
                .setP0(-500.0)
                .setQ0(0.0)
                .setPairingKey("XnodeDc1Fr")
                .add();
        VoltageLevel vldcfr150 = sDcFr.newVoltageLevel()
                .setId("VLDCFR150")
                .setNominalV(150.0)
                .setLowVoltageLimit(120.0)
                .setHighVoltageLimit(180.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        Bus bDcFr1501 = vldcfr150.getBusBreakerView().newBus()
                .setId("BDCFR150-1")
                .add();
        Bus bDcFr1502 = vldcfr150.getBusBreakerView().newBus()
                .setId("BDCFR150-2")
                .add();
        sDcFr.newTwoWindingsTransformer()
                .setId("TRDCFR1")
                .setVoltageLevel1(vldcfr400.getId())
                .setBus1(bDcFr400.getId())
                .setConnectableBus1(bDcFr400.getId())
                .setRatedU1(400.0)
                .setVoltageLevel2(vldcfr150.getId())
                .setBus2(bDcFr1501.getId())
                .setConnectableBus2(bDcFr1501.getId())
                .setRatedU2(150)
                .setR(0.1)
                .setX(5.0)
                .setG(0.0)
                .setB(0.0)
                .add();
        sDcFr.newTwoWindingsTransformer()
                .setId("TRDCFR2")
                .setVoltageLevel1(vldcfr400.getId())
                .setBus1(bDcFr400.getId())
                .setConnectableBus1(bDcFr400.getId())
                .setRatedU1(400.0)
                .setVoltageLevel2(vldcfr150.getId())
                .setBus2(bDcFr1502.getId())
                .setConnectableBus2(bDcFr1502.getId())
                .setRatedU2(150)
                .setR(0.1)
                .setX(5.0)
                .setG(0.0)
                .setB(0.0)
                .add();

        Substation sDcGb = dcNetwork.newSubstation()
                .setId("SDCGB")
                .add();
        VoltageLevel vldcgb400 = sDcGb.newVoltageLevel()
                .setId("VLDCGB400")
                .setNominalV(400.0)
                .setLowVoltageLimit(380.0)
                .setHighVoltageLimit(420.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        Bus bDcGb400 = vldcgb400.getBusBreakerView().newBus()
                .setId("BDCGB400")
                .add();
        vldcgb400.newDanglingLine()
                .setId("DLGB2")
                .setBus(bDcGb400.getId())
                .setR(0.3)
                .setX(3.0)
                .setB(0.0)
                .setG(0.0)
                .setP0(500.0)
                .setQ0(0.0)
                .setPairingKey("XnodeDc1Gb")
                .add();
        VoltageLevel vldcgb150 = sDcGb.newVoltageLevel()
                .setId("VLDCGB150")
                .setNominalV(150.0)
                .setLowVoltageLimit(120.0)
                .setHighVoltageLimit(180.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        Bus bDcGb1501 = vldcgb150.getBusBreakerView().newBus()
                .setId("BDCGB150-1")
                .add();
        Bus bDcGb1502 = vldcgb150.getBusBreakerView().newBus()
                .setId("BDCGB150-2")
                .add();
        sDcGb.newThreeWindingsTransformer()
                .setId("3WT")
                .setRatedU0(400.0)
                .newLeg1()
                .setR(0.1)
                .setX(0.0)
                .setG(0.0)
                .setB(0.0)
                .setRatedU(400.0)
                .setVoltageLevel(vldcgb400.getId())
                .setBus(bDcGb400.getId())
                .add()
                .newLeg2()
                .setR(0.1)
                .setX(5.0)
                .setG(0.0)
                .setB(0.0)
                .setRatedU(150.0)
                .setVoltageLevel(vldcgb150.getId())
                .setBus(bDcGb1501.getId())
                .add()
                .newLeg3()
                .setR(0.1)
                .setX(5.0)
                .setG(0.0)
                .setB(0.0)
                .setRatedU(150.0)
                .setVoltageLevel(vldcgb150.getId())
                .setBus(bDcGb1502.getId())
                .add()
                .add();

        return Network.merge(dcNetwork, fr, gb);
    }

    public static Network createLccMonopole() {
        return createLccMonopole(NetworkFactory.findDefault());
    }

    public static Network createLccMonopole(NetworkFactory networkFactory) {
        Network network = createBase2xnodes(networkFactory, "CsMonopole");
        Network dcNetwork = network.getSubnetwork("CsMonopole");
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
        dcNetwork.getVoltageLevel("VLDCFR150").newDcLineCommutatedConverter()
                .setId("CsFr")
                .setBus1("BDCFR150-1")
                .setBus2("BDCFR150-2")
                .setDcNode1Id(dcNodeFrNeg.getId())
                .setDcNode2Id(dcNodeFrPos.getId())
                .add();
        dcNetwork.getVoltageLevel("VLDCGB150").newDcLineCommutatedConverter()
                .setId("CsGb")
                .setBus1("BDCGB150-1")
                .setBus2("BDCGB150-2")
                .setDcNode1Id(dcNodeGbNeg.getId())
                .setDcNode2Id(dcNodeGbPos.getId())
                .add();
        return network;
    }
}
