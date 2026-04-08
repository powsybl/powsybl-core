/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.ial.simulator;

import com.powsybl.action.ial.dsl.ActionDb;
import com.powsybl.action.ial.dsl.ActionDslLoader;
import com.powsybl.action.ial.simulator.loadflow.DefaultLoadFlowActionSimulatorObserver;
import com.powsybl.action.ial.simulator.loadflow.LoadFlowActionSimulator;
import com.powsybl.action.ial.simulator.loadflow.LoadFlowActionSimulatorConfig;
import com.powsybl.action.ial.simulator.loadflow.LoadFlowActionSimulatorObserver;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.loadflow.LoadFlowParameters;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyShell;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import java.io.InputStreamReader;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
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

    @BeforeEach
    void setUp() {
        network = createNetwork();
        LoadFlowActionSimulatorObserver observer = createObserver();
        GroovyCodeSource src = new GroovyCodeSource(new InputStreamReader(getClass().getResourceAsStream(getDslFile())), "test", GroovyShell.DEFAULT_CODE_BASE);
        actionDb = new ActionDslLoader(src).load(network);
        engine = new LoadFlowActionSimulator(network, computationManager, new LoadFlowActionSimulatorConfig("LoadFlowMock", 3, false, false),
                applyIfWorks(), new LoadFlowParameters(), observer);
    }

    protected boolean applyIfWorks() {
        return true;
    }

    @AfterEach
    void tearDown() {
    }

}
