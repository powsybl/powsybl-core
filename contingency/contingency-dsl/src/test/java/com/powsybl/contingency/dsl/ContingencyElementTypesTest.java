/**
 * Copyright (c) 2020,2021, RTE (http://www.rte-france.com)
 * Copyright (c) 2024, Artelys (http://www.artelys.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency.dsl;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.contingency.*;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * @author Yichen TANG {@literal <yichen.tang at rte-france.com>}
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
class ContingencyElementTypesTest {

    private FileSystem fileSystem;

    private Path dslFile;

    @BeforeEach
    void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        dslFile = fileSystem.getPath("/test.dsl");
    }

    @AfterEach
    void tearDown() throws Exception {
        fileSystem.close();
    }

    private void test(Network network, String contingencyId, String equipmentId, Class<?> contingencyElementClass) throws IOException {
        Files.writeString(dslFile, String.format("""
                        contingency('%s') {
                            equipments '%s'
                        }""", contingencyId, equipmentId),
                StandardCharsets.UTF_8);
        List<Contingency> contingencies = new GroovyDslContingenciesProvider(dslFile)
                .getContingencies(network);
        assertEquals(1, contingencies.size());
        Contingency contingency = contingencies.get(0);
        assertEquals(contingencyId, contingency.getId());
        assertEquals(0, contingency.getExtensions().size());
        assertEquals(1, contingency.getElements().size());
        ContingencyElement element = contingency.getElements().get(0);
        assertInstanceOf(contingencyElementClass, element);
        assertEquals(equipmentId, element.getId());
    }

    @Test
    void generatorTest() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        test(network, "GEN_CONTINGENCY", "GEN", GeneratorContingency.class);
    }

    @Test
    void loadTest() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        test(network, "LOAD_CONTINGENCY", "LOAD", LoadContingency.class);
    }

    @Test
    void twoWindingsTransformerTest() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        test(network, "2WT_CONTINGENCY", "NGEN_NHV1", TwoWindingsTransformerContingency.class);
    }

    @Test
    void threeWindingsTransformerTest() throws IOException {
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        test(network, "3WT_CONTINGENCY", "3WT", ThreeWindingsTransformerContingency.class);
    }

    @Test
    void lineTest() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        test(network, "LINE_CONTINGENCY", "NHV1_NHV2_1", LineContingency.class);
    }

    @Test
    void tieLineTest() throws IOException {
        Network network = EurostagTutorialExample1Factory.createWithTieLine();
        test(network, "TIELINE_CONTINGENCY", "NHV1_NHV2_1", TieLineContingency.class);
    }

    @Test
    void danglingLineTest() throws IOException {
        Network network = DanglingLineNetworkFactory.create();
        test(network, "DL_CONTINGENCY", "DL", DanglingLineContingency.class);
    }

    @Test
    void shuntCompensatorTest() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        network.getVoltageLevel("VLLOAD")
                .newShuntCompensator()
                .setId("SC")
                .setConnectableBus("NLOAD")
                .setBus("NLOAD")
                .setSectionCount(1)
                .newLinearModel()
                .setBPerSection(1e-5)
                .setMaximumSectionCount(1)
                .add()
                .add();
        test(network, "SC_CONTINGENCY", "SC", ShuntCompensatorContingency.class);
    }

    @Test
    void busTest() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        test(network, "BUS_CONTINGENCY", "NLOAD", BusContingency.class);
    }

    @Test
    void busbarSectionTest() throws IOException {
        Network network = FourSubstationsNodeBreakerFactory.create();
        test(network, "BBS_CONTINGENCY", "S1VL1_BBS", BusbarSectionContingency.class);
    }

    @Test
    void svcTest() throws IOException {
        Network network = SvcTestCaseFactory.create();
        test(network, "SVC_CONTINGENCY", "SVC2", StaticVarCompensatorContingency.class);
    }

    @Test
    void switchTest() throws IOException {
        Network network = FourSubstationsNodeBreakerFactory.create();
        test(network, "SWITCH_CONTINGENCY", "S1VL1_LD1_BREAKER", SwitchContingency.class);
    }

    @Test
    void hvdcTest() throws IOException {
        Network network = HvdcTestNetwork.createLcc();
        test(network, "HVDC_LINE_CONTINGENCY", "L", HvdcLineContingency.class);
    }

    @Test
    void batteryTest() throws IOException {
        Network network = BatteryNetworkFactory.create();
        test(network, "BAT_CONTINGENCY", "BAT", BatteryContingency.class);
    }
}
