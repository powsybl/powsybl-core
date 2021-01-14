/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.tck.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.CoordinatedReactiveControl;
import com.powsybl.iidm.network.extensions.CoordinatedReactiveControlAdder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public abstract class AbstractCoordinatedReactiveControlTest {

    private Generator generator;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() {
        Network network = EurostagTutorialExample1Factory.create();
        generator = network.getGenerator("GEN");
    }

    @Test
    public void test() {
        generator.newExtension(CoordinatedReactiveControlAdder.class)
                .withQPercent(100.0)
                .add();
        CoordinatedReactiveControl control = generator.getExtension(CoordinatedReactiveControl.class);
        assertEquals(100.0, control.getQPercent(), 0.0);
        control.setQPercent(99.0);
        assertEquals(99.0, control.getQPercent(), 0.0);
        assertEquals("GEN", control.getExtendable().getId());
    }

    @Test
    public void testUndefined() {
        exception.expect(PowsyblException.class);
        exception.expectMessage("Undefined value for qPercent");
        generator.newExtension(CoordinatedReactiveControlAdder.class)
                .withQPercent(Double.NaN)
                .add();
    }
}
