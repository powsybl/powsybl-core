/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.nc;

import com.powsybl.action.ShuntCompensatorPositionAction;
import com.powsybl.nc.reader.ShuntCompensatorModificationReader;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Thomas Bouquet {@literal <thomas.bouquet at rte-france.com>}
 */
public class ShuntCompensatorModificationReaderTest extends AbstractReaderTest {

    @Test
    void importShuntCompensatorModifications() {
        queryManager.read(new ZipInputStream(Objects.requireNonNull(getClass().getResourceAsStream("/ShuntCompensatorModifications.zip"))));

        ShuntCompensatorModificationReader reader = new ShuntCompensatorModificationReader(queryManager, NETWORK);
        List<ShuntCompensatorPositionAction> shuntCompensatorPositionActions = reader.readFromProfiles().stream().sorted(Comparator.comparing(ShuntCompensatorPositionAction::getId)).toList();

        assertEquals(1, shuntCompensatorPositionActions.size());

        ShuntCompensatorPositionAction shuntCompensatorPositionAction = shuntCompensatorPositionActions.getFirst();
        assertEquals("shunt-compensator-modification-1", shuntCompensatorPositionAction.getId());
        assertEquals("shunt-compensator", shuntCompensatorPositionAction.getShuntCompensatorId());
        assertEquals(3, shuntCompensatorPositionAction.getSectionCount());

        // test logs content
        assertEquals(22, appender.getEvents().size());
        assertEquals("Processing entry RTE_RA.xml.", appender.getEvents().getFirst().getFormattedMessage());
        assertEquals("StaticPropertyRange static-property-range-2 associated to ShuntCompensatorModification shunt-compensator-modification-2 has an invalid relative direction kind and will be ignored (expected http://entsoe.eu/ns/nc#RelativeDirectionKind.none, got http://entsoe.eu/ns/nc#RelativeDirectionKind.up).", appender.getEvents().get(1).getFormattedMessage());
        assertEquals("StaticPropertyRange static-property-range-3 associated to ShuntCompensatorModification shunt-compensator-modification-3 has an invalid relative direction kind and will be ignored (expected http://entsoe.eu/ns/nc#RelativeDirectionKind.none, got http://entsoe.eu/ns/nc#RelativeDirectionKind.down).", appender.getEvents().get(2).getFormattedMessage());
        assertEquals("StaticPropertyRange static-property-range-4 associated to ShuntCompensatorModification shunt-compensator-modification-4 has an invalid relative direction kind and will be ignored (expected http://entsoe.eu/ns/nc#RelativeDirectionKind.none, got http://entsoe.eu/ns/nc#RelativeDirectionKind.up).", appender.getEvents().get(3).getFormattedMessage());
        assertEquals("StaticPropertyRange static-property-range-5 associated to ShuntCompensatorModification shunt-compensator-modification-5 has an invalid relative direction kind and will be ignored (expected http://entsoe.eu/ns/nc#RelativeDirectionKind.none, got http://entsoe.eu/ns/nc#RelativeDirectionKind.down).", appender.getEvents().get(4).getFormattedMessage());
        assertEquals("ShuntCompensatorModification shunt-compensator-modification-6 has an invalid property reference and will be ignored (expected http://energy.referencedata.eu/PropertyReference/ShuntCompensator.sections, got http://energy.referencedata.eu/PropertyReference/Switch.open).", appender.getEvents().get(5).getFormattedMessage());
        assertEquals("StaticPropertyRange static-property-range-7 associated to ShuntCompensatorModification shunt-compensator-modification-7 has an invalid property reference and will be ignored (expected http://energy.referencedata.eu/PropertyReference/ShuntCompensator.sections, got http://energy.referencedata.eu/PropertyReference/Switch.open).", appender.getEvents().get(6).getFormattedMessage());
        assertEquals("StaticPropertyRange static-property-range-8 associated to ShuntCompensatorModification shunt-compensator-modification-8 has an invalid relative direction kind and will be ignored (expected http://entsoe.eu/ns/nc#RelativeDirectionKind.none, got http://entsoe.eu/ns/nc#RelativeDirectionKind.up).", appender.getEvents().get(7).getFormattedMessage());
        assertEquals("StaticPropertyRange static-property-range-9 associated to ShuntCompensatorModification shunt-compensator-modification-9 has an invalid relative direction kind and will be ignored (expected http://entsoe.eu/ns/nc#RelativeDirectionKind.none, got http://entsoe.eu/ns/nc#RelativeDirectionKind.down).", appender.getEvents().get(8).getFormattedMessage());
        assertEquals("StaticPropertyRange static-property-range-10 associated to ShuntCompensatorModification shunt-compensator-modification-10 has an invalid value offset kind and will be ignored (expected http://entsoe.eu/ns/nc#ValueOffsetKind.absolute, got http://entsoe.eu/ns/nc#ValueOffsetKind.incremental).", appender.getEvents().get(9).getFormattedMessage());
        assertEquals("StaticPropertyRange static-property-range-11 associated to ShuntCompensatorModification shunt-compensator-modification-11 has an invalid value offset kind and will be ignored (expected http://entsoe.eu/ns/nc#ValueOffsetKind.absolute, got http://entsoe.eu/ns/nc#ValueOffsetKind.incrementalPercentage).", appender.getEvents().get(10).getFormattedMessage());
        assertEquals("ShuntCompensatorModification shunt-compensator-modification-12 refers to a non-existing shunt compensator unknown-shunt-compensator and will be ignored.", appender.getEvents().get(11).getFormattedMessage());
        assertEquals("ShuntCompensatorModification shunt-compensator-modification-13 has no static property range and will be ignored.", appender.getEvents().get(12).getFormattedMessage());
        assertEquals("ShuntCompensatorModification shunt-compensator-modification-14 has multiple static property ranges and will be ignored.", appender.getEvents().get(13).getFormattedMessage());
        assertEquals("ShuntCompensatorModification shunt-compensator-modification-15 is not enabled and will be ignored.", appender.getEvents().get(14).getFormattedMessage());
        assertEquals("StaticPropertyRange static-property-range-16 associated to ShuntCompensatorModification shunt-compensator-modification-16 has an invalid normal value and will be ignored (expected positive integer, got 3.5).", appender.getEvents().get(15).getFormattedMessage());
        assertEquals("StaticPropertyRange static-property-range-17 associated to ShuntCompensatorModification shunt-compensator-modification-17 has an invalid relative direction kind and will be ignored (expected http://entsoe.eu/ns/nc#RelativeDirectionKind.none, got http://entsoe.eu/ns/nc#RelativeDirectionKind.up).", appender.getEvents().get(16).getFormattedMessage());
        assertEquals("StaticPropertyRange static-property-range-18 associated to ShuntCompensatorModification shunt-compensator-modification-18 has an invalid relative direction kind and will be ignored (expected http://entsoe.eu/ns/nc#RelativeDirectionKind.none, got http://entsoe.eu/ns/nc#RelativeDirectionKind.down).", appender.getEvents().get(17).getFormattedMessage());
        assertEquals("StaticPropertyRange static-property-range-19 associated to ShuntCompensatorModification shunt-compensator-modification-19 has an invalid relative direction kind and will be ignored (expected http://entsoe.eu/ns/nc#RelativeDirectionKind.none, got http://entsoe.eu/ns/nc#RelativeDirectionKind.up).", appender.getEvents().get(18).getFormattedMessage());
        assertEquals("StaticPropertyRange static-property-range-20 associated to ShuntCompensatorModification shunt-compensator-modification-20 has an invalid relative direction kind and will be ignored (expected http://entsoe.eu/ns/nc#RelativeDirectionKind.none, got http://entsoe.eu/ns/nc#RelativeDirectionKind.down).", appender.getEvents().get(19).getFormattedMessage());
        assertEquals("StaticPropertyRange static-property-range-21 associated to ShuntCompensatorModification shunt-compensator-modification-21 has an invalid normal value and will be ignored (expected positive integer, got -5).", appender.getEvents().get(20).getFormattedMessage());
        assertEquals("StaticPropertyRange static-property-range-22 associated to ShuntCompensatorModification shunt-compensator-modification-22 has an invalid relative direction kind and will be ignored (expected http://entsoe.eu/ns/nc#RelativeDirectionKind.none, got http://entsoe.eu/ns/nc#RelativeDirectionKind.down).", appender.getEvents().get(21).getFormattedMessage());
    }
}
