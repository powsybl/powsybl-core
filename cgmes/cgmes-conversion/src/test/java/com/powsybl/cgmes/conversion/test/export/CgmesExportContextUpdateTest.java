/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.test.export;

import com.powsybl.cgmes.conformity.test.CgmesConformity1Catalog;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.export.CgmesExportContext;
import com.powsybl.cgmes.extensions.CgmesIidmMapping;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.network.*;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class CgmesExportContextUpdateTest {

    @Test
    public void testBuildNodeBreakerIidmMappingAndChangingSwitchStatus() {
        Network n = buildNodeBreakerNetwork();
        createNodeBreakerBus(n);
        CgmesExportContext context = buildIidmMapping(n);

        // Check that the topology mapping of the buses associated with the switch exists, both buses are mapped in a single busView
        assertNotNull(n.getExtension(CgmesIidmMapping.class).getTopologicalNodes("_04636548-c766-11e1-8775-005056c00008_0"));
        assertNull(n.getExtension(CgmesIidmMapping.class).getTopologicalNodes("_04636548-c766-11e1-8775-005056c00008_34"));

        // Change switch status
        n.getSwitch("testSwitch").setOpen(true);

        // Check that the topology mapping of the buses associated with the switch has been invalidated
        assertNull(n.getExtension(CgmesIidmMapping.class).getTopologicalNodes("_04636548-c766-11e1-8775-005056c00008_0"));
        assertNull(n.getExtension(CgmesIidmMapping.class).getTopologicalNodes("_04636548-c766-11e1-8775-005056c00008_34"));

        // Check that the topology mapping of the buses associated with the switch exists, each bus is mapped in a different busView
        context.updateTopologicalNodesMapping(n);
        assertNotNull(n.getExtension(CgmesIidmMapping.class).getTopologicalNodes("_04636548-c766-11e1-8775-005056c00008_0"));
        assertNotNull(n.getExtension(CgmesIidmMapping.class).getTopologicalNodes("_04636548-c766-11e1-8775-005056c00008_34"));
    }

    @Test
    public void testBuildBusBranchIidmMappingAndChangingSwitchStatus() {
        Network n = buildBusBranchNetwork();
        createBusBranchBus(n);
        CgmesExportContext context = buildIidmMapping(n);

        // Check that the topology mapping of the buses associated with the switch exists, both buses are mapped in a single busView
        assertNotNull(n.getExtension(CgmesIidmMapping.class).getTopologicalNodes("_04636548-c766-11e1-8775-005056c00008_0"));
        assertNull(n.getExtension(CgmesIidmMapping.class).getTopologicalNodes("_04636548-c766-11e1-8775-005056c00008_1"));

        // Change switch status
        n.getSwitch("testSwitch").setOpen(true);

        // Check that the topology mapping of the buses associated with the switch has been invalidated
        assertNull(n.getExtension(CgmesIidmMapping.class).getTopologicalNodes("_04636548-c766-11e1-8775-005056c00008_0"));
        assertNull(n.getExtension(CgmesIidmMapping.class).getTopologicalNodes("_04636548-c766-11e1-8775-005056c00008_1"));

        // Check that the topology mapping of the buses associated with the switch exists, each bus is mapped in a different busView
        context.updateTopologicalNodesMapping(n);
        assertNotNull(n.getExtension(CgmesIidmMapping.class).getTopologicalNodes("_04636548-c766-11e1-8775-005056c00008_0"));
        assertNotNull(n.getExtension(CgmesIidmMapping.class).getTopologicalNodes("_04636548-c766-11e1-8775-005056c00008_1"));
    }

    @Test
    public void testBuildNodeBreakerIidmMappingAndCreatingBus() {
        Network n = buildNodeBreakerNetwork();
        CgmesExportContext context = buildIidmMapping(n);

        // Check that the bus topology mapping exists
        assertNotNull(n.getExtension(CgmesIidmMapping.class).getTopologicalNodes("_04636548-c766-11e1-8775-005056c00008_0"));

        // Create a Bus with a Load
        createNodeBreakerBus(n);

        // Check that the bus topology mapping has been invalidated
        assertNull(n.getExtension(CgmesIidmMapping.class).getTopologicalNodes("_04636548-c766-11e1-8775-005056c00008_0"));

        // Check that buses topology mapping exists again, both buses are mapped in a single busView
        context.updateTopologicalNodesMapping(n);
        assertNotNull(n.getExtension(CgmesIidmMapping.class).getTopologicalNodes("_04636548-c766-11e1-8775-005056c00008_0"));
        assertNull(n.getExtension(CgmesIidmMapping.class).getTopologicalNodes("_04636548-c766-11e1-8775-005056c00008_34"));
    }

    @Test
    public void testBuildBusBranchIidmMappingAndCreatingBus() {
        Network n = buildBusBranchNetwork();
        CgmesExportContext context = buildIidmMapping(n);

        // Check that the bus topology mapping exists
        assertNotNull(n.getExtension(CgmesIidmMapping.class).getTopologicalNodes("_04636548-c766-11e1-8775-005056c00008_0"));

        // Create a Bus with a Load
        createBusBranchBus(n);

        // Check that the bus topology mapping has been invalidated
        assertNull(n.getExtension(CgmesIidmMapping.class).getTopologicalNodes("_04636548-c766-11e1-8775-005056c00008_0"));

        // Check that buses topology mapping exists again, both buses are mapped in a single busView
        context.updateTopologicalNodesMapping(n);
        assertNotNull(n.getExtension(CgmesIidmMapping.class).getTopologicalNodes("_04636548-c766-11e1-8775-005056c00008_0"));
        assertNull(n.getExtension(CgmesIidmMapping.class).getTopologicalNodes("_04636548-c766-11e1-8775-005056c00008_1"));
    }

    @Test
    public void testBuildNodeBreakerIidmMappingAndCreatingSwitch() {
        Network n = buildNodeBreakerNetwork();
        CgmesExportContext context = buildIidmMapping(n);

        // Check that bus topology mapping exists again
        assertNotNull(n.getExtension(CgmesIidmMapping.class).getTopologicalNodes("_04636548-c766-11e1-8775-005056c00008_0"));

        // Create switch
        n.getVoltageLevel("_04636548-c766-11e1-8775-005056c00008").getNodeBreakerView().newSwitch().setId("test").setNode1(0).setNode2(2).setOpen(false).setKind(SwitchKind.BREAKER).add();

        // Check that the bus topology mapping has been invalidated
        assertNull(n.getExtension(CgmesIidmMapping.class).getTopologicalNodes("_04636548-c766-11e1-8775-005056c00008_0"));

        // Check that bus topology mapping exists again
        context.updateTopologicalNodesMapping(n);
        assertNotNull(n.getExtension(CgmesIidmMapping.class).getTopologicalNodes("_04636548-c766-11e1-8775-005056c00008_0"));
    }

    @Test
    public void testBuildNodeBreakerIidmMappingAndCreatingInternalConnection() {
        Network n = buildNodeBreakerNetwork();
        CgmesExportContext context = buildIidmMapping(n);

        // Check that bus topology mapping exists again
        assertNotNull(n.getExtension(CgmesIidmMapping.class).getTopologicalNodes("_04636548-c766-11e1-8775-005056c00008_0"));

        // Create internal connection
        n.getVoltageLevel("_04636548-c766-11e1-8775-005056c00008").getNodeBreakerView().newInternalConnection().setNode1(0).setNode2(2).add();

        // Check that the bus topology mapping has been invalidated
        assertNull(n.getExtension(CgmesIidmMapping.class).getTopologicalNodes("_04636548-c766-11e1-8775-005056c00008_0"));

        // Check that bus topology mapping exists again
        context.updateTopologicalNodesMapping(n);
        assertNotNull(n.getExtension(CgmesIidmMapping.class).getTopologicalNodes("_04636548-c766-11e1-8775-005056c00008_0"));
    }

    @Test
    public void testBuildNodeBreakerIidmMappingAndDeletingInternalConnection() {
        Network n = buildNodeBreakerNetwork();
        CgmesExportContext context = buildIidmMapping(n);

        // Check that bus topology mapping exists again
        assertNotNull(n.getExtension(CgmesIidmMapping.class).getTopologicalNodes("_04636548-c766-11e1-8775-005056c00008_0"));

        // Delete internal connection
        n.getVoltageLevel("_04636548-c766-11e1-8775-005056c00008").getNodeBreakerView().removeInternalConnections(0, 10);

        // Check that the bus topology mapping has been invalidated
        assertNull(n.getExtension(CgmesIidmMapping.class).getTopologicalNodes("_04636548-c766-11e1-8775-005056c00008_0"));

        // Check that bus topology mapping exists again
        context.updateTopologicalNodesMapping(n);
        assertNotNull(n.getExtension(CgmesIidmMapping.class).getTopologicalNodes("_04636548-c766-11e1-8775-005056c00008_0"));
    }

    @Test
    public void testBuildNodeBreakerIidmMappingAndDeletingBusbarSection() {
        Network n = buildNodeBreakerNetwork();
        CgmesExportContext context = buildIidmMapping(n);

        // Check that bus topology mapping exists again
        assertNotNull(n.getExtension(CgmesIidmMapping.class).getTopologicalNodes("_044f4102-c766-11e1-8775-005056c00008_0"));

        // Delete BusbarSection
        n.getVoltageLevel("_044f4102-c766-11e1-8775-005056c00008").getNodeBreakerView().getBusbarSection("_d18227ba-b246-4525-afa4-ebb50e068768").remove();

        // Check that the bus topology mapping has been invalidated
        assertNull(n.getExtension(CgmesIidmMapping.class).getTopologicalNodes("_044f4102-c766-11e1-8775-005056c00008_0"));

        // Check that bus topology mapping exists again
        context.updateTopologicalNodesMapping(n);
        assertNotNull(n.getExtension(CgmesIidmMapping.class).getTopologicalNodes("_044f4102-c766-11e1-8775-005056c00008_0"));
    }

    @Test
    public void testBuildNodeBreakerIidmMappingAndDeletingSwitch() {
        Network n = buildNodeBreakerNetwork();
        CgmesExportContext context = buildIidmMapping(n);

        // Check that the bus topology mapping exists
        assertNotNull(n.getExtension(CgmesIidmMapping.class).getTopologicalNodes("_04636548-c766-11e1-8775-005056c00008_0"));

        // Delete switch
        n.getVoltageLevel("_04636548-c766-11e1-8775-005056c00008").getNodeBreakerView().removeSwitch("_9550e743-98fd-4be7-848f-b6a600d6c67b");

        // Check that the bus topology mapping has been invalidated
        assertNull(n.getExtension(CgmesIidmMapping.class).getTopologicalNodes("_04636548-c766-11e1-8775-005056c00008_0"));

        // Check that the bus topology mapping exists again
        context.updateTopologicalNodesMapping(n);
        assertNotNull(n.getExtension(CgmesIidmMapping.class).getTopologicalNodes("_04636548-c766-11e1-8775-005056c00008_0"));
    }

    @Test
    public void testBuildBusBranchIidmMappingAndDeletingSwitch() {
        Network n = buildBusBranchNetwork();
        createBusBranchBus(n);
        CgmesExportContext context = buildIidmMapping(n);

        // Check that the bus topology mapping exists
        assertNotNull(n.getExtension(CgmesIidmMapping.class).getTopologicalNodes("_04636548-c766-11e1-8775-005056c00008_0"));

        // Delete switch
        n.getVoltageLevel("_04636548-c766-11e1-8775-005056c00008").getBusBreakerView().removeSwitch("testSwitch");

        // Check that the bus topology mapping has been invalidated
        assertNull(n.getExtension(CgmesIidmMapping.class).getTopologicalNodes("_04636548-c766-11e1-8775-005056c00008_0"));

        // Check that the bus topology mapping exists again
        context.updateTopologicalNodesMapping(n);
        assertNotNull(n.getExtension(CgmesIidmMapping.class).getTopologicalNodes("_04636548-c766-11e1-8775-005056c00008_0"));
    }

    @Test
    public void testBuildNodeBreakerIidmMappingAndDeletingSwitches() {
        Network n = buildNodeBreakerNetwork();
        CgmesExportContext context = buildIidmMapping(n);

        // Check that the bus topology mapping exists
        assertNotNull(n.getExtension(CgmesIidmMapping.class).getTopologicalNodes("_04636548-c766-11e1-8775-005056c00008_0"));

        // Delete voltage level (containing switches)
        VoltageLevel vl = n.getVoltageLevel("_04636548-c766-11e1-8775-005056c00008");
        vl.getLines().forEach(Connectable::remove);
        vl.remove();

        // Check that the bus topology mapping has been invalidated
        assertNull(n.getExtension(CgmesIidmMapping.class).getTopologicalNodes("_04636548-c766-11e1-8775-005056c00008_0"));

        // Check that the bus topology mapping does not exist
        context.updateTopologicalNodesMapping(n);
        assertNull(n.getExtension(CgmesIidmMapping.class).getTopologicalNodes("_04636548-c766-11e1-8775-005056c00008_0"));
    }

    private static Network buildNodeBreakerNetwork() {
        return buildNetwork(CgmesConformity1Catalog.smallNodeBreaker().dataSource());
    }

    private static void createNodeBreakerBus(Network n) {
        VoltageLevel vl = n.getVoltageLevel("_04636548-c766-11e1-8775-005056c00008");
        vl.getNodeBreakerView().newBusbarSection().setId("testBus").setNode(34).add();
        vl.newLoad().setId("testLoad").setNode(35).setP0(0.0).setQ0(0.0).add();
        vl.getNodeBreakerView().newSwitch().setId("testLoadSwitch").setNode1(34).setNode2(35).setOpen(false).setKind(SwitchKind.BREAKER).add();

        // Connect the new bus with the main island
        vl.getNodeBreakerView().newSwitch().setId("testSwitch").setNode1(10).setNode2(34).setOpen(false).setKind(SwitchKind.BREAKER).add();
    }

    private static Network buildBusBranchNetwork() {
        Network n = buildNetwork(CgmesConformity1Catalog.smallBusBranch().dataSource());
        return n;
    }

    private static void createBusBranchBus(Network n) {
        VoltageLevel vl = n.getVoltageLevel("_04636548-c766-11e1-8775-005056c00008");
        vl.getBusBreakerView().newBus().setId("testBus").add();
        n.newLine().setId("testLine")
                .setBus1("testBus").setBus2("_04675ce7-c766-11e1-8775-005056c00008")
                .setVoltageLevel1("_04636548-c766-11e1-8775-005056c00008")
                .setVoltageLevel2("_047b3309-c766-11e1-8775-005056c00008")
                .setR(0.0).setX(0.0).setB1(0.0).setB2(0.0).setG1(0.0).setG2(0.0)
                .add();
        vl.newLoad().setId("testLoad").setBus("testBus").setP0(0.0).setQ0(0.0).add();

        vl.getBusBreakerView().newSwitch().setId("testSwitch").setBus1("_04723255-c766-11e1-8775-005056c00008").setBus2("testBus").setOpen(false).add();
    }

    private static Network buildNetwork(ReadOnlyDataSource ds) {
        // Import without creating mappings
        Properties ip = new Properties();
        ip.put("iidm.import.cgmes.create-cgmes-export-mapping", "false");
        return new CgmesImport().importData(ds, NetworkFactory.findDefault(), ip);
    }

    private static CgmesExportContext buildIidmMapping(Network n) {
        CgmesExportContext context = new CgmesExportContext(n);
        assertNotNull(n.getExtension(CgmesIidmMapping.class));

        for (Bus bus : n.getBusView().getBuses()) {
            assertNotNull(context.getTopologicalNodesByBusViewBus(bus.getId()));
        }

        for (VoltageLevel voltageLevel : n.getVoltageLevels()) {
            assertNotNull(context.getBaseVoltageByNominalVoltage(voltageLevel.getNominalV()));
        }

        assertFalse(n.getExtension(CgmesIidmMapping.class).isTopologicalNodeEmpty());
        return context;
    }
}
