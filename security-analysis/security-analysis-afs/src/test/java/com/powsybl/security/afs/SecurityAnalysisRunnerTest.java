/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.afs;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.powsybl.afs.*;
import com.powsybl.afs.ext.base.*;
import com.powsybl.afs.mapdb.storage.MapDbAppStorage;
import com.powsybl.afs.storage.AppStorage;
import com.powsybl.afs.storage.NodeGenericMetadata;
import com.powsybl.afs.storage.NodeInfo;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.contingency.BranchContingency;
import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.afs.ContingencyStore;
import com.powsybl.contingency.afs.ContingencyStoreBuilder;
import com.powsybl.contingency.afs.ContingencyStoreExtension;
import com.powsybl.iidm.import_.ImportConfig;
import com.powsybl.iidm.import_.Importer;
import com.powsybl.iidm.import_.ImportersLoader;
import com.powsybl.iidm.import_.ImportersLoaderList;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.StateManager;
import com.powsybl.security.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SecurityAnalysisRunnerTest extends AbstractProjectFileTest {

    private static SecurityAnalysisResult createResult() {
        LimitViolationsResult preContingencyResult = new LimitViolationsResult(true, ImmutableList.of(new LimitViolation("s1", LimitViolationType.HIGH_VOLTAGE, 400.0, 1f, 440.0)));
        return new SecurityAnalysisResult(preContingencyResult, Collections.emptyList());
    }

    private static class SecurityAnalysisServiceMock implements SecurityAnalysisRunningService {

        @Override
        public void run(SecurityAnalysisRunner runner) {
            runner.writeResult(createResult());
        }
    }

    @AutoService(ServiceExtension.class)
    public class SecurityAnalysisServiceExtensionMock implements ServiceExtension<SecurityAnalysisRunningService> {

        @Override
        public ServiceKey<SecurityAnalysisRunningService> getServiceKey() {
            return new ServiceKey<>(SecurityAnalysisRunningService.class, false);
        }

        @Override
        public SecurityAnalysisRunningService createService(ServiceCreationContext context) {
            return new SecurityAnalysisServiceMock();
        }
    }

    private static class ImporterMock implements Importer {

        static final String FORMAT = "net";

        @Override
        public String getFormat() {
            return FORMAT;
        }

        @Override
        public String getComment() {
            return "";
        }

        @Override
        public boolean exists(ReadOnlyDataSource dataSource) {
            return true;
        }

        @Override
        public Network importData(ReadOnlyDataSource dataSource, Properties parameters) {
            Network network = Mockito.mock(Network.class);
            StateManager stateManager = Mockito.mock(StateManager.class);
            Mockito.when(stateManager.getWorkingStateId()).thenReturn("s1");
            Mockito.when(network.getStateManager()).thenReturn(stateManager);
            return network;
        }

        @Override
        public void copy(ReadOnlyDataSource fromDataSource, DataSource toDataSource) {
        }
    }

    private final ImportersLoader importersLoader = new ImportersLoaderList(Collections.singletonList(new ImporterMock()));

    @Override
    protected AppStorage createStorage() {
        return MapDbAppStorage.createHeap("mem");
    }

    @Override
    protected List<FileExtension> getFileExtensions() {
        return ImmutableList.of(new CaseExtension(importersLoader));
    }

    @Override
    protected List<ProjectFileExtension> getProjectFileExtensions() {
        return ImmutableList.of(new ImportedCaseExtension(importersLoader, new ImportConfig()),
                                new ContingencyStoreExtension(),
                                new SecurityAnalysisRunnerExtension(new SecurityAnalysisParameters()));
    }

    @Override
    protected List<ServiceExtension> getServiceExtensions() {
        return ImmutableList.of(new SecurityAnalysisServiceExtensionMock(),
                new LocalNetworkCacheServiceExtension());
    }

    @Before
    public void setup() throws IOException {
        super.setup();

        NodeInfo rootFolderInfo = storage.createRootNodeIfNotExists("root", Folder.PSEUDO_CLASS);

        // create network.net
        storage.createNode(rootFolderInfo.getId(), "network", Case.PSEUDO_CLASS, "", Case.VERSION,
                new NodeGenericMetadata().setString(Case.FORMAT, ImporterMock.FORMAT));

        // create network2.net
        storage.createNode(rootFolderInfo.getId(), "network2", Case.PSEUDO_CLASS, "", Case.VERSION,
                new NodeGenericMetadata().setString(Case.FORMAT, ImporterMock.FORMAT));
    }

    @Test
    public void test() {
        Case aCase = afs.getRootFolder().getChild(Case.class, "network")
                                        .orElseThrow(AssertionError::new);

        // create project in the root folder
        Project project = afs.getRootFolder().createProject("project");

        // import network.net in root folder of the project
        ImportedCase importedCase = project.getRootFolder().fileBuilder(ImportedCaseBuilder.class)
                .withCase(aCase)
                .build();

        // create contingency list
        ContingencyStore contingencyStore = project.getRootFolder().fileBuilder(ContingencyStoreBuilder.class)
                .withName("contingencies")
                .build();
        contingencyStore.write(new Contingency("c1", new BranchContingency("l1")));

        // create a security analysis runner that point to imported case
        SecurityAnalysisRunner runner = project.getRootFolder().fileBuilder(SecurityAnalysisRunnerBuilder.class)
                .withName("sa")
                .withCase(importedCase)
                .withContingencyStore(contingencyStore)
                .build();

        assertTrue(runner.getCase().isPresent());
        assertEquals(importedCase.getId(), runner.getCase().get().getId());
        assertTrue(runner.getContingencyStore().isPresent());
        assertEquals(contingencyStore.getId(), runner.getContingencyStore().get().getId());

        // check there is no results
        assertFalse(runner.hasResult());
        assertNull(runner.readResult());

        // check default parameters can be changed
        SecurityAnalysisParameters parameters = runner.readParameters();
        assertNotNull(parameters);
        assertFalse(parameters.getLoadFlowParameters().isSpecificCompatibility());
        parameters.getLoadFlowParameters().setSpecificCompatibility(true);
        runner.writeParameters(parameters);
        assertTrue(runner.readParameters().getLoadFlowParameters().isSpecificCompatibility());

        // run security analysis
        runner.run();

        // check results
        assertTrue(runner.hasResult());
        SecurityAnalysisResult result = runner.readResult();
        assertNotNull(result);
        assertNotNull(result.getPreContingencyResult());
        assertEquals(1, result.getPreContingencyResult().getLimitViolations().size());

        // update dependencies
        Case aCase2 = afs.getRootFolder().getChild(Case.class, "network2")
                .orElseThrow(AssertionError::new);
        ImportedCase importedCase2 = project.getRootFolder().fileBuilder(ImportedCaseBuilder.class)
                .withCase(aCase2)
                .build();
        ContingencyStore contingencyStore2 = project.getRootFolder().fileBuilder(ContingencyStoreBuilder.class)
                .withName("contingencies2")
                .build();
        runner.setCase(importedCase2);
        runner.setContingencyStore(contingencyStore2);
        assertTrue(runner.getCase().isPresent());
        assertEquals(importedCase2.getId(), runner.getCase().get().getId());
        assertTrue(runner.getContingencyStore().isPresent());
        assertEquals(contingencyStore2.getId(), runner.getContingencyStore().get().getId());
    }
}
