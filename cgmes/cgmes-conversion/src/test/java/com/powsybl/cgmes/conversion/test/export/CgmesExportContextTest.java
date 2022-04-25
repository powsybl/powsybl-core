/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.test.export;

import com.powsybl.cgmes.conformity.test.CgmesConformity1Catalog;
import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.export.CgmesExportContext;
import com.powsybl.cgmes.extensions.CgmesIidmMapping;
import com.powsybl.cgmes.extensions.CgmesSvMetadataAdder;
import com.powsybl.cgmes.extensions.CgmesTopologyKind;
import com.powsybl.cgmes.extensions.CimCharacteristicsAdder;
import com.powsybl.cgmes.model.CgmesNamespace;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class CgmesExportContextTest {

    @Test
    public void testExporter() {
        var exporter = new CgmesExport();
        assertEquals("ENTSO-E CGMES version 2.4.15", exporter.getComment());
        assertEquals(6, exporter.getParameters().size());
    }

    @Test
    public void networkConstructor() {
        Network network = EurostagTutorialExample1Factory.create();

        CgmesExportContext context1 = new CgmesExportContext(network);

        assertEquals(16, context1.getCimVersion());
        assertEquals(CgmesNamespace.CIM_16_NAMESPACE, context1.getCim().getNamespace());
        assertEquals(CgmesTopologyKind.BUS_BRANCH, context1.getTopologyKind());
        assertEquals(network.getCaseDate(), context1.getScenarioTime());
        assertEquals("SV Model", context1.getSvModelDescription().getDescription());
        assertEquals(1, context1.getSvModelDescription().getVersion());
        assertTrue(context1.getSvModelDescription().getDependencies().isEmpty());
        assertEquals("powsybl.org", context1.getSvModelDescription().getModelingAuthoritySet());

        network.newExtension(CimCharacteristicsAdder.class)
            .setCimVersion(14)
            .setTopologyKind(CgmesTopologyKind.NODE_BREAKER)
            .add();
        network.newExtension(CgmesSvMetadataAdder.class)
            .setDescription("test")
            .setSvVersion(2)
            .addDependency("powsybl.test.org")
            .addDependency("cgmes")
            .setModelingAuthoritySet("cgmes.org")
            .add();

        CgmesExportContext context2 = new CgmesExportContext(network);

        assertEquals(14, context2.getCimVersion());
        assertEquals(CgmesNamespace.CIM_14_NAMESPACE, context2.getCim().getNamespace());
        assertEquals(CgmesTopologyKind.NODE_BREAKER, context2.getTopologyKind());
        assertEquals(network.getCaseDate(), context2.getScenarioTime());
        assertEquals("test", context2.getSvModelDescription().getDescription());
        assertEquals(3, context2.getSvModelDescription().getVersion());
        assertEquals(2, context2.getSvModelDescription().getDependencies().size());
        assertTrue(context2.getSvModelDescription().getDependencies().contains("powsybl.test.org"));
        assertTrue(context2.getSvModelDescription().getDependencies().contains("cgmes"));
        assertEquals("cgmes.org", context2.getSvModelDescription().getModelingAuthoritySet());
    }

    @Test
    public void emptyConstructor() {
        CgmesExportContext context = new CgmesExportContext();
        assertEquals(16, context.getCimVersion());
        assertEquals(CgmesNamespace.CIM_16_NAMESPACE, context.getCim().getNamespace());
        assertEquals(CgmesTopologyKind.BUS_BRANCH, context.getTopologyKind());
        assertTrue(new Duration(DateTime.now(), context.getScenarioTime()).getStandardMinutes() < 1);
        assertEquals("SV Model", context.getSvModelDescription().getDescription());
        assertEquals(1, context.getSvModelDescription().getVersion());
        assertTrue(context.getSvModelDescription().getDependencies().isEmpty());
        assertEquals("powsybl.org", context.getSvModelDescription().getModelingAuthoritySet());
        assertTrue(context.exportBoundaryPowerFlows());
    }

    @Test
    public void getSet() {
        CgmesExportContext context = new CgmesExportContext()
            .setCimVersion(14)
            .setTopologyKind(CgmesTopologyKind.NODE_BREAKER)
            .setScenarioTime(DateTime.parse("2020-09-22T17:21:11.381+02:00"))
            .setExportBoundaryPowerFlows(true)
            .setExportFlowsForSwitches(false);
        context.getSvModelDescription()
            .setDescription("test")
            .setVersion(2)
            .addDependency("powsybl.test.org")
            .addDependency("cgmes")
            .setModelingAuthoritySet("cgmes.org");

        assertEquals(14, context.getCimVersion());
        assertEquals(CgmesNamespace.CIM_14_NAMESPACE, context.getCim().getNamespace());
        assertEquals(CgmesTopologyKind.NODE_BREAKER, context.getTopologyKind());
        assertEquals(DateTime.parse("2020-09-22T17:21:11.381+02:00"), context.getScenarioTime());
        assertEquals("test", context.getSvModelDescription().getDescription());
        assertEquals(2, context.getSvModelDescription().getVersion());
        assertEquals(2, context.getSvModelDescription().getDependencies().size());
        assertTrue(context.getSvModelDescription().getDependencies().contains("powsybl.test.org"));
        assertTrue(context.getSvModelDescription().getDependencies().contains("cgmes"));
        assertEquals("cgmes.org", context.getSvModelDescription().getModelingAuthoritySet());
        assertTrue(context.exportBoundaryPowerFlows());

        List<String> dependencies = Arrays.asList("test1", "test2", "test3");
        context.getSvModelDescription().addDependencies(dependencies);
        assertEquals(5, context.getSvModelDescription().getDependencies().size());
        assertTrue(context.getSvModelDescription().getDependencies().containsAll(dependencies));

        context.getSvModelDescription().clearDependencies();
        assertTrue(context.getSvModelDescription().getDependencies().isEmpty());
    }

    @Test
    public void testBuildIidmMappingAndChangingSwitchStatus() {
        Network n = buildIidmMapping();

        // Change switch status
        n.getSwitch("9550e743-98fd-4be7-848f-b6a600d6c67b").setOpen(true);

        // Check that topology mapping has been invalidated
        assertTrue(n.getExtension(CgmesIidmMapping.class).isTopologicalNodeEmpty());
    }

    @Test
    public void testBuildIidmMappingAndCreatingSwitch() {
        Network n = buildIidmMapping();

        // Create switch
        n.getVoltageLevel("04636548-c766-11e1-8775-005056c00008").getNodeBreakerView().newSwitch().setId("test").setNode1(0).setNode2(2).setOpen(false).setKind(SwitchKind.BREAKER).add();

        // Check that topology mapping has been invalidated
        assertTrue(n.getExtension(CgmesIidmMapping.class).isTopologicalNodeEmpty());
    }

    @Test
    public void testBuildIidmMappingAndCreatingInternalConnection() {
        Network n = buildIidmMapping();

        // Create internal connection
        n.getVoltageLevel("04636548-c766-11e1-8775-005056c00008").getNodeBreakerView().newInternalConnection().setNode1(0).setNode2(2).add();

        // Check that topology mapping has been invalidated
        assertTrue(n.getExtension(CgmesIidmMapping.class).isTopologicalNodeEmpty());
    }

    @Test
    public void testBuildIidmMappingAndDeletingSwitch() {
        Network n = buildIidmMapping();

        // Delete switch
        n.getVoltageLevel("04636548-c766-11e1-8775-005056c00008").getNodeBreakerView().removeSwitch("9550e743-98fd-4be7-848f-b6a600d6c67b");

        // Check that topology mapping has been invalidated
        assertTrue(n.getExtension(CgmesIidmMapping.class).isTopologicalNodeEmpty());
    }

    @Test
    public void testBuildIidmMappingAndDeletingSwitches() {
        Network n = buildIidmMapping();

        // Delete voltage level (containing switches)
        VoltageLevel vl = n.getVoltageLevel("04636548-c766-11e1-8775-005056c00008");
        vl.getLines().forEach(Connectable::remove);
        vl.remove();

        // Check that topology mapping has been invalidated
        assertTrue(n.getExtension(CgmesIidmMapping.class).isTopologicalNodeEmpty());
    }

    private static Network buildIidmMapping() {
        ReadOnlyDataSource ds = CgmesConformity1Catalog.smallNodeBreaker().dataSource();

        // Import without creating mappings
        Properties ip = new Properties();
        ip.put("iidm.import.cgmes.create-cgmes-export-mapping", "false");
        Network n = new CgmesImport().importData(ds, NetworkFactory.findDefault(), ip);
        CgmesExportContext context = new CgmesExportContext(n, true);
        assertNotNull(n.getExtension(CgmesIidmMapping.class));

        for (Bus bus : n.getBusView().getBuses()) {
            assertNotNull(context.getTopologicalNodesByBusViewBus(bus.getId()));
        }

        for (VoltageLevel voltageLevel : n.getVoltageLevels()) {
            assertNotNull(context.getBaseVoltageByNominalVoltage(voltageLevel.getNominalV()));
        }

        assertFalse(n.getExtension(CgmesIidmMapping.class).isTopologicalNodeEmpty());
        return n;
    }
}
