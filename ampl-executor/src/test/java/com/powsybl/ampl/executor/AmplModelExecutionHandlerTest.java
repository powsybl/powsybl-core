package com.powsybl.ampl.executor;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.ampl.converter.NetworkApplier;
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
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

public class AmplModelExecutionHandlerTest {

    @Test
    public void test() throws Exception {
        FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
        Files.createDirectory(fs.getPath("/workingDir"));
        // Test data
        Network network = EurostagTutorialExample1Factory.create();
        DummyAmplModel model = new DummyAmplModel();
        AmplConfig cfg = getAmplConfig();
        // Test config
        String variantId = network.getVariantManager().getWorkingVariantId();
        ComputationManager manager = new LocalComputationManager(
                new LocalComputationConfig(fs.getPath("/workingDir")),
                new MockAmplLocalExecutor(List.of("output_generators.txt")),
                ForkJoinPool.commonPool());
        ExecutionEnvironment env = ExecutionEnvironment.createDefault().setWorkingDirPrefix("ampl_").setDebug(true);
        // Test execution
        AmplModelExecutionHandler handler = new AmplModelExecutionHandler(model, network, variantId, cfg);
        CompletableFuture<AmplResults> result = manager.execute(env, handler);
        AmplResults amplState = result.join();
        // Test assert
        Assert.assertTrue("AmplResult must be OK.", amplState.isSuccess());
        // Test cleaning
        manager.close();
    }

    @Test
    public void testUtilities() {
        String amplBinPath = AmplModelExecutionHandler.getAmplBinPath(getAmplConfig());
        Assert.assertEquals("Ampl binary is wrongly named ",
                "/home/test/ampl" + File.separator + "ampl",
                amplBinPath);
        // next instruction must not throw
        AmplModelExecutionHandler.createAmplRunCommand(getAmplConfig(), new MockAmplModel());
    }

    private AmplConfig getAmplConfig() {
        FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
        InMemoryPlatformConfig platformConfig = new InMemoryPlatformConfig(fs);
        MapModuleConfig moduleConfig = platformConfig.createModuleConfig("ampl");
        moduleConfig.setStringProperty("homeDir", "/home/test/ampl");
        return AmplConfig.getConfig(platformConfig);
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
                    Assert.assertNotNull("An Ampl result resources is missing : " + amplResource, amplResourceStream);
                    Files.copy(amplResourceStream, workingDir.resolve(amplResource), StandardCopyOption.REPLACE_EXISTING);
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
            return Arrays.asList("testampl.run", "foo.run", "bar.run");
        }

        @Override
        public String getOutputFilePrefix() {
            throw new IllegalStateException("Should not be called to create ampl command");
        }

        @Override
        public NetworkApplier getNetworkApplier() {
            throw new IllegalStateException("Should not be called to create ampl command");
        }
    }

}
