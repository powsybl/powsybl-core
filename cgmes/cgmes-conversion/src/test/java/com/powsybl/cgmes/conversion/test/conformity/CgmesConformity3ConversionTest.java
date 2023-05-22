/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test.conformity;

import com.powsybl.cgmes.conformity.CgmesConformity3Catalog;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.elements.ACLineSegmentConversion;
import com.powsybl.cgmes.conversion.export.CgmesExportContext;
import com.powsybl.cgmes.conversion.export.StateVariablesExport;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.CgmesNamespace;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.mergingview.MergingView;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TieLine;
import org.junit.jupiter.api.Test;

import javax.xml.stream.*;
import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
class CgmesConformity3ConversionTest {

    @Test
    void microGridBaseCaseBEMergedWithNL() {
        Network be = Network.read(CgmesConformity3Catalog.microGridBaseCaseBE().dataSource());
        assertNotEquals("unknown", be.getId());
        int nSubBE = be.getSubstationCount();
        int nDlBE = be.getDanglingLineCount();
        Network nl = Network.read(CgmesConformity3Catalog.microGridBaseCaseNL().dataSource());
        assertNotEquals("unknown", nl.getId());
        int nSubNL = nl.getSubstationCount();
        int nDlNL = nl.getDanglingLineCount();
        // Both networks have the same number of dangling lines
        assertEquals(nDlBE, nDlNL);
        be.merge(nl);
        int nSub = be.getSubstationCount();
        assertEquals(nSubBE + nSubNL, nSub);
        long nTl = be.getTieLineCount();
        // All dangling lines must have been converted to tie lines
        assertEquals(nDlBE, nTl);

        // Check SV export contains tie line terminals
        checkExportSvTerminals(be);
    }

    private void checkExportSvTerminals(Network network) {
        CgmesExportContext context = new CgmesExportContext(network);
        context.getSvModelDescription().setVersion(2);
        context.setExportBoundaryPowerFlows(true);
        context.setExportFlowsForSwitches(true);

        byte[] xml = export(network, context);

        // For all tie lines we have exported the power flows with the right terminal identifiers
        for (TieLine tieLine : network.getTieLines()) {
            if (ACLineSegmentConversion.DRAFT_LUMA_REMOVE_TIE_LINE_PROPERTIES_ALIASES) {
                String terminal1 = tieLine.getDanglingLine1().getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL1).orElseThrow();
                String terminal2 = tieLine.getDanglingLine2().getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL1).orElseThrow();
                assertEquals(tieLine.getProperty("CGMES.Terminal_1"), terminal1);
                assertEquals(tieLine.getProperty("CGMES.Terminal_2"), terminal2);
                String terminal1Resource = "#_" + terminal1;
                String terminal2Resource = "#_" + terminal2;
                assertTrue(xmlContains(xml, "SvPowerFlow.Terminal", CgmesNamespace.RDF_NAMESPACE, "resource", terminal1Resource));
                assertTrue(xmlContains(xml, "SvPowerFlow.Terminal", CgmesNamespace.RDF_NAMESPACE, "resource", terminal2Resource));
            } else {
                String terminal1 = tieLine.getAliasFromType("CGMES.Terminal1").orElseThrow();
                String terminal2 = tieLine.getAliasFromType("CGMES.Terminal2").orElseThrow();
                assertEquals(tieLine.getProperty("CGMES.Terminal_1"), terminal1);
                assertEquals(tieLine.getProperty("CGMES.Terminal_2"), terminal2);
                String terminal1Resource = "#_" + terminal1;
                String terminal2Resource = "#_" + terminal2;
                assertTrue(xmlContains(xml, "SvPowerFlow.Terminal", CgmesNamespace.RDF_NAMESPACE, "resource", terminal1Resource));
                assertTrue(xmlContains(xml, "SvPowerFlow.Terminal", CgmesNamespace.RDF_NAMESPACE, "resource", terminal2Resource));
            }
        }
    }

    private byte[] export(Network network, CgmesExportContext context) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (OutputStream os = baos) {
            XMLStreamWriter writer = XmlUtil.initializeWriter(true, "    ", os);
            StateVariablesExport.write(network, writer, context);
        } catch (XMLStreamException | IOException e) {
            throw new RuntimeException(e);
        }
        return baos.toByteArray();
    }

    private static boolean xmlContains(byte[] xml, String clazz, String ns, String attr, String expectedValue) {
        try (InputStream is = new ByteArrayInputStream(xml)) {
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(is);
            while (reader.hasNext()) {
                if (reader.next() == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals(clazz)) {
                    String actualValue = reader.getAttributeValue(ns, attr);
                    if (expectedValue.equals(actualValue)) {
                        reader.close();
                        return true;
                    }
                }
            }
            reader.close();
        } catch (XMLStreamException | IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    @Test
    void microGridBaseCaseBEMergingViewNL() {
        Network be = Network.read(CgmesConformity3Catalog.microGridBaseCaseBE().dataSource());
        assertNotEquals("unknown", be.getId());
        int nSubBE = be.getSubstationCount();
        int nDlBE = be.getDanglingLineCount();
        Network nl = Network.read(CgmesConformity3Catalog.microGridBaseCaseNL().dataSource());
        assertNotEquals("unknown", nl.getId());
        int nSubNL = nl.getSubstationCount();
        int nDlNL = nl.getDanglingLineCount();
        // Both networks have the same number of dangling lines
        assertEquals(nDlBE, nDlNL);

        Network n = MergingView.create("be-nl", "CGMES");
        n.merge(nl, be);

        int nSub = n.getSubstationCount();
        assertEquals(nSubBE + nSubNL, nSub);
        long nTl = n.getTieLineCount();
        // All dangling lines must have been converted to tie lines
        assertEquals(nDlBE, nTl);
        for (TieLine tl : n.getTieLines()) {
            // The danglingLine1 and danglingLine1.boundary.dl must be the same object
            // Both should correspond to objects at my level of merging
            assertEquals(tl.getDanglingLine1(), tl.getDanglingLine1().getBoundary().getDanglingLine());
            assertEquals(tl.getDanglingLine2(), tl.getDanglingLine2().getBoundary().getDanglingLine());
        }
        // No dangling lines should be seen in the merging view
        // Even if dangling line adapters have been added to the cached identifiables in the merging index
        assertEquals(10, n.getDanglingLineCount());
    }
}
