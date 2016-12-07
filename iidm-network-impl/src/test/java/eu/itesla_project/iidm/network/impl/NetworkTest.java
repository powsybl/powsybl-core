/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.*;
import com.google.common.collect.Iterables;
import eu.itesla_project.iidm.network.VoltageLevel.NodeBreakerView;
import eu.itesla_project.iidm.network.test.EurostagTutorialExample1Factory;
import eu.itesla_project.iidm.network.test.HvdcTestNetwork;
import eu.itesla_project.iidm.network.test.NetworkTest1Factory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import eu.itesla_project.iidm.network.test.SvcTestCaseFactory;
import org.junit.Test;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NetworkTest {

    public NetworkTest() {
    }

    @Test
    public void testNetwork1() {
        Network network = NetworkTest1Factory.create();
        assertTrue(Iterables.size(network.getCountries()) == 1);
        assertTrue(network.getCountryCount() == 1);
        Country country1 = network.getCountries().iterator().next();

        assertTrue(Iterables.size(network.getSubstations()) == 1);
        assertTrue(network.getSubstationCount() == 1);

        Substation substation1 = network.getSubstation("substation1");
        assertTrue(substation1 != null);
        assertTrue(substation1.getId().equals("substation1"));
        assertTrue(substation1.getCountry() == country1);
        assertTrue(substation1.getGeographicalTags().size() == 1);
        assertTrue(substation1.getGeographicalTags().contains("region1"));
        assertTrue(Iterables.size(network.getVoltageLevels()) == 1);
        assertTrue(network.getVoltageLevelCount() == 1);

        VoltageLevel voltageLevel1 = network.getVoltageLevel("voltageLevel1");
        assertTrue(voltageLevel1 != null);
        assertTrue(voltageLevel1.getId().equals("voltageLevel1"));
        assertTrue(voltageLevel1.getNominalV() == 400);
        assertTrue(voltageLevel1.getSubstation() == substation1);
        assertTrue(voltageLevel1.getTopologyKind() == TopologyKind.NODE_BREAKER);

        NodeBreakerView topology1 = voltageLevel1.getNodeBreakerView();
        assertTrue(topology1.getNodeCount() == 10);
        assertTrue(Iterables.size(topology1.getBusbarSections()) == 2);
        assertTrue(topology1.getBusbarSectionCount() == 2);

        BusbarSection voltageLevel1BusbarSection1 = topology1.getBusbarSection("voltageLevel1BusbarSection1");
        assertTrue(voltageLevel1BusbarSection1 != null);
        assertTrue(voltageLevel1BusbarSection1.getId().equals("voltageLevel1BusbarSection1"));

        BusbarSection voltageLevel1BusbarSection2 = topology1.getBusbarSection("voltageLevel1BusbarSection2");
        assertTrue(voltageLevel1BusbarSection2 != null);
        assertTrue(voltageLevel1BusbarSection2.getId().equals("voltageLevel1BusbarSection2"));
        assertTrue(Iterables.size(topology1.getSwitches()) == 5);
        assertTrue(topology1.getSwitchCount() == 5);

        Switch voltageLevel1Breaker1 = topology1.getSwitch("voltageLevel1Breaker1");
        assertTrue(voltageLevel1Breaker1 != null);
        assertTrue(voltageLevel1Breaker1.getId().equals("voltageLevel1Breaker1"));
        assertTrue(!voltageLevel1Breaker1.isOpen());
        assertTrue(voltageLevel1Breaker1.isRetained());
        assertTrue(voltageLevel1Breaker1.getKind() == SwitchKind.BREAKER);
        assertTrue(topology1.getNode1(voltageLevel1Breaker1.getId()) == voltageLevel1BusbarSection1.getTerminal().getNodeBreakerView().getNode());
        assertTrue(topology1.getNode2(voltageLevel1Breaker1.getId()) == voltageLevel1BusbarSection2.getTerminal().getNodeBreakerView().getNode());
        assertTrue(Iterables.size(voltageLevel1.getLoads()) == 1);
        assertTrue(voltageLevel1.getLoadCount() == 1);

        Load load1 = network.getLoad("load1");
        assertTrue(load1 != null);
        assertTrue(load1.getId().equals("load1"));
        assertTrue(load1.getTerminal().getNodeBreakerView().getNode() == 2);
        assertTrue(load1.getP0() == 10);
        assertTrue(load1.getQ0() == 3);

        Generator generator1 = network.getGenerator("generator1");
        assertTrue(generator1 != null);
        assertTrue(generator1.getId().equals("generator1"));
        assertTrue(generator1.getTerminal().getNodeBreakerView().getNode() == 5);
        assertTrue(generator1.getMinP() == 200);
        assertTrue(generator1.getMaxP() == 900);
        assertTrue(generator1.getEnergySource() == EnergySource.NUCLEAR);
        assertTrue(generator1.isVoltageRegulatorOn());
        assertTrue(generator1.getTargetP() == 900);
        assertTrue(generator1.getTargetV() == 380);
        ReactiveCapabilityCurve rcc1 = generator1.getReactiveLimits(ReactiveCapabilityCurve.class);
        assertTrue(rcc1.getPointCount() == 2);
        assertTrue(rcc1.getMaxQ(500) == 500);
        assertTrue(rcc1.getMinQ(500) == 300);

        assertTrue(Iterables.size(voltageLevel1.getBusBreakerView().getBuses()) == 2);
        Bus busCalc1 = voltageLevel1BusbarSection1.getTerminal().getBusBreakerView().getBus();
        Bus busCalc2 = voltageLevel1BusbarSection2.getTerminal().getBusBreakerView().getBus();
        assertTrue(load1.getTerminal().getBusBreakerView().getBus() == busCalc1);
        assertTrue(generator1.getTerminal().getBusBreakerView().getBus() == busCalc2);
        assertTrue(busCalc1.getConnectedComponent().getNum() == 0);
        assertTrue(busCalc2.getConnectedComponent().getNum() == 0);

        assertTrue(Iterables.size(voltageLevel1.getBusView().getBuses()) == 1);
        Bus busCalc = voltageLevel1BusbarSection1.getTerminal().getBusView().getBus();
        assertTrue(busCalc == voltageLevel1BusbarSection2.getTerminal().getBusView().getBus());
        assertTrue(load1.getTerminal().getBusView().getBus() == busCalc);
        assertTrue(generator1.getTerminal().getBusView().getBus() == busCalc);
        assertTrue(busCalc.getConnectedComponent().getNum() == 0);
    }

    @Test
    public void testVoltageLevelGetConnectable() {
        Network n = EurostagTutorialExample1Factory.create();
        assertTrue(n.getVoltageLevel("VLLOAD").getConnectable("LOAD", Load.class) != null);
        assertTrue(n.getVoltageLevel("VLLOAD").getConnectable("NHV2_NLOAD", TwoTerminalsConnectable.class) != null);
        assertTrue(n.getVoltageLevel("VLGEN").getConnectable("LOAD", Load.class) == null);
        assertTrue(n.getVoltageLevel("VLGEN").getConnectable("NHV2_NLOAD", TwoTerminalsConnectable.class) == null);
    }

    @Test
    public void testStreams() {
        Function<Stream<? extends Identifiable<?>>, List<String>> mapper = stream -> stream.map(Identifiable::getId).collect(Collectors.toList());
        Function<Stream<? extends Identifiable<?>>, Set<String>> mapperSet = stream -> stream.map(Identifiable::getId).collect(Collectors.toSet());

        Network network = EurostagTutorialExample1Factory.create();
        assertEquals(Arrays.asList("P1", "P2"), mapper.apply(network.getSubstationsStream()));
        assertEquals(network.getSubstationCount(), network.getSubstationsStream().count());
        assertEquals(Arrays.asList("NHV1_NHV2_1", "NHV1_NHV2_2"), mapper.apply(network.getLinesStream()));
        assertEquals(network.getLineCount(), network.getLinesStream().count());

        assertEquals(Arrays.asList("VLGEN", "VLHV1", "VLHV2", "VLLOAD"), mapper.apply(network.getVoltageLevelsStream()));
        assertEquals(network.getVoltageLevelCount(), network.getVoltageLevelsStream().count());
        assertEquals(Arrays.asList("VLGEN", "VLHV1"), mapper.apply(network.getSubstation("P1").getVoltageLevelsStream()));
        assertEquals(Arrays.asList("VLHV2", "VLLOAD"), mapper.apply(network.getSubstation("P2").getVoltageLevelsStream()));

        assertEquals(Arrays.asList("NGEN", "NHV1", "NHV2", "NLOAD"), mapper.apply(network.getBusBreakerView().getBusesStream()));
        assertEquals(Collections.singletonList("NGEN"), mapper.apply(network.getVoltageLevel("VLGEN").getBusBreakerView().getBusesStream()));
        assertEquals(Collections.singletonList("NHV1"), mapper.apply(network.getVoltageLevel("VLHV1").getBusBreakerView().getBusesStream()));
        assertEquals(Collections.singletonList("NHV2"), mapper.apply(network.getVoltageLevel("VLHV2").getBusBreakerView().getBusesStream()));
        assertEquals(Collections.singletonList("NLOAD"), mapper.apply(network.getVoltageLevel("VLLOAD").getBusBreakerView().getBusesStream()));

        assertEquals(Arrays.asList("NGEN_NHV1", "NHV2_NLOAD"), mapper.apply(network.getTwoWindingsTransformersStream()));
        assertEquals(network.getTwoWindingsTransformerCount(), network.getTwoWindingsTransformersStream().count());
        assertEquals(Collections.singleton("NGEN_NHV1"), mapperSet.apply(network.getSubstation("P1").getTwoWindingsTransformersStream()));
        assertEquals(Collections.singleton("NHV2_NLOAD"), mapperSet.apply(network.getSubstation("P2").getTwoWindingsTransformersStream()));

        assertEquals(Collections.emptyList(), mapper.apply(network.getThreeWindingsTransformersStream()));
        assertEquals(network.getThreeWindingsTransformerCount(), network.getThreeWindingsTransformersStream().count());
        assertEquals(Collections.emptyList(), mapper.apply(network.getSubstation("P1").getThreeWindingsTransformersStream()));
        assertEquals(Collections.emptyList(), mapper.apply(network.getSubstation("P2").getThreeWindingsTransformersStream()));

        assertEquals(Collections.emptyList(), mapper.apply(network.getDanglingLinesStream()));
        assertEquals(network.getDanglingLineCount(), network.getDanglingLinesStream().count());
        assertEquals(Collections.emptyList(), mapper.apply(network.getShuntsStream()));
        assertEquals(network.getShuntCount(), network.getShuntsStream().count());

        assertEquals(Collections.singletonList("LOAD"), mapper.apply(network.getLoadsStream()));
        assertEquals(network.getLoadCount(), network.getLoadsStream().count());
        assertEquals(Collections.singletonList("LOAD"), mapper.apply(network.getVoltageLevel("VLLOAD").getLoadsStream()));

        assertEquals(Collections.singletonList("GEN"), mapper.apply(network.getGeneratorsStream()));
        assertEquals(network.getGeneratorCount(), network.getGeneratorsStream().count());
        assertEquals(Collections.singletonList("GEN"), mapper.apply(network.getVoltageLevel("VLGEN").getGeneratorsStream()));

        // SVC
        network = SvcTestCaseFactory.create();
        assertEquals(Collections.singletonList("SVC2"), mapper.apply(network.getStaticVarCompensatorsStream()));
        assertEquals(network.getStaticVarCompensatorCount(), network.getStaticVarCompensatorsStream().count());
        assertEquals(Collections.singletonList("SVC2"), mapper.apply(network.getVoltageLevel("VL2").getStaticVarCompensatorsStream()));

        // HVDC
        network = HvdcTestNetwork.createLcc();
        assertEquals(Collections.singletonList("L"), mapper.apply(network.getHvdcLinesStream()));
        assertEquals(network.getHvdcLineCount(), network.getHvdcLinesStream().count());
        assertEquals(Arrays.asList("C1", "C2"), mapper.apply(network.getLccConverterStationsStream()));
        assertEquals(network.getLccConverterStationCount(), network.getLccConverterStationsStream().count());
        assertEquals(Arrays.asList("C1", "C2"), mapper.apply(network.getHvdcConverterStationsStream()));
        assertEquals(network.getHvdcConverterStationCount(), network.getHvdcConverterStationsStream().count());
        assertEquals(Collections.singletonList("C1"), mapper.apply(network.getVoltageLevel("VL1").getLccConverterStationsStream()));
        assertEquals(Collections.singletonList("C2"), mapper.apply(network.getVoltageLevel("VL2").getLccConverterStationsStream()));
        assertEquals(Collections.singletonList("L"), mapper.apply(network.getHvdcLinesStream()));
        assertEquals(2, network.getLccConverterStation("C1").getFiltersStream().count());

        assertEquals(Collections.singletonList("DISC_BBS1_BK1"), mapper.apply(network.getBusBreakerView().getSwitchesStream()));

        network = HvdcTestNetwork.createVsc();
        assertEquals(Collections.singletonList("L"), mapper.apply(network.getHvdcLinesStream()));
        assertEquals(network.getHvdcLineCount(), network.getHvdcLinesStream().count());
        assertEquals(Arrays.asList("C1", "C2"), mapper.apply(network.getVscConverterStationsStream()));
        assertEquals(network.getLccConverterStationCount(), network.getLccConverterStationsStream().count());
        assertEquals(Arrays.asList("C1", "C2"), mapper.apply(network.getHvdcConverterStationsStream()));
        assertEquals(network.getHvdcConverterStationCount(), network.getHvdcConverterStationsStream().count());
        assertEquals(Collections.singletonList("C1"), mapper.apply(network.getVoltageLevel("VL1").getVscConverterStationsStream()));
        assertEquals(Collections.singletonList("C2"), mapper.apply(network.getVoltageLevel("VL2").getVscConverterStationsStream()));
        assertEquals(Collections.singletonList("L"), mapper.apply(network.getHvdcLinesStream()));
    }
}
