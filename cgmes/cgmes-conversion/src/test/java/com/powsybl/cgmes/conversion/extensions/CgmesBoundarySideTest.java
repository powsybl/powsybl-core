/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.test.DanglingLineNetworkFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class CgmesBoundarySideTest {

    private DanglingLine dl;

    @Rule
    public final ExpectedException expected = ExpectedException.none();

    @Before
    public void setUp() {
        dl = DanglingLineNetworkFactory.create().getDanglingLine("DL");
    }

    @Test
    public void test() {
        dl.newExtension(CgmesBoundarySideAdder.class)
                .setBoundarySide(1)
                .add();
        CgmesBoundarySide extension = dl.getExtension(CgmesBoundarySide.class);
        assertNotNull(extension);
        assertEquals(1, extension.getBoundarySide());
    }

    @Test
    public void noBoundarySideSet() {
        expected.expect(PowsyblException.class);
        expected.expectMessage("Incorrect boundary side (-1) for dangling line DL");
        dl.newExtension(CgmesBoundarySideAdder.class).add();
    }

    @Test
    public void failBoundarySideSet() {
        expected.expect(PowsyblException.class);
        expected.expectMessage("Incorrect boundary side (3) for dangling line DL");
        dl.newExtension(CgmesBoundarySideAdder.class)
                .setBoundarySide(3)
                .add();
    }
}
