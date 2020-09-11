/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.extensions;

import com.powsybl.cgmes.conversion.elements.CgmesTopologyKind;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class CimCharacteristicsTest {

    @Rule
    public final ExpectedException expected = ExpectedException.none();

    @Test
    public void test() {
        Network network = EurostagTutorialExample1Factory.create();
        network.newExtension(CimCharacteristicsAdder.class)
                .setTopologyKind(CgmesTopologyKind.NODE_BREAKER)
                .setCimVersion(16)
                .add();
        CimCharacteristics extension = network.getExtension(CimCharacteristics.class);
        assertNotNull(extension);
        assertEquals(CgmesTopologyKind.NODE_BREAKER, extension.getTopologyKind());
        assertEquals(16, extension.getCimVersion());
    }

    @Test
    public void invalid() {
        expected.expect(PowsyblException.class);
        expected.expectMessage("CimCharacteristics.topologyKind is undefined");
        EurostagTutorialExample1Factory.create().newExtension(CimCharacteristicsAdder.class).add();
    }
}
