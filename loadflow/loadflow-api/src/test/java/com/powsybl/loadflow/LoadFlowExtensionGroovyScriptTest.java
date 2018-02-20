/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow;

import com.powsybl.computation.ComputationManager;
import com.powsybl.scripting.groovy.AbstractGroovyScriptTest;
import com.powsybl.scripting.groovy.GroovyScriptExtension;
import com.powsybl.iidm.network.Network;
import org.junit.Before;
import org.mockito.Mockito;

import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LoadFlowExtensionGroovyScriptTest extends AbstractGroovyScriptTest {

    private LoadFlowFactory loadFlowFactory;

    private Network fooNetwork;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        // create loadflow factory mock
        LoadFlowResult result = Mockito.mock(LoadFlowResult.class);
        Mockito.when(result.isOk())
                .thenReturn(true);
        LoadFlow loadFlow = Mockito.mock(LoadFlow.class);
        Mockito.when(loadFlow.run(Mockito.any()))
                .thenReturn(result);
        loadFlowFactory = Mockito.mock(LoadFlowFactory.class);
        Mockito.when(loadFlowFactory.create(Mockito.any(Network.class), Mockito.any(ComputationManager.class), Mockito.anyInt()))
                .thenReturn(loadFlow);

        // create network mock
        fooNetwork = Mockito.mock(Network.class);
        Mockito.when(fooNetwork.getId())
                .thenReturn("test");
    }

    @Override
    protected Reader getCodeReader() {
        return new StringReader("r = loadFlow(n)\n" +
                                "print r.ok");
    }

    @Override
    protected String getExpectedOutput() {
        return "true";
    }

    @Override
    protected List<GroovyScriptExtension> getExtensions() {
        return Arrays.asList(new LoadFlowGroovyScriptExtension(loadFlowFactory, new LoadFlowParameters()),
            (binding, computationManager) -> binding.setVariable("n", fooNetwork));
    }
}
