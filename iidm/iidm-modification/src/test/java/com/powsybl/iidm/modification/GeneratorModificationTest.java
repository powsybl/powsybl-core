/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class GeneratorModificationTest {

    private Network network;
    private Generator generator;

    @BeforeEach
    void setUp() {
        network = EurostagTutorialExample1Factory.create();
        generator = network.getGenerator("GEN");
    }

    @Test
    void testHasImpact() {
        GeneratorModification.Modifs modifs = new GeneratorModification.Modifs();
        NetworkModification modification1 = new GeneratorModification("WRONG_ID", modifs);
        assertEquals(NetworkModificationImpact.CANNOT_BE_APPLIED, modification1.hasImpactOnNetwork(network));

        NetworkModification modification2 = new GeneratorModification("GEN", modifs);
        assertEquals(NetworkModificationImpact.NO_IMPACT_ON_NETWORK, modification2.hasImpactOnNetwork(network));

        modifs = new GeneratorModification.Modifs();
        modifs.setVoltageRegulationMode(RegulationMode.VOLTAGE);
        NetworkModification modification3 = new GeneratorModification("GEN", modifs);
        assertEquals(NetworkModificationImpact.NO_IMPACT_ON_NETWORK, modification3.hasImpactOnNetwork(network));

        modifs = new GeneratorModification.Modifs();
        modifs.setConnected(true);
        NetworkModification modification4 = new GeneratorModification("GEN", modifs);
        assertEquals(NetworkModificationImpact.NO_IMPACT_ON_NETWORK, modification4.hasImpactOnNetwork(network));

        modifs = new GeneratorModification.Modifs();
        modifs.setDeltaTargetP(5.0);
        NetworkModification modification5 = new GeneratorModification("GEN", modifs);
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification5.hasImpactOnNetwork(network));

        modifs = new GeneratorModification.Modifs();
        modifs.setTargetP(225.0);
        NetworkModification modification6 = new GeneratorModification("GEN", modifs);
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification6.hasImpactOnNetwork(network));

        generator.getVoltageRegulation().setRegulating(false);
        modifs = new GeneratorModification.Modifs();
        modifs.setRegulating(true);
        NetworkModification modification7 = new GeneratorModification("GEN", modifs);
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification7.hasImpactOnNetwork(network));

        generator.getVoltageRegulation().setRegulating(true);
        modifs = new GeneratorModification.Modifs();
        modifs.setRegulating(false);
        NetworkModification modification8 = new GeneratorModification("GEN", modifs);
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification8.hasImpactOnNetwork(network));

        generator.getVoltageRegulation().setRegulating(false);
        generator.getVoltageRegulation().setRegulating(true);
        generator.setTargetV(Double.NaN);
        modifs = new GeneratorModification.Modifs();
        modifs.setRegulating(false);
        modifs.setVoltageRegulationMode(RegulationMode.VOLTAGE);
        NetworkModification modification9 = new GeneratorModification("GEN", modifs);
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification9.hasImpactOnNetwork(network));

        generator.setLocalTargetV(24.5);
        generator.getVoltageRegulation().setRegulating(true);
        modifs = new GeneratorModification.Modifs();
        modifs.setConnected(false);
        NetworkModification modification10 = new GeneratorModification("GEN", modifs);
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification10.hasImpactOnNetwork(network));

        modifs = new GeneratorModification.Modifs();
        modifs.setTargetQ(12.0);
        NetworkModification modification11 = new GeneratorModification("GEN", modifs);
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification11.hasImpactOnNetwork(network));

        modifs = new GeneratorModification.Modifs();
        modifs.setTargetV(12.0);
        NetworkModification modification12 = new GeneratorModification("GEN", modifs);
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification12.hasImpactOnNetwork(network));

        modifs = new GeneratorModification.Modifs();
        modifs.setMaxP(12.0);
        NetworkModification modification13 = new GeneratorModification("GEN", modifs);
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification13.hasImpactOnNetwork(network));

        modifs = new GeneratorModification.Modifs();
        modifs.setMinP(12.0);
        NetworkModification modification14 = new GeneratorModification("GEN", modifs);
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification14.hasImpactOnNetwork(network));
    }

    @Test
    void testHasImpactVoltageRegulationVoltageRegulationAbsent() {
        GeneratorModification.Modifs modifs;
        GeneratorModification modification;
        generator.removeVoltageRegulation();

        //
        modifs = new GeneratorModification.Modifs();
        modifs.setRegulating(false);
        modification = new GeneratorModification("GEN", modifs);
        assertEquals(NetworkModificationImpact.NO_IMPACT_ON_NETWORK, modification.hasImpactOnNetwork(network));

        //
        modifs = new GeneratorModification.Modifs();
        modifs.setRegulating(true);
        modification = new GeneratorModification("GEN", modifs);
        assertEquals(NetworkModificationImpact.NO_IMPACT_ON_NETWORK, modification.hasImpactOnNetwork(network));
    }

    @Test
    void testHasImpactVoltageRegulationVoltageRegulationPresent() {
        GeneratorModification.Modifs modifs;
        GeneratorModification modification;
        generator.newVoltageRegulation()
            .withTargetValue(100.0)
            .withRegulating(true)
            .withMode(RegulationMode.VOLTAGE);

        // mode modification
        modifs = new GeneratorModification.Modifs();
        modifs.setRegulating(false);
        modification = new GeneratorModification("GEN", modifs);
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification.hasImpactOnNetwork(network));

        // regulating modification
        modifs = new GeneratorModification.Modifs();
        modifs.setRegulating(false);
        modification = new GeneratorModification("GEN", modifs);
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification.hasImpactOnNetwork(network));

        generator.getVoltageRegulation().setRegulating(false);

        // mode modification
        modifs = new GeneratorModification.Modifs();
        modifs.setRegulating(false);
        modification = new GeneratorModification("GEN", modifs);
        assertEquals(NetworkModificationImpact.NO_IMPACT_ON_NETWORK, modification.hasImpactOnNetwork(network));

        // regulating modification
        modifs = new GeneratorModification.Modifs();
        modifs.setRegulating(false);
        modification = new GeneratorModification("GEN", modifs);
        assertEquals(NetworkModificationImpact.NO_IMPACT_ON_NETWORK, modification.hasImpactOnNetwork(network));

        // regulating modification
        modifs = new GeneratorModification.Modifs();
        modifs.setRegulating(true);
        modification = new GeneratorModification("GEN", modifs);
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification.hasImpactOnNetwork(network));
    }

    @Test
    void testGeneratorWithoutVoltageRegulationModifToAddEmptyVoltageRegulation() {
        // GIVEN
        generator.removeVoltageRegulation();
        GeneratorModification.Modifs modifs = new GeneratorModification.Modifs();
        GeneratorModification modification = new GeneratorModification(generator.getId(), modifs);
        // WHEN
        modification.apply(network);
        // THEN
        assertNull(generator.getVoltageRegulation());
    }

    @Test
    void testGeneratorWithoutVoltageRegulationModifToAddVoltageRegulationWithMode() {
        // GIVEN
        generator.removeVoltageRegulation();
        GeneratorModification.Modifs modifs = new GeneratorModification.Modifs();
        modifs.setVoltageRegulationMode(RegulationMode.VOLTAGE);
        GeneratorModification modification = new GeneratorModification(generator.getId(), modifs);
        // WHEN
        modification.apply(network);
        // THEN
        assertNotNull(generator.getVoltageRegulation());
        assertEquals(RegulationMode.VOLTAGE, generator.getVoltageRegulation().getMode());
    }

    @Test
    void testGeneratorWithoutVoltageRegulationModifToAddVoltageRegulationWithTargetValue() {
        // GIVEN
        generator.removeVoltageRegulation();
        GeneratorModification.Modifs modifs = new GeneratorModification.Modifs();
        modifs.setTargetV(100.0);
        GeneratorModification modification = new GeneratorModification(generator.getId(), modifs);
        // WHEN
        modification.apply(network);
        // THEN
        assertNull(generator.getVoltageRegulation());
    }

    @Test
    void testGeneratorWithoutVoltageRegulationModifToAddVoltageRegulationWithRegulating() {
        // GIVEN
        generator.removeVoltageRegulation();
        GeneratorModification.Modifs modifs = new GeneratorModification.Modifs();
        modifs.setRegulating(true);
        GeneratorModification modification = new GeneratorModification(generator.getId(), modifs);
        // WHEN
        modification.apply(network);
        // THEN
        assertNotNull(generator.getVoltageRegulation());
        assertEquals(RegulationMode.VOLTAGE, generator.getVoltageRegulation().getMode());
    }

    @Test
    void testGeneratorWithoutVoltageRegulationModifToAddVoltageRegulation() {
        // GIVEN
        generator.removeVoltageRegulation();
        GeneratorModification.Modifs modifs = new GeneratorModification.Modifs();
        modifs.setVoltageRegulationMode(RegulationMode.VOLTAGE);
        modifs.setTargetV(100.0);
        modifs.setRegulating(true);
        GeneratorModification modification = new GeneratorModification(generator.getId(), modifs);
        // WHEN
        modification.apply(network);
        // THEN
        assertNotNull(generator.getVoltageRegulation());
        assertEquals(RegulationMode.VOLTAGE, generator.getVoltageRegulation().getMode());
        assertEquals(Double.NaN, generator.getVoltageRegulation().getTargetValue());
        assertEquals(100.0, generator.getRegulatingTargetV());
        assertEquals(100.0, generator.getLocalTargetV());
        assertTrue(generator.getVoltageRegulation().isRegulating());
    }

    @Test
    void testGeneratorWithVoltageRegulationModifToAddEmptyVoltageRegulation() {
        // GIVEN
        GeneratorModification.Modifs modifs = new GeneratorModification.Modifs();
        GeneratorModification modification = new GeneratorModification(generator.getId(), modifs);
        // WHEN
        modification.apply(network);
        // THEN
        assertNotNull(generator.getVoltageRegulation());
        assertEquals(RegulationMode.VOLTAGE, generator.getVoltageRegulation().getMode());
        assertEquals(Double.NaN, generator.getVoltageRegulation().getTargetValue());
        assertEquals(301.0, generator.getRegulatingTargetQ());
        assertEquals(301.0, generator.getLocalTargetQ());
        assertEquals(24.5, generator.getLocalTargetV());
        assertEquals(24.5, generator.getRegulatingTargetV());
        assertTrue(generator.getVoltageRegulation().isRegulating());
    }

    @Test
    void testGeneratorWithVoltageRegulationModifToAddVoltageRegulationReactiveMode() {
        // GIVEN
        GeneratorModification.Modifs modifs = new GeneratorModification.Modifs();
        modifs.setRegulating(false);
        GeneratorModification modification = new GeneratorModification(generator.getId(), modifs);
        // WHEN
        modification.apply(network);
        // THEN
        assertNotNull(generator.getVoltageRegulation());
        assertEquals(Double.NaN, generator.getVoltageRegulation().getTargetValue());
        assertEquals(301.0, generator.getLocalTargetQ());
        assertEquals(24.5, generator.getLocalTargetV());
        assertFalse(generator.getVoltageRegulation().isRegulating());
    }

    @Test
    void testGeneratorWithVoltageRegulationModifToAddVoltageRegulationReactiveModeAndValue() {
        // GIVEN
        GeneratorModification.Modifs modifs = new GeneratorModification.Modifs();
        modifs.setRegulating(false);
        modifs.setTargetQ(140.0);
        GeneratorModification modification = new GeneratorModification(generator.getId(), modifs);
        // WHEN
        modification.apply(network);
        // THEN
        assertNotNull(generator.getVoltageRegulation());
        assertEquals(RegulationMode.VOLTAGE, generator.getVoltageRegulation().getMode());
        assertEquals(Double.NaN, generator.getVoltageRegulation().getTargetValue());
        assertEquals(140.0, generator.getRegulatingTargetQ());
        assertEquals(140.0, generator.getLocalTargetQ());
        assertEquals(24.5, generator.getLocalTargetV());
        assertEquals(24.5, generator.getRegulatingTargetV());
        assertFalse(generator.getVoltageRegulation().isRegulating());
    }

    @Test
    void testGeneratorWithVoltageRegulationModifToAddVoltageRegulationReactiveModeThrowException() {
        // GIVEN
        generator.setTargetQ(Double.NaN);
        generator.newVoltageRegulation().withMode(RegulationMode.VOLTAGE).build();
        GeneratorModification.Modifs modifs = new GeneratorModification.Modifs();
        modifs.setVoltageRegulationMode(RegulationMode.REACTIVE_POWER);
        GeneratorModification modification = new GeneratorModification(generator.getId(), modifs);
        // WHEN
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> modification.apply(network));
        // THEN
        assertEquals("Unexpected value: REACTIVE_POWER not yet implemented", exception.getMessage());
    }

    @Test
    void testGeneratorWithVoltageRegulationModifToAddVoltageRegulationVoltageModeAndValue() {
        // GIVEN
        generator.removeVoltageRegulation();
        GeneratorModification.Modifs modifs = new GeneratorModification.Modifs();
        modifs.setVoltageRegulationMode(RegulationMode.VOLTAGE);
        modifs.setTargetV(120.0);
        GeneratorModification modification = new GeneratorModification(generator.getId(), modifs);
        // WHEN
        modification.apply(network);
        // THEN
        assertNotNull(generator.getVoltageRegulation());
        assertEquals(RegulationMode.VOLTAGE, generator.getVoltageRegulation().getMode());
        assertEquals(Double.NaN, generator.getVoltageRegulation().getTargetValue());
        assertEquals(301.0, generator.getRegulatingTargetQ());
        assertEquals(301.0, generator.getLocalTargetQ());
        assertEquals(120.0, generator.getLocalTargetV());
        assertEquals(120.0, generator.getRegulatingTargetV());
        assertTrue(generator.isRegulating());
    }

    @Test
    void testGeneratorWithVoltageRegulationModifToAddVoltageRegulationVoltageModeAndValueFromTargetV() {
        // GIVEN
        generator.removeVoltageRegulation();
        generator.setTargetV(130.0);
        GeneratorModification.Modifs modifs = new GeneratorModification.Modifs();
        modifs.setVoltageRegulationMode(RegulationMode.VOLTAGE);
        GeneratorModification modification = new GeneratorModification(generator.getId(), modifs);
        // WHEN
        modification.apply(network);
        // THEN
        assertNotNull(generator.getVoltageRegulation());
        assertEquals(RegulationMode.VOLTAGE, generator.getVoltageRegulation().getMode());
        assertEquals(Double.NaN, generator.getVoltageRegulation().getTargetValue());
        assertEquals(301.0, generator.getRegulatingTargetQ());
        assertEquals(301.0, generator.getLocalTargetQ());
        assertEquals(130.0, generator.getLocalTargetV());
        assertEquals(130.0, generator.getRegulatingTargetV());
        assertTrue(generator.isRegulating());
    }

    @ParameterizedTest
    @EnumSource(
        value = RegulationMode.class,
        mode = EnumSource.Mode.EXCLUDE,
        names = {"VOLTAGE", "REACTIVE_POWER"}
    )
    void testGeneratorWithVoltageRegulationModifToAddUnsupportedMode(RegulationMode unsupportedMode) {
        // GIVEN
        GeneratorModification.Modifs modifs = new GeneratorModification.Modifs();
        modifs.setVoltageRegulationMode(unsupportedMode);
        GeneratorModification modification = new GeneratorModification(generator.getId(), modifs);
        // WHEN
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> modification.apply(network));
        // THEN
        assertEquals("Unexpected value: " + unsupportedMode + " not yet implemented", exception.getMessage());
    }
}
