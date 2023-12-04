/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test.export.issues;

import com.powsybl.cgmes.conformity.CgmesConformity1ModifiedCatalog;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.model.CgmesNamespace;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.SwitchKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.stream.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    void testSwitchTypePreservedBusBranch() {
        // Load a bus/branch network containing a generic "Switch", not a breaker
        Network network = Network.read(CgmesConformity1ModifiedCatalog.microGridBaseCaseNLSwitchTypePreserved().dataSource(), importParams);
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
        Network network = Network.read(CgmesConformity1ModifiedCatalog.miniGridNodeBreakerSwitchTypePreserved().dataSource(), importParams);
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
