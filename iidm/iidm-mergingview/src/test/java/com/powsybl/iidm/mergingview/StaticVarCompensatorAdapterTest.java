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

import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.test.SvcTestCaseFactory;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class StaticVarCompensatorAdapterTest {
    private MergingView mergingView;

    @Before
    public void setup() {
        mergingView = MergingView.create("StaticVarCompensatorAdapterTest", "iidm");
        mergingView.merge(SvcTestCaseFactory.create());
    }

    @Test
    public void testSetterGetter() {
        final StaticVarCompensator svc = mergingView.getStaticVarCompensator("SVC2");
        assertNotNull(svc);
        assertTrue(svc instanceof StaticVarCompensatorAdapter);
        assertSame(mergingView, svc.getNetwork());

        // Not implemented yet !
        TestUtil.notImplemented(svc::getTerminal);
        TestUtil.notImplemented(svc::getType);
        TestUtil.notImplemented(svc::getTerminals);
        TestUtil.notImplemented(svc::remove);
        TestUtil.notImplemented(svc::getBmin);
        TestUtil.notImplemented(() -> svc.setBmin(0.0d));
        TestUtil.notImplemented(svc::getBmax);
        TestUtil.notImplemented(() -> svc.setBmax(0.0d));
        TestUtil.notImplemented(svc::getVoltageSetPoint);
        TestUtil.notImplemented(() -> svc.setVoltageSetPoint(0.0d));
        TestUtil.notImplemented(svc::getReactivePowerSetPoint);
        TestUtil.notImplemented(() -> svc.setReactivePowerSetPoint(0.0d));
        TestUtil.notImplemented(svc::getRegulationMode);
        TestUtil.notImplemented(() -> svc.setRegulationMode(null));
    }
}
