/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.test.conformity;

import com.powsybl.cgmes.conformity.test.Cgmes3Catalog;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.test.ConversionTester;
import com.powsybl.cgmes.conversion.test.network.compare.Comparison;
import com.powsybl.cgmes.conversion.test.network.compare.ComparisonConfig;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesModelFactory;
import com.powsybl.cgmes.model.test.TestGridModel;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.LoadingLimits.TemporaryLimit;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.triplestore.api.TripleStoreFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Properties;

import org.junit.Test;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class Cgmes3ConversionTest {
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

        Line ln = n.getLine("_ffbabc27-1ccd-4fdc-b037-e341706c8d29");
        assertEquals(1312.0, ln.getCurrentLimits1().getPermanentLimit(), 0.0);
        assertEquals(1312.0, ln.getCurrentLimits2().getPermanentLimit(), 0.0);

        assertEquals(1, ln.getCurrentLimits1().getTemporaryLimits().size());
        TemporaryLimit lntl1 = ln.getCurrentLimits1().getTemporaryLimits().stream().findFirst().get();
        assertEquals(500.0, lntl1.getValue(), 0.0);
        assertEquals(10, lntl1.getAcceptableDuration());

        assertEquals(1, ln.getCurrentLimits2().getTemporaryLimits().size());
        TemporaryLimit lntl2 = ln.getCurrentLimits2().getTemporaryLimits().stream().findFirst().get();
        assertEquals(500.0, lntl2.getValue(), 0.0);
        assertEquals(10, lntl2.getAcceptableDuration());

        Line tln = n.getLine("_dad02278-bd25-476f-8f58-dbe44be72586 + _ed0c5d75-4a54-43c8-b782-b20d7431630b");
        assertEquals(1371.0, tln.getCurrentLimits1().getPermanentLimit(), 0.0);
        assertEquals(1226.0, tln.getCurrentLimits2().getPermanentLimit(), 0.0);

        assertEquals(1, tln.getCurrentLimits1().getTemporaryLimits().size());
        TemporaryLimit tlntl1 = tln.getCurrentLimits1().getTemporaryLimits().stream().findFirst().get();
        assertEquals(500.0, tlntl1.getValue(), 0.0);
        assertEquals(10, tlntl1.getAcceptableDuration());

        assertEquals(1, tln.getCurrentLimits2().getTemporaryLimits().size());
        TemporaryLimit tlntl2 = ln.getCurrentLimits2().getTemporaryLimits().stream().findFirst().get();
        assertEquals(500.0, tlntl2.getValue(), 0.0);
        assertEquals(10, tlntl2.getAcceptableDuration());
    }

    @Test
    public void microGridWithAndWithoutTp() {
        Network network = new CgmesImport().importData(
            Cgmes3Catalog.microGrid().dataSource(),
            NetworkFactory.findDefault(), null);

        Network networkWithoutTp = new CgmesImport().importData(
            Cgmes3Catalog.microGridWithoutTp().dataSource(),
            NetworkFactory.findDefault(), null);

        fixBusVoltageAndAngleBeforeComparison(network);
        // RegulatingTerminals of both networks are localized to avoid differences.
        // TODO must be deleted after fixing regulatingTerminals.
        // Differences are associated with regulating cgmesTerminals defined for breakers
        fixRegulatingTerminalsBeforeComparison(network);
        fixRegulatingTerminalsBeforeComparison(networkWithoutTp);
        new Comparison(network, networkWithoutTp, new ComparisonConfig()).compare();
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

        TwoWindingsTransformer tw2t = n.getTwoWindingsTransformer("_813365c3-5be7-4ef0-a0a7-abd1ae6dc174");
        assertEquals(753.0659, tw2t.getCurrentLimits1().getPermanentLimit(), 0.0);
        assertEquals(4123.933, tw2t.getCurrentLimits2().getPermanentLimit(), 0.0);

        assertEquals(0, tw2t.getCurrentLimits1().getTemporaryLimits().size());
        assertEquals(0, tw2t.getCurrentLimits2().getTemporaryLimits().size());

        ThreeWindingsTransformer tw3t = n.getThreeWindingsTransformer("_411b5401-0a43-404a-acb4-05c3d7d0c95c");
        assertEquals(505.1817, tw3t.getLeg1().getCurrentLimits().getPermanentLimit(), 0.0);
        assertEquals(1683.939, tw3t.getLeg2().getCurrentLimits().getPermanentLimit(), 0.0);
        assertEquals(962.2509, tw3t.getLeg3().getCurrentLimits().getPermanentLimit(), 0.0);

        assertEquals(0, tw3t.getLeg1().getCurrentLimits().getTemporaryLimits().size());
        assertEquals(0, tw3t.getLeg2().getCurrentLimits().getTemporaryLimits().size());
        assertEquals(0, tw3t.getLeg3().getCurrentLimits().getTemporaryLimits().size());
    }

    @Test
    public void miniGridWithAndWithoutTp() throws IOException {
        Network network = new CgmesImport().importData(
            Cgmes3Catalog.miniGrid().dataSource(),
            NetworkFactory.findDefault(), null);

        Network networkWithoutTp = new CgmesImport().importData(
            Cgmes3Catalog.miniGridWithoutTp().dataSource(),
            NetworkFactory.findDefault(), null);

        fixBusVoltageAndAngleBeforeComparison(network);
        new Comparison(network, networkWithoutTp, new ComparisonConfig()).compare();
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

        Line ln = n.getLine("_04658820-c766-11e1-8775-005056c00008");
        assertEquals(1000.0, ln.getCurrentLimits1().getPermanentLimit(), 0.0);
        assertEquals(1000.0, ln.getCurrentLimits2().getPermanentLimit(), 0.0);

        assertEquals(1, ln.getCurrentLimits1().getTemporaryLimits().size());
        TemporaryLimit lntl1 = ln.getCurrentLimits1().getTemporaryLimits().stream().findFirst().get();
        assertEquals(500.0, lntl1.getValue(), 0.0);
        assertEquals(900, lntl1.getAcceptableDuration());

        assertEquals(1, ln.getCurrentLimits2().getTemporaryLimits().size());
        TemporaryLimit lntl2 = ln.getCurrentLimits2().getTemporaryLimits().stream().findFirst().get();
        assertEquals(500.0, lntl2.getValue(), 0.0);
        assertEquals(900, lntl2.getAcceptableDuration());
    }

    @Test
    public void smallGridWithAndWithoutTp() throws IOException {
        Network network = new CgmesImport().importData(
            Cgmes3Catalog.smallGrid().dataSource(),
            NetworkFactory.findDefault(), null);

        Network networkWithoutTp = new CgmesImport().importData(
            Cgmes3Catalog.smallGridWithoutTp().dataSource(),
            NetworkFactory.findDefault(), null);

        fixBusVoltageAndAngleBeforeComparison(network);
        new Comparison(network, networkWithoutTp, new ComparisonConfig()).compare();
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

        Line ln = n.getLine("_c6278b38-b777-4ad9-b395-50c4009afdff");
        assertEquals(2970.0, ln.getCurrentLimits1().getPermanentLimit(), 0.0);
        assertEquals(2970.0, ln.getCurrentLimits2().getPermanentLimit(), 0.0);

        assertEquals(1, ln.getCurrentLimits1().getTemporaryLimits().size());
        TemporaryLimit lntl1 = ln.getCurrentLimits1().getTemporaryLimits().stream().findFirst().get();
        assertEquals(500.0, lntl1.getValue(), 0.0);
        assertEquals(600, lntl1.getAcceptableDuration());

        assertEquals(1, ln.getCurrentLimits2().getTemporaryLimits().size());
        TemporaryLimit lntl2 = ln.getCurrentLimits2().getTemporaryLimits().stream().findFirst().get();
        assertEquals(500.0, lntl2.getValue(), 0.0);
        assertEquals(600, lntl2.getAcceptableDuration());
    }

    @Test
    public void svedalaWithAndWithoutTp() throws IOException {
        Network network = new CgmesImport().importData(
            Cgmes3Catalog.svedala().dataSource(),
            NetworkFactory.findDefault(), null);

        Network networkWithoutTp = new CgmesImport().importData(
            Cgmes3Catalog.svedalaWithoutTp().dataSource(),
            NetworkFactory.findDefault(), null);

        fixBusVoltageAndAngleBeforeComparison(network);
        // regulatingTerminals of network are localized to avoid differences.
        // TODO must be deleted after fixing regulatingTerminals.
        // Differences are associated with regulating cgmesTerminals defined for breakers
        fixRegulatingTerminalsBeforeComparison(network);
        new Comparison(network, networkWithoutTp, new ComparisonConfig()).compare();
        assertTrue(true);
    }

    private Network networkModel(TestGridModel testGridModel, Conversion.Config config) throws IOException {
        ReadOnlyDataSource ds = testGridModel.dataSource();
        String impl = TripleStoreFactory.defaultImplementation();

        CgmesModel cgmes = CgmesModelFactory.create(ds, impl);

        config.setConvertSvInjections(true);
        config.setProfileUsedForInitialStateValues(Conversion.Config.StateProfile.SSH.name());
        Conversion c = new Conversion(cgmes, config);
        return c.convert();
    }

    private static void fixBusVoltageAndAngleBeforeComparison(Network network) {
        network.getBusBreakerView().getBuses().forEach(bus -> {
            bus.setV(Double.NaN);
            bus.setAngle(Double.NaN);
        });
        network.getGenerators().forEach(generator -> {
            generator.setRegulatingTerminal(generator.getTerminal());
        });
        network.getShuntCompensators().forEach(shuntCompensator -> {
            shuntCompensator.setRegulatingTerminal(shuntCompensator.getTerminal());
        });
    }

    private static void fixRegulatingTerminalsBeforeComparison(Network network) {
        network.getGenerators().forEach(generator -> {
            generator.setRegulatingTerminal(generator.getTerminal());
        });
        network.getShuntCompensators().forEach(shuntCompensator -> {
            shuntCompensator.setRegulatingTerminal(shuntCompensator.getTerminal());
        });
    }
}
