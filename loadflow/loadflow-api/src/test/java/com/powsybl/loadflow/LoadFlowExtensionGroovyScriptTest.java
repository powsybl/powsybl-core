/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow;

import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VariantManager;
import com.powsybl.iidm.network.VariantManagerConstants;
import com.powsybl.scripting.groovy.GroovyScriptExtension;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LoadFlowExtensionGroovyScriptTest {

    private Network fooNetwork;
    private ComputationManager computationManager;

    @Before
    public void setUp() throws Exception {
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

        computationManager = Mockito.mock(ComputationManager.class);
    }

    protected Reader getCodeReader() {
        return new StringReader("r = loadFlow(n)\n" +
                                "print r.ok");
    }

    protected String getExpectedOutput() {
        return "true";
    }

    protected List<GroovyScriptExtension> getExtensions() {
        return Arrays.asList(new LoadFlowGroovyScriptExtension(new LoadFlowParameters()),
            (binding, computationManager) -> binding.setVariable("n", fooNetwork));
    }

    @Test
    public void test() throws IOException {
        Binding binding = new Binding();
        StringWriter out = null;
        try (StringWriter writer = new StringWriter()) {
            binding.setVariable("out", writer);

            CompilerConfiguration conf = new CompilerConfiguration();
            getExtensions().forEach(it -> it.load(binding, computationManager));
            GroovyShell shell = new GroovyShell(binding, conf);
            Object evaluate = shell.evaluate(getCodeReader());
            out = (StringWriter) binding.getProperty("out");

            assertEquals(getExpectedOutput(), out.toString());
        } finally {
            out.close();
        }
    }
}
