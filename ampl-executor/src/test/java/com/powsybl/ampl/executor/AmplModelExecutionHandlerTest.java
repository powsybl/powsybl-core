/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ampl.executor;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.ampl.converter.AmplNetworkUpdaterFactory;
import com.powsybl.ampl.converter.AmplReadableElement;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.MapModuleConfig;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.ExecutionEnvironment;
import com.powsybl.computation.local.LocalCommandExecutor;
import com.powsybl.computation.local.LocalComputationConfig;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Nicolas Pierre <nicolas.pierre@artelys.com>
 */
class AmplModelExecutionHandlerTest {

    @Test
    void test() throws Exception {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            Files.createDirectory(fs.getPath("/workingDir"));
            // Test data
            Network network = EurostagTutorialExample1Factory.create();
            DummyAmplModel model = new DummyAmplModel();
            AmplConfig cfg = getAmplConfig();
            // Test config
            String variantId = network.getVariantManager().getWorkingVariantId();
            try (ComputationManager manager = new LocalComputationManager(
                    new LocalComputationConfig(fs.getPath("/workingDir")),
                    new MockAmplLocalExecutor(List.of("output_generators.txt", "output_indic.txt")),
                    ForkJoinPool.commonPool())) {
                ExecutionEnvironment env = ExecutionEnvironment.createDefault()
                                                               .setWorkingDirPrefix("ampl_")
                                                               .setDebug(true);
                // Test execution
                AmplModelExecutionHandler handler = new AmplModelExecutionHandler(model, network, variantId, cfg,
                        new EmptyAmplParameters());
                CompletableFuture<AmplResults> result = manager.execute(env, handler);
                AmplResults amplState = result.join();
                // Test assert
                assertTrue(amplState.isSuccess(), "AmplResult must be OK.");
                assertEquals("OK", amplState.getIndicators().get("STATUS"), "AmplResult must contain indicators.");
            }
        }
    }

    @Test
    void testConvergingModel() throws Exception {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            Files.createDirectory(fs.getPath("/workingDir"));
            // Test data
            Network network = EurostagTutorialExample1Factory.create();
            DummyAmplModel model = new DummyAmplModel();
            AmplConfig cfg = getAmplConfig();
            // Test config
            String variantId = network.getVariantManager().getWorkingVariantId();
            try (ComputationManager manager = new LocalComputationManager(
                    new LocalComputationConfig(fs.getPath("/workingDir")), new MockAmplLocalExecutor(
                    List.of("output_generators.txt", "output_indic.txt", "simple_output.txt")),
                    ForkJoinPool.commonPool())) {
                ExecutionEnvironment env = ExecutionEnvironment.createDefault()
                                                               .setWorkingDirPrefix("ampl_")
                                                               .setDebug(true);
                // Test execution
                SimpleAmplParameters parameters = new SimpleAmplParameters();
                AmplModelExecutionHandler handler = new AmplModelExecutionHandler(model, network, variantId, cfg,
                        parameters);
                CompletableFuture<AmplResults> result = manager.execute(env, handler);
                AmplResults amplState = result.join();
                // Test assert
                assertTrue(amplState.isSuccess(), "AmplResult must be OK.");
                assertTrue(parameters.isReadingDone(), "Did not read custom result file.");
            }
        }
    }

    @Test
    void testInputParametersWriting() throws Exception {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            Files.createDirectory(fs.getPath("/workingDir"));
            // Test data
            Network network = EurostagTutorialExample1Factory.create();
            DummyAmplModel model = new DummyAmplModel();
            AmplConfig cfg = getAmplConfig();
            // Test config
            String variantId = network.getVariantManager().getWorkingVariantId();
            AmplParameters parameters = new SimpleAmplParameters();
            AmplModelExecutionHandler handler = new AmplModelExecutionHandler(model, network, variantId, cfg,
                    parameters);
            // Test execution
            handler.before(fs.getPath("/workingDir"));
            // Test assert
            assertEquals("some_content", Files.readString(fs.getPath("/workingDir/simple_input.txt")),
                    "Custom file input is not written.");
        }
    }

    @Test
    void testUtilities() {
        String amplBinPath = AmplModelExecutionHandler.getAmplBinPath(getAmplConfig());
        Assertions.assertEquals("/home/test/ampl" + File.separator + "ampl", amplBinPath,
                "Ampl binary is wrongly named ");
        // next instruction must not throw
        AmplModelExecutionHandler.createAmplRunCommand(getAmplConfig(), new MockAmplModel());
    }

    private AmplConfig getAmplConfig() {
        FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
        InMemoryPlatformConfig platformConfig = new InMemoryPlatformConfig(fs);
        MapModuleConfig moduleConfig = platformConfig.createModuleConfig("ampl");
        moduleConfig.setStringProperty("homeDir", "/home/test/ampl");
        return AmplConfig.load(platformConfig);
    }

    /**
     * This class mocks a LocalCommandExecutor, it will paste every resource files listed in the constructor
     * to the directory instead of running the ampl.
     */
    private static class MockAmplLocalExecutor implements LocalCommandExecutor {
        private final List<String> amplResourcesPathes;

        public MockAmplLocalExecutor(List<String> amplResourcesPathes) {
            this.amplResourcesPathes = amplResourcesPathes;
        }

        @Override
        public int execute(String program, List<String> args, Path outFile, Path errFile, Path workingDir, Map<String, String> env) throws IOException {
            for (String amplResource : amplResourcesPathes) {
                try (InputStream amplResourceStream = this.getClass().getClassLoader().getResourceAsStream(amplResource)) {
                    Assertions.assertNotNull(amplResourceStream,
                            "An Ampl result resources is missing : " + amplResource);
                    Files.copy(amplResourceStream, workingDir.resolve(amplResource),
                            StandardCopyOption.REPLACE_EXISTING);
                }
            }
            return 0;
        }

        @Override
        public void stop(Path workingDir) {
            //do nothing
        }

        @Override
        public void stopForcibly(Path workingDir) {
            //do nothing
        }
    }

    private static class MockAmplModel extends AbstractAmplModel {
        @Override
        public List<Pair<String, InputStream>> getModelAsStream() {
            throw new IllegalStateException("Should not be called to create ampl command");
        }

        @Override
        public List<String> getAmplRunFiles() {
            return List.of("testampl.run", "foo.run", "bar.run");
        }

        @Override
        public String getOutputFilePrefix() {
            throw new IllegalStateException("Should not be called to create ampl command");
        }

        @Override
        public AmplNetworkUpdaterFactory getNetworkApplierFactory() {
            throw new IllegalStateException("Should not be called to create ampl command");
        }

        @Override
        public Collection<AmplReadableElement> getAmplReadableElement() {
            throw new IllegalStateException("Should not be called to create ampl command");
        }

        @Override
        public boolean checkModelConvergence(Map<String, String> metrics) {
            return true;
        }
    }

}
