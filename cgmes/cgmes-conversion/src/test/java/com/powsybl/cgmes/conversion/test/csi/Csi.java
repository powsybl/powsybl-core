package com.powsybl.cgmes.conversion.test.csi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexUtils;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.cgmes.conformity.test.CgmesConformity1Catalog;
import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.CgmesImportPostProcessor;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.PhaseAngleClock;
import com.powsybl.cgmes.conversion.Conversion.Config;
import com.powsybl.cgmes.conversion.test.ConversionTester;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesModelFactory;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.commons.datasource.ZipFileDataSource;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Branch.Side;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.HvdcConverterStation;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.LccConverterStation;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ReactiveLimits;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.TopologyLevel;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.VscConverterStation;
import com.powsybl.iidm.network.util.BranchData;
import com.powsybl.iidm.network.util.LinkData;
import com.powsybl.iidm.xml.XMLExporter;
import com.powsybl.iidm.xml.XMLImporter;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.loadflow.validation.ValidationConfig;
import com.powsybl.loadflow.validation.ValidationType;
import com.powsybl.triplestore.api.TripleStoreFactory;

public class Csi {

    //@Test
    public void issue1004() throws IOException {
        Path p = Paths.get("issue1004", "be.zip");
        Networks ns = analyze("issue1004", LOCAL, p);
    }

    //@Test
    public void renAugustLocal() throws IOException {
        Path p = Paths.get("ren", "20190821_1130_FO3_PT1.zip");
        Networks ns = analyze("REN_20190821_1130", IOP_AUGUST_LOCAL, p);

        for (Generator g : ns.imported.getGenerators()) {
            Terminal t = g.getRegulatingTerminal();
            if (t == null) {
                continue;
            }
            Bus b1 = t.getBusBreakerView().getBus();
            Bus b2 = ns.exported.getGenerator(g.getId()).getRegulatingTerminal().getBusBreakerView().getBus();
            System.out.println("generator " + g);
            System.out.println("    t1    " + t);
            System.out.println("    b1    " + b1);
            System.out.println("    g2    " + ns.exported.getGenerator(g.getId()));
            System.out.println("    t2    " + ns.exported.getGenerator(g.getId()).getRegulatingTerminal());
            if (b1 == null) {
                assertNull("generator " + g + " b1 null", b2);
            } else {
                assertEquals("generator " + g, b1.getId(), b2.getId());
            }
        }
        // new Comparison(ns.imported, ns.exported, new ComparisonConfig()).compare();
    }

    //@Test
    public void issue1033() throws IOException {
        Path m = Paths.get("/Users/zamarrenolm/works/RTE/data/issue-1033/diagrams_test_case");
        Path bd = Paths.get("/Users/zamarrenolm/works/RTE/data/issue-1033/boundaries");

        String impl = TripleStoreFactory.defaultImplementation();
        CgmesModel cgmes = CgmesModelFactory.create(
            dataSource(m),
            dataSource(bd),
            impl);

        Conversion.Config config = new Conversion.Config();
        config.setConvertSvInjections(true);
        config.setProfileUsedForInitialStateValues(Conversion.Config.StateProfile.SSH.name());
        Conversion c = new Conversion(cgmes, config);
        Network n = c.convert();

        String[] loadBreakSwitches = {
            "_8d9ee2fe-c1e7-5b5c-b2fd-504d17901d52",
            "_acd4e063-5f6f-5a5e-864d-715197a25c2e",
            "_ef1ac69f-7509-55dd-902e-74fccf67976c",
            "_fece819e-733b-5d72-bda5-5a6b9fb55340",
        };
        String[] loads = {
            "_aab761bb-3319-5c95-b82a-692ebec0fb70",
            "_300ee7e4-2c06-5542-bb1c-b2661120f711",
            "_dd2ef4ef-7b2b-51d2-b161-8a90b6eea53a",
            "_e99a2b8d-7173-55e5-bfa7-849ebbcc7c2e"
        };
        String scenario = "missingLoadBreakSwitches";
        for (String loadid : loads) {
            Load load = n.getLoad(loadid);
            assertNotNull(load);
            load.getTerminal().getVoltageLevel().exportTopology(Paths.get("/Users/zamarrenolm/Downloads/" + scenario + "/" + loadid + ".gv"));
        }
        for (String swid : loadBreakSwitches) {
            Switch sw = n.getSwitch(swid);
            assertNotNull(sw);
            sw.getVoltageLevel().exportTopology(Paths.get("/Users/zamarrenolm/Downloads/" + swid + ".gv"));
        }
    }

    //@Test
    public void rteSeptemberLocal() throws IOException {
        Path p = Paths.get("rte", "20190911_1130_FO3_XX4.zip");
        Networks ns = analyze("RTE_20190911_1130", IOP_SEPTEMBER_LOCAL, p);

        for (Load l : ns.imported.getLoads()) {
            if (l.getTerminal() == null) {
                System.err.println(l);
            }
        }
        for (Generator g : ns.imported.getGenerators()) {
            if (g.getTerminal() == null) {
                System.err.println(g);
            }
        }
        for (ShuntCompensator sh : ns.imported.getShuntCompensators()) {
            if (sh.getTerminal() == null) {
                System.err.println(sh);
            }
        }
    }

    //@Test
    public void apgJuneLocal() throws IOException {
        Path p = Paths.get("CE03_BD05062019_1D_APG_BusBranch", "20190605_1130_FO3_AT3.zip");
        analyze("APG_20190605_1130", IOP_JUNE_LOCAL, p);
    }

    //@Test
    public void ostJuneLocal() throws IOException {
        Path p = Paths.get("CE29_BD05062019_1D_OST_BusBranch", "20190605_1130_FO3_AL1.zip");
        analyze("OST_20190605_1130", IOP_JUNE_LOCAL, p);
    }

    //@Test
    public void nosbihSeptember() throws IOException {
        Path p = Paths.get("CE15_BD11092019_1D_NOSBiH_BusBranch", "20190911_1130_FO3_BA0.zip");
        analyze("NOSBiH_20190911_1130", IOP_SEPTEMBER, p);
    }

    //@Test
    public void mepsoJuneLocal() throws IOException {
        Path p = Paths.get("CE14_BD05062019_1D_MEPSO_BusBranch", "20190605_1130_FO3_MK0.zip");
        analyze("MEPSO_20190605_1130", IOP_JUNE_LOCAL, p);
    }

    //@Test
    public void energinetJune() throws IOException {
        Path p = Paths.get("CE09_BD05062019_1D_Energinet_NodeBreaker", "20190605_1130_FO3_DK0.zip");
        analyze("energinetDK0-2019060_1130", IOP_JUNE, p);
    }

    //@Test
    public void transnetJune() throws IOException {
        Path p = Paths.get("CE26_BD05062019_1D_TransnetBW_BusBranch", "20190605_1130_FO3_D40.zip");
        analyze("TransnetBW-20190605_1130", IOP_JUNE, p);
    }

    //@Test
    public void renJune() throws Exception {
        Path p = Paths.get("CE18_BD05062019_1D_REN_BusBranch", "20190605_1130_FO3_PT1.zip");
        analyze("REN_20190605_1130", IOP_JUNE, p);
    }

    //@Test
    public void elesJune() throws Exception {
        Path p = Paths.get("CE06_BD05062019_1D_ELES_NodeBreaker", "20190605_1130_FO3_XX1.zip");
        analyze("ELES_20190605_1130", IOP_JUNE, p);
    }

    //@Test
    public void hopsJune() throws Exception {
        Path p = Paths.get("CE11_BD05062019_1D_HOPS_BusBranch", "20190605_1130_FO3_HR1.zip");
        analyze("HOPS_20190605_1130", IOP_JUNE, p);
    }

    //@Test
    public void reeJune() throws Exception {
        Path p = Paths.get("CE17_BD05062019_1D_REE_BusBranch", "20190605_1130_FO3_ES1.zip");
        analyze("REE_20190605_1130", IOP_JUNE, p);
    }

    //@Test
    public void amprionJune() throws Exception {
        Path p = Paths.get("CE02_BD05062019_1D_Amprion_BusBranch", "20190605_1130_FO3_D71.zip");
        analyze("Amprion_20190605_1130", IOP_JUNE, p);
    }

    //@Test
    public void ternaJune() throws Exception {
        Path p = Paths.get("CE24_BD05062019_1D_Terna_BusBranch", "20190605_1130_FO3_IT0.zip");
        analyze("Terna_20190605_1130", IOP_JUNE, p);
    }

    //@Test
    public void ternaFebruary() throws Exception {
        Path p = Paths.get("CE24_BD05062019_1D_Terna_BusBranch", "20190605_1130_FO3_IT0.zip");
        analyze("Terna_20190605_1130", IOP_JUNE, p);
    }

    //@Test
    public void elesOctober() throws Exception {
        Path p = Paths.get("CE06_BD09102019_ELES_NodeBreaker", "20191009_1130_FO3_SI1.zip");
        analyze("ELES_20191009_1130", IOP_OCTOBER, p);
    }

    //@Test
    public void cepsOctober() throws Exception {
        Path p = Paths.get("CE04_BD09102019_1D_CEPS_BusBranch", "20191009_1130_FO3_CZ0.zip");
        analyze("CEPS_20191009_1130", IOP_OCTOBER, p);
    }

    //@Test
    public void litgridJune() throws Exception {
        Path p = Paths.get("BA03_BD05062019_1D_LITGRID_005_NodeBreaker", "20190605_1130_FO3_LT5.zip");
        Network n = analyze("LITGRID_20190605_1130", IOP_JUNE, p).imported;
        String debugVl = "_31aaa056-79fe-11e6-a326-d89d67d10dc7";
        VoltageLevel vl = n.getVoltageLevel(debugVl);
        vl.exportTopology(Paths.get("/Users/zamarrenolm/Downloads/litgrid-vl-" + debugVl + ".dot"));
    }

    //@Test
    public void reeJuneFixTx098838aa() throws Exception {
        Path p = Paths.get("CE17_BD05062019_1D_REE_BusBranch", "kkfixtx098838aa", "20190605_1130_FO3_ES1-fixed.zip");
        analyze("REE_20190605_1130_fixtx0908838aa", IOP_JUNE, p);
    }

    //@Test
    public void swissgridOctober() throws Exception {
        Path p = Paths.get("CE21_BD09102019_1D_Swissgrid_NodeBreaker", "20191009_1130_FO3_CH2.zip");
        analyze("Swissgrid October", IOP_OCTOBER, p);
    }

    @Test
    public void eleringOctober1130() throws IOException {
        CatalogLocation catalog = IOP_OCTOBER;
        Path p = Paths.get("BA02_BD09102019_1D_Elering_001_NodeBreaker", "20191009_1130_FO3_EE1.zip");
        CgmesModel cgmes = cgmes(catalog, p);

        Conversion c = new Conversion(cgmes);
        Network n = c.convert();

        // Two ways to solve
        // XMLExporter xmlExporter = new
        // XMLExporter(Mockito.mock(PlatformConfig.class));
        FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
        PlatformConfig platformConfig = new InMemoryPlatformConfig(fs);
        XMLExporter xmlExporter = new XMLExporter(platformConfig);
        Path out = Paths.get("\\work\\tmp");
        Properties params = new Properties();
        params.put("iidm.export.xml.topology-level", TopologyLevel.BUS_BREAKER.toString());
        xmlExporter.export(n, params, new FileDataSource(out, "Elering-20191009-1130"));
    }

    //@Test
    public void ttgOctober1130() throws IOException {
        CatalogLocation catalog = IOP_OCTOBER;
        Path p = Paths.get("CE22_BD09102019_1D_TTG", "20191009_1130_FO3_D20.zip");
        CgmesModel cgmes = cgmes(catalog, p);

        Conversion c = new Conversion(cgmes);
        Network n = c.convert();

        // Two ways to solve
        // XMLExporter xmlExporter = new
        // XMLExporter(Mockito.mock(PlatformConfig.class));
        FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
        PlatformConfig platformConfig = new InMemoryPlatformConfig(fs);
        XMLExporter xmlExporter = new XMLExporter(platformConfig);
        Path out = Paths.get("\\work\\tmp");
        Properties params = new Properties();
        params.put("iidm.export.xml.topology-level", TopologyLevel.BUS_BRANCH.toString());
        xmlExporter.export(n, params, new FileDataSource(out, "TTG-20191009-1130-OctoberBoundary-newConversion"));
    }

    //@Test
    public void energinetOctober1130() throws IOException {
        CatalogLocation catalog = IOP_OCTOBER;
        Path p = Paths.get("CE09_BD20191009_1D_Energinet_NodeBreaker", "20191009_1130_FO3_D10.zip");
        CgmesModel cgmes = cgmes(catalog, p);

        Conversion c = new Conversion(cgmes);
        Network n = c.convert();

        // Two ways to solve
        // XMLExporter xmlExporter = new
        // XMLExporter(Mockito.mock(PlatformConfig.class));
        FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
        PlatformConfig platformConfig = new InMemoryPlatformConfig(fs);
        XMLExporter xmlExporter = new XMLExporter(platformConfig);
        Path out = Paths.get("\\work\\tmp");
        Properties params = new Properties();
        params.put("iidm.export.xml.topology-level", TopologyLevel.NODE_BREAKER.toString());
        xmlExporter.export(n, params, new FileDataSource(out, "Energinet-Oct-1130"));
    }

    //@Test
    public void energinetDkOctober1130() throws IOException {
        CatalogLocation catalog = IOP_OCTOBER;
        Path p = Paths.get("CE09_BD20191009_1D_Energinet_NodeBreaker", "20191009_1130_FO3_DK0.zip");
        CgmesModel cgmes = cgmes(catalog, p);

        Conversion c = new Conversion(cgmes);
        Network n = c.convert();

        logHvdcConverterStations(n);
        logHvdcLines(n);

        // Two ways to solve
        // XMLExporter xmlExporter = new
        // XMLExporter(Mockito.mock(PlatformConfig.class));
        FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
        PlatformConfig platformConfig = new InMemoryPlatformConfig(fs);
        XMLExporter xmlExporter = new XMLExporter(platformConfig);
        Path out = Paths.get("\\work\\tmp");
        Properties params = new Properties();
        params.put("iidm.export.xml.topology-level", TopologyLevel.NODE_BREAKER.toString());
        xmlExporter.export(n, params, new FileDataSource(out, "Energinet-DK-Oct-1130"));
    }

    //@Test
    public void eirgridsoniOctober1130() throws IOException {
        CatalogLocation catalog = IOP_OCTOBER;
        Path p = Paths.get("IE00_BD09102019_1D_EIRGRIDSONI_NodeBreaker", "20191009_1130_FO3_IE1.zip");
        CgmesModel cgmes = cgmes(catalog, p);

        Conversion c = new Conversion(cgmes);
        Network n = c.convert();

        logHvdcConverterStations(n);
        logHvdcLines(n);

        // Two ways to solve
        // XMLExporter xmlExporter = new
        // XMLExporter(Mockito.mock(PlatformConfig.class));
        FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
        PlatformConfig platformConfig = new InMemoryPlatformConfig(fs);
        XMLExporter xmlExporter = new XMLExporter(platformConfig);
        Path out = Paths.get("\\work\\tmp");
        Properties params = new Properties();
        params.put("iidm.export.xml.topology-level", TopologyLevel.NODE_BREAKER.toString());
        xmlExporter.export(n, params, new FileDataSource(out, "Eirgridsoni-Oct-1130"));
    }

    @Test
    public void eirgridsoniFebruary20201030() throws IOException {
        CatalogLocation catalog = IOP_FEBRUARY_2020;
        Path p = Paths.get("IE00_BD12022020_1D_EIRGRIDSONI_Nodebreaker", "20200212_1030_FO3_IE1.zip");
        CgmesModel cgmes = cgmes(catalog, p);

        Conversion c = new Conversion(cgmes);
        Network n = c.convert();

        logHvdcConverterStations(n);
        logHvdcLines(n);

        // Two ways to solve
        // XMLExporter xmlExporter = new
        // XMLExporter(Mockito.mock(PlatformConfig.class));
        FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
        PlatformConfig platformConfig = new InMemoryPlatformConfig(fs);
        XMLExporter xmlExporter = new XMLExporter(platformConfig);
        Path out = Paths.get("\\work\\tmp");
        Properties params = new Properties();
        params.put("iidm.export.xml.topology-level", TopologyLevel.NODE_BREAKER.toString());
        xmlExporter.export(n, params, new FileDataSource(out, "Eirgridsoni-Feb-2020-1030"));
    }

    @Test
    public void anneSmallGridTestConfigurationHvdcComplete() throws IOException {

        CatalogLocation catalog = IOP_ANNE;
        Path p = Paths.get("hvdc", "CGMES_v2-4-15_SmallGridTestConfiguration_HVDC_Complete_v3-0-0.zip");
        CgmesModel cgmes = cgmes(catalog, p);

        Conversion c = new Conversion(cgmes);
        Network n = c.convert();

        // Two ways to solve
        // XMLExporter xmlExporter = new
        // XMLExporter(Mockito.mock(PlatformConfig.class));
        FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
        PlatformConfig platformConfig = new InMemoryPlatformConfig(fs);
        XMLExporter xmlExporter = new XMLExporter(platformConfig);
        Path out = Paths.get("\\work\\tmp");
        Properties params = new Properties();
        params.put("iidm.export.xml.topology-level", TopologyLevel.NODE_BREAKER.toString());
        xmlExporter.export(n, params, new FileDataSource(out, "SmallGridTestConfiguration_HVDC"));
    }

    //@Test
    public void rteFranceOctober1130() throws IOException {
        CatalogLocation catalog = IOP_OCTOBER;
        Path p = Paths.get("CE19_BD09102019_1D_RTEFRANCE_BusBranch", "20191009_1130_FO3_XX0.zip");
        CgmesModel cgmes = cgmes(catalog, p);

        Conversion c = new Conversion(cgmes);
        Network n = c.convert();

        // Two ways to solve
        // XMLExporter xmlExporter = new
        // XMLExporter(Mockito.mock(PlatformConfig.class));
        FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
        PlatformConfig platformConfig = new InMemoryPlatformConfig(fs);
        XMLExporter xmlExporter = new XMLExporter(platformConfig);
        Path out = Paths.get("\\work\\tmp");
        Properties params = new Properties();
        params.put("iidm.export.xml.topology-level", TopologyLevel.NODE_BREAKER.toString());
        xmlExporter.export(n, params, new FileDataSource(out, "RTE-Oct-1130"));
    }

    //@Test
    public void iptoDecember20191030() throws IOException {
        CatalogLocation catalog = IOP_DECEMBER_2019;
        Path p = Paths.get("CE12_BD04122019_1D_IPTO_NodeBreaker", "20191204_1030_FO3_GR1.zip");
        CgmesModel cgmes = cgmes(catalog, p);

        Conversion c = new Conversion(cgmes);
        Network n = c.convert();

        // Two ways to solve
        // XMLExporter xmlExporter = new
        // XMLExporter(Mockito.mock(PlatformConfig.class));
        FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
        PlatformConfig platformConfig = new InMemoryPlatformConfig(fs);
        XMLExporter xmlExporter = new XMLExporter(platformConfig);
        Path out = Paths.get("\\work\\tmp");
        Properties params = new Properties();
        params.put("iidm.export.xml.topology-level", TopologyLevel.BUS_BRANCH.toString());
        validateBusBalances(n, 0.01);
        //xmlExporter.export(n, params, new FileDataSource(out, "ttg-jan-2019-0030"));
    }

    //@Test
    public void ttgJanuary20190030() throws IOException {
        CatalogLocation catalog = IOP_JANUARY_2019;
        Path p = Paths.get("CE22_BD16012019_1D_TTG", "20190116_0030_FO3_D20.zip");
        CgmesModel cgmes = cgmes(catalog, p);

        Conversion c = new Conversion(cgmes);
        Network n = c.convert();

        // Two ways to solve
        // XMLExporter xmlExporter = new
        // XMLExporter(Mockito.mock(PlatformConfig.class));
        FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
        PlatformConfig platformConfig = new InMemoryPlatformConfig(fs);
        XMLExporter xmlExporter = new XMLExporter(platformConfig);
        Path out = Paths.get("\\work\\tmp");
        Properties params = new Properties();
        params.put("iidm.export.xml.topology-level", TopologyLevel.BUS_BRANCH.toString());
        validateBusBalances(n, 0.01);
        //xmlExporter.export(n, params, new FileDataSource(out, "ttg-jan-2019-0030"));
    }

    //@Test
    public void ttgFebruary20201030() throws IOException {
        CatalogLocation catalog = IOP_FEBRUARY_2020;
        Path p = Paths.get("CE22_BD12022020_1D_TTG", "20200212_1030_FO3_D20.zip");

        CgmesModel cgmes = cgmes(catalog, p);

        Conversion c = new Conversion(cgmes);
        Network n = c.convert();

        // Two ways to solve
        // XMLExporter xmlExporter = new
        // XMLExporter(Mockito.mock(PlatformConfig.class));
        FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
        PlatformConfig platformConfig = new InMemoryPlatformConfig(fs);
        XMLExporter xmlExporter = new XMLExporter(platformConfig);
        Path out = Paths.get("\\work\\tmp");
        Properties params = new Properties();
        params.put("iidm.export.xml.topology-level", TopologyLevel.NODE_BREAKER.toString());
        xmlExporter.export(n, params, new FileDataSource(out, "TTG-2020-February-1030"));
    }

    //@Test
    public void emsFebruary20201030() throws IOException {
        CatalogLocation catalog = IOP_FEBRUARY_2020;
        Path p = Paths.get("CE08_BD12022020_1D_EMS_BusBranch", "20200212_1030_FO3_RS0.zip");

        CgmesModel cgmes = cgmes(catalog, p);

        Conversion c = new Conversion(cgmes);
        Network n = c.convert();

        // Two ways to solve
        // XMLExporter xmlExporter = new
        // XMLExporter(Mockito.mock(PlatformConfig.class));
        FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
        PlatformConfig platformConfig = new InMemoryPlatformConfig(fs);
        XMLExporter xmlExporter = new XMLExporter(platformConfig);
        Path out = Paths.get("\\work\\tmp");
        Properties params = new Properties();
        params.put("iidm.export.xml.topology-level", TopologyLevel.NODE_BREAKER.toString());
        xmlExporter.export(n, params, new FileDataSource(out, "EMS-2020-February-1030"));
    }

    //@Test
    public void mavirFebruary20201030() throws IOException {
        CatalogLocation catalog = IOP_FEBRUARY_2020;
        Path p = Paths.get("CE13_BD12022020_1D_MAVIR_NodeBreaker", "20200212_1030_FO3_HU1.zip");

        CgmesModel cgmes = cgmes(catalog, p);

        Conversion c = new Conversion(cgmes);
        Network n = c.convert();

        // Two ways to solve
        // XMLExporter xmlExporter = new
        // XMLExporter(Mockito.mock(PlatformConfig.class));
        FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
        PlatformConfig platformConfig = new InMemoryPlatformConfig(fs);
        XMLExporter xmlExporter = new XMLExporter(platformConfig);
        Path out = Paths.get("\\work\\tmp");
        Properties params = new Properties();
        params.put("iidm.export.xml.topology-level", TopologyLevel.NODE_BREAKER.toString());
        xmlExporter.export(n, params, new FileDataSource(out, "Mavir-2020-February-1030"));
    }

    //@Test
    public void tennetOctober20191130() throws IOException {
        CatalogLocation catalog = IOP_OCTOBER;
        Path p = Paths.get("CE23_BD09102019_1D_TenneT_NodeBreaker", "20191009_1130_FO3_NL1.zip");

        CgmesModel cgmes = cgmes(catalog, p);

        //List<CgmesImportPostProcessor> postProcessors = new ArrayList<>();
        //postProcessors.add(new PhaseAngleClock());
        //Conversion c = new Conversion(cgmes, new Config(), postProcessors);
        Conversion c = new Conversion(cgmes);
        Network n = c.convert();

        // Two ways to solve
        // XMLExporter xmlExporter = new
        // XMLExporter(Mockito.mock(PlatformConfig.class));
        FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
        PlatformConfig platformConfig = new InMemoryPlatformConfig(fs);
        XMLExporter xmlExporter = new XMLExporter(platformConfig);
        Path out = Paths.get("\\work\\tmp");
        Properties params = new Properties();
        params.put("iidm.export.xml.topology-level", TopologyLevel.NODE_BREAKER.toString());
        xmlExporter.export(n, params, new FileDataSource(out, "Tennet-2019-October-1130"));
    }

    @Test
    public void tennetFebruary20201030() throws IOException {
        CatalogLocation catalog = IOP_FEBRUARY_2020;
        Path p = Paths.get("CE23_BD12022020_1D_TenneT_NodeBreaker", "20200217_1030_FO1_NL1.zip");

        CgmesModel cgmes = cgmes(catalog, p);

        Conversion.Config config = new Conversion.Config();
        //List<CgmesImportPostProcessor> postProcessors = new ArrayList<>();
        //postProcessors.add(new PhaseAngleClock());
        //Conversion c = new Conversion(cgmes, config, postProcessors);
        Conversion c = new Conversion(cgmes, config);
        Network n = c.convert();

        // Two ways to solve
        // XMLExporter xmlExporter = new
        // XMLExporter(Mockito.mock(PlatformConfig.class));
        FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
        PlatformConfig platformConfig = new InMemoryPlatformConfig(fs);
        XMLExporter xmlExporter = new XMLExporter(platformConfig);
        Path out = Paths.get("\\work\\tmp");
        Properties params = new Properties();
        params.put("iidm.export.xml.topology-level", TopologyLevel.NODE_BREAKER.toString());
        xmlExporter.export(n, params, new FileDataSource(out, "Tennet-2020-February-1030"));

        //double threshold = 0.01;
        //ValidationConfig vconfig = loadFlowValidationConfig(threshold);
        //LoadFlowParameters lfParameters = defineLoadflowParameters(vconfig.getLoadFlowParameters(), config);
        //ConversionTester.computeMissingFlows(n, lfParameters);
    }

    private LoadFlowParameters defineLoadflowParameters(LoadFlowParameters loadFlowParameters, Conversion.Config config) {
        LoadFlowParameters copyLoadFlowParameters = loadFlowParameters.copy();
        return copyLoadFlowParameters;
    }

    //@Test
    public void ternaFebruary20201130() throws IOException {
        CatalogLocation catalog = IOP_FEBRUARY_2020;
        Path p = Paths.get("CE24_BD12022020_1D_Terna_BusBranch", "20200212_1130_FO3_IT0.zip");

        CgmesModel cgmes = cgmes(catalog, p);

        //List<CgmesImportPostProcessor> postProcessors = new ArrayList<>();
        //postProcessors.add(new PhaseAngleClock());
        //Conversion c = new Conversion(cgmes, postProcessors);
        Conversion c = new Conversion(cgmes);
        Network n = c.convert();

        // Two ways to solve
        // XMLExporter xmlExporter = new
        // XMLExporter(Mockito.mock(PlatformConfig.class));
        FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
        PlatformConfig platformConfig = new InMemoryPlatformConfig(fs);
        XMLExporter xmlExporter = new XMLExporter(platformConfig);
        Path out = Paths.get("\\work\\tmp");
        Properties params = new Properties();
        params.put("iidm.export.xml.topology-level", TopologyLevel.NODE_BREAKER.toString());
        xmlExporter.export(n, params, new FileDataSource(out, "Terna-2020-February-1130"));
    }

    //@Test
    public void swissgridFebruary20201130() throws IOException {
        CatalogLocation catalog = IOP_FEBRUARY_2020;
        Path p = Paths.get("CE21_BD12022020_1D_Swissgrid_NodeBreaker", "20200212_1130_FO3_CH0.zip");

        CgmesModel cgmes = cgmes(catalog, p);

        //List<CgmesImportPostProcessor> postProcessors = new ArrayList<>();
        //postProcessors.add(new PhaseAngleClock());
        //Conversion c = new Conversion(cgmes, postProcessors);
        Conversion c = new Conversion(cgmes);
        Network n = c.convert();

        // Two ways to solve
        // XMLExporter xmlExporter = new
        // XMLExporter(Mockito.mock(PlatformConfig.class));
        FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
        PlatformConfig platformConfig = new InMemoryPlatformConfig(fs);
        XMLExporter xmlExporter = new XMLExporter(platformConfig);
        Path out = Paths.get("\\work\\tmp");
        Properties params = new Properties();
        params.put("iidm.export.xml.topology-level", TopologyLevel.NODE_BREAKER.toString());
        xmlExporter.export(n, params, new FileDataSource(out, "Swissgrid-2020-February-1130"));
    }

    @Test
    public void all2019October1030() throws IOException {
        CatalogLocation catalog = IOP_OCTOBER;
        String selection = "glob:**_1D_**20191009*_1030*.zip";

        Map<String, Integer> conversionMilliseconds = new HashMap<>();
        reviewAll(catalog, selection, p -> {
            String modelName = modelName(catalog, p);

            if (!tsoNameFromPathname(modelName).contains("Energinet")) {
                return;
            }

            CgmesModel cgmes = null;
            try {
                cgmes = cgmes(catalog, p);
            } catch (Exception x) {
                System.err.println(x);
            }
            if (cgmes != null) {

                // List<CgmesImportPostProcessor> postProcessors = new ArrayList<>();
                // postProcessors.add(new PhaseAngleClock());
                // Conversion c = new Conversion(cgmes, postProcessors);

                long startTime = System.currentTimeMillis();

                Conversion c = new Conversion(cgmes);
                Network n = c.convert();

                long endTime = System.currentTimeMillis();
                long searchTime = endTime - startTime;

                conversionMilliseconds.put(modelName, (int) searchTime);

                // Two ways to solve
                // XMLExporter xmlExporter = new
                // XMLExporter(Mockito.mock(PlatformConfig.class));
                FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
                PlatformConfig platformConfig = new InMemoryPlatformConfig(fs);
                XMLExporter xmlExporter = new XMLExporter(platformConfig);
                Path out = Paths.get("\\work\\tmp");
                Properties params = new Properties();
                params.put("iidm.export.xml.topology-level", TopologyLevel.NODE_BREAKER.toString());
                String tsoName = tsoNameFromPathname(modelName);
                xmlExporter.export(n, params, new FileDataSource(out, String.format("%s-2019-October-1030", tsoName)));
            }
        });
        System.out.println("Conversion Milliseconds");
        conversionMilliseconds.forEach((key, value) -> System.out.println(key + " " + value));
    }

    @Test
    public void all2020February1030() throws IOException {
        CatalogLocation catalog = IOP_FEBRUARY_2020;
        String selection = "glob:**_1D_**20200212*_1030*.zip";

        Map<String, Integer> conversionMilliseconds = new HashMap<>();
        reviewAll(catalog, selection, p -> {
            String modelName = modelName(catalog, p);

            System.err.printf("ModelName (%s) %n", modelName);
            //if (tsoNameFromPathname(modelName).contains("NGESO") || !tsoNameFromPathname(modelName).contains("ESO")) {
            if (!tsoNameFromPathname(modelName).contains("EIRGRIDSONI")) {
                return;
            }
            System.err.printf("JAM model name %s %n", modelName);

            CgmesModel cgmes = null;
            try {
                cgmes = cgmes(catalog, p);
            } catch (Exception x) {
                System.err.println(x);
            }
            if (cgmes != null) {

                // List<CgmesImportPostProcessor> postProcessors = new ArrayList<>();
                // postProcessors.add(new PhaseAngleClock());
                // Conversion c = new Conversion(cgmes, postProcessors);

                long startTime = System.currentTimeMillis();
                Conversion c = new Conversion(cgmes);
                Network n = c.convert();
                long endTime = System.currentTimeMillis();
                long searchTime = endTime - startTime;

                conversionMilliseconds.put(modelName, (int) searchTime);

                // Export using only memory information
                Path out = Paths.get("\\work\\tmp");
                Properties params = new Properties();
                params.put("iidm.export.xml.topology-level", TopologyLevel.BUS_BRANCH.toString());
                params.put("cgmes.export.usingOnlyNetwork", "true");
                new CgmesExport().export(n, params, new FileDataSource(out, "foo"));

                // Two ways to solve
                // XMLExporter xmlExporter = new
                // XMLExporter(Mockito.mock(PlatformConfig.class));

                /***
                FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
                PlatformConfig platformConfig = new InMemoryPlatformConfig(fs);
                XMLExporter xmlExporter = new XMLExporter(platformConfig);
                Path out = Paths.get("\\work\\tmp");
                Properties params = new Properties();
                params.put("iidm.export.xml.topology-level", TopologyLevel.BUS_BRANCH.toString());
                String tsoName = tsoNameFromPathname(modelName);
                xmlExporter.export(n, params, new FileDataSource(out, String.format("%s-2020-February-1030-BusBranch", tsoName)));
                ***/
            }
        });
        System.out.println("Conversion Milliseconds");
        conversionMilliseconds.forEach((key, value) -> System.out.println(key + " " + value));
    }

    @Test
    public void all2020February1030Svc() throws IOException {
        CatalogLocation catalog = IOP_FEBRUARY_2020;
        String selection = "glob:**_1D_**20200212*_1030*.zip";

        Map<String, Svc> svclog = new HashMap<>();
        reviewAll(catalog, selection, p -> {
            String modelName = modelName(catalog, p);

            // if (!tsoNameFromPathname(modelName).contains("GESO")) {
            // return;
            // }

            CgmesModel cgmes = null;
            try {
                cgmes = cgmes(catalog, p);
            } catch (Exception x) {
                System.err.println(x);
            }
            if (cgmes != null) {

                Conversion c = new Conversion(cgmes);
                Network n = c.convert();

                int slopeZero = 0;
                int slopeNonZero = 0;

                // No se registra el slope asi que temporalmente lo he registrado en el Bmin + 1.0
                // Para utilizar esta rutina hay que actualizar donde se registra el slope
                slopeZero = (int) n.getStaticVarCompensatorStream().filter(s -> s.getBmin() == 1.0).count();
                slopeNonZero = (int) n.getStaticVarCompensatorStream().filter(s -> s.getBmin() != 1.0).count();

                Svc svc = new Svc(tsoNameFromPathname(modelName), slopeZero, slopeNonZero);
                svclog.put(modelName, svc);
            }
        });
        System.out.println("Conversion Milliseconds");
        svclog.forEach((key, value) -> System.out
            .println(key + " " + value.tso + " Zero " + value.slopeZero + " Nonzero " + value.slopeNonZero));
    }

    static class Svc {
        String tso;
        int slopeZero;
        int slopeNonZero;

        Svc(String tso, int slopeZero, int slopeNonZero) {
            this.tso = tso;
            this.slopeZero = slopeZero;
            this.slopeNonZero = slopeNonZero;
        }
    }

    @Test
    public void all2020February1030LinesDifferenVnom() throws IOException {
        CatalogLocation catalog = IOP_FEBRUARY_2020;
        String selection = "glob:**_1D_**20200212*_1030*.zip";

        Conversion.Config config = new Conversion.Config();
        config.setConvertBoundary(true);

        Map<String, Integer> conversionMilliseconds = new HashMap<>();
        reviewAll(catalog, selection, p -> {
            String modelName = modelName(catalog, p);

            System.err.printf("ModelName (%s) %n", modelName);
            if (!tsoNameFromPathname(modelName).contains("Amprion")) {
                return;
            }

            CgmesModel cgmes = null;
            try {
                cgmes = cgmes(catalog, p);
            } catch (Exception x) {
                System.err.println(x);
            }
            if (cgmes != null) {

                System.err.printf("JAM Tso %s model %s %n", tsoNameFromPathname(modelName), modelName);
                Conversion c = new Conversion(cgmes, config);
                Network n = c.convert();

                n.getLines().forEach(line -> {
                    checkLine(line);
                });

            }
        });
    }

    private void checkLine(Line line) {
        //if (line.getTerminal1().getVoltageLevel().getNominalV() == line.getTerminal2().getVoltageLevel().getNominalV()) {
        //    return;
        //}
        System.err.printf("JAM %n");
        double u1 = Double.NaN;
        double theta1 = Double.NaN;
        Bus bus1 = line.getTerminal1().getBusView().getBus();
        if (bus1 != null) {
            u1 = bus1.getV();
            theta1 = Math.toRadians(bus1.getAngle());
        }
        double u2 = Double.NaN;
        double theta2 = Double.NaN;
        Bus bus2 = line.getTerminal2().getBusView().getBus();
        if (bus2 != null) {
            u2 = bus2.getV();
            theta2 = Math.toRadians(bus2.getAngle());
        }
        double p1 = line.getTerminal1().getP();
        double q1 = line.getTerminal1().getQ();
        double p2 = line.getTerminal2().getP();
        double q2 = line.getTerminal2().getQ();
        double r = line.getR();
        double x = line.getX();
        double g1 = line.getG1();
        double b1 = line.getB1();
        double g2 = line.getG2();
        double b2 = line.getB2();
        LinkData.BranchAdmittanceMatrix adm = LinkData.calculateBranchAdmittance(r, x, 1.0, 0.0, 1.0, 0.0,
            new Complex(g1, b1), new Complex(g2, b2));

        if (!Double.isNaN(p1) && !Double.isNaN(q1) && !Double.isNaN(p2) && !Double.isNaN(q2) && !Double.isNaN(u1)
            && !Double.isNaN(theta1) && Double.isNaN(u2) && Double.isNaN(theta2)) {
            Complex s1 = new Complex(p1, q1);
            Complex v1 = ComplexUtils.polar2Complex(u1, theta1);
            Complex v2 = ((s1.conjugate().divide(v1.conjugate())).subtract(adm.y11().multiply(v1))).divide(adm.y12());
            Complex s2 = (adm.y21().multiply(v1).add(adm.y22().multiply(v2))).multiply(v2.conjugate()).conjugate();

            System.err.printf("Line Id %s Name %s %n", line.getId(), line.getName());
            System.err.printf("    Vnom1 %f Vnom2 %f %n", line.getTerminal1().getVoltageLevel().getNominalV(),
                line.getTerminal2().getVoltageLevel().getNominalV());
            System.err.printf("    R %f X %f g1 %f b1 %f g2 %f b2 %f %n", r, x, g1, b1, g2, b2);
            System.err.printf("    u1 %f angle1 %f %n", u1, Math.toDegrees(theta1));
            System.err.printf("    P1 %f q1 %f p2 %f q2 %f %n", p1, q1, p2, q2);
            System.err.printf("    calculated u2 %f angle2 %f %n", v2.abs(), Math.toDegrees(v2.getArgument()));
            System.err.printf("    calculated p2 %f q2 %f errP %f errQ %f %n", s2.getReal(), s2.getImaginary(), p2 - s2.getReal(), q2 - s2.getImaginary());
        } else if (!Double.isNaN(p1) && !Double.isNaN(q1) && !Double.isNaN(p2) && !Double.isNaN(q2) && !Double.isNaN(u2)
            && !Double.isNaN(theta2) && Double.isNaN(u1) && Double.isNaN(theta1)) {
            Complex s2 = new Complex(p2, q2);
            Complex v2 = ComplexUtils.polar2Complex(u2, theta2);
            Complex v1 = ((s2.conjugate().divide(v2.conjugate())).subtract(adm.y22().multiply(v2))).divide(adm.y21());
            Complex s1 = (adm.y11().multiply(v1).add(adm.y12().multiply(v2))).multiply(v1.conjugate()).conjugate();

            System.err.printf("Line Id %s Name %s %n", line.getId(), line.getName());
            System.err.printf("    Vnom1 %f Vnom2 %f %n", line.getTerminal1().getVoltageLevel().getNominalV(),
                line.getTerminal2().getVoltageLevel().getNominalV());
            System.err.printf("    R %f X %f g1 %f b1 %f g2 %f b2 %f %n", r, x, g1, b1, g2, b2);
            System.err.printf("    u2 %f angle2 %f %n", u2, Math.toDegrees(theta2));
            System.err.printf("    P1 %f q1 %f p2 %f q2 %f %n", p1, q1, p2, q2);
            System.err.printf("    calculated u1 %f angle1 %f %n", v1.abs(), Math.toDegrees(v1.getArgument()));
            System.err.printf("    calculated p1 %f q1 %f errP %f errQ %f %n", s1.getReal(), s1.getImaginary(), p1 - s1.getReal(), q1 - s1.getImaginary());
        } else if (!Double.isNaN(p1) && !Double.isNaN(q1) && !Double.isNaN(p2) && !Double.isNaN(q2) && !Double.isNaN(u1)
            && !Double.isNaN(theta1) && !Double.isNaN(u2) && !Double.isNaN(theta2)) {
            Complex v1 = ComplexUtils.polar2Complex(u1, theta1);
            Complex v2 = ComplexUtils.polar2Complex(u2, theta2);
            Complex s1 = (adm.y11().multiply(v1).add(adm.y12().multiply(v2))).multiply(v1.conjugate()).conjugate();
            Complex s2 = (adm.y21().multiply(v1).add(adm.y22().multiply(v2))).multiply(v2.conjugate()).conjugate();

            System.err.printf("Line Id %s Name %s %n", line.getId(), line.getName());
            System.err.printf("    Vnom1 %f Vnom2 %f %n", line.getTerminal1().getVoltageLevel().getNominalV(),
                line.getTerminal2().getVoltageLevel().getNominalV());
            System.err.printf("    R %f X %f g1 %f b1 %f g2 %f b2 %f %n", r, x, g1, b1, g2, b2);
            System.err.printf("    u1 %f angle1 %f %n", u1, Math.toDegrees(theta1));
            System.err.printf("    u2 %f angle2 %f %n", u2, Math.toDegrees(theta2));
            System.err.printf("    P1 %f q1 %f p2 %f q2 %f %n", p1, q1, p2, q2);
            System.err.printf("    calculated p1 %f q1 %f errP %f errQ %f %n", s1.getReal(), s1.getImaginary(),
                p1 - s1.getReal(), q1 - s1.getImaginary());
            System.err.printf("    calculated p2 %f q2 %f errP %f errQ %f %n", s2.getReal(), s2.getImaginary(),
                p2 - s2.getReal(), q2 - s2.getImaginary());
        } else if (!Double.isNaN(u1) && !Double.isNaN(theta1) && !Double.isNaN(u2) && !Double.isNaN(theta2)) {
            System.err.printf("Line with voltages at both ends Id %s Name %s %n", line.getId(), line.getName());
            System.err.printf("    P1 %f q1 %f p2 %f q2 %f %n", p1, q1, p2, q2);

            Complex v1 = ComplexUtils.polar2Complex(u1, theta1);
            Complex v2 = ComplexUtils.polar2Complex(u2, theta2);
            Complex s1 = (adm.y11().multiply(v1).add(adm.y12().multiply(v2))).multiply(v1.conjugate()).conjugate();
            Complex s2 = (adm.y21().multiply(v1).add(adm.y22().multiply(v2))).multiply(v2.conjugate()).conjugate();

            System.err.printf("Line Id %s Name %s %n", line.getId(), line.getName());
            System.err.printf("    Bus1 Id %s Bus2 Id %s %n", bus1.getId(), bus2.getId());
            System.err.printf("    Vnom1 %f Vnom2 %f %n", line.getTerminal1().getVoltageLevel().getNominalV(),
                line.getTerminal2().getVoltageLevel().getNominalV());
            System.err.printf("    R %f X %f g1 %f b1 %f g2 %f b2 %f %n", r, x, g1, b1, g2, b2);
            System.err.printf("    u1 %f angle1 %f %n", u1, Math.toDegrees(theta1));
            System.err.printf("    u2 %f angle2 %f %n", u2, Math.toDegrees(theta2));
            System.err.printf("    P1 %f q1 %f p2 %f q2 %f %n", p1, q1, p2, q2);
            System.err.printf("    calculated p1 %f q1 %f %n", s1.getReal(), s1.getImaginary());
            System.err.printf("    calculated p2 %f q2 %f %n", s2.getReal(), s2.getImaginary());

        } else {
            System.err.printf("Line not checked Id %s Name %s %n", line.getId(), line.getName());
        }
    }

    @Test
    public void ngeso2020May() throws IOException {
        CatalogLocation catalog = IOP_OTHER;
        Path p = Paths.get("NGESO", "20200517T2230Z_1D_NG_fixed.zip");
        CgmesModel cgmes = cgmes(catalog, p);

        List<CgmesImportPostProcessor> postProcessors = new ArrayList<>();
        postProcessors.add(new PhaseAngleClock());
        Conversion c = new Conversion(cgmes, new Config(), postProcessors);
        //Conversion c = new Conversion(cgmes);
        Network n = c.convert();

        // Two ways to solve
        // XMLExporter xmlExporter = new
        // XMLExporter(Mockito.mock(PlatformConfig.class));
        FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
        PlatformConfig platformConfig = new InMemoryPlatformConfig(fs);
        XMLExporter xmlExporter = new XMLExporter(platformConfig);
        Path out = Paths.get("\\work\\tmp");
        Properties params = new Properties();
        params.put("iidm.export.xml.topology-level", TopologyLevel.NODE_BREAKER.toString());
        xmlExporter.export(n, params, new FileDataSource(out, "NGESO-2020-May-PhaseAngleClock-fixed"));
    }

    @Test
    public void microGridBaseCaseAssembled() throws Exception {

        Properties iparams = new Properties();
        iparams.put(CgmesImport.PROFILE_USED_FOR_INITIAL_STATE_VALUES, "SV");
        iparams.put(CgmesImport.CONVERT_BOUNDARY, "true");
        Network assembled = Importers.importData("CGMES", CgmesConformity1Catalog.microGridBaseCaseAssembled().dataSource(), iparams);

        assembled.getLines().forEach(line -> {
            System.err.printf("--->       LineId %s %s  %n", line.getId(), line.getName());
            BranchData branchData = new BranchData(line, 0.0, false);

            System.err.printf("    From computed P1 %f Q1 %f %n", branchData.getComputedP1(), branchData.getComputedQ1());
            System.err.printf("    From computed P2 %f Q2 %f %n", branchData.getComputedP2(), branchData.getComputedQ2());
        });

        /***
        assembled.getTwoWindingsTransformers().forEach(twt -> {
            System.err.printf("--->       T2wtId %s %s  %n", twt.getId(), twt.getName());
            BranchData branchData = new BranchData(twt, 0.0, false, false);

            System.err.printf("    From computed P1 %f Q1 %f %n", branchData.getComputedP1(), branchData.getComputedQ1());
            System.err.printf("    From computed P2 %f Q2 %f %n", branchData.getComputedP2(), branchData.getComputedQ2());
        });
        ***/

        FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
        PlatformConfig platformConfig = new InMemoryPlatformConfig(fs);
        XMLExporter xmlExporter = new XMLExporter(platformConfig);
        Path out = Paths.get("\\work\\tmp");
        Properties params = new Properties();
        params.put("iidm.export.xml.topology-level", TopologyLevel.NODE_BREAKER.toString());
        xmlExporter.export(assembled, params, new FileDataSource(out, "microGridBaseCaseAssembled"));
    }

    //@Test
    public void allCountDCLineSegments() throws Exception {
        CatalogLocation catalog = IOP_OCTOBER;
        String selection = "glob:**_1D_**201910*_1130*.zip";

        Map<String, Integer> sizes = new HashMap<>();
        Map<String, Integer> numDcLineSegments = new HashMap<>();
        reviewAll(catalog, selection, p -> {
            String modelName = modelName(catalog, p);
            System.out.println("");
            System.out.println(modelName);
            System.out.println("");
            CgmesModel cgmes = null;
            try {
                cgmes = cgmes(catalog, p);
            } catch (Exception x) {
                System.err.println(x);
            }
            if (cgmes != null) {
                sizes.put(modelName, cgmes.connectivityNodes().size());
                numDcLineSegments.put(modelName, cgmes.dcLineSegments().size());
            }
        });
        assertFalse(sizes.isEmpty());
        System.out.println("case size numDCLineSegments");
        sizes.forEach((m, size) -> System.out.println(m + " " + size + " " + numDcLineSegments.get(m)));
    }

    //@Test
    public void allCheckPhaseAngleClocks() throws Exception {
        CatalogLocation catalog = IOP_OCTOBER;
        String selection = "glob:**201910*_1130*.zip";

        Map<String, String> samples1 = new HashMap<>();
        Map<String, String> samples2 = new HashMap<>();
        reviewAll(catalog, selection, p -> {
            String modelName = modelName(catalog, p);
            System.out.println("");
            System.out.println(modelName);
            System.out.println("");
            CgmesModel cgmes = null;
            try {
                cgmes = cgmes(catalog, p);
            } catch (Exception x) {
                System.err.println(x);
            }
            if (cgmes != null) {
                cgmes.groupedTransformerEnds().values().stream()
                    .filter(tends -> tends.size() == 2)
                    .forEach(tends -> {
                        String id = tends.get(0).getLocal("PowerTransformer");
                        int pac1 = tends.get(0).asInt("phaseAngleClock", 0);
                        int pac2 = tends.get(1).asInt("phaseAngleClock", 0);
                        int n = 0;
                        n += pac1 > 0 ? 1 : 0;
                        n += pac2 > 0 ? 1 : 0;
                        if (n == 1) {
                            samples1.put(modelName, id);
                        } else if (n > 1) {
                            samples2.put(modelName, id);
                        }
                    });
            }
        });
        System.out.println("case powerTransformer with 1 pac");
        samples1.forEach((m, id) -> System.out.println(m + " " + id));
        System.out.println("case powerTransformer with 2 pac");
        samples2.forEach((m, id) -> System.out.println(m + " " + id));
    }

    static class Networks {
        Network imported;
        Network exported;
    }

    private Networks analyze(String name, CatalogLocation location, Path rpath) throws IOException {
        Conversion.Config config = new Conversion.Config();
        config.setConvertSvInjections(true);
        config.setProfileUsedForInitialStateValues(Conversion.Config.StateProfile.SV.name());
        Conversion c = new Conversion(cgmes(location, rpath), config);
        Network n = c.convert();

        //Switch sw = n.getSwitch("_1960c4e3-579d-4e35-82c0-ec64b03164f4");
        //if (sw != null) {
        //    sw.getVoltageLevel().exportTopology(Paths.get("/Users/zamarrenolm/Downloads/T41.gv"));
        //}

        String version = System.getProperty("cgmesConversionVersion");
        String s = System.getProperty("cgmesConversionValidationInvalidateFlows");
        if (s != null && Boolean.parseBoolean(s)) {
            invalidateFlows(n);
        }
        validateBusBalances(n, 0.01);
        XMLExporter xmlExporter = new XMLExporter(Mockito.mock(PlatformConfig.class));
        Path out = Paths.get("C:\\work\\tmp\\");
        Properties p = new Properties();
        p.setProperty(XMLExporter.TOPOLOGY_LEVEL, TopologyLevel.BUS_BREAKER.toString());
        DataSource ds = new FileDataSource(out, name + "-" + version);
        xmlExporter.export(n, p, ds);

        XMLImporter xmlImporter = new XMLImporter(Mockito.mock(PlatformConfig.class));
        Network network2 = xmlImporter.importData(ds, null);

        Networks networks = new Networks();
        networks.imported = n;
        networks.exported = network2;

        return networks;
    }

    private static void validateBusBalances(Network network, double threshold) throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            ValidationConfig config = loadFlowValidationConfig(threshold);
            Path working = Files.createDirectories(fs.getPath("lf-validation"));

            ValidationType.SVCS.check(network, config, working);
            if (network == null) {
                ConversionTester.computeMissingFlows(network, config.getLoadFlowParameters());
                assertTrue(ValidationType.BUSES.check(network, config, working));
            }
        }
    }

    private static ValidationConfig loadFlowValidationConfig(double threshold) {
        ValidationConfig config = ValidationConfig.load();
        config.setVerbose(true);
        config.setThreshold(threshold);
        config.setOkMissingValues(false);
        config.setLoadFlowParameters(new LoadFlowParameters());
        return config;
    }

    private void invalidateFlows(Network n) {
        n.getLineStream().forEach(line -> {
            invalidateFlow(line.getTerminal(Side.ONE));
            invalidateFlow(line.getTerminal(Side.TWO));
        });
        n.getTwoWindingsTransformerStream().forEach(twt -> {
            invalidateFlow(twt.getTerminal(Side.ONE));
            invalidateFlow(twt.getTerminal(Side.TWO));
        });
        n.getShuntCompensatorStream().forEach(sh -> {
            Terminal terminal = sh.getTerminal();
            terminal.setQ(Double.NaN);
        });
        n.getThreeWindingsTransformerStream().forEach(twt -> {
            invalidateFlow(twt.getLeg1().getTerminal());
            invalidateFlow(twt.getLeg2().getTerminal());
            invalidateFlow(twt.getLeg3().getTerminal());
        });
    }

    private void invalidateFlow(Terminal t) {
        t.setP(Double.NaN);
        t.setQ(Double.NaN);
    }

    private CgmesModel cgmes(CatalogLocation location, Path rpath) {
        String impl = TripleStoreFactory.defaultImplementation();
        return CgmesModelFactory.create(
            dataSource(location.dataRoot().resolve(rpath)),
            dataSource(location.boundary()),
            impl);
    }

    public String modelName(CatalogLocation location, Path rpath) {
        // Identify the model using the portion of path relative to data root
        return rpath.subpath(location.dataRoot().getNameCount(), rpath.getNameCount()).toString();
    }

    private DataSource dataSource(Path path) {
        if (!path.toFile().exists()) {
            return null;
        }
        String spath = path.toString();
        if (path.toFile().isDirectory()) {
            String basename = spath.substring(spath.lastIndexOf('/') + 1);
            return new FileDataSource(path, basename);
        } else if (path.toFile().isFile() && spath.endsWith(".zip")) {
            return new ZipFileDataSource(path);
        }
        return null;
    }

    public interface CatalogLocation {

        Path dataRoot();

        Path boundary();
    }

    static final CatalogLocation IOP_JANUARY_2019 = new CatalogLocation() {
        @Override
        public Path dataRoot() {
            return Paths.get("\\work\\Projects\\RTE\\CgmesCases\\IOP\\CGMES_IOP_20190116");
        }

        @Override
        public Path boundary() {
            return Paths.get("\\work\\Projects\\RTE\\CgmesCases\\IOP\\CGMES_IOP_20190116\\boundary");
        }
    };

    static final CatalogLocation IOP_JUNE = new CatalogLocation() {
        @Override
        public Path dataRoot() {
            return Paths.get("/Volumes/LuScratch/work/works/RTE/CGMES/data/csi/IOP/IOP20190605");
        }

        @Override
        public Path boundary() {
            // Boundary data from January IOP
            return Paths.get("/Volumes/LuScratch/work/works/RTE/CGMES/data/csi/IOP/CGMES_IOP_20190116/boundary");
        }
    };

    static final CatalogLocation IOP_JUNE_LOCAL = new CatalogLocation() {
        @Override
        public Path dataRoot() {
            return Paths.get("/Users/zamarrenolm/works/RTE/data/iopJuneLocal");
        }

        @Override
        public Path boundary() {
            // Boundary data from August
            return Paths.get("/Users/zamarrenolm/works/RTE/data/boundaries/20190812");
        }
    };

    static final CatalogLocation IOP_SEPTEMBER = new CatalogLocation() {
        @Override
        public Path dataRoot() {
            return Paths.get("/Volumes/LuScratch/work/works/RTE/CGMES/data/csi/IOP/IOP20190911");
        }

        @Override
        public Path boundary() {
            // Boundary data from August
            return Paths.get("/Volumes/LuScratch/work/works/RTE/CGMES/data/csi/IOP/20190812_BD");
        }
    };

    static final CatalogLocation IOP_OCTOBER = new CatalogLocation() {
        @Override
        public Path dataRoot() {
            //return Paths.get("/Volumes/LuScratch/work/works/RTE/CGMES/data/csi/IOP/IOP20191009");
            return Paths.get("\\work\\Projects\\RTE\\CgmesCases\\IOP\\IOP20191009");
        }

        @Override
        public Path boundary() {
            // Boundary data from October
            //return Paths.get("/Volumes/LuScratch/work/works/RTE/CGMES/data/csi/IOP/20190812_BD");
            return Paths.get("\\work\\Projects\\RTE\\CgmesCases\\IOP\\IOP20191009\\boundary");
        }
    };

    static final CatalogLocation IOP_ANNE = new CatalogLocation() {
        @Override
        public Path dataRoot() {
            return Paths.get("\\work\\Projects\\RTE\\CgmesCases\\IOP\\AnneCases");
        }

        @Override
        public Path boundary() {
            return Paths.get("\\work\\Projects\\RTE\\CgmesCases\\IOP\\AnneCases\\boundary");
        }
    };

    static final CatalogLocation IOP_OTHER = new CatalogLocation() {
        @Override
        public Path dataRoot() {
            return Paths.get("\\work\\Projects\\RTE\\CgmesCases\\IOP\\OtherCases");
        }

        @Override
        public Path boundary() {
            return Paths.get("\\work\\Projects\\RTE\\CgmesCases\\IOP\\OtherCases\\boundary");
        }
    };

    static final CatalogLocation IOP_DECEMBER_2019 = new CatalogLocation() {
        @Override
        public Path dataRoot() {
            return Paths.get("\\work\\Projects\\RTE\\CgmesCases\\IOP\\IOP20191204");
        }

        @Override
        public Path boundary() {
            return Paths.get("\\work\\Projects\\RTE\\CgmesCases\\IOP\\IOP20191204\\boundary");
        }
    };

    static final CatalogLocation IOP_FEBRUARY_2020 = new CatalogLocation() {
        @Override
        public Path dataRoot() {
            return Paths.get("\\work\\Projects\\RTE\\CgmesCases\\IOP\\IOP20200212");
        }

        @Override
        public Path boundary() {
            return Paths.get("\\work\\Projects\\RTE\\CgmesCases\\IOP\\IOP20200212\\boundary");
        }
    };

    static final CatalogLocation IOP_AUGUST_LOCAL = new CatalogLocation() {
        @Override
        public Path dataRoot() {
            return Paths.get("/Users/zamarrenolm/works/RTE/data/iopAugustLocal");
        }

        @Override
        public Path boundary() {
            return Paths.get("/Users/zamarrenolm/works/RTE/data/boundaries/20190812");
        }
    };

    static final CatalogLocation IOP_SEPTEMBER_LOCAL = new CatalogLocation() {
        @Override
        public Path dataRoot() {
            return Paths.get("/Users/zamarrenolm/works/RTE/data/iopSeptemberLocal");
        }

        @Override
        public Path boundary() {
            return Paths.get("/Users/zamarrenolm/works/RTE/data/boundaries/20190812");
        }
    };

    static final CatalogLocation LOCAL = new CatalogLocation() {
        @Override
        public Path dataRoot() {
            return Paths.get("/Users/zamarrenolm/works/RTE/data/");
        }

        @Override
        public Path boundary() {
            return Paths.get("/Users/zamarrenolm/works/RTE/data/boundaries/20190812");
        }
    };

    private void reviewAll(CatalogLocation location, String pattern, Consumer<Path> consumer) throws IOException {
        // Review all files or folders that match a given pattern
        // Using "glob" patterns:
        // a double "**" means that there could be intermediate folders
        // a single "*" is any sequence of characters inside the same folder
        PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(pattern);
        try (Stream<Path> paths = Files.walk(location.dataRoot())) {
            paths
                .filter(pathMatcher::matches)
                .forEach(consumer::accept);
        }
    }

    private void logHvdcConverterStations(Network n) {
        n.getHvdcConverterStationStream().forEach(c -> logHvdcConverterStation(n, c));
    }

    private void logHvdcConverterStation(Network n, HvdcConverterStation<?> hvdcConverterStation) {
        LOG.info("HvdcConverterStation");
        LOG.info("Id: {}", hvdcConverterStation.getId());
        LOG.info("HvdcType: {}", hvdcConverterStation.getHvdcType());
        LOG.info("Name: {}", hvdcConverterStation.getName());
        LOG.info("HvdcLine Id: {}", hvdcConverterStation.getHvdcLine().getId());
        LOG.info("LossFactor: {}", hvdcConverterStation.getLossFactor());
        if (hvdcConverterStation.getHvdcType() == HvdcConverterStation.HvdcType.LCC) {
            LccConverterStation lccConverterStation = n.getLccConverterStation(hvdcConverterStation.getId());
            LOG.info("PowerFactor: {}", lccConverterStation.getPowerFactor());
        } else if (hvdcConverterStation.getHvdcType() == HvdcConverterStation.HvdcType.VSC) {
            VscConverterStation vscConverterStation = n.getVscConverterStation(hvdcConverterStation.getId());
            LOG.info("isVoltageRegulatorOn: {}", vscConverterStation.isVoltageRegulatorOn());
            LOG.info("voltageSetPoint: {}", vscConverterStation.getVoltageSetpoint());
            LOG.info("reactivePowerSetPoint: {}", vscConverterStation.getReactivePowerSetpoint());
            ReactiveLimits qLimits = vscConverterStation.getReactiveLimits();
            LOG.info("reactiveLimites: qMin(153 Mw) {} qMax(153 Mw) {}", qLimits.getMinQ(153.0), qLimits.getMaxQ(153.0));
        }
    }

    private void logHvdcLines(Network n) {
        n.getHvdcLineStream().forEach(dc -> logHvdcLine(dc));
    }

    private void logHvdcLine(HvdcLine hvdcLine) {
        LOG.info("HvdcLine");
        LOG.info("Id: {}", hvdcLine.getId());
        LOG.info("convertersMode: {}", hvdcLine.getConvertersMode());
        LOG.info("Name: {}", hvdcLine.getName());
        LOG.info("converterStationId1: {}", hvdcLine.getConverterStation1().getId());
        LOG.info("converterStationId2: {}", hvdcLine.getConverterStation2().getId());
        LOG.info("R: {}", hvdcLine.getR());
        LOG.info("activePowerSetPoint: {}", hvdcLine.getActivePowerSetpoint());
        LOG.info("MaxP: {}", hvdcLine.getMaxP());
    }

    private static String tsoNameFromPathname(String sp) {
        int i = sp.indexOf("_1D_") + 4;
        if (sp.indexOf("_1D_") == -1) {
            i = sp.indexOf("_2D_") + 4;
        }
        int j = sp.indexOf('_', i);
        int j2 = sp.indexOf('\\', i);
        if (j2 < 0) {
            j2 = sp.indexOf('/', i);
        }
        if (sp.indexOf('_', i) > j2) {
            j = j2;
        }
        if (j > i) {
            return sp.substring(i, j);
        } else {
            return sp.substring(i);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(Csi.class);
}
