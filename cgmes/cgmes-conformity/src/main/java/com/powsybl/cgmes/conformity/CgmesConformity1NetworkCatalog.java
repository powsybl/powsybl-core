/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conformity;

import com.powsybl.iidm.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public final class CgmesConformity1NetworkCatalog {

    private static final String CL_0 = "CL-0";
    private static final String CL_1 = "CL-1";
    private static final String CL_2 = "CL-2";
    private static final String VOLTAGE_LEVEL_ID_1 = "469df5f7-058f-4451-a998-57a48e8a56fe";
    private static final String VOLTAGE_LEVEL_ID_2 = "d0486169-2205-40b2-895e-b672ecb9e5fc";
    private static final String BUS_ID_1 = "f70f6bad-eb8d-4b8f-8431-4ab93581514e";
    private static final String BUS_ID_2 = "99b219f3-4593-428b-a4da-124a54630178";
    private static final String DANGLING_LINE_ID_1 = "17086487-56ba-4979-b8de-064025a6b4da";
    private static final String SHUNT_ID_1 = "002b0a40-3957-46db-b84a-30420083558f";
    private static final String TWT_ID_1 = "b94318f6-6d24-4f56-96b9-df2531ad6543";
    private static final String TWT_ID_2 = "a708c3bc-465d-4fe7-b6ef-6fa6408a62b0";

    private CgmesConformity1NetworkCatalog() {
    }

    private static Network microBE(String modelId) {
        Network network = Network.create(modelId, "no-format");

        Substation sBrussels = network.newSubstation()
                .setId("37e14a0f-5e34-4647-a062-8bfd9305fa9d")
                .setName("PP_Brussels")
                .setCountry(Country.BE)
                .setGeographicalTags("ELIA-Brussels") // _c1d5bfc88f8011e08e4d00247eb1f55e
                .add();
        sBrussels.setProperty("CGMES.subRegionId", "c1d5bfc88f8011e08e4d00247eb1f55e");
        Substation sAnvers = network.newSubstation()
                .setId("87f7002b-056f-4a6a-a872-1744eea757e3")
                .setName("Anvers")
                .setCountry(Country.BE)
                .setGeographicalTags("ELIA-Anvers") // _c1d5c0378f8011e08e4d00247eb1f55e
                .add();
        sAnvers.setProperty("CGMES.subRegionId", "c1d5c0378f8011e08e4d00247eb1f55e");
        VoltageLevel vlBrussels21 = sBrussels.newVoltageLevel()
                .setId("929ba893-c9dc-44d7-b1fd-30834bd3ab85")
                .setName("21.0")
                .setNominalV(21.0)
                .setLowVoltageLimit(18.9)
                .setHighVoltageLimit(23.1)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        VoltageLevel vlBrussels110 = sBrussels.newVoltageLevel()
                .setId("8bbd7e74-ae20-4dce-8780-c20f8e18c2e0")
                .setName("110.0")
                .setNominalV(110.0)
                .setLowVoltageLimit(99.0)
                .setHighVoltageLimit(121.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        VoltageLevel vlBrussels10 = sBrussels.newVoltageLevel()
                .setId("4ba71b59-ee2f-450b-9f7d-cc2f1cc5e386")
                .setName("10.5")
                .setNominalV(10.5)
                .setLowVoltageLimit(9.45)
                .setHighVoltageLimit(11.55)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        VoltageLevel vlBrussels380 = sBrussels.newVoltageLevel()
                .setId(VOLTAGE_LEVEL_ID_1)
                .setName("380.0")
                .setNominalV(380.0)
                .setLowVoltageLimit(342.0)
                .setHighVoltageLimit(418.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        VoltageLevel vlBrussels225 = sBrussels.newVoltageLevel()
                .setId("b10b171b-3bc5-4849-bb1f-61ed9ea1ec7c")
                .setName("225.0")
                .setNominalV(225.0)
                .setLowVoltageLimit(202.5)
                .setHighVoltageLimit(247.5)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        VoltageLevel vlAnvers220 = sAnvers.newVoltageLevel()
                .setId(VOLTAGE_LEVEL_ID_2)
                .setName("220.0")
                .setNominalV(225.0)
                .setLowVoltageLimit(202.5)
                .setHighVoltageLimit(247.5)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        Bus busAnvers220 = vlAnvers220.getBusBreakerView().newBus()
                .setId(BUS_ID_1)
                .setName("BE-Busbar_2")
                .add();
        busAnvers220.setV(224.871595);
        busAnvers220.setAngle(-7.624900);
        Load loadAnvers220 = vlAnvers220.newLoad()
                .setId("b1480a00-b427-4001-a26c-51954d2bb7e9")
                .setName("L-1230804819")
                .setConnectableBus(busAnvers220.getId())
                .setBus(busAnvers220.getId())
                .setP0(1.0)
                .setQ0(0.0)
                .add();
        loadAnvers220.getTerminal().setP(1.0);
        loadAnvers220.getTerminal().setQ(0.0);
        DanglingLine be7 = vlAnvers220.newDanglingLine()
                .setId("a16b4a6c-70b1-4abf-9a9d-bd0fa47f9fe4")
                .setName("BE-Line_7")
                .setConnectableBus(busAnvers220.getId())
                .setBus(busAnvers220.getId())
                .setP0(-26.805006)
                .setQ0(1.489867)
                .setR(4.6)
                .setX(69.0)
                .setG(5.75e-5)
                .setB(2.1677e-5)
                .setPairingKey("TN_Border_ST24")
                .add();
        be7.newCurrentLimits().setPermanentLimit(1180)
                .beginTemporaryLimit()
                    .setName(CL_0)
                    .setValue(1312.0)
                    .setAcceptableDuration(20)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                    .setName(CL_1)
                    .setValue(1443.0)
                    .setAcceptableDuration(10)
                .endTemporaryLimit()
                .add();

        DanglingLine be1 = vlAnvers220.newDanglingLine()
                .setId(DANGLING_LINE_ID_1)
                .setName("BE-Line_1")
                .setConnectableBus(busAnvers220.getId())
                .setBus(busAnvers220.getId())
                .setP0(-27.365225)
                .setQ0(0.425626)
                .setR(2.2)
                .setX(68.2)
                .setG(3.08e-5)
                .setB(8.2938E-5)
                .setPairingKey("TN_Border_ST23")
                .add();

        Bus busBrussels225 = vlBrussels225.getBusBreakerView().newBus()
                .setId(BUS_ID_2)
                .setName("BE_TR_BUS4")
                .add();
        busBrussels225.setV(224.315268);
        busBrussels225.setAngle(-8.770120);
        Load loadBrussels225 = vlBrussels225.newLoad()
                .setId("1c6beed6-1acf-42e7-ba55-0cc9f04bddd8")
                .setName("BE-Load_2")
                .setConnectableBus(BUS_ID_2)
                .setBus(BUS_ID_2)
                .setP0(200.0)
                .setQ0(50.0)
                .add();
        loadBrussels225.getTerminal().setP(200.0);
        loadBrussels225.getTerminal().setQ(50.0);
        Bus busBrussels110 = vlBrussels110.getBusBreakerView().newBus()
                .setId("5c74cb26-ce2f-40c6-951d-89091eb781b6")
                .setName("BE-Busbar_6")
                .add();
        busBrussels110.setV(115.5);
        busBrussels110.setAngle(-9.391330);
        Load loadBrussels110 = vlBrussels110.newLoad()
                .setId("cb459405-cc14-4215-a45c-416789205904")
                .setName("BE-Load_1")
                .setConnectableBus(busBrussels110.getId())
                .setBus(busBrussels110.getId())
                .setP0(200.0)
                .setQ0(90.0)
                .add();
        loadBrussels110.getTerminal().setP(200.0);
        loadBrussels110.getTerminal().setQ(90.0);
        Bus busBrussels380 = vlBrussels380.getBusBreakerView().newBus()
                .setId("e44141af-f1dc-44d3-bfa4-b674e5c953d7")
                .setName("BE_TR_BUS2")
                .add();
        busBrussels380.setV(412.989001);
        busBrussels380.setAngle(-6.780710);
        ShuntCompensator shBrussels380 = vlBrussels380.newShuntCompensator()
                .setId(SHUNT_ID_1)
                .setName("BE_S2")
                .setConnectableBus(busBrussels380.getId())
                .setBus(busBrussels380.getId())
                .setSectionCount(1)
                .newLinearModel()
                    .setBPerSection(3.46e-4)
                    .setGPerSection(7.0e-6)
                    .setMaximumSectionCount(1)
                    .add()
                .setTargetV(380.0)
                .setTargetDeadband(0.5)
                .setVoltageRegulatorOn(false)
                .add();
        shBrussels380.getTerminal().setQ(-59.058144);
        DanglingLine be3 = vlBrussels380.newDanglingLine()
                .setId("78736387-5f60-4832-b3fe-d50daf81b0a6")
                .setName("BE-Line_3")
                .setConnectableBus(busBrussels380.getId())
                .setBus(busBrussels380.getId())
                .setP0(-46.816625)
                .setQ0(79.193778)
                .setR(1.05)
                .setX(12.0)
                .setG(6e-5)
                .setB(1.49854e-4)
                .setPairingKey("TN_Border_AL11")
                .add();
        be3.newCurrentLimits().setPermanentLimit(1371)
                .beginTemporaryLimit()
                    .setName(CL_0)
                    .setValue(1443.0)
                    .setAcceptableDuration(20)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                    .setName(CL_1)
                    .setValue(1515.0)
                    .setAcceptableDuration(10)
                .endTemporaryLimit()
                .add();

        DanglingLine be5 = vlBrussels380.newDanglingLine()
                .setId("b18cd1aa-7808-49b9-a7cf-605eaf07b006")
                .setName("BE-Line_5")
                .setConnectableBus(busBrussels380.getId())
                .setBus(busBrussels380.getId())
                .setP0(-90.037005)
                .setQ0(148.603743)
                .setR(0.42)
                .setX(6.3)
                .setG(4.2e-5)
                .setB(6.59734E-5)
                .setPairingKey("TN_Border_GY11")
                .add();
        be5.newCurrentLimits().setPermanentLimit(1804)
                .beginTemporaryLimit()
                    .setName(CL_0)
                    .setValue(1876.0)
                    .setAcceptableDuration(20)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                    .setName(CL_1)
                    .setValue(1948.0)
                    .setAcceptableDuration(10)
                .endTemporaryLimit()
                .add();

        DanglingLine be4 = vlBrussels380.newDanglingLine()
                .setId("ed0c5d75-4a54-43c8-b782-b20d7431630b")
                .setName("BE-Line_4")
                .setConnectableBus(busBrussels380.getId())
                .setBus(busBrussels380.getId())
                .setP0(-43.687227)
                .setQ0(84.876604)
                .setR(0.24)
                .setX(2.0)
                .setG(4e-5)
                .setB(2.51956e-5)
                .setPairingKey("TN_Border_MA11")
                .add();
        be4.newCurrentLimits().setPermanentLimit(1226)
                .beginTemporaryLimit()
                    .setName(CL_0)
                    .setValue(1299.0)
                    .setAcceptableDuration(20)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                    .setName(CL_1)
                    .setValue(1371.0)
                    .setAcceptableDuration(10)
                .endTemporaryLimit()
                .add();

        ShuntCompensator shBrussels110 = vlBrussels110.newShuntCompensator()
                .setId("d771118f-36e9-4115-a128-cc3d9ce3e3da")
                .setName("BE_S1")
                .setConnectableBus(busBrussels110.getId())
                .setBus(busBrussels110.getId())
                .setSectionCount(1)
                .newLinearModel()
                    .setBPerSection(0.024793)
                    .setGPerSection(0.0)
                    .setMaximumSectionCount(1)
                    .add()
                .add();
        shBrussels110.getTerminal().setQ(-330.75);
        shBrussels110.setTargetV(110.0);
        shBrussels110.setTargetDeadband(0.5);
        shBrussels110.setVoltageRegulatorOn(false);
        Bus busBrussels21 = vlBrussels21.getBusBreakerView().newBus()
                .setId("f96d552a-618d-4d0c-a39a-2dea3c411dee")
                .setName("BE-Busbar_5")
                .add();
        busBrussels21.setV(21.987000);
        busBrussels21.setAngle(-6.650800);
        addGenBrussels21(vlBrussels21, busBrussels21);
        Bus busBrussels10 = vlBrussels10.getBusBreakerView().newBus()
                .setId("a81d08ed-f51d-4538-8d1e-fb2d0dbd128e")
                .setName("BE-Busbar_4")
                .add();
        busBrussels10.setV(10.820805);
        busBrussels10.setAngle(-7.057180);
        // TODO Consider lines that are touching boundaries
        // expected.newLine()
        // .setId(DANGLING_LINE_ID_1)
        // .add();
        Line lineBE2 = network.newLine()
                .setId("b58bf21a-096a-4dae-9a01-3f03b60c24c7")
                .setName("BE-Line_2")
                .setR(1.935)
                .setX(34.2)
                .setB1(2.120575e-5)
                .setG1(3.375e-5)
                .setB2(2.120575e-5)
                .setG2(3.375e-5)
                .setConnectableBus1(busBrussels225.getId())
                .setBus1(busBrussels225.getId())
                .setVoltageLevel1(vlBrussels225.getId())
                .setConnectableBus2(busAnvers220.getId())
                .setBus2(busAnvers220.getId())
                .setVoltageLevel2(vlAnvers220.getId())
                .add();
        lineBE2.newCurrentLimits1().setPermanentLimit(1443.0)
                .beginTemporaryLimit()
                    .setName(CL_0)
                    .setValue(1574.0)
                    .setAcceptableDuration(20)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                    .setName(CL_1)
                    .setValue(1705.0)
                    .setAcceptableDuration(10)
                .endTemporaryLimit()
                .add();
        lineBE2.newCurrentLimits2().setPermanentLimit(1443.0)
                .beginTemporaryLimit()
                    .setName(CL_0)
                    .setValue(1574.0)
                    .setAcceptableDuration(20)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                    .setName(CL_1)
                    .setValue(1705.0)
                    .setAcceptableDuration(10)
                .endTemporaryLimit()
                .add();
        // expected.newLine()
        // .setId("78736387-5f60-4832-b3fe-d50daf81b0a6")
        // .add();
        // expected.newLine()
        // .setId("ed0c5d75-4a54-43c8-b782-b20d7431630b")
        // .add();
        // expected.newLine()
        // .setId("b18cd1aa-7808-49b9-a7cf-605eaf07b006")
        // .add();
        Line lineBE6 = network.newLine()
                .setId("ffbabc27-1ccd-4fdc-b037-e341706c8d29")
                .setName("BE-Line_6")
                .setR(5.203)
                .setX(71.0)
                .setB1(1.000595e-5)
                .setG1(0.6e-4)
                .setB2(1.000595e-5)
                .setG2(0.6e-4)
                .setConnectableBus1(busBrussels225.getId())
                .setBus1(busBrussels225.getId())
                .setVoltageLevel1(vlBrussels225.getId())
                .setConnectableBus2(busAnvers220.getId())
                .setBus2(busAnvers220.getId())
                .setVoltageLevel2(vlAnvers220.getId())
                .add();
        lineBE6.newCurrentLimits1().setPermanentLimit(1180.0)
                .beginTemporaryLimit()
                    .setName(CL_0)
                    .setValue(1312.0)
                    .setAcceptableDuration(20)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                    .setName(CL_1)
                    .setValue(1443.0)
                    .setAcceptableDuration(10)
                .endTemporaryLimit()
                .add();
        lineBE6.newCurrentLimits2().setPermanentLimit(1180.0)
                .beginTemporaryLimit()
                    .setName(CL_0)
                    .setValue(1312.0)
                    .setAcceptableDuration(20)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                    .setName(CL_1)
                    .setValue(1443.0)
                    .setAcceptableDuration(10)
                .endTemporaryLimit()
                .add();
        addTransformerBrussels110Brussels10(sBrussels, busBrussels110, busBrussels10, vlBrussels110, vlBrussels10);
        addTransformerBrussels225Brussels110(sBrussels, busBrussels225, busBrussels110, vlBrussels225, vlBrussels110);
        TwoWindingsTransformer txBE21 = addTransformerBrussels380Brussels110(sBrussels, busBrussels380, busBrussels110, vlBrussels380, vlBrussels110);
        addGeneratorBrussels10(vlBrussels10, busBrussels10, txBE21);
        ThreeWindingsTransformer txBETR3 = addTWTBrussels380Brussels225Brussels21(sBrussels,
            busBrussels380, busBrussels225, busBrussels21,
            vlBrussels380, vlBrussels225, vlBrussels21);
        return network;
    }

    private static void addGenBrussels21(VoltageLevel vlBrussels21, Bus busBrussels21) {
        double p = -118;
        double targetQ = 18.720301;
        double q = -92.612077;

        Generator genBrussels21 = vlBrussels21.newGenerator()
            .setId("550ebe0d-f2b2-48c1-991f-cebea43a21aa")
            .setName("BE-G2")
            .setConnectableBus(busBrussels21.getId())
            .setBus(busBrussels21.getId())
            .setMinP(50)
            .setMaxP(200)
            .setTargetP(-p)
            .setTargetQ(targetQ)
            .setTargetV(21.987)
            .setVoltageRegulatorOn(true)
            .setRatedS(300)
            .add();
        genBrussels21.newMinMaxReactiveLimits()
            .setMinQ(-200)
            .setMaxQ(200)
            .add();
        genBrussels21.getTerminal().setP(p);
        genBrussels21.getTerminal().setQ(q);
    }

    private static Generator addGeneratorBrussels10(VoltageLevel vlBrussels10, Bus busBrussels10, TwoWindingsTransformer txBE21) {
        double p = -90;
        double targetQ = 100.256;
        double q = 51.115627;
        Generator genBrussels10 = vlBrussels10.newGenerator()
            .setId("3a3b27be-b18b-4385-b557-6735d733baf0")
            .setName("BE-G1")
            .setConnectableBus(busBrussels10.getId())
            .setBus(busBrussels10.getId())
            .setMinP(50)
            .setMaxP(200)
            .setTargetP(-p)
            .setTargetQ(targetQ)
            .setTargetV(115.5)
            .setVoltageRegulatorOn(true)
            // This generator regulates one end point of a power transformer
            // (110 kV side of BE-TR2_1)
            .setRegulatingTerminal(txBE21.getTerminal(TwoSides.TWO))
            .setRatedS(300)
            .add();
        ReactiveCapabilityCurveAdder rcca = genBrussels10.newReactiveCapabilityCurve();
        rcca.beginPoint()
            .setP(-100.0)
            .setMinQ(-200.0)
            .setMaxQ(200.0)
            .endPoint();
        rcca.beginPoint()
            .setP(0.0)
            .setMinQ(-300.0)
            .setMaxQ(300.0)
            .endPoint();
        rcca.beginPoint()
            .setP(100.0)
            .setMinQ(-200.0)
            .setMaxQ(200.0)
            .endPoint();
        rcca.add();
        genBrussels10.getTerminal().setP(p);
        genBrussels10.getTerminal().setQ(q);

        return genBrussels10;
    }

    private static TwoWindingsTransformer addTransformerBrussels110Brussels10(Substation sBrussels,
                                                            Bus busBrussels110, Bus busBrussels10,
                                                            VoltageLevel vlBrussels110, VoltageLevel vlBrussels10) {
        double u1 = 110.34375;
        double u2 = 10.5;
        double rho = u2 / u1;
        double rho2 = rho * rho;
        double r1 = 0.104711;
        double x1 = 5.843419;
        double g1 = 1.73295e-5;
        double b1 = -8.30339e-5;
        double r2 = 0.0;
        double x2 = 0.0;
        double g2 = 0.0;
        double b2 = 0.0;
        double r = r1 * rho2 + r2;
        double x = x1 * rho2 + x2;
        double g = g1 / rho2 + g2;
        double b = b1 / rho2 + b2;
        TwoWindingsTransformer tx = sBrussels.newTwoWindingsTransformer()
            .setId("e482b89a-fa84-4ea9-8e70-a83d44790957")
            .setName("BE-TR2_3")
            .setR(r)
            .setX(x)
            .setG(g)
            .setB(b)
            .setConnectableBus1(busBrussels110.getId())
            .setBus1(busBrussels110.getId())
            .setConnectableBus2(busBrussels10.getId())
            .setBus2(busBrussels10.getId())
            .setVoltageLevel1(vlBrussels110.getId())
            .setVoltageLevel2(vlBrussels10.getId())
            .setRatedU1(u1)
            .setRatedU2(u2)
            .add();
        tx.newCurrentLimits1().setPermanentLimit(1308.1)
            .beginTemporaryLimit()
            .setName(CL_0)
            .setValue(1408.1)
            .setAcceptableDuration(20)
            .endTemporaryLimit()
            .beginTemporaryLimit()
            .setName(CL_1)
            .setValue(1508.1)
            .setAcceptableDuration(10)
            .endTemporaryLimit()
            .add();
        tx.newCurrentLimits2().setPermanentLimit(13746.4)
            .beginTemporaryLimit()
            .setName(CL_0)
            .setValue(14746.4)
            .setAcceptableDuration(20)
            .endTemporaryLimit()
            .beginTemporaryLimit()
            .setName(CL_1)
            .setValue(15746.4)
            .setAcceptableDuration(10)
            .endTemporaryLimit()
            .add();
        int low = 1;
        int high = 33;
        int neutral = 17;
        double voltageInc = 0.8;
        TwoSides side = TwoSides.TWO;
        RatioTapChangerAdder rtca = tx.newRatioTapChanger()
            .setLowTapPosition(low)
            .setTapPosition(14)
            .setTargetDeadband(0.5);
        for (int k = low; k <= high; k++) {
            int n = k - neutral;
            double du = voltageInc / 100;
            double rhok = side.equals(TwoSides.ONE) ? 1 / (1 + n * du) : (1 + n * du);
            double dz = 0;
            double dy = 0;
            if (side.equals(TwoSides.TWO)) {
                double rhok2 = rhok * rhok;
                dz = (rhok2 - 1) * 100;
                dy = (1 / rhok2 - 1) * 100;
            }
            rtca.beginStep()
                .setRho(rhok)
                .setR(dz)
                .setX(dz)
                .setG(dy)
                .setB(dy)
                .endStep();
        }
        rtca.setLoadTapChangingCapabilities(true)
            .setRegulating(true)
            .setTargetV(10.815)
            // TODO Set the right regulation terminal
            .setRegulationTerminal(tx.getTerminal(side));
        rtca.add();

        return tx;
    }

    private static TwoWindingsTransformer addTransformerBrussels225Brussels110(Substation sBrussels,
                                                             Bus busBrussels225, Bus busBrussels110,
                                                             VoltageLevel vlBrussels225, VoltageLevel vlBrussels110) {
        double u1 = 220.0;
        double u2 = 110.0;
        double rho = u2 / u1;
        double rho2 = rho * rho;
        double r1 = 0.8228;
        double x1 = 11.138883;
        double g1 = 0.0;
        double b1 = 0.0;
        double r2 = 0.0;
        double x2 = 0.0;
        double g2 = 0.0;
        double b2 = 0.0;
        double r = r1 * rho2 + r2;
        double x = x1 * rho2 + x2;
        double g = g1 / rho2 + g2;
        double b = b1 / rho2 + b2;
        TwoWindingsTransformer txBE22 = sBrussels.newTwoWindingsTransformer()
            .setId(TWT_ID_1)
            .setName("BE-TR2_2")
            .setR(r)
            .setX(x)
            .setG(g)
            .setB(b)
            .setConnectableBus1(busBrussels225.getId())
            .setBus1(busBrussels225.getId())
            .setConnectableBus2(busBrussels110.getId())
            .setBus2(busBrussels110.getId())
            .setVoltageLevel1(vlBrussels225.getId())
            .setVoltageLevel2(vlBrussels110.getId())
            .setRatedU1(u1)
            .setRatedU2(u2)
            .add();
        txBE22.newCurrentLimits2().setPermanentLimit(3411.6)
            .beginTemporaryLimit()
            .setName(CL_0)
            .setValue(3611.6)
            .setAcceptableDuration(20)
            .endTemporaryLimit()
            .beginTemporaryLimit()
            .setName(CL_1)
            .setValue(3811.6)
            .setAcceptableDuration(10)
            .endTemporaryLimit()
            .add();
        int low = 1;
        int high = 25;
        int neutral = 13;
        double voltageInc = 1.25;
        TwoSides side = TwoSides.ONE;
        RatioTapChangerAdder rtca = txBE22.newRatioTapChanger()
            .setLowTapPosition(low)
            .setTapPosition(10);
        for (int k = low; k <= high; k++) {
            int n = k - neutral;
            double du = voltageInc / 100;
            double rhok = side.equals(TwoSides.ONE) ? 1 / (1 + n * du) : (1 + n * du);
            rtca.beginStep()
                .setRho(rhok)
                .setR(0)
                .setX(0)
                .setG(0)
                .setB(0)
                .endStep();
        }
        rtca.setLoadTapChangingCapabilities(true)
            .setRegulating(false)
            .setTargetV(0.0)
            .setTargetDeadband(0.5)
            .setRegulationTerminal(txBE22.getTerminal2());
        rtca.add();

        return txBE22;
    }

    private static TwoWindingsTransformer addTransformerBrussels380Brussels110(Substation sBrussels,
                                                                               Bus busBrussels380, Bus busBrussels110,
                                                                               VoltageLevel vlBrussels380, VoltageLevel vlBrussels110) {
        double u1 = 400.0;
        double u2 = 110.0;
        double rho0 = u2 / u1;
        double rho02 = rho0 * rho0;
        double r1 = 2.707692;
        double x1 = 14.518904;
        double g1 = 0.0;
        double b1 = 0.0;
        double r2 = 0.0;
        double x2 = 0.0;
        double g2 = 0.0;
        double b2 = 0.0;
        double r = r1 * rho02 + r2;
        double x = x1 * rho02 + x2;
        double g = g1 / rho02 + g2;
        double b = b1 / rho02 + b2;
        TwoWindingsTransformer txBE21 = sBrussels.newTwoWindingsTransformer()
            .setId(TWT_ID_2)
            .setName("BE-TR2_1")
            .setR(r)
            .setX(x)
            .setG(g)
            .setB(b)
            .setConnectableBus1(busBrussels380.getId())
            .setBus1(busBrussels380.getId())
            .setConnectableBus2(busBrussels110.getId())
            .setBus2(busBrussels110.getId())
            .setVoltageLevel1(vlBrussels380.getId())
            .setVoltageLevel2(vlBrussels110.getId())
            .setRatedU1(u1)
            .setRatedU2(u2)
            .add();
        txBE21.newCurrentLimits2().setPermanentLimit(3411.6)
            .beginTemporaryLimit()
            .setName(CL_0)
            .setValue(3611.6)
            .setAcceptableDuration(20)
            .endTemporaryLimit()
            .beginTemporaryLimit()
            .setName(CL_1)
            .setValue(3811.6)
            .setAcceptableDuration(10)
            .endTemporaryLimit()
            .add();
        int low = 1;
        int high = 25;
        int neutral = 13;
        int position = 10;
        double xmin = 14.518904;
        double xmax = 14.518904;
        double voltageInc = 1.25;
        double windingConnectionAngle = 90;
        addPhaseTapChanger(txBE21,
            PhaseTapChangerType.ASYMMETRICAL,
            low, high, neutral, position,
            xmin, xmax,
            voltageInc, windingConnectionAngle,
            PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL,
            true, -65.0, 35.0);

        return txBE21;
    }

    private static ThreeWindingsTransformer addTWTBrussels380Brussels225Brussels21(Substation sBrussels,
                                                                                   Bus busBrussels380, Bus busBrussels225, Bus busBrussels21,
                                                                                   VoltageLevel vlBrussels380, VoltageLevel vlBrussels225, VoltageLevel vlBrussels21) {
        double ratedU1 = 400.0;
        double ratedU2 = 220.0;
        double ratedU3 = 21.0;
        double ratedU0 = ratedU1;
        double r1 = 0.898462;
        double x1 = 17.204128;
        double g1 = 0.0;
        double b1 = 0.0000024375;
        double r2 = 0.323908;
        double x2 = 5.949086;
        double g2 = 0.0;
        double b2 = 0.0;
        double r3 = 0.013332;
        double x3 = 0.059978;
        double g3 = 0.0;
        double b3 = 0.0;
        ThreeWindingsTransformer txBETR3 = sBrussels.newThreeWindingsTransformer()
            .setId("84ed55f4-61f5-4d9d-8755-bba7b877a246")
            .setName("BE-TR3_1")
            .newLeg1()
            .setRatedU(ratedU1)
            .setR(r1)
            .setX(x1)
            .setG(g1)
            .setB(b1)
            .setConnectableBus(busBrussels380.getId())
            .setBus(busBrussels380.getId())
            .setVoltageLevel(vlBrussels380.getId())
            .add()
            .newLeg2()
            .setRatedU(ratedU2)
            .setR(r2 * (ratedU0 / ratedU2) * (ratedU0 / ratedU2))
            .setX(x2 * (ratedU0 / ratedU2) * (ratedU0 / ratedU2))
            .setG(g2)
            .setB(b2)
            .setConnectableBus(busBrussels225.getId())
            .setBus(busBrussels225.getId())
            .setVoltageLevel(vlBrussels225.getId())
            .add()
            .newLeg3()
            .setRatedU(ratedU3)
            .setR(r3 * (ratedU0 / ratedU3) * (ratedU0 / ratedU3))
            .setX(x3 * (ratedU0 / ratedU3) * (ratedU0 / ratedU3))
            .setG(g3)
            .setB(b3)
            .setConnectableBus(busBrussels21.getId())
            .setBus(busBrussels21.getId())
            .setVoltageLevel(vlBrussels21.getId())
            .add()
            .add();
        txBETR3.getLeg1().newCurrentLimits()
            .setPermanentLimit(938.2)
            .beginTemporaryLimit()
            .setAcceptableDuration(20)
            .setName(CL_0)
            .setValue(968.2)
            .endTemporaryLimit()
            .beginTemporaryLimit()
            .setAcceptableDuration(10)
            .setName(CL_1)
            .setValue(998.2)
            .endTemporaryLimit()
            .add();
        txBETR3.getLeg2().newCurrentLimits()
            .setPermanentLimit(1705.8)
            .beginTemporaryLimit()
            .setAcceptableDuration(20)
            .setName(CL_0)
            .setValue(1805.8)
            .endTemporaryLimit()
            .beginTemporaryLimit()
            .setAcceptableDuration(10)
            .setName(CL_1)
            .setValue(1905.8)
            .endTemporaryLimit()
            .add();
        txBETR3.getLeg3().newCurrentLimits()
            .setPermanentLimit(17870.4)
            .beginTemporaryLimit()
            .setAcceptableDuration(20)
            .setName(CL_0)
            .setValue(18870.4)
            .endTemporaryLimit()
            .beginTemporaryLimit()
            .setAcceptableDuration(10)
            .setName(CL_1)
            .setValue(19870.4)
            .endTemporaryLimit()
            .add();

        int low = 1;
        int high = 33;
        int neutral = 17;
        int position = 17;
        double voltageInc = 0.625;
        RatioTapChangerAdder rtca = txBETR3.getLeg2().newRatioTapChanger()
            .setLowTapPosition(low)
            .setTapPosition(position);
        for (int k = low; k <= high; k++) {
            int n = k - neutral;
            double du = voltageInc / 100;
            double rhok = 1 / (1 + n * du);
            double dz = 0;
            double dy = 0;
            rtca.beginStep()
                .setRho(rhok)
                .setR(dz)
                .setX(dz)
                .setG(dy)
                .setB(dy)
                .endStep();
        }
        rtca.setLoadTapChangingCapabilities(true)
            .setRegulating(false)
            .setTargetV(0.0)
            .setTargetDeadband(0.5);
        rtca.add();

        return txBETR3;
    }

    public static Network microBaseCaseBE() {
        String modelId = "urn:uuid:d400c631-75a0-4c30-8aed-832b0d282e73";
        Network network = microBE(modelId);
        DanglingLine be1 = network.getDanglingLine(DANGLING_LINE_ID_1);
        be1.newCurrentLimits().setPermanentLimit(1443)
                .beginTemporaryLimit()
                    .setName("CL-4")
                    .setValue(1500.0)
                    .setAcceptableDuration(30)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                    .setName("CL-3")
                    .setValue(1550.0)
                    .setAcceptableDuration(25)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                    .setName(CL_0)
                    .setValue(1574.0)
                    .setAcceptableDuration(20)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                    .setName(CL_1)
                    .setValue(1705.0)
                    .setAcceptableDuration(10)
                .endTemporaryLimit()
                .add();
        TwoWindingsTransformer txBE21 = network.getTwoWindingsTransformer(TWT_ID_2);
        txBE21.newCurrentLimits1().setPermanentLimit(938.2)
                .beginTemporaryLimit()
                    .setName(CL_0)
                    .setValue(958.2)
                    .setAcceptableDuration(20)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                    .setName(CL_1)
                    .setValue(998.2)
                    .setAcceptableDuration(10)
                .endTemporaryLimit()
                .add();
        TwoWindingsTransformer txBE22 = network.getTwoWindingsTransformer(TWT_ID_1);
        txBE22.newCurrentLimits1().setPermanentLimit(1705.8)
                .beginTemporaryLimit()
                    .setName(CL_0)
                    .setValue(1805.8)
                    .setAcceptableDuration(20)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                    .setName(CL_1)
                    .setValue(1905.8)
                    .setAcceptableDuration(10)
                .endTemporaryLimit()
                .add();
        return network;
    }

    public static Network microType4BE() {
        String modelId = "urn:uuid:96adadbe-902b-4cd6-9fc8-01a56ecbee79";
        Network network = microBE(modelId);
        // Add voltage level in Anvers
        VoltageLevel vlAnvers225 = network.getSubstation("87f7002b-056f-4a6a-a872-1744eea757e3")
                .newVoltageLevel()
                .setId("69ef0dbd-da79-4eef-a02f-690cb8a28361")
                .setName("225.0")
                .setNominalV(225.0)
                .setLowVoltageLimit(202.5)
                .setHighVoltageLimit(247.5)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        Bus busAnvers225 = vlAnvers225.getBusBreakerView().newBus()
                .setId("23b65c6b-2351-4673-89e9-1895c7291543")
                .setName("Series Compensator")
                .add()
                .setV(223.435281)
                .setAngle(-17.412200);

        Bus busBrussels21 = network
                .getVoltageLevel("929ba893-c9dc-44d7-b1fd-30834bd3ab85")
                .getBusBreakerView()
                .getBus("f96d552a-618d-4d0c-a39a-2dea3c411dee")
                .setV(21.987000)
                .setAngle(-20.588300);
        Bus busBrussels110 = network
                .getVoltageLevel("8bbd7e74-ae20-4dce-8780-c20f8e18c2e0")
                .getBusBreakerView()
                .getBus("5c74cb26-ce2f-40c6-951d-89091eb781b6")
                .setV(115.5)
                .setAngle(-22.029800);
        Bus busBrussels10 = network
                .getVoltageLevel("4ba71b59-ee2f-450b-9f7d-cc2f1cc5e386")
                .getBusBreakerView()
                .getBus("a81d08ed-f51d-4538-8d1e-fb2d0dbd128e")
                .setV(10.816961)
                .setAngle(-19.642100);

        Bus busBrussels380 = network
                .getVoltageLevel(VOLTAGE_LEVEL_ID_1)
                .getBusBreakerView()
                .getBus("e44141af-f1dc-44d3-bfa4-b674e5c953d7")
                .setV(414.114413)
                .setAngle(-21.526500);

        Bus busBrussels225 = network
                .getVoltageLevel("b10b171b-3bc5-4849-bb1f-61ed9ea1ec7c")
                .getBusBreakerView()
                .getBus(BUS_ID_2)
                .setV(224.156562)
                .setAngle(-21.796200);

        Bus busAnvers220 = network
                .getVoltageLevel(VOLTAGE_LEVEL_ID_2)
                .getBusBreakerView()
                .getBus(BUS_ID_1)
                .setV(223.435281)
                .setAngle(-17.412200);

        VoltageLevel vlAnvers220 = network.getVoltageLevel(VOLTAGE_LEVEL_ID_2);
        vlAnvers220.newStaticVarCompensator()
                .setId("3c69652c-ff14-4550-9a87-b6fdaccbb5f4")
                .setName("SVC-1230797516")
                .setBus(BUS_ID_1)
                .setConnectableBus(BUS_ID_1)
                .setBmax(1 / 5062.5)
                .setBmin(1 / (-5062.5))
                .setRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE)
                .setVoltageSetpoint(229.5)
                .add();

        double p = -118.0;
        double targetQ = 18.720301;
        double q = -85.603401;
        Generator genBrussels21 = network
                .getGenerator("550ebe0d-f2b2-48c1-991f-cebea43a21aa")
                .setTargetP(-p)
                .setTargetQ(targetQ);
        genBrussels21.getTerminal().setP(p).setQ(q);

        p = -90.0;
        targetQ = 100.256;
        q = 84.484905;
        Generator genBrussels10 = network
                .getGenerator("3a3b27be-b18b-4385-b557-6735d733baf0")
                .setTargetP(-p)
                .setTargetQ(targetQ);
        genBrussels10.getTerminal().setP(p).setQ(q);

        // Line _df16b3dd comes from a SeriesCompensator in CGMES model
        Line scAnvers = network.newLine()
                .setId("df16b3dd-c905-4a6f-84ee-f067be86f5da")
                .setName("SER-RLC-1230822986")
                .setR(0)
                .setX(-31.830989)
                .setB1(0)
                .setG1(0)
                .setB2(0)
                .setG2(0)
                .setConnectableBus1(busAnvers220.getId())
                .setBus1(busAnvers220.getId())
                .setVoltageLevel1(vlAnvers220.getId())
                .setConnectableBus2(busAnvers225.getId())
                .setBus2(busAnvers225.getId())
                .setVoltageLevel2(vlAnvers225.getId())
                .add();

        network.getTwoWindingsTransformer("e482b89a-fa84-4ea9-8e70-a83d44790957")
                .getRatioTapChanger().setTapPosition(14);

        TwoWindingsTransformer txBE22 = network.getTwoWindingsTransformer(TWT_ID_1);
        txBE22.getRatioTapChanger().remove();
        addPhaseTapChangerOnTxBE22(txBE22);
        txBE22.newCurrentLimits1().setPermanentLimit(1705.8)
                .beginTemporaryLimit()
                    .setName(CL_2)
                    .setValue(1805.8)
                    .setAcceptableDuration(20)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                    .setName(CL_1)
                    .setValue(1905.8)
                    .setAcceptableDuration(10)
                .endTemporaryLimit()
                .add();
        txBE22.newCurrentLimits2().setPermanentLimit(3411.6)
                .beginTemporaryLimit()
                    .setName(CL_2)
                    .setValue(3611.6)
                    .setAcceptableDuration(20)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                    .setName(CL_1)
                    .setValue(3811.6)
                    .setAcceptableDuration(10)
                .endTemporaryLimit()
                .add();

        TwoWindingsTransformer txBE21 = network.getTwoWindingsTransformer(TWT_ID_2);
        txBE21.getPhaseTapChanger().remove();
        addPhaseTapChangerOnTxBE21(txBE21);
        txBE21.newCurrentLimits1().setPermanentLimit(938.2)
                .beginTemporaryLimit()
                    .setName(CL_2)
                    .setValue(958.2)
                    .setAcceptableDuration(20)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                    .setName(CL_1)
                    .setValue(998.2)
                    .setAcceptableDuration(10)
                .endTemporaryLimit()
                .add();
        txBE21.newCurrentLimits2().setPermanentLimit(3411.6)
                .beginTemporaryLimit()
                    .setName(CL_2)
                    .setValue(3611.6)
                    .setAcceptableDuration(20)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                    .setName(CL_1)
                    .setValue(3811.6)
                    .setAcceptableDuration(10)
                .endTemporaryLimit()
                .add();

        network.getDanglingLine("a16b4a6c-70b1-4abf-9a9d-bd0fa47f9fe4")
                .setP0(-86.814383)
                .setQ0(4.958972);
        network.getDanglingLine(DANGLING_LINE_ID_1)
                .setP0(-89.462903)
                .setQ0(1.519011)
                .newCurrentLimits()
                    .setPermanentLimit(1443)
                    .beginTemporaryLimit()
                        .setName(CL_0)
                        .setValue(1574.0)
                        .setAcceptableDuration(20)
                    .endTemporaryLimit()
                    .beginTemporaryLimit()
                        .setName(CL_1)
                        .setValue(1705.0)
                        .setAcceptableDuration(10)
                    .endTemporaryLimit()
                .add();
        network.getDanglingLine("78736387-5f60-4832-b3fe-d50daf81b0a6")
                .setP0(-16.452662)
                .setQ0(64.018020);
        network.getDanglingLine("b18cd1aa-7808-49b9-a7cf-605eaf07b006")
                .setP0(-31.579291)
                .setQ0(120.813763);
        network.getDanglingLine("ed0c5d75-4a54-43c8-b782-b20d7431630b")
                .setP0(-11.518776)
                .setQ0(67.377544);

        network.getShuntCompensator(SHUNT_ID_1).remove();
        network.getVoltageLevel(VOLTAGE_LEVEL_ID_1)
                .newShuntCompensator()
                    .setId(SHUNT_ID_1)
                    .setName("BE_S2")
                    .setConnectableBus(busBrussels380.getId())
                    .setBus(busBrussels380.getId())
                    .setSectionCount(1)
                    .newNonLinearModel()
                        .beginSection()
                            .setB(3.46e-4)
                            .setG(7.0e-6)
                        .endSection()
                        .beginSection()
                            .setB(5.19E-4)
                            .setG(9.0E-6)
                        .endSection()
                        .beginSection()
                            .setB(6.58E-4)
                            .setG(9.999999999999999E-6)
                        .endSection()
                        .beginSection()
                            .setB(7.27E-4)
                            .setG(1.06E-5)
                        .endSection()
                        .beginSection()
                            .setB(7.620000000000001E-4)
                            .setG(1.09E-5)
                        .endSection()
                    .add()
                .setTargetV(380.0)
                .setTargetDeadband(0.5)
                .setVoltageRegulatorOn(false)
                .add();
        return network;
    }

    private static void addPhaseTapChangerOnTxBE22(TwoWindingsTransformer txBE22) {
        int low = 1;
        int high = 25;
        int neutral = 13;
        int position = 10;
        double xmin = 10.396291;
        double xmax = 11.881475;
        double voltageInc = 1.25;
        double windingConnectionAngle = 5;
        addPhaseTapChanger(txBE22,
            PhaseTapChangerType.ASYMMETRICAL,
            low, high, neutral, position,
            xmin, xmax,
            voltageInc, windingConnectionAngle,
            PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL, false,
            0.0, 0.5);
    }

    private static void addPhaseTapChangerOnTxBE21(TwoWindingsTransformer txBE21) {
        int low = 1;
        int high = 25;
        int neutral = 13;
        int position = 10;
        double xmin = 12.099087;
        double xmax = 16.938722;
        double voltageInc = 1.25;
        // winding connection angle property is only defined for Asymmetrical
        double windingConnectionAngle = Double.NaN;
        addPhaseTapChanger(txBE21,
            PhaseTapChangerType.SYMMETRICAL,
            low, high, neutral, position,
            xmin, xmax,
            voltageInc, windingConnectionAngle,
            PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL, true,
            -65.0, 35.0);
    }

    enum PhaseTapChangerType {
        ASYMMETRICAL, SYMMETRICAL
    };

    private static void addPhaseTapChanger(
            TwoWindingsTransformer tx,
            PhaseTapChangerType type,
            int low, int high, int neutral, int position,
            double xmin, double xmax,
            double voltageInc,
            double windingConnectionAngle,
            PhaseTapChanger.RegulationMode mode, boolean regulating,
            double regulationValue, double targetDeadband) {
        LOG.debug("EXPECTED tx {}", tx.getId());
        double ratio0 = tx.getRatedU2() / tx.getRatedU1();
        double ratio02 = ratio0 * ratio0;

        PhaseTapChangerAdder ptca = tx.newPhaseTapChanger()
                .setLowTapPosition(low)
                .setTapPosition(position);
        // Intermediate calculations made using double precision
        double du0 = 0;
        double du = voltageInc / 100;
        double theta = Math.toRadians(
                type == PhaseTapChangerType.ASYMMETRICAL
                        ? windingConnectionAngle
                        : 90);
        LOG.debug("EXPECTED du0,du,theta {} {} {}", du0, du, theta);

        List<Double> angles = new ArrayList<>();
        List<Double> ratios = new ArrayList<>();
        for (int k = low; k <= high; k++) {
            int n = k - neutral;
            double angle;
            double ratio;
            if (type == PhaseTapChangerType.ASYMMETRICAL) {
                double dx = (n * du - du0) * Math.cos(theta);
                double dy = (n * du - du0) * Math.sin(theta);
                angle = Math.atan2(dy, 1 + dx);
                ratio = Math.hypot(dy, 1 + dx);
                LOG.debug("EXPECTED    n,dx,dy,angle,ratio  {} {} {} {} {}", n, dx, dy, angle, ratio);
            } else if (type == PhaseTapChangerType.SYMMETRICAL) {
                double dy = (n * du / 2 - du0) * Math.sin(theta);
                angle = 2 * Math.atan(dy);
                ratio = 1.0;
                LOG.debug("EXPECTED    n,dy,angle,ratio  {} {} {} {}", n, dy, angle, ratio);
            } else {
                angle = Double.NaN;
                ratio = Double.NaN;
            }
            angles.add(angle);
            ratios.add(ratio);
        }
        // Use ratio, not rho to calculate step x
        double angleMax = angles.stream()
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(Double.NaN);
        LOG.debug("EXPECTED    angleMax {}", angleMax);
        LOG.debug("EXPECTED    xStepMin, xStepMax {}, {}", xmin, xmax);
        LOG.debug("EXPECTED    u2,u1,ratio0square {}, {}, {}", tx.getRatedU2(), tx.getRatedU1(), ratio02);
        for (int k = 0; k < angles.size(); k++) {
            double angle = angles.get(k);
            double ratio = ratios.get(k);

            // x for current k
            double xn;
            if (type == PhaseTapChangerType.ASYMMETRICAL) {
                double numer = Math.sin(theta) - Math.tan(angleMax) * Math.cos(theta);
                double denom = Math.sin(theta) - Math.tan(angle) * Math.cos(theta);
                xn = xmin + (xmax - xmin)
                        * Math.pow(Math.tan(angle) / Math.tan(angleMax) * numer / denom, 2);
            } else if (type == PhaseTapChangerType.SYMMETRICAL) {
                xn = xmin + (xmax - xmin)
                        * Math.pow(Math.sin(angle / 2) / Math.sin(angleMax / 2), 2);
            } else {
                xn = Double.NaN;
            }
            xn = xn * ratio02;
            double dx = (xn - tx.getX()) / tx.getX() * 100;

            double alpha = -angle;
            double rho = 1 / ratio;

            ptca.beginStep()
                    .setRho(rho)
                    .setAlpha(Math.toDegrees(alpha))
                    .setR(0)
                    .setX(dx)
                    .setG(0)
                    .setB(0)
                    .endStep();
            if (LOG.isDebugEnabled()) {
                int n = (low + k) - neutral;
                LOG.debug("EXPECTED    n,rho,alpha,x,dx   {} {} {} {} {}",
                        n, rho, Math.toDegrees(alpha), xn, dx);
            }
        }
        ptca.setRegulating(regulating)
                .setRegulationMode(mode)
                .setRegulationValue(regulationValue)
                .setTargetDeadband(targetDeadband)
                .setRegulationTerminal(tx.getTerminal2())
                .add();
    }

    private static final Logger LOG = LoggerFactory.getLogger(CgmesConformity1NetworkCatalog.class);
}
