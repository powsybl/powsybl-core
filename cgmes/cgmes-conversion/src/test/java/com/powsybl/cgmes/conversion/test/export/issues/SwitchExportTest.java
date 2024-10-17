/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test.export.issues;

import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.commons.datasource.DirectoryDataSource;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class SwitchExportTest extends AbstractSerDeTest {

    private Properties importParams;

    @BeforeEach
    public void setUp() throws IOException {
        super.setUp();
        importParams = new Properties();
        importParams.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");
    }

    @Test
    void testExportRetainedSwitchWithSameBusBreakerBusAtBothEnds() {
        // We create a network where a retained breaker has the same bus-breaker buses at both ends
        // After #2574, a breaker with these characteristics was not exported to CGMES
        // It may happen in some double bar configurations where lines (or other equipment) may be connected to both bars
        // and there is also a retained coupler
        Network n = Network.create("retained-breaker-between-busbar-sections", "manual");
        VoltageLevel vl = n.newVoltageLevel().setId("vl").setName("vl").setTopologyKind(TopologyKind.NODE_BREAKER).setNominalV(10).add();
        VoltageLevel.NodeBreakerView nb = vl.getNodeBreakerView();
        nb.newBusbarSection().setId("bbs1").setNode(1).add();
        nb.newBusbarSection().setId("bbs2").setNode(2).add();
        nb.newSwitch().setId("coupler").setName("coupler").setNode1(1).setNode2(2).setKind(SwitchKind.BREAKER).setRetained(true).add();
        vl.newLoad().setId("load").setName("load").setNode(3)
                .setP0(10).setQ0(0).add();
        vl.newGenerator().setId("gen").setName("gen").setNode(4)
                .setTargetP(10).setTargetV(10).setMinP(0).setMaxP(100).setVoltageRegulatorOn(true).add();
        nb.newSwitch().setId("load-bk").setName("load-bk").setNode1(3).setNode2(31).setKind(SwitchKind.BREAKER).add();
        nb.newSwitch().setId("load-dis1").setName("load-dis1").setNode1(31).setNode2(1).setKind(SwitchKind.DISCONNECTOR).add();
        nb.newSwitch().setId("load-dis2").setName("load-dis2").setNode1(31).setNode2(2).setKind(SwitchKind.DISCONNECTOR).add();
        nb.newSwitch().setId("gen-bk").setName("gen-bk").setNode1(4).setNode2(41).setKind(SwitchKind.BREAKER).add();
        nb.newSwitch().setId("gen-dis1").setName("gen-dis1").setNode1(41).setNode2(1).setKind(SwitchKind.DISCONNECTOR).add();
        nb.newSwitch().setId("gen-dis2").setName("gen-dis2").setNode1(41).setNode2(2).setKind(SwitchKind.DISCONNECTOR).add();

        // Check that the coupler is preserved when exported to CGMES
        String basename = n.getNameOrId();
        n.write("CGMES", null, tmpDir.resolve(basename));
        Network n1 = Network.read(new DirectoryDataSource(tmpDir, basename));
        assertNotNull(n1.getSwitch("coupler"));
    }

}
