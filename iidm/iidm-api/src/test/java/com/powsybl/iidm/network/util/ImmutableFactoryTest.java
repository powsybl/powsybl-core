/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.*;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class ImmutableFactoryTest {

    @Test
    public void test() {
        Line line = mock(Line.class);
        when(line.getType()).thenReturn(ConnectableType.LINE);
        when(line.isTieLine()).thenReturn(false);
        assertTrue(ImmutableFactory.ofNullableConnectable(line) instanceof ImmutableLine);

        TieLine tl = mock(TieLine.class);
        when(tl.getType()).thenReturn(ConnectableType.LINE);
        when(tl.isTieLine()).thenReturn(true);
        assertTrue(ImmutableFactory.ofNullableConnectable(tl) instanceof ImmutableTieLine);

        Generator g = mock(Generator.class);
        when(g.getType()).thenReturn(ConnectableType.GENERATOR);
        assertTrue(ImmutableFactory.ofNullableConnectable(g) instanceof ImmutableGenerator);

        TwoWindingsTransformer twt = mock(TwoWindingsTransformer.class);
        when(twt.getType()).thenReturn(ConnectableType.TWO_WINDINGS_TRANSFORMER);
        assertTrue(ImmutableFactory.ofNullableConnectable(twt) instanceof ImmutableTwoWindingsTransformer);
        ThreeWindingsTransformer twt3 = mock(ThreeWindingsTransformer.class);
        when(twt3.getType()).thenReturn(ConnectableType.THREE_WINDINGS_TRANSFORMER);
        assertTrue(ImmutableFactory.ofNullableConnectable(twt3) instanceof ImmutableThreeWindingsTransformer);
    }
}
