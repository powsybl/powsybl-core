/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.test.export;

import com.powsybl.cgmes.conformity.CgmesConformity1Catalog;
import com.powsybl.cgmes.conformity.CgmesConformity1ModifiedCatalog;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.export.CgmesExportContext;
import com.powsybl.cgmes.conversion.export.StateVariablesExport;
import com.powsybl.cgmes.extensions.CgmesIidmMapping;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.computation.DefaultComputationManagerConfig;
import com.powsybl.iidm.export.ExportOptions;
import com.powsybl.iidm.import_.ImportConfig;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.xml.NetworkXml;
import org.junit.Test;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class StateVariablesExportTest extends AbstractConverterTest {

    @Test
    public void microGridBE() throws IOException, XMLStreamException {
        test(CgmesConformity1Catalog.microGridBaseCaseBE().dataSource(), 2);
    }

    @Test
    public void microGridBEFlowsForSwitches() throws IOException, XMLStreamException {
        // Activate export of flows for switches on a small network with few switches,
        // Writing flows for all switches has impact on performance
        test(CgmesConformity1Catalog.microGridBaseCaseNL().dataSource(), 2, true);
    }

    @Test
    public void microGridAssembled() throws IOException, XMLStreamException {
        test(CgmesConformity1Catalog.microGridBaseCaseAssembled().dataSource(), 4, false, r -> {
            addRepackagerFiles("NL", r);
            addRepackagerFiles("BE", r);
        });
    }

    @Test
    public void smallGridBusBranch() throws IOException, XMLStreamException {
        test(CgmesConformity1Catalog.smallBusBranch().dataSource(), 4);
    }

    @Test
    public void smallGridNodeBreakerHVDC() throws IOException, XMLStreamException {
        test(CgmesConformity1Catalog.smallNodeBreakerHvdc().dataSource(), 4);
    }

    @Test
    public void smallGridNodeBreaker() throws IOException, XMLStreamException {
        test(CgmesConformity1Catalog.smallNodeBreaker().dataSource(), 4);
    }

    @Test
    public void miniBusBranchWithSvInjection() throws IOException, XMLStreamException {
        test(CgmesConformity1ModifiedCatalog.smallBusBranchWithSvInjection().dataSource(), 4);
    }

    @Test
    public void miniBusBranchWithSvInjectionExportPQ() throws IOException, XMLStreamException {

        Network network = importNetwork(CgmesConformity1ModifiedCatalog.smallBusBranchWithSvInjection().dataSource());
        String loadId = "0448d86a-c766-11e1-8775-005056c00008";
        Load load = network.getLoad(loadId);
        String cgmesTerminal = getCgmesTerminal(load.getTerminal());

        // Only when P and Q are NaN is not exported

        load.getTerminal().setP(-0.12);
        load.getTerminal().setQ(-13.03);
        String sv = exportSvAsString(network, 4);
        assertTrue(sv.contains(cgmesTerminal));

        load.getTerminal().setP(Double.NaN);
        load.getTerminal().setQ(-13.03);
        String sv1 = exportSvAsString(network, 4);
        assertTrue(sv1.contains(cgmesTerminal));

        load.getTerminal().setP(-0.12);
        load.getTerminal().setQ(Double.NaN);
        String sv2 = exportSvAsString(network, 4);
        assertTrue(sv2.contains(cgmesTerminal));

        load.getTerminal().setP(Double.NaN);
        load.getTerminal().setQ(Double.NaN);
        String sv3 = exportSvAsString(network, 4);
        assertFalse(sv3.contains(cgmesTerminal));
    }

    @Test
    public void miniBusBranchWithSvInjectionExportQ() throws IOException, XMLStreamException {

        Network network = importNetwork(CgmesConformity1ModifiedCatalog.smallBusBranchWithSvInjection().dataSource());
        String shuntCompensatorId = "04553478-c766-11e1-8775-005056c00008";
        ShuntCompensator shuntCompensator = network.getShuntCompensator(shuntCompensatorId);
        String cgmesTerminal = getCgmesTerminal(shuntCompensator.getTerminal());

        // If Q are NaN is not exported

        shuntCompensator.getTerminal().setQ(-13.03);
        String sv = exportSvAsString(network, 4);
        assertTrue(sv.contains(cgmesTerminal));

        shuntCompensator.getTerminal().setQ(Double.NaN);
        String sv1 = exportSvAsString(network, 4);
        assertFalse(sv1.contains(cgmesTerminal));
    }

    private static Network importNetwork(ReadOnlyDataSource ds) {
        Properties properties = new Properties();
        properties.put("iidm.import.cgmes.create-cgmes-export-mapping", "true");
        return new CgmesImport().importData(ds, NetworkFactory.findDefault(), properties);
    }

    private String exportSvAsString(Network network, int svVersion) throws XMLStreamException {
        CgmesExportContext context = new CgmesExportContext(network);
        StringWriter stringWriter = new StringWriter();
        XMLStreamWriter writer = XmlUtil.initializeWriter(true, "    ", stringWriter);
        context.getSvModelDescription().setVersion(svVersion);
        context.setExportBoundaryPowerFlows(true);
        StateVariablesExport.write(network, writer, context);

        return stringWriter.toString();
    }

    private static String getCgmesTerminal(Terminal terminal) {
        return ((Connectable<?>) terminal.getConnectable()).getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL1).orElse(null);
    }

    private static void addRepackagerFiles(String tso, Repackager repackager) {
        repackager.with("test_" + tso + "_EQ.xml", name -> name.contains(tso) && name.contains("EQ"))
                .with("test_" + tso + "_TP.xml", name -> name.contains(tso) && name.contains("TP"))
                .with("test_" + tso + "_SSH.xml", name -> name.contains(tso) && name.contains("SSH"));
    }

    private void test(ReadOnlyDataSource dataSource, int svVersion) throws IOException, XMLStreamException {
        test(dataSource, svVersion, false);
    }

    private void test(ReadOnlyDataSource dataSource, int svVersion, boolean exportFlowsForSwitches) throws IOException, XMLStreamException {
        test(dataSource, svVersion, exportFlowsForSwitches, r -> r
                .with("test_EQ.xml", Repackager::eq)
                .with("test_TP.xml", Repackager::tp)
                .with("test_SSH.xml", Repackager::ssh));
    }

    private static void checkLineTNMappingOnSide(Line l, Branch.Side side, int numSide, CgmesIidmMapping iidmMapping) {
        String lineSideTNId = iidmMapping.getTopologicalNode(l.getId(), numSide);
        Set<CgmesIidmMapping.CgmesTopologicalNode> busSideTNs = iidmMapping.getTopologicalNodes(l.getTerminal(side).getBusView().getBus().getId());
        Set<String> busSideTNIds = busSideTNs.stream().map(CgmesIidmMapping.CgmesTopologicalNode::getCgmesId).collect(Collectors.toSet());
        assertTrue(busSideTNIds.contains(lineSideTNId));
    }

    private void test(ReadOnlyDataSource dataSource, int svVersion, boolean exportFlowsForSwitches, Consumer<Repackager> repackagerConsumer) throws XMLStreamException, IOException {
        // Import original
        Properties properties = new Properties();
        properties.put("iidm.import.cgmes.create-cgmes-export-mapping", "true");
        Network expected0 = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), properties);

        // Ensure all information in IIDM mapping extensions is created
        // Some of the mappings are not built until export is requested
        new CgmesExportContext().addIidmMappings(expected0);

        // Check the information stored in the extension before it is serialized
        CgmesIidmMapping iidmMapping = expected0.getExtension(CgmesIidmMapping.class);
        assertNotNull(iidmMapping);
        for (Line l : expected0.getLines()) {
            checkLineTNMappingOnSide(l, Branch.Side.ONE, 1, iidmMapping);
            checkLineTNMappingOnSide(l, Branch.Side.TWO, 2, iidmMapping);
        }

        // Export to XIIDM and re-import to test serialization of CGMES-IIDM extension
        NetworkXml.write(expected0, tmpDir.resolve("temp.xiidm"));
        Network expected = NetworkXml.read(tmpDir.resolve("temp.xiidm"));

        // Export SV
        CgmesExportContext context = new CgmesExportContext(expected, true);
        Path exportedSv = tmpDir.resolve("exportedSv.xml");
        try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(exportedSv))) {
            XMLStreamWriter writer = XmlUtil.initializeWriter(true, "    ", os);
            context.getSvModelDescription().setVersion(svVersion);
            context.setExportBoundaryPowerFlows(true);
            context.setExportFlowsForSwitches(exportFlowsForSwitches);
            StateVariablesExport.write(expected, writer, context);
        }

        // Zip with new SV
        Path repackaged = tmpDir.resolve("repackaged.zip");
        Repackager r = new Repackager(dataSource)
                .with("test_SV.xml", exportedSv)
                .with("test_EQ_BD.xml", Repackager::eqBd)
                .with("test_TP_BD.xml", Repackager::tpBd);
        repackagerConsumer.accept(r);
        r.zip(repackaged);

        // Import with new SV
        Network actual = Importers.loadNetwork(repackaged,
                DefaultComputationManagerConfig.load().createShortTimeExecutionComputationManager(), ImportConfig.load(), properties);

        // Export original and with new SV
        // comparison without extensions, only Networks
        ExportOptions exportOptions = new ExportOptions();
        exportOptions.setExtensions(Collections.emptySet());
        NetworkXml.writeAndValidate(expected, exportOptions, tmpDir.resolve("expected.xml"));
        NetworkXml.writeAndValidate(actual, exportOptions, tmpDir.resolve("actual.xml"));

        // Compare
        ExportXmlCompare.compareNetworks(tmpDir.resolve("expected.xml"), tmpDir.resolve("actual.xml"));
    }
}
