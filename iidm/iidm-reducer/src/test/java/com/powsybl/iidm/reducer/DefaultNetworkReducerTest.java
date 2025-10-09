/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.reducer;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ActivePowerControl;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class DefaultNetworkReducerTest {

    private static final String NHV1_NHV2_1 = "NHV1_NHV2_1";

    private static final String NHV1_NHV2_2 = "NHV1_NHV2_2";

    @Test
    void testLoad() {
        Network network = EurostagTutorialExample1Factory.createWithLFResults();

        NetworkReducerObserverImpl observer = new NetworkReducerObserverImpl();

        NetworkReducer reducer = NetworkReducer.builder()
                .withNetworkPredicate(IdentifierNetworkPredicate.of("P1"))
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

        Load load1 = network.getLoad(NHV1_NHV2_1);
        assertNotNull(load1);
        assertEquals(LoadType.FICTITIOUS, load1.getLoadType());
        assertEquals(302.4440612792969, load1.getP0(), 0.0);
        assertEquals(98.74027252197266, load1.getQ0(), 0.0);
        assertEquals(302.4440612792969, load1.getTerminal().getP(), 0.0);
        assertEquals(98.74027252197266, load1.getTerminal().getQ(), 0.0);

        Load load2 = network.getLoad(NHV1_NHV2_2);
        assertNotNull(load2);
        assertEquals(LoadType.FICTITIOUS, load2.getLoadType());
        assertEquals(302.4440612792969, load2.getP0(), 0.0);
        assertEquals(98.74027252197266, load2.getQ0(), 0.0);
        assertEquals(302.4440612792969, load2.getTerminal().getP(), 0.0);
        assertEquals(98.74027252197266, load2.getTerminal().getQ(), 0.0);

        assertEquals(1, observer.getSubstationRemovedCount());
        assertEquals(2, observer.getVoltageLevelRemovedCount());
        assertEquals(2, observer.getLineReplacedCount());
        assertEquals(2, observer.getLineRemovedCount());
        assertEquals(0, observer.getTwoWindingsTransformerReplacedCount());
        assertEquals(1, observer.getTwoWindingsTransformerRemovedCount());
        assertEquals(0, observer.getThreeWindingsTransformerReplacedCount());
        assertEquals(0, observer.getThreeWindingsTransformerRemovedCount());
        assertEquals(0, observer.getHvdcLineReplacedCount());
        assertEquals(0, observer.getHvdcLineRemovedCount());
    }

    @Test
    void testLoad2() {
        Network network = EurostagTutorialExample1Factory.createWithLFResults();

        NetworkReducerObserverImpl observer = new NetworkReducerObserverImpl();

        NetworkReducer reducer = NetworkReducer.builder()
                .withNetworkPredicate(new NominalVoltageNetworkPredicate(225.0, 400.0))
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
        assertEquals(0, observer.getLineReplacedCount());
        assertEquals(0, observer.getLineRemovedCount());
        assertEquals(2, observer.getTwoWindingsTransformerReplacedCount());
        assertEquals(2, observer.getTwoWindingsTransformerRemovedCount());
        assertEquals(0, observer.getThreeWindingsTransformerReplacedCount());
        assertEquals(0, observer.getThreeWindingsTransformerRemovedCount());
        assertEquals(0, observer.getHvdcLineRemovedCount());
        assertEquals(0, observer.getHvdcLineRemovedCount());
    }

    @Test
    void testDanglingLine() {
        Network network = EurostagTutorialExample1Factory.createWithLFResults();

        NetworkReducer reducer = NetworkReducer.builder()
                .withNetworkPredicate(IdentifierNetworkPredicate.of("P2"))
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

        DanglingLine dl1 = network.getDanglingLine(NHV1_NHV2_1);
        assertNotNull(dl1);
        assertEquals(1.5, dl1.getR(), 0.0);
        assertEquals(16.5, dl1.getX(), 0.0);
        assertEquals(0.0, dl1.getG(), 0.0);
        assertEquals(1.93e-4, dl1.getB(), 0.0);
        assertEquals(-300.43389892578125, dl1.getP0(), 0.0);
        assertEquals(-137.18849182128906, dl1.getQ0(), 0.0);
        assertEquals(-300.43389892578125, dl1.getTerminal().getP(), 0.0);
        assertEquals(-137.18849182128906, dl1.getTerminal().getQ(), 0.0);

        DanglingLine dl2 = network.getDanglingLine(NHV1_NHV2_2);
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
    void testWithNoLoadflowResults() {
        Network network = EurostagTutorialExample1Factory.create();

        NetworkReducer reducer = NetworkReducer.builder()
                .withNetworkPredicate(IdentifierNetworkPredicate.of("P1"))
                .build();
        reducer.reduce(network);

        Load load1 = network.getLoad(NHV1_NHV2_1);
        assertEquals(0.0, load1.getP0(), 0.0);
        assertEquals(0.0, load1.getQ0(), 0.0);
        assertTrue(Double.isNaN(load1.getTerminal().getP()));
        assertTrue(Double.isNaN(load1.getTerminal().getQ()));

        Load load2 = network.getLoad(NHV1_NHV2_2);
        assertEquals(0.0, load2.getP0(), 0.0);
        assertEquals(0.0, load2.getQ0(), 0.0);
        assertTrue(Double.isNaN(load2.getTerminal().getP()));
        assertTrue(Double.isNaN(load2.getTerminal().getQ()));
    }

    @Test
    void testWithDisconnectedLines() {
        Network network = EurostagTutorialExample1Factory.create();

        Line line1 = network.getLine(NHV1_NHV2_1);
        line1.getTerminal1().disconnect();
        line1.getTerminal2().disconnect();

        Line line2 = network.getLine(NHV1_NHV2_2);
        line2.getTerminal1().disconnect();
        line2.getTerminal2().disconnect();

        NetworkReducer reducer = NetworkReducer.builder()
                .withNetworkPredicate(IdentifierNetworkPredicate.of("P1"))
                .build();
        reducer.reduce(network);

        Load load1 = network.getLoad(NHV1_NHV2_1);
        assertEquals(0.0, load1.getP0(), 0.0);
        assertEquals(0.0, load1.getQ0(), 0.0);
        assertTrue(Double.isNaN(load1.getTerminal().getP()));
        assertTrue(Double.isNaN(load1.getTerminal().getQ()));

        Load load2 = network.getLoad(NHV1_NHV2_2);
        assertEquals(0.0, load2.getP0(), 0.0);
        assertEquals(0.0, load2.getQ0(), 0.0);
        assertTrue(Double.isNaN(load2.getTerminal().getP()));
        assertTrue(Double.isNaN(load2.getTerminal().getQ()));
    }

    @Test
    void testHvdc() {
        Network network = HvdcTestNetwork.createLcc();
        assertEquals(1, network.getHvdcLineCount());

        NetworkReducerObserverImpl observer = new NetworkReducerObserverImpl();

        // Keeping both end of the HVDC is OK
        NetworkReducer reducer = NetworkReducer.builder()
                .withNetworkPredicate(IdentifierNetworkPredicate.of("VL1", "VL2"))
                .withObservers(observer)
                .build();
        reducer.reduce(network);
        assertEquals(1, network.getHvdcLineCount());

        assertEquals(0, observer.getHvdcLineReplacedCount());
        assertEquals(0, observer.getHvdcLineRemovedCount());

        // Keeping none of the ends of the HVDC is OK
        reducer = NetworkReducer.builder()
                .withNetworkPredicate(new IdentifierNetworkPredicate(Collections.emptyList()))
                .withObservers(Collections.singleton(observer))
                .build();
        reducer.reduce(network);
        assertEquals(0, network.getHvdcLineCount());

        assertEquals(0, observer.getHvdcLineReplacedCount());
        assertEquals(1, observer.getHvdcLineRemovedCount());
    }

    @Test
    void testHvdcReplacement() {
        testHvdcReplacementLcc();
        testHvdcReplacementVscWithMinMaxReactiveLimits();
        tetHvdcReplacementVscWithReactiveCapabilityCurve();
    }

    private static void testHvdcReplacementLcc() {
        NetworkReducerObserverImpl observerLcc = new NetworkReducerObserverImpl();
        Network networkLcc = HvdcTestNetwork.createLcc();
        assertEquals(0, networkLcc.getLoadCount());
        assertEquals(2, networkLcc.getHvdcConverterStationCount());
        NetworkReducer reducerLcc = NetworkReducer.builder()
                .withNetworkPredicate(IdentifierNetworkPredicate.of("VL1"))
                .withObservers(observerLcc)
                .build();
        reducerLcc.reduce(networkLcc);
        assertEquals(0, networkLcc.getHvdcLineCount());
        assertEquals(1, observerLcc.getHvdcLineReplacedCount());
        assertEquals(1, observerLcc.getHvdcLineRemovedCount());
        assertEquals(1, networkLcc.getLoadCount());
        assertEquals(0, networkLcc.getHvdcConverterStationCount());
    }

    private static void testHvdcReplacementVscWithMinMaxReactiveLimits() {
        NetworkReducerObserverImpl observerVsc = new NetworkReducerObserverImpl();
        Network networkVsc = HvdcTestNetwork.createVsc();
        VscConverterStation station = networkVsc.getVscConverterStation("C1");
        double targetV = station.getVoltageSetpoint();
        station.newMinMaxReactiveLimits().setMaxQ(200).setMinQ(-200).add();
        double p = station.getTerminal().getP();
        assertEquals(0, networkVsc.getGeneratorCount());
        assertEquals(2, networkVsc.getHvdcConverterStationCount());
        NetworkReducer reducerVsc = NetworkReducer.builder()
                .withNetworkPredicate(IdentifierNetworkPredicate.of("VL1"))
                .withObservers(observerVsc)
                .build();
        reducerVsc.reduce(networkVsc);
        assertEquals(0, networkVsc.getHvdcLineCount());
        assertEquals(1, observerVsc.getHvdcLineReplacedCount());
        assertEquals(1, observerVsc.getHvdcLineRemovedCount());
        assertEquals(1, networkVsc.getGeneratorCount());
        Generator gen = networkVsc.getGenerator("L");
        assertEquals(300, gen.getMaxP());
        assertEquals(-300, gen.getMinP());
        assertEquals(-p, gen.getTargetP());
        assertTrue(gen.isVoltageRegulatorOn());
        assertEquals(targetV, gen.getTargetV());
        assertEquals(200, gen.getReactiveLimits(MinMaxReactiveLimits.class).getMaxQ());
        assertFalse(gen.getExtension(ActivePowerControl.class).isParticipate());
    }

    private static void tetHvdcReplacementVscWithReactiveCapabilityCurve() {
        NetworkReducerObserverImpl observerVsc2 = new NetworkReducerObserverImpl();
        Network networkVsc2 = HvdcTestNetwork.createVsc();
        VscConverterStation station2 = networkVsc2.getVscConverterStation("C1");
        station2.newReactiveCapabilityCurve()
                .beginPoint().setP(0.0).setMinQ(-200).setMaxQ(200).endPoint()
                .beginPoint().setP(100.0).setMinQ(-200).setMaxQ(200).endPoint()
                .add();
        NetworkReducer reducerVsc2 = NetworkReducer.builder()
                .withNetworkPredicate(IdentifierNetworkPredicate.of("VL1"))
                .withObservers(observerVsc2)
                .build();
        reducerVsc2.reduce(networkVsc2);
        Generator gen2 = networkVsc2.getGenerator("L");
        assertEquals(2, gen2.getReactiveLimits(ReactiveCapabilityCurve.class).getPointCount());
        Iterator<ReactiveCapabilityCurve.Point> pointIt = gen2.getReactiveLimits(ReactiveCapabilityCurve.class).getPoints().iterator();
        ReactiveCapabilityCurve.Point point = pointIt.next();
        assertEquals(0.0, point.getP(), 0.001);
        assertEquals(-200.0, point.getMinQ(), 0.001);
        assertEquals(200.0, point.getMaxQ(), 0.001);
        point = pointIt.next();
        assertEquals(100.0, point.getP(), 0.001);
        assertEquals(-200.0, point.getMinQ(), 0.001);
        assertEquals(200.0, point.getMaxQ(), 0.001);
    }

    @Test
    void testHvdcVoltageRegulatorOffReplacement() {
        Network network = HvdcTestNetwork.createVsc();

        // Set voltageRegulatorOn to false
        HvdcLine hvdcLine = network.getHvdcLine("L");
        HvdcConverterStation hvdcConverterStation = hvdcLine.getConverterStation1();
        assertEquals("VSC", hvdcConverterStation.getHvdcType().name());
        VscConverterStation vscConverterStation = (VscConverterStation) hvdcConverterStation;
        vscConverterStation.setReactivePowerSetpoint(45.0);
        vscConverterStation.setVoltageRegulatorOn(false);

        assertEquals(1, network.getHvdcLineCount());
        assertEquals(0, network.getLoadCount());

        // Hvdc is replaced by a load
        NetworkReducerObserverImpl observer = new NetworkReducerObserverImpl();
        NetworkReducer reducer = NetworkReducer.builder()
                .withNetworkPredicate(IdentifierNetworkPredicate.of("VL1"))
                .withObservers(observer)
                .build();
        reducer.reduce(network);

        assertEquals(0, network.getHvdcLineCount());
        assertEquals(1, network.getLoadCount());
    }

    @Test
    void test3WT() {
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        assertEquals(1, network.getThreeWindingsTransformerCount());

        NetworkReducerObserverImpl observer = new NetworkReducerObserverImpl();

        // Keeping all the ends of the 3WT is OK
        NetworkReducer reducer = NetworkReducer.builder()
                .withNetworkPredicate(IdentifierNetworkPredicate.of("VL_132", "VL_33", "VL_11"))
                .withObservers(observer)
                .build();
        reducer.reduce(network);
        assertEquals(1, network.getThreeWindingsTransformerCount());

        assertEquals(0, observer.getThreeWindingsTransformerReplacedCount());
        assertEquals(0, observer.getThreeWindingsTransformerRemovedCount());

        // Keeping none of the ends of the 3WT is OK
        reducer = NetworkReducer.builder()
                .withNetworkPredicate(new IdentifierNetworkPredicate(Collections.emptyList()))
                .withObservers(Collections.singleton(observer))
                .build();
        reducer.reduce(network);
        assertEquals(0, network.getThreeWindingsTransformerCount());

        assertEquals(0, observer.getThreeWindingsTransformerReplacedCount());
        assertEquals(1, observer.getThreeWindingsTransformerRemovedCount());
    }

    @Test
    void test3WTReplacement() {
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        NetworkReducerObserverImpl observer = new NetworkReducerObserverImpl();
        NetworkReducer reducer = NetworkReducer.builder()
                .withNetworkPredicate(IdentifierNetworkPredicate.of("VL_11"))
                .withObservers(observer)
                .build();
        reducer.reduce(network);

        assertEquals(1, observer.getThreeWindingsTransformerRemovedCount());
        assertEquals(1, observer.getThreeWindingsTransformerReplacedCount());
    }

    @Test
    void test3WTReductionVoltageLevelsModification() {
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        NetworkReducerObserverImpl observer = new NetworkReducerObserverImpl();
        NetworkReducer reducer = NetworkReducer.builder()
                .withNetworkPredicate(IdentifierNetworkPredicate.of("VL_11", "VL_33"))
                .withObservers(observer)
                .build();
        reducer.reduce(network);

        assertEquals(0, observer.getThreeWindingsTransformerRemovedCount());
        assertEquals(0, observer.getThreeWindingsTransformerReplacedCount());
    }

    @Test
    void testNodeBreaker() {
        Network network = FictitiousSwitchFactory.create();

        NetworkReducer reducer = NetworkReducer.builder()
                .withNetworkPredicate(IdentifierNetworkPredicate.of("C"))
                .build();
        reducer.reduce(network);

        assertEquals(1, network.getSubstationCount());
        assertEquals(1, network.getVoltageLevelCount());
        assertEquals(0, network.getTwoWindingsTransformerCount());
        assertEquals(0, network.getLineCount());
        assertEquals(0, network.getGeneratorCount());
        assertEquals(2, network.getLoadCount());
        assertEquals(0, network.getDanglingLineCount());
        assertEquals(4, network.getSwitchCount());
        assertEquals(1, network.getBusbarSectionCount());
    }
}
