/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.nc;

import com.powsybl.action.SwitchAction;
import com.powsybl.nc.reader.TopologyActionReader;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Thomas Bouquet {@literal <thomas.bouquet at rte-france.com>}
 */
public class TopologyActionReaderTest extends AbstractReaderTest {

    @Test
    void importTopologicalActions() {
        queryManager.read(new ZipInputStream(Objects.requireNonNull(getClass().getResourceAsStream("/SwitchActions.zip"))));

        TopologyActionReader reader = new TopologyActionReader(queryManager, NETWORK);
        List<SwitchAction> switchActions = reader.readFromProfiles().stream().sorted(Comparator.comparing(SwitchAction::getId)).toList();

        assertEquals(2, switchActions.size());

        SwitchAction switchAction1 = switchActions.getFirst();
        assertEquals("topology-action-1", switchAction1.getId());
        assertEquals("BBE1AA1  BBE4AA1  1", switchAction1.getSwitchId());
        assertTrue(switchAction1.isOpen());

        SwitchAction switchAction2 = switchActions.getLast();
        assertEquals("topology-action-2", switchAction2.getId());
        assertEquals("DDE3AA1  DDE4AA1  1", switchAction2.getSwitchId());
        assertFalse(switchAction2.isOpen());

        // test logs content
        assertEquals(10, appender.getEvents().size());
        assertEquals("Processing entry RTE_RA.xml.",
            appender.getEvents().getFirst().getFormattedMessage());
        assertEquals("TopologyAction topology-action-3 refers to a non-existing Switch unknown-switch and will be ignored.",
            appender.getEvents().get(1).getFormattedMessage());
        assertEquals("TopologyAction topology-action-4 is not enabled and will be ignored.",
            appender.getEvents().get(2).getFormattedMessage());
        assertEquals("TopologyAction topology-action-5 has an invalid property reference and will be ignored "
                + "(expected http://energy.referencedata.eu/PropertyReference/Switch.open, got http://energy.referencedata.eu/PropertyReference/RotatingMachine.p).",
            appender.getEvents().get(3).getFormattedMessage());
        assertEquals("StaticPropertyRange static-property-range-6 associated to TopologyAction topology-action-6 has an invalid property reference and will be ignored "
                + "(expected http://energy.referencedata.eu/PropertyReference/Switch.open, got http://energy.referencedata.eu/PropertyReference/RotatingMachine.p).",
            appender.getEvents().get(4).getFormattedMessage());
        assertEquals("StaticPropertyRange static-property-range-7 associated to TopologyAction topology-action-7 has an invalid relative direction kind and will be ignored "
                + "(expected http://entsoe.eu/ns/nc#RelativeDirectionKind.none, got http://entsoe.eu/ns/nc#RelativeDirectionKind.up).",
            appender.getEvents().get(5).getFormattedMessage());
        assertEquals("StaticPropertyRange static-property-range-8 associated to TopologyAction topology-action-8 has an invalid value offset kind and will be ignored "
                + "(expected http://entsoe.eu/ns/nc#ValueOffsetKind.absolute, got http://entsoe.eu/ns/nc#ValueOffsetKind.incrementalPercentage).",
            appender.getEvents().get(6).getFormattedMessage());
        assertEquals("TopologyAction topology-action-9 has no static property range and will be ignored.",
            appender.getEvents().get(7).getFormattedMessage());
        assertEquals("StaticPropertyRange static-property-range-10 associated to TopologyAction topology-action-10 has an invalid normal value and will be ignored (expected 0 or 1, got 2).",
            appender.getEvents().get(8).getFormattedMessage());
        assertEquals("TopologyAction topology-action-11 has multiple static property ranges and will be ignored.",
            appender.getEvents().get(9).getFormattedMessage());
    }
}
