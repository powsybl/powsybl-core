/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.reducer;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.LoadType;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import com.powsybl.iidm.xml.XMLImporter;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.Collections;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class DefaultNetworkReducerTest {

    private FileSystem fileSystem;

    private PlatformConfig platformConfig;

    private ReadOnlyDataSource dataSource;

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        platformConfig = new InMemoryPlatformConfig(fileSystem);

        dataSource = new ResourceDataSource("eurostag-tutorial1-lf", new ResourceSet("/", "eurostag-tutorial1-lf.xml"));
    }

    @After
    public void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    public void testLoad() {
        Network network = new XMLImporter(platformConfig).importData(dataSource, new Properties());

        NetworkReducerObserverImpl observer = new NetworkReducerObserverImpl();

        NetworkReducer reducer = NetworkReducer.builder()
                .withNetworkFilter(IdentifierNetworkPredicate.of("P1"))
                .withObservers(observer)
                .build();
        reducer.reduce(network);

        assertEquals(1, network.getSubstationCount());
        assertEquals(2, network.getVoltageLevelCount());
        assertEquals(1, network.getTwoWindingsTransformerCount());
        assertEquals(0, network.getLineCount());
        assertEquals(1, network.getGeneratorCount());
        assertEquals(2, network.getLoadCount());
        assertEquals(0, network.getDanglingLineCount());

        Load load1 = network.getLoad("NHV1_NHV2_1");
        assertNotNull(load1);
        assertEquals(LoadType.FICTITIOUS, load1.getLoadType());
        assertEquals(302.4440612792969, load1.getP0(), 0.0);
        assertEquals(98.74027252197266, load1.getQ0(), 0.0);
        assertEquals(302.4440612792969, load1.getTerminal().getP(), 0.0);
        assertEquals(98.74027252197266, load1.getTerminal().getQ(), 0.0);

        Load load2 = network.getLoad("NHV1_NHV2_2");
        assertNotNull(load2);
        assertEquals(LoadType.FICTITIOUS, load2.getLoadType());
        assertEquals(302.4440612792969, load2.getP0(), 0.0);
        assertEquals(98.74027252197266, load2.getQ0(), 0.0);
        assertEquals(302.4440612792969, load2.getTerminal().getP(), 0.0);
        assertEquals(98.74027252197266, load2.getTerminal().getQ(), 0.0);

        assertEquals(1, observer.getSubstationRemovedCount());
        assertEquals(2, observer.getVoltageLevelRemovedCount());
        assertEquals(2, observer.getLineReducedCount());
        assertEquals(2, observer.getLineRemovedCount());
        assertEquals(0, observer.getTwoWindingsTransformerReducedCount());
        assertEquals(1, observer.getTwoWindingsTransformerRemovedCount());
        assertEquals(0, observer.getThreeWindingsTransformerReducedCount());
        assertEquals(0, observer.getThreeWindingsTransformerRemovedCount());
        assertEquals(0, observer.getHvdcLineReducedCount());
        assertEquals(0, observer.getHvdcLineRemovedCount());
    }

    @Test
    public void testLoad2() {
        Network network = new XMLImporter(platformConfig).importData(dataSource, new Properties());

        NetworkReducerObserverImpl observer = new NetworkReducerObserverImpl();

        NetworkReducer reducer = NetworkReducer.builder()
                .withNetworkFilter(new NominalVoltageNetworkPredicate(225.0, 400.0))
                .withObservers(observer)
                .build();
        reducer.reduce(network);

        assertEquals(2, network.getSubstationCount());
        assertEquals(2, network.getVoltageLevelCount());
        assertEquals(0, network.getTwoWindingsTransformerCount());
        assertEquals(2, network.getLineCount());
        assertEquals(0, network.getGeneratorCount());
        assertEquals(2, network.getLoadCount());
        assertEquals(0, network.getDanglingLineCount());

        Load load1 = network.getLoad("NGEN_NHV1");
        assertNotNull(load1);
        assertEquals(LoadType.FICTITIOUS, load1.getLoadType());
        assertEquals(-604.8909301757812, load1.getP0(), 0.0);
        assertEquals(-197.48046875, load1.getQ0(), 0.0);
        assertEquals(-604.8909301757812, load1.getTerminal().getP(), 0.0);
        assertEquals(-197.48046875, load1.getTerminal().getQ(), 0.0);

        Load load2 = network.getLoad("NHV2_NLOAD");
        assertNotNull(load2);
        assertEquals(LoadType.FICTITIOUS, load2.getLoadType());
        assertEquals(600.8677978515625, load2.getP0(), 0.0);
        assertEquals(274.3769836425781, load2.getQ0(), 0.0);
        assertEquals(600.8677978515625, load2.getTerminal().getP(), 0.0);
        assertEquals(274.3769836425781, load2.getTerminal().getQ(), 0.0);

        assertEquals(0, observer.getSubstationRemovedCount());
        assertEquals(2, observer.getVoltageLevelRemovedCount());
        assertEquals(0, observer.getLineReducedCount());
        assertEquals(0, observer.getLineRemovedCount());
        assertEquals(2, observer.getTwoWindingsTransformerReducedCount());
        assertEquals(2, observer.getTwoWindingsTransformerRemovedCount());
        assertEquals(0, observer.getThreeWindingsTransformerReducedCount());
        assertEquals(0, observer.getThreeWindingsTransformerRemovedCount());
        assertEquals(0, observer.getHvdcLineReducedCount());
        assertEquals(0, observer.getHvdcLineRemovedCount());
    }

    @Test
    public void testDanglingLine() {
        Network network = new XMLImporter(platformConfig).importData(dataSource, new Properties());

        NetworkReducer reducer = NetworkReducer.builder()
                .withNetworkFilter(IdentifierNetworkPredicate.of("P2"))
                .withDanglingLines(true)
                .build();

        reducer.reduce(network);

        assertEquals(1, network.getSubstationCount());
        assertEquals(2, network.getVoltageLevelCount());
        assertEquals(1, network.getTwoWindingsTransformerCount());
        assertEquals(0, network.getLineCount());
        assertEquals(0, network.getGeneratorCount());
        assertEquals(1, network.getLoadCount());
        assertEquals(2, network.getDanglingLineCount());

        DanglingLine dl1 = network.getDanglingLine("NHV1_NHV2_1");
        assertNotNull(dl1);
        assertEquals(1.5, dl1.getR(), 0.0);
        assertEquals(16.5, dl1.getX(), 0.0);
        assertEquals(0.0, dl1.getG(), 0.0);
        assertEquals(1.93e-4, dl1.getB(), 0.0);
        assertEquals(-300.43389892578125, dl1.getP0(), 0.0);
        assertEquals(-137.18849182128906, dl1.getQ0(), 0.0);
        assertEquals(-300.43389892578125, dl1.getTerminal().getP(), 0.0);
        assertEquals(-137.18849182128906, dl1.getTerminal().getQ(), 0.0);

        DanglingLine dl2 = network.getDanglingLine("NHV1_NHV2_2");
        assertNotNull(dl2);
        assertEquals(1.5, dl2.getR(), 0.0);
        assertEquals(16.5, dl2.getX(), 0.0);
        assertEquals(0.0, dl2.getG(), 0.0);
        assertEquals(1.93e-4, dl2.getB(), 0.0);
        assertEquals(-300.43389892578125, dl2.getP0(), 0.0);
        assertEquals(-137.18849182128906, dl2.getQ0(), 0.0);
        assertEquals(-300.43389892578125, dl2.getTerminal().getP(), 0.0);
        assertEquals(-137.18849182128906, dl2.getTerminal().getQ(), 0.0);
    }

    @Test
    public void testFailure() {
        thrown.expect(PowsyblException.class);
        thrown.expectMessage("The active power of 'NHV1_NHV2_1' (VLHV1) is not set. Do you forget to compute the flows?");

        Network network = EurostagTutorialExample1Factory.create();

        NetworkReducer reducer = NetworkReducer.builder()
                .withNetworkFilter(IdentifierNetworkPredicate.of("P1"))
                .build();
        reducer.reduce(network);
    }

    @Test
    public void testHvdc() {
        Network network = HvdcTestNetwork.createLcc();
        assertEquals(1, network.getHvdcLineCount());

        NetworkReducerObserverImpl observer = new NetworkReducerObserverImpl();

        // Keeping both end of the HVDC is OK
        NetworkReducer reducer = NetworkReducer.builder()
                .withNetworkFilter(IdentifierNetworkPredicate.of("VL1", "VL2"))
                .withObservers(observer)
                .build();
        reducer.reduce(network);
        assertEquals(1, network.getHvdcLineCount());

        assertEquals(0, observer.getHvdcLineReducedCount());
        assertEquals(0, observer.getHvdcLineRemovedCount());

        // Keeping none of the ends of the HVDC is OK
        reducer = NetworkReducer.builder()
                .withNetworkFilter(new IdentifierNetworkPredicate(Collections.emptyList()))
                .withObservers(Collections.singleton(observer))
                .build();
        reducer.reduce(network);
        assertEquals(0, network.getHvdcLineCount());

        assertEquals(0, observer.getHvdcLineReducedCount());
        assertEquals(1, observer.getHvdcLineRemovedCount());
    }

    @Test
    public void testHvdcFailure() {
        thrown.expect(UnsupportedOperationException.class);
        thrown.expectMessage("Reduction of HVDC lines is not supported");

        Network network = HvdcTestNetwork.createLcc();
        NetworkReducer reducer = NetworkReducer.builder()
                    .withNetworkFilter(IdentifierNetworkPredicate.of("VL1"))
                    .withReductionOptions(new ReductionOptions())
                    .build();
        reducer.reduce(network);
    }

    @Test
    public void test3WT() {
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        assertEquals(1, network.getThreeWindingsTransformerCount());

        NetworkReducerObserverImpl observer = new NetworkReducerObserverImpl();

        // Keeping all the ends of the 3WT is OK
        NetworkReducer reducer = NetworkReducer.builder()
                .withNetworkFilter(IdentifierNetworkPredicate.of("VL_132", "VL_33", "VL_11"))
                .withObservers(observer)
                .build();
        reducer.reduce(network);
        assertEquals(1, network.getThreeWindingsTransformerCount());

        assertEquals(0, observer.getThreeWindingsTransformerReducedCount());
        assertEquals(0, observer.getThreeWindingsTransformerRemovedCount());

        // Keeping none of the ends of the 3WT is OK
        reducer = NetworkReducer.builder()
                .withNetworkFilter(new IdentifierNetworkPredicate(Collections.emptyList()))
                .withObservers(Collections.singleton(observer))
                .build();
        reducer.reduce(network);
        assertEquals(0, network.getThreeWindingsTransformerCount());

        assertEquals(0, observer.getThreeWindingsTransformerReducedCount());
        assertEquals(1, observer.getThreeWindingsTransformerRemovedCount());
    }

    @Test
    public void test3WTFailure() {
        thrown.expect(UnsupportedOperationException.class);
        thrown.expectMessage("Reduction of three-windings transformers is not supported");

        Network network = ThreeWindingsTransformerNetworkFactory.create();
        NetworkReducer reducer = NetworkReducer.builder()
                .withNetworkFilter(IdentifierNetworkPredicate.of("VL_132", "VL_11"))
                .withReductionOptions(new ReductionOptions())
                .build();
        reducer.reduce(network);
    }
}
