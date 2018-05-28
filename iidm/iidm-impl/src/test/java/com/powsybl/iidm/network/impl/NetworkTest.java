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
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
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

        assertEquals(2, Iterables.size(network.getBusbarSections()));
        assertEquals(2, network.getBusbarSectionCount());
        assertEquals(2, network.getBusbarSectionStream().count());

        BusbarSection voltageLevel1BusbarSection1 = topology1.getBusbarSection("voltageLevel1BusbarSection1");
        assertTrue(voltageLevel1BusbarSection1 != null);
        assertTrue(voltageLevel1BusbarSection1.getId().equals("voltageLevel1BusbarSection1"));

        BusbarSection voltageLevel1BusbarSection2 = topology1.getBusbarSection("voltageLevel1BusbarSection2");
        assertTrue(voltageLevel1BusbarSection2 != null);
        assertTrue(voltageLevel1BusbarSection2.getId().equals("voltageLevel1BusbarSection2"));
        assertTrue(Iterables.size(topology1.getSwitches()) == 5);
        assertTrue(topology1.getSwitchCount() == 5);

        assertEquals(5, Iterables.size(network.getSwitches()));
        assertEquals(5, network.getSwitchCount());
        assertEquals(5, network.getSwitchStream().count());

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
        assertTrue(n.getVoltageLevel("VLLOAD").getConnectable("NHV2_NLOAD", Branch.class) != null);
        assertTrue(n.getVoltageLevel("VLGEN").getConnectable("LOAD", Load.class) == null);
        assertTrue(n.getVoltageLevel("VLGEN").getConnectable("NHV2_NLOAD", Branch.class) == null);
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
        assertEquals(Collections.emptyList(), mapper.apply(network.getShuntStream()));
        assertEquals(Collections.emptyList(), mapper.apply(network.getVoltageLevel("VLHV2").getShuntStream()));
        assertEquals(network.getShuntCount(), network.getShuntStream().count());

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
        boolean failed = false;
        try {
            voltageLevel.getNodeBreakerView().getTerminal1("fictitiousSwitchId");
        } catch (PowsyblException p) {
            failed = true;
        }
        assertTrue(failed);
        failed = false;
        try {
            voltageLevel.getNodeBreakerView().getTerminal2("fictitiousSwitchId");
        } catch (PowsyblException p) {
            failed = true;
        }
        assertTrue(failed);

        Network nodeViewNetwork = NetworkTest1Factory.create();
        voltageLevel = nodeViewNetwork.getVoltageLevel("voltageLevel1");
        NodeBreakerView topology = voltageLevel.getNodeBreakerView();
        assertEquals(topology.getTerminal(topology.getNode1("voltageLevel1Breaker1")),
                     voltageLevel.getNodeBreakerView().getTerminal1("voltageLevel1Breaker1"));
        assertEquals(topology.getTerminal(topology.getNode2("voltageLevel1Breaker1")),
                     voltageLevel.getNodeBreakerView().getTerminal2("voltageLevel1Breaker1"));

    }

}
