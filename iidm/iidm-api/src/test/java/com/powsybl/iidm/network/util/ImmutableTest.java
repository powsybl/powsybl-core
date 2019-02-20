/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.google.common.collect.Sets;
import com.powsybl.iidm.network.*;
import org.junit.Test;
import org.mockito.verification.VerificationMode;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This test is to guarant that objects acquired by getter in immutable object are always be wrapped as immutbale
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class ImmutableTest {

    private static final VerificationMode ONCE = times(1);

    private static final List<Bus> MOCK_BUS_LIST = Arrays.asList(mock(Bus.class), mock(Bus.class));
    private static final Supplier<Stream<Bus>> MOCK_BUS_STREAM = () -> MOCK_BUS_LIST.stream();

    private static final TieLine MOCK_TIELINE = mock(TieLine.class);
    private static final Line MOCK_LINE = mock(Line.class);

    static {
        when(MOCK_TIELINE.isTieLine()).thenReturn(true);
        when(MOCK_TIELINE.getType()).thenReturn(ConnectableType.LINE);
        when(MOCK_TIELINE.getHalf1()).thenReturn(mock(TieLine.HalfLine.class));
        when(MOCK_TIELINE.getHalf2()).thenReturn(mock(TieLine.HalfLine.class));
        when(MOCK_LINE.isTieLine()).thenReturn(false);
        when(MOCK_LINE.getType()).thenReturn(ConnectableType.LINE);
    }

    private static final List<Line> MOCK_LINE_LIST = Arrays.asList(MOCK_TIELINE, MOCK_LINE);
    private static final Supplier<Stream<Line>> MOCK_LINE_STREAM = () -> MOCK_LINE_LIST.stream();

    private static final List<TwoWindingsTransformer> MOCK_2WT_LIST = Collections.singletonList(mock(TwoWindingsTransformer.class));
    private static final Supplier<Stream<TwoWindingsTransformer>> MOCK_2WT_STREAM = () -> MOCK_2WT_LIST.stream();

    private static final List<ThreeWindingsTransformer> MOCK_3WT_LIST = Collections.singletonList(mock(ThreeWindingsTransformer.class));
    private static final Supplier<Stream<ThreeWindingsTransformer>> MOCK_3WT_STREAM = () -> MOCK_3WT_LIST.stream();

    private static final List<Generator> MOCK_GENERATOR_LIST = Collections.singletonList(mock(Generator.class));
    private static final Supplier<Stream<Generator>> MOCK_GENERATOR_STREAM = () -> MOCK_GENERATOR_LIST.stream();

    private static final List<Load> MOCK_LOAD_LIST = Collections.singletonList(mock(Load.class));
    private static final Supplier<Stream<Load>> MOCK_LOAD_STREAM = () -> MOCK_LOAD_LIST.stream();

    private static final List<DanglingLine> MOCK_DDL_LIST = Collections.singletonList(mock(DanglingLine.class));
    private static final Supplier<Stream<DanglingLine>> MOCK_DDL_STREAM = () -> MOCK_DDL_LIST.stream();

    private static final List<Switch> MOCK_SW_LIST = Collections.singletonList(mock(Switch.class));
    private static final Supplier<Stream<Switch>> MOCK_SW_STREAM = () -> MOCK_SW_LIST.stream();

    private static final List<ShuntCompensator> MOCK_SHUNT_LIST = Collections.singletonList(mock(ShuntCompensator.class));
    private static final Supplier<Stream<ShuntCompensator>> MOCK_SHUNT_STREAM = () -> MOCK_SHUNT_LIST.stream();

    private static final List<StaticVarCompensator> MOCK_SVC_LIST = Collections.singletonList(mock(StaticVarCompensator.class));
    private static final Supplier<Stream<StaticVarCompensator>> MOCK_SVC_STREAM = () -> MOCK_SVC_LIST.stream();

    private static final List<LccConverterStation> MOCK_LCC_LIST = Collections.singletonList(mock(LccConverterStation.class));
    private static final Supplier<Stream<LccConverterStation>> MOCK_LCC_STREAM = () -> MOCK_LCC_LIST.stream();

    private static final List<VscConverterStation> MOCK_VSC_LIST = Collections.singletonList(mock(VscConverterStation.class));
    private static final Supplier<Stream<VscConverterStation>> MOCK_VSC_STREAM = () -> MOCK_VSC_LIST.stream();

    private static final List<Substation> MOCK_SUB_LIST = Collections.singletonList(mock(Substation.class));
    private static final Supplier<Stream<Substation>> MOCK_SUB_STREAM = () -> MOCK_SUB_LIST.stream();

    private static final List<VoltageLevel> MOCK_VL_LIST = Collections.singletonList(mock(VoltageLevel.class));
    private static final Supplier<Stream<VoltageLevel>> MOCK_VL_STREAM = () -> MOCK_VL_LIST.stream();

    private static final List<Branch> MOCK_BRANCH_LIST = Collections.singletonList(mock(TwoWindingsTransformer.class));
    private static final Supplier<Stream<Branch>> MOCK_BRANCH_STREAM = () -> MOCK_BRANCH_LIST.stream();

    @Test
    public void testBus() {
        Bus delegate = mock(Bus.class);
        Bus sut = new ImmutableBus(delegate, new ImmutableCacheIndex(mock(Network.class)));

        when(delegate.getP()).thenReturn(42.0);
        assertEquals(42.0, sut.getP(), 0.0);
        verify(delegate, ONCE).getP();
        when(delegate.getQ()).thenReturn(42.1);
        assertEquals(42.1, sut.getQ(), 0.0);
        verify(delegate, ONCE).getQ();

        when(delegate.isInMainConnectedComponent()).thenReturn(false);
        assertFalse(sut.isInMainConnectedComponent());
        verify(delegate, ONCE).isInMainConnectedComponent();
        when(delegate.isInMainSynchronousComponent()).thenReturn(true);
        assertTrue(sut.isInMainSynchronousComponent());
        verify(delegate, ONCE).isInMainSynchronousComponent();
        when(delegate.getConnectedTerminalCount()).thenReturn(3);
        assertEquals(3, sut.getConnectedTerminalCount());

        Component component = mock(Component.class);
        when(delegate.getConnectedComponent()).thenReturn(component);
        assertTrue(sut.getConnectedComponent() instanceof ImmutableComponent);
        when(delegate.getSynchronousComponent()).thenReturn(component);
        assertSame(sut.getConnectedComponent(), sut.getSynchronousComponent());

        when(delegate.getLines()).thenReturn(MOCK_LINE_LIST);
        when(delegate.getLineStream()).thenReturn(MOCK_LINE_STREAM.get());
        Iterator<Line> iterator = sut.getLines().iterator();
        assertTrue(iterator.next().isTieLine());
        assertFalse(iterator.next().isTieLine());

        when(delegate.getTwoWindingsTransformers()).thenReturn(MOCK_2WT_LIST);
        when(delegate.getTwoWindingsTransformerStream()).thenReturn(MOCK_2WT_STREAM.get());
        assertElementType(ImmutableTwoWindingsTransformer.class, sut.getTwoWindingsTransformers(), sut.getTwoWindingsTransformerStream());

        when(delegate.getThreeWindingsTransformers()).thenReturn(MOCK_3WT_LIST);
        when(delegate.getThreeWindingsTransformerStream()).thenReturn(MOCK_3WT_STREAM.get());
        assertElementType(ImmutableThreeWindingsTransformer.class, sut.getThreeWindingsTransformers(), sut.getThreeWindingsTransformerStream());

        when(delegate.getGenerators()).thenReturn(MOCK_GENERATOR_LIST);
        when(delegate.getGeneratorStream()).thenReturn(MOCK_GENERATOR_STREAM.get());
        assertElementType(ImmutableGenerator.class, sut.getGenerators(), sut.getGeneratorStream());

        when(delegate.getLoads()).thenReturn(MOCK_LOAD_LIST);
        when(delegate.getLoadStream()).thenReturn(MOCK_LOAD_STREAM.get());
        assertElementType(ImmutableLoad.class, sut.getLoads(), sut.getLoadStream());

        when(delegate.getShuntCompensators()).thenReturn(MOCK_SHUNT_LIST);
        when(delegate.getShuntCompensatorStream()).thenReturn(MOCK_SHUNT_STREAM.get());
        assertElementType(ImmutableShuntCompensator.class, sut.getShuntCompensators(), sut.getShuntCompensatorStream());

        when(delegate.getDanglingLines()).thenReturn(MOCK_DDL_LIST);
        when(delegate.getDanglingLineStream()).thenReturn(MOCK_DDL_STREAM.get());
        assertElementType(ImmutableDanglingLine.class, sut.getDanglingLines(), sut.getDanglingLineStream());

        when(delegate.getStaticVarCompensators()).thenReturn(MOCK_SVC_LIST);
        when(delegate.getStaticVarCompensatorStream()).thenReturn(MOCK_SVC_STREAM.get());
        assertElementType(ImmutableStaticVarCompensator.class, sut.getStaticVarCompensators(), sut.getStaticVarCompensatorStream());

        when(delegate.getLccConverterStations()).thenReturn(MOCK_LCC_LIST);
        when(delegate.getLccConverterStationStream()).thenReturn(MOCK_LCC_STREAM.get());
        assertElementType(ImmutableLccConverterStation.class, sut.getLccConverterStations(), sut.getLccConverterStationStream());

        when(delegate.getVscConverterStations()).thenReturn(MOCK_VSC_LIST);
        when(delegate.getVscConverterStationStream()).thenReturn(MOCK_VSC_STREAM.get());
        assertElementType(ImmutableVscConverterStation.class, sut.getVscConverterStations(), sut.getVscConverterStationStream());
    }

    @Test
    public void testComponent() {
        Component delegate = mock(Component.class);
        Component sut = new ImmutableComponent(delegate, new ImmutableCacheIndex(mock(Network.class)));

        when(delegate.getNum()).thenReturn(3);
        when(delegate.getSize()).thenReturn(33);
        assertEquals(3, sut.getNum());
        assertEquals(33, sut.getSize());
        verify(delegate, ONCE).getNum();
        verify(delegate, ONCE).getSize();

        when(delegate.getBuses()).thenReturn(MOCK_BUS_LIST);
        when(delegate.getBusStream()).thenReturn(MOCK_BUS_STREAM.get());
        assertElementType(ImmutableBus.class, sut.getBuses(), sut.getBusStream());
    }

    @Test
    public void testLine() {
        Line delegate = mock(Line.class);
        Line sut = new ImmutableLine(delegate, new ImmutableCacheIndex(mock(Network.class)));

        when(delegate.isOverloaded()).thenReturn(false);
        when(delegate.isOverloaded(0.9f)).thenReturn(true);
        when(delegate.getOverloadDuration()).thenReturn(99);
        assertFalse(sut.isOverloaded());
        assertTrue(sut.isOverloaded(0.9f));
        assertEquals(99, sut.getOverloadDuration());
        verify(delegate, ONCE).isOverloaded();
        verify(delegate, ONCE).isOverloaded(0.9f);
        verify(delegate, ONCE).getOverloadDuration();

        when(delegate.checkPermanentLimit(Branch.Side.ONE)).thenReturn(false);
        when(delegate.checkPermanentLimit(Branch.Side.ONE, 0.9f)).thenReturn(true);
        assertFalse(sut.checkPermanentLimit(Branch.Side.ONE));
        assertTrue(sut.checkPermanentLimit(Branch.Side.ONE, 0.9f));
        verify(delegate, ONCE).checkPermanentLimit(Branch.Side.ONE);
        verify(delegate, ONCE).checkPermanentLimit(Branch.Side.ONE, 0.9f);
        when(delegate.checkPermanentLimit1()).thenReturn(false);
        assertFalse(sut.checkPermanentLimit1());
        verify(delegate, ONCE).checkPermanentLimit1();
        when(delegate.checkPermanentLimit2()).thenReturn(true);
        assertTrue(sut.checkPermanentLimit2());
        verify(delegate, ONCE).checkPermanentLimit2();

        Branch.Overload ol1 = mock(Branch.Overload.class);
        Branch.Overload ol1r = mock(Branch.Overload.class);
        Branch.Overload ol2 = mock(Branch.Overload.class);
        Branch.Overload ol2r = mock(Branch.Overload.class);
        when(delegate.checkTemporaryLimits(Branch.Side.ONE)).thenReturn(ol1);
        when(delegate.checkTemporaryLimits1()).thenReturn(ol1);
        when(delegate.checkTemporaryLimits1(0.9f)).thenReturn(ol1r);
        when(delegate.checkTemporaryLimits2()).thenReturn(ol2);
        when(delegate.checkTemporaryLimits2(0.9f)).thenReturn(ol2r);
        assertSame(ol1, sut.checkTemporaryLimits(Branch.Side.ONE));
        assertSame(ol1, sut.checkTemporaryLimits1());
        assertSame(ol1r, sut.checkTemporaryLimits1(0.9f));
        assertSame(ol2, sut.checkTemporaryLimits2());
        assertSame(ol2r, sut.checkTemporaryLimits2(0.9f));

    }

    @Test
    public void testVariantManager() {
        VariantManager delegate = mock(VariantManager.class);
        VariantManager sut = new ImmutableVariantManager(delegate);
        sut.setWorkingVariant("set");
        verify(delegate, ONCE).setWorkingVariant("set");
        try {
            sut.cloneVariant("a", "b");
            fail();
        } catch (Exception e) {
            assertEquals("Unmodifiable identifiable", e.getMessage());
        }
        try {
            sut.removeVariant("q");
            fail();
        } catch (Exception e) {
            assertEquals("Unmodifiable identifiable", e.getMessage());
        }
        sut.allowVariantMultiThreadAccess(false);
        verify(delegate, ONCE).allowVariantMultiThreadAccess(false);
        assertFalse(sut.isVariantMultiThreadAccessAllowed());
        verify(delegate, ONCE).isVariantMultiThreadAccessAllowed();
        when(delegate.getWorkingVariantId()).thenReturn("set");
        assertEquals("set", sut.getWorkingVariantId());
        verify(delegate, ONCE).getWorkingVariantId();
        Collection<String> ids = Collections.unmodifiableSet(Sets.newHashSet("vars"));
        when(delegate.getVariantIds()).thenReturn(ids);
        assertEquals(ids, new HashSet<>(sut.getVariantIds()));
        verify(delegate, ONCE).getVariantIds();
    }

    @Test
    public void testNetwork() {
        Network delegate = mock(Network.class);
        Network sut = ImmutableNetwork.of(delegate);

        Set<Country> countries = new HashSet<>();
        countries.add(Country.FR);
        when(delegate.getCountries()).thenReturn(countries);
        assertEquals(countries, sut.getCountries());
        when(delegate.getCountryCount()).thenReturn(1);
        assertEquals(1, sut.getCountryCount());

        when(delegate.getVoltageLevels()).thenReturn(MOCK_VL_LIST);
        when(delegate.getVoltageLevelStream()).thenReturn(MOCK_VL_STREAM.get());
        assertElementType(ImmutableVoltageLevel.class, sut.getVoltageLevels(), sut.getVoltageLevelStream());

        when(delegate.getSubstationCount()).thenReturn(1);
        assertEquals(1, sut.getSubstationCount());
        when(delegate.getSubstations()).thenReturn(MOCK_SUB_LIST);
        when(delegate.getSubstationStream()).thenReturn(MOCK_SUB_STREAM.get());
        assertElementType(ImmutableSubstation.class, sut.getSubstations(), sut.getSubstationStream());

        when(delegate.getLineCount()).thenReturn(2);
        assertEquals(2, sut.getLineCount());
        when(delegate.getLines()).thenReturn(MOCK_LINE_LIST);
        when(delegate.getLineStream()).thenReturn(MOCK_LINE_STREAM.get());
        Iterator<Line> iterator = sut.getLines().iterator();
        assertTrue(iterator.next().isTieLine());
        assertFalse(iterator.next().isTieLine());

        when(delegate.getTwoWindingsTransformerCount()).thenReturn(1);
        assertEquals(1, sut.getTwoWindingsTransformerCount());
        when(delegate.getTwoWindingsTransformers()).thenReturn(MOCK_2WT_LIST);
        when(delegate.getTwoWindingsTransformerStream()).thenReturn(MOCK_2WT_STREAM.get());
        assertElementType(ImmutableTwoWindingsTransformer.class, sut.getTwoWindingsTransformers(), sut.getTwoWindingsTransformerStream());

        when(delegate.getThreeWindingsTransformerCount()).thenReturn(2);
        assertEquals(2, sut.getThreeWindingsTransformerCount());
        when(delegate.getThreeWindingsTransformers()).thenReturn(MOCK_3WT_LIST);
        when(delegate.getThreeWindingsTransformerStream()).thenReturn(MOCK_3WT_STREAM.get());
        assertElementType(ImmutableThreeWindingsTransformer.class, sut.getThreeWindingsTransformers(), sut.getThreeWindingsTransformerStream());

        when(delegate.getGenerators()).thenReturn(MOCK_GENERATOR_LIST);
        when(delegate.getGeneratorStream()).thenReturn(MOCK_GENERATOR_STREAM.get());
        assertElementType(ImmutableGenerator.class, sut.getGenerators(), sut.getGeneratorStream());

        when(delegate.getLoadCount()).thenReturn(1);
        assertEquals(1, sut.getLoadCount());
        when(delegate.getLoads()).thenReturn(MOCK_LOAD_LIST);
        when(delegate.getLoadStream()).thenReturn(MOCK_LOAD_STREAM.get());
        assertElementType(ImmutableLoad.class, sut.getLoads(), sut.getLoadStream());

        when(delegate.getShuntCompensatorCount()).thenReturn(1);
        assertEquals(1, sut.getShuntCompensatorCount());
        when(delegate.getShuntCompensators()).thenReturn(MOCK_SHUNT_LIST);
        when(delegate.getShuntCompensatorStream()).thenReturn(MOCK_SHUNT_STREAM.get());
        assertElementType(ImmutableShuntCompensator.class, sut.getShuntCompensators(), sut.getShuntCompensatorStream());

        when(delegate.getDanglingLineCount()).thenReturn(1);
        assertEquals(1, sut.getDanglingLineCount());
        when(delegate.getDanglingLines()).thenReturn(MOCK_DDL_LIST);
        when(delegate.getDanglingLineStream()).thenReturn(MOCK_DDL_STREAM.get());
        assertElementType(ImmutableDanglingLine.class, sut.getDanglingLines(), sut.getDanglingLineStream());

        when(delegate.getStaticVarCompensators()).thenReturn(MOCK_SVC_LIST);
        when(delegate.getStaticVarCompensatorStream()).thenReturn(MOCK_SVC_STREAM.get());
        assertElementType(ImmutableStaticVarCompensator.class, sut.getStaticVarCompensators(), sut.getStaticVarCompensatorStream());

        when(delegate.getLccConverterStationCount()).thenReturn(1);
        assertEquals(1, sut.getLccConverterStationCount());
        when(delegate.getLccConverterStations()).thenReturn(MOCK_LCC_LIST);
        when(delegate.getLccConverterStationStream()).thenReturn(MOCK_LCC_STREAM.get());
        assertElementType(ImmutableLccConverterStation.class, sut.getLccConverterStations(), sut.getLccConverterStationStream());

        when(delegate.getVscConverterStationCount()).thenReturn(1);
        assertEquals(1, sut.getVscConverterStationCount());
        when(delegate.getVscConverterStations()).thenReturn(MOCK_VSC_LIST);
        when(delegate.getVscConverterStationStream()).thenReturn(MOCK_VSC_STREAM.get());
        assertElementType(ImmutableVscConverterStation.class, sut.getVscConverterStations(), sut.getVscConverterStationStream());

        when(delegate.getSwitches()).thenReturn(MOCK_SW_LIST);
        when(delegate.getSwitchStream()).thenReturn(MOCK_SW_STREAM.get());
        when(delegate.getSwitchCount()).thenReturn(1);
        assertEquals(1, sut.getSwitchCount());
        assertElementType(ImmutableSwitch.class, sut.getSwitches(), sut.getSwitchStream());
    }

    @Test
    public void testVoltageLevel() {
        VoltageLevel delegate = mock(VoltageLevel.class);
        ImmutableCacheIndex immutableCacheIndex = new ImmutableCacheIndex(mock(Network.class));
        VoltageLevel sut = new ImmutableVoltageLevel(delegate, immutableCacheIndex);

        when(delegate.getConnectables(Line.class)).thenReturn(MOCK_LINE_LIST);
        Iterator<Line> iterator = sut.getConnectables(Line.class).iterator();
        assertTrue(iterator.next().isTieLine());
        assertFalse(iterator.next().isTieLine());

        when(delegate.getSwitches()).thenReturn(MOCK_SW_LIST);
        when(delegate.getSwitchCount()).thenReturn(1);
        assertEquals(1, sut.getSwitchCount());
        assertTrue(sut.getSwitches().iterator().next() instanceof ImmutableSwitch);

        when(delegate.getLoadCount()).thenReturn(3);
        assertEquals(3, sut.getLoadCount());
        when(delegate.getLoads()).thenReturn(MOCK_LOAD_LIST);
        when(delegate.getLoadStream()).thenReturn(MOCK_LOAD_STREAM.get());
        assertElementType(ImmutableLoad.class, sut.getLoads(), sut.getLoadStream());

        when(delegate.getGeneratorCount()).thenReturn(1);
        assertEquals(1, sut.getGeneratorCount());
        when(delegate.getGenerators()).thenReturn(MOCK_GENERATOR_LIST);
        when(delegate.getGeneratorStream()).thenReturn(MOCK_GENERATOR_STREAM.get());
        assertElementType(ImmutableGenerator.class, sut.getGenerators(), sut.getGeneratorStream());

        when(delegate.getStaticVarCompensators()).thenReturn(MOCK_SVC_LIST);
        when(delegate.getStaticVarCompensatorStream()).thenReturn(MOCK_SVC_STREAM.get());
        when(delegate.getStaticVarCompensatorCount()).thenReturn(2);
        assertEquals(2, sut.getStaticVarCompensatorCount());
        assertElementType(ImmutableStaticVarCompensator.class, sut.getStaticVarCompensators(), sut.getStaticVarCompensatorStream());

        when(delegate.getShuntCompensatorCount()).thenReturn(1);
        assertEquals(1, sut.getShuntCompensatorCount());
        when(delegate.getShuntCompensators()).thenReturn(MOCK_SHUNT_LIST);
        when(delegate.getShuntCompensatorStream()).thenReturn(MOCK_SHUNT_STREAM.get());
        assertElementType(ImmutableShuntCompensator.class, sut.getShuntCompensators(), sut.getShuntCompensatorStream());

        when(delegate.getDanglingLineCount()).thenReturn(1);
        assertEquals(1, sut.getDanglingLineCount());
        when(delegate.getDanglingLines()).thenReturn(MOCK_DDL_LIST);
        when(delegate.getDanglingLineStream()).thenReturn(MOCK_DDL_STREAM.get());
        assertElementType(ImmutableDanglingLine.class, sut.getDanglingLines(), sut.getDanglingLineStream());

        when(delegate.getLccConverterStationCount()).thenReturn(1);
        assertEquals(1, sut.getLccConverterStationCount());
        when(delegate.getLccConverterStations()).thenReturn(MOCK_LCC_LIST);
        when(delegate.getLccConverterStationStream()).thenReturn(MOCK_LCC_STREAM.get());
        assertElementType(ImmutableLccConverterStation.class, sut.getLccConverterStations(), sut.getLccConverterStationStream());

        when(delegate.getVscConverterStationCount()).thenReturn(1);
        assertEquals(1, sut.getVscConverterStationCount());
        when(delegate.getVscConverterStations()).thenReturn(MOCK_VSC_LIST);
        when(delegate.getVscConverterStationStream()).thenReturn(MOCK_VSC_STREAM.get());
        assertElementType(ImmutableVscConverterStation.class, sut.getVscConverterStations(), sut.getVscConverterStationStream());

        VoltageLevel.BusBreakerView delegateBbv = mock(VoltageLevel.BusBreakerView.class);
        when(delegate.getBusBreakerView()).thenReturn(delegateBbv);
        when(delegateBbv.getBuses()).thenReturn(MOCK_BUS_LIST);
        when(delegateBbv.getBusStream()).thenReturn(MOCK_BUS_STREAM.get());
        assertElementType(ImmutableBus.class, sut.getBusBreakerView().getBuses(), sut.getBusBreakerView().getBusStream());

        Bus b1 = mock(Bus.class);
        Bus b2 = mock(Bus.class);
        ImmutableBus bus1 = (ImmutableBus) immutableCacheIndex.getBus(b1);
        ImmutableBus bus2 = (ImmutableBus) immutableCacheIndex.getBus(b2);

        when(delegateBbv.getSwitches()).thenReturn(MOCK_SW_LIST);
        when(delegateBbv.getSwitchStream()).thenReturn(MOCK_SW_STREAM.get());
        when(delegateBbv.getSwitchCount()).thenReturn(1);
        assertEquals(1, sut.getSwitchCount());
        assertElementType(ImmutableSwitch.class, sut.getBusBreakerView().getSwitches(), sut.getBusBreakerView().getSwitchStream());

        when(delegateBbv.getBus("bid")).thenReturn(b1);
        when(delegateBbv.getBus1("bus1")).thenReturn(b1);
        when(delegateBbv.getBus2("bus2")).thenReturn(b2);
        assertSame(bus1, sut.getBusBreakerView().getBus("bid"));
        assertSame(bus1, sut.getBusBreakerView().getBus1("bus1"));
        assertSame(bus2, sut.getBusBreakerView().getBus2("bus2"));
        verify(delegateBbv, ONCE).getBus("bid");
        verify(delegateBbv, ONCE).getBus1("bus1");
        verify(delegateBbv, ONCE).getBus2("bus2");

        when(delegateBbv.getBuses()).thenReturn(MOCK_BUS_LIST);
        when(delegateBbv.getBusStream()).thenReturn(MOCK_BUS_STREAM.get());
        assertElementType(ImmutableBus.class, sut.getBusBreakerView().getBuses(), sut.getBusBreakerView().getBusStream());

        VoltageLevel.NodeBreakerView delegateNbv = mock(VoltageLevel.NodeBreakerView.class);
        when(sut.getNodeBreakerView()).thenReturn(delegateNbv);
        when(delegateNbv.getNodeCount()).thenReturn(2);
        when(delegateNbv.getNode1("s1")).thenReturn(1);
        when(delegateNbv.getNode2("s2")).thenReturn(2);
        assertEquals(1, sut.getNodeBreakerView().getNode1("s1"));
        assertEquals(2, sut.getNodeBreakerView().getNode2("s2"));

        Terminal t1 = mock(Terminal.class);
        Terminal terminal1 = immutableCacheIndex.getTerminal(t1);
        Terminal t2 = mock(Terminal.class);
        Terminal terminal2 = immutableCacheIndex.getTerminal(t2);
        when(delegateNbv.getTerminal1("s1")).thenReturn(t1);
        when(delegateNbv.getTerminal2("s2")).thenReturn(t2);
        assertSame(terminal1, sut.getNodeBreakerView().getTerminal1("s1"));
        assertSame(terminal2, sut.getNodeBreakerView().getTerminal2("s2"));

        when(delegateNbv.getSwitches()).thenReturn(MOCK_SW_LIST);
        when(delegateNbv.getSwitchStream()).thenReturn(MOCK_SW_STREAM.get());
        when(delegateNbv.getSwitchCount()).thenReturn(1);
        assertEquals(1, sut.getNodeBreakerView().getSwitchCount());
        assertElementType(ImmutableSwitch.class, sut.getNodeBreakerView().getSwitches(), sut.getNodeBreakerView().getSwitchStream());
    }

    @Test
    public void testSubstation() {
        Substation delegate = mock(Substation.class);
        Substation sut = new ImmutableSubstation(delegate, new ImmutableCacheIndex(mock(Network.class)));

        when(delegate.getTwoWindingsTransformerCount()).thenReturn(1);
        assertEquals(1, sut.getTwoWindingsTransformerCount());
        when(delegate.getTwoWindingsTransformers()).thenReturn(MOCK_2WT_LIST);
        when(delegate.getTwoWindingsTransformerStream()).thenReturn(MOCK_2WT_STREAM.get());
        assertElementType(ImmutableTwoWindingsTransformer.class, sut.getTwoWindingsTransformers(), sut.getTwoWindingsTransformerStream());

        when(delegate.getThreeWindingsTransformerCount()).thenReturn(2);
        assertEquals(2, sut.getThreeWindingsTransformerCount());
        when(delegate.getThreeWindingsTransformers()).thenReturn(MOCK_3WT_LIST);
        when(delegate.getThreeWindingsTransformerStream()).thenReturn(MOCK_3WT_STREAM.get());
        assertElementType(ImmutableThreeWindingsTransformer.class, sut.getThreeWindingsTransformers(), sut.getThreeWindingsTransformerStream());

    }


    private static <T extends Identifiable> void assertElementType(Class expectedClazz, Iterable<T> iterable, Stream<T> stream) {
        Iterator<T> iterator = iterable.iterator();
        assertTrue(iterator.hasNext());
        assertEquals(expectedClazz, iterator.next().getClass());
        stream.findAny().ifPresent(e -> assertEquals(expectedClazz, e.getClass()));
    }

}
