/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test.conformity;

import com.powsybl.cgmes.conformity.CgmesConformity3Catalog;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.export.CgmesExportContext;
import com.powsybl.cgmes.conversion.export.StateVariablesExport;
import com.powsybl.cgmes.conversion.test.ConversionUtil;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.CgmesNamespace;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TieLine;
import org.junit.jupiter.api.Test;

import javax.xml.stream.*;
import java.io.*;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class CgmesConformity3ConversionTest {

    @Test
    void microGridBaseCaseBEMergedWithNL() {
        Properties importParams = new Properties();
        importParams.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");

        Network be = Network.read(CgmesConformity3Catalog.microGridBaseCaseBE().dataSource(), importParams);
        assertNotEquals("unknown", be.getId());
        int nSubBE = be.getSubstationCount();
        int nDlBE = be.getDanglingLineCount();
        Network nl = Network.read(CgmesConformity3Catalog.microGridBaseCaseNL().dataSource(), importParams);
        assertNotEquals("unknown", nl.getId());
        int nSubNL = nl.getSubstationCount();
        int nDlNL = nl.getDanglingLineCount();
        // Both networks have the same number of dangling lines
        assertEquals(nDlBE, nDlNL);

        Network merge = Network.merge(be, nl);
        int nSub = merge.getSubstationCount();
        assertEquals(nSubBE + nSubNL, nSub);
        long nTl = merge.getTieLineCount();
        // All dangling lines must have been converted to tie lines
        assertEquals(nDlBE, nTl);

        for (TieLine tl : merge.getTieLines()) {
            // The danglingLine1 and danglingLine1.boundary.dl must be the same object
            // Both should correspond to objects at my level of merging
            assertEquals(tl.getDanglingLine1(), tl.getDanglingLine1().getBoundary().getDanglingLine());
            assertEquals(tl.getDanglingLine2(), tl.getDanglingLine2().getBoundary().getDanglingLine());
        }
        assertEquals(10, merge.getDanglingLineCount());

        // Check SV export contains tie line terminals
        checkExportSvTerminals(merge);
    }

    @Test
    void microGridBaseCaseAssembled() {
        Network n = Network.read(CgmesConformity3Catalog.microGridBaseCaseAssembled().dataSource());
        checkExportSvTerminals(n);
    }

    @Test
    void microGridBaseCaseAssembledSeparatingByFilename() {
        Properties params = new Properties();
        params.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "true");
        params.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS_DEFINED_BY, CgmesImport.SubnetworkDefinedBy.FILENAME.name());
        Network n = Network.read(CgmesConformity3Catalog.microGridBaseCaseAssembled().dataSource(), params);
        assertEquals(2, n.getSubnetworks().size());
        assertEquals(List.of("BE", "NL"),
                n.getSubnetworks().stream()
                        .map(n1 -> n1.getSubstations().iterator().next().getCountry().map(Objects::toString).orElse(""))
                        .sorted()
                        .toList());
        checkExportSvTerminals(n);
    }

    @Test
    void microGridBaseCaseAssembledSeparatingByModelingAuthority() {
        Properties params = new Properties();
        params.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "true");
        params.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS_DEFINED_BY, CgmesImport.SubnetworkDefinedBy.MODELING_AUTHORITY.name());
        Network n = Network.read(CgmesConformity3Catalog.microGridBaseCaseAssembled().dataSource(), params);
        assertEquals(2, n.getSubnetworks().size());
        assertEquals(List.of("BE", "NL"),
                n.getSubnetworks().stream()
                        .map(n1 -> n1.getSubstations().iterator().next().getCountry().map(Objects::toString).orElse(""))
                        .sorted()
                        .toList());
        checkExportSvTerminals(n);
    }

    private void checkExportSvTerminals(Network network) {
        CgmesExportContext context = new CgmesExportContext(network);
        context.setExportBoundaryPowerFlows(true);
        context.setExportFlowsForSwitches(true);

        byte[] xml = export(network, context);

        // For all tie lines we have exported the power flows with the right terminal identifiers
        for (TieLine tieLine : network.getTieLines()) {
            String terminal1 = tieLine.getDanglingLine1().getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL1).orElseThrow();
            String terminal2 = tieLine.getDanglingLine2().getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL1).orElseThrow();
            String terminal1Resource = "#_" + terminal1;
            String terminal2Resource = "#_" + terminal2;
            assertTrue(ConversionUtil.xmlContains(xml, "SvPowerFlow.Terminal", CgmesNamespace.RDF_NAMESPACE, "resource", terminal1Resource));
            assertTrue(ConversionUtil.xmlContains(xml, "SvPowerFlow.Terminal", CgmesNamespace.RDF_NAMESPACE, "resource", terminal2Resource));

            String eiTerminal1 = tieLine.getDanglingLine1().getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "EquivalentInjectionTerminal");
            String eiTerminal2 = tieLine.getDanglingLine2().getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "EquivalentInjectionTerminal");
            String eiTerminal1Resource = "#_" + eiTerminal1;
            String eiTerminal2Resource = "#_" + eiTerminal2;
            assertTrue(ConversionUtil.xmlContains(xml, "SvPowerFlow.Terminal", CgmesNamespace.RDF_NAMESPACE, "resource", eiTerminal1Resource));
            assertTrue(ConversionUtil.xmlContains(xml, "SvPowerFlow.Terminal", CgmesNamespace.RDF_NAMESPACE, "resource", eiTerminal2Resource));
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
}
