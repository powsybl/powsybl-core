/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conformity.test;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.network.PhaseTapChangerAdder;
import com.powsybl.iidm.network.RatioTapChangerAdder;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.VoltageLevel;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class CgmesConformity1NetworkCatalog {

    public Network microBE() {
        String modelId = "urn:uuid:d400c631-75a0-4c30-8aed-832b0d282e73";
        Network expected = NetworkFactory.create(modelId, "no-format");
        Substation sBrussels = expected.newSubstation()
                .setId("_37e14a0f-5e34-4647-a062-8bfd9305fa9d")
                .setName("PP_Brussels")
                .setCountry(Country.BE)
                .setGeographicalTags("_c1d5bfc88f8011e08e4d00247eb1f55e") // ELIA-Brussels
                .add();
        Substation sAnvers = expected.newSubstation()
                .setId("_87f7002b-056f-4a6a-a872-1744eea757e3")
                .setName("Anvers")
                .setCountry(Country.BE)
                .setGeographicalTags("_c1d5c0378f8011e08e4d00247eb1f55e") // ELIA-Anvers
                .add();
        VoltageLevel vlBrussels21 = sBrussels.newVoltageLevel()
                .setId("_929ba893-c9dc-44d7-b1fd-30834bd3ab85")
                .setName("21.0")
                .setNominalV(21.0)
                .setLowVoltageLimit(18.9)
                .setHighVoltageLimit(23.1)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        VoltageLevel vlBrussels110 = sBrussels.newVoltageLevel()
                .setId("_8bbd7e74-ae20-4dce-8780-c20f8e18c2e0")
                .setName("110.0")
                .setNominalV(110.0)
                .setLowVoltageLimit(99.0)
                .setHighVoltageLimit(121.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        VoltageLevel vlBrussels10 = sBrussels.newVoltageLevel()
                .setId("_4ba71b59-ee2f-450b-9f7d-cc2f1cc5e386")
                .setName("10.5")
                .setNominalV(10.5)
                .setLowVoltageLimit(9.45)
                .setHighVoltageLimit(11.55)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        VoltageLevel vlBrussels380 = sBrussels.newVoltageLevel()
                .setId("_469df5f7-058f-4451-a998-57a48e8a56fe")
                .setName("380.0")
                .setNominalV(380.0)
                .setLowVoltageLimit(342.0)
                .setHighVoltageLimit(418.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        VoltageLevel vlBrussels225 = sBrussels.newVoltageLevel()
                .setId("_b10b171b-3bc5-4849-bb1f-61ed9ea1ec7c")
                .setName("225.0")
                .setNominalV(225.0)
                .setLowVoltageLimit(202.5)
                .setHighVoltageLimit(247.5)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        VoltageLevel vlAnvers220 = sAnvers.newVoltageLevel()
                .setId("_d0486169-2205-40b2-895e-b672ecb9e5fc")
                .setName("220.0")
                .setNominalV(225.0)
                .setLowVoltageLimit(202.5)
                .setHighVoltageLimit(247.5)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        Bus busAnvers220 = vlAnvers220.getBusBreakerView().newBus()
                .setId("_f70f6bad-eb8d-4b8f-8431-4ab93581514e")
                .add();
        busAnvers220.setV(224.871595);
        busAnvers220.setAngle(-7.624900);
        Load loadAnvers220 = vlAnvers220.newLoad()
                .setId("_b1480a00-b427-4001-a26c-51954d2bb7e9")
                .setName("L-1230804819")
                .setConnectableBus(busAnvers220.getId())
                .setBus(busAnvers220.getId())
                .setP0(1.0)
                .setQ0(0.0)
                .add();
        loadAnvers220.getTerminal().setP(1.0);
        loadAnvers220.getTerminal().setQ(0.0);
        vlAnvers220.newDanglingLine()
                .setId("_a16b4a6c-70b1-4abf-9a9d-bd0fa47f9fe4")
                .setName("BE-Line_7")
                .setConnectableBus(busAnvers220.getId())
                .setBus(busAnvers220.getId())
                .setP0(-26.805005)
                .setQ0(1.489867)
                .setR(4.6)
                .setX(69.0)
                .setG(5.75e-5)
                .setB(2.1677e-5)
                .setUcteXnodeCode("TN_Border_ST24")
                .add();
        vlAnvers220.newDanglingLine()
                .setId("_17086487-56ba-4979-b8de-064025a6b4da")
                .setName("BE-Line_1")
                .setConnectableBus(busAnvers220.getId())
                .setBus(busAnvers220.getId())
                .setP0(-27.365225)
                .setQ0(0.425626)
                .setR(2.2)
                .setX(68.2)
                .setG(3.08e-5)
                .setB(8.2938E-5)
                .setUcteXnodeCode("TN_Border_ST23")
                .add();
        Bus busBrussels225 = vlBrussels225.getBusBreakerView().newBus()
                .setId("_99b219f3-4593-428b-a4da-124a54630178")
                .add();
        busBrussels225.setV(224.315268);
        busBrussels225.setAngle(-8.770120);
        Load loadBrussels225 = vlBrussels225.newLoad()
                .setId("_1c6beed6-1acf-42e7-ba55-0cc9f04bddd8")
                .setName("BE-Load_2")
                .setConnectableBus("_99b219f3-4593-428b-a4da-124a54630178")
                .setBus("_99b219f3-4593-428b-a4da-124a54630178")
                .setP0(200.0)
                .setQ0(50.0)
                .add();
        loadBrussels225.getTerminal().setP(200.0);
        loadBrussels225.getTerminal().setQ(50.0);
        Bus busBrussels110 = vlBrussels110.getBusBreakerView().newBus()
                .setId("_5c74cb26-ce2f-40c6-951d-89091eb781b6")
                .add();
        busBrussels110.setV(115.5);
        busBrussels110.setAngle(-9.391330);
        Load loadBrussels110 = vlBrussels110.newLoad()
                .setId("_cb459405-cc14-4215-a45c-416789205904")
                .setName("BE-Load_1")
                .setConnectableBus(busBrussels110.getId())
                .setBus(busBrussels110.getId())
                .setP0(200.0)
                .setQ0(90.0)
                .add();
        loadBrussels110.getTerminal().setP(200.0);
        loadBrussels110.getTerminal().setQ(90.0);
        Bus busBrussels380 = vlBrussels380.getBusBreakerView().newBus()
                .setId("_e44141af-f1dc-44d3-bfa4-b674e5c953d7")
                .add();
        busBrussels380.setV(412.989001);
        busBrussels380.setAngle(-6.780710);
        ShuntCompensator shBrussels380 = vlBrussels380.newShunt()
                .setId("_002b0a40-3957-46db-b84a-30420083558f")
                .setName("BE_S2")
                .setConnectableBus(busBrussels380.getId())
                .setBus(busBrussels380.getId())
                .setbPerSection(3.46e-4)
                .setMaximumSectionCount(1)
                .setCurrentSectionCount(1)
                .add();
        shBrussels380.getTerminal().setQ(-59.058144);
        vlBrussels380.newDanglingLine()
                .setId("_78736387-5f60-4832-b3fe-d50daf81b0a6")
                .setName("BE-Line_3")
                .setConnectableBus(busBrussels380.getId())
                .setBus(busBrussels380.getId())
                .setP0(-46.816624)
                .setQ0(79.193778)
                .setR(1.05)
                .setX(12.0)
                .setG(6e-5)
                .setB(1.49854e-4)
                .setUcteXnodeCode("TN_Border_AL11")
                .add();
        vlBrussels380.newDanglingLine()
                .setId("_b18cd1aa-7808-49b9-a7cf-605eaf07b006")
                .setName("BE-Line_5")
                .setConnectableBus(busBrussels380.getId())
                .setBus(busBrussels380.getId())
                .setP0(-90.037004)
                .setQ0(148.603742)
                .setR(0.42)
                .setX(6.3)
                .setG(4.2e-5)
                .setB(6.59734E-5)
                .setUcteXnodeCode("TN_Border_GY11")
                .add();
        vlBrussels380.newDanglingLine()
                .setId("_ed0c5d75-4a54-43c8-b782-b20d7431630b")
                .setName("BE-Line_4")
                .setConnectableBus(busBrussels380.getId())
                .setBus(busBrussels380.getId())
                .setP0(-43.687227)
                .setQ0(84.876604)
                .setR(0.24)
                .setX(2.0)
                .setG(4e-5)
                .setB(2.51956e-5)
                .setUcteXnodeCode("TN_Border_MA11")
                .add();
        ShuntCompensator shBrussels110 = vlBrussels110.newShunt()
                .setId("_d771118f-36e9-4115-a128-cc3d9ce3e3da")
                .setName("BE_S1")
                .setConnectableBus(busBrussels110.getId())
                .setBus(busBrussels110.getId())
                .setbPerSection(0.024793)
                .setMaximumSectionCount(1)
                .setCurrentSectionCount(1)
                .add();
        shBrussels110.getTerminal().setQ(-330.75);
        Bus busBrussels21 = vlBrussels21.getBusBreakerView().newBus()
                .setId("_f96d552a-618d-4d0c-a39a-2dea3c411dee")
                .add();
        busBrussels21.setV(21.987000);
        busBrussels21.setAngle(-6.650800);
        {
            double p = -118;
            double q = -92.612077;
            Generator genBrussels21 = vlBrussels21.newGenerator()
                    .setId("_550ebe0d-f2b2-48c1-991f-cebea43a21aa")
                    .setName("Gen-1229753024")
                    .setConnectableBus(busBrussels21.getId())
                    .setBus(busBrussels21.getId())
                    .setMinP(50)
                    .setMaxP(200)
                    .setTargetP(-p)
                    .setTargetQ(-q)
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
        Bus busBrussels10 = vlBrussels10.getBusBreakerView().newBus()
                .setId("_a81d08ed-f51d-4538-8d1e-fb2d0dbd128e")
                .add();
        busBrussels10.setV(10.820805);
        busBrussels10.setAngle(-7.057180);
        // TODO Consider lines that are touching boundaries
        // expected.newLine()
        // .setId("17086487-56ba-4979-b8de-064025a6b4da")
        // .add();
        Line lineBE2 = expected.newLine()
                .setId("_b58bf21a-096a-4dae-9a01-3f03b60c24c7")
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
        lineBE2.newCurrentLimits1().setPermanentLimit(1443.0).add();
        lineBE2.newCurrentLimits2().setPermanentLimit(1443.0).add();
        // expected.newLine()
        // .setId("78736387-5f60-4832-b3fe-d50daf81b0a6")
        // .add();
        // expected.newLine()
        // .setId("ed0c5d75-4a54-43c8-b782-b20d7431630b")
        // .add();
        // expected.newLine()
        // .setId("b18cd1aa-7808-49b9-a7cf-605eaf07b006")
        // .add();
        Line lineBE6 = expected.newLine()
                .setId("_ffbabc27-1ccd-4fdc-b037-e341706c8d29")
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
        lineBE6.newCurrentLimits1().setPermanentLimit(1180.0).add();
        lineBE6.newCurrentLimits2().setPermanentLimit(1180.0).add();
        {
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
                    .setId("_e482b89a-fa84-4ea9-8e70-a83d44790957")
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
            tx.newCurrentLimits1().setPermanentLimit(1308.1).add();
            tx.newCurrentLimits2().setPermanentLimit(13746.4).add();
            int low = 1;
            int high = 33;
            int neutral = 17;
            double voltageInc = 0.8;
            Branch.Side side = Branch.Side.TWO;
            RatioTapChangerAdder rtca = tx.newRatioTapChanger()
                    .setLowTapPosition(low)
                    .setTapPosition(18);
            for (int k = low; k <= high; k++) {
                int n = k - neutral;
                double du = voltageInc / 100;
                double rhok = side.equals(Branch.Side.ONE) ? 1 / (1 + n * du) : (1 + n * du);
                double dz = 0;
                double dy = 0;
                if (side.equals(Branch.Side.TWO)) {
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
        }
        {
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
                    .setId("_b94318f6-6d24-4f56-96b9-df2531ad6543")
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
            txBE22.newCurrentLimits1().setPermanentLimit(1705.8).add();
            txBE22.newCurrentLimits2().setPermanentLimit(3411.6).add();
            int low = 1;
            int high = 25;
            int neutral = 13;
            double voltageInc = 1.25;
            Branch.Side side = Branch.Side.ONE;
            RatioTapChangerAdder rtca = txBE22.newRatioTapChanger()
                    .setLowTapPosition(low)
                    .setTapPosition(10);
            for (int k = low; k <= high; k++) {
                int n = k - neutral;
                double du = voltageInc / 100;
                double rhok = side.equals(Branch.Side.ONE) ? 1 / (1 + n * du) : (1 + n * du);
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
                    .setTargetV(Float.NaN)
                    // TODO Set the right regulation terminal
                    .setRegulationTerminal(txBE22.getTerminal(side));
            rtca.add();
        }
        TwoWindingsTransformer txBE21;
        {
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
            txBE21 = sBrussels.newTwoWindingsTransformer()
                    .setId("_a708c3bc-465d-4fe7-b6ef-6fa6408a62b0")
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
            txBE21.newCurrentLimits1().setPermanentLimit(938.2).add();
            txBE21.newCurrentLimits2().setPermanentLimit(3411.6).add();
            int low = 1;
            int high = 25;
            int neutral = 13;
            int position = 16;
            double xmin = 14.518904;
            double xmax = 14.518904;
            double voltageInc = 1.25;
            PhaseTapChangerAdder ptca = txBE21.newPhaseTapChanger()
                    .setLowTapPosition(low)
                    .setTapPosition(position);
            // Intermediate calculations made using double precision
            double du0 = 0;
            double du = voltageInc / 100;
            double theta = Math.PI / 2;
            LOG.debug("EXPECTED du0,du,theta {} {} {}", du0, du, theta);

            List<Double> alphas = new ArrayList<>();
            List<Double> rhos = new ArrayList<>();
            for (int k = low; k <= high; k++) {
                int n = k - neutral;
                double dx = (n * du - du0) * Math.cos(theta);
                double dy = (n * du - du0) * Math.sin(theta);
                double alpha = Math.atan2(dy, 1 + dx);
                double rho = 1 / Math.hypot(dy, 1 + dx);
                alphas.add(alpha);
                rhos.add(rho);
                LOG.debug("EXPECTED    n,dx,dy,alpha,rho  {} {} {} {} {}", n, dx, dy, alpha, rho);
            }
            double alphaMax = alphas.stream()
                    .mapToDouble(Double::doubleValue)
                    .max()
                    .orElse(Double.NaN);
            LOG.debug("EXPECTED    alphaMax {}", alphaMax);
            LOG.debug("EXPECTED    xStepMin, xStepMax {}, {}", xmin, xmax);
            for (int k = 0; k < alphas.size(); k++) {
                double alpha = alphas.get(k);
                double rho = rhos.get(k);
                // x for current k
                double numer = Math.sin(theta) - Math.tan(alphaMax) * Math.cos(theta);
                double denom = Math.sin(theta) - Math.tan(alpha) * Math.cos(theta);
                double xn = xmin + (xmax - xmin)
                        * Math.pow(Math.tan(alpha) / Math.tan(alphaMax) * numer / denom, 2);
                xn = xn * rho02;
                double dx = (xn - txBE21.getX()) / txBE21.getX() * 100;
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
            ptca.add();
        }
        {
            double p = -90;
            double q = 51.115627;
            Generator genBrussels10 = vlBrussels10.newGenerator()
                    .setId("_3a3b27be-b18b-4385-b557-6735d733baf0")
                    .setName("Gen-1229753060")
                    .setConnectableBus(busBrussels10.getId())
                    .setBus(busBrussels10.getId())
                    .setMinP(50)
                    .setMaxP(200)
                    .setTargetP(-p)
                    .setTargetQ(-q)
                    .setTargetV(115.5)
                    .setVoltageRegulatorOn(true)
                    // This generator regulates one end point of a power transformer
                    // (110 kV side of BE-TR2_1)
                    .setRegulatingTerminal(txBE21.getTerminal(Branch.Side.TWO))
                    .setRatedS(300)
                    .add();
            genBrussels10.newMinMaxReactiveLimits()
                    .setMinQ(0)
                    .setMaxQ(0)
                    .add();
            genBrussels10.getTerminal().setP(p);
            genBrussels10.getTerminal().setQ(q);
        }

        return expected;
    }

    private static final Logger LOG = LoggerFactory.getLogger(CgmesConformity1NetworkCatalog.class);
}
