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
    public void testBuildIidmMappingAndChangingSwitchStatus() {
        Network n = buildIidmMapping();

        // Change switch status
        n.getSwitch("_9550e743-98fd-4be7-848f-b6a600d6c67b").setOpen(true);

        // Check that topology mapping has been invalidated
        assertTrue(n.getExtension(CgmesIidmMapping.class).isTopologicalNodeEmpty());
    }

    @Test
    public void testBuildIidmMappingAndCreatingBus() {
        Network n = buildIidmMapping();

        // Create a Bus with a Load
        VoltageLevel vl = n.getVoltageLevel("_04636548-c766-11e1-8775-005056c00008");
        vl.getNodeBreakerView().newBusbarSection().setId("testBus").setNode(34).add();
        vl.newLoad().setId("testLoad").setNode(35).setP0(0.0).setQ0(0.0).add();
        vl.getNodeBreakerView().newSwitch().setId("testLoadSwitch").setNode1(34).setNode2(35).setOpen(false).setKind(SwitchKind.BREAKER).add();
        vl.getNodeBreakerView().newSwitch().setId("testSwitch").setNode1(0).setNode2(34).setOpen(false).setKind(SwitchKind.BREAKER).add();

        // Check that topology mapping has been invalidated
        assertTrue(n.getExtension(CgmesIidmMapping.class).isTopologicalNodeEmpty());
    }

    @Test
    public void testBuildIidmMappingAndCreatingSwitch() {
        Network n = buildIidmMapping();

        // Create switch
        n.getVoltageLevel("_04636548-c766-11e1-8775-005056c00008").getNodeBreakerView().newSwitch().setId("test").setNode1(0).setNode2(2).setOpen(false).setKind(SwitchKind.BREAKER).add();

        // Check that topology mapping has been invalidated
        assertTrue(n.getExtension(CgmesIidmMapping.class).isTopologicalNodeEmpty());
    }

    @Test
    public void testBuildIidmMappingAndCreatingInternalConnection() {
        Network n = buildIidmMapping();

        // Create internal connection
        n.getVoltageLevel("_04636548-c766-11e1-8775-005056c00008").getNodeBreakerView().newInternalConnection().setNode1(0).setNode2(2).add();

        // Check that topology mapping has been invalidated
        assertTrue(n.getExtension(CgmesIidmMapping.class).isTopologicalNodeEmpty());
    }

    @Test
    public void testBuildIidmMappingAndDeletingSwitch() {
        Network n = buildIidmMapping();

        // Delete switch
        n.getVoltageLevel("_04636548-c766-11e1-8775-005056c00008").getNodeBreakerView().removeSwitch("_9550e743-98fd-4be7-848f-b6a600d6c67b");

        // Check that topology mapping has been invalidated
        assertTrue(n.getExtension(CgmesIidmMapping.class).isTopologicalNodeEmpty());
    }

    @Test
    public void testBuildIidmMappingAndDeletingSwitches() {
        Network n = buildIidmMapping();

        // Delete voltage level (containing switches)
        VoltageLevel vl = n.getVoltageLevel("_04636548-c766-11e1-8775-005056c00008");
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
        CgmesExportContext context = new CgmesExportContext(n);
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
