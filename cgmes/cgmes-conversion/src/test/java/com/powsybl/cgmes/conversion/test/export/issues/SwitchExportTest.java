/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test.export.issues;

import com.powsybl.cgmes.conformity.CgmesConformity1ModifiedCatalog;
import com.powsybl.cgmes.model.CgmesNamespace;
import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;
import org.junit.jupiter.api.Test;

import javax.xml.stream.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class SwitchExportTest extends AbstractSerDeTest {

    @Test
    void testSwitchTypePreservedBusBranch() {
        // Load a bus/branch network containing a generic "Switch", not a breaker
        Network network = Network.read(CgmesConformity1ModifiedCatalog.microGridBaseCaseNLSwitchTypePreserved().dataSource());
        String basename = "micro-nl";
        network.write("CGMES", null, tmpDir.resolve(basename));

        // In IIDM the switch has been created of kind "Breaker"
        String switchId = "5f5d40ae-d52d-4631-9285-b3ceefff784c";
        assertEquals(SwitchKind.BREAKER, network.getSwitch(switchId).getKind());

        // Check that the "Switch" type has been preserved in EQ and SSH when we export
        String switchIdEq = readId("Switch", "ID", tmpDir.resolve(basename + "_EQ.xml"));
        String switchIdSsh = readId("Switch", "about", tmpDir.resolve(basename + "_SSH.xml"));
        assertEquals("_" + switchId, switchIdEq);
        assertEquals("#_" + switchId, switchIdSsh);
    }

    @Test
    void testSwitchTypePreservedNodeBreaker() {
        // Load a node/branch network containing a "ProtectedSwitch"
        Network network = Network.read(CgmesConformity1ModifiedCatalog.miniGridNodeBreakerSwitchTypePreserved().dataSource());
        String basename = "mini";
        network.write("CGMES", null, tmpDir.resolve(basename));

        // In IIDM the switch has been created of kind "Breaker", the default when the type read is not supported
        String switchId = "5e9f0079-647e-46da-b0ee-f5f24e127602";
        assertEquals(SwitchKind.BREAKER, network.getSwitch(switchId).getKind());

        // Check that the "ProtectedSwitch" type has been preserved in EQ and SSH when we export
        String switchIdEq = readId("ProtectedSwitch", "ID", tmpDir.resolve(basename + "_EQ.xml"));
        String switchIdSsh = readId("ProtectedSwitch", "about", tmpDir.resolve(basename + "_SSH.xml"));
        assertEquals("_" + switchId, switchIdEq);
        assertEquals("#_" + switchId, switchIdSsh);
    }

    @Test
    void testExportRetainedSwitchWithSameBusBreakerBusAtBothEnds() {
        // We create a network where a retained breaker has the same bus-breaker buses at both ends
        // After #2574, a breaker with these characteristics was not exported to CGMES
        Network n = Network.create("double-breaker-between-busbar-sections", "manual");
        VoltageLevel vl = n.newVoltageLevel().setId("vl").setName("vl").setTopologyKind(TopologyKind.NODE_BREAKER).setNominalV(10).add();
        VoltageLevel.NodeBreakerView nb = vl.getNodeBreakerView();
        vl.newLoad().setId("load").setName("load").setNode(11)
                .setP0(10).setQ0(0).add();
        nb.newBusbarSection().setId("bbs1").setNode(1).add();
        nb.newSwitch().setId("bkload").setName("bkload").setNode1(1).setNode2(11).setKind(SwitchKind.BREAKER).add();
        vl.newGenerator().setId("gen").setName("gen").setNode(22)
                .setTargetP(10).setTargetV(10).setMinP(0).setMaxP(100).setVoltageRegulatorOn(true).add();
        nb.newBusbarSection().setId("bbs2").setNode(2).add();
        nb.newSwitch().setId("bkgen").setName("bkgen").setNode1(2).setNode2(22).setKind(SwitchKind.BREAKER).add();
        // There are two couplers linking the busbar sections, only one is marked as retained
        nb.newSwitch().setId("coupler1").setName("coupler1").setNode1(1).setNode2(2).setKind(SwitchKind.BREAKER).setRetained(true).add();
        nb.newSwitch().setId("coupler2").setName("coupler2").setNode1(1).setNode2(2).setKind(SwitchKind.BREAKER).setRetained(false).add();

        // Check that both couplers are preserved when exported to CGMES
        String basename = "double-breaker-between-busbars";
        n.write("CGMES", null, tmpDir.resolve(basename));
        Network n1 = Network.read(new FileDataSource(tmpDir, basename));
        assertNotNull(n1.getSwitch("coupler2"));
        assertNotNull(n1.getSwitch("coupler1"));
    }

    private static String readId(String elementName, String rdfIdAttrName, Path ssh) {
        String id;
        try (InputStream is = Files.newInputStream(ssh)) {
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(is);
            while (reader.hasNext()) {
                int next = reader.next();
                if (next == XMLStreamConstants.START_ELEMENT) {
                    if (reader.getLocalName().equals(elementName)) {
                        id = reader.getAttributeValue(CgmesNamespace.RDF_NAMESPACE, rdfIdAttrName);
                        reader.close();
                        return id;
                    }
                }
            }
            reader.close();
        } catch (XMLStreamException | IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

}
