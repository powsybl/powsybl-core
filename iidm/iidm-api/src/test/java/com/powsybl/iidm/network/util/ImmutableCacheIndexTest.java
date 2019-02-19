/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.*;
import org.junit.Test;

import static com.powsybl.iidm.network.ConnectableType.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.junit.Assert.*;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class ImmutableCacheIndexTest {

    @Test
    public void test() {
        ImmutableCacheIndex cache = new ImmutableCacheIndex(mock(Network.class));
        // mock all types of connectable
        BusbarSection busbarSection = mock(BusbarSection.class);
        when(busbarSection.getType()).thenReturn(BUSBAR_SECTION);
        Line line = mock(Line.class);
        when(line.getType()).thenReturn(LINE);
        TwoWindingsTransformer twt2 = mock(TwoWindingsTransformer.class);
        when(twt2.getType()).thenReturn(TWO_WINDINGS_TRANSFORMER);
        ThreeWindingsTransformer twt3 = mock(ThreeWindingsTransformer.class);
        when(twt3.getType()).thenReturn(THREE_WINDINGS_TRANSFORMER);
        Generator gen = mock(Generator.class);
        when(gen.getType()).thenReturn(GENERATOR);
        Load load = mock(Load.class);
        when(load.getType()).thenReturn(LOAD);
        ShuntCompensator shunt = mock(ShuntCompensator.class);
        when(shunt.getType()).thenReturn(SHUNT_COMPENSATOR);
        DanglingLine dll = mock(DanglingLine.class);
        when(dll.getType()).thenReturn(DANGLING_LINE);
        StaticVarCompensator svc = mock(StaticVarCompensator.class);
        when(svc.getType()).thenReturn(STATIC_VAR_COMPENSATOR);
        HvdcConverterStation lcc = mock(LccConverterStation.class);
        when(lcc.getType()).thenReturn(HVDC_CONVERTER_STATION);
        when(lcc.getHvdcType()).thenReturn(HvdcConverterStation.HvdcType.LCC);

        assertNull(cache.getConnectable(null));

        Connectable cacheBs = cache.getConnectable(busbarSection);
        assertSame(busbarSection, cacheBs);

        Line cacheLine = cache.getLine(line);
        assertTrue(cacheLine instanceof ImmutableLine);
        assertSame(cacheLine, cache.getConnectable(line));
        assertSame(cacheLine, cache.getBranch(line));

        TwoWindingsTransformer cache2wt = cache.getTwoWindingsTransformer(twt2);
        assertTrue(cache2wt instanceof ImmutableTwoWindingsTransformer);
        assertSame(cache2wt, cache.getConnectable(twt2));
        assertSame(cache2wt, cache.getBranch(twt2));

        ThreeWindingsTransformer cache3wt = cache.getThreeWindingsTransformer(twt3);
        assertTrue(cache3wt instanceof ImmutableThreeWindingsTransformer);
        assertSame(cache3wt, cache.getConnectable(twt3));

        Generator cacheGen = cache.getGenerator(gen);
        assertTrue(cacheGen instanceof ImmutableGenerator);
        assertSame(cacheGen, cache.getConnectable(gen));

        Load cacheLoad = cache.getLoad(load);
        assertTrue(cacheLoad instanceof ImmutableLoad);
        assertSame(cacheLoad, cache.getConnectable(load));

        ShuntCompensator cacheShunt = cache.getShuntCompensator(shunt);
        assertTrue(cacheShunt instanceof ImmutableShuntCompensator);
        assertSame(cacheShunt, cache.getConnectable(shunt));

        DanglingLine cacheDll = cache.getDanglingLine(dll);
        assertTrue(cacheDll instanceof ImmutableDanglingLine);
        assertSame(cacheDll, cache.getConnectable(dll));

        StaticVarCompensator cacheSvc = cache.getStaticVarCompensator(svc);
        assertTrue(cacheSvc instanceof ImmutableStaticVarCompensator);
        assertSame(cacheSvc, cache.getConnectable(svc));

        HvdcConverterStation cacheLcc = cache.getHvdcConverterStation(lcc);
        assertTrue(cacheLcc instanceof ImmutableLccConverterStation);
        assertSame(cacheLcc, cache.getConnectable(lcc));

        assertNull(cache.getHvdcConverterStation(null));
    }
}
