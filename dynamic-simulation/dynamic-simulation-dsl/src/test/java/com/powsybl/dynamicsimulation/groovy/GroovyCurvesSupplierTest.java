/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.dynamicsimulation.groovy;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.dynamicsimulation.Curve;
import com.powsybl.dynamicsimulation.CurvesSupplier;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Mathieu Bague {@literal <mathieu.bague@rte-france.com>}
 */
class GroovyCurvesSupplierTest {

    private FileSystem fileSystem;

    @BeforeEach
    void setup() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());

        Files.copy(Objects.requireNonNull(getClass().getResourceAsStream("/curves.groovy")), fileSystem.getPath("/curves.groovy"));
    }

    @AfterEach
    void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    void test() {
        Network network = EurostagTutorialExample1Factory.create();

        List<CurveGroovyExtension> extensions = GroovyExtension.find(CurveGroovyExtension.class, "dummy");
        assertEquals(1, extensions.size());
        assertInstanceOf(DummyCurveGroovyExtension.class, extensions.get(0));

        CurvesSupplier supplier = new GroovyCurvesSupplier(fileSystem.getPath("/curves.groovy"), extensions);

        testCurveSupplier(network, supplier);
    }

    @Test
    void testWithInputStream() {
        Network network = EurostagTutorialExample1Factory.create();

        List<CurveGroovyExtension> extensions = GroovyExtension.find(CurveGroovyExtension.class, "dummy");
        assertEquals(1, extensions.size());
        assertInstanceOf(DummyCurveGroovyExtension.class, extensions.get(0));

        CurvesSupplier supplier = new GroovyCurvesSupplier(getClass().getResourceAsStream("/curves.groovy"), extensions);

        testCurveSupplier(network, supplier);
    }

    private static void testCurveSupplier(Network network, CurvesSupplier supplier) {
        List<Curve> curves = supplier.get(network);
        assertEquals(2, curves.size());

        assertInstanceOf(DummyCurve.class, curves.get(0));
        DummyCurve curve1 = (DummyCurve) curves.get(0);
        assertEquals("id", curve1.getId());
        assertEquals("variable", curve1.getVariable());

        assertInstanceOf(DummyCurve.class, curves.get(1));
        DummyCurve curve2 = (DummyCurve) curves.get(1);
        assertEquals("LOAD", curve2.getId());
        assertEquals("p0", curve2.getVariable());
    }
}
