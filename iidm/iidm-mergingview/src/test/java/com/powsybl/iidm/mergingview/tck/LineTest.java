/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview.tck;

import com.powsybl.iidm.mergingview.MergingView;
import com.powsybl.iidm.mergingview.TestUtil;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.tck.AbstractLineTest;
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;
import org.junit.Test;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class LineTest extends AbstractLineTest {

    @Override
    protected Network createNetwork() {
        Network network = MergingView.create("merged", "test");
        network.merge(NoEquipmentNetworkFactory.create());
        return network;
    }

    @Test
    public void testChangesNotification() {
        TestUtil.notImplemented(super::testChangesNotification);
    }

    @Test
    public void testRemoveAcLine() {
        TestUtil.notImplemented(super::testRemoveAcLine);
    }

    @Test
    public void testTieLineAdder() {
        TestUtil.notImplemented(super::testTieLineAdder);
    }

    @Test
    public void invalidHalfLineCharacteristicsR() {
        TestUtil.notImplemented(() -> createTieLineWithHalfline2ByDefault(INVALID, INVALID, INVALID, Double.NaN, 2.0,
                3.0, 3.5, 4.0, 4.5, 5.0, 6.0, "code"));
    }

    @Test
    public void invalidHalfLineCharacteristicsX() {
        TestUtil.notImplemented(() -> createTieLineWithHalfline2ByDefault(INVALID, INVALID, INVALID, 1.0, Double.NaN,
                3.0, 3.5, 4.0, 4.5, 5.0, 6.0, "code"));
    }

    @Test
    public void invalidHalfLineCharacteristicsG1() {
        TestUtil.notImplemented(() -> createTieLineWithHalfline2ByDefault(INVALID, INVALID, INVALID, 1.0, 2.0,
                Double.NaN, 3.5, 4.0, 4.5, 5.0, 6.0, "code"));
    }

    @Test
    public void invalidHalfLineCharacteristicsG2() {
        TestUtil.notImplemented(() -> createTieLineWithHalfline2ByDefault(INVALID, INVALID, INVALID, 1.0, 2.0,
                3.0, Double.NaN, 4.0, 4.5, 5.0, 6.0, "code"));
    }

    @Test
    public void invalidHalfLineCharacteristicsB1() {
        TestUtil.notImplemented(() -> createTieLineWithHalfline2ByDefault(INVALID, INVALID, INVALID, 1.0, 2.0,
                3.0, 3.5, Double.NaN, 4.5, 5.0, 6.0, "code"));
    }

    @Test
    public void invalidHalfLineCharacteristicsB2() {
        TestUtil.notImplemented(() -> createTieLineWithHalfline2ByDefault(INVALID, INVALID, INVALID, 1.0, 2.0,
                3.0, 3.5, 4.0, Double.NaN, 5.0, 6.0, "code"));
    }

    @Test
    public void invalidHalfLineCharacteristicsP() {
        TestUtil.notImplemented(() -> createTieLineWithHalfline2ByDefault(INVALID, INVALID, INVALID, 1.0, 2.0,
                3.0, 3.5, 4.0, 4.5, Double.NaN, 6.0, "code"));
    }

    @Test
    public void invalidHalfLineCharacteristicsQ() {
        TestUtil.notImplemented(() -> createTieLineWithHalfline2ByDefault(INVALID, INVALID, INVALID, 1.0, 2.0,
                3.0, 3.5, 4.0, 4.5, 5.0, Double.NaN, "code"));
    }

    @Test
    public void halfLineIdNull() {
        TestUtil.notImplemented(() -> createTieLineWithHalfline2ByDefault(INVALID, INVALID, null, 1.0, 2.0,
                3.0, 3.5, 4.0, 4.5, 5.0, 6.0, "code"));
    }

    @Test
    public void uctecodeNull() {
        TestUtil.notImplemented(() -> createTieLineWithHalfline2ByDefault(INVALID, INVALID, INVALID, 1.0, 2.0,
                3.0, 3.5, 4.0, 4.5, 5.0, 6.0, null));
    }

    @Test
    public void duplicate() {
        TestUtil.notImplemented(super::duplicate);
    }

    @Test
    public void testRemove() {
        TestUtil.notImplemented(super::testRemove);
    }
}
