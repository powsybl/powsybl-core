/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.test.conformity;

import com.powsybl.cgmes.conformity.Cgmes3Catalog;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.test.ConversionTester;
import com.powsybl.cgmes.conversion.test.network.compare.Comparison;
import com.powsybl.cgmes.conversion.test.network.compare.ComparisonConfig;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesModelFactory;
import com.powsybl.cgmes.model.test.TestGridModel;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.LoadingLimits.TemporaryLimit;
import com.powsybl.triplestore.api.TripleStoreFactory;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class Cgmes3ConversionTest {

    @Test
    public void loadNetworkMicroGrid() {
        // Check that CGMES importer supports check existence of valid CGMES 3 (CIM100) files
        CgmesImport importer = new CgmesImport();
        ReadOnlyDataSource ds = Cgmes3Catalog.microGrid().dataSource();
        assertTrue(importer.exists(ds));
        Network network = importer.importData(ds, NetworkFactory.findDefault(), null);
        assertNotNull(network);
    }

    @Test
    public void microGrid() throws IOException {
        Properties importParams = new Properties();
        ConversionTester t = new ConversionTester(
            importParams,
            TripleStoreFactory.onlyDefaultImplementation(),
            new ComparisonConfig());
        Network expected = null;
        t.testConversion(expected, Cgmes3Catalog.microGrid());
    }

    @Test
    public void microGridConvertBoundary() throws IOException {
        Properties importParams = new Properties();
        importParams.put(CgmesImport.CONVERT_BOUNDARY, "true");
        ConversionTester t = new ConversionTester(
            importParams,
            TripleStoreFactory.onlyDefaultImplementation(),
            new ComparisonConfig());
        Network expected = null;
        t.testConversion(expected, Cgmes3Catalog.microGrid());
    }

    @Test
    public void microGridOperationalLimits() throws IOException {
        Network n = networkModel(Cgmes3Catalog.microGrid(), new Conversion.Config());

        Line ln = n.getLine("ffbabc27-1ccd-4fdc-b037-e341706c8d29");
        assertEquals(1312.0, ln.getCurrentLimits1().map(LoadingLimits::getPermanentLimit).orElse(0.0), 0.0);
        assertEquals(1312.0, ln.getCurrentLimits2().map(LoadingLimits::getPermanentLimit).orElse(0.0), 0.0);

        assertEquals(1, (int) ln.getCurrentLimits1().map(lim -> lim.getTemporaryLimits().size()).orElse(-1));
        TemporaryLimit lntl1 = ln.getCurrentLimits1().flatMap(lim -> lim.getTemporaryLimits().stream().findFirst()).orElseThrow(AssertionError::new);
        assertEquals(500.0, lntl1.getValue(), 0.0);
        assertEquals(10, lntl1.getAcceptableDuration());

        assertEquals(1, (int) ln.getCurrentLimits2().map(lim -> lim.getTemporaryLimits().size()).orElse(-1));
        TemporaryLimit lntl2 = ln.getCurrentLimits2().flatMap(lim -> lim.getTemporaryLimits().stream().findFirst()).orElseThrow(AssertionError::new);
        assertEquals(500.0, lntl2.getValue(), 0.0);
        assertEquals(10, lntl2.getAcceptableDuration());

        Line tln = n.getLine("dad02278-bd25-476f-8f58-dbe44be72586 + ed0c5d75-4a54-43c8-b782-b20d7431630b");
        assertEquals(1371.0, tln.getCurrentLimits1().map(LoadingLimits::getPermanentLimit).orElse(0.0), 0.0);
        assertEquals(1226.0, tln.getCurrentLimits2().map(LoadingLimits::getPermanentLimit).orElse(0.0), 0.0);

        assertEquals(1, (int) tln.getCurrentLimits1().map(lim -> lim.getTemporaryLimits().size()).orElse(-1));
        TemporaryLimit tlntl1 = tln.getCurrentLimits1().flatMap(lim -> lim.getTemporaryLimits().stream().findFirst()).orElseThrow(AssertionError::new);
        assertEquals(500.0, tlntl1.getValue(), 0.0);
        assertEquals(10, tlntl1.getAcceptableDuration());

        assertEquals(1, (int) tln.getCurrentLimits2().map(lim -> lim.getTemporaryLimits().size()).orElse(-1));
        TemporaryLimit tlntl2 = ln.getCurrentLimits2().flatMap(lim -> lim.getTemporaryLimits().stream().findFirst()).orElseThrow(AssertionError::new);
        assertEquals(500.0, tlntl2.getValue(), 0.0);
        assertEquals(10, tlntl2.getAcceptableDuration());
    }

    @Test
    public void microGridWithAndWithoutTpSv() {
        Network network = new CgmesImport().importData(
            Cgmes3Catalog.microGrid().dataSource(),
            NetworkFactory.findDefault(), null);

        Network networkwithoutTpSv = new CgmesImport().importData(
            Cgmes3Catalog.microGridWithoutTpSv().dataSource(),
            NetworkFactory.findDefault(), null);

        resetBusVoltageAndAngleBeforeComparison(network);
        resetTerminalPQofLoadsAndGeneratorsBeforeComparison(network);
        new Comparison(network, networkwithoutTpSv, new ComparisonConfig()).compare();
        assertTrue(true);
    }

    @Test
    public void miniGrid() throws IOException {
        Properties importParams = new Properties();
        ConversionTester t = new ConversionTester(
            importParams,
            TripleStoreFactory.onlyDefaultImplementation(),
            new ComparisonConfig());
        Network expected = null;
        t.testConversion(expected, Cgmes3Catalog.miniGrid());
    }

    @Test
    public void miniGridConvertBoundary() throws IOException {
        Properties importParams = new Properties();
        importParams.put(CgmesImport.CONVERT_BOUNDARY, "true");
        ConversionTester t = new ConversionTester(
            importParams,
            TripleStoreFactory.onlyDefaultImplementation(),
            new ComparisonConfig());
        Network expected = null;
        t.testConversion(expected, Cgmes3Catalog.miniGrid());
    }

    @Test
    public void miniGridOperationalLimits() throws IOException {
        Network n = networkModel(Cgmes3Catalog.miniGrid(), new Conversion.Config());

        TwoWindingsTransformer tw2t = n.getTwoWindingsTransformer("813365c3-5be7-4ef0-a0a7-abd1ae6dc174");
        assertEquals(753.0659, tw2t.getCurrentLimits1().map(LoadingLimits::getPermanentLimit).orElse(0.0), 0.0);
        assertEquals(4123.933, tw2t.getCurrentLimits2().map(LoadingLimits::getPermanentLimit).orElse(0.0), 0.0);

        assertEquals(0, (int) tw2t.getCurrentLimits1().map(l -> l.getTemporaryLimits().size()).orElse(-1));
        assertEquals(0, (int) tw2t.getCurrentLimits2().map(l -> l.getTemporaryLimits().size()).orElse(-1));

        ThreeWindingsTransformer tw3t = n.getThreeWindingsTransformer("411b5401-0a43-404a-acb4-05c3d7d0c95c");
        assertEquals(505.1817, tw3t.getLeg1().getCurrentLimits().map(LoadingLimits::getPermanentLimit).orElse(0.0), 0.0);
        assertEquals(1683.939, tw3t.getLeg2().getCurrentLimits().map(LoadingLimits::getPermanentLimit).orElse(0.0), 0.0);
        assertEquals(962.2509, tw3t.getLeg3().getCurrentLimits().map(LoadingLimits::getPermanentLimit).orElse(0.0), 0.0);

        assertEquals(0, (int) tw3t.getLeg1().getCurrentLimits().map(l -> l.getTemporaryLimits().size()).orElse(-1));
        assertEquals(0, (int) tw3t.getLeg2().getCurrentLimits().map(l -> l.getTemporaryLimits().size()).orElse(-1));
        assertEquals(0, (int) tw3t.getLeg3().getCurrentLimits().map(l -> l.getTemporaryLimits().size()).orElse(-1));
    }

    @Test
    public void miniGridWithAndWithoutTpSv() throws IOException {
        Network network = new CgmesImport().importData(
            Cgmes3Catalog.miniGrid().dataSource(),
            NetworkFactory.findDefault(), null);

        Network networkwithoutTpSv = new CgmesImport().importData(
            Cgmes3Catalog.miniGridWithoutTpSv().dataSource(),
            NetworkFactory.findDefault(), null);

        resetBusVoltageAndAngleBeforeComparison(network);
        resetTerminalPQofLoadsAndGeneratorsBeforeComparison(network);
        new Comparison(network, networkwithoutTpSv, new ComparisonConfig()).compare();
        assertTrue(true);
    }

    @Test
    public void smallGrid() throws IOException {
        Properties importParams = new Properties();
        ConversionTester t = new ConversionTester(
            importParams,
            TripleStoreFactory.onlyDefaultImplementation(),
            new ComparisonConfig());
        Network expected = null;
        t.testConversion(expected, Cgmes3Catalog.smallGrid());
    }

    @Test
    public void smallGridConvertBoundary() throws IOException {
        Properties importParams = new Properties();
        importParams.put(CgmesImport.CONVERT_BOUNDARY, "true");
        ConversionTester t = new ConversionTester(
            importParams,
            TripleStoreFactory.onlyDefaultImplementation(),
            new ComparisonConfig());
        Network expected = null;
        t.testConversion(expected, Cgmes3Catalog.smallGrid());
    }

    @Test
    public void smallGridOperationalLimits() throws IOException {
        Network n = networkModel(Cgmes3Catalog.smallGrid(), new Conversion.Config());

        Line ln = n.getLine("04658820-c766-11e1-8775-005056c00008");
        assertEquals(1000.0, ln.getCurrentLimits1().map(LoadingLimits::getPermanentLimit).orElse(0.0), 0.0);
        assertEquals(1000.0, ln.getCurrentLimits2().map(LoadingLimits::getPermanentLimit).orElse(0.0), 0.0);

        assertEquals(1, (int) ln.getCurrentLimits1().map(l -> l.getTemporaryLimits().size()).orElse(-1));
        TemporaryLimit lntl1 = ln.getCurrentLimits1().flatMap(l -> l.getTemporaryLimits().stream().findFirst()).orElseThrow(AssertionError::new);
        assertEquals(500.0, lntl1.getValue(), 0.0);
        assertEquals(900, lntl1.getAcceptableDuration());

        assertEquals(1, (int) ln.getCurrentLimits2().map(l -> l.getTemporaryLimits().size()).orElse(-1));
        TemporaryLimit lntl2 = ln.getCurrentLimits2().flatMap(l -> l.getTemporaryLimits().stream().findFirst()).orElseThrow(AssertionError::new);
        assertEquals(500.0, lntl2.getValue(), 0.0);
        assertEquals(900, lntl2.getAcceptableDuration());
    }

    @Test
    public void smallGridWithAndWithoutTpSv() throws IOException {
        Network network = new CgmesImport().importData(
            Cgmes3Catalog.smallGrid().dataSource(),
            NetworkFactory.findDefault(), null);

        Network networkwithoutTpSv = new CgmesImport().importData(
            Cgmes3Catalog.smallGridWithoutTpSv().dataSource(),
            NetworkFactory.findDefault(), null);

        resetBusVoltageAndAngleBeforeComparison(network);
        resetTerminalPQofLoadsAndGeneratorsBeforeComparison(network);
        new Comparison(network, networkwithoutTpSv, new ComparisonConfig()).compare();
        assertTrue(true);
    }

    @Test
    public void svedala() throws IOException {
        Properties importParams = new Properties();
        ConversionTester t = new ConversionTester(
            importParams,
            TripleStoreFactory.onlyDefaultImplementation(),
            new ComparisonConfig());
        Network expected = null;
        t.testConversion(expected, Cgmes3Catalog.svedala());
    }

    @Test
    public void svedalaConvertBoundary() throws IOException {
        Properties importParams = new Properties();
        importParams.put(CgmesImport.CONVERT_BOUNDARY, "true");
        ConversionTester t = new ConversionTester(
            importParams,
            TripleStoreFactory.onlyDefaultImplementation(),
            new ComparisonConfig());
        Network expected = null;
        t.testConversion(expected, Cgmes3Catalog.svedala());
    }

    @Test
    public void svedalaOperationalLimits() throws IOException {
        Network n = networkModel(Cgmes3Catalog.svedala(), new Conversion.Config());

        Line ln = n.getLine("c6278b38-b777-4ad9-b395-50c4009afdff");
        assertEquals(2970.0, ln.getCurrentLimits1().map(LoadingLimits::getPermanentLimit).orElse(0.0), 0.0);
        assertEquals(2970.0, ln.getCurrentLimits2().map(LoadingLimits::getPermanentLimit).orElse(0.0), 0.0);

        assertEquals(1, (int) ln.getCurrentLimits1().map(l -> l.getTemporaryLimits().size()).orElse(-1));
        TemporaryLimit lntl1 = ln.getCurrentLimits1().flatMap(l -> l.getTemporaryLimits().stream().findFirst()).orElseThrow(AssertionError::new);
        assertEquals(500.0, lntl1.getValue(), 0.0);
        assertEquals(600, lntl1.getAcceptableDuration());

        assertEquals(1, (int) ln.getCurrentLimits2().map(l -> l.getTemporaryLimits().size()).orElse(-1));
        TemporaryLimit lntl2 = ln.getCurrentLimits2().flatMap(l -> l.getTemporaryLimits().stream().findFirst()).orElseThrow(AssertionError::new);
        assertEquals(500.0, lntl2.getValue(), 0.0);
        assertEquals(600, lntl2.getAcceptableDuration());
    }

    @Test
    public void svedalaWithAndWithoutTpSv() throws IOException {
        Network network = new CgmesImport().importData(
            Cgmes3Catalog.svedala().dataSource(),
            NetworkFactory.findDefault(), null);

        Network networkwithoutTpSv = new CgmesImport().importData(
            Cgmes3Catalog.svedalaWithoutTpSv().dataSource(),
            NetworkFactory.findDefault(), null);

        resetBusVoltageAndAngleBeforeComparison(network);
        resetTerminalPQofLoadsAndGeneratorsBeforeComparison(network);
        new Comparison(network, networkwithoutTpSv, new ComparisonConfig()).compare();
        assertTrue(true);
    }

    private Network networkModel(TestGridModel testGridModel, Conversion.Config config) throws IOException {
        ReadOnlyDataSource ds = testGridModel.dataSource();
        String impl = TripleStoreFactory.defaultImplementation();

        CgmesModel cgmes = CgmesModelFactory.create(ds, impl);

        config.setConvertSvInjections(true);
        Conversion c = new Conversion(cgmes, config);
        return c.convert();
    }

    private static void resetBusVoltageAndAngleBeforeComparison(Network network) {
        network.getBusBreakerView().getBuses().forEach(bus -> {
            bus.setV(Double.NaN);
            bus.setAngle(Double.NaN);
        });
    }

    private static void resetTerminalPQofLoadsAndGeneratorsBeforeComparison(Network network) {
        network.getGenerators().forEach(generator -> {
            generator.getTerminal().setP(Double.NaN);
            generator.getTerminal().setQ(Double.NaN);
        });
        network.getLoads().forEach(load -> {
            load.getTerminal().setP(Double.NaN);
            load.getTerminal().setQ(Double.NaN);
        });
    }
}
