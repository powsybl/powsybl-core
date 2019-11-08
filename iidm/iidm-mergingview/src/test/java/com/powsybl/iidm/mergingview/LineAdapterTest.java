/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import static org.junit.Assert.assertFalse;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class LineAdapterTest {

    private MergingView network;

    @Before
    public void setUp() {
        network = MergingView.create("LineAdapterTest", "iidm");
        network.merge(BatteryNetworkFactory.create());
    }

    @Test
    public void testSetterGetter() {
        final Line acLine = network.getLine("NHV1_NHV2_1");

        assertFalse(acLine.isTieLine());

        // Not implemented yet !
        TestUtil.notImplemented(acLine::getTerminal1);
        TestUtil.notImplemented(acLine::getTerminal2);
        TestUtil.notImplemented(() -> acLine.getTerminal((Branch.Side) null));
        TestUtil.notImplemented(() -> acLine.getTerminal(""));
        TestUtil.notImplemented(() -> acLine.getSide((Terminal) null));
        TestUtil.notImplemented(() -> acLine.getCurrentLimits((Branch.Side) null));
        TestUtil.notImplemented(acLine::getCurrentLimits1);
        TestUtil.notImplemented(acLine::newCurrentLimits1);
        TestUtil.notImplemented(acLine::getCurrentLimits2);
        TestUtil.notImplemented(acLine::newCurrentLimits2);
        TestUtil.notImplemented(acLine::isOverloaded);
        TestUtil.notImplemented(() -> acLine.isOverloaded(0.0f));
        TestUtil.notImplemented(acLine::getOverloadDuration);
        TestUtil.notImplemented(() -> acLine.checkPermanentLimit((Branch.Side) null, 0.0f));
        TestUtil.notImplemented(() -> acLine.checkPermanentLimit((Branch.Side) null));
        TestUtil.notImplemented(() -> acLine.checkPermanentLimit1(0.0f));
        TestUtil.notImplemented(acLine::checkPermanentLimit1);
        TestUtil.notImplemented(() -> acLine.checkPermanentLimit2(0.0f));
        TestUtil.notImplemented(acLine::checkPermanentLimit2);
        TestUtil.notImplemented(() -> acLine.checkTemporaryLimits((Branch.Side) null, 0.0f));
        TestUtil.notImplemented(() -> acLine.checkTemporaryLimits((Branch.Side) null));
        TestUtil.notImplemented(() -> acLine.checkTemporaryLimits1(0.0f));
        TestUtil.notImplemented(acLine::checkTemporaryLimits1);
        TestUtil.notImplemented(() -> acLine.checkTemporaryLimits2(0.0f));
        TestUtil.notImplemented(acLine::checkTemporaryLimits2);
        TestUtil.notImplemented(acLine::getType);
        TestUtil.notImplemented(acLine::getTerminals);
        TestUtil.notImplemented(acLine::remove);
        TestUtil.notImplemented(acLine::getR);
        TestUtil.notImplemented(() -> acLine.setR(0.0d));
        TestUtil.notImplemented(acLine::getX);
        TestUtil.notImplemented(() -> acLine.setX(0.0d));
        TestUtil.notImplemented(acLine::getG1);
        TestUtil.notImplemented(() -> acLine.setG1(0.0d));
        TestUtil.notImplemented(acLine::getG2);
        TestUtil.notImplemented(() -> acLine.setG2(0.0d));
        TestUtil.notImplemented(acLine::getB1);
        TestUtil.notImplemented(() -> acLine.setB1(0.0d));
        TestUtil.notImplemented(acLine::getB2);
        TestUtil.notImplemented(() -> acLine.setB2(0.0d));
    }
}
