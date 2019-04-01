/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
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
import com.powsybl.commons.io.table.TableFormatterConfig;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.loadflow.mock.LoadFlowFactoryMock;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyShell;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mockito;

import java.io.InputStreamReader;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractLoadFlowRulesEngineTest {

    protected Network network;

    protected ActionDb actionDb;

    protected LoadFlowActionSimulator engine;

    protected final ComputationManager computationManager = Mockito.mock(ComputationManager.class);

    protected abstract Network createNetwork();

    protected LoadFlowActionSimulatorObserver createObserver() {
        return new DefaultLoadFlowActionSimulatorObserver();
    }

    protected abstract String getDslFile();

    @Before
    public void setUp() {
        network = createNetwork();
        LoadFlowActionSimulatorObserver observer = createObserver();
        GroovyCodeSource src = new GroovyCodeSource(new InputStreamReader(getClass().getResourceAsStream(getDslFile())), "test", GroovyShell.DEFAULT_CODE_BASE);
        actionDb = new ActionDslLoader(src).load(network);
        engine = new LoadFlowActionSimulator(network, computationManager, new LoadFlowActionSimulatorConfig(LoadFlowFactoryMock.class, 3, false, false),
                new TableFormatterConfig(), applyIfWorks(), observer);
    }

    protected boolean applyIfWorks() {
        return true;
    }

    @After
    public void tearDown() {
    }

}
