/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dynamicsimulation.dsl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.dsl.DslException;
import com.powsybl.dynamicsimulation.Curves;
import com.powsybl.dynamicsimulation.CurvesProvider;
import com.powsybl.dynamicsimulation.CurvesProviderFactory;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public class GroovyDslCurvesProviderTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private FileSystem fileSystem;

    private Path dslFile;

    private Network network;

    @Before
    public void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        dslFile = fileSystem.getPath("/test.dsl");
        network = EurostagTutorialExample1Factory.create();
    }

    @After
    public void tearDown() throws Exception {
        fileSystem.close();
    }

    private void writeToDslFile(String... lines) throws IOException {
        try (Writer writer = Files.newBufferedWriter(dslFile, StandardCharsets.UTF_8)) {
            writer.write(String.join(System.lineSeparator(), lines));
        }
    }

    private static String createCurvesDsl() {
        return String.join(System.lineSeparator(),
            "for (b in network.busBreakerView.buses) {",
            "    curves {",
            "        modelId b.id",
            "        variables b.id + '_Upu_value'",
            "    }",
            "}");
    }

    @Test
    public void test() throws IOException {
        CurvesProviderFactory factory = new GroovyDslCurvesProviderFactory();
        writeToDslFile(createCurvesDsl());
        List<Curves> curves = factory.create(dslFile)
            .getCurves(network);
        assertEquals(4, curves.size());
        Curves curve = curves.get(0);
        assertEquals("NGEN", curve.getModelId());
        assertEquals(1, curve.getVariables().size());
        String variable = curve.getVariables().iterator().next();
        assertEquals("NGEN_Upu_value", variable);
    }

    @Test
    public void testFactory() throws IOException {
        CurvesProviderFactory factory = new GroovyDslCurvesProviderFactory();
        String dsl = createCurvesDsl();

        InputStream inputStreamDsl = new ByteArrayInputStream(dsl.getBytes(StandardCharsets.UTF_8));
        CurvesProvider providerFromStream = factory.create(inputStreamDsl);
        assertTrue(providerFromStream instanceof GroovyDslCurvesProvider);
        List<Curves> curvesFromStream = providerFromStream.getCurves(network);
        assertEquals(4, curvesFromStream.size());
        Curves curve = curvesFromStream.get(0);
        assertEquals("NGEN", curve.getModelId());
        assertEquals(1, curve.getVariables().size());
        String variable = curve.getVariables().iterator().next();
        assertEquals("NGEN_Upu_value", variable);
    }

    @Test
    public void testModelIdException() throws IOException {
        exception.expect(DslException.class);
        exception.expectMessage("'modelId' field is not set");

        CurvesProviderFactory factory = new GroovyDslCurvesProviderFactory();
        String dsl = String.join(System.lineSeparator(),
            "for (b in network.busBreakerView.buses) {",
            "    curves {",
            "        variables b.id + '_Upu_value'",
            "    }",
            "}");

        InputStream inputStreamDsl = new ByteArrayInputStream(dsl.getBytes(StandardCharsets.UTF_8));
        CurvesProvider providerFromStream = factory.create(inputStreamDsl);
        assertTrue(providerFromStream instanceof GroovyDslCurvesProvider);
        List<Curves> curvesFromStream = providerFromStream.getCurves(network);
    }

    @Test
    public void testVariablesException() throws IOException {
        exception.expect(DslException.class);
        exception.expectMessage("'variables' field is not set");

        CurvesProviderFactory factory = new GroovyDslCurvesProviderFactory();
        String dsl = String.join(System.lineSeparator(),
            "for (b in network.busBreakerView.buses) {",
            "    curves {",
            "        modelId b.id",
            "    }",
            "}");

        InputStream inputStreamDsl = new ByteArrayInputStream(dsl.getBytes(StandardCharsets.UTF_8));
        CurvesProvider providerFromStream = factory.create(inputStreamDsl);
        assertTrue(providerFromStream instanceof GroovyDslCurvesProvider);
        List<Curves> curvesFromStream = providerFromStream.getCurves(network);
    }

    @Test
    public void testElementNotFoundException() throws IOException {
        exception.expect(DslException.class);
        exception.expectMessage("Curves is invalid: Equipment 'dummy' not found");

        CurvesProviderFactory factory = new GroovyDslCurvesProviderFactory();
        String dsl = String.join(System.lineSeparator(),
            "for (b in network.busBreakerView.buses) {",
            "    curves {",
            "        modelId 'dummy'",
            "        variables b.id + '_Upu_value'",
            "    }",
            "}");

        InputStream inputStreamDsl = new ByteArrayInputStream(dsl.getBytes(StandardCharsets.UTF_8));
        CurvesProvider providerFromStream = factory.create(inputStreamDsl);
        assertTrue(providerFromStream instanceof GroovyDslCurvesProvider);
        List<Curves> curvesFromStream = providerFromStream.getCurves(network);
    }
}
