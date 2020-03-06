/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.scripting;

import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VariantManager;
import com.powsybl.iidm.network.VariantManagerConstants;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.scripting.AbstractGroovyScriptTest;
import com.powsybl.scripting.groovy.GroovyScriptExtension;
import groovy.lang.Binding;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LoadFlowExtensionGroovyScriptTest extends AbstractGroovyScriptTest {

    private Network fooNetwork;
    private ComputationManager computationManager;

    @Before
    public void setUp() {
        // create variant manager
        VariantManager variantManager = Mockito.mock(VariantManager.class);
        Mockito.when(variantManager.getWorkingVariantId())
                .thenReturn(VariantManagerConstants.INITIAL_VARIANT_ID);

        // create network mock
        fooNetwork = Mockito.mock(Network.class);
        Mockito.when(fooNetwork.getId())
                .thenReturn("test");
        Mockito.when(fooNetwork.getVariantManager())
                .thenReturn(variantManager);
    }

    protected String getCode() {
        return "r = loadFlow(n)\n" +
                "print r.ok";
    }

    protected String getExpectedOutput() {
        return "true";
    }

    protected List<GroovyScriptExtension> getExtensions() {
        GroovyScriptExtension ext = new GroovyScriptExtension() {
            @Override
            public void load(Binding binding, ComputationManager computationManager) {
                binding.setVariable("n", fooNetwork);
            }

            @Override
            public void unload() {
            }
        };

        return Arrays.asList(new LoadFlowGroovyScriptExtension(new LoadFlowParameters()), ext);
    }

    @Test
    public void test() {
        doTest();
    }
}
