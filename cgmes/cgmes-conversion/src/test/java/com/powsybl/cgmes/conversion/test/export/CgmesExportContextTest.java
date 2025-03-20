/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test.export;

import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.conversion.export.CgmesExportContext;
import com.powsybl.cgmes.extensions.CgmesTopologyKind;
import com.powsybl.cgmes.model.CgmesNamespace;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import java.time.ZonedDateTime;
import java.time.Duration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
class CgmesExportContextTest {

    @Test
    void testExporter() {
        var exporter = new CgmesExport();
        assertEquals("ENTSO-E CGMES version 2.4.15", exporter.getComment());
        assertEquals(21, exporter.getParameters().size());
    }

    @Test
    void networkConstructor() {
        Network network = EurostagTutorialExample1Factory.create();

        CgmesExportContext context1 = new CgmesExportContext(network);

        assertEquals(16, context1.getCimVersion());
        assertEquals(CgmesNamespace.CIM_16_NAMESPACE, context1.getCim().getNamespace());
        assertEquals(CgmesTopologyKind.BUS_BRANCH, context1.getTopologyKind());
        assertEquals(network.getCaseDate(), context1.getScenarioTime());
        assertEquals("1D", context1.getBusinessProcess());
    }

    @Test
    void emptyConstructor() {
        CgmesExportContext context = new CgmesExportContext();
        assertEquals(16, context.getCimVersion());
        assertEquals(CgmesNamespace.CIM_16_NAMESPACE, context.getCim().getNamespace());
        assertEquals(CgmesTopologyKind.NODE_BREAKER, context.getTopologyKind());
        assertTrue(Duration.between(ZonedDateTime.now(), context.getScenarioTime()).toMinutes() < 1);
        assertTrue(context.exportBoundaryPowerFlows());
        assertEquals("1D", context.getBusinessProcess());
    }
}
