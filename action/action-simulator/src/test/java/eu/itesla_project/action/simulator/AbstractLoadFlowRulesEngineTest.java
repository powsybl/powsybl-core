/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.action.simulator;

import eu.itesla_project.action.dsl.ActionDb;
import eu.itesla_project.action.dsl.ActionDslLoader;
import eu.itesla_project.action.simulator.loadflow.AbstractLoadFlowActionSimulatorObserver;
import eu.itesla_project.action.simulator.loadflow.LoadFlowActionSimulator;
import eu.itesla_project.action.simulator.loadflow.LoadFlowActionSimulatorConfig;
import eu.itesla_project.action.simulator.loadflow.LoadFlowActionSimulatorObserver;
import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.loadflow.api.LoadFlow;
import eu.itesla_project.loadflow.api.LoadFlowFactory;
import eu.itesla_project.loadflow.api.LoadFlowResult;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyShell;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractLoadFlowRulesEngineTest {

    protected Network network;

    protected ActionDb actionDb;

    protected LoadFlowActionSimulator engine;

    protected abstract Network createNetwork();

    protected LoadFlowActionSimulatorObserver createObserver() {
        return new AbstractLoadFlowActionSimulatorObserver();
    }

    protected abstract String getDslFile();

    @Before
    public void setUp() throws Exception {
        network = createNetwork();
        ComputationManager computationManager = Mockito.mock(ComputationManager.class);
        LoadFlow loadFlow = Mockito.mock(LoadFlow.class);
        LoadFlowFactory loadFlowFactory = Mockito.mock(LoadFlowFactory.class);
        Mockito.when(loadFlowFactory.create(Mockito.any(Network.class), Mockito.any(ComputationManager.class), Mockito.anyInt()))
                .thenReturn(loadFlow);
        LoadFlowResult loadFlowResult = Mockito.mock(LoadFlowResult.class);
        Mockito.when(loadFlowResult.isOk())
                .thenReturn(true);
        Mockito.when(loadFlow.getName()).thenReturn("load flow mock");
        Mockito.when(loadFlow.run())
                .thenReturn(loadFlowResult);
        LoadFlowActionSimulatorObserver observer = createObserver();
        GroovyCodeSource src = new GroovyCodeSource(new InputStreamReader(getClass().getResourceAsStream(getDslFile())), "test", GroovyShell.DEFAULT_CODE_BASE);
        actionDb = new ActionDslLoader(src).load(network);
        engine = new LoadFlowActionSimulator(network, computationManager, new LoadFlowActionSimulatorConfig(LoadFlowFactory.class, 3, false), observer) {
            @Override
            protected LoadFlowFactory newLoadLoadLowFactory() throws InstantiationException, IllegalAccessException {
                return loadFlowFactory;
            }
        };
    }

    @After
    public void tearDown() throws IOException {
    }

}
