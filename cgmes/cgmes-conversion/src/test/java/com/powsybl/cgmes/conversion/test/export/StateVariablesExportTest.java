/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.test.export;

import com.powsybl.cgmes.conformity.CgmesConformity1Catalog;
import com.powsybl.cgmes.conformity.CgmesConformity1ModifiedCatalog;
import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.export.CgmesExportContext;
import com.powsybl.cgmes.conversion.export.StateVariablesExport;
import com.powsybl.cgmes.conversion.export.TopologyExport;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.test.AbstractConverterTest;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.computation.DefaultComputationManagerConfig;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.xml.ExportOptions;
import com.powsybl.iidm.xml.NetworkXml;
import com.powsybl.iidm.xml.XMLExporter;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.loadflow.resultscompletion.LoadFlowResultsCompletion;
import com.powsybl.loadflow.resultscompletion.LoadFlowResultsCompletionParameters;
import org.junit.jupiter.api.Test;

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
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class StateVariablesExportTest extends AbstractConverterTest {

    @Test
    void microGridBE() throws IOException, XMLStreamException {
        test(CgmesConformity1Catalog.microGridBaseCaseBE().dataSource(),
                false,
                2,
                false,
                StateVariablesExportTest::addRepackagerFiles);
    }

    @Test
    void microGridBEFlowsForSwitches() throws IOException, XMLStreamException {
        // Activate export of flows for switches on a small network with few switches,
        // Writing flows for all switches has impact on performance
        test(CgmesConformity1Catalog.microGridBaseCaseNL().dataSource(),
                false,
                2,
                true,
                StateVariablesExportTest::addRepackagerFiles);
    }

    @Test
    void minimalNodeBreakerFlowsForSwitches() {
        Network n = Network.create("minimal", "iidm");
        Substation s1 = n.newSubstation().setId("S1").add();
        Substation s2 = n.newSubstation().setId("S2").add();
        VoltageLevel vl1 = s1.newVoltageLevel().setId("VL1").setNominalV(100).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        Load load = vl1.newLoad().setId("LOAD").setNode(0)
                .setP0(10).setQ0(1).add();
        vl1.getNodeBreakerView().newBreaker().setId("BK11").setNode1(0).setNode2(1).add();
        vl1.getNodeBreakerView().newBusbarSection().setId("BBS1").setNode(1).add();
        vl1.getNodeBreakerView().newBreaker().setId("BK12").setNode1(1).setNode2(2).add();
        VoltageLevel vl2 = s2.newVoltageLevel().setId("VL2").setNominalV(100).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        Generator gen = vl2.newGenerator().setId("GEN").setNode(0)
                .setTargetP(10.01).setTargetQ(1.10)
                .setMinP(0).setMaxP(20)
                .setVoltageRegulatorOn(false).add();
        vl2.getNodeBreakerView().newBreaker().setId("BK21").setNode1(0).setNode2(1).add();
        vl2.getNodeBreakerView().newBusbarSection().setId("BBS2").setNode(1).add();
        vl2.getNodeBreakerView().newBreaker().setId("BK22").setNode1(1).setNode2(2).add();
        Line line = n.newLine().setId("LINE1").setVoltageLevel1("VL1").setVoltageLevel2("VL2").setNode1(2).setNode2(2)
                .setR(1).setX(10).add();

        Bus b1 = vl1.getBusView().getBuses().iterator().next();
        Bus b2 = vl2.getBusView().getBuses().iterator().next();
        b2.setV(100.0).setAngle(0);
        b1.setV(99.7946677).setAngle(-0.568404640);

        new LoadFlowResultsCompletion(new LoadFlowResultsCompletionParameters(), new LoadFlowParameters()).run(n, null);
        Properties writeParams = new Properties();
        writeParams.setProperty(XMLExporter.VERSION, "1.9");
        n.write("XIIDM", writeParams, tmpDir.resolve("minimal.xiidm"));
        System.out.printf("GEN  : %.2f %.2f%n", gen.getTerminal().getP(), gen.getTerminal().getQ());
        System.out.printf("LOAD : %.2f %.2f%n", load.getTerminal().getP(), load.getTerminal().getQ());
        System.out.printf("LINE : %.2f %.2f   %.2f %.2f%n",
                line.getTerminal1().getP(), line.getTerminal1().getQ(),
                line.getTerminal2().getP(), line.getTerminal2().getQ());

        Properties exportParams = new Properties();
        exportParams.setProperty(CgmesExport.EXPORT_POWER_FLOWS_FOR_SWITCHES, "true");
        n.write("CGMES", exportParams, tmpDir.resolve("exported"));
        // FIXME(Luma) check the flows on terminals of switches
    }

    @Test
    void microGridAssembled() throws IOException, XMLStreamException {
        test(CgmesConformity1Catalog.microGridBaseCaseAssembled().dataSource(),
                false,
                4,
                false,
            r -> {
                addRepackagerFiles("NL", r);
                addRepackagerFiles("BE", r);
            });
    }

    @Test
    void smallGridBusBranch() throws IOException, XMLStreamException {
        test(CgmesConformity1Catalog.smallBusBranch().dataSource(),
                false,
                4,
                false,
                StateVariablesExportTest::addRepackagerFiles);
    }

    @Test
    void smallGridNodeBreakerHVDC() throws IOException, XMLStreamException {
        test(CgmesConformity1Catalog.smallNodeBreakerHvdc().dataSource(),
                true,
                4,
                false,
                StateVariablesExportTest::addRepackagerFilesExcludeTp);
    }

    @Test
    void smallGridNodeBreaker() throws IOException, XMLStreamException {
        test(CgmesConformity1Catalog.smallNodeBreaker().dataSource(),
                true,
                4,
                false,
                StateVariablesExportTest::addRepackagerFilesExcludeTp);
    }

    @Test
    void miniBusBranchWithSvInjection() throws IOException, XMLStreamException {
        test(CgmesConformity1ModifiedCatalog.smallBusBranchWithSvInjection().dataSource(),
                false,
                4,
                false,
                StateVariablesExportTest::addRepackagerFiles);
    }

    @Test
    void miniBusBranchWithSvInjectionExportPQ() throws XMLStreamException {

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
    }

    @Test
    void miniBusBranchWithSvInjectionExportQ() throws XMLStreamException {

        Network network = importNetwork(CgmesConformity1ModifiedCatalog.smallBusBranchWithSvInjection().dataSource());
        String shuntCompensatorId = "04553478-c766-11e1-8775-005056c00008";
        ShuntCompensator shuntCompensator = network.getShuntCompensator(shuntCompensatorId);
        String cgmesTerminal = getCgmesTerminal(shuntCompensator.getTerminal());

        // If P and Q both are NaN is not exported

        shuntCompensator.getTerminal().setQ(-13.03);
        String sv = exportSvAsString(network, 4);
        assertTrue(sv.contains(cgmesTerminal));
    }

    @Test
    void microGridBEWithHiddenTapChangers() throws XMLStreamException {
        Network network = importNetwork(CgmesConformity1ModifiedCatalog.microGridBaseCaseBEHiddenTapChangers().dataSource());
        String sv = exportSvAsString(network, 2);
        String hiddenTapChangerId = "_6ebbef67-3061-4236-a6fd-6ccc4595f6c3-x";
        assertTrue(sv.contains(hiddenTapChangerId));
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

    private static void addRepackagerFiles(Repackager repackager) {
        repackager.with("test_EQ.xml", Repackager::eq)
                .with("test_TP.xml", Repackager::tp)
                .with("test_SSH.xml", Repackager::ssh);
    }

    private static void addRepackagerFilesExcludeTp(Repackager repackager) {
        repackager.with("test_EQ.xml", Repackager::eq)
                .with("test_SSH.xml", Repackager::ssh);
    }

    private void test(ReadOnlyDataSource dataSource, boolean exportTp, int svVersion, boolean exportFlowsForSwitches, Consumer<Repackager> repackagerConsumer) throws XMLStreamException, IOException {
        // Import original
        Properties properties = new Properties();
        properties.put("iidm.import.cgmes.create-cgmes-export-mapping", "true");
        Network expected0 = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), properties);

        // Ensure all information in IIDM mapping extensions is created
        // Some mappings are not built until export is requested
        new CgmesExportContext().addIidmMappings(expected0);

        // Export to XIIDM and re-import to test serialization of CGMES-IIDM extension
        NetworkXml.write(expected0, tmpDir.resolve("temp.xiidm"));
        Network expected = NetworkXml.read(tmpDir.resolve("temp.xiidm"));

        // Export SV
        CgmesExportContext context = new CgmesExportContext(expected);
        context.getSvModelDescription().setVersion(svVersion);
        context.setExportBoundaryPowerFlows(true);
        context.setExportFlowsForSwitches(exportFlowsForSwitches);
        Path exportedSv = tmpDir.resolve("exportedSv.xml");
        try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(exportedSv))) {
            XMLStreamWriter writer = XmlUtil.initializeWriter(true, "    ", os);
            StateVariablesExport.write(expected, writer, context);
        }
        // Export TP if required (node/breaker models require an export of TP in addition to SV file)
        Path exportedTp = tmpDir.resolve("exportedTp.xml");
        if (exportTp) {
            try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(exportedTp))) {
                XMLStreamWriter writer = XmlUtil.initializeWriter(true, "    ", os);
                TopologyExport.write(expected, writer, context);
            }
        }

        // Zip with new SV (and eventually a new TP)
        Path repackaged = tmpDir.resolve("repackaged.zip");
        Repackager r = new Repackager(dataSource)
                .with("test_SV.xml", exportedSv)
                .with("test_EQ_BD.xml", Repackager::eqBd)
                .with("test_TP_BD.xml", Repackager::tpBd);
        if (exportTp) {
            r.with("test_TP.xml", exportedTp);
        }
        repackagerConsumer.accept(r);
        r.zip(repackaged);

        // Import with new SV
        Network actual = Network.read(repackaged,
                DefaultComputationManagerConfig.load().createShortTimeExecutionComputationManager(), ImportConfig.load(), properties);

        // Before comparison, set undefined p/q in expected network at 0.0
        expected.getConnectableStream()
                .filter(c -> c instanceof Injection)
                .filter(c -> !(c instanceof BusbarSection))
                .filter(c -> !(c instanceof HvdcConverterStation))
                .filter(c -> !(c instanceof DanglingLine && ((DanglingLine) c).isPaired()))
                .map(c -> (Injection<?>) c)
                .map(Injection::getTerminal)
                .filter(t -> Double.isNaN(t.getP()) && Double.isNaN(t.getQ()))
                .forEach(t -> t.setP(0.0).setQ(0.0));

        // Export original and with new SV
        // comparison without extensions, only Networks
        ExportOptions exportOptions = new ExportOptions().setSorted(true);
        exportOptions.setExtensions(Collections.emptySet());
        NetworkXml.writeAndValidate(expected, exportOptions, tmpDir.resolve("expected.xml"));
        NetworkXml.writeAndValidate(actual, exportOptions, tmpDir.resolve("actual.xml"));

        // Compare
        ExportXmlCompare.compareNetworks(tmpDir.resolve("expected.xml"), tmpDir.resolve("actual.xml"));
    }
}
