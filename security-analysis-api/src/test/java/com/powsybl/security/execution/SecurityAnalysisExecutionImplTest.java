/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.execution;

import com.powsybl.computation.ComputationManager;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class SecurityAnalysisExecutionImplTest {

    private SecurityAnalysis analysis;
    private SecurityAnalysisFactory factory;

    private LimitViolationFilter filter;
    private LimitViolationDetector detector;
    private ContingenciesProvider contingencies;
    private SecurityAnalysisParameters parameters;
    private SecurityAnalysisExecution execution;
    private Network network;
    private ComputationManager computationManager;
    private SecurityAnalysisExecutionInput input;

    @Before
    public void setUp() {
        analysis = mock(SecurityAnalysis.class);
        factory = mock(SecurityAnalysisFactory.class);
        when(factory.create(any(), any(), any(), any(), anyInt()))
                .thenReturn(analysis);

        filter = Mockito.mock(LimitViolationFilter.class);
        detector = Mockito.mock(LimitViolationDetector.class);
        contingencies = Mockito.mock(ContingenciesProvider.class);
        parameters = Mockito.mock(SecurityAnalysisParameters.class);

        execution = new SecurityAnalysisExecutionImpl(factory,
            execInput -> new SecurityAnalysisInput(execInput.getNetworkVariant())
                        .setFilter(filter)
                        .setDetector(detector)
                        .setContingencies(contingencies)
                        .setParameters(parameters)
        );

        network = mock(Network.class);
        computationManager = mock(ComputationManager.class);
        input = new SecurityAnalysisExecutionInput();
        input.setNetworkVariant(network, "variantId");
    }

    @Test
    public void checkExecutionCallAndArguments() {

        execution.execute(computationManager, input);

        Mockito.verify(factory, Mockito.times(1))
                .create(same(network), same(detector), same(filter), same(computationManager), anyInt());
        Mockito.verify(analysis, Mockito.times(1))
                .run(eq("variantId"), same(parameters), same(contingencies));
    }

    @Test
    public void checkExecutionWithLogCallAndArguments() {

        execution.executeWithLog(computationManager, input);
        Mockito.verify(factory, Mockito.times(1))
                .create(same(network), same(detector), same(filter), same(computationManager), anyInt());
        Mockito.verify(analysis, Mockito.times(1))
                .runWithLog(eq("variantId"), same(parameters), same(contingencies));
    }
}
