/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.test.cim14;

import com.powsybl.cgmes.model.GridModelReference;
import com.powsybl.cgmes.model.test.Cim14SmallCasesCatalog;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.serde.XMLImporter;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
final class Cim14SmallCasesNetworkCatalog {

    private Cim14SmallCasesNetworkCatalog() {
    }

    static Network smallcase1() {
        String sGenGeoTag = "1 ";
        String sInfGeoTag = "1 ";
        String genName = "GEN     ";
        String genInfName = "INF     ";
        Network network = Network.create("unknown", "no-format");
        Substation sGen = network.newSubstation()
            .setId("GEN______SS")
            .setName("GEN     _SS")
            .setGeographicalTags(sGenGeoTag)
            .add();
        sGen.setProperty("CGMES.subRegionId", "SGR_1_");
        Substation sInf = network.newSubstation()
            .setId("INF______SS")
            .setName("INF     _SS")
            .setGeographicalTags(sInfGeoTag)
            .add();
        sInf.setProperty("CGMES.subRegionId", "SGR_1_");
        VoltageLevel vlInf = sInf.newVoltageLevel()
            .setId("INF______VL")
            .setName("INF     _VL")
            .setNominalV(380.0)
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();
        VoltageLevel vlGrid = sGen.newVoltageLevel()
            .setId("GRID_____VL")
            .setName("GRID    _VL")
            .setNominalV(380.0)
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();
        VoltageLevel vlGen = sGen.newVoltageLevel()
            .setId("GEN______VL")
            .setName("GEN     _VL")
            .setNominalV(21.0)
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();
        Bus busGrid = vlGrid.getBusBreakerView().newBus()
            .setId("GRID_____TN")
            .setName("GRID")
            .add();
        busGrid.setV(419);
        busGrid.setAngle(0);
        Bus busGen = vlGen.getBusBreakerView().newBus()
            .setId("GEN______TN")
            .setName("GEN")
            .add();
        busGen.setV(21);
        busGen.setAngle(0);
        Generator gen = vlGen.newGenerator()
            .setId("GEN______SM")
            .setName(genName)
            .setConnectableBus(busGen.getId())
            .setBus(busGen.getId())
            .setMinP(-999)
            .setMaxP(999)
            .setTargetP(-0.0)
            .setTargetQ(-0.0)
            .setTargetV(21.0)
            .setVoltageRegulatorOn(true)
            .add();
        gen.newMinMaxReactiveLimits()
            .setMinQ(-999)
            .setMaxQ(999)
            .add();
        gen.getTerminal().setP(0);
        gen.getTerminal().setQ(0);
        gen.setRegulatingTerminal(gen.getTerminal());
        Bus busInf = vlInf.getBusBreakerView().newBus()
            .setId("INF______TN")
            .setName("INF")
            .add();
        busInf.setV(419);
        busInf.setAngle(0);
        Generator genInf = vlInf.newGenerator()
            .setId("INF______SM")
            .setName(genInfName)
            .setConnectableBus(busInf.getId())
            .setBus(busInf.getId())
            .setMinP(-999999.0)
            .setMaxP(999999.0)
            .setTargetP(-0.0)
            .setTargetQ(-0.0)
            .setTargetV(419.0)
            .setVoltageRegulatorOn(true)
            .add();
        genInf.newMinMaxReactiveLimits()
            .setMinQ(-999999.0)
            .setMaxQ(999999.0)
            .add();
        genInf.getTerminal().setP(0);
        genInf.getTerminal().setQ(0);
        genInf.setRegulatingTerminal(genInf.getTerminal());
        Line line = network.newLine()
            .setId("GRID____-INF_____-1_AC")
            .setName("GRID    -INF     -1")
            .setR(0.0)
            .setX(86.64)
            .setG1(0.0)
            .setB1(0.0)
            .setG2(0.0)
            .setB2(0.0)
            .setConnectableBus1(busGrid.getId())
            .setBus1(busGrid.getId())
            .setConnectableBus2(busInf.getId())
            .setBus2(busInf.getId())
            .setVoltageLevel1(vlGrid.getId())
            .setVoltageLevel2(vlInf.getId())
            .add();
        line.newCurrentLimits1().setPermanentLimit(9116.06).add();
        {
            double u2 = 419.0;
            double u1 = 21.0;
            double rho = u2 / u1;
            double rho2 = rho * rho;
            double r1 = 0.001323;
            double x1 = 0.141114;
            double g1 = 0.0;
            double b1 = -0.0;
            double r2 = 0.0;
            double x2 = 0.0;
            double g2 = 0.0;
            double b2 = 0.0;
            double r = r1 * rho2 + r2;
            double x = x1 * rho2 + x2;
            double g = g1 / rho2 + g2;
            double b = b1 / rho2 + b2;
            TwoWindingsTransformer tx = sGen.newTwoWindingsTransformer()
                .setId("GEN_____-GRID____-1_PT")
                .setName("GEN     -GRID    -1")
                .setR(r)
                .setX(x)
                .setG(g)
                .setB(b)
                .setConnectableBus1(busGen.getId())
                .setBus1(busGen.getId())
                .setConnectableBus2(busGrid.getId())
                .setBus2(busGrid.getId())
                .setVoltageLevel1(vlGen.getId())
                .setVoltageLevel2(vlGrid.getId())
                .setRatedU1(u1)
                .setRatedU2(u2)
                .add();
            tx.newCurrentLimits1().setPermanentLimit(13746.4).add();
            tx.newCurrentLimits2().setPermanentLimit(759.671).add();
        }

        return network;
    }

    static Network ieee14() {
        return loadNetwork(Cim14SmallCasesCatalog.ieee14());
    }

    static Network nordic32() {
        return loadNetwork(Cim14SmallCasesCatalog.nordic32());
    }

    static Network m7buses() {
        return loadNetwork(Cim14SmallCasesCatalog.m7buses());
    }

    static Network txMicroBEAdapted() {
        return loadNetwork(Cim14SmallCasesCatalog.txMicroBEAdapted());
    }

    private static Network loadNetwork(GridModelReference gm) {
        XMLImporter xmli = new XMLImporter();
        ReadOnlyDataSource ds = new ResourceDataSource(gm.name(), new ResourceSet("/cim14", gm.name() + ".xiidm"));
        return xmli.importData(ds, NetworkFactory.findDefault(), null);
    }
}
