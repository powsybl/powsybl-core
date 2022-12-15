/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.dynamicsimulation.groovy;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.dynamicsimulation.Curve;
import com.powsybl.dynamicsimulation.CurvesSupplier;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class GroovyCurvesSupplierTest {

    private FileSystem fileSystem;

    @Before
    public void setup() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());

        Files.copy(getClass().getResourceAsStream("/curves.groovy"), fileSystem.getPath("/curves.groovy"));
    }

    @After
    public void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    public void test() {
        Network network = EurostagTutorialExample1Factory.create();

        List<CurveGroovyExtension> extensions = GroovyExtension.find(CurveGroovyExtension.class, "dummy");
        assertEquals(1, extensions.size());
        assertTrue(extensions.get(0) instanceof DummyCurveGroovyExtension);

        CurvesSupplier supplier = new GroovyCurvesSupplier(fileSystem.getPath("/curves.groovy"), extensions);

        testCurveSupplier(network, supplier);
    }

    @Test
    public void testWithInputStream() {
        Network network = EurostagTutorialExample1Factory.create();

        List<CurveGroovyExtension> extensions = GroovyExtension.find(CurveGroovyExtension.class, "dummy");
        assertEquals(1, extensions.size());
        assertTrue(extensions.get(0) instanceof DummyCurveGroovyExtension);

        CurvesSupplier supplier = new GroovyCurvesSupplier(getClass().getResourceAsStream("/curves.groovy"), extensions);

        testCurveSupplier(network, supplier);
    }

    private static void testCurveSupplier(Network network, CurvesSupplier supplier) {
        List<Curve> curves = supplier.get(network);
        assertEquals(2, curves.size());

        assertTrue(curves.get(0) instanceof DummyCurve);
        DummyCurve curve1 = (DummyCurve) curves.get(0);
        assertEquals("id", curve1.getId());
        assertEquals("variable", curve1.getVariable());

        assertTrue(curves.get(1) instanceof DummyCurve);
        DummyCurve curve2 = (DummyCurve) curves.get(1);
        assertEquals("LOAD", curve2.getId());
        assertEquals("p0", curve2.getVariable());
    }
}
