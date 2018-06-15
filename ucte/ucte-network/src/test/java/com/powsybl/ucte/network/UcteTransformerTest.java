/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.network;

import org.junit.Assert;
import org.junit.Test;

import static com.powsybl.ucte.network.UcteElementStatus.REAL_ELEMENT_IN_OPERATION;
import static org.junit.Assert.assertEquals;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class UcteTransformerTest extends AbstractUcteElementTest {

    private UcteElementId createElementId() {
        UcteNodeCode node1 = new UcteNodeCode(UcteCountryCode.FR, "AAAAA", UcteVoltageLevelCode.VL_380, 'A');
        UcteNodeCode node2 = new UcteNodeCode(UcteCountryCode.BE, "BBBBB", UcteVoltageLevelCode.VL_220, 'B');
        return new UcteElementId(node1, node2, '1');
    }

    @Test
    public void test() {
        UcteElementId id = createElementId();
        UcteTransformer transformer = new UcteTransformer(id, REAL_ELEMENT_IN_OPERATION, 1.0f, 2.0f, 3.0f, 1000, "Transformer", 4.0f, 5.0f, 6.0f, 7.0f);

        // Test the constructor
        assertEquals(id, transformer.getId());
        assertEquals(id.toString(), transformer.toString());
        assertEquals(REAL_ELEMENT_IN_OPERATION, transformer.getStatus());
        assertEquals(1.0f, transformer.getResistance(), 0.0f);
        assertEquals(2.0f, transformer.getReactance(), 0.0f);
        assertEquals(3.0f, transformer.getSusceptance(), 0.0f);
        assertEquals(Integer.valueOf(1000), transformer.getCurrentLimit());

        // Test getters and setters
        testElement(transformer);

        transformer.setRatedVoltage1(4.1f);
        assertEquals(4.1f, transformer.getRatedVoltage1(), 0.0f);
        transformer.setRatedVoltage2(5.1f);
        assertEquals(5.1f, transformer.getRatedVoltage2(), 0.0f);
        transformer.setNominalPower(6.1f);
        assertEquals(6.1f, transformer.getNominalPower(), 0.0f);
        transformer.setConductance(7.1f);
        assertEquals(7.1f, transformer.getConductance(), 0.0f);
    }

    @Test
    public void testFix() {
        UcteElementId id = createElementId();
        UcteTransformer invalidTransformer1 = new UcteTransformer(id, REAL_ELEMENT_IN_OPERATION, 0.0f, 0.0f, 0.0f, -1, null, 0.0f, 0.0f, 0.0f, 0.0f);
        invalidTransformer1.fix();
        Assert.assertEquals(0.05f, invalidTransformer1.getReactance(), 0.0f);

        UcteTransformer invalidTransformer2 = new UcteTransformer(id, REAL_ELEMENT_IN_OPERATION, 0.0f, -0.01f, 0.0f, null, null, 0.0f, 0.0f, 0.0f, 0.0f);
        invalidTransformer2.fix();
        Assert.assertEquals(-0.05f, invalidTransformer2.getReactance(), 0.0f);
    }
}
