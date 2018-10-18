/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.google.common.collect.Iterables;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.VoltageLevel.NodeBreakerView;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import com.powsybl.iidm.network.test.NetworkTest1Factory;
import com.powsybl.iidm.network.test.SvcTestCaseFactory;
import org.joda.time.DateTime;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static junit.framework.TestCase.assertSame;
import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NetworkTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public NetworkTest() {
    }

    @Test
    public void testNetwork1() {
        Network network = NetworkTest1Factory.create();
        assertEquals(1, Iterables.size(network.getCountries()));
        assertEquals(1, network.getCountryCount());
        Country country1 = network.getCountries().iterator().next();

        assertEquals(1, Iterables.size(network.getSubstations()));
        assertEquals(1, Iterables.size(network.getSubstations(Country.FR, "TSO1", "region1")));
        assertEquals(1, network.getSubstationCount());

        Substation substation1 = network.getSubstation("substation1");
        assertNotNull(substation1);
        assertEquals("substation1", substation1.getId());
        assertSame(country1, substation1.getCountry());
        assertEquals(1, substation1.getGeographicalTags().size());
        assertTrue(substation1.getGeographicalTags().contains("region1"));
        assertEquals(1, Iterables.size(network.getVoltageLevels()));
        assertEquals(1, network.getVoltageLevelCount());

        VoltageLevel voltageLevel1 = network.getVoltageLevel("voltageLevel1");
        assertNotNull(voltageLevel1);
        assertEquals("voltageLevel1", voltageLevel1.getId());
        assertEquals(400.0, voltageLevel1.getNominalV(), 0.0);
        assertSame(substation1, voltageLevel1.getSubstation());
        assertSame(TopologyKind.NODE_BREAKER, voltageLevel1.getTopologyKind());

        NodeBreakerView topology1 = voltageLevel1.getNodeBreakerView();
        assertEquals(10, topology1.getNodeCount());
        assertEquals(2, Iterables.size(topology1.getBusbarSections()));
        assertEquals(2, topology1.getBusbarSectionCount());

        assertEquals(2, Iterables.size(network.getBusbarSections()));
        assertEquals(2, network.getBusbarSectionCount());
        assertEquals(2, network.getBusbarSectionStream().count());

        BusbarSection voltageLevel1BusbarSection1 = topology1.getBusbarSection("voltageLevel1BusbarSection1");
        assertNotNull(voltageLevel1BusbarSection1);
        assertEquals("voltageLevel1BusbarSection1", voltageLevel1BusbarSection1.getId());

        BusbarSection voltageLevel1BusbarSection2 = topology1.getBusbarSection("voltageLevel1BusbarSection2");
        assertNotNull(voltageLevel1BusbarSection2);
        assertEquals("voltageLevel1BusbarSection2", voltageLevel1BusbarSection2.getId());
        assertEquals(5, Iterables.size(topology1.getSwitches()));
        assertEquals(5, topology1.getSwitchCount());

        assertEquals(5, Iterables.size(network.getSwitches()));
        assertEquals(5, network.getSwitchCount());
        assertEquals(5, network.getSwitchStream().count());

        Switch voltageLevel1Breaker1 = topology1.getSwitch("voltageLevel1Breaker1");
        assertNotNull(voltageLevel1Breaker1);
        assertEquals("voltageLevel1Breaker1", voltageLevel1Breaker1.getId());
        assertFalse(voltageLevel1Breaker1.isOpen());
        assertTrue(voltageLevel1Breaker1.isRetained());
        assertSame(SwitchKind.BREAKER, voltageLevel1Breaker1.getKind());
        assertSame(voltageLevel1BusbarSection1.getTerminal().getNodeBreakerView().getNode(), topology1.getNode1(voltageLevel1Breaker1.getId()));
        assertSame(voltageLevel1BusbarSection2.getTerminal().getNodeBreakerView().getNode(), topology1.getNode2(voltageLevel1Breaker1.getId()));
        assertEquals(1, Iterables.size(voltageLevel1.getLoads()));
        assertEquals(1, voltageLevel1.getLoadCount());

        Load load1 = network.getLoad("load1");
        assertNotNull(load1);
        assertEquals("load1", load1.getId());
        assertEquals(2, load1.getTerminal().getNodeBreakerView().getNode());
        assertEquals(10.0, load1.getP0(), 0.0);
        assertEquals(3.0, load1.getQ0(), 0.0);

        Generator generator1 = network.getGenerator("generator1");
        assertNotNull(generator1);
        assertEquals("generator1", generator1.getId());
        assertEquals(5, generator1.getTerminal().getNodeBreakerView().getNode());
        assertEquals(200.0, generator1.getMinP(), 0.0);
        assertEquals(900.0, generator1.getMaxP(), 0.0);
        assertSame(EnergySource.NUCLEAR, generator1.getEnergySource());
        assertTrue(generator1.isVoltageRegulatorOn());
        assertEquals(900.0, generator1.getTargetP(), 0.0);
        assertEquals(380.0, generator1.getTargetV(), 0.0);
        ReactiveCapabilityCurve rcc1 = generator1.getReactiveLimits(ReactiveCapabilityCurve.class);
        assertEquals(2, rcc1.getPointCount());
        assertEquals(500.0, rcc1.getMaxQ(500), 0.0);
        assertEquals(300.0, rcc1.getMinQ(500), 0.0);

        assertEquals(2, Iterables.size(voltageLevel1.getBusBreakerView().getBuses()));
        Bus busCalc1 = voltageLevel1BusbarSection1.getTerminal().getBusBreakerView().getBus();
        Bus busCalc2 = voltageLevel1BusbarSection2.getTerminal().getBusBreakerView().getBus();
        assertSame(busCalc1, load1.getTerminal().getBusBreakerView().getBus());
        assertSame(busCalc2, generator1.getTerminal().getBusBreakerView().getBus());
        assertEquals(0, busCalc1.getConnectedComponent().getNum());
        assertEquals(0, busCalc2.getConnectedComponent().getNum());

        assertEquals(1, Iterables.size(voltageLevel1.getBusView().getBuses()));
        Bus busCalc = voltageLevel1BusbarSection1.getTerminal().getBusView().getBus();
        assertSame(busCalc, voltageLevel1BusbarSection2.getTerminal().getBusView().getBus());
        assertSame(busCalc, load1.getTerminal().getBusView().getBus());
        assertSame(busCalc, generator1.getTerminal().getBusView().getBus());
        assertEquals(0, busCalc.getConnectedComponent().getNum());
    }

    @Test
    public void testVoltageLevelGetConnectable() {
        Network n = EurostagTutorialExample1Factory.create();
        assertNotNull(n.getVoltageLevel("VLLOAD").getConnectable("LOAD", Load.class));
        assertNotNull(n.getVoltageLevel("VLLOAD").getConnectable("NHV2_NLOAD", Branch.class));
        assertNull(n.getVoltageLevel("VLGEN").getConnectable("LOAD", Load.class));
        assertNull(n.getVoltageLevel("VLGEN").getConnectable("NHV2_NLOAD", Branch.class));
    }

    @Test
    public void testStreams() {
        Function<Stream<? extends Identifiable>, List<String>> mapper = stream -> stream.map(Identifiable::getId).collect(Collectors.toList());
        Function<Stream<? extends Identifiable>, Set<String>> mapperSet = stream -> stream.map(Identifiable::getId).collect(Collectors.toSet());

        Network network = EurostagTutorialExample1Factory.create();
        assertEquals(Arrays.asList("P1", "P2"), mapper.apply(network.getSubstationStream()));
        assertEquals(network.getSubstationCount(), network.getSubstationStream().count());
        assertEquals(Arrays.asList("NHV1_NHV2_1", "NHV1_NHV2_2"), mapper.apply(network.getLineStream()));
        assertEquals(network.getLineCount(), network.getLineStream().count());

        assertEquals(Arrays.asList("VLGEN", "VLHV1", "VLHV2", "VLLOAD"), mapper.apply(network.getVoltageLevelStream()));
        assertEquals(network.getVoltageLevelCount(), network.getVoltageLevelStream().count());
        assertEquals(Arrays.asList("VLGEN", "VLHV1"), mapper.apply(network.getSubstation("P1").getVoltageLevelStream()));
        assertEquals(Arrays.asList("VLHV2", "VLLOAD"), mapper.apply(network.getSubstation("P2").getVoltageLevelStream()));

        assertEquals(Arrays.asList("NGEN", "NHV1", "NHV2", "NLOAD"), mapper.apply(network.getBusBreakerView().getBusStream()));
        assertEquals(Collections.singletonList("NGEN"), mapper.apply(network.getVoltageLevel("VLGEN").getBusBreakerView().getBusStream()));
        assertEquals(Collections.singletonList("NHV1"), mapper.apply(network.getVoltageLevel("VLHV1").getBusBreakerView().getBusStream()));
        assertEquals(Collections.singletonList("NHV2"), mapper.apply(network.getVoltageLevel("VLHV2").getBusBreakerView().getBusStream()));
        assertEquals(Collections.singletonList("NLOAD"), mapper.apply(network.getVoltageLevel("VLLOAD").getBusBreakerView().getBusStream()));

        assertEquals(Arrays.asList("NGEN_NHV1", "NHV2_NLOAD"), mapper.apply(network.getTwoWindingsTransformerStream()));
        assertEquals(network.getTwoWindingsTransformerCount(), network.getTwoWindingsTransformerStream().count());
        assertEquals(Collections.singleton("NGEN_NHV1"), mapperSet.apply(network.getSubstation("P1").getTwoWindingsTransformerStream()));
        assertEquals(Collections.singleton("NHV2_NLOAD"), mapperSet.apply(network.getSubstation("P2").getTwoWindingsTransformerStream()));

        assertEquals(Arrays.asList("NHV1_NHV2_1", "NHV1_NHV2_2", "NGEN_NHV1", "NHV2_NLOAD"), mapper.apply(network.getBranchStream()));
        assertEquals(network.getBranchCount(), network.getBranchStream().count());

        assertEquals(Collections.emptyList(), mapper.apply(network.getThreeWindingsTransformerStream()));
        assertEquals(network.getThreeWindingsTransformerCount(), network.getThreeWindingsTransformerStream().count());
        assertEquals(Collections.emptyList(), mapper.apply(network.getSubstation("P1").getThreeWindingsTransformerStream()));
        assertEquals(Collections.emptyList(), mapper.apply(network.getSubstation("P2").getThreeWindingsTransformerStream()));

        assertEquals(Collections.emptyList(), mapper.apply(network.getDanglingLineStream()));
        assertEquals(Collections.emptyList(), mapper.apply(network.getVoltageLevel("VLHV1").getDanglingLineStream()));
        assertEquals(network.getDanglingLineCount(), network.getDanglingLineStream().count());
        assertEquals(Collections.emptyList(), mapper.apply(network.getShuntCompensatorStream()));
        assertEquals(Collections.emptyList(), mapper.apply(network.getVoltageLevel("VLHV2").getShuntCompensatorStream()));
        assertEquals(network.getShuntCompensatorCount(), network.getShuntCompensatorStream().count());

        assertEquals(Collections.singletonList("LOAD"), mapper.apply(network.getLoadStream()));
        assertEquals(network.getLoadCount(), network.getLoadStream().count());
        assertEquals(Collections.singletonList("LOAD"), mapper.apply(network.getVoltageLevel("VLLOAD").getLoadStream()));

        assertEquals(Collections.singletonList("GEN"), mapper.apply(network.getGeneratorStream()));
        assertEquals(network.getGeneratorCount(), network.getGeneratorStream().count());
        assertEquals(Collections.singletonList("GEN"), mapper.apply(network.getVoltageLevel("VLGEN").getGeneratorStream()));

        Bus bus = network.getVoltageLevel("VLGEN").getBusView().getBus("VLGEN_0");
        assertEquals(Collections.singletonList("NGEN_NHV1"), mapper.apply(bus.getTwoWindingTransformerStream()));
        assertEquals(Collections.singletonList("GEN"), mapper.apply(bus.getGeneratorStream()));
        bus = network.getVoltageLevel("VLHV1").getBusView().getBus("VLHV1_0");
        assertEquals(Arrays.asList("NHV1_NHV2_1", "NHV1_NHV2_2"), mapper.apply(bus.getLineStream()));
        assertEquals(Collections.singletonList("NGEN_NHV1"), mapper.apply(bus.getTwoWindingTransformerStream()));
        bus = network.getVoltageLevel("VLHV2").getBusView().getBus("VLHV2_0");
        assertEquals(Collections.singletonList("NHV2_NLOAD"), mapper.apply(bus.getTwoWindingTransformerStream()));
        bus = network.getVoltageLevel("VLLOAD").getBusView().getBus("VLLOAD_0");
        assertEquals(Collections.singletonList("LOAD"), mapper.apply(bus.getLoadStream()));

        // SVC
        network = SvcTestCaseFactory.create();
        assertEquals(Collections.singletonList("SVC2"), mapper.apply(network.getStaticVarCompensatorStream()));
        assertEquals(network.getStaticVarCompensatorCount(), network.getStaticVarCompensatorStream().count());
        assertEquals(Collections.singletonList("SVC2"), mapper.apply(network.getVoltageLevel("VL2").getStaticVarCompensatorStream()));
        bus = network.getVoltageLevel("VL2").getBusView().getBus("VL2_0");
        assertEquals(Collections.singletonList("SVC2"), mapper.apply(bus.getStaticVarCompensatorStream()));

        // HVDC
        network = HvdcTestNetwork.createLcc();
        assertEquals(Collections.singletonList("L"), mapper.apply(network.getHvdcLineStream()));
        assertEquals(network.getHvdcLineCount(), network.getHvdcLineStream().count());
        assertEquals(Arrays.asList("C1", "C2"), mapper.apply(network.getLccConverterStationStream()));
        assertEquals(network.getLccConverterStationCount(), network.getLccConverterStationStream().count());
        assertEquals(Arrays.asList("C1", "C2"), mapper.apply(network.getHvdcConverterStationStream()));
        assertEquals(network.getHvdcConverterStationCount(), network.getHvdcConverterStationStream().count());
        assertEquals(Collections.singletonList("C1"), mapper.apply(network.getVoltageLevel("VL1").getLccConverterStationStream()));
        assertEquals(Collections.singletonList("C2"), mapper.apply(network.getVoltageLevel("VL2").getLccConverterStationStream()));
        assertEquals(Collections.singletonList("L"), mapper.apply(network.getHvdcLineStream()));
        bus = network.getVoltageLevel("VL2").getBusView().getBus("VL2_0");
        assertEquals(Collections.singletonList("C2"), mapper.apply(bus.getLccConverterStationStream()));

        assertEquals(Arrays.asList("BK1", "BK2", "BK3"), mapper.apply(network.getBusBreakerView().getSwitchStream()));

        network = HvdcTestNetwork.createVsc();
        assertEquals(Collections.singletonList("L"), mapper.apply(network.getHvdcLineStream()));
        assertEquals(network.getHvdcLineCount(), network.getHvdcLineStream().count());
        assertEquals(Arrays.asList("C1", "C2"), mapper.apply(network.getVscConverterStationStream()));
        assertEquals(network.getLccConverterStationCount(), network.getLccConverterStationStream().count());
        assertEquals(Arrays.asList("C1", "C2"), mapper.apply(network.getHvdcConverterStationStream()));
        assertEquals(network.getHvdcConverterStationCount(), network.getHvdcConverterStationStream().count());
        assertEquals(Collections.singletonList("C1"), mapper.apply(network.getVoltageLevel("VL1").getVscConverterStationStream()));
        assertEquals(Collections.singletonList("C2"), mapper.apply(network.getVoltageLevel("VL2").getVscConverterStationStream()));
        assertEquals(Collections.singletonList("L"), mapper.apply(network.getHvdcLineStream()));
        bus = network.getVoltageLevel("VL2").getBusView().getBus("VL2_0");
        assertEquals(Collections.singletonList("C2"), mapper.apply(bus.getVscConverterStationStream()));

        // Topology
        network = NetworkTest1Factory.create();
        assertEquals(Arrays.asList("voltageLevel1BusbarSection1", "voltageLevel1BusbarSection2"),
                mapper.apply(network.getVoltageLevel("voltageLevel1").getNodeBreakerView().getBusbarSectionStream()));
        assertEquals(Collections.singletonList("voltageLevel1Breaker1"),
                mapper.apply(network.getVoltageLevel("voltageLevel1").getNodeBreakerView()
                        .getSwitchStream()
                        .filter(sw -> sw.getKind() == SwitchKind.BREAKER)));
        assertEquals(Arrays.asList("load1Disconnector1", "load1Breaker1"),
                mapper.apply(network.getVoltageLevel("voltageLevel1").getNodeBreakerView()
                        .getSwitchStream()
                        .filter(sw -> sw.getKind() == SwitchKind.DISCONNECTOR)
                        .limit(2)));
        assertEquals(Collections.emptyList(),
                mapper.apply(network.getVoltageLevel("voltageLevel1").getNodeBreakerView()
                        .getSwitchStream()
                        .filter(sw -> sw.getKind() == SwitchKind.LOAD_BREAK_SWITCH)));
    }

    @Test
    public void testSetterGetter() {
        String sourceFormat = "test_sourceFormat";
        Network network = NetworkFactory.create("test", sourceFormat);
        DateTime caseDate = new DateTime();
        network.setCaseDate(caseDate);
        assertEquals(caseDate, network.getCaseDate());
        network.setForecastDistance(3);
        assertEquals(3, network.getForecastDistance());
        assertEquals(sourceFormat, network.getSourceFormat());
        assertEquals(ContainerType.NETWORK, network.getContainerType());
    }

    @Test
    public void getSwitchTerminalTest() {
        Network busViewNetwork = EurostagTutorialExample1Factory.create();
        VoltageLevel voltageLevel = busViewNetwork.getVoltageLevel("VLGEN");

        Network nodeViewNetwork = NetworkTest1Factory.create();
        voltageLevel = nodeViewNetwork.getVoltageLevel("voltageLevel1");
        NodeBreakerView topology = voltageLevel.getNodeBreakerView();
        assertEquals(topology.getTerminal(topology.getNode1("voltageLevel1Breaker1")),
                     voltageLevel.getNodeBreakerView().getTerminal1("voltageLevel1Breaker1"));
        assertEquals(topology.getTerminal(topology.getNode2("voltageLevel1Breaker1")),
                     voltageLevel.getNodeBreakerView().getTerminal2("voltageLevel1Breaker1"));
    }

    @Test
    public void testExceptionGetSwitchTerminal1() {
        Network busViewNetwork = EurostagTutorialExample1Factory.create();
        VoltageLevel voltageLevel = busViewNetwork.getVoltageLevel("VLGEN");
        thrown.expect(PowsyblException.class);
        thrown.expectMessage("Not supported in a bus breaker topology");
        voltageLevel.getNodeBreakerView().getTerminal1("fictitiousSwitchId");
    }

    @Test
    public void testExceptionGetSwitchTerminal2() {
        Network busViewNetwork = EurostagTutorialExample1Factory.create();
        VoltageLevel voltageLevel = busViewNetwork.getVoltageLevel("VLGEN");
        thrown.expect(PowsyblException.class);
        thrown.expectMessage("Not supported in a bus breaker topology");
        voltageLevel.getNodeBreakerView().getTerminal2("fictitiousSwitchId");
    }

}
