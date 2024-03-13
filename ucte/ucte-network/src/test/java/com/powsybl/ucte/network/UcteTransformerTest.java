/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.network;

import com.powsybl.commons.report.ReportNode;
import org.junit.jupiter.api.Test;

import static com.powsybl.ucte.network.UcteElementStatus.REAL_ELEMENT_IN_OPERATION;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class UcteTransformerTest extends AbstractUcteElementTest {

    private UcteElementId createElementId() {
        UcteNodeCode node1 = new UcteNodeCode(UcteCountryCode.FR, "AAAAA", UcteVoltageLevelCode.VL_380, 'A');
        UcteNodeCode node2 = new UcteNodeCode(UcteCountryCode.BE, "BBBBB", UcteVoltageLevelCode.VL_220, 'B');
        return new UcteElementId(node1, node2, '1');
    }

    @Test
    void test() {
        UcteElementId id = createElementId();
        UcteTransformer transformer = new UcteTransformer(id, REAL_ELEMENT_IN_OPERATION, 1.0, 2.0, 3.0, 1000, "Transformer", 4.0, 5.0, 6.0, 7.0);

        // Test the constructor
        assertEquals(id, transformer.getId());
        assertEquals(id.toString(), transformer.toString());
        assertEquals(REAL_ELEMENT_IN_OPERATION, transformer.getStatus());
        assertEquals(1.0, transformer.getResistance(), 0.0);
        assertEquals(2.0, transformer.getReactance(), 0.0);
        assertEquals(3.0, transformer.getSusceptance(), 0.0);
        assertEquals(Integer.valueOf(1000), transformer.getCurrentLimit());

        // Test getters and setters
        testElement(transformer);

        transformer.setRatedVoltage1(4.1);
        assertEquals(4.1, transformer.getRatedVoltage1(), 0.0);
        transformer.setRatedVoltage2(5.1);
        assertEquals(5.1, transformer.getRatedVoltage2(), 0.0);
        transformer.setNominalPower(6.1);
        assertEquals(6.1, transformer.getNominalPower(), 0.0);
        transformer.setConductance(7.1);
        assertEquals(7.1, transformer.getConductance(), 0.0);
    }

    @Test
    void testFix() {
        UcteElementId id = createElementId();
        UcteTransformer invalidTransformer1 = new UcteTransformer(id, REAL_ELEMENT_IN_OPERATION, 0.0, 0.0, 0.0, -1, null, 0.0, 0.0, 0.0, 0.0);
        invalidTransformer1.fix(ReportNode.NO_OP);
        assertEquals(0.05, invalidTransformer1.getReactance(), 0.0);

        UcteTransformer invalidTransformer2 = new UcteTransformer(id, REAL_ELEMENT_IN_OPERATION, 0.0, -0.01, 0.0, null, null, 0.0, 0.0, 0.0, 0.0);
        invalidTransformer2.fix(ReportNode.NO_OP);
        assertEquals(-0.05, invalidTransformer2.getReactance(), 0.0);
    }
}
