/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.nc;

import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.ContingencyElementType;
import com.powsybl.nc.reader.ContingencyReader;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Thomas Bouquet {@literal <thomas.bouquet at rte-france.com>}
 */
public class ContingencyReaderTest extends AbstractReaderTest {

    @Test
    void importContingencies() {
        queryManager.read(getResourcePath("/Contingencies.zip"));

        ContingencyReader reader = new ContingencyReader(queryManager, NETWORK);
        List<Contingency> contingencies = reader.readFromProfiles().stream().sorted(Comparator.comparing(Contingency::getId)).toList();

        assertEquals(8, contingencies.size());

        Contingency contingency1 = contingencies.getFirst();
        assertEquals("contingency-1", contingency1.getId());
        assertTrue(contingency1.getName().isPresent());
        assertEquals("CO1", contingency1.getName().get());
        assertEquals(1, contingency1.getElements().size());
        assertEquals("FFR1AA1  FFR2AA1  1", contingency1.getElements().getFirst().getId());
        assertEquals(ContingencyElementType.LINE, contingency1.getElements().getFirst().getType());

        Contingency contingency12 = contingencies.get(1);
        assertEquals("contingency-12", contingency12.getId());
        assertTrue(contingency12.getName().isPresent());
        assertEquals("CO12", contingency12.getName().get());
        assertEquals(1, contingency12.getElements().size());
        assertEquals("FFR1AA1  FFR2AA1  1", contingency12.getElements().getFirst().getId());
        assertEquals(ContingencyElementType.LINE, contingency12.getElements().getFirst().getType());

        Contingency contingency2 = contingencies.get(2);
        assertEquals("contingency-2", contingency2.getId());
        assertTrue(contingency2.getName().isPresent());
        assertEquals("CO2", contingency2.getName().get());
        assertEquals(1, contingency2.getElements().size());
        assertEquals("FFR1AA1  FFR2AA1  1", contingency2.getElements().getFirst().getId());
        assertEquals(ContingencyElementType.LINE, contingency2.getElements().getFirst().getType());

        Contingency contingency3 = contingencies.get(3);
        assertEquals("contingency-3", contingency3.getId());
        assertTrue(contingency3.getName().isPresent());
        assertEquals("CO3", contingency3.getName().get());
        assertEquals(1, contingency3.getElements().size());
        assertEquals("FFR1AA1  FFR2AA1  1", contingency3.getElements().getFirst().getId());
        assertEquals(ContingencyElementType.LINE, contingency3.getElements().getFirst().getType());

        Contingency contingency4 = contingencies.get(4);
        assertEquals("contingency-4", contingency4.getId());
        assertTrue(contingency4.getName().isPresent());
        assertEquals("CO4", contingency4.getName().get());
        assertEquals(2, contingency4.getElements().size());
        assertEquals("FFR1AA1  FFR2AA1  1", contingency4.getElements().getFirst().getId());
        assertEquals(ContingencyElementType.LINE, contingency4.getElements().getFirst().getType());
        assertEquals("FFR1AA1  FFR3AA1  1", contingency4.getElements().getLast().getId());
        assertEquals(ContingencyElementType.LINE, contingency4.getElements().getLast().getType());

        Contingency contingency5 = contingencies.get(5);
        assertEquals("contingency-5", contingency5.getId());
        assertTrue(contingency5.getName().isEmpty());
        assertEquals(1, contingency5.getElements().size());
        assertEquals("FFR1AA1  FFR2AA1  1", contingency5.getElements().getFirst().getId());
        assertEquals(ContingencyElementType.LINE, contingency5.getElements().getFirst().getType());

        Contingency contingency6 = contingencies.get(6);
        assertEquals("contingency-6", contingency6.getId());
        assertTrue(contingency6.getName().isPresent());
        assertEquals("CO6", contingency6.getName().get());
        assertEquals(1, contingency6.getElements().size());
        assertEquals("FFR1AA1  FFR2AA1  1", contingency6.getElements().getFirst().getId());
        assertEquals(ContingencyElementType.LINE, contingency6.getElements().getFirst().getType());

        Contingency contingency7 = contingencies.get(7);
        assertEquals("contingency-7", contingency7.getId());
        assertTrue(contingency7.getName().isEmpty());
        assertEquals(1, contingency7.getElements().size());
        assertEquals("FFR1AA1  FFR2AA1  1", contingency7.getElements().getFirst().getId());
        assertEquals(ContingencyElementType.LINE, contingency7.getElements().getFirst().getType());

        // test logs content
        assertEquals(11, appender.getEvents().size());
        assertEquals("Processing entry RTE_CO.xml.", appender.getEvents().getFirst().getFormattedMessage());
        assertEquals("ContingencyEquipment with equipment FFR1AA1  FFR2AA1  1 associated to Contingency contingency-10 must not be put out of service and will be ignored.",
            appender.getEvents().get(1).getFormattedMessage());
        assertEquals("Contingency contingency-10 does not contain any valid equipment and will be ignored.",
            appender.getEvents().get(2).getFormattedMessage());
        assertEquals("Contingency contingency-9 should not be studied and will be ignored.",
            appender.getEvents().get(3).getFormattedMessage());
        assertEquals("Contingency contingency-8 does not contain any valid equipment and will be ignored.",
            appender.getEvents().get(4).getFormattedMessage());
        assertEquals("ContingencyEquipment with equipment FFR1AA1  FFR4AA1  1 associated to Contingency contingency-12 must not be put out of service and will be ignored.",
            appender.getEvents().get(5).getFormattedMessage());
        assertEquals("ContingencyEquipment with equipment missing-line associated to Contingency contingency-12 refers to a non-existing network element and will be ignored.",
            appender.getEvents().get(6).getFormattedMessage());
        assertEquals("ContingencyEquipment with equipment missing-generator associated to Contingency contingency-12 refers to a non-existing network element and will be ignored.",
            appender.getEvents().get(7).getFormattedMessage());
        assertEquals("ContingencyEquipment with equipment FFR1AA1  FFR3AA1  1 associated to Contingency contingency-12 must not be put out of service and will be ignored.",
            appender.getEvents().get(8).getFormattedMessage());
        assertEquals("ContingencyEquipment with equipment unknown-network-element associated to Contingency contingency-11 refers to a non-existing network element and will be ignored.",
            appender.getEvents().get(9).getFormattedMessage());
        assertEquals("Contingency contingency-11 does not contain any valid equipment and will be ignored.",
            appender.getEvents().get(10).getFormattedMessage());
    }
}
