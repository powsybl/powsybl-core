/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class ThreeWindingsTransformerAdapterTest {
    private MergingView mergingView;

    @Before
    public void setup() {
        mergingView = MergingView.create("ThreeWindingsTransformerAdapterTest", "iidm");
        mergingView.merge(ThreeWindingsTransformerNetworkFactory.create());
    }

    @Test
    public void testSetterGetter() {
        final ThreeWindingsTransformer twt = mergingView.getThreeWindingsTransformer("3WT");
        assertNotNull(twt);
        assertTrue(twt instanceof ThreeWindingsTransformerAdapter);
        assertSame(mergingView, twt.getNetwork());

        // Not implemented yet !
        TestUtil.notImplemented(twt::getType);
        TestUtil.notImplemented(twt::getTerminals);
        TestUtil.notImplemented(twt::remove);
        TestUtil.notImplemented(() -> twt.getTerminal(null));
        TestUtil.notImplemented(() -> twt.getSide(null));
        TestUtil.notImplemented(twt::getSubstation);
        TestUtil.notImplemented(twt::getLeg1);
        TestUtil.notImplemented(twt::getLeg2);
        TestUtil.notImplemented(twt::getLeg3);
    }
}
