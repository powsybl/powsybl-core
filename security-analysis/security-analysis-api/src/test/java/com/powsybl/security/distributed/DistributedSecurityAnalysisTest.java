package com.powsybl.security.distributed;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.computation.CommandExecution;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.ExecutionHandler;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.StateManagerConstants;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.security.SecurityAnalysis;
import com.powsybl.security.SecurityAnalysisParameters;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * @author Sylvain Leclerc <sylvain.leclerc@rte-france.com>
 */
public class DistributedSecurityAnalysisTest {

    private FileSystem fileSystem;
    private Path workingDir;

    @Before
    public void setUp() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        workingDir = fileSystem.getPath("/working-dir");
        Files.createDirectory(workingDir);
    }

    @After
    public void tearDown() throws IOException {
        fileSystem.close();
    }

    /**
     * Checks that input files are written to working dir
     * and that execution count is correctly set.
     */
    @Test
    public void test() throws IOException {

        ComputationManager cm = mock(ComputationManager.class);
        Network network = EurostagTutorialExample1Factory.create();

        SecurityAnalysis analysis = new DistributedSecurityAnalysis(network, cm, Collections.emptyList(), 5);

        ContingenciesProvider contingencies = new ContingenciesProvider() {
            @Override
            public List<Contingency> getContingencies(Network network) {
                return IntStream.range(1, 6)
                        .mapToObj(i -> new Contingency("contingency-" + i))
                        .collect(Collectors.toList());
            }

            @Override
            public String asScript() {
                return "";
            }
        };

        analysis.run(StateManagerConstants.INITIAL_STATE_ID, new SecurityAnalysisParameters(), contingencies);

        //Capture the execution handler
        ArgumentCaptor<ExecutionHandler> capt = ArgumentCaptor.forClass(ExecutionHandler.class);
        verify(cm, times(1)).execute(any(), capt.capture());

        //checks methods of the execution handler
        List<CommandExecution> cmd = capt.getValue().before(workingDir);

        assertTrue(Files.exists(workingDir.resolve("network.xiidm")));
        assertTrue(Files.exists(workingDir.resolve("contingencies.groovy")));
        assertTrue(Files.exists(workingDir.resolve("parameters.json")));

        assertEquals(1, cmd.size());
        assertEquals(5, cmd.get(0).getExecutionCount());
    }

}
