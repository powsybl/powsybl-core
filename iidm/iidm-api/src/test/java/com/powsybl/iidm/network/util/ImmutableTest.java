/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.*;
import org.junit.Test;
import org.mockito.verification.VerificationMode;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This test is to guarant that objects acquired by getter in immutable ojbect are always be wrapped as immutbale
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class ImmutableTest {

    private static final VerificationMode ONCE = times(1);

    private static final List<Bus> MOCK_BUS_LIST = Arrays.asList(mock(Bus.class), mock(Bus.class));
    private static final Supplier<Stream<Bus>> MOCK_BUS_STREAM = () -> MOCK_BUS_LIST.stream();

    private static final TieLine MOCK_TIELINE = mock(TieLine.class);

    static {
        when(MOCK_TIELINE.isTieLine()).thenReturn(true);
    }

    private static final List<Line> MOCK_LINE_LIST = Arrays.asList(MOCK_TIELINE, mock(Line.class));
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
        Bus sut = ImmutableBus.ofNullable(delegate);
        assertSame(sut, ImmutableBus.ofNullable(delegate));

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
        Component sut = ImmutableComponent.ofNullable(delegate);

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
        Line sut = ImmutableFactory.ofNullableLine(delegate);

        when(delegate.isOverloaded()).thenReturn(false);
        when(delegate.isOverloaded(0.9f)).thenReturn(true);
        when(delegate.getOverloadDuration()).thenReturn(99);
        assertFalse(sut.isOverloaded());
        assertTrue(sut.isOverloaded(0.9f));
        assertEquals(99, sut.getOverloadDuration());
        verify(delegate, ONCE).isOverloaded();
        verify(delegate, ONCE).isOverloaded(0.9f);
        verify(delegate, ONCE).getOverloadDuration();

        when(delegate.checkPermanentLimit1()).thenReturn(false);
        assertFalse(sut.checkPermanentLimit1());
        verify(delegate, ONCE).checkPermanentLimit1();
    }

    @Test
    public void testVariantManager() {
        VariantManager delegate = mock(VariantManager.class);
        VariantManager sut = ImmutableVariantManager.of(delegate);
        sut.setWorkingVariant("set");
        verify(delegate, ONCE).setWorkingVariant("set");
        try {
            sut.cloneVariant("a", "b");
            fail();
        } catch (Exception excepted) {
            assertEquals(excepted.getCause(), ImmutableNetwork.UNMODIFIABLE_EXCEPTION.getCause());
        }
        try {
            sut.removeVariant("q");
            fail();
        } catch (Exception excepted) {
            assertEquals(excepted.getCause(), ImmutableNetwork.UNMODIFIABLE_EXCEPTION.getCause());
        }
        // TODO
//        try {
//            sut.allowVariantMultiThreadAccess(false);
//            fail();
//        } catch (Exception excepted) {
//            assertEquals(excepted.getCause(), ImmutableNetwork.UNMODIFIABLE_EXCEPTION.getCause());
//        }
    }

    @Test
    public void testNetwork() {
        Network delegate = mock(Network.class);
        Network sut = ImmutableNetwork.of(delegate);

        when(delegate.getVoltageLevels()).thenReturn(MOCK_VL_LIST);
        when(delegate.getVoltageLevelStream()).thenReturn(MOCK_VL_STREAM.get());
        assertElementType(ImmutableVoltageLevel.class, sut.getVoltageLevels(), sut.getVoltageLevelStream());

        when(delegate.getSubstations()).thenReturn(MOCK_SUB_LIST);
        when(delegate.getSubstationStream()).thenReturn(MOCK_SUB_STREAM.get());
        assertElementType(ImmutableSubstation.class, sut.getSubstations(), sut.getSubstationStream());

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

    private static <T extends Identifiable> void assertElementType(Class expectedClazz, Iterable<T> iterable, Stream<T> stream) {
        Iterator<T> iterator = iterable.iterator();
        assertTrue(iterator.hasNext());
        assertEquals(expectedClazz, iterator.next().getClass());
        stream.findAny().ifPresent(e -> assertEquals(expectedClazz, e.getClass()));
    }

}
