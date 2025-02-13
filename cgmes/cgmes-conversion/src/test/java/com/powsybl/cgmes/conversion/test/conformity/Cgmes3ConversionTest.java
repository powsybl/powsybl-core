/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.test.conformity;

import com.powsybl.cgmes.conformity.Cgmes3Catalog;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.test.ConversionTester;
import com.powsybl.cgmes.conversion.test.ConversionUtil;
import com.powsybl.cgmes.conversion.test.network.compare.Comparison;
import com.powsybl.cgmes.conversion.test.network.compare.ComparisonConfig;
import com.powsybl.cgmes.model.GridModelReference;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ReadOnlyMemDataSource;
import com.powsybl.iidm.network.*;
import com.powsybl.triplestore.api.TripleStoreFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class Cgmes3ConversionTest {

    @Test
    void loadNetworkMicroGrid() {
        // Check that CGMES importer supports check existence of valid CGMES 3 (CIM100) files
        CgmesImport importer = new CgmesImport();
        ReadOnlyDataSource ds = Cgmes3Catalog.microGrid().dataSource();
        assertTrue(importer.exists(ds));
        Properties importParams = new Properties();
        importParams.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");
        Network network = importer.importData(ds, NetworkFactory.findDefault(), importParams);
        assertNotNull(network);
    }

    @Test
    void microGrid() throws IOException {
        Properties importParams = new Properties();
        importParams.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");
        ConversionTester t = new ConversionTester(
            importParams,
            TripleStoreFactory.onlyDefaultImplementation(),
            new ComparisonConfig());
        Network expected = null;
        t.testConversion(expected, Cgmes3Catalog.microGrid());
    }

    @Test
    void microGridConvertBoundary() throws IOException {
        Properties importParams = new Properties();
        importParams.put(CgmesImport.CONVERT_BOUNDARY, "true");
        importParams.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");
        ConversionTester t = new ConversionTester(
            importParams,
            TripleStoreFactory.onlyDefaultImplementation(),
            new ComparisonConfig());
        Network expected = null;
        t.testConversion(expected, Cgmes3Catalog.microGrid());
    }

    @Test
    void microGridWithAndWithoutTpSv() {
        Properties importParams = new Properties();
        importParams.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");

        Network network = new CgmesImport().importData(
            Cgmes3Catalog.microGrid().dataSource(),
            NetworkFactory.findDefault(), importParams);

        Network networkwithoutTpSv = new CgmesImport().importData(
            Cgmes3Catalog.microGridWithoutTpSv().dataSource(),
            NetworkFactory.findDefault(), importParams);

        resetBusVoltageAndAngleBeforeComparison(network);
        resetTerminalPQofLoadsAndGeneratorsBeforeComparison(network);
        new Comparison(network, networkwithoutTpSv, new ComparisonConfig().ignoreMissingMetadata()).compare();
        assertTrue(true);
    }

    @Test
    void miniGrid() throws IOException {
        Properties importParams = new Properties();
        importParams.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");
        ConversionTester t = new ConversionTester(
            importParams,
            TripleStoreFactory.onlyDefaultImplementation(),
            new ComparisonConfig());
        Network expected = null;
        t.testConversion(expected, Cgmes3Catalog.miniGrid());
    }

    @Test
    void miniGridConvertBoundary() throws IOException {
        Properties importParams = new Properties();
        importParams.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");
        importParams.put(CgmesImport.CONVERT_BOUNDARY, "true");
        ConversionTester t = new ConversionTester(
            importParams,
            TripleStoreFactory.onlyDefaultImplementation(),
            new ComparisonConfig());
        Network expected = null;
        t.testConversion(expected, Cgmes3Catalog.miniGrid());
    }

    @Test
    void miniGridRatedS() {
        Network n = networkModel(Cgmes3Catalog.miniGrid(), new Conversion.Config());

        assertEquals(31.5, n.getTwoWindingsTransformer("ceb5d06a-a7ff-4102-a620-7f3ea5fb4a51").getRatedS(), 0.0);
        assertEquals(150.0, n.getTwoWindingsTransformer("813365c3-5be7-4ef0-a0a7-abd1ae6dc174").getRatedS(), 0.0);
        assertEquals(100.0, n.getTwoWindingsTransformer("f1e72854-ec35-46e9-b614-27db354e8dbb").getRatedS(), 0.0);
        assertEquals(31.5, n.getTwoWindingsTransformer("6c89588b-3df5-4120-88e5-26164afb43e9").getRatedS(), 0.0);

        assertEquals(350.0, n.getThreeWindingsTransformer("411b5401-0a43-404a-acb4-05c3d7d0c95c").getLeg1().getRatedS(), 0.0);
        assertEquals(350.0, n.getThreeWindingsTransformer("411b5401-0a43-404a-acb4-05c3d7d0c95c").getLeg2().getRatedS(), 0.0);
        assertEquals(50.0, n.getThreeWindingsTransformer("411b5401-0a43-404a-acb4-05c3d7d0c95c").getLeg3().getRatedS(), 0.0);

        assertEquals(350.0, n.getThreeWindingsTransformer("5d38b7ed-73fd-405a-9cdb-78425e003773").getLeg1().getRatedS(), 0.0);
        assertEquals(350.0, n.getThreeWindingsTransformer("5d38b7ed-73fd-405a-9cdb-78425e003773").getLeg2().getRatedS(), 0.0);
        assertEquals(50.0, n.getThreeWindingsTransformer("5d38b7ed-73fd-405a-9cdb-78425e003773").getLeg3().getRatedS(), 0.0);
    }

    @Test
    void miniGridWithAndWithoutTpSv() {
        Properties importParams = new Properties();
        importParams.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");

        Network network = new CgmesImport().importData(
            Cgmes3Catalog.miniGrid().dataSource(),
            NetworkFactory.findDefault(), importParams);

        Network networkwithoutTpSv = new CgmesImport().importData(
            Cgmes3Catalog.miniGridWithoutTpSv().dataSource(),
            NetworkFactory.findDefault(), importParams);

        resetBusVoltageAndAngleBeforeComparison(network);
        resetTerminalPQofLoadsAndGeneratorsBeforeComparison(network);
        new Comparison(network, networkwithoutTpSv, new ComparisonConfig().ignoreMissingMetadata()).compare();
        assertTrue(true);
    }

    @Test
    void smallGrid() throws IOException {
        Properties importParams = new Properties();
        importParams.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");
        ConversionTester t = new ConversionTester(
            importParams,
            TripleStoreFactory.onlyDefaultImplementation(),
            new ComparisonConfig());
        Network expected = null;
        t.testConversion(expected, Cgmes3Catalog.smallGrid());
    }

    @Test
    void smallGridConvertBoundary() throws IOException {
        Properties importParams = new Properties();
        importParams.put(CgmesImport.CONVERT_BOUNDARY, "true");
        importParams.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");

        ConversionTester t = new ConversionTester(
            importParams,
            TripleStoreFactory.onlyDefaultImplementation(),
            new ComparisonConfig());
        Network expected = null;
        t.testConversion(expected, Cgmes3Catalog.smallGrid());
    }

    @Test
    void smallGridWithAndWithoutTpSv() {
        Properties importParams = new Properties();
        importParams.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");

        Network network = new CgmesImport().importData(
            Cgmes3Catalog.smallGrid().dataSource(),
            NetworkFactory.findDefault(), importParams);

        Network networkwithoutTpSv = new CgmesImport().importData(
            Cgmes3Catalog.smallGridWithoutTpSv().dataSource(),
            NetworkFactory.findDefault(), importParams);

        resetBusVoltageAndAngleBeforeComparison(network);
        resetTerminalPQofLoadsAndGeneratorsBeforeComparison(network);
        new Comparison(network, networkwithoutTpSv, new ComparisonConfig().ignoreMissingMetadata()).compare();
        assertTrue(true);
    }

    @Test
    void svedala() throws IOException {
        Properties importParams = new Properties();
        importParams.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");
        ConversionTester t = new ConversionTester(
            importParams,
            TripleStoreFactory.onlyDefaultImplementation(),
            new ComparisonConfig());
        Network expected = null;
        t.testConversion(expected, Cgmes3Catalog.svedala());
    }

    @Test
    void svedalaWithDifferentFictitiousSwitchesCreationModes() throws IOException {
        Properties importParams = new Properties();
        importParams.put(CgmesImport.CREATE_FICTITIOUS_SWITCHES_FOR_DISCONNECTED_TERMINALS_MODE, "NEVER");
        importParams.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");
        ConversionTester t = new ConversionTester(importParams, null,
                TripleStoreFactory.onlyDefaultImplementation(),
                new ComparisonConfig());
        t.testConversion(null, Cgmes3Catalog.svedala());

        importParams = new Properties();
        importParams.put(CgmesImport.CREATE_FICTITIOUS_SWITCHES_FOR_DISCONNECTED_TERMINALS_MODE, "ALWAYS_EXCEPT_SWITCHES");
        importParams.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");
        t = new ConversionTester(importParams, null,
                TripleStoreFactory.onlyDefaultImplementation(),
                new ComparisonConfig());
        t.testConversion(null, Cgmes3Catalog.svedala());
    }

    @Test
    void svedalaConvertBoundary() throws IOException {
        Properties importParams = new Properties();
        importParams.put(CgmesImport.CONVERT_BOUNDARY, "true");
        importParams.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");
        ConversionTester t = new ConversionTester(
            importParams,
            TripleStoreFactory.onlyDefaultImplementation(),
            new ComparisonConfig());
        Network expected = null;
        t.testConversion(expected, Cgmes3Catalog.svedala());
    }

    @Test
    void svedalaWithAndWithoutTpSv() {
        Properties importParams = new Properties();
        importParams.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");

        Network network = new CgmesImport().importData(
            Cgmes3Catalog.svedala().dataSource(),
            NetworkFactory.findDefault(), importParams);

        Network networkwithoutTpSv = new CgmesImport().importData(
            Cgmes3Catalog.svedalaWithoutTpSv().dataSource(),
            NetworkFactory.findDefault(), importParams);

        resetBusVoltageAndAngleBeforeComparison(network);
        resetTerminalPQofLoadsAndGeneratorsBeforeComparison(network);
        new Comparison(network, networkwithoutTpSv, new ComparisonConfig().ignoreMissingMetadata()).compare();
        assertTrue(true);
    }

    @Test
    void throwsUncheckedIOExceptionOnImport() {
        CgmesImport importer = new CgmesImport();
        IOException ioException = new IOException("Error");
        ReadOnlyDataSource ds = new ReadOnlyMemDataSource() {
            @Override
            public String getDataExtension() {
                return "xml"; // Should be equal to CgmesOnDataSource.EXTENSION
            }

            @Override
            public boolean exists(String fileName) throws IOException {
                throw ioException;
            }
        };
        // When the dataSource throws an IOException during "exists" check,
        // the exception should be propagated as an UncheckedIOException
        UncheckedIOException exception = assertThrows(UncheckedIOException.class, () -> importer.exists(ds));
        assertEquals(ioException, exception.getCause());
    }

    private Network networkModel(GridModelReference testGridModel, Conversion.Config config) {
        config.setConvertSvInjections(true);
        return ConversionUtil.networkModel(testGridModel, config);
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
