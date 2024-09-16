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
import com.powsybl.dynamicsimulation.OutputVariable;
import com.powsybl.dynamicsimulation.OutputVariablesSupplier;
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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Mathieu Bague {@literal <mathieu.bague@rte-france.com>}
 */
class GroovyOutputVariablesSupplierTest {

    private FileSystem fileSystem;
    private Network network;

    @BeforeEach
    void setup() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        Files.copy(Objects.requireNonNull(getClass().getResourceAsStream("/outputVariables.groovy")), fileSystem.getPath("/outputVariables.groovy"));
        network = EurostagTutorialExample1Factory.create();
    }

    @AfterEach
    void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    void test() {
        List<OutputVariableGroovyExtension> extensions = GroovyExtension.find(OutputVariableGroovyExtension.class, "dummy");
        assertThat(extensions).hasSize(1).hasOnlyElementsOfType(DummyOutputVariableGroovyExtension.class);
        OutputVariablesSupplier supplier = new GroovyOutputVariablesSupplier(fileSystem.getPath("/outputVariables.groovy"), extensions);
        testCurveSupplier(supplier.get(network));
    }

    @Test
    void testWithInputStream() {
        List<OutputVariableGroovyExtension> extensions = GroovyExtension.find(OutputVariableGroovyExtension.class, "dummy");
        assertThat(extensions).hasSize(1).hasOnlyElementsOfType(DummyOutputVariableGroovyExtension.class);
        OutputVariablesSupplier supplier = new GroovyOutputVariablesSupplier(getClass().getResourceAsStream("/outputVariables.groovy"), extensions);
        testCurveSupplier(supplier.get(network));
    }

    private static void testCurveSupplier(List<OutputVariable> outputVariables) {
        assertThat(outputVariables).hasSize(3).containsExactly(
                new DummyOutputVariable("id", "variable", OutputVariable.OutputType.CURVE),
                new DummyOutputVariable("id", "variable", OutputVariable.OutputType.FSV),
                new DummyOutputVariable("LOAD", "p0", OutputVariable.OutputType.CURVE)
        );
    }
}
