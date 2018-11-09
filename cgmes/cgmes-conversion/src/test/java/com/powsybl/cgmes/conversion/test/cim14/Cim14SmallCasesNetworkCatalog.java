/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.test.cim14;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.util.Arrays;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.jimfs.Jimfs;
import com.powsybl.cgmes.model.test.TestGridModel;
import com.powsybl.cgmes.model.test.cim14.Cim14SmallCasesCatalog;
import com.powsybl.cim1.converter.CIM1Importer;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.VoltageLevel;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class Cim14SmallCasesNetworkCatalog {

    public Network smallcase1() {
        Country defaultCountry = Country.AF;
        String sGenGeoTag = "_SGR_1_";
        String sInfGeoTag = "_SGR_1_";
        String genName = "GEN     ";
        String genInfName = "INF     ";
        Network network = NetworkFactory.create("unknown", "no-format");
        Substation sGen = network.newSubstation()
                .setId("_GEN______SS")
                .setName("GEN     _SS")
                .setCountry(defaultCountry)
                .setGeographicalTags(sGenGeoTag)
                .add();
        Substation sInf = network.newSubstation()
                .setId("_INF______SS")
                .setName("INF     _SS")
                .setCountry(defaultCountry)
                .setGeographicalTags(sInfGeoTag)
                .add();
        VoltageLevel vlInf = sInf.newVoltageLevel()
                .setId("_INF______VL")
                .setName("INF     _VL")
                .setNominalV(380.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        VoltageLevel vlGrid = sGen.newVoltageLevel()
                .setId("_GRID_____VL")
                .setName("GRID    _VL")
                .setNominalV(380.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        VoltageLevel vlGen = sGen.newVoltageLevel()
                .setId("_GEN______VL")
                .setName("GEN     _VL")
                .setNominalV(21.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        Bus busGrid = vlGrid.getBusBreakerView().newBus()
                .setId("_GRID_____TN")
                .add();
        busGrid.setV(419);
        busGrid.setAngle(0);
        Bus busGen = vlGen.getBusBreakerView().newBus()
                .setId("_GEN______TN")
                .add();
        busGen.setV(21);
        busGen.setAngle(0);
        Generator gen = vlGen.newGenerator()
                .setId("_GEN______SM")
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
                .setId("_INF______TN")
                .add();
        busInf.setV(419);
        busInf.setAngle(0);
        Generator genInf = vlInf.newGenerator()
                .setId("_INF______SM")
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
                .setId("_GRID____-INF_____-1_AC")
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
                    .setId("_GEN_____-GRID____-1_PT")
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

    public Network ieee14() {
        return cimImport(catalog.ieee14());
    }

    public Network nordic32() {
        return cimImport(catalog.nordic32());
    }

    public Network m7buses() {
        return cimImport(catalog.m7buses());
    }

    public Network txMicroBEAdapted() {
        return cimImport(catalog.txMicroBEAdapted());
    }

    private Network cimImport(TestGridModel gm) {
        try {
            return cimImport1(gm);
        } catch (IOException e) {
            throw new PowsyblException("failed to import CIM1 model " + gm.name(), e);
        }
    }

    private Network cimImport1(TestGridModel gm) throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem()) {

            DataSource ds = gm.dataSourceBasedOn(fs);
            // Always include in the data source the required boundaries
            resourceToDataSource("ENTSO-E_Boundary_Set_EU_EQ.xml", ds);
            resourceToDataSource("ENTSO-E_Boundary_Set_EU_TP.xml", ds);

            Set<String> names1 = ds.listNames(".*");
            if (LOG.isInfoEnabled()) {
                LOG.info("List of names in data source for CIM1Importer = {}", Arrays.toString(names1.toArray()));
            }
            return new CIM1Importer().importData(ds, null);
        }
    }

    private void resourceToDataSource(String name, DataSource dataSource) throws IOException {
        try (OutputStream stream = dataSource.newOutputStream(name, false)) {
            IOUtils.copy(getClass().getResourceAsStream("/" + name), stream);
        }
    }

    private final Cim14SmallCasesCatalog catalog = new Cim14SmallCasesCatalog();

    private static final Logger LOG = LoggerFactory.getLogger(Cim14SmallCasesNetworkCatalog.class);
}
