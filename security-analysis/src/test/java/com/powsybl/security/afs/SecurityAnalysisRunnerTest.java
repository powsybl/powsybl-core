/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.afs;

import com.google.common.collect.ImmutableList;
import com.powsybl.afs.*;
import com.powsybl.afs.ext.base.*;
import com.powsybl.afs.mapdb.storage.MapDbAppStorage;
import com.powsybl.afs.storage.AppStorage;
import com.powsybl.afs.storage.NodeGenericMetadata;
import com.powsybl.afs.storage.NodeInfo;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.computation.ComputationManager;
import com.powsybl.contingency.BranchContingency;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.contingency.ContingencyImpl;
import com.powsybl.contingency.afs.ContingencyStore;
import com.powsybl.contingency.afs.ContingencyStoreBuilder;
import com.powsybl.contingency.afs.ContingencyStoreExtension;
import com.powsybl.iidm.import_.Importer;
import com.powsybl.iidm.import_.ImportersLoader;
import com.powsybl.iidm.import_.ImportersLoaderList;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.StateManager;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.security.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SecurityAnalysisRunnerTest extends AbstractProjectFileTest {

    private static class SecurityAnalysisFactoryMock implements SecurityAnalysisFactory {
        @Override
        public SecurityAnalysis create(Network network, ComputationManager computationManager, int priority) {
            return new SecurityAnalysis() {
                @Override
                public CompletableFuture<SecurityAnalysisResult> runAsync(ContingenciesProvider contingenciesProvider, String workingStateId, LoadFlowParameters parameters) {
                    LimitViolationsResult preContingencyResult = new LimitViolationsResult(true, ImmutableList.of(new LimitViolation("s1", LimitViolationType.HIGH_VOLTAGE, 400f, 1f, 440f)));
                    SecurityAnalysisResult result = new SecurityAnalysisResult(preContingencyResult, Collections.emptyList());
                    return CompletableFuture.completedFuture(result);
                }

                @Override
                public CompletableFuture<SecurityAnalysisResult> runAsync(ContingenciesProvider contingenciesProvider, String workingStateId) {
                    throw new AssertionError();
                }

                @Override
                public CompletableFuture<SecurityAnalysisResult> runAsync(ContingenciesProvider contingenciesProvider) {
                    throw new AssertionError();
                }
            };
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
        public InputStream get16x16Icon() {
            return new ByteArrayInputStream(new byte[] {});
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
        return ImmutableList.of(new ImportedCaseExtension(importersLoader),
                                new ContingencyStoreExtension(),
                                new SecurityAnalysisRunnerExtension(new SecurityAnalysisParameters()));
    }

    @Override
    protected List<ServiceExtension> getServiceExtensions() {
        return ImmutableList.of(new LocalSecurityAnalysisRunningServiceExtension(new SecurityAnalysisFactoryMock()),
                                new LocalNetworkServiceExtension());
    }

    @Before
    public void setup() {
        super.setup();

        // create network.net
        NodeInfo rootFolderInfo = storage.createRootNodeIfNotExists("root", Folder.PSEUDO_CLASS);
        storage.createNode(rootFolderInfo.getId(), "network", Case.PSEUDO_CLASS, "", Case.VERSION,
                new NodeGenericMetadata().setString(Case.FORMAT, ImporterMock.FORMAT));
    }

    @Test
    public void test() {
        Case aCase = afs.getRootFolder().getChild(Case.class, "network")
                                        .orElseThrow(AssertionError::new);

        // create project in the root folder
        Project project = afs.getRootFolder().createProject("project");

        // import network.net in root folder of the project
        project.getRootFolder().fileBuilder(ImportedCaseBuilder.class)
                .withCase(aCase)
                .build();

        // create contingency list
        ContingencyStore contingencyStore = project.getRootFolder().fileBuilder(ContingencyStoreBuilder.class)
                .withName("contingencies")
                .build();
        contingencyStore.write(Collections.singletonList(new ContingencyImpl("c1", Collections.singletonList(new BranchContingency("l1")))));

        // create a security analysis runner that point to imported case
        SecurityAnalysisRunner runner = project.getRootFolder().fileBuilder(SecurityAnalysisRunnerBuilder.class)
                .withName("sa")
                .withCase("network")
                .withContingencyStore("contingencies")
                .build();

        // check there is no results
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
        SecurityAnalysisResult result = runner.readResult();
        assertNotNull(result);
        assertNotNull(result.getPreContingencyResult());
        assertEquals(1, result.getPreContingencyResult().getLimitViolations().size());
    }
}
