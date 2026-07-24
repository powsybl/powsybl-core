/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.nc;

import com.powsybl.action.GeneratorAction;
import com.powsybl.nc.reader.RotatingMachineActionReader;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Thomas Bouquet {@literal <thomas.bouquet at rte-france.com>}
 */
public class RotatingMachineActionReaderTest extends AbstractReaderTest {

    @Test
    void importRotatingMachineActions() {
        queryManager.read(getResourcePath("/RotatingMachineActions.zip"));

        RotatingMachineActionReader reader = new RotatingMachineActionReader(queryManager, NETWORK);
        List<GeneratorAction> generatorActions = reader.readFromProfiles().stream().sorted(Comparator.comparing(GeneratorAction::getId)).toList();

        assertEquals(3, generatorActions.size());

        GeneratorAction generatorAction1 = generatorActions.getFirst();
        assertEquals("rotating-machine-action-1", generatorAction1.getId());
        assertEquals("FFR1AA1 _generator", generatorAction1.getGeneratorId());
        assertTrue(generatorAction1.getActivePowerValue().isPresent());
        assertEquals(1500.0, generatorAction1.getActivePowerValue().getAsDouble());
        assertTrue(generatorAction1.isActivePowerRelativeValue().isPresent());
        assertFalse(generatorAction1.isActivePowerRelativeValue().get());

        GeneratorAction generatorAction2 = generatorActions.get(1);
        assertEquals("rotating-machine-action-2", generatorAction2.getId());
        assertEquals("FFR2AA1 _generator", generatorAction2.getGeneratorId());
        assertTrue(generatorAction2.getActivePowerValue().isPresent());
        assertEquals(350.0, generatorAction2.getActivePowerValue().getAsDouble());
        assertTrue(generatorAction2.isActivePowerRelativeValue().isPresent());
        assertTrue(generatorAction2.isActivePowerRelativeValue().get());

        GeneratorAction generatorAction3 = generatorActions.get(2);
        assertEquals("rotating-machine-action-3", generatorAction3.getId());
        assertEquals("FFR2AA1 _generator", generatorAction3.getGeneratorId());
        assertTrue(generatorAction3.getActivePowerValue().isPresent());
        assertEquals(-210.0, generatorAction3.getActivePowerValue().getAsDouble());
        assertTrue(generatorAction3.isActivePowerRelativeValue().isPresent());
        assertTrue(generatorAction3.isActivePowerRelativeValue().get());

        // test logs content
        assertEquals(13, appender.getEvents().size());
        assertEquals("Processing entry RTE_RA.xml.", appender.getEvents().getFirst().getFormattedMessage());
        assertEquals("RotatingMachineAction rotating-machine-action-4 refers to a non-existing Generator FFR1AA1 _load and will be ignored.",
            appender.getEvents().get(1).getFormattedMessage());
        assertEquals("RotatingMachineAction rotating-machine-action-5 refers to a non-existing Generator FFR1AA1 _load and will be ignored.",
            appender.getEvents().get(2).getFormattedMessage());
        assertEquals("RotatingMachineAction rotating-machine-action-6 has an invalid property reference and will be ignored "
                + "(expected http://energy.referencedata.eu/PropertyReference/RotatingMachine.p, got http://energy.referencedata.eu/PropertyReference/Switch.open).",
            appender.getEvents().get(3).getFormattedMessage());
        assertEquals("StaticPropertyRange static-property-range-7 associated to RotatingMachineAction rotating-machine-action-7 "
                + "has an invalid property reference and will be ignored "
                + "(expected http://energy.referencedata.eu/PropertyReference/RotatingMachine.p, got http://energy.referencedata.eu/PropertyReference/Switch.open).",
            appender.getEvents().get(4).getFormattedMessage());
        assertEquals("StaticPropertyRange static-property-range-8 associated to RotatingMachineAction rotating-machine-action-8 "
                + "has an invalid relative direction kind and value offset kind combination and will be ignored.",
            appender.getEvents().get(5).getFormattedMessage());
        assertEquals("StaticPropertyRange static-property-range-9 associated to RotatingMachineAction rotating-machine-action-9 "
                + "has an invalid relative direction kind and value offset kind combination and will be ignored.",
            appender.getEvents().get(6).getFormattedMessage());
        assertEquals("StaticPropertyRange static-property-range-10 associated to RotatingMachineAction rotating-machine-action-10 "
                + "has an invalid relative direction kind and value offset kind combination and will be ignored.",
            appender.getEvents().get(7).getFormattedMessage());
        assertEquals("StaticPropertyRange static-property-range-11 associated to RotatingMachineAction rotating-machine-action-11 "
                + "has an invalid relative direction kind and value offset kind combination and will be ignored.",
            appender.getEvents().get(8).getFormattedMessage());
        assertEquals("RotatingMachineAction rotating-machine-action-12 refers to a non-existing Generator unknown-rotating-machine and will be ignored.",
            appender.getEvents().get(9).getFormattedMessage());
        assertEquals("RotatingMachineAction rotating-machine-action-13 has no static property range and will be ignored.",
            appender.getEvents().get(10).getFormattedMessage());
        assertEquals("RotatingMachineAction rotating-machine-action-14 has multiple static property ranges and will be ignored.",
            appender.getEvents().get(11).getFormattedMessage());
        assertEquals("RotatingMachineAction rotating-machine-action-15 is not enabled and will be ignored.",
            appender.getEvents().get(12).getFormattedMessage());
    }
}
