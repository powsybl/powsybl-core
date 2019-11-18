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

import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class LoadAdapterTest {
    private MergingView mergingView;

    @Before
    public void setup() {
        mergingView = MergingView.create("LoadAdapterTest", "iidm");
        mergingView.merge(FictitiousSwitchFactory.create());
    }

    @Test
    public void testSetterGetter() {
        final Load load = mergingView.getLoad("CE");
        assertNotNull(load);
        assertTrue(load instanceof LoadAdapter);
        assertSame(mergingView, load.getNetwork());

        // Not implemented yet !
        TestUtil.notImplemented(load::getTerminal);
        TestUtil.notImplemented(load::getType);
        TestUtil.notImplemented(load::getTerminals);
        TestUtil.notImplemented(load::remove);
        TestUtil.notImplemented(load::getLoadType);
        TestUtil.notImplemented(() -> load.setLoadType(null));
        TestUtil.notImplemented(load::getP0);
        TestUtil.notImplemented(() -> load.setP0(0.0d));
        TestUtil.notImplemented(load::getQ0);
        TestUtil.notImplemented(() -> load.setQ0(0.0d));
    }
}
