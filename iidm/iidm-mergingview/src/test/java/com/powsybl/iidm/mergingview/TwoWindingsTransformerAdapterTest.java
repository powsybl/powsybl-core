/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.powsybl.iidm.network.Branch.Side;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class TwoWindingsTransformerAdapterTest {
    private MergingView mergingView;

    @Before
    public void setup() {
        mergingView = MergingView.create("TwoWindingsTransformerAdapterTest", "iidm");
        mergingView.merge(EurostagTutorialExample1Factory.create());
    }

    @Test
    public void testSetterGetter() {
        final TwoWindingsTransformer twt = mergingView.getTwoWindingsTransformer("NGEN_NHV1");
        assertNotNull(twt);
        assertSame(twt, mergingView.getBranch("NGEN_NHV1"));
        assertTrue(twt instanceof TwoWindingsTransformerAdapter);
        assertSame(mergingView, twt.getNetwork());

        assertNull(twt.getPhaseTapChanger());
        assertNull(twt.getRatioTapChanger());

        // Not implemented yet !
        TestUtil.notImplemented(twt::getTerminal1);
        TestUtil.notImplemented(twt::getTerminal2);
        TestUtil.notImplemented(() -> twt.getTerminal((Side) null));
        TestUtil.notImplemented(() -> twt.getTerminal(""));
        TestUtil.notImplemented(() -> twt.getSide((Terminal) null));
        TestUtil.notImplemented(() -> twt.getCurrentLimits(null));
        TestUtil.notImplemented(twt::getCurrentLimits1);
        TestUtil.notImplemented(twt::newCurrentLimits1);
        TestUtil.notImplemented(twt::getCurrentLimits2);
        TestUtil.notImplemented(twt::newCurrentLimits2);
        TestUtil.notImplemented(twt::isOverloaded);
        TestUtil.notImplemented(() -> twt.isOverloaded(0.0f));
        TestUtil.notImplemented(twt::getOverloadDuration);
        TestUtil.notImplemented(() -> twt.checkPermanentLimit((Side) null, 0.0f));
        TestUtil.notImplemented(() -> twt.checkPermanentLimit((Side) null));
        TestUtil.notImplemented(() -> twt.checkPermanentLimit1(0.0f));
        TestUtil.notImplemented(twt::checkPermanentLimit1);
        TestUtil.notImplemented(() -> twt.checkPermanentLimit2(0.0f));
        TestUtil.notImplemented(twt::checkPermanentLimit2);
        TestUtil.notImplemented(() -> twt.checkTemporaryLimits((Side) null, 0.0f));
        TestUtil.notImplemented(() -> twt.checkTemporaryLimits((Side) null));
        TestUtil.notImplemented(() -> twt.checkTemporaryLimits1(0.0f));
        TestUtil.notImplemented(twt::checkTemporaryLimits1);
        TestUtil.notImplemented(() -> twt.checkTemporaryLimits2(0.0f));
        TestUtil.notImplemented(twt::checkTemporaryLimits2);
        TestUtil.notImplemented(twt::getType);
        TestUtil.notImplemented(twt::getTerminals);
        TestUtil.notImplemented(twt::remove);
        TestUtil.notImplemented(twt::newRatioTapChanger);
        TestUtil.notImplemented(twt::newPhaseTapChanger);
        TestUtil.notImplemented(twt::getSubstation);
        TestUtil.notImplemented(twt::getR);
        TestUtil.notImplemented(() -> twt.setR(0.0d));
        TestUtil.notImplemented(twt::getX);
        TestUtil.notImplemented(() -> twt.setX(0.0d));
        TestUtil.notImplemented(twt::getG);
        TestUtil.notImplemented(() -> twt.setG(0.0d));
        TestUtil.notImplemented(twt::getB);
        TestUtil.notImplemented(() -> twt.setB(0.0d));
        TestUtil.notImplemented(twt::getRatedU1);
        TestUtil.notImplemented(() -> twt.setRatedU1(0.0d));
        TestUtil.notImplemented(twt::getRatedU2);
        TestUtil.notImplemented(() -> twt.setRatedU2(0.0d));
    }
}
