/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.test.update;

import com.powsybl.cgmes.conversion.elements.CgmesTopologyKind;
import com.powsybl.cgmes.conversion.extensions.CgmesSvMetadataAdder;
import com.powsybl.cgmes.conversion.extensions.CimCharacteristicsAdder;
import com.powsybl.cgmes.conversion.update.CgmesExportContext;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class CgmesExportContextTest {

    @Test
    public void networkConstructor() {
        Network network = EurostagTutorialExample1Factory.create();

        CgmesExportContext context1 = new CgmesExportContext(network);

        assertEquals(16, context1.getCimVersion());
        assertEquals(CgmesTopologyKind.BUS_BRANCH, context1.getTopologyKind());
        assertEquals(network.getCaseDate(), context1.getScenarioTime());
        assertEquals("SV Model", context1.getSvDescription());
        assertEquals(1, context1.getSvVersion());
        assertTrue(context1.getDependencies().isEmpty());
        assertEquals("powsybl.org", context1.getModelingAuthoritySet());

        network.newExtension(CimCharacteristicsAdder.class)
                .setCimVersion(14)
                .setTopologyKind(CgmesTopologyKind.NODE_BREAKER)
                .add();
        network.newExtension(CgmesSvMetadataAdder.class)
                .setDescription("test")
                .setSvVersion(2)
                .setScenarioTime(network.getCaseDate().toString())
                .addDependency("powsybl.test.org")
                .addDependency("cgmes")
                .setModelingAuthoritySet("cgmes.org")
                .add();

        CgmesExportContext context2 = new CgmesExportContext(network);

        assertEquals(14, context2.getCimVersion());
        assertEquals(CgmesTopologyKind.NODE_BREAKER, context2.getTopologyKind());
        assertEquals(network.getCaseDate(), context2.getScenarioTime());
        assertEquals("test", context2.getSvDescription());
        assertEquals(3, context2.getSvVersion());
        assertEquals(2, context2.getDependencies().size());
        assertTrue(context2.getDependencies().contains("powsybl.test.org"));
        assertTrue(context2.getDependencies().contains("cgmes"));
        assertEquals("cgmes.org", context2.getModelingAuthoritySet());

    }

    @Test
    public void emptyConstructor() {
        CgmesExportContext context = new CgmesExportContext();
        assertEquals(16, context.getCimVersion());
        assertEquals(CgmesTopologyKind.BUS_BRANCH, context.getTopologyKind());
        assertTrue(new Duration(DateTime.now(), context.getScenarioTime()).getStandardMinutes() < 1);
        assertEquals("SV Model", context.getSvDescription());
        assertEquals(1, context.getSvVersion());
        assertTrue(context.getDependencies().isEmpty());
        assertEquals("powsybl.org", context.getModelingAuthoritySet());
        assertFalse(context.exportBoundaryPowerFlows());
    }

    @Test
    public void getSet() {
        CgmesExportContext context = new CgmesExportContext()
                .setCimVersion(14)
                .setTopologyKind(CgmesTopologyKind.NODE_BREAKER)
                .setScenarioTime(DateTime.parse("2020-09-22T17:21:11.381+02:00"))
                .setSvDescription("test")
                .setSvVersion(2)
                .addDependency("powsybl.test.org")
                .addDependency("cgmes")
                .setModelingAuthoritySet("cgmes.org")
                .setExportBoundaryPowerFlows(true);

        assertEquals(14, context.getCimVersion());
        assertEquals(CgmesTopologyKind.NODE_BREAKER, context.getTopologyKind());
        assertEquals(DateTime.parse("2020-09-22T17:21:11.381+02:00"), context.getScenarioTime());
        assertEquals("test", context.getSvDescription());
        assertEquals(2, context.getSvVersion());
        assertEquals(2, context.getDependencies().size());
        assertTrue(context.getDependencies().contains("powsybl.test.org"));
        assertTrue(context.getDependencies().contains("cgmes"));
        assertEquals("cgmes.org", context.getModelingAuthoritySet());
        assertTrue(context.exportBoundaryPowerFlows());

        List<String> dependencies = Arrays.asList("test1", "test2", "test3");
        context.addDependencies(dependencies);
        assertEquals(5, context.getDependencies().size());
        assertTrue(context.getDependencies().containsAll(dependencies));

        context.clearDependencies();
        assertTrue(context.getDependencies().isEmpty());

    }
}
