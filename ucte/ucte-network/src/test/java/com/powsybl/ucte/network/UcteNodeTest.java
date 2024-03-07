/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.network;

import com.powsybl.commons.report.ReportNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class UcteNodeTest {

    @Test
    void test() {
        UcteNodeCode code1 = new UcteNodeCode(UcteCountryCode.FR, "AAAAA", UcteVoltageLevelCode.VL_380, '1');
        UcteNode node = new UcteNode(code1, "name", UcteNodeStatus.REAL, UcteNodeTypeCode.UT,
                1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, UctePowerPlantType.C);

        assertEquals(code1, node.getCode());
        UcteNodeCode code2 = new UcteNodeCode(UcteCountryCode.BE, "BBBBB", UcteVoltageLevelCode.VL_220, '2');
        node.setCode(code2);
        assertEquals(code2, node.getCode());

        assertEquals(code2.toString(), node.toString());

        assertEquals("name", node.getGeographicalName());
        node.setGeographicalName("NAME");
        assertEquals("NAME", node.getGeographicalName());
        node.setGeographicalName(null);
        assertNull(node.getGeographicalName());

        assertEquals(UcteNodeStatus.REAL, node.getStatus());
        node.setStatus(UcteNodeStatus.EQUIVALENT);
        assertEquals(UcteNodeStatus.EQUIVALENT, node.getStatus());

        assertEquals(UcteNodeTypeCode.UT, node.getTypeCode());
        node.setTypeCode(UcteNodeTypeCode.PQ);
        assertEquals(UcteNodeTypeCode.PQ, node.getTypeCode());

        assertEquals(1.0, node.getVoltageReference(), 0.0);
        node.setVoltageReference(1.1);
        assertEquals(1.1, node.getVoltageReference(), 0.0);

        assertEquals(2.0, node.getActiveLoad(), 0.0);
        node.setActiveLoad(2.1);
        assertEquals(2.1, node.getActiveLoad(), 0.0);

        assertEquals(3.0, node.getReactiveLoad(), 0.0);
        node.setReactiveLoad(3.1);
        assertEquals(3.1, node.getReactiveLoad(), 0.0);

        assertEquals(4.0, node.getActivePowerGeneration(), 0.0);
        node.setActivePowerGeneration(4.1);
        assertEquals(4.1, node.getActivePowerGeneration(), 0.0);

        assertEquals(5.0, node.getReactivePowerGeneration(), 0.0);
        node.setReactivePowerGeneration(5.1);
        assertEquals(5.1, node.getReactivePowerGeneration(), 0.0);

        assertEquals(6.0, node.getMinimumPermissibleActivePowerGeneration(), 0.0);
        node.setMinimumPermissibleActivePowerGeneration(6.1);
        assertEquals(6.1, node.getMinimumPermissibleActivePowerGeneration(), 0.0);

        assertEquals(7.0, node.getMaximumPermissibleActivePowerGeneration(), 0.0);
        node.setMaximumPermissibleActivePowerGeneration(7.1);
        assertEquals(7.1, node.getMaximumPermissibleActivePowerGeneration(), 0.0);

        assertEquals(8.0, node.getMinimumPermissibleReactivePowerGeneration(), 0.0);
        node.setMinimumPermissibleReactivePowerGeneration(8.1);
        assertEquals(8.1, node.getMinimumPermissibleReactivePowerGeneration(), 0.0);

        assertEquals(9.0, node.getMaximumPermissibleReactivePowerGeneration(), 0.0);
        node.setMaximumPermissibleReactivePowerGeneration(9.1);
        assertEquals(9.1, node.getMaximumPermissibleReactivePowerGeneration(), 0.0);

        assertEquals(10.0, node.getStaticOfPrimaryControl(), 0.0);
        node.setStaticOfPrimaryControl(10.1);
        assertEquals(10.1, node.getStaticOfPrimaryControl(), 0.0);

        assertEquals(11.0, node.getNominalPowerPrimaryControl(), 0.0);
        node.setNominalPowerPrimaryControl(11.1);
        assertEquals(11.1, node.getNominalPowerPrimaryControl(), 0.0);

        assertEquals(12.0, node.getThreePhaseShortCircuitPower(), 0.0);
        node.setThreePhaseShortCircuitPower(12.1);
        assertEquals(12.1, node.getThreePhaseShortCircuitPower(), 0.0);

        assertEquals(13.0, node.getXrRatio(), 0.0);
        node.setXrRatio(13.1);
        assertEquals(13.1, node.getXrRatio(), 0.0);

        assertEquals(UctePowerPlantType.C, node.getPowerPlantType());
        node.setPowerPlantType(UctePowerPlantType.F);
        assertEquals(UctePowerPlantType.F, node.getPowerPlantType());
        node.setPowerPlantType(null);
        assertNull(node.getPowerPlantType());
    }

    private UcteNode createNode() {
        UcteNodeCode code = new UcteNodeCode(UcteCountryCode.FR, "AAAAA", UcteVoltageLevelCode.VL_380, '1');
        return new UcteNode(code, null, UcteNodeStatus.REAL, UcteNodeTypeCode.PQ,
                Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
                Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, null);
    }

    @Test
    void testIsRegulatingVoltage() {
        UcteNode node = createNode();

        node.setTypeCode(UcteNodeTypeCode.PQ);
        assertFalse(node.isRegulatingVoltage());

        node.setTypeCode(UcteNodeTypeCode.QT);
        assertFalse(node.isRegulatingVoltage());

        node.setTypeCode(UcteNodeTypeCode.PU);
        assertTrue(node.isRegulatingVoltage());

        node.setTypeCode(UcteNodeTypeCode.UT);
        assertTrue(node.isRegulatingVoltage());
    }

    @Test
    void testIsGenerator() {
        UcteNode node = createNode();

        assertFalse(node.isGenerator());

        node.setTypeCode(UcteNodeTypeCode.UT);
        assertTrue(node.isGenerator());
        node.setTypeCode(UcteNodeTypeCode.PQ);
        assertFalse(node.isGenerator());

        node.setActivePowerGeneration(1000.0);
        assertTrue(node.isGenerator());
        node.setActivePowerGeneration(Double.NaN);
        assertFalse(node.isGenerator());

        node.setReactivePowerGeneration(1000.0);
        assertTrue(node.isGenerator());
        node.setReactivePowerGeneration(Double.NaN);
        assertFalse(node.isGenerator());

        node.setMinimumPermissibleActivePowerGeneration(1000.0);
        node.setMaximumPermissibleActivePowerGeneration(1000.0);
        assertFalse(node.isGenerator());
        node.setMinimumPermissibleActivePowerGeneration(-1000.0);
        assertTrue(node.isGenerator());
        node.setMinimumPermissibleActivePowerGeneration(Double.NaN);
        node.setMaximumPermissibleActivePowerGeneration(Double.NaN);
        assertFalse(node.isGenerator());

        node.setMinimumPermissibleReactivePowerGeneration(1000.0);
        node.setMaximumPermissibleReactivePowerGeneration(1000.0);
        assertFalse(node.isGenerator());
        node.setMinimumPermissibleReactivePowerGeneration(-1000.0);
        assertTrue(node.isGenerator());
        node.setMinimumPermissibleReactivePowerGeneration(Double.NaN);
        node.setMaximumPermissibleReactivePowerGeneration(Double.NaN);
        assertFalse(node.isGenerator());
    }

    @Test
    void testIsRegulatingFrequency() {
        UcteNode node = createNode();

        assertFalse(node.isRegulatingFrequency());

        node.setActivePowerGeneration(1000.0);
        assertFalse(node.isRegulatingFrequency());

        node.setActivePowerGeneration(0.0);
        assertFalse(node.isRegulatingFrequency());

        node.setActivePowerGeneration(-1000.0);
        assertTrue(node.isRegulatingFrequency());
    }

    @Test
    void testFix() {
        UcteNode node = createNode();
        node.setTypeCode(UcteNodeTypeCode.UT);

        node.setTypeCode(UcteNodeTypeCode.UT);
        node.setMinimumPermissibleActivePowerGeneration(-1000.0);
        node.setMaximumPermissibleActivePowerGeneration(1000.0);
        node.setMinimumPermissibleReactivePowerGeneration(-2000.0);
        node.setMaximumPermissibleReactivePowerGeneration(2000.0);
        node.fix(ReportNode.NO_OP);
        assertEquals(1000.0, node.getMinimumPermissibleActivePowerGeneration(), 0.0);
        assertEquals(-1000.0, node.getMaximumPermissibleActivePowerGeneration(), 0.0);
        assertEquals(2000.0, node.getMinimumPermissibleReactivePowerGeneration(), 0.0);
        assertEquals(-2000.0, node.getMaximumPermissibleReactivePowerGeneration(), 0.0);

        node = createNode();
        node.setTypeCode(UcteNodeTypeCode.UT);
        node.setActivePowerGeneration(10.0);
        node.setReactivePowerGeneration(10.0);
        node.setMinimumPermissibleActivePowerGeneration(0.0);
        node.setMinimumPermissibleReactivePowerGeneration(0.0);
        node.fix(ReportNode.NO_OP);
        assertEquals(node.getActivePowerGeneration(), node.getMinimumPermissibleActivePowerGeneration(), 0.0);
        assertEquals(node.getReactivePowerGeneration(), node.getMinimumPermissibleReactivePowerGeneration(), 0.0);

        node = createNode();
        node.setTypeCode(UcteNodeTypeCode.UT);
        node.setActivePowerGeneration(0.0);
        node.setReactivePowerGeneration(0.0);
        node.setMaximumPermissibleActivePowerGeneration(10.0);
        node.setMaximumPermissibleReactivePowerGeneration(10.0);
        node.fix(ReportNode.NO_OP);
        assertEquals(node.getActivePowerGeneration(), node.getMaximumPermissibleActivePowerGeneration(), 0.0);
        assertEquals(node.getReactivePowerGeneration(), node.getMaximumPermissibleReactivePowerGeneration(), 0.0);

        node = createNode();
        node.setTypeCode(UcteNodeTypeCode.UT);
        node.setReactivePowerGeneration(0.0);
        node.setMinimumPermissibleReactivePowerGeneration(0.0);
        node.setMaximumPermissibleReactivePowerGeneration(0.0);
        node.fix(ReportNode.NO_OP);
        assertEquals(9999.0, node.getMinimumPermissibleReactivePowerGeneration(), 0.0);
        assertEquals(-9999.0, node.getMaximumPermissibleReactivePowerGeneration(), 0.0);

        node.setTypeCode(UcteNodeTypeCode.UT);
        node.setReactivePowerGeneration(0.0);
        node.setMinimumPermissibleReactivePowerGeneration(10000.0);
        node.setMaximumPermissibleReactivePowerGeneration(-10000.0);
        node.fix(ReportNode.NO_OP);
    }
}
