/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.io.TreeDataFormat;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TopologyKind;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
class BinaryTest extends AbstractIidmSerDeTest {

    @Test
    void testEmptyNullStringAttributes() {
        Network n0 = Network.create("test", "test");
        n0.setProperty("property", "");
        n0.newVoltageLevel().setId("vl1").setNominalV(220).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        n0.newVoltageLevel().setId("vl2").setNominalV(220).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        Line l0 = n0.newLine().setId("line").setName("")
                .setVoltageLevel1("vl1").setNode1(0)
                .setVoltageLevel2("vl2").setNode2(0)
                .setR(0.1).setX(10)
                .add();
        l0.newCurrentLimits1().add();
        l0.cancelSelectedOperationalLimitsGroup1(); // selected group id is now null on side 1
        l0.newOperationalLimitsGroup2("").newCurrentLimits().add();
        l0.setSelectedOperationalLimitsGroup2(""); // selected group id is now "" on side 2

        Path binFile = fileSystem.getPath("/work/test");
        NetworkSerDe.write(n0, new ExportOptions().setFormat(TreeDataFormat.BIN), binFile);
        Network n1 = NetworkSerDe.read(binFile, new ImportOptions().setFormat(TreeDataFormat.BIN));
        Line s = n1.getLine("line");
        assertEquals("", n1.getProperty("property"));
        assertEquals(Optional.of(""), s.getOptionalName());
        assertEquals(Optional.empty(), l0.getSelectedOperationalLimitsGroupId1());
        assertEquals(Optional.of(""), l0.getSelectedOperationalLimitsGroupId2());
    }
}
