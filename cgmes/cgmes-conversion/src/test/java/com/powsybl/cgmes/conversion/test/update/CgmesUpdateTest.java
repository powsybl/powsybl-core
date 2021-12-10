/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.test.update;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.cgmes.conformity.test.CgmesConformity1Catalog;
import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.CgmesModelExtension;
import com.powsybl.cgmes.conversion.test.ConversionTester;
import com.powsybl.cgmes.conversion.test.network.compare.Comparison;
import com.powsybl.cgmes.conversion.test.network.compare.ComparisonConfig;
import com.powsybl.cgmes.conversion.update.IidmChange;
import com.powsybl.cgmes.conversion.update.IidmChangeUpdate;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.triplestore.api.TripleStoreFactory;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.fail;

/**
 * @author Elena Kaltakova <kaltakovae at aia.es>
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class CgmesUpdateTest {

    @Before
    public void setUp() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        cgmesImport = new CgmesImport(new InMemoryPlatformConfig(fileSystem));
    }

    @After
    public void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    public void testSimpleUpdateSmallGrid() throws IOException {
        testSimpleUpdate(CgmesConformity1Catalog.smallBusBranch().dataSource());
    }

    @Ignore("Contains an AsynchronousMachine that is mapped to an IIDM load")
    @Test
    public void testSimpleUpdateMiniBusBranch() throws IOException {
        testSimpleUpdate(CgmesConformity1Catalog.miniBusBranch().dataSource());
    }

    @Ignore("Requires node-breaker management of updates")
    @Test
    public void testSimpleUpdateMiniNodeBreaker() throws IOException {
        testSimpleUpdate(CgmesConformity1Catalog.miniNodeBreaker().dataSource());
    }

    void testSimpleUpdate(ReadOnlyDataSource ds) throws IOException {
        testSimpleUpdate(ds, TripleStoreFactory.defaultImplementation());
    }

    private void testSimpleUpdate(ReadOnlyDataSource ds, String impl) throws IOException {
        Network network0 = cgmesImport.importData(ds, NetworkFactory.findDefault(), importParameters(impl));
        if (isEmpty(network0)) {
            fail("Network is empty");
            return;
        }

        network0.getVariantManager().cloneVariant(network0.getVariantManager().getWorkingVariantId(), "1");
        network0.getVariantManager().setWorkingVariant("1");

        NetworkChanges.modifyEquipmentCharacteristics(network0);
        int maxChanges = 1000;
        NetworkChanges.scaleLoadGenerator(network0, maxChanges);
        NetworkChanges.modifySteadyStateHypothesis(network0);

        // Modify state variables altering all the voltages
        // and recalculating all the flows
        NetworkChanges.modifyStateVariables(network0);
        ConversionTester.invalidateFlows(network0);
        ConversionTester.computeMissingFlows(network0);

        // Flows and voltages should have not been recorded as changes
        checkStateVariablesNotRecordedInChangelog(network0);

        DataSource tmp = tmpDataSource(impl);
        CgmesExport e = new CgmesExport();
        e.export(network0, new Properties(), tmp);

        // import saved data and compare with original Network
        Network network1 = cgmesImport.importData(tmp, NetworkFactory.findDefault(), importParameters(impl));
        // TODO Think if we should complete flows in the re-imported Network
        // ConversionTester.computeMissingFlows(network1);
        compare(network0, network1);
    }

    private static void checkStateVariablesNotRecordedInChangelog(Network network) {
        Set<String> stateVariables = Stream.of("v", "angle", "p", "q", "p1", "p2", "p3", "q1", "q2", "q3")
                .collect(Collectors.toCollection(HashSet::new));
        List<IidmChange> changes = network.getExtension(CgmesModelExtension.class).getCgmesUpdate().changelog()
                .getChangesForVariant(network.getVariantManager().getWorkingVariantId());
        changes.stream().filter(c -> c instanceof IidmChangeUpdate).map(c -> (IidmChangeUpdate) c)
                .filter(c -> stateVariables.contains(c.getAttribute())).findFirst().map(c -> {
                    LOG.error("State variable found in changelog {}.{}", c.getIdentifiable().getClass().getSimpleName(),
                            c.getAttribute());
                    return c;
                }).ifPresent(c -> fail());
    }

    private void compare(Network network0, Network network1) {
        ComparisonConfig config = new ComparisonConfig().checkNetworkId(false).tolerance(2.4e-4);
        Comparison comparison = new Comparison(network0, network1, config);
        comparison.compare();
    }

    private boolean isEmpty(Network network) {
        return network.getSubstationCount() == 0;
    }

    private Properties importParameters(String impl) {
        Properties importParameters = new Properties();
        importParameters.put(CgmesImport.POWSYBL_TRIPLESTORE, impl);
        importParameters.put(CgmesImport.STORE_CGMES_MODEL_AS_NETWORK_EXTENSION, "true");
        // SvInjections are converted to IIDM loads
        // Changes in these loads are not mapped back to CGMES,
        // because there is no corresponding object
        // For these tests, we simply ignore SvInjections when importing
        importParameters.put(CgmesImport.CONVERT_SV_INJECTIONS, "false");
        return importParameters;
    }

    private DataSource tmpDataSource(String impl) throws IOException {
        Path exportFolder = fileSystem.getPath("impl-" + impl);
        if (Files.exists(exportFolder)) {
            FileUtils.cleanDirectory(exportFolder.toFile());
        }
        Files.createDirectories(exportFolder);
        DataSource tmpDataSource = new FileDataSource(exportFolder, "");
        return tmpDataSource;
    }

    private FileSystem fileSystem;
    private CgmesImport cgmesImport;

    private static final Logger LOG = LoggerFactory.getLogger(CgmesUpdateTest.class);
}
