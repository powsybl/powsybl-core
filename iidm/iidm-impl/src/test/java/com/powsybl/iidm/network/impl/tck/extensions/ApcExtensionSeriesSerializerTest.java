/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.tck.extensions;

import com.powsybl.commons.extensions.ExtensionSeriesBuilder;
import com.powsybl.commons.extensions.ExtensionSeriesSerializer;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.impl.extensions.ActivePowerControlImpl;
import com.powsybl.iidm.network.impl.extensions.ApcExtensionSeriesSerializer;
import org.junit.Test;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class ApcExtensionSeriesSerializerTest {

    @Test
    public void test() {
        Generator g1 = mock(Generator.class);
        Generator g2 = mock(Generator.class);
        ActivePowerControlImpl<Generator> apc = new ActivePowerControlImpl<>(g2, true, 0.1f);
        when(g2.getExtension(eq(ActivePowerControlImpl.class))).thenReturn(apc);

        MockSeriesBuilder mockSeriesBuilder = new MockSeriesBuilder(g1, g2);

        ApcExtensionSeriesSerializer sut = new ApcExtensionSeriesSerializer();
        sut.serialize(mockSeriesBuilder);
        sut.deserialize(g2, "apc_droop", 0.3d);
        assertEquals((float) 0.3d, apc.getDroop(), 0.0);

        assertEquals(ExtensionSeriesSerializer.BOOLEAN_SERIES_TYPE, (int) sut.getTypeMap().get("apc_participate"));
        assertEquals(ExtensionSeriesSerializer.DOUBLE_SERIES_TYPE, (int) sut.getTypeMap().get("apc_droop"));
    }

    static class MockSeriesBuilder implements ExtensionSeriesBuilder<MockSeriesBuilder, Generator> {

        private final Generator g1;
        private final Generator g2;

        MockSeriesBuilder(Generator g1, Generator g2) {
            this.g1 = g1;
            this.g2 = g2;
        }

        @Override
        public MockSeriesBuilder addBooleanSeries(String seriesName, Predicate<Generator> booleanGetter) {
            assertFalse(booleanGetter.test(g1));
            assertTrue(booleanGetter.test(g2));
            return this;
        }

        @Override
        public MockSeriesBuilder addDoubleSeries(String seriesName, ToDoubleFunction<Generator> doubleGetter) {
            assertEquals(Double.NaN, doubleGetter.applyAsDouble(g1), 0.01);
            assertEquals(0.1d, doubleGetter.applyAsDouble(g2), 0.01);
            return this;
        }

        @Override
        public MockSeriesBuilder addStringSeries(String seriesName, Function<Generator, String> stringGetter) {
            fail();
            return this;
        }

        @Override
        public <U> MockSeriesBuilder addIntSeries(String seriesName, Function<Generator, U> objectGetter, ToIntFunction<U> intGetter, int undefinedValue) {
            fail();
            return this;
        }
    }
}
