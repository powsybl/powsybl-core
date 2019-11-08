/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import org.junit.Before;
import org.junit.Test;

public class HvdcLineAdapterTest {

    private MergingView network;

    @Before
    public void setUp() {
        network = MergingView.create("LineAdapterTest", "iidm");
        network.merge(HvdcTestNetwork.createLcc());
    }

    @Test
    public void testSetterGetter() {
        final HvdcLine line = network.getHvdcLine("L");

        // Not implemented yet !
        TestUtil.notImplemented(line::getConvertersMode);
        TestUtil.notImplemented(() -> line.setConvertersMode((HvdcLine.ConvertersMode) null));
        TestUtil.notImplemented(line::getR);
        TestUtil.notImplemented(() -> line.setR(0.0d));
        TestUtil.notImplemented(line::getNominalV);
        TestUtil.notImplemented(() -> line.setNominalV(0.0d));
        TestUtil.notImplemented(line::getActivePowerSetpoint);
        TestUtil.notImplemented(() -> line.setActivePowerSetpoint(0.0d));
        TestUtil.notImplemented(line::getMaxP);
        TestUtil.notImplemented(() -> line.setMaxP(0.0d));
        TestUtil.notImplemented(() -> line.getConverterStation((HvdcLine.Side) null));
        TestUtil.notImplemented(line::getConverterStation1);
        TestUtil.notImplemented(line::getConverterStation2);
        TestUtil.notImplemented(line::remove);
    }
}
