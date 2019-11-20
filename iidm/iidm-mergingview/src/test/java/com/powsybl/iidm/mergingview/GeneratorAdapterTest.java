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

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class GeneratorAdapterTest {
    private MergingView mergingView;

    @Before
    public void setup() {
        mergingView = MergingView.create("GeneratorAdapterTest", "iidm");
        mergingView.merge(EurostagTutorialExample1Factory.create());
    }

    @Test
    public void testSetterGetter() {
        final Generator generator = mergingView.getGenerator("GEN");
        assertNotNull(generator);
        assertTrue(generator instanceof GeneratorAdapter);
        assertSame(mergingView, generator.getNetwork());

        // Not implemented yet !
        TestUtil.notImplemented(generator::getTerminal);
        TestUtil.notImplemented(generator::getType);
        TestUtil.notImplemented(generator::getTerminals);
        TestUtil.notImplemented(generator::remove);
        TestUtil.notImplemented(generator::getReactiveLimits);
        TestUtil.notImplemented(() -> generator.getReactiveLimits(null));
        TestUtil.notImplemented(generator::newReactiveCapabilityCurve);
        TestUtil.notImplemented(generator::newMinMaxReactiveLimits);
        TestUtil.notImplemented(generator::getEnergySource);
        TestUtil.notImplemented(() -> generator.setEnergySource(null));
        TestUtil.notImplemented(generator::getMaxP);
        TestUtil.notImplemented(() -> generator.setMaxP(0.0d));
        TestUtil.notImplemented(generator::getMinP);
        TestUtil.notImplemented(() -> generator.setMinP(0.0d));
        TestUtil.notImplemented(generator::isVoltageRegulatorOn);
        TestUtil.notImplemented(() -> generator.setVoltageRegulatorOn(false));
        TestUtil.notImplemented(generator::getRegulatingTerminal);
        TestUtil.notImplemented(() -> generator.setRegulatingTerminal(null));
        TestUtil.notImplemented(generator::getTargetV);
        TestUtil.notImplemented(() -> generator.setTargetV(0.0d));
        TestUtil.notImplemented(generator::getTargetP);
        TestUtil.notImplemented(() -> generator.setTargetP(0.0d));
        TestUtil.notImplemented(generator::getTargetQ);
        TestUtil.notImplemented(() -> generator.setTargetQ(0.0d));
        TestUtil.notImplemented(generator::getRatedS);
        TestUtil.notImplemented(() -> generator.setRatedS(0.0d));
    }
}
