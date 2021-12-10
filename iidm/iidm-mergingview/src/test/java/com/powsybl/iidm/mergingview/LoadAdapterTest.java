/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.LoadType;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class LoadAdapterTest {
    private MergingView mergingView;

    @Before
    public void setup() {
        mergingView = MergingView.create("LoadAdapterTest", "iidm");
    }

    @Test
    public void testSetterGetter() {
        Network networkRef = FictitiousSwitchFactory.create();
        mergingView.merge(networkRef);

        final Load loadExpected = networkRef.getLoad("CE");
        final Load loadActual = mergingView.getLoad("CE");
        assertNotNull(loadActual);
        assertTrue(loadActual instanceof LoadAdapter);
        assertSame(mergingView, loadActual.getNetwork());

        assertEquals(loadExpected.getType(), loadActual.getType());
        assertTrue(loadActual.getTerminal() instanceof TerminalAdapter);
        loadActual.getTerminals().forEach(t -> {
            assertTrue(t instanceof TerminalAdapter);
            assertNotNull(t);
        });

        assertEquals(loadExpected.getLoadType(), loadActual.getLoadType());
        LoadType loadType = LoadType.UNDEFINED;
        assertTrue(loadActual.setLoadType(loadType) instanceof LoadAdapter);
        assertEquals(loadType, loadActual.getLoadType());
        double p0 = loadExpected.getP0();
        assertEquals(p0, loadActual.getP0(), 0.0d);
        assertTrue(loadActual.setP0(++p0) instanceof LoadAdapter);
        assertEquals(p0, loadActual.getP0(), 0.0d);
        double q0 = loadExpected.getQ0();
        assertEquals(q0, loadActual.getQ0(), 0.0d);
        assertTrue(loadActual.setQ0(++q0) instanceof LoadAdapter);
        assertEquals(q0, loadActual.getQ0(), 0.0d);

        // Not implemented yet !
        TestUtil.notImplemented(loadActual::remove);
    }
}
