/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.google.common.collect.Iterables;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.VoltageLevel.NodeBreakerView;
import com.powsybl.iidm.network.test.*;
import com.powsybl.iidm.network.util.Networks;
import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public abstract class AbstractNetworkTest {

    private static final String REGION1 = "region1";
    private static final String NGEN_NHV1 = "NGEN_NHV1";
    private static final String NHV2_NLOAD = "NHV2_NLOAD";
    private static final String NHV1_NHV2_2 = "NHV1_NHV2_2";
    private static final String NHV1_NHV2_1 = "NHV1_NHV2_1";
    private static final String VOLTAGE_LEVEL1_BUSBAR_SECTION2 = "voltageLevel1BusbarSection2";
    private static final String SUBSTATION12 = "substation1";
    private static final String VL2_0 = "VL2_0";
    private static final String VOLTAGE_LEVEL1 = "voltageLevel1";
    private static final String VOLTAGE_LEVEL1_BUSBAR_SECTION1 = "voltageLevel1BusbarSection1";
    private static final String VOLTAGE_LEVEL1_BREAKER1 = "voltageLevel1Breaker1";
    private static final String VLHV2 = "VLHV2";
    private static final String VLLOAD = "VLLOAD";
    private static final String VLHV1 = "VLHV1";
    private static final String VLGEN = "VLGEN";
    private static final String VLBAT = "VLBAT";

    @Test
    public void testNetwork1() {
        Network network = NetworkTest1Factory.create();
        assertSame(network, network.getNetwork());
        assertEquals(1, Iterables.size(network.getCountries()));
        assertEquals(1, network.getCountryCount());
        Country country1 = network.getCountries().iterator().next();

        assertEquals(1, Iterables.size(network.getSubstations()));
        assertEquals(1, Iterables.size(network.getSubstations(Country.FR, "TSO1", REGION1)));
        assertEquals(1, network.getSubstationCount());
        assertEquals(2, network.getBusBreakerView().getBusCount());

        Substation substation1 = network.getSubstation(SUBSTATION12);
        assertNotNull(substation1);
        assertEquals(SUBSTATION12, substation1.getId());
        assertSame(country1, substation1.getCountry().orElse(null));
        assertEquals(1, substation1.getGeographicalTags().size());
        assertTrue(substation1.getGeographicalTags().contains(REGION1));
        assertEquals(1, Iterables.size(network.getVoltageLevels()));
        assertEquals(1, network.getVoltageLevelCount());

        VoltageLevel voltageLevel1 = network.getVoltageLevel(VOLTAGE_LEVEL1);
        assertNotNull(voltageLevel1);
        assertEquals(VOLTAGE_LEVEL1, voltageLevel1.getId());
        assertEquals(400.0, voltageLevel1.getNominalV(), 0.0);
        assertSame(substation1, voltageLevel1.getSubstation().orElse(null));
        assertSame(TopologyKind.NODE_BREAKER, voltageLevel1.getTopologyKind());

        NodeBreakerView topology1 = voltageLevel1.getNodeBreakerView();

        assertEquals(0.0, topology1.getFictitiousP0(0), 0.0);
        assertEquals(0.0, topology1.getFictitiousQ0(0), 0.0);
        topology1.setFictitiousP0(0, 1.0).setFictitiousQ0(0, 2.0);
        assertEquals(1.0, topology1.getFictitiousP0(0), 0.0);
        assertEquals(2.0, topology1.getFictitiousQ0(0), 0.0);
        Map<String, Set<Integer>> nodesByBus = Networks.getNodesByBus(voltageLevel1);
        nodesByBus.forEach((busId, nodes) -> {
            if (nodes.contains(0)) {
                assertEquals(1.0, voltageLevel1.getBusView().getBus(busId).getFictitiousP0(), 0.0);
            } else if (nodes.contains(1)) {
                assertEquals(2.0, voltageLevel1.getBusView().getBus(busId).getFictitiousP0(), 0.0);
            }
        });

        assertEquals(6, topology1.getMaximumNodeIndex());
        assertEquals(2, Iterables.size(topology1.getBusbarSections()));
        assertEquals(2, topology1.getBusbarSectionCount());

        assertEquals(2, Iterables.size(network.getBusbarSections()));
        assertEquals(2, network.getBusbarSectionCount());
        assertEquals(2, network.getBusbarSectionStream().count());

        BusbarSection voltageLevel1BusbarSection1 = topology1.getBusbarSection(VOLTAGE_LEVEL1_BUSBAR_SECTION1);
        assertNotNull(voltageLevel1BusbarSection1);
        assertEquals(VOLTAGE_LEVEL1_BUSBAR_SECTION1, voltageLevel1BusbarSection1.getId());

        BusbarSection voltageLevel1BusbarSection2 = topology1.getBusbarSection(VOLTAGE_LEVEL1_BUSBAR_SECTION2);
        assertNotNull(voltageLevel1BusbarSection2);
        assertEquals(VOLTAGE_LEVEL1_BUSBAR_SECTION2, voltageLevel1BusbarSection2.getId());
        assertEquals(5, Iterables.size(topology1.getSwitches()));
        assertEquals(5, topology1.getSwitchCount());

        VoltageLevel voltageLevel2 = substation1.newVoltageLevel().setId("VL2").setNominalV(320).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        assertNull(voltageLevel2.getNodeBreakerView().getBusbarSection(VOLTAGE_LEVEL1_BUSBAR_SECTION1));

        assertEquals(Arrays.asList(network.getSwitch("generator1Disconnector1"), network.getSwitch("generator1Breaker1")),
            topology1.getSwitches(6));
        assertEquals(Arrays.asList(network.getSwitch("load1Disconnector1"), network.getSwitch("load1Breaker1")),
            topology1.getSwitchStream(3).collect(Collectors.toList()));
        assertEquals(Collections.singletonList(network.getSwitch("load1Disconnector1")), topology1.getSwitches(2));

        assertEquals(5, Iterables.size(network.getSwitches()));
        assertEquals(5, network.getSwitchCount());
        assertEquals(5, network.getSwitchStream().count());

        Switch voltageLevel1Breaker1 = topology1.getSwitch(VOLTAGE_LEVEL1_BREAKER1);
        assertNotNull(voltageLevel1Breaker1);
        assertEquals(VOLTAGE_LEVEL1_BREAKER1, voltageLevel1Breaker1.getId());
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
        assertEquals(2, voltageLevel1.getBusBreakerView().getBusCount());
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

        // Changes listener
        NetworkListener exceptionListener = mock(DefaultNetworkListener.class);
        doThrow(new UnsupportedOperationException()).when(exceptionListener).onElementAdded(any(), anyString(), any());
        doThrow(new UnsupportedOperationException()).when(exceptionListener).onElementReplaced(any(), anyString(),
                any(), any());
        NetworkListener mockedListener = mock(DefaultNetworkListener.class);

        // Identifiable properties
        String key = "keyTest";
        String value = "ValueTest";
        assertFalse(busCalc.hasProperty());
        assertTrue(busCalc.getPropertyNames().isEmpty());
        // Test without listeners registered
        busCalc.setProperty("listeners", "no listeners");
        // Test without listeners registered & same values
        busCalc.setProperty("listeners", "no listeners");
        verifyNoMoreInteractions(mockedListener);
        verifyNoMoreInteractions(exceptionListener);
        // Add observer changes to current network
        network.addListener(mockedListener);
        network.addListener(exceptionListener);
        // Test with listeners registered
        busCalc.setProperty(key, value);
        assertTrue(busCalc.hasProperty());
        assertTrue(busCalc.hasProperty(key));
        assertEquals(value, busCalc.getProperty(key));
        assertEquals("default", busCalc.getProperty("invalid", "default"));
        assertEquals(2, busCalc.getPropertyNames().size());

        // Check notification done
        verify(mockedListener, times(1))
                .onElementAdded(busCalc, "properties[" + key + "]", value);
        // Check no notification on same property
        String value2 = "ValueTest2";
        busCalc.setProperty(key, value2);
        verify(mockedListener, times(1))
                .onElementReplaced(busCalc, "properties[" + key + "]", value, value2);
        // Check no notification on same property
        busCalc.setProperty(key, value2);
        verifyNoMoreInteractions(mockedListener);
        // Remove changes observer
        network.removeListener(mockedListener);
        // Adding same property without listener registered
        busCalc.setProperty(key, value);
        // Check no notification
        verifyNoMoreInteractions(mockedListener);

        // validation
        assertEquals(ValidationLevel.STEADY_STATE_HYPOTHESIS, network.getValidationLevel());
        network.runValidationChecks();
        network.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        assertEquals(ValidationLevel.STEADY_STATE_HYPOTHESIS, network.getValidationLevel());
        network.getLoad("load1").setP0(0.0);
        voltageLevel1.newLoad()
                .setId("unchecked")
                .setP0(1.0)
                .setQ0(1.0)
                .setNode(3)
                .add();
        assertEquals(ValidationLevel.STEADY_STATE_HYPOTHESIS, network.getValidationLevel());
        network.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        Load unchecked2 = voltageLevel1.newLoad()
                .setId("unchecked2")
                .setNode(10)
                .add();
        assertEquals(ValidationLevel.EQUIPMENT, network.getValidationLevel());
        unchecked2.setP0(0.0).setQ0(0.0);
        assertEquals(ValidationLevel.STEADY_STATE_HYPOTHESIS, network.getValidationLevel());
        network.setMinimumAcceptableValidationLevel(ValidationLevel.STEADY_STATE_HYPOTHESIS);
    }

    @Test
    public void testNetwork1WithoutCountry() {
        Network network = NetworkTest1Factory.create();

        Substation substation1 = network.getSubstation(SUBSTATION12);
        substation1.setCountry(null);

        assertEquals(0, Iterables.size(network.getCountries()));
        assertEquals(0, network.getCountryCount());
        assertEquals(1, Iterables.size(network.getSubstations("", "TSO1", REGION1)));
        assertFalse(substation1.getCountry().isPresent());
    }

    @Test
    public void testNetworkWithBattery() {
        Network network = BatteryNetworkFactory.create();
        assertEquals(1, Iterables.size(network.getCountries()));
        assertEquals(1, network.getCountryCount());
        Country country1 = network.getCountries().iterator().next();

        assertEquals(2, Iterables.size(network.getSubstations()));
        assertEquals(1, Iterables.size(network.getSubstations(Country.FR, "R", "A")));
        assertEquals(2, network.getSubstationCount());
        assertEquals(2, Iterables.size(network.getVoltageLevels()));
        assertEquals(2, network.getVoltageLevelCount());
        assertEquals(2, Iterables.size(network.getBatteries()));
        assertEquals(2, network.getBatteryCount());
        assertEquals(2, network.getBusBreakerView().getBusCount());

        // Substation A
        Substation substation1 = network.getSubstation("P1");
        assertNotNull(substation1);
        assertEquals("P1", substation1.getId());
        assertSame(country1, substation1.getCountry().orElse(null));
        assertEquals(1, substation1.getGeographicalTags().size());
        assertTrue(substation1.getGeographicalTags().contains("A"));
        assertEquals(1, Iterables.size(substation1.getVoltageLevels()));

        VoltageLevel voltageLevel1 = network.getVoltageLevel(VLGEN);
        assertNotNull(voltageLevel1);
        assertEquals(VLGEN, voltageLevel1.getId());
        assertEquals(400.0, voltageLevel1.getNominalV(), 0.0);
        assertSame(substation1, voltageLevel1.getSubstation().orElse(null));
        assertSame(TopologyKind.BUS_BREAKER, voltageLevel1.getTopologyKind());
        assertEquals(1, voltageLevel1.getBusBreakerView().getBusCount());

        Bus bus1 = voltageLevel1.getBusBreakerView().getBus("NGEN");
        assertEquals(3, bus1.getConnectedTerminalCount());

        Generator generator1 = network.getGenerator("GEN");
        assertNotNull(generator1);
        assertEquals("GEN", generator1.getId());
        assertEquals(-9999.99, generator1.getMinP(), 0.0);
        assertEquals(9999.99, generator1.getMaxP(), 0.0);
        assertSame(EnergySource.OTHER, generator1.getEnergySource());
        assertTrue(generator1.isVoltageRegulatorOn());
        assertEquals(607.0, generator1.getTargetP(), 0.0);
        assertEquals(24.5, generator1.getTargetV(), 0.0);
        assertEquals(301.0, generator1.getTargetQ(), 0.0);
        assertEquals(bus1.getId(), generator1.getTerminal().getBusBreakerView().getBus().getId());

        // Substation B
        Substation substation2 = network.getSubstation("P2");
        assertNotNull(substation2);
        assertEquals("P2", substation2.getId());
        assertSame(country1, substation2.getCountry().orElse(null));
        assertEquals(1, substation2.getGeographicalTags().size());
        assertTrue(substation2.getGeographicalTags().contains("B"));
        assertEquals(1, Iterables.size(substation1.getVoltageLevels()));

        VoltageLevel voltageLevel2 = network.getVoltageLevel(VLBAT);
        assertNotNull(voltageLevel2);
        assertEquals(VLBAT, voltageLevel2.getId());
        assertEquals(400.0, voltageLevel2.getNominalV(), 0.0);
        assertSame(substation2, voltageLevel2.getSubstation().orElse(null));
        assertSame(TopologyKind.BUS_BREAKER, voltageLevel2.getTopologyKind());

        Bus bus2 = voltageLevel2.getBusBreakerView().getBus("NBAT");
        assertEquals(5, bus2.getConnectedTerminalCount());

        Battery battery1 = network.getBattery("BAT");
        assertNotNull(battery1);
        assertEquals("BAT", battery1.getId());
        assertEquals(9999.99, battery1.getTargetP(), 0.0);
        assertEquals(9999.99, battery1.getTargetQ(), 0.0);
        assertEquals(-9999.99, battery1.getMinP(), 0.0);
        assertEquals(9999.99, battery1.getMaxP(), 0.0);
        assertEquals(bus2.getId(), battery1.getTerminal().getBusBreakerView().getBus().getId());

        Battery battery2 = network.getBattery("BAT2");
        assertNotNull(battery2);
        assertEquals("BAT2", battery2.getId());
        assertEquals(100, battery2.getTargetP(), 0.0);
        assertEquals(200, battery2.getTargetQ(), 0.0);
        assertEquals(-200, battery2.getMinP(), 0.0);
        assertEquals(200, battery2.getMaxP(), 0.0);
        assertEquals(bus2.getId(), battery2.getTerminal().getBusBreakerView().getBus().getId());

        Load load1 = network.getLoad("LOAD");
        assertNotNull(load1);
        assertEquals("LOAD", load1.getId());
        assertEquals(600.0, load1.getP0(), 0.0);
        assertEquals(200.0, load1.getQ0(), 0.0);
        assertEquals(bus2.getId(), load1.getTerminal().getBusBreakerView().getBus().getId());

        //Specific test on battery
        assertEquals(battery1, voltageLevel2.getConnectable("BAT", Battery.class));
        //Stream test
        Function<Stream<? extends Identifiable>, List<String>> mapper = stream -> stream.map(Identifiable::getId).collect(Collectors.toList());
        assertEquals(Arrays.asList("BAT", "BAT2"), mapper.apply(network.getBatteryStream()));
        assertEquals(network.getBatteryCount(), network.getBatteryStream().count());
        assertEquals(Arrays.asList("BAT", "BAT2"), mapper.apply(network.getVoltageLevel(VLBAT).getBatteryStream()));
        assertEquals(Arrays.asList("BAT", "BAT2"), mapper.apply(bus2.getBatteryStream()));
    }

    @Test
    public void testVoltageLevelGetConnectable() {
        Network n = EurostagTutorialExample1Factory.create();
        assertNotNull(n.getVoltageLevel(VLLOAD).getConnectable("LOAD", Load.class));
        assertNotNull(n.getVoltageLevel(VLLOAD).getConnectable(NHV2_NLOAD, TwoWindingsTransformer.class));
        assertNull(n.getVoltageLevel(VLGEN).getConnectable("LOAD", Load.class));
        assertNull(n.getVoltageLevel(VLGEN).getConnectable(NHV2_NLOAD, TwoWindingsTransformer.class));
    }

    @Test
    public void testGetConnectable() {
        Network n = EurostagTutorialExample1Factory.create();
        assertEquals(6, n.getConnectableCount());
        assertNotNull(n.getConnectable("GEN"));
        assertTrue(n.getConnectable("GEN") instanceof Generator);
        assertEquals("GEN", n.getConnectable("GEN").getId());
    }

    @Test
    public void testStreams() {
        Function<Stream<? extends Identifiable>, List<String>> mapper = stream -> stream.map(Identifiable::getId).collect(Collectors.toList());
        Function<Stream<? extends Identifiable>, Set<String>> mapperSet = stream -> stream.map(Identifiable::getId).collect(Collectors.toSet());

        Network network = EurostagTutorialExample1Factory.create();
        String nhv1nhv1 = "NHV1_NHV1";
        network.getVoltageLevel(VLHV1).getBusBreakerView()
                .newBus()
                .setId("NHV1_bis")
                .add();
        network.getSubstation("P1").newTwoWindingsTransformer()
                .setId(nhv1nhv1)
                .setVoltageLevel1(VLHV1)
                .setBus1("NHV1")
                .setConnectableBus1("NHV1")
                .setRatedU1(380)
                .setVoltageLevel2(VLHV1)
                .setBus2("NHV1_bis")
                .setConnectableBus2("NHV1_bis")
                .setRatedU2(400.0)
                .setR(0.24 / 1300 * (38 * 38))
                .setX(Math.sqrt(10 * 10 - 0.24 * 0.24) / 1300 * (38 * 38))
                .setG(0.0)
                .setB(0.0)
                .add();

        assertEquals(Arrays.asList("P1", "P2"), mapper.apply(network.getSubstationStream()));
        assertEquals(network.getSubstationCount(), network.getSubstationStream().count());
        assertEquals(Arrays.asList(NHV1_NHV2_1, NHV1_NHV2_2), mapper.apply(network.getLineStream()));
        assertEquals(network.getLineCount(), network.getLineStream().count());

        assertEquals(Arrays.asList(VLGEN, VLHV1, VLHV2, VLLOAD), mapper.apply(network.getVoltageLevelStream()));
        assertEquals(network.getVoltageLevelCount(), network.getVoltageLevelStream().count());
        assertEquals(Arrays.asList(VLGEN, VLHV1), mapper.apply(network.getSubstation("P1").getVoltageLevelStream()));
        assertEquals(Arrays.asList(VLHV2, VLLOAD), mapper.apply(network.getSubstation("P2").getVoltageLevelStream()));

        assertEquals(Arrays.asList("NGEN", "NHV1", "NHV1_bis", "NHV2", "NLOAD"), mapper.apply(network.getBusBreakerView().getBusStream()));
        assertEquals(Collections.singletonList("NGEN"), mapper.apply(network.getVoltageLevel(VLGEN).getBusBreakerView().getBusStream()));
        assertEquals(Arrays.asList("NHV1", "NHV1_bis"), mapper.apply(network.getVoltageLevel(VLHV1).getBusBreakerView().getBusStream()));
        assertEquals(Collections.singletonList("NHV2"), mapper.apply(network.getVoltageLevel(VLHV2).getBusBreakerView().getBusStream()));
        assertEquals(Collections.singletonList("NLOAD"), mapper.apply(network.getVoltageLevel(VLLOAD).getBusBreakerView().getBusStream()));

        assertEquals(Arrays.asList(NHV1_NHV2_1, NHV1_NHV2_2, NGEN_NHV1, nhv1nhv1), mapper.apply(network.getVoltageLevel(VLHV1).getConnectableStream()));
        assertEquals(network.getVoltageLevel(VLHV1).getConnectableCount(), network.getVoltageLevel(VLHV1).getConnectableStream().count());

        assertEquals(Arrays.asList(NGEN_NHV1, NHV2_NLOAD, nhv1nhv1), mapper.apply(network.getTwoWindingsTransformerStream()));
        assertEquals(network.getTwoWindingsTransformerCount(), network.getTwoWindingsTransformerStream().count());
        assertEquals(Arrays.asList(NGEN_NHV1, nhv1nhv1), mapper.apply(network.getSubstation("P1").getTwoWindingsTransformerStream()));
        assertEquals(Collections.singleton(NHV2_NLOAD), mapperSet.apply(network.getSubstation("P2").getTwoWindingsTransformerStream()));
        assertEquals(Arrays.asList(NGEN_NHV1, nhv1nhv1), mapper.apply(network.getVoltageLevel(VLHV1).getConnectableStream(TwoWindingsTransformer.class)));
        assertEquals(network.getVoltageLevel(VLHV1).getConnectableCount(TwoWindingsTransformer.class), network.getVoltageLevel(VLHV1).getConnectableStream(TwoWindingsTransformer.class).count());

        assertEquals(Arrays.asList(NHV1_NHV2_1, NHV1_NHV2_2, NGEN_NHV1, NHV2_NLOAD, nhv1nhv1), mapper.apply(network.getBranchStream()));
        assertEquals(network.getBranchCount(), network.getBranchStream().count());

        assertEquals(Collections.emptyList(), mapper.apply(network.getThreeWindingsTransformerStream()));
        assertEquals(network.getThreeWindingsTransformerCount(), network.getThreeWindingsTransformerStream().count());
        assertEquals(Collections.emptyList(), mapper.apply(network.getSubstation("P1").getThreeWindingsTransformerStream()));
        assertEquals(Collections.emptyList(), mapper.apply(network.getSubstation("P2").getThreeWindingsTransformerStream()));

        assertEquals(Collections.emptyList(), mapper.apply(network.getDanglingLineStream(DanglingLineFilter.ALL)));
        assertEquals(Collections.emptyList(), mapper.apply(network.getVoltageLevel(VLHV1).getDanglingLineStream(DanglingLineFilter.ALL)));
        assertEquals(network.getDanglingLineCount(), network.getDanglingLineStream(DanglingLineFilter.ALL).count());
        assertEquals(Collections.emptyList(), mapper.apply(network.getShuntCompensatorStream()));
        assertEquals(Collections.emptyList(), mapper.apply(network.getVoltageLevel(VLHV2).getShuntCompensatorStream()));
        assertEquals(network.getShuntCompensatorCount(), network.getShuntCompensatorStream().count());

        assertEquals(Collections.singletonList("LOAD"), mapper.apply(network.getLoadStream()));
        assertEquals(network.getLoadCount(), network.getLoadStream().count());
        assertEquals(Collections.singletonList("LOAD"), mapper.apply(network.getVoltageLevel(VLLOAD).getLoadStream()));

        assertEquals(Collections.singletonList("GEN"), mapper.apply(network.getGeneratorStream()));
        assertEquals(network.getGeneratorCount(), network.getGeneratorStream().count());
        assertEquals(Collections.singletonList("GEN"), mapper.apply(network.getVoltageLevel(VLGEN).getGeneratorStream()));

        Bus bus = network.getVoltageLevel(VLGEN).getBusView().getBus("VLGEN_0");
        assertEquals(Collections.singletonList(NGEN_NHV1), mapper.apply(bus.getTwoWindingsTransformerStream()));
        assertEquals(Collections.singletonList("GEN"), mapper.apply(bus.getGeneratorStream()));
        bus = network.getVoltageLevel(VLHV1).getBusView().getBus("VLHV1_0");
        assertEquals(Arrays.asList(NHV1_NHV2_1, NHV1_NHV2_2), mapper.apply(bus.getLineStream()));
        assertEquals(Arrays.asList(NGEN_NHV1, nhv1nhv1), mapper.apply(bus.getTwoWindingsTransformerStream()));
        bus = network.getVoltageLevel(VLHV2).getBusView().getBus("VLHV2_0");
        assertEquals(Collections.singletonList(NHV2_NLOAD), mapper.apply(bus.getTwoWindingsTransformerStream()));
        bus = network.getVoltageLevel(VLLOAD).getBusView().getBus("VLLOAD_0");
        assertEquals(Collections.singletonList("LOAD"), mapper.apply(bus.getLoadStream()));

        // Connectables
        assertEquals(Arrays.asList(NHV1_NHV2_1, NHV1_NHV2_2, NGEN_NHV1, NHV2_NLOAD, "LOAD", "GEN", "NHV1_NHV1"), mapper.apply(network.getConnectableStream()));
        assertArrayEquals(Iterables.toArray(network.getConnectables(), Connectable.class), network.getConnectableStream().toArray());
        assertEquals(network.getConnectableCount(), network.getConnectableStream().count());

        // SVC
        network = SvcTestCaseFactory.create();
        assertEquals(Collections.singletonList("SVC2"), mapper.apply(network.getStaticVarCompensatorStream()));
        assertEquals(network.getStaticVarCompensatorCount(), network.getStaticVarCompensatorStream().count());
        assertEquals(Collections.singletonList("SVC2"), mapper.apply(network.getVoltageLevel("VL2").getStaticVarCompensatorStream()));
        bus = network.getVoltageLevel("VL2").getBusView().getBus(VL2_0);
        assertEquals(Collections.singletonList("SVC2"), mapper.apply(bus.getStaticVarCompensatorStream()));
        assertEquals(Collections.singletonList("SVC2"), mapper.apply(network.getConnectableStream(StaticVarCompensator.class)));
        assertArrayEquals(Iterables.toArray(network.getConnectables(StaticVarCompensator.class), StaticVarCompensator.class),
                network.getConnectableStream(StaticVarCompensator.class).toArray());
        assertEquals(network.getConnectableCount(StaticVarCompensator.class), network.getConnectableStream(StaticVarCompensator.class).count());

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
        bus = network.getVoltageLevel("VL2").getBusView().getBus(VL2_0);
        assertEquals(Collections.singletonList("C2"), mapper.apply(bus.getLccConverterStationStream()));

        assertEquals(Arrays.asList("BK1", "BK2", "BK3"), mapper.apply(network.getBusBreakerView().getSwitchStream()));

        network = HvdcTestNetwork.createVsc();
        assertEquals(Collections.singletonList("L"), mapper.apply(network.getHvdcLineStream()));
        assertEquals(network.getHvdcLineCount(), network.getHvdcLineStream().count());
        assertEquals(Arrays.asList("C1", "C2"), mapper.apply(network.getVscConverterStationStream()));
        assertEquals(network.getVscConverterStationCount(), network.getVscConverterStationStream().count());
        assertEquals(Arrays.asList("C1", "C2"), mapper.apply(network.getHvdcConverterStationStream()));
        assertEquals(network.getHvdcConverterStationCount(), network.getHvdcConverterStationStream().count());
        assertEquals(Collections.singletonList("C1"), mapper.apply(network.getVoltageLevel("VL1").getVscConverterStationStream()));
        assertEquals(Collections.singletonList("C2"), mapper.apply(network.getVoltageLevel("VL2").getVscConverterStationStream()));
        assertEquals(Collections.singletonList("L"), mapper.apply(network.getHvdcLineStream()));
        bus = network.getVoltageLevel("VL2").getBusView().getBus(VL2_0);
        assertEquals(Collections.singletonList("C2"), mapper.apply(bus.getVscConverterStationStream()));

        // Topology
        network = NetworkTest1Factory.create();
        assertEquals(Arrays.asList(VOLTAGE_LEVEL1_BUSBAR_SECTION1, VOLTAGE_LEVEL1_BUSBAR_SECTION2),
                mapper.apply(network.getVoltageLevel(VOLTAGE_LEVEL1).getNodeBreakerView().getBusbarSectionStream()));
        assertEquals(Collections.singletonList(VOLTAGE_LEVEL1_BREAKER1),
                mapper.apply(network.getVoltageLevel(VOLTAGE_LEVEL1).getNodeBreakerView()
                        .getSwitchStream()
                        .filter(sw -> sw.getKind() == SwitchKind.BREAKER)));
        assertEquals(Arrays.asList("load1Disconnector1", "load1Breaker1"),
                mapper.apply(network.getVoltageLevel(VOLTAGE_LEVEL1).getNodeBreakerView()
                        .getSwitchStream()
                        .filter(sw -> sw.getKind() == SwitchKind.DISCONNECTOR)
                        .limit(2)));
        assertEquals(Collections.emptyList(),
                mapper.apply(network.getVoltageLevel(VOLTAGE_LEVEL1).getNodeBreakerView()
                        .getSwitchStream()
                        .filter(sw -> sw.getKind() == SwitchKind.LOAD_BREAK_SWITCH)));
    }

    @Test
    public void testSetterGetter() {
        String sourceFormat = "test_sourceFormat";
        Network network = Network.create("test", sourceFormat);
        ZonedDateTime caseDate = ZonedDateTime.now(ZoneOffset.UTC);
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
        busViewNetwork.getVoltageLevel(VLGEN);

        Network nodeViewNetwork = NetworkTest1Factory.create();
        VoltageLevel voltageLevel = nodeViewNetwork.getVoltageLevel(VOLTAGE_LEVEL1);
        NodeBreakerView topology = voltageLevel.getNodeBreakerView();
        assertEquals(topology.getTerminal(topology.getNode1(VOLTAGE_LEVEL1_BREAKER1)),
                voltageLevel.getNodeBreakerView().getTerminal1(VOLTAGE_LEVEL1_BREAKER1));
        assertEquals(topology.getTerminal(topology.getNode2(VOLTAGE_LEVEL1_BREAKER1)),
                voltageLevel.getNodeBreakerView().getTerminal2(VOLTAGE_LEVEL1_BREAKER1));
    }

    @Test
    public void testExceptionGetSwitchTerminal1() {
        Network busViewNetwork = EurostagTutorialExample1Factory.create();
        VoltageLevel voltageLevel = busViewNetwork.getVoltageLevel(VLGEN);
        PowsyblException e = assertThrows(PowsyblException.class, () -> voltageLevel.getNodeBreakerView().getTerminal1("fictitiousSwitchId"));
        assertTrue(e.getMessage().contains("Not supported in a bus breaker topology"));
    }

    @Test
    public void testExceptionGetSwitchTerminal2() {
        Network busViewNetwork = EurostagTutorialExample1Factory.create();
        VoltageLevel voltageLevel = busViewNetwork.getVoltageLevel(VLGEN);
        PowsyblException e = assertThrows(PowsyblException.class, () -> voltageLevel.getNodeBreakerView().getTerminal2("fictitiousSwitchId"));
        assertTrue(e.getMessage().contains("Not supported in a bus breaker topology"));
    }

    @Test
    public void testCreate() {
        // check default implementation is used
        Network network = assertDoesNotThrow(() -> Network.create("test", "test"));
        assertNotNull(network);
    }

    @Test
    public void testWith() {
        // check default implementation is returned
        Network.with("Default").create("test", "test");

        // check that an exception is thrown if implementation is not found
        try {
            Network.with("???").create("test", "test");
            fail();
        } catch (PowsyblException ignored) {
            // ignore
        }
    }

    @Test
    public void testScadaNetwork() {
        Network network = ScadaNetworkFactory.create();
        assertEquals(ValidationLevel.EQUIPMENT, network.getValidationLevel());

        assertEquals(ValidationLevel.EQUIPMENT, network.runValidationChecks(false));

        ReportNode reportNode = ReportNode.newRootReportNode().withMessageTemplate("testReportScadaNetwork", "Test reporting of SCADA network").build();
        assertEquals(ValidationLevel.EQUIPMENT, network.runValidationChecks(false, reportNode));

        List<ReportNode> children = reportNode.getChildren();
        assertEquals(1, children.size());
        ReportNode reportNodeChild = children.get(0);
        assertEquals("IIDMValidation", reportNodeChild.getMessageKey());
        assertEquals("Running validation checks on IIDM network scada", reportNodeChild.getMessage());

        List<ReportNode> messageNodes = reportNodeChild.getChildren();
        assertFalse(messageNodes.isEmpty());

        assertEquals(ValidationLevel.EQUIPMENT, network.getValidationLevel());

        try {
            network.runValidationChecks();
            fail();
        } catch (ValidationException e) {
            // Ignore
        }

        try {
            network.setMinimumAcceptableValidationLevel(ValidationLevel.STEADY_STATE_HYPOTHESIS);
            fail();
        } catch (ValidationException e) {
            // Ignore
        }
    }

    @Test
    public void testPermanentLimitOnSelectedOperationalLimitsGroup() {
        Network network = EurostagTutorialExample1Factory.createWithFixedCurrentLimits();
        assertEquals(ValidationLevel.STEADY_STATE_HYPOTHESIS, network.getValidationLevel());
        ValidationException e = assertThrows(ValidationException.class, () -> network.getLine("NHV1_NHV2_1").getCurrentLimits2().orElseThrow().setPermanentLimit(Double.NaN));
        assertTrue(e.getMessage().contains("AC line 'NHV1_NHV2_1': permanent limit must be defined if temporary limits are present"));
        network.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        network.getLine("NHV1_NHV2_1").getCurrentLimits2().orElseThrow().setPermanentLimit(Double.NaN);
        assertTrue(Double.isNaN(network.getLine("NHV1_NHV2_1").getCurrentLimits2().orElseThrow().getPermanentLimit()));
        assertEquals(ValidationLevel.EQUIPMENT, network.getValidationLevel());
    }

    @Test
    public void testPermanentLimitViaAdder() {
        Network network = EurostagTutorialExample1Factory.createWithFixedCurrentLimits();
        assertEquals(ValidationLevel.STEADY_STATE_HYPOTHESIS, network.getValidationLevel());
        OperationalLimitsGroup unselectedGroup = network.getLine("NHV1_NHV2_1").newOperationalLimitsGroup1("unselectedGroup");
        assertNotEquals("unselectedGroup", network.getLine("NHV1_NHV2_1").getSelectedOperationalLimitsGroupId1().orElseThrow());
        CurrentLimitsAdder adder = unselectedGroup.newCurrentLimits()
                .setPermanentLimit(Double.NaN)
                .beginTemporaryLimit()
                .setName("5'")
                .setAcceptableDuration(300)
                .setValue(1000)
                .endTemporaryLimit();
        ValidationException e = assertThrows(ValidationException.class, adder::add);
        assertTrue(e.getMessage().contains("AC line 'NHV1_NHV2_1': permanent limit must be defined if temporary limits are present"));
        network.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        adder.add();
        assertEquals(ValidationLevel.EQUIPMENT, network.getValidationLevel());
    }

    @Test
    public void testPermanentLimitOnUnselectedOperationalLimitsGroup() {
        Network network = EurostagTutorialExample1Factory.createWithFixedCurrentLimits();
        assertEquals(ValidationLevel.STEADY_STATE_HYPOTHESIS, network.getValidationLevel());
        OperationalLimitsGroup unselectedGroup = network.getLine("NHV1_NHV2_1").newOperationalLimitsGroup1("unselectedGroup");
        assertNotEquals("unselectedGroup", network.getLine("NHV1_NHV2_1").getSelectedOperationalLimitsGroupId1().orElseThrow());
        CurrentLimitsAdder adder = unselectedGroup.newCurrentLimits()
                .setPermanentLimit(Double.NaN)
                .beginTemporaryLimit()
                    .setName("5'")
                    .setAcceptableDuration(300)
                    .setValue(1000)
                .endTemporaryLimit();
        ValidationException e = assertThrows(ValidationException.class, adder::add);
        assertTrue(e.getMessage().contains("AC line 'NHV1_NHV2_1': permanent limit must be defined if temporary limits are present"));
        CurrentLimits currentLimits = adder.setPermanentLimit(1000).add();
        assertEquals(ValidationLevel.STEADY_STATE_HYPOTHESIS, network.getValidationLevel());

        e = assertThrows(ValidationException.class, () -> currentLimits.setPermanentLimit(Double.NaN));
        assertTrue(e.getMessage().contains("AC line 'NHV1_NHV2_1': permanent limit must be defined if temporary limits are present"));
        network.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        currentLimits.setPermanentLimit(Double.NaN);
        assertTrue(Double.isNaN(currentLimits.getPermanentLimit()));
        assertEquals(ValidationLevel.EQUIPMENT, network.getValidationLevel());
    }
}
