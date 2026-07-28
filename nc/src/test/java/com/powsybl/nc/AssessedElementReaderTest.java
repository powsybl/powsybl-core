/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.nc;

import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.ContingencyContextType;
import com.powsybl.iidm.network.Branch;
import com.powsybl.nc.reader.AssessedElementReader;
import com.powsybl.nc.reader.ContingencyReader;
import com.powsybl.security.monitor.StateMonitor;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Thomas Bouquet {@literal <thomas.bouquet at rte-france.com>}
 */
public class AssessedElementReaderTest extends AbstractReaderTest {

    @Test
    void importAssessedElements() {
        queryManager.read(getResourcePath("/AssessedElements.zip"));

        ContingencyReader reader = new ContingencyReader(queryManager, importerContext, NETWORK);
        List<Contingency> contingencies = reader.readFromProfiles().stream().sorted(Comparator.comparing(Contingency::getId)).toList();

        AssessedElementReader assessedElementReader = new AssessedElementReader(queryManager, importerContext, NETWORK, new HashSet<>(contingencies));
        Set<StateMonitor> stateMonitors = assessedElementReader.readFromProfiles();

        assertEquals(3, stateMonitors.size());

        StateMonitor preventiveStateMonitor = stateMonitors.stream().filter(s -> s.getContingencyContext().getContingencyId() == null).findFirst().orElseThrow();
        assertNull(preventiveStateMonitor.getContingencyContext().getContingencyId());
        assertEquals(ContingencyContextType.NONE, preventiveStateMonitor.getContingencyContext().getContextType());
        assertEquals(Set.of("FFR1AA1  FFR2AA1  1", "FFR2AA1  FFR3AA1  1"), preventiveStateMonitor.getBranchIds());

        StateMonitor curative1StateMonitor = stateMonitors.stream().filter(s -> "contingency-1".equals(s.getContingencyContext().getContingencyId())).findFirst().orElseThrow();
        assertEquals("contingency-1", curative1StateMonitor.getContingencyContext().getContingencyId());
        assertEquals(ContingencyContextType.SPECIFIC, curative1StateMonitor.getContingencyContext().getContextType());
        assertEquals(Set.of("FFR3AA1  FFR5AA1  1", "FFR2AA1  FFR3AA1  1"), curative1StateMonitor.getBranchIds());

        StateMonitor curative2StateMonitor = stateMonitors.stream().filter(s -> "contingency-2".equals(s.getContingencyContext().getContingencyId())).findFirst().orElseThrow();
        assertEquals("contingency-2", curative2StateMonitor.getContingencyContext().getContingencyId());
        assertEquals(ContingencyContextType.SPECIFIC, curative2StateMonitor.getContingencyContext().getContextType());
        assertEquals(Set.of("FFR2AA1  FFR3AA1  1"), curative2StateMonitor.getBranchIds());

        // test logs content
        assertEquals(9, appender.getEvents().size());
        assertEquals("Processing entry RTE_AE.xml.", appender.getEvents().getFirst().getFormattedMessage());
        assertEquals("Processing entry RTE_CO.xml.", appender.getEvents().get(1).getFormattedMessage());
        assertEquals("Association between Contingency contingency-2 and AssessedElement assessed-element-2 is disabled. "
                + "The branch FFR3AA1  FFR5AA1  1 will not be monitored for this contingency.",
            appender.getEvents().get(2).getFormattedMessage());
        assertEquals("Contingency contingency-3 associated to AssessedElement assessed-element-3 was not imported. "
                + "The branch FFR2AA1  FFR3AA1  1 will not be monitored for this contingency.",
            appender.getEvents().get(3).getFormattedMessage());
        assertEquals("AssessedElement assessed-element-4 is disabled and will be ignored.",
            appender.getEvents().get(4).getFormattedMessage());
        assertEquals("AssessedElement assessed-element-5 has no conductingEquipment and will be ignored.",
            appender.getEvents().get(5).getFormattedMessage());
        assertEquals("Association between Contingency contingency-1 and AssessedElement assessed-element-6 is not included. "
                + "The branch FFR1AA1  FFR4AA1  1 will not be monitored for this contingency.",
            appender.getEvents().get(6).getFormattedMessage());
        assertEquals("No Contingency associated to AssessedElement assessed-element-6 was properly imported. "
                + "The branch FFR1AA1  FFR4AA1  1 will not be monitored in curative.",
            appender.getEvents().get(7).getFormattedMessage());
        assertEquals("ConductingEquipment FFR1AA1 _generator belonging to AssessedElement assessed-element-7 is not a branch. "
                + "AssessedElement will be ignored.",
            appender.getEvents().get(8).getFormattedMessage());

        // check context
        assertEquals(3, importerContext.getImportedAssessedElements().size());
        checkAssessedElementContext("assessed-element-1", NETWORK.getBranch("FFR1AA1  FFR2AA1  1"), true, Set.of());
        checkAssessedElementContext("assessed-element-2", NETWORK.getBranch("FFR3AA1  FFR5AA1  1"), false, Set.of("contingency-1"));
        checkAssessedElementContext("assessed-element-3", NETWORK.getBranch("FFR2AA1  FFR3AA1  1"), true, Set.of("contingency-1", "contingency-2"));
    }

    private void checkAssessedElementContext(String assessedElementId, Branch<?> branch, boolean inBaseCase, Set<String> contingencies) {
        ImporterContext.AssessedElementContext expectedContext = new ImporterContext.AssessedElementContext(
            assessedElementId, branch, inBaseCase, contingencies
        );
        assertEquals(expectedContext, importerContext.getImportedAssessedElements().get(assessedElementId));
    }
}
