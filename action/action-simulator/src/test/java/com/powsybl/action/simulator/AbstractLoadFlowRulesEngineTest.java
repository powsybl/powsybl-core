/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.simulator;

import com.powsybl.action.dsl.ActionDb;
import com.powsybl.action.dsl.ActionDslLoader;
import com.powsybl.action.simulator.loadflow.DefaultLoadFlowActionSimulatorObserver;
import com.powsybl.action.simulator.loadflow.LoadFlowActionSimulator;
import com.powsybl.action.simulator.loadflow.LoadFlowActionSimulatorConfig;
import com.powsybl.action.simulator.loadflow.LoadFlowActionSimulatorObserver;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.loadflow.LoadFlow;
import com.powsybl.loadflow.LoadFlowFactory;
import com.powsybl.loadflow.LoadFlowResult;
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

    protected ComputationManager computationManager;

    protected LoadFlowFactory loadFlowFactory;

    protected LoadFlowActionSimulatorObserver observer;

    protected LoadFlowResult loadFlowResult;

    protected LoadFlow loadFlow;

    protected abstract Network createNetwork();

    protected LoadFlowActionSimulatorObserver createObserver() {
        return new DefaultLoadFlowActionSimulatorObserver();
    }

    protected abstract String getDslFile();

    public void initialize() throws Exception {
        network = createNetwork();
        computationManager = Mockito.mock(ComputationManager.class);
        loadFlow = Mockito.mock(LoadFlow.class);
        loadFlowFactory = Mockito.mock(LoadFlowFactory.class);
        Mockito.when(loadFlowFactory.create(Mockito.any(Network.class), Mockito.any(ComputationManager.class), Mockito.anyInt()))
                .thenReturn(loadFlow);
        loadFlowResult = Mockito.mock(LoadFlowResult.class);
        Mockito.when(loadFlowResult.isOk())
                .thenReturn(true);
        Mockito.when(loadFlow.getName()).thenReturn("load flow mock");
        Mockito.when(loadFlow.run())
                .thenReturn(loadFlowResult);
        observer = createObserver();
        GroovyCodeSource src = new GroovyCodeSource(new InputStreamReader(getClass().getResourceAsStream(getDslFile())), "test", GroovyShell.DEFAULT_CODE_BASE);
        actionDb = new ActionDslLoader(src).load(network);
    }

    @Before
    public void setUp() throws Exception {
        initialize();
        engine = new LoadFlowActionSimulator(network, computationManager, new LoadFlowActionSimulatorConfig(LoadFlowFactory.class, 3, false), observer) {
            @Override
            protected LoadFlowFactory newLoadFlowFactory() {
                return loadFlowFactory;
            }
        };
    }

    @After
    public void tearDown() throws IOException {
    }

}
