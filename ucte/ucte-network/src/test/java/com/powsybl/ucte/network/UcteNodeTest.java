/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.network;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class UcteNodeTest {

    @Test
    public void test() {
        UcteNodeCode code1 = new UcteNodeCode(UcteCountryCode.FR, "AAAAA", UcteVoltageLevelCode.VL_380, '1');
        UcteNode node = new UcteNode(code1, "name", UcteNodeStatus.REAL, UcteNodeTypeCode.UT,
                1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f, 8.0f, 9.0f, 10.0f, 11.0f, 12.0f, 13.0f, UctePowerPlantType.C);

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

        assertEquals(1.0f, node.getVoltageReference(), 0.0f);
        node.setVoltageReference(1.1f);
        assertEquals(1.1f, node.getVoltageReference(), 0.0f);

        assertEquals(2.0f, node.getActiveLoad(), 0.0f);
        node.setActiveLoad(2.1f);
        assertEquals(2.1f, node.getActiveLoad(), 0.0f);

        assertEquals(3.0f, node.getReactiveLoad(), 0.0f);
        node.setReactiveLoad(3.1f);
        assertEquals(3.1f, node.getReactiveLoad(), 0.0f);

        assertEquals(4.0f, node.getActivePowerGeneration(), 0.0f);
        node.setActivePowerGeneration(4.1f);
        assertEquals(4.1f, node.getActivePowerGeneration(), 0.0f);

        assertEquals(5.0f, node.getReactivePowerGeneration(), 0.0f);
        node.setReactivePowerGeneration(5.1f);
        assertEquals(5.1f, node.getReactivePowerGeneration(), 0.0f);

        assertEquals(6.0f, node.getMinimumPermissibleActivePowerGeneration(), 0.0f);
        node.setMinimumPermissibleActivePowerGeneration(6.1f);
        assertEquals(6.1f, node.getMinimumPermissibleActivePowerGeneration(), 0.0f);

        assertEquals(7.0f, node.getMaximumPermissibleActivePowerGeneration(), 0.0f);
        node.setMaximumPermissibleActivePowerGeneration(7.1f);
        assertEquals(7.1f, node.getMaximumPermissibleActivePowerGeneration(), 0.0f);

        assertEquals(8.0f, node.getMinimumPermissibleReactivePowerGeneration(), 0.0f);
        node.setMinimumPermissibleReactivePowerGeneration(8.1f);
        assertEquals(8.1f, node.getMinimumPermissibleReactivePowerGeneration(), 0.0f);

        assertEquals(9.0f, node.getMaximumPermissibleReactivePowerGeneration(), 0.0f);
        node.setMaximumPermissibleReactivePowerGeneration(9.1f);
        assertEquals(9.1f, node.getMaximumPermissibleReactivePowerGeneration(), 0.0f);

        assertEquals(10.0f, node.getStaticOfPrimaryControl(), 0.0f);
        node.setStaticOfPrimaryControl(10.1f);
        assertEquals(10.1f, node.getStaticOfPrimaryControl(), 0.0f);

        assertEquals(11.0f, node.getNominalPowerPrimaryControl(), 0.0f);
        node.setNominalPowerPrimaryControl(11.1f);
        assertEquals(11.1f, node.getNominalPowerPrimaryControl(), 0.0f);

        assertEquals(12.0f, node.getThreePhaseShortCircuitPower(), 0.0f);
        node.setThreePhaseShortCircuitPower(12.1f);
        assertEquals(12.1f, node.getThreePhaseShortCircuitPower(), 0.0f);

        assertEquals(13.0f, node.getXrRatio(), 0.0f);
        node.setXrRatio(13.1f);
        assertEquals(13.1f, node.getXrRatio(), 0.0f);

        assertEquals(UctePowerPlantType.C, node.getPowerPlantType());
        node.setPowerPlantType(UctePowerPlantType.F);
        assertEquals(UctePowerPlantType.F, node.getPowerPlantType());
        node.setPowerPlantType(null);
        assertNull(node.getPowerPlantType());
    }

    private UcteNode createNode() {
        UcteNodeCode code = new UcteNodeCode(UcteCountryCode.FR, "AAAAA", UcteVoltageLevelCode.VL_380, '1');
        return new UcteNode(code, null, UcteNodeStatus.REAL, UcteNodeTypeCode.PQ,
                Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN,
                Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN, null);
    }

    @Test
    public void testIsRegulatingVoltage() {
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
    public void testIsGenerator() {
        UcteNode node = createNode();

        assertFalse(node.isGenerator());

        node.setTypeCode(UcteNodeTypeCode.UT);
        assertTrue(node.isGenerator());
        node.setTypeCode(UcteNodeTypeCode.PQ);
        assertFalse(node.isGenerator());

        node.setActivePowerGeneration(1000.0f);
        assertTrue(node.isGenerator());
        node.setActivePowerGeneration(Float.NaN);
        assertFalse(node.isGenerator());

        node.setReactivePowerGeneration(1000.0f);
        assertTrue(node.isGenerator());
        node.setReactivePowerGeneration(Float.NaN);
        assertFalse(node.isGenerator());

        node.setMinimumPermissibleActivePowerGeneration(1000.0f);
        node.setMaximumPermissibleActivePowerGeneration(1000.0f);
        assertFalse(node.isGenerator());
        node.setMinimumPermissibleActivePowerGeneration(-1000.0f);
        assertTrue(node.isGenerator());
        node.setMinimumPermissibleActivePowerGeneration(Float.NaN);
        node.setMaximumPermissibleActivePowerGeneration(Float.NaN);
        assertFalse(node.isGenerator());

        node.setMinimumPermissibleReactivePowerGeneration(1000.0f);
        node.setMaximumPermissibleReactivePowerGeneration(1000.0f);
        assertFalse(node.isGenerator());
        node.setMinimumPermissibleReactivePowerGeneration(-1000.0f);
        assertTrue(node.isGenerator());
        node.setMinimumPermissibleReactivePowerGeneration(Float.NaN);
        node.setMaximumPermissibleReactivePowerGeneration(Float.NaN);
        assertFalse(node.isGenerator());
    }

    @Test
    public void testIsRegulatingFrequency() {
        UcteNode node = createNode();

        assertFalse(node.isRegulatingFrequency());

        node.setActivePowerGeneration(1000.0f);
        assertFalse(node.isRegulatingFrequency());

        node.setActivePowerGeneration(0.0f);
        assertFalse(node.isRegulatingFrequency());

        node.setActivePowerGeneration(-1000.0f);
        assertTrue(node.isRegulatingFrequency());
    }

    @Test
    public void testFix() {
        UcteNode node = createNode();
        node.setTypeCode(UcteNodeTypeCode.UT);

        node.setTypeCode(UcteNodeTypeCode.UT);
        node.setMinimumPermissibleActivePowerGeneration(-1000.0f);
        node.setMaximumPermissibleActivePowerGeneration(1000.0f);
        node.setMinimumPermissibleReactivePowerGeneration(-2000.0f);
        node.setMaximumPermissibleReactivePowerGeneration(2000.0f);
        node.fix();
        assertEquals(1000.0f, node.getMinimumPermissibleActivePowerGeneration(), 0.0f);
        assertEquals(-1000.0f, node.getMaximumPermissibleActivePowerGeneration(), 0.0f);
        assertEquals(2000.0f, node.getMinimumPermissibleReactivePowerGeneration(), 0.0f);
        assertEquals(-2000.0f, node.getMaximumPermissibleReactivePowerGeneration(), 0.0f);

        node = createNode();
        node.setTypeCode(UcteNodeTypeCode.UT);
        node.setActivePowerGeneration(10.0f);
        node.setReactivePowerGeneration(10.0f);
        node.setMinimumPermissibleActivePowerGeneration(0.0f);
        node.setMinimumPermissibleReactivePowerGeneration(0.0f);
        node.fix();
        assertEquals(node.getActivePowerGeneration(), node.getMinimumPermissibleActivePowerGeneration(), 0.0f);
        assertEquals(node.getReactivePowerGeneration(), node.getMinimumPermissibleReactivePowerGeneration(), 0.0f);

        node = createNode();
        node.setTypeCode(UcteNodeTypeCode.UT);
        node.setActivePowerGeneration(0.0f);
        node.setReactivePowerGeneration(0.0f);
        node.setMaximumPermissibleActivePowerGeneration(10.0f);
        node.setMaximumPermissibleReactivePowerGeneration(10.0f);
        node.fix();
        assertEquals(node.getActivePowerGeneration(), node.getMaximumPermissibleActivePowerGeneration(), 0.0f);
        assertEquals(node.getReactivePowerGeneration(), node.getMaximumPermissibleReactivePowerGeneration(), 0.0f);

        node = createNode();
        node.setTypeCode(UcteNodeTypeCode.UT);
        node.setReactivePowerGeneration(0.0f);
        node.setMinimumPermissibleReactivePowerGeneration(0.0f);
        node.setMaximumPermissibleReactivePowerGeneration(0.0f);
        node.fix();
        assertEquals(9999.0f, node.getMinimumPermissibleReactivePowerGeneration(), 0.0f);
        assertEquals(-9999.0f, node.getMaximumPermissibleReactivePowerGeneration(), 0.0f);

        node.setTypeCode(UcteNodeTypeCode.UT);
        node.setReactivePowerGeneration(0.0f);
        node.setMinimumPermissibleReactivePowerGeneration(10000.0f);
        node.setMaximumPermissibleReactivePowerGeneration(-10000.0f);
        node.fix();
    }
}
