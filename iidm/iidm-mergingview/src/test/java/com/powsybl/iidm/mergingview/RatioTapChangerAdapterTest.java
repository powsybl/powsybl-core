/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class RatioTapChangerAdapterTest {
    private MergingView mergingView;

    @Before
    public void setup() {
        mergingView = MergingView.create("RatioTapChangerAdapterTest", "iidm");
        mergingView.merge(EurostagTutorialExample1Factory.create());
    }

    @Test
    public void testSetterGetter() {
        final TwoWindingsTransformer twt = mergingView.getTwoWindingsTransformer("NHV2_NLOAD");
        final RatioTapChanger ptc = twt.getRatioTapChanger();
        assertNotNull(ptc);
        assertTrue(ptc instanceof RatioTapChangerAdapter);

        // Not implemented yet !
        TestUtil.notImplemented(ptc::getLowTapPosition);
        TestUtil.notImplemented(() -> ptc.setLowTapPosition(0));
        TestUtil.notImplemented(ptc::getHighTapPosition);
        TestUtil.notImplemented(ptc::getTapPosition);
        TestUtil.notImplemented(() -> ptc.setTapPosition(0));
        TestUtil.notImplemented(ptc::getStepCount);
        TestUtil.notImplemented(() -> ptc.getStep(0));
        TestUtil.notImplemented(ptc::getCurrentStep);
        TestUtil.notImplemented(ptc::isRegulating);
        TestUtil.notImplemented(() -> ptc.setRegulating(false));
        TestUtil.notImplemented(ptc::getRegulationTerminal);
        TestUtil.notImplemented(() -> ptc.setRegulationTerminal(null));
        TestUtil.notImplemented(ptc::remove);
        TestUtil.notImplemented(ptc::getTargetV);
        TestUtil.notImplemented(() -> ptc.setTargetV(0.0d));
        TestUtil.notImplemented(ptc::hasLoadTapChangingCapabilities);
        TestUtil.notImplemented(() -> ptc.setLoadTapChangingCapabilities(false));
    }
}
