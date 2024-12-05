/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test.export;

import com.powsybl.cgmes.conformity.Cgmes3ModifiedCatalog;
import com.powsybl.cgmes.conformity.Cgmes3Catalog;
import com.powsybl.cgmes.conformity.CgmesConformity1Catalog;
import com.powsybl.cgmes.conformity.CgmesConformity1ModifiedCatalog;
import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.CgmesModelExtension;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.export.CgmesExportContext;
import com.powsybl.cgmes.conversion.export.EquipmentExport;
import com.powsybl.cgmes.conversion.export.TopologyExport;
import com.powsybl.cgmes.extensions.*;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.commons.datasource.DirectoryDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.extensions.RemoteReactivePowerControl;
import com.powsybl.iidm.network.test.*;
import com.powsybl.iidm.serde.ExportOptions;
import com.powsybl.iidm.serde.NetworkSerDe;
import com.powsybl.iidm.serde.XMLImporter;
import com.powsybl.iidm.network.util.BranchData;
import com.powsybl.iidm.network.util.TwtData;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xmlunit.diff.DifferenceEvaluator;
import org.xmlunit.diff.DifferenceEvaluators;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.FileSystem;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import static com.powsybl.cgmes.conversion.test.ConversionUtil.writeCgmesProfile;
import static com.powsybl.cgmes.conversion.test.ConversionUtil.getFirstMatch;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
class EquipmentExportTest extends AbstractSerDeTest {

    private Properties importParams;

    @Override
    @BeforeEach
    public void setUp() throws IOException {
        super.setUp();
        importParams = new Properties();
        importParams.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");
    }

    @Test
    void smallGridHvdc() throws IOException, XMLStreamException {
        ReadOnlyDataSource dataSource = CgmesConformity1Catalog.smallNodeBreakerHvdc().dataSource();
        Network expected = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), importParams);
        Network actual = exportImportNodeBreaker(expected, dataSource);
        assertTrue(compareNetworksEQdata(expected, actual));
    }

    @Test
    void smallNodeBreaker() throws IOException, XMLStreamException {
        ReadOnlyDataSource dataSource = CgmesConformity1Catalog.smallNodeBreaker().dataSource();
        Network expected = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), importParams);
        Network actual = exportImportNodeBreaker(expected, dataSource);
        assertTrue(compareNetworksEQdata(expected, actual));
    }

    @Test
    void smallBusBranch() throws IOException, XMLStreamException {
        ReadOnlyDataSource dataSource = CgmesConformity1Catalog.smallBusBranch().dataSource();
        Network expected = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), importParams);
        Network actual = exportImportBusBranch(expected, dataSource);
        assertTrue(compareNetworksEQdata(expected, actual));
    }

    @Test
    void smallGridHvdcWithCapabilityCurve() throws IOException, XMLStreamException {
        ReadOnlyDataSource dataSource = CgmesConformity1ModifiedCatalog.smallNodeBreakerHvdcWithVsCapabilityCurve().dataSource();
        Network expected = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), importParams);
        Network actual = exportImportNodeBreaker(expected, dataSource);
        assertTrue(compareNetworksEQdata(expected, actual));
    }

    @Test
    void miniNodeBreaker() throws IOException, XMLStreamException {
        ReadOnlyDataSource dataSource = CgmesConformity1Catalog.miniNodeBreaker().dataSource();
        Network expected = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), importParams);
        Network actual = exportImportNodeBreaker(expected, dataSource);

        // Avoid negative zeros during the comparison
        expected.getGenerators().forEach(generator -> {
            generator.setTargetP(generator.getTargetP() + 0.0);
            generator.setTargetQ(generator.getTargetQ() + 0.0);
        });
        actual.getGenerators().forEach(generator -> {
            generator.setTargetP(generator.getTargetP() + 0.0);
            generator.setTargetQ(generator.getTargetQ() + 0.0);
        });

        assertTrue(compareNetworksEQdata(expected, actual));
    }

    @Test
    void miniBusBranch() throws IOException, XMLStreamException {
        ReadOnlyDataSource dataSource = CgmesConformity1Catalog.miniBusBranch().dataSource();
        Network expected = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), importParams);
        Network actual = exportImportBusBranchNoBoundaries(expected, dataSource);

        // Avoid negative zeros during the comparison
        expected.getGenerators().forEach(generator -> {
            generator.setTargetP(generator.getTargetP() + 0.0);
            generator.setTargetQ(generator.getTargetQ() + 0.0);
        });
        actual.getGenerators().forEach(generator -> {
            generator.setTargetP(generator.getTargetP() + 0.0);
            generator.setTargetQ(generator.getTargetQ() + 0.0);
        });

        assertTrue(compareNetworksEQdata(expected, actual));
    }

    @Test
    void microGridWithTieFlowMappedToEquivalentInjection() throws IOException, XMLStreamException {
        ReadOnlyDataSource dataSource = CgmesConformity1ModifiedCatalog.microGridBaseCaseBEWithTieFlowMappedToEquivalentInjection().dataSource();
        Network expected = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), importParams);
        Network actual = exportImportBusBranch(expected, dataSource);
        assertTrue(compareNetworksEQdata(expected, actual));
    }

    @Test
    void microGridBaseCaseAssembledSwitchAtBoundary() throws XMLStreamException, IOException {
        ReadOnlyDataSource dataSource = CgmesConformity1ModifiedCatalog.microGridBaseCaseAssembledSwitchAtBoundary().dataSource();
        Network network = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), importParams);

        network.newExtension(CgmesControlAreasAdder.class).add();
        CgmesControlAreas cgmesControlAreas = network.getExtension(CgmesControlAreas.class);
        CgmesControlArea cgmesControlArea = cgmesControlAreas.newCgmesControlArea()
                .setId("controlAreaId")
                .setName("controlAreaName")
                .setEnergyIdentificationCodeEic("energyIdentCodeEic")
                .setNetInterchange(Double.NaN)
                .add();
        TieLine tieLine = network.getTieLine("78736387-5f60-4832-b3fe-d50daf81b0a6 + 7f43f508-2496-4b64-9146-0a40406cbe49");
        cgmesControlArea.add(tieLine.getDanglingLine2().getBoundary());

        // TODO(Luma) updated expected result after halves of tie lines are exported as equipment
        //  instead of an error logged and the tie flow ignored,
        //  the reimported network control area should contain one tie flow
        Network actual = exportImportNodeBreaker(network, dataSource);
        Area actualControlArea = actual.getArea("controlAreaId");
        boolean tieFlowsAtTieLinesAreSupported = false;
        if (tieFlowsAtTieLinesAreSupported) {
            assertEquals(1, actualControlArea.getAreaBoundaryStream().count());
            assertEquals("7f43f508-2496-4b64-9146-0a40406cbe49", actualControlArea.getAreaBoundaries().iterator().next().getBoundary().get().getDanglingLine().getId());
        } else {
            assertNull(actualControlArea);
        }
    }

    @Test
    void microGrid() throws IOException, XMLStreamException {
        ReadOnlyDataSource dataSource = CgmesConformity1Catalog.microGridType4BE().dataSource();
        Network expected = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), importParams);
        Network actual = exportImportBusBranch(expected, dataSource);
        assertTrue(compareNetworksEQdata(expected, actual));
    }

    @Test
    void microGridCreateEquivalentInjectionAliases() throws IOException, XMLStreamException {
        ReadOnlyDataSource dataSource = CgmesConformity1Catalog.microGridBaseCaseBE().dataSource();
        Network expected = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), importParams);
        // Remove aliases of equivalent injections, so they will have to be created during export
        for (DanglingLine danglingLine : expected.getDanglingLines(DanglingLineFilter.ALL)) {
            danglingLine.removeProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.EQUIVALENT_INJECTION);
            danglingLine.removeProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "EquivalentInjectionTerminal");
        }
        Network actual = exportImportBusBranch(expected, dataSource);
        assertTrue(compareNetworksEQdata(expected, actual));
    }

    @Test
    void nordic32() throws IOException, XMLStreamException {
        ReadOnlyDataSource dataSource = new ResourceDataSource("nordic32", new ResourceSet("/cim14", "nordic32.xiidm"));
        Network network = new XMLImporter().importData(dataSource, NetworkFactory.findDefault(), null);
        exportToCgmesEQ(network);
        exportToCgmesTP(network);
        // Import EQ & TP file, no additional information (boundaries) are required
        Network actual = new CgmesImport().importData(new DirectoryDataSource(tmpDir, "exported"), NetworkFactory.findDefault(), null);

        // The xiidm file does not contain ratedS values, but during the cgmes export process default values
        // are exported for each transformer that are reading in the import process.
        // we reset the default imported ratedS values before comparing
        TwoWindingsTransformer twta = actual.getTwoWindingsTransformerStream().findFirst().orElseThrow();
        network.getTwoWindingsTransformers().forEach(twtn -> twtn.setRatedS(twta.getRatedS()));

        // Ignore OperationalLimitsGroup id
        DifferenceEvaluator knownDiffs = DifferenceEvaluators.chain(
                DifferenceEvaluators.Default,
                ExportXmlCompare::numericDifferenceEvaluator,
                ExportXmlCompare::ignoringNonEQ,
                ExportXmlCompare::ignoringOperationalLimitsGroupId);
        assertTrue(compareNetworksEQdata(network, actual, knownDiffs));
    }

    @Test
    void nordic32SortTransformerEnds() throws IOException, XMLStreamException {
        ReadOnlyDataSource dataSource = new ResourceDataSource("nordic32", new ResourceSet("/cim14", "nordic32.xiidm"));
        Network network = new XMLImporter().importData(dataSource, NetworkFactory.findDefault(), importParams);
        exportToCgmesEQ(network, true);
        exportToCgmesTP(network);
        // Import EQ & TP file, no additional information (boundaries) are required
        Network actual = new CgmesImport().importData(new DirectoryDataSource(tmpDir, "exported"), NetworkFactory.findDefault(), null);
        // Before comparing, interchange ends in twoWindingsTransformers that do not follow the high voltage at end1 rule
        prepareNetworkForSortedTransformerEndsComparison(network);

        // The xiidm file does not contain ratedS values, but during the cgmes export process default values
        // are exported for each transformer that are reading in the import process.
        // we reset the default imported ratedS values before comparing
        TwoWindingsTransformer twta = actual.getTwoWindingsTransformerStream().findFirst().orElseThrow();
        network.getTwoWindingsTransformers().forEach(twtn -> twtn.setRatedS(twta.getRatedS()));

        // Ignore OperationalLimitsGroup id
        DifferenceEvaluator knownDiffs = DifferenceEvaluators.chain(
                DifferenceEvaluators.Default,
                ExportXmlCompare::numericDifferenceEvaluator,
                ExportXmlCompare::ignoringNonEQ,
                ExportXmlCompare::ignoringOperationalLimitsGroupId);
        assertTrue(compareNetworksEQdata(network, actual, knownDiffs));
    }

    private void prepareNetworkForSortedTransformerEndsComparison(Network network) {
        List<Pair<String, TwtRecord>> pairs = new ArrayList<>();
        network.getTwoWindingsTransformerStream().filter(twt -> twt
                        .getTerminal1().getVoltageLevel().getNominalV() < twt.getTerminal2().getVoltageLevel().getNominalV())
                .forEach(twt -> {
                    TwtRecord twtRecord = obtainRecord(twt);
                    pairs.add(Pair.of(twt.getId(), twtRecord));
                });

        pairs.forEach(pair -> {
            TwoWindingsTransformer twt = network.getTwoWindingsTransformer(pair.getLeft());
            twt.remove();
            TwoWindingsTransformer newTwt = pair.getRight().getAdder().add();
            Optional<CurrentLimits> currentLimits1 = pair.getRight().getCurrentLimits1();
            if (currentLimits1.isPresent()) {
                newTwt.newCurrentLimits1().setPermanentLimit(currentLimits1.get().getPermanentLimit()).add();
            }
            Optional<CurrentLimits> currentLimits2 = pair.getRight().getCurrentLimits2();
            if (currentLimits2.isPresent()) {
                newTwt.newCurrentLimits2().setPermanentLimit(currentLimits2.get().getPermanentLimit()).add();
            }
            pair.getRight().getAliases().forEach(aliasPair -> {
                if (aliasPair.getLeft() == null) {
                    newTwt.addAlias(aliasPair.getRight());
                } else {
                    newTwt.addAlias(aliasPair.getRight(), aliasPair.getLeft());
                }
            });
        });
    }

    private TwtRecord obtainRecord(TwoWindingsTransformer twt) {
        Substation substation = twt.getSubstation().orElseThrow();
        double a0 = twt.getRatedU1() / twt.getRatedU2();
        double a02 = a0 * a0;
        TwoWindingsTransformerAdder adder = substation.newTwoWindingsTransformer()
            .setId(twt.getId())
            .setName(twt.getNameOrId())
            .setBus1(twt.getTerminal2().getBusBreakerView().getBus().getId())
            .setBus2(twt.getTerminal1().getBusBreakerView().getBus().getId())
            .setR(twt.getR() * a02)
            .setX(twt.getX() * a02)
            .setG(twt.getG() / a02)
            .setB(twt.getB() / a02)
            .setRatedU1(twt.getRatedU2())
            .setRatedU2(twt.getRatedU1());

        CurrentLimits currentLimits1 = twt.getCurrentLimits1().orElse(null);
        CurrentLimits currentLimits2 = twt.getCurrentLimits2().orElse(null);

        List<Pair<String, String>> aliases = new ArrayList<>();
        twt.getAliases().forEach(alias -> {
            String type = twt.getAliasType(alias).orElse(null);
            aliases.add(Pair.of(type, alias));
        });
        return new TwtRecord(adder, currentLimits2, currentLimits1, aliases);
    }

    private static final class TwtRecord {
        private final TwoWindingsTransformerAdder adder;
        private final CurrentLimits currentLimits1;
        private final CurrentLimits currentLimits2;
        private final List<Pair<String, String>> aliases;

        private TwtRecord(TwoWindingsTransformerAdder adder, CurrentLimits currentLimits1, CurrentLimits currentLimits2,
            List<Pair<String, String>> aliases) {
            this.adder = adder;
            this.currentLimits1 = currentLimits1;
            this.currentLimits2 = currentLimits2;
            this.aliases = aliases;
        }

        private TwoWindingsTransformerAdder getAdder() {
            return adder;
        }

        private Optional<CurrentLimits> getCurrentLimits1() {
            return Optional.ofNullable(currentLimits1);
        }

        private Optional<CurrentLimits> getCurrentLimits2() {
            return Optional.ofNullable(currentLimits2);
        }

        private List<Pair<String, String>> getAliases() {
            return aliases;
        }
    }

    @Test
    void bPerSectionTest() throws IOException, XMLStreamException {
        ReadOnlyDataSource ds = CgmesConformity1Catalog.microGridType4BE().dataSource();

        Network network = new CgmesImport().importData(ds, NetworkFactory.findDefault(), importParams);
        ShuntCompensatorLinearModel sh = (ShuntCompensatorLinearModel) network.getShuntCompensator("d771118f-36e9-4115-a128-cc3d9ce3e3da").getModel();
        assertEquals(0.024793, sh.getBPerSection(), 0.0);

        sh.setBPerSection(1E-14);

        Network reimported = exportImportBusBranch(network, ds);
        sh = (ShuntCompensatorLinearModel) reimported.getShuntCompensator("d771118f-36e9-4115-a128-cc3d9ce3e3da").getModel();
        assertEquals(1E-14, sh.getBPerSection(), 0.0);
    }

    @Test
    void threeWindingsTransformerTest() throws IOException, XMLStreamException {
        Network network = createThreeWindingTransformerNetwork();
        String t3id = "threeWindingsTransformer1";

        // Export an IIDM Network created from scratch, identifiers for tap changers will be created and stored in aliases
        exportToCgmesEQ(network);
        ThreeWindingsTransformer expected = network.getThreeWindingsTransformer(t3id);

        // The 3-winding transformer has a ratio and phase tap changer at every end
        Network network1 = new CgmesImport().importData(new DirectoryDataSource(tmpDir, "exportedEq"), NetworkFactory.findDefault(), null);
        ThreeWindingsTransformer actual1 = network1.getThreeWindingsTransformer(t3id);
        for (int k = 1; k <= 3; k++) {
            String aliasType;
            aliasType = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.RATIO_TAP_CHANGER + k;
            assertEquals(
                    expected.getAliasFromType(aliasType).get(),
                    actual1.getAliasFromType(aliasType).get());
            aliasType = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.PHASE_TAP_CHANGER + k;
            assertEquals(
                    expected.getAliasFromType(aliasType).get(),
                    actual1.getAliasFromType(aliasType).get());
        }

        // Export an IIDM Network that has been imported from CGMES,
        // identifiers for tap changers must be preserved
        exportToCgmesEQ(network1);
        Network network2 = new CgmesImport().importData(new DirectoryDataSource(tmpDir, "exportedEq"), NetworkFactory.findDefault(), null);
        ThreeWindingsTransformer actual2 = network2.getThreeWindingsTransformer(t3id);
        for (int k = 1; k <= 3; k++) {
            String aliasType;
            aliasType = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.RATIO_TAP_CHANGER + k;
            assertEquals(
                    expected.getAliasFromType(aliasType).get(),
                    actual2.getAliasFromType(aliasType).get());
            aliasType = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.PHASE_TAP_CHANGER + k;
            assertEquals(
                    expected.getAliasFromType(aliasType).get(),
                    actual2.getAliasFromType(aliasType).get());
        }
    }

    @Test
    void twoWindingsTransformerCgmesExportTest() throws IOException, XMLStreamException {
        Network network = FourSubstationsNodeBreakerFactory.create();

        TwoWindingsTransformer twt = network.getTwoWindingsTransformer("TWT");
        assertEquals(225.0, twt.getTerminal1().getVoltageLevel().getNominalV(), 0.0);
        assertEquals(400.0, twt.getTerminal2().getVoltageLevel().getNominalV(), 0.0);

        // Change the tap position to have a ratio != 1.0
        // Models are only equivalent if G and B are zero
        twt.setB(0.0);
        twt.getRatioTapChanger().setTapPosition(0);

        // Voltage at both ends of the transformer
        Bus bus400 = twt.getTerminal2().getBusView().getBus();
        bus400.setV(400.0).setAngle(0.0);
        Bus bus225 = twt.getTerminal1().getBusView().getBus();
        bus225.setV(264.38396259257394).setAngle(2.4025237265837864);

        BranchData twtData = new BranchData(twt, 0.0, false, false);

        // Export network as cgmes files and re-import again,
        // the ends of the transformer must be sorted
        // to have the high nominal voltage at end1
        Network networkSorted = exportImportNodeBreakerNoBoundaries(network);

        TwoWindingsTransformer twtSorted = networkSorted.getTwoWindingsTransformer("TWT");
        assertEquals(400.0, twtSorted.getTerminal1().getVoltageLevel().getNominalV(), 0.0);
        assertEquals(225.0, twtSorted.getTerminal2().getVoltageLevel().getNominalV(), 0.0);

        assertTrue(compareCurrentLimits(twt.getCurrentLimits1().orElse(null), twtSorted.getCurrentLimits2().orElse(null)));
        assertTrue(compareCurrentLimits(twt.getCurrentLimits2().orElse(null), twtSorted.getCurrentLimits1().orElse(null)));

        // Voltage at both ends of the transformer
        Bus busS400 = twtSorted.getTerminal1().getBusView().getBus();
        busS400.setV(400.0).setAngle(0.0);
        Bus busS225 = twtSorted.getTerminal2().getBusView().getBus();
        busS225.setV(264.38396259257394).setAngle(2.4025237265837864);

        BranchData twtDataSorted = new BranchData(twtSorted, 0.0, false, false);

        double tol = 0.0000001;
        assertEquals(twtData.getComputedP1(), twtDataSorted.getComputedP2(), tol);
        assertEquals(twtData.getComputedQ1(), twtDataSorted.getComputedQ2(), tol);
        assertEquals(twtData.getComputedP2(), twtDataSorted.getComputedP1(), tol);
        assertEquals(twtData.getComputedQ2(), twtDataSorted.getComputedQ1(), tol);
    }

    @Test
    void twoWindingsTransformerWithShuntAdmittanceCgmesExportTest() throws IOException, XMLStreamException {
        Network network = FourSubstationsNodeBreakerFactory.create();

        TwoWindingsTransformer twt = network.getTwoWindingsTransformer("TWT");
        assertEquals(225.0, twt.getTerminal1().getVoltageLevel().getNominalV(), 0.0);
        assertEquals(400.0, twt.getTerminal2().getVoltageLevel().getNominalV(), 0.0);

        // Change the tap position to have a ratio != 1.0
        twt.getRatioTapChanger().setTapPosition(0);

        // Voltage at both ends of the transformer
        Bus bus400 = twt.getTerminal2().getBusView().getBus();
        bus400.setV(400.0).setAngle(0.0);
        Bus bus225 = twt.getTerminal1().getBusView().getBus();
        bus225.setV(264.38396259257394).setAngle(2.4025237265837864);

        BranchData twtData = new BranchData(twt, 0.0, false, false);

        // Export network as cgmes files and re-import again,
        // the ends of the transformer must be sorted
        // to have the high nominal voltage at end1
        Network networkSorted = exportImportNodeBreakerNoBoundaries(network);

        TwoWindingsTransformer twtSorted = networkSorted.getTwoWindingsTransformer("TWT");
        assertEquals(400.0, twtSorted.getTerminal1().getVoltageLevel().getNominalV(), 0.0);
        assertEquals(225.0, twtSorted.getTerminal2().getVoltageLevel().getNominalV(), 0.0);

        assertTrue(compareCurrentLimits(twt.getCurrentLimits1().orElse(null), twtSorted.getCurrentLimits2().orElse(null)));
        assertTrue(compareCurrentLimits(twt.getCurrentLimits2().orElse(null), twtSorted.getCurrentLimits1().orElse(null)));

        // Voltage at both ends of the transformer
        Bus busS400 = twtSorted.getTerminal1().getBusView().getBus();
        busS400.setV(400.0).setAngle(0.0);
        Bus busS225 = twtSorted.getTerminal2().getBusView().getBus();
        busS225.setV(264.38396259257394).setAngle(2.4025237265837864);

        BranchData twtDataSorted = new BranchData(twtSorted, 0.0, false, false);

        // Models are only equivalent if G and B are zero
        double tol = 0.0000001;
        assertEquals(twtData.getComputedP1(), twtDataSorted.getComputedP2(), tol);
        assertEquals(-12.553777142703378, twtData.getComputedQ1(), tol);
        assertEquals(-7.446222857261597, twtDataSorted.getComputedQ2(), tol);
        assertEquals(twtData.getComputedP2(), twtDataSorted.getComputedP1(), tol);
        assertEquals(7.871048170667905, twtData.getComputedQ2(), tol);
        assertEquals(2.751048170611625, twtDataSorted.getComputedQ1(), tol);
    }

    @Test
    void threeWindingsTransformerCgmesExportTest() throws IOException, XMLStreamException {
        Network network = ThreeWindingsTransformerNetworkFactory.createWithUnsortedEndsAndCurrentLimits();

        ThreeWindingsTransformer twt = network.getThreeWindingsTransformer("3WT");
        assertEquals(11.0, twt.getLeg1().getTerminal().getVoltageLevel().getNominalV(), 0.0);
        assertEquals(132.0, twt.getLeg2().getTerminal().getVoltageLevel().getNominalV(), 0.0);
        assertEquals(33.0, twt.getLeg3().getTerminal().getVoltageLevel().getNominalV(), 0.0);

        // Set the voltage at each end for calculating flows and voltage at the star bus

        Bus bus132 = twt.getLeg2().getTerminal().getBusView().getBus();
        bus132.setV(135.0).setAngle(0.0);
        Bus bus33 = twt.getLeg3().getTerminal().getBusView().getBus();
        bus33.setV(28.884977348881097).setAngle(-0.7602433704291399);
        Bus bus11 = twt.getLeg1().getTerminal().getBusView().getBus();
        bus11.setV(11.777636198340568).setAngle(-0.78975650100671);

        TwtData twtData = new TwtData(twt, 0.0, false);

        // Export network as cgmes files and re-import again,
        // the ends of the transformer must be sorted
        // in concordance with nominal voltage
        Network networkSorted = exportImportNodeBreakerNoBoundaries(network);

        ThreeWindingsTransformer twtSorted = networkSorted.getThreeWindingsTransformer("3WT");
        assertEquals(132.0, twtSorted.getLeg1().getTerminal().getVoltageLevel().getNominalV(), 0.0);
        assertEquals(33.0, twtSorted.getLeg2().getTerminal().getVoltageLevel().getNominalV(), 0.0);
        assertEquals(11.0, twtSorted.getLeg3().getTerminal().getVoltageLevel().getNominalV(), 0.0);

        assertTrue(compareCurrentLimits(twt.getLeg2().getCurrentLimits().orElse(null), twtSorted.getLeg1().getCurrentLimits().orElse(null)));
        assertTrue(compareCurrentLimits(twt.getLeg3().getCurrentLimits().orElse(null), twtSorted.getLeg2().getCurrentLimits().orElse(null)));
        assertTrue(compareCurrentLimits(twt.getLeg1().getCurrentLimits().orElse(null), twtSorted.getLeg3().getCurrentLimits().orElse(null)));

        // Set the voltage at each end for calculating flows and voltage at the star bus

        Bus busS132 = twtSorted.getLeg1().getTerminal().getBusView().getBus();
        busS132.setV(135.0).setAngle(0.0);
        Bus busS33 = twtSorted.getLeg2().getTerminal().getBusView().getBus();
        busS33.setV(28.884977348881097).setAngle(-0.7602433704291399);
        Bus busS11 = twtSorted.getLeg3().getTerminal().getBusView().getBus();
        busS11.setV(11.777636198340568).setAngle(-0.78975650100671);

        TwtData twtDataSorted = new TwtData(twtSorted, 0.0, false);

        // star bus voltage must be checked in per unit as it depends on the ratedU0 (vnominal0 = ratedU0)
        double tol = 0.0000001;
        assertEquals(twtData.getStarU() / twt.getRatedU0(), twtDataSorted.getStarU() / twtDataSorted.getRatedU0(), tol);
        assertEquals(twtData.getStarTheta(), twtDataSorted.getStarTheta(), tol);
        assertEquals(twtData.getComputedP(ThreeSides.ONE), twtDataSorted.getComputedP(ThreeSides.THREE), tol);
        assertEquals(twtData.getComputedQ(ThreeSides.ONE), twtDataSorted.getComputedQ(ThreeSides.THREE), tol);
        assertEquals(twtData.getComputedP(ThreeSides.TWO), twtDataSorted.getComputedP(ThreeSides.ONE), tol);
        assertEquals(twtData.getComputedQ(ThreeSides.TWO), twtDataSorted.getComputedQ(ThreeSides.ONE), tol);
        assertEquals(twtData.getComputedP(ThreeSides.THREE), twtDataSorted.getComputedP(ThreeSides.TWO), tol);
        assertEquals(twtData.getComputedQ(ThreeSides.THREE), twtDataSorted.getComputedQ(ThreeSides.TWO), tol);
    }

    private static boolean compareCurrentLimits(CurrentLimits expected, CurrentLimits actual) {
        if (expected == null && actual == null) {
            return true;
        }
        if (expected != null && actual != null) {
            return expected.getPermanentLimit() == actual.getPermanentLimit();
        }
        return false;
    }

    @Test
    void testLoadGroups() throws XMLStreamException, IOException {
        ReadOnlyDataSource dataSource = CgmesConformity1ModifiedCatalog.microGridBaseCaseBEConformNonConformLoads().dataSource();
        Network expected = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), importParams);
        Network actual = exportImportBusBranch(expected, dataSource);
        assertTrue(compareNetworksEQdata(expected, actual));
    }

    @Test
    void microGridCgmesExportPreservingOriginalClassesOfLoads() throws IOException, XMLStreamException {
        ReadOnlyDataSource dataSource = Cgmes3ModifiedCatalog.microGridBaseCaseAllTypesOfLoads().dataSource();
        importParams.put("iidm.import.cgmes.convert-boundary", "true");
        Network expected = Network.read(dataSource, importParams);
        importParams.put("iidm.import.cgmes.convert-boundary", "false");
        Network actual = exportImportNodeBreaker(expected, dataSource);

        assertEquals(loadsCreatedFromOriginalClassCount(expected, CgmesNames.ASYNCHRONOUS_MACHINE), loadsCreatedFromOriginalClassCount(actual, CgmesNames.ASYNCHRONOUS_MACHINE));
        assertEquals(loadsCreatedFromOriginalClassCount(expected, CgmesNames.ENERGY_SOURCE), loadsCreatedFromOriginalClassCount(actual, CgmesNames.ENERGY_SOURCE));
        assertEquals(loadsCreatedFromOriginalClassCount(expected, CgmesNames.SV_INJECTION), loadsCreatedFromOriginalClassCount(actual, CgmesNames.SV_INJECTION));
        assertEquals(loadsCreatedFromOriginalClassCount(expected, CgmesNames.ENERGY_CONSUMER), loadsCreatedFromOriginalClassCount(actual, CgmesNames.ENERGY_CONSUMER));
        assertEquals(loadsCreatedFromOriginalClassCount(expected, CgmesNames.CONFORM_LOAD), loadsCreatedFromOriginalClassCount(actual, CgmesNames.CONFORM_LOAD));
        assertEquals(loadsCreatedFromOriginalClassCount(expected, CgmesNames.NONCONFORM_LOAD), loadsCreatedFromOriginalClassCount(actual, CgmesNames.NONCONFORM_LOAD));
        assertEquals(loadsCreatedFromOriginalClassCount(expected, CgmesNames.STATION_SUPPLY), loadsCreatedFromOriginalClassCount(actual, CgmesNames.STATION_SUPPLY));

        // Remove the default control area created in expected network during export
        expected.getAreaStream().map(Area::getId).toList().forEach(a -> expected.getArea(a).remove());

        // Avoid comparing targetP and targetQ (reimport does not consider the SSH file);
        expected.getGenerators().forEach(expectedGenerator -> {
            Generator actualGenerator = actual.getGenerator(expectedGenerator.getId());
            actualGenerator.setTargetP(expectedGenerator.getTargetP());
            actualGenerator.setTargetQ(expectedGenerator.getTargetQ());
        });

        DifferenceEvaluator knownDiffs = DifferenceEvaluators.chain(
                DifferenceEvaluators.Default,
                ExportXmlCompare::numericDifferenceEvaluator,
                ExportXmlCompare::ignoringSubstationNumAttributes,
                ExportXmlCompare::ignoringSubstationLookup,
                ExportXmlCompare::ignoringGeneratorAttributes,
                ExportXmlCompare::ignoringLoadChildNodeListLength);
        assertTrue(compareNetworksEQdata(expected, actual, knownDiffs));
    }

    private static long loadsCreatedFromOriginalClassCount(Network network, String originalClass) {
        return network.getLoadStream().filter(load -> loadOriginalClass(load, originalClass)).count();
    }

    private static boolean loadOriginalClass(Load load, String originalClass) {
        String cgmesClass = load.getProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS);
        return cgmesClass != null && cgmesClass.equals(originalClass);
    }

    @Test
    void testExportEquivalentInjectionBaseVoltage() throws IOException {
        // Ensure equivalent injections outside boundaries are exported with a reference to a base voltage

        // Create a minimal network with a generator labelled as CGMES equivalent injection
        Network network = Network.create("testEIexport", "IIDM");
        VoltageLevel vl = network.newVoltageLevel().setId("VL1").setNominalV(400.0).setTopologyKind(TopologyKind.BUS_BREAKER).add();
        vl.getBusBreakerView().newBus().setId("Bus1").add();
        Generator g = vl.newGenerator().setId("Generator1").setBus("Bus1")
                .setVoltageRegulatorOn(true).setTargetV(400)
                .setTargetP(0).setMinP(0).setMaxP(10)
                .add();
        g.setProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS, "EquivalentInjection");

        // Export to CGMES only the EQ instance file
        Properties exportParams = new Properties();
        exportParams.put(CgmesExport.PROFILES, "EQ");
        String basename = "test-EI-export";
        network.write("CGMES", exportParams, tmpDir.resolve(basename));

        // Read the exported EQ file
        String eqFilename = String.format("%s_%s.xml", basename, "EQ");
        String eqFileContent = Files.readString(tmpDir.resolve(eqFilename));

        // Obtain the equivalent injection reference to base voltage
        Pattern eiBaseVoltageRegex = Pattern.compile("(?s)<cim:EquivalentInjection rdf:ID=\"_Generator1\".*ConductingEquipment.BaseVoltage rdf:resource=\"#(.*?)\".*</cim:EquivalentInjection>");
        Matcher matcher = eiBaseVoltageRegex.matcher(eqFileContent);
        assertTrue(matcher.find());
        String eiBaseVoltageId = matcher.group(1);

        // Obtain the (unique) base voltage defined in the EQ file
        Pattern baseVoltageDefinitionRegex = Pattern.compile("cim:BaseVoltage rdf:ID=\"(.*?)\"");
        matcher = baseVoltageDefinitionRegex.matcher(eqFileContent);
        assertTrue(matcher.find());
        String baseVoltageId = matcher.group(1);

        // Check that the equivalent injection base voltage is the base voltage defined in the exported EQ file
        assertEquals(baseVoltageId, eiBaseVoltageId);
    }

    @Test
    void miniGridCgmesExportPreservingOriginalClassesOfGenerators() throws IOException, XMLStreamException {
        ReadOnlyDataSource dataSource = Cgmes3Catalog.miniGrid().dataSource();
        Properties properties = new Properties();
        properties.setProperty("iidm.import.cgmes.convert-boundary", "true");
        Network expected = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), properties);
        Network actual = exportImportNodeBreaker(expected, dataSource);

        assertEquals(generatorsCreatedFromOriginalClassCount(expected, "SynchronousMachine"), generatorsCreatedFromOriginalClassCount(actual, "SynchronousMachine"));
        assertEquals(generatorsCreatedFromOriginalClassCount(expected, "ExternalNetworkInjection"), generatorsCreatedFromOriginalClassCount(actual, "ExternalNetworkInjection"));
        assertEquals(generatorsCreatedFromOriginalClassCount(expected, "EquivalentInjection"), generatorsCreatedFromOriginalClassCount(actual, "EquivalentInjection"));

        // Avoid comparing targetP and targetQ (reimport does not consider the SSH file);
        expected.getGenerators().forEach(expectedGenerator -> {
            Generator actualGenerator = actual.getGenerator(expectedGenerator.getId());
            actualGenerator.setTargetP(expectedGenerator.getTargetP());
            actualGenerator.setTargetQ(expectedGenerator.getTargetQ());
        });

        DifferenceEvaluator knownDiffs = DifferenceEvaluators.chain(
                DifferenceEvaluators.Default,
                ExportXmlCompare::numericDifferenceEvaluator,
                ExportXmlCompare::ignoringSubstationNumAttributes,
                ExportXmlCompare::ignoringSubstationLookup);
        compareNetworksEQdata(expected, actual, knownDiffs);
    }

    private static long generatorsCreatedFromOriginalClassCount(Network network, String originalClass) {
        return network.getGeneratorStream().filter(generator -> generatorOriginalClass(generator, originalClass)).count();
    }

    private static boolean generatorOriginalClass(Generator generator, String originalClass) {
        String cgmesClass = generator.getProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS);
        return cgmesClass != null && cgmesClass.equals(originalClass);
    }

    @Test
    void equivalentShuntTest() throws IOException {
        ReadOnlyDataSource ds = CgmesConformity1ModifiedCatalog.microGridBaseCaseBEEquivalentShunt().dataSource();
        Network network = new CgmesImport().importData(ds, NetworkFactory.findDefault(), importParams);

        // Export as cgmes
        Path outputPath = tmpDir.resolve("temp.cgmesExport");
        Files.createDirectories(outputPath);
        String baseName = "microGridEquivalentShunt";
        new CgmesExport().export(network, new Properties(), new DirectoryDataSource(outputPath, baseName));

        // re-import after adding the original boundary files
        copyBoundary(outputPath, baseName, ds);
        Network actual = new CgmesImport().importData(new DirectoryDataSource(outputPath, baseName), NetworkFactory.findDefault(), importParams);

        ShuntCompensator expectedEquivalentShunt = network.getShuntCompensator("d771118f-36e9-4115-a128-cc3d9ce3e3da");
        ShuntCompensator actualEquivalentShunt = actual.getShuntCompensator("d771118f-36e9-4115-a128-cc3d9ce3e3da");
        assertTrue(equivalentShuntsAreEqual(expectedEquivalentShunt, actualEquivalentShunt));
    }

    @Test
    void equivalentShuntWithZeroSectionCountTest() throws IOException {
        ReadOnlyDataSource ds = CgmesConformity1ModifiedCatalog.microGridBaseCaseBEEquivalentShunt().dataSource();
        Network network = new CgmesImport().importData(ds, NetworkFactory.findDefault(), importParams);
        ShuntCompensator equivalentShunt = network.getShuntCompensator("d771118f-36e9-4115-a128-cc3d9ce3e3da");
        equivalentShunt.setSectionCount(0);

        // Export as cgmes
        Path outputPath = tmpDir.resolve("temp.cgmesExport");
        Files.createDirectories(outputPath);
        String baseName = "microGridEquivalentShunt";
        new CgmesExport().export(network, new Properties(), new DirectoryDataSource(outputPath, baseName));

        // re-import after adding the original boundary files
        copyBoundary(outputPath, baseName, ds);
        Network actual = new CgmesImport().importData(new DirectoryDataSource(outputPath, baseName), NetworkFactory.findDefault(), importParams);

        ShuntCompensator actualEquivalentShunt = actual.getShuntCompensator("d771118f-36e9-4115-a128-cc3d9ce3e3da");
        assertTrue(equivalentShuntsAreEqual(equivalentShunt, actualEquivalentShunt));

        // Terminal is disconnected in the actual network as equivalent shunts with
        // sectionCount equals to zero are declared as disconnected in the SSH
        assertTrue(equivalentShunt.getTerminal().isConnected());
        assertFalse(actualEquivalentShunt.getTerminal().isConnected());
    }

    private static void copyBoundary(Path outputFolder, String baseName, ReadOnlyDataSource originalDataSource) throws IOException {
        String eqbd = originalDataSource.listNames(".*EQ_BD.*").stream().findFirst().orElse(null);
        if (eqbd != null) {
            try (InputStream is = originalDataSource.newInputStream(eqbd)) {
                Files.copy(is, outputFolder.resolve(baseName + "_EQ_BD.xml"));
            }
        }
    }

    private static boolean equivalentShuntsAreEqual(ShuntCompensator expectedShunt, ShuntCompensator actualShunt) {
        if (expectedShunt.getMaximumSectionCount() != actualShunt.getMaximumSectionCount()
                || expectedShunt.getSectionCount() != actualShunt.getSectionCount()
                || expectedShunt.getModelType() != actualShunt.getModelType()
                || expectedShunt.getPropertyNames().size() != actualShunt.getPropertyNames().size()) {
            return false;
        }
        if (expectedShunt.getPropertyNames().stream().anyMatch(propertyName -> !propertyInBothAndEqual(expectedShunt, actualShunt, propertyName))) {
            return false;
        }
        ShuntCompensatorLinearModel expectedModel = (ShuntCompensatorLinearModel) expectedShunt.getModel();
        ShuntCompensatorLinearModel actualModel = (ShuntCompensatorLinearModel) actualShunt.getModel();
        return expectedModel.getGPerSection() == actualModel.getGPerSection()
                && expectedModel.getBPerSection() == actualModel.getBPerSection();
    }

    private static boolean propertyInBothAndEqual(ShuntCompensator expected, ShuntCompensator actual, String propertyName) {
        if (!actual.hasProperty(propertyName)) {
            return false;
        }
        return expected.getProperty(propertyName).equals(actual.getProperty(propertyName));
    }

    @Test
    void tapChangerControlDefineControlTest() throws IOException {
        ReadOnlyDataSource ds = Cgmes3Catalog.microGrid().dataSource();
        Network network = new CgmesImport().importData(ds, NetworkFactory.findDefault(), importParams);

        TwoWindingsTransformer twtNetwork = network.getTwoWindingsTransformer("e8a7eaec-51d6-4571-b3d9-c36d52073c33");
        twtNetwork.getPhaseTapChanger().setRegulationMode(PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL)
                .setRegulationValue(75.24)
                .setTargetDeadband(2.0)
                .setRegulationTerminal(twtNetwork.getTerminal1());

        // Export as cgmes
        Path outputPath = tmpDir.resolve("temp.cgmesExport");
        Files.createDirectories(outputPath);
        String baseName = "microGridTapChangerDefineControl";
        new CgmesExport().export(network, new Properties(), new DirectoryDataSource(outputPath, baseName));

        // re-import after adding the original boundary files
        copyBoundary(outputPath, baseName, ds);
        Network actual = new CgmesImport().importData(new DirectoryDataSource(outputPath, baseName), NetworkFactory.findDefault(), importParams);
        TwoWindingsTransformer twtActual = actual.getTwoWindingsTransformer("e8a7eaec-51d6-4571-b3d9-c36d52073c33");

        assertEquals(twtNetwork.getPhaseTapChanger().getRegulationMode().name(), twtActual.getPhaseTapChanger().getRegulationMode().name());
        assertEquals(twtNetwork.getPhaseTapChanger().getRegulationValue(), twtActual.getPhaseTapChanger().getRegulationValue());
        assertEquals(twtNetwork.getPhaseTapChanger().getTargetDeadband(), twtActual.getPhaseTapChanger().getTargetDeadband());
    }

    @Test
    void tapChangerControlDefineRatioTapChangerAndPhaseTapChangerTest() throws IOException {
        ReadOnlyDataSource ds = Cgmes3Catalog.miniGrid().dataSource();
        Network network = Network.read(ds, importParams);

        TwoWindingsTransformer twtNetwork = network.getTwoWindingsTransformer("ceb5d06a-a7ff-4102-a620-7f3ea5fb4a51");
        twtNetwork.newRatioTapChanger()
                .setLowTapPosition(0)
                .setTapPosition(0)
                .beginStep()
                .setRho(1.0)
                .endStep()
                .setTargetV(twtNetwork.getTerminal1().getVoltageLevel().getNominalV())
                .setTargetDeadband(2.0)
                .setRegulationTerminal(twtNetwork.getTerminal1())
                .add();
        twtNetwork.newPhaseTapChanger()
                .setLowTapPosition(0)
                .setTapPosition(0)
                .beginStep()
                .setRho(1.0)
                .setAlpha(0)
                .endStep()
                .setRegulationMode(PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL)
                .setRegulationValue(75.24)
                .setTargetDeadband(2.0)
                .setRegulationTerminal(twtNetwork.getTerminal1())
                .add();

        // Export as cgmes
        Path outputPath = tmpDir.resolve("temp.cgmesExport");
        Files.createDirectories(outputPath);
        String baseName = "microGridTapChangerDefineRatioTapChangerAndPhaseTapChanger";
        network.write("CGMES", null, new DirectoryDataSource(outputPath, baseName));

        // re-import after adding the original boundary files
        copyBoundary(outputPath, baseName, ds);
        Network actual = new CgmesImport().importData(new DirectoryDataSource(outputPath, baseName), NetworkFactory.findDefault(), importParams);
        TwoWindingsTransformer twtActual = actual.getTwoWindingsTransformer("ceb5d06a-a7ff-4102-a620-7f3ea5fb4a51");

        assertEquals(twtNetwork.getRatioTapChanger().getTargetV(), twtActual.getRatioTapChanger().getTargetV());
        assertEquals(twtNetwork.getRatioTapChanger().getTargetDeadband(), twtActual.getRatioTapChanger().getTargetDeadband());

        assertEquals(twtNetwork.getPhaseTapChanger().getRegulationMode().name(), twtActual.getPhaseTapChanger().getRegulationMode().name());
        assertEquals(twtNetwork.getPhaseTapChanger().getRegulationValue(), twtActual.getPhaseTapChanger().getRegulationValue());
        assertEquals(twtNetwork.getPhaseTapChanger().getTargetDeadband(), twtActual.getPhaseTapChanger().getTargetDeadband());
    }

    @Test
    void tapChangerControlDefineRatioTapChangerAndPhaseTapChangerT3wLeg1Test() throws IOException {
        ReadOnlyDataSource ds = Cgmes3Catalog.microGrid().dataSource();
        Network network = new CgmesImport().importData(ds, NetworkFactory.findDefault(), importParams);

        ThreeWindingsTransformer twtNetwork = network.getThreeWindingsTransformer("84ed55f4-61f5-4d9d-8755-bba7b877a246");
        addRatioTapChangerAndPhaseTapChanger(twtNetwork.getLeg1());

        // Export as cgmes
        Path outputPath = tmpDir.resolve("temp.cgmesExport");
        Files.createDirectories(outputPath);
        String baseName = "microGridTapChangerDefineRatioTapChangerAndPhaseTapChangerLeg1";
        new CgmesExport().export(network, new Properties(), new DirectoryDataSource(outputPath, baseName));

        // re-import after adding the original boundary files
        copyBoundary(outputPath, baseName, ds);
        Network actual = new CgmesImport().importData(new DirectoryDataSource(outputPath, baseName), NetworkFactory.findDefault(), importParams);
        ThreeWindingsTransformer twtActual = actual.getThreeWindingsTransformer("84ed55f4-61f5-4d9d-8755-bba7b877a246");

        checkLeg(twtNetwork.getLeg1(), twtActual.getLeg1());
    }

    @Test
    void tapChangerControlDefineRatioTapChangerAndPhaseTapChangerT3wLeg2Test() throws IOException {
        ReadOnlyDataSource ds = Cgmes3Catalog.miniGrid().dataSource();
        Network network = new CgmesImport().importData(ds, NetworkFactory.findDefault(), importParams);
        ThreeWindingsTransformer twtNetwork = network.getThreeWindingsTransformer("411b5401-0a43-404a-acb4-05c3d7d0c95c");
        addRatioTapChangerAndPhaseTapChanger(twtNetwork.getLeg2());

        // Export as cgmes
        Path outputPath = tmpDir.resolve("temp.cgmesExport");
        Files.createDirectories(outputPath);
        String baseName = "microGridTapChangerDefineRatioTapChangerAndPhaseTapChangerLeg2";
        new CgmesExport().export(network, new Properties(), new DirectoryDataSource(outputPath, baseName));

        // re-import after adding the original boundary files
        copyBoundary(outputPath, baseName, ds);
        Network actual = new CgmesImport().importData(new DirectoryDataSource(outputPath, baseName), NetworkFactory.findDefault(), importParams);
        ThreeWindingsTransformer twtActual = actual.getThreeWindingsTransformer("411b5401-0a43-404a-acb4-05c3d7d0c95c");

        checkLeg(twtNetwork.getLeg2(), twtActual.getLeg2());
    }

    @Test
    void tapChangerControlDefineRatioTapChangerAndPhaseTapChangerT3wLeg3Test() throws IOException {
        ReadOnlyDataSource ds = Cgmes3Catalog.miniGrid().dataSource();
        Network network = new CgmesImport().importData(ds, NetworkFactory.findDefault(), importParams);
        ThreeWindingsTransformer twtNetwork = network.getThreeWindingsTransformer("411b5401-0a43-404a-acb4-05c3d7d0c95c");
        addRatioTapChangerAndPhaseTapChanger(twtNetwork.getLeg3());

        // Export as cgmes
        Path outputPath = tmpDir.resolve("temp.cgmesExport");
        Files.createDirectories(outputPath);
        String baseName = "microGridTapChangerDefineRatioTapChangerAndPhaseTapChangerLeg3";
        new CgmesExport().export(network, new Properties(), new DirectoryDataSource(outputPath, baseName));

        // re-import after adding the original boundary files
        copyBoundary(outputPath, baseName, ds);
        Network actual = new CgmesImport().importData(new DirectoryDataSource(outputPath, baseName), NetworkFactory.findDefault(), importParams);
        ThreeWindingsTransformer twtActual = actual.getThreeWindingsTransformer("411b5401-0a43-404a-acb4-05c3d7d0c95c");

        checkLeg(twtNetwork.getLeg3(), twtActual.getLeg3());
    }

    private static void addRatioTapChangerAndPhaseTapChanger(ThreeWindingsTransformer.Leg leg) {
        leg.newRatioTapChanger()
                .setLowTapPosition(0)
                .setTapPosition(0)
                .beginStep()
                .setRho(1.0)
                .endStep()
                .setTargetV(leg.getTerminal().getVoltageLevel().getNominalV())
                .setTargetDeadband(2.0)
                .setRegulationTerminal(leg.getTerminal())
                .add();
        leg.newPhaseTapChanger()
                .setLowTapPosition(0)
                .setTapPosition(0)
                .beginStep()
                .setRho(1.0)
                .setAlpha(0)
                .endStep()
                .setRegulationMode(PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL)
                .setRegulationValue(75.24)
                .setTargetDeadband(2.0)
                .setRegulationTerminal(leg.getTerminal())
                .add();
    }

    private static void checkLeg(ThreeWindingsTransformer.Leg legNetwork, ThreeWindingsTransformer.Leg legActual) {
        assertEquals(legNetwork.getRatioTapChanger().getTargetV(), legActual.getRatioTapChanger().getTargetV());
        assertEquals(legNetwork.getRatioTapChanger().getTargetDeadband(), legActual.getRatioTapChanger().getTargetDeadband());

        assertEquals(legNetwork.getPhaseTapChanger().getRegulationMode().name(), legActual.getPhaseTapChanger().getRegulationMode().name());
        assertEquals(legNetwork.getPhaseTapChanger().getRegulationValue(), legActual.getPhaseTapChanger().getRegulationValue());
        assertEquals(legNetwork.getPhaseTapChanger().getTargetDeadband(), legActual.getPhaseTapChanger().getTargetDeadband());
    }

    @Test
    void fossilFuelExportAndImportTest() throws IOException {
        Network network = createOneGeneratorNetwork();

        // Define fossilFuelType property
        Generator expectedGenerator = network.getGenerator("generator1");
        expectedGenerator.setEnergySource(EnergySource.THERMAL);
        String expectedFuelType = "gas;lignite";
        expectedGenerator.setProperty(Conversion.PROPERTY_FOSSIL_FUEL_TYPE, expectedFuelType);

        // Export as cgmes
        Path outputPath = tmpDir.resolve("temp.cgmesExport");
        Files.createDirectories(outputPath);
        String baseName = "oneGeneratorFossilFuel";
        new CgmesExport().export(network, new Properties(), new DirectoryDataSource(outputPath, baseName));

        // re-import
        Network actual = new CgmesImport().importData(new DirectoryDataSource(outputPath, baseName), NetworkFactory.findDefault(), new Properties());
        Generator actualGenerator = actual.getGenerator("generator1");

        // check the fuelType property
        String actualFuelType = actualGenerator.getProperty(Conversion.PROPERTY_FOSSIL_FUEL_TYPE);
        assertEquals(expectedFuelType, actualFuelType);
    }

    @Test
    void hydroPowerPlantExportAndImportTest() throws IOException {
        Network network = createOneGeneratorNetwork();

        // Define hydroPowerPlant property
        Generator expectedGenerator = network.getGenerator("generator1");
        expectedGenerator.setEnergySource(EnergySource.HYDRO);
        String expectedStorageKind = "pumpedStorage";
        expectedGenerator.setProperty(Conversion.PROPERTY_HYDRO_PLANT_STORAGE_TYPE, expectedStorageKind);

        // Export as cgmes
        Path outputPath = tmpDir.resolve("temp.cgmesExport");
        Files.createDirectories(outputPath);
        String baseName = "oneGeneratorFossilHydroPowerPlant";
        new CgmesExport().export(network, new Properties(), new DirectoryDataSource(outputPath, baseName));

        // re-import
        Network actual = new CgmesImport().importData(new DirectoryDataSource(outputPath, baseName), NetworkFactory.findDefault(), new Properties());
        Generator actualGenerator = actual.getGenerator("generator1");

        // check the storage kind property
        String actualStorageKind = actualGenerator.getProperty(Conversion.PROPERTY_HYDRO_PLANT_STORAGE_TYPE);
        assertEquals(expectedStorageKind, actualStorageKind);
    }

    @Test
    void synchronousMachineKindExportAndImportTest() throws IOException {
        Network network = createOneGeneratorNetwork();

        // Define the synchronous machine kind property
        Generator expectedGenerator = network.getGenerator("generator1");
        expectedGenerator.setMinP(-50.0).setMaxP(0.0).setTargetP(-10.0);
        String expectedSynchronousMachineKind = "motorOrCondenser";
        expectedGenerator.setProperty(Conversion.PROPERTY_CGMES_SYNCHRONOUS_MACHINE_TYPE, expectedSynchronousMachineKind);
        String expectedOperatingMode = "motor";
        expectedGenerator.setProperty(Conversion.PROPERTY_CGMES_SYNCHRONOUS_MACHINE_OPERATING_MODE, expectedOperatingMode);

        // Export as cgmes
        Path outputPath = tmpDir.resolve("temp.cgmesExport");
        Files.createDirectories(outputPath);
        String baseName = "oneGeneratorSynchronousMachineKind";
        new CgmesExport().export(network, new Properties(), new DirectoryDataSource(outputPath, baseName));

        // re-import
        Network actual = new CgmesImport().importData(new DirectoryDataSource(outputPath, baseName), NetworkFactory.findDefault(), new Properties());
        Generator actualGenerator = actual.getGenerator("generator1");

        // check the synchronous machine kind
        String actualSynchronousMachineKind = actualGenerator.getProperty(Conversion.PROPERTY_CGMES_SYNCHRONOUS_MACHINE_TYPE);
        assertEquals(expectedSynchronousMachineKind, actualSynchronousMachineKind);
        String actualOperatingMode = actualGenerator.getProperty(Conversion.PROPERTY_CGMES_SYNCHRONOUS_MACHINE_OPERATING_MODE);
        assertEquals(expectedOperatingMode, actualOperatingMode);
    }

    @Test
    void phaseTapChangerTapChangerControlEQTest() throws IOException {
        String exportFolder = "/test-pst-tcc";
        String baseName = "testPstTcc";
        Network network;
        String eq;
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            Path tmpDir = Files.createDirectory(fs.getPath(exportFolder));
            Properties exportParams = new Properties();
            exportParams.put(CgmesExport.PROFILES, "EQ");

            // PST with FIXED_TAP
            network = PhaseShifterTestCaseFactory.createWithTargetDeadband();
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testTcTccWithoutAttribute(eq, "_PS1_PTC_RC", "_PS1_PT_T_2", "activePower");

            // PST local with ACTIVE_POWER_CONTROL
            network = PhaseShifterTestCaseFactory.createLocalActivePowerWithTargetDeadband();
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testTcTccWithAttribute(eq, "_PS1_PTC_RC", "_PS1_PT_T_2", "activePower");
            network.getTwoWindingsTransformer("PS1").getPhaseTapChanger().setRegulating(true);
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testTcTccWithAttribute(eq, "_PS1_PTC_RC", "_PS1_PT_T_2", "activePower");

            // PST local with CURRENT_LIMITER
            network = PhaseShifterTestCaseFactory.createLocalCurrentLimiterWithTargetDeadband();
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testTcTccWithAttribute(eq, "_PS1_PTC_RC", "_PS1_PT_T_2", "currentFlow");
            network.getTwoWindingsTransformer("PS1").getPhaseTapChanger().setRegulating(true);
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testTcTccWithAttribute(eq, "_PS1_PTC_RC", "_PS1_PT_T_2", "currentFlow");

            // PST remote with CURRENT_LIMITER
            network = PhaseShifterTestCaseFactory.createRemoteCurrentLimiterWithTargetDeadband();
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testTcTccWithAttribute(eq, "_PS1_PTC_RC", "_LD2_EC_T_1", "currentFlow");
            network.getTwoWindingsTransformer("PS1").getPhaseTapChanger().setRegulating(true);
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testTcTccWithAttribute(eq, "_PS1_PTC_RC", "_LD2_EC_T_1", "currentFlow");

            // PST remote with ACTIVE_POWER_CONTROL
            network = PhaseShifterTestCaseFactory.createRemoteActivePowerWithTargetDeadband();
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testTcTccWithAttribute(eq, "_PS1_PTC_RC", "_LD2_EC_T_1", "activePower");
            network.getTwoWindingsTransformer("PS1").getPhaseTapChanger().setRegulating(true);
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testTcTccWithAttribute(eq, "_PS1_PTC_RC", "_LD2_EC_T_1", "activePower");
        }
    }

    @Test
    void ratioTapChangerTapChangerControlEQTest() throws IOException {
        String exportFolder = "/test-rtc-tcc";
        String baseName = "testRtcTcc";
        Network network;
        String eq;
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            Path tmpDir = Files.createDirectory(fs.getPath(exportFolder));
            Properties exportParams = new Properties();
            exportParams.put(CgmesExport.PROFILES, "EQ");

            // RTC without control
            network = EurostagTutorialExample1Factory.createWithoutRtcControl();
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testTcTccWithoutAttribute(eq, "_NHV2_NLOAD_RTC_RC", "", "dummy");

            // RTC local with VOLTAGE
            network = EurostagTutorialExample1Factory.create();
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testTcTccWithAttribute(eq, "_NHV2_NLOAD_RTC_RC", "_NHV2_NLOAD_PT_T_2", "voltage");
            network.getTwoWindingsTransformer("NHV2_NLOAD").getRatioTapChanger().setRegulating(false);
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testTcTccWithAttribute(eq, "_NHV2_NLOAD_RTC_RC", "_NHV2_NLOAD_PT_T_2", "voltage");

            // RTC local with REACTIVE_POWER
            network = EurostagTutorialExample1Factory.createWithReactiveTcc();
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testTcTccWithAttribute(eq, "_NHV2_NLOAD_RTC_RC", "_NHV2_NLOAD_PT_T_2", "reactivePower");
            network.getTwoWindingsTransformer("NHV2_NLOAD").getRatioTapChanger().setRegulating(false);
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testTcTccWithAttribute(eq, "_NHV2_NLOAD_RTC_RC", "_NHV2_NLOAD_PT_T_2", "reactivePower");

            // RTC remote with VOLTAGE
            network = EurostagTutorialExample1Factory.createRemoteVoltageTcc();
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testTcTccWithAttribute(eq, "_NHV2_NLOAD_RTC_RC", "_GEN_SM_T_1", "voltage");
            network.getTwoWindingsTransformer("NHV2_NLOAD").getRatioTapChanger().setRegulating(false);
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testTcTccWithAttribute(eq, "_NHV2_NLOAD_RTC_RC", "_GEN_SM_T_1", "voltage");

            // RTC remote with REACTIVE_POWER
            network = EurostagTutorialExample1Factory.createRemoteReactiveTcc();
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testTcTccWithAttribute(eq, "_NHV2_NLOAD_RTC_RC", "_GEN_SM_T_1", "reactivePower");
            network.getTwoWindingsTransformer("NHV2_NLOAD").getRatioTapChanger().setRegulating(false);
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testTcTccWithAttribute(eq, "_NHV2_NLOAD_RTC_RC", "_GEN_SM_T_1", "reactivePower");

            // 3w without control
            network = EurostagTutorialExample1Factory.createWith3wWithoutControl();
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testTcTccWithoutAttribute(eq, "_NGEN_V2_NHV1_RTC_RC", "", "dummy");

            // 3w with local voltage control
            network = EurostagTutorialExample1Factory.createWith3wWithVoltageControl();
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testTcTccWithAttribute(eq, "_NGEN_V2_NHV1_RTC_RC", "_NGEN_V2_NHV1_PT_T_1", "voltage");

            // 3w with local reactive control
            network = EurostagTutorialExample1Factory.create3wWithReactiveTcc();
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testTcTccWithAttribute(eq, "_NGEN_V2_NHV1_RTC_RC", "_NGEN_V2_NHV1_PT_T_1", "reactivePower");
            network.getThreeWindingsTransformer("NGEN_V2_NHV1").getLeg1().getRatioTapChanger().setRegulating(false);
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testTcTccWithAttribute(eq, "_NGEN_V2_NHV1_RTC_RC", "_NGEN_V2_NHV1_PT_T_1", "reactivePower");

            // 3w with remote voltage
            network = EurostagTutorialExample1Factory.create3wRemoteVoltageTcc();
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testTcTccWithAttribute(eq, "_NGEN_V2_NHV1_RTC_RC", "_GEN_SM_T_1", "voltage");
            network.getThreeWindingsTransformer("NGEN_V2_NHV1").getLeg1().getRatioTapChanger().setRegulating(false);
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testTcTccWithAttribute(eq, "_NGEN_V2_NHV1_RTC_RC", "_GEN_SM_T_1", "voltage");

            // 3w with remote reactive
            network = EurostagTutorialExample1Factory.create3wRemoteReactiveTcc();
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testTcTccWithAttribute(eq, "_NGEN_V2_NHV1_RTC_RC", "_GEN_SM_T_1", "reactivePower");
            network.getThreeWindingsTransformer("NGEN_V2_NHV1").getLeg1().getRatioTapChanger().setRegulating(false);
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testTcTccWithAttribute(eq, "_NGEN_V2_NHV1_RTC_RC", "_GEN_SM_T_1", "reactivePower");
        }
    }

    @Test
    void staticVarCompensatorRegulatingControlEQTest() throws IOException {
        String exportFolder = "/test-svc-rc";
        String baseName = "testSvcRc";
        Network network;
        String eq;
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            Path tmpDir = Files.createDirectory(fs.getPath(exportFolder));
            Properties exportParams = new Properties();
            exportParams.put(CgmesExport.PROFILES, "EQ");

            // SVC VOLTAGE
            // Local
            network = SvcTestCaseFactory.createLocalVoltageControl();
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(eq, "_SVC2_RC", "_SVC2_SVC_T_1", "voltage");

            // Remote
            network = SvcTestCaseFactory.createRemoteVoltageControl();
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(eq, "_SVC2_RC", "_L2_EC_T_1", "voltage");

            // SVC REACTIVE_POWER
            // Local
            network = SvcTestCaseFactory.createLocalReactiveControl();
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(eq, "_SVC2_RC", "_SVC2_SVC_T_1", "reactivePower");

            // Remote
            network = SvcTestCaseFactory.createRemoteReactiveControl();
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(eq, "_SVC2_RC", "_L2_EC_T_1", "reactivePower");

            // SVC OFF
            // Local
            network = SvcTestCaseFactory.createLocalOffNoTarget();
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testRcEqRCWithoutAttribute(eq, "_SVC2_RC", "_SVC2_SVC_T_1", "dummy");
            network = SvcTestCaseFactory.createLocalOffReactiveTarget();
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(eq, "_SVC2_RC", "_SVC2_SVC_T_1", "reactivePower");
            network = SvcTestCaseFactory.createLocalOffVoltageTarget();
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(eq, "_SVC2_RC", "_SVC2_SVC_T_1", "voltage");
            network = SvcTestCaseFactory.createLocalOffBothTarget();
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(eq, "_SVC2_RC", "_SVC2_SVC_T_1", "voltage");

            // Remote
            network = SvcTestCaseFactory.createRemoteOffNoTarget();
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(eq, "_SVC2_RC", "_L2_EC_T_1", "voltage");
            network = SvcTestCaseFactory.createRemoteOffReactiveTarget();
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(eq, "_SVC2_RC", "_L2_EC_T_1", "reactivePower");
            network = SvcTestCaseFactory.createRemoteOffVoltageTarget();
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(eq, "_SVC2_RC", "_L2_EC_T_1", "voltage");
            network = SvcTestCaseFactory.createRemoteOffBothTarget();
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(eq, "_SVC2_RC", "_L2_EC_T_1", "voltage");
        }
    }

    @Test
    void shuntCompensatorRegulatingControlEQTest() throws IOException {
        String exportFolder = "/test-sc-rc";
        String baseName = "testScRc";
        Network network;
        String eq;
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            Path tmpDir = Files.createDirectory(fs.getPath(exportFolder));
            Properties exportParams = new Properties();
            exportParams.put(CgmesExport.PROFILES, "EQ");

            // SC linear
            network = ShuntTestCaseFactory.create();
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(eq, "_SHUNT_RC", "_LOAD_EC_T_1", "voltage");
            testRcEqRCWithoutAttribute(eq, "", "", "reactivePower");

            network = ShuntTestCaseFactory.createLocalLinear();
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(eq, "_SHUNT_RC", "_SHUNT_SC_T_1", "voltage");
            testRcEqRCWithoutAttribute(eq, "", "", "reactivePower");

            network = ShuntTestCaseFactory.createDisabledRemoteLinear();
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(eq, "_SHUNT_RC", "_LOAD_EC_T_1", "voltage");
            testRcEqRCWithoutAttribute(eq, "", "", "reactivePower");

            network = ShuntTestCaseFactory.createDisabledLocalLinear();
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(eq, "_SHUNT_RC", "_SHUNT_SC_T_1", "voltage");
            testRcEqRCWithoutAttribute(eq, "", "", "reactivePower");

            network = ShuntTestCaseFactory.createLocalLinearNoTarget();
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testRcEqRCWithoutAttribute(eq, "_SHUNT_RC", "", "");

            network = ShuntTestCaseFactory.createRemoteLinearNoTarget();
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(eq, "_SHUNT_RC", "_LOAD_EC_T_1", "voltage");
            testRcEqRCWithoutAttribute(eq, "", "", "reactivePower");

            // SC nonlinear
            network = ShuntTestCaseFactory.createNonLinear();
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(eq, "_SHUNT_RC", "_LOAD_EC_T_1", "voltage");
            testRcEqRCWithoutAttribute(eq, "", "", "reactivePower");

            network = ShuntTestCaseFactory.createLocalNonLinear();
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(eq, "_SHUNT_RC", "_SHUNT_SC_T_1", "voltage");
            testRcEqRCWithoutAttribute(eq, "", "", "reactivePower");

            network = ShuntTestCaseFactory.createDisabledRemoteNonLinear();
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(eq, "_SHUNT_RC", "_LOAD_EC_T_1", "voltage");
            testRcEqRCWithoutAttribute(eq, "", "", "reactivePower");

            network = ShuntTestCaseFactory.createDisabledLocalNonLinear();
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(eq, "_SHUNT_RC", "_SHUNT_SC_T_1", "voltage");

            network = ShuntTestCaseFactory.createLocalNonLinearNoTarget();
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testRcEqRCWithoutAttribute(eq, "_SHUNT_RC", "", "");

            network = ShuntTestCaseFactory.createRemoteNonLinearNoTarget();
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(eq, "_SHUNT_RC", "_LOAD_EC_T_1", "voltage");
            testRcEqRCWithoutAttribute(eq, "", "", "reactivePower");
        }
    }

    @Test
    void generatorRegulatingControlEQTest() throws IOException {
        String exportFolder = "/test-gen-rc";
        String baseName = "testGenRc";
        Network network;
        String eq;
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            Path tmpDir = Files.createDirectory(fs.getPath(exportFolder));
            Properties exportParams = new Properties();
            exportParams.put(CgmesExport.PROFILES, "EQ");

            // Generator local voltage
            network = EurostagTutorialExample1Factory.create();
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(eq, "_GEN_RC", "_GEN_SM_T_1", "voltage");
            network.getGenerator("GEN").setVoltageRegulatorOn(false);
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(eq, "_GEN_RC", "_GEN_SM_T_1", "voltage");

            // Generator remote voltage
            network = EurostagTutorialExample1Factory.createWithRemoteVoltageGenerator();
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(eq, "_GEN_RC", "_NHV2_NLOAD_PT_T_1", "voltage");
            network.getGenerator("GEN").setVoltageRegulatorOn(false);
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(eq, "_GEN_RC", "_NHV2_NLOAD_PT_T_1", "voltage");

            // Generator with remote voltage regulation exported in local regulation mode
            Properties exportInLocalRegulationModeParams = new Properties();
            exportInLocalRegulationModeParams.put(CgmesExport.PROFILES, "EQ");
            exportInLocalRegulationModeParams.put(CgmesExport.EXPORT_GENERATORS_IN_LOCAL_REGULATION_MODE, true);
            network = EurostagTutorialExample1Factory.createWithRemoteVoltageGenerator();
            eq = getEQ(network, baseName, tmpDir, exportInLocalRegulationModeParams);
            testRcEqRcWithAttribute(eq, "_GEN_RC", "_GEN_SM_T_1", "voltage");

            // Generator with local reactive
            network = EurostagTutorialExample1Factory.createWithLocalReactiveGenerator();
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(eq, "_GEN_RC", "_GEN_SM_T_1", "reactivePower");
            network.getGenerator("GEN").getExtension(RemoteReactivePowerControl.class).setEnabled(false);
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(eq, "_GEN_RC", "_GEN_SM_T_1", "reactivePower");

            // Generator with remote reactive
            network = EurostagTutorialExample1Factory.createWithRemoteReactiveGenerator();
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(eq, "_GEN_RC", "_NHV2_NLOAD_PT_T_1", "reactivePower");
            network.getGenerator("GEN").getExtension(RemoteReactivePowerControl.class).setEnabled(false);
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(eq, "_GEN_RC", "_NHV2_NLOAD_PT_T_1", "reactivePower");

            // Generator with local reactive and voltage
            network = EurostagTutorialExample1Factory.createWithLocalReactiveAndVoltageGenerator();
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(eq, "_GEN_RC", "_GEN_SM_T_1", "voltage");
            network.getGenerator("GEN").setVoltageRegulatorOn(false);
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(eq, "_GEN_RC", "_GEN_SM_T_1", "reactivePower");
            network.getGenerator("GEN").getExtension(RemoteReactivePowerControl.class).setEnabled(false);
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(eq, "_GEN_RC", "_GEN_SM_T_1", "voltage");
            network.getGenerator("GEN").setVoltageRegulatorOn(true);
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(eq, "_GEN_RC", "_GEN_SM_T_1", "voltage");

            // Generator with remote reactive and voltage
            network = EurostagTutorialExample1Factory.createWithRemoteReactiveAndVoltageGenerators();
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(eq, "_GEN_RC", "_NHV2_NLOAD_PT_T_1", "voltage");
            network.getGenerator("GEN").setVoltageRegulatorOn(false);
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(eq, "_GEN_RC", "_NHV2_NLOAD_PT_T_1", "reactivePower");
            network.getGenerator("GEN").getExtension(RemoteReactivePowerControl.class).setEnabled(false);
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(eq, "_GEN_RC", "_NHV2_NLOAD_PT_T_1", "voltage");
            network.getGenerator("GEN").setVoltageRegulatorOn(true);
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(eq, "_GEN_RC", "_NHV2_NLOAD_PT_T_1", "voltage");

            // Generator without control
            network = EurostagTutorialExample1Factory.createWithoutControl();
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(eq, "_GEN_RC", "_GEN_SM_T_1", "voltage");

            // Generator with remote terminal without control
            network = EurostagTutorialExample1Factory.createRemoteWithoutControl();
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(eq, "_GEN_RC", "_NHV2_NLOAD_PT_T_1", "voltage");

            // Generator without control capability
            network = EurostagTutorialExample1Factory.create();
            network.getGenerator("GEN").newMinMaxReactiveLimits().setMaxQ(0).setMinQ(0).add();
            eq = getEQ(network, baseName, tmpDir, exportParams);
            testRcEqRCWithoutAttribute(eq, "_GEN_RC", "", "dummy");
        }
    }

    private void testTcTccWithoutAttribute(String eq, String rcID, String terID, String rcMode) {
        assertFalse(eq.contains("cim:TapChangerControl rdf:ID=\"" + rcID + "\""));
        assertFalse(eq.contains("cim:TapChanger.TapChangerControl rdf:resource=\"#" + rcID + "\""));
        assertFalse(eq.contains("RegulatingControlModeKind." + rcMode));
        assertFalse(eq.contains("cim:RegulatingControl.Terminal rdf:resource=\"#" + terID + "\""));
    }

    private void testTcTccWithAttribute(String eq, String rcID, String terID, String rcMode) {
        assertTrue(eq.contains("cim:TapChangerControl rdf:ID=\"" + rcID + "\""));
        assertTrue(eq.contains("cim:TapChanger.TapChangerControl rdf:resource=\"#" + rcID + "\""));
        assertTrue(eq.contains("RegulatingControlModeKind." + rcMode));
        assertTrue(eq.contains("cim:RegulatingControl.Terminal rdf:resource=\"#" + terID + "\""));
    }

    private void testRcEqRCWithoutAttribute(String eq, String rcID, String terID, String rcMode) {
        assertFalse(eq.contains("cim:RegulatingControl rdf:ID=\"" + rcID + "\""));
        assertFalse(eq.contains("cim:RegulatingCondEq.RegulatingControl rdf:resource=\"#" + rcID + "\""));
        // dummy kind is used when false assertion would trigger because other RCs are present from other equipment
        assertFalse(eq.contains("RegulatingControlModeKind." + rcMode));
        assertFalse(eq.contains("cim:RegulatingControl.Terminal rdf:resource=\"#" + terID + "\""));
    }

    private void testRcEqRcWithAttribute(String eq, String rcID, String terID, String rcMode) {
        assertTrue(eq.contains("cim:RegulatingControl rdf:ID=\"" + rcID + "\""));
        assertTrue(eq.contains("cim:RegulatingCondEq.RegulatingControl rdf:resource=\"#" + rcID + "\""));
        assertTrue(eq.contains("RegulatingControlModeKind." + rcMode));
        assertTrue(eq.contains("cim:RegulatingControl.Terminal rdf:resource=\"#" + terID + "\""));
    }

    private String getEQ(Network network, String baseName, Path tmpDir, Properties exportParams) throws IOException {
        new CgmesExport().export(network, exportParams, new DirectoryDataSource(tmpDir, baseName));
        return Files.readString(tmpDir.resolve(baseName + "_EQ.xml"));
    }

    private Network createOneGeneratorNetwork() {
        Network network = NetworkFactory.findDefault().createNetwork("network", "test");
        Substation substation1 = network.newSubstation()
                .setId("substation1")
                .setCountry(Country.FR)
                .setTso("TSO1")
                .setGeographicalTags("region1")
                .add();
        VoltageLevel voltageLevel1 = substation1.newVoltageLevel()
                .setId("voltageLevel1")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        voltageLevel1.getNodeBreakerView()
                .newBusbarSection()
                .setId("busbarSection1")
                .setNode(0)
                .add();
        Generator generator1 = voltageLevel1.newGenerator()
                .setId("generator1")
                .setNode(1)
                .setMinP(0.0)
                .setMaxP(100.0)
                .setTargetP(25.0)
                .setTargetQ(10.0)
                .setVoltageRegulatorOn(false)
                .add();
        generator1.newMinMaxReactiveLimits().setMinQ(-50.0).setMaxQ(50.0).add();
        voltageLevel1.getNodeBreakerView().newInternalConnection().setNode1(0).setNode2(1).add();
        return network;
    }

    private Network exportImportNodeBreaker(Network expected, ReadOnlyDataSource dataSource) throws IOException, XMLStreamException {
        return exportImport(expected, dataSource, false, true, false);
    }

    private Network exportImportBusBranch(Network expected, ReadOnlyDataSource dataSource) throws IOException, XMLStreamException {
        return exportImport(expected, dataSource, true, true, false);
    }

    private Network exportImportBusBranchNoBoundaries(Network expected, ReadOnlyDataSource dataSource) throws IOException, XMLStreamException {
        return exportImport(expected, dataSource, true, false, false);
    }

    private Network exportImportNodeBreakerNoBoundaries(Network expected) throws IOException, XMLStreamException {
        return exportImport(expected, null, false, false, true);
    }

    private Network createThreeWindingTransformerNetwork() {
        Network network = NetworkFactory.findDefault().createNetwork("network", "test");
        Substation substation1 = network.newSubstation()
                .setId("substation1")
                .setCountry(Country.FR)
                .setTso("TSO1")
                .setGeographicalTags("region1")
                .add();
        VoltageLevel voltageLevel1 = substation1.newVoltageLevel()
                .setId("voltageLevel1")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        VoltageLevel voltageLevel2 = substation1.newVoltageLevel()
                .setId("voltageLevel2")
                .setNominalV(220)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        VoltageLevel voltageLevel3 = substation1.newVoltageLevel()
                .setId("voltageLevel3")
                .setNominalV(60)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        VoltageLevel.NodeBreakerView topology1 = voltageLevel1.getNodeBreakerView();
        BusbarSection voltageLevel1BusbarSection1 = topology1.newBusbarSection()
                .setId("voltageLevel1BusbarSection1")
                .setNode(0)
                .add();
        VoltageLevel.NodeBreakerView topology2 = voltageLevel2.getNodeBreakerView();
        BusbarSection voltageLevel1BusbarSection2 = topology2.newBusbarSection()
                .setId("voltageLevel1BusbarSection2")
                .setNode(0)
                .add();
        VoltageLevel.NodeBreakerView topology3 = voltageLevel3.getNodeBreakerView();
        BusbarSection voltageLevel1BusbarSection3 = topology3.newBusbarSection()
                .setId("voltageLevel1BusbarSection3")
                .setNode(0)
                .add();
        ThreeWindingsTransformerAdder threeWindingsTransformerAdder1 = substation1.newThreeWindingsTransformer()
                .setId("threeWindingsTransformer1")
                .setRatedU0(400);
        threeWindingsTransformerAdder1.newLeg1()
                .setNode(1)
                .setR(0.001)
                .setX(0.000001)
                .setB(0)
                .setG(0)
                .setRatedU(400)
                .setVoltageLevel("voltageLevel1")
                .add();
        threeWindingsTransformerAdder1.newLeg2()
                .setNode(1)
                .setR(0.1)
                .setX(0.00001)
                .setB(0)
                .setG(0)
                .setRatedU(220)
                .setVoltageLevel("voltageLevel2")
                .add();
        threeWindingsTransformerAdder1.newLeg3()
                .setNode(1)
                .setR(0.01)
                .setX(0.0001)
                .setB(0)
                .setG(0)
                .setRatedU(60)
                .setVoltageLevel("voltageLevel3")
                .add();
        ThreeWindingsTransformer threeWindingsTransformer1 = threeWindingsTransformerAdder1.add();
        threeWindingsTransformer1.getLeg1().newRatioTapChanger()
                .setLowTapPosition(0)
                .setTapPosition(0)
                .beginStep()
                    .setR(0.01)
                    .setX(0.0001)
                    .setB(0)
                    .setG(0)
                    .setRho(1.1)
                    .endStep()
                .add();
        threeWindingsTransformer1.getLeg2().newRatioTapChanger()
                .setLowTapPosition(0)
                .setTapPosition(0)
                .beginStep()
                    .setR(0.02)
                    .setX(0.0002)
                    .setB(0)
                    .setG(0)
                    .setRho(1.2)
                    .endStep()
                .add();
        threeWindingsTransformer1.getLeg3().newRatioTapChanger()
                .setLowTapPosition(0)
                .setTapPosition(0)
                .beginStep()
                    .setR(0.03)
                    .setX(0.0003)
                    .setB(0)
                    .setG(0)
                    .setRho(1.3)
                .endStep()
                .add();
        threeWindingsTransformer1.getLeg1().newPhaseTapChanger()
                .setLowTapPosition(0)
                .setTapPosition(0)
                .beginStep()
                    .setR(0.01)
                    .setX(0.0001)
                    .setB(0)
                    .setG(0)
                    .setRho(1.1)
                    .setAlpha(10)
                .endStep()
                .add();
        threeWindingsTransformer1.getLeg2().newPhaseTapChanger()
                .setLowTapPosition(0)
                .setTapPosition(0)
                .beginStep()
                    .setR(0.02)
                    .setX(0.0002)
                    .setB(0)
                    .setG(0)
                    .setRho(1.2)
                    .setAlpha(20)
                .endStep()
                .add();
        threeWindingsTransformer1.getLeg3().newPhaseTapChanger()
                .setLowTapPosition(0)
                .setTapPosition(0)
                .beginStep()
                    .setR(0.03)
                    .setX(0.0003)
                    .setB(0)
                    .setG(0)
                    .setRho(1.3)
                    .setAlpha(30)
                .endStep()
                .add();

        topology1.newDisconnector()
                .setId("Disconnector1")
                .setOpen(false)
                .setNode1(threeWindingsTransformer1.getLeg1().getTerminal().getNodeBreakerView().getNode())
                .setNode2(voltageLevel1BusbarSection1.getTerminal().getNodeBreakerView().getNode())
                .add();
        topology2.newDisconnector()
                .setId("Disconnector2")
                .setOpen(false)
                .setNode1(threeWindingsTransformer1.getLeg2().getTerminal().getNodeBreakerView().getNode())
                .setNode2(voltageLevel1BusbarSection2.getTerminal().getNodeBreakerView().getNode())
                .add();
        topology3.newDisconnector()
                .setId("Disconnector3")
                .setOpen(false)
                .setNode1(threeWindingsTransformer1.getLeg3().getTerminal().getNodeBreakerView().getNode())
                .setNode2(voltageLevel1BusbarSection3.getTerminal().getNodeBreakerView().getNode())
                .add();

        return network;
    }

    private Network exportImport(Network expected, ReadOnlyDataSource dataSource, boolean importTP, boolean importBD, boolean transformersWithHighestVoltageAtEnd1) throws IOException, XMLStreamException {
        Path exportedEq = exportToCgmesEQ(expected, transformersWithHighestVoltageAtEnd1);

        // From reference data source we use only boundaries
        Path repackaged = tmpDir.resolve("repackaged.zip");
        Repackager r = new Repackager(dataSource)
                .with("test_EQ.xml", exportedEq);
        if (importTP) {
            r.with("test_TP.xml", Repackager::tp);
        }
        if (importBD) {
            r.with("test_EQ_BD.xml", Repackager::eqBd)
                    .with("test_TP_BD.xml", Repackager::tpBd);
        }
        r.zip(repackaged);

        // Import with new EQ
        // There is no need to create the IIDM-CGMES mappings
        // We are reading only an EQ, we won't have TP data in the input
        // And to compare the expected and actual networks we are dropping all IIDM-CGMES mapping context information
        return Network.read(repackaged, LocalComputationManager.getDefault(), ImportConfig.load(), importParams);
    }

    private Path exportToCgmesEQ(Network network) throws IOException, XMLStreamException {
        return exportToCgmesEQ(network, false);
    }

    private Path exportToCgmesEQ(Network network, boolean transformersWithHighestVoltageAtEnd1) throws IOException, XMLStreamException {
        // Export CGMES EQ file
        Path exportedEq = tmpDir.resolve("exportedEq.xml");
        try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(exportedEq))) {
            XMLStreamWriter writer = XmlUtil.initializeWriter(true, "    ", os);
            CgmesExportContext context = new CgmesExportContext(network)
                    .setExportEquipment(true)
                    .setExportTransformersWithHighestVoltageAtEnd1(transformersWithHighestVoltageAtEnd1);
            EquipmentExport.write(network, writer, context);
        }
        return exportedEq;
    }

    private Path exportToCgmesTP(Network network) throws IOException, XMLStreamException {
        // Export CGMES EQ file
        Path exportedTp = tmpDir.resolve("exportedTp.xml");
        try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(exportedTp))) {
            XMLStreamWriter writer = XmlUtil.initializeWriter(true, "    ", os);
            CgmesExportContext context = new CgmesExportContext(network);
            TopologyExport.write(network, writer, context);
        }

        return exportedTp;
    }

    private boolean compareNetworksEQdata(Network expected, Network actual) {
        DifferenceEvaluator knownDiffs = DifferenceEvaluators.chain(
                DifferenceEvaluators.Default,
                ExportXmlCompare::numericDifferenceEvaluator,
                ExportXmlCompare::ignoringNonEQ);
        return compareNetworksEQdata(expected, actual, knownDiffs);
    }

    private boolean compareNetworksEQdata(Network expected, Network actual, DifferenceEvaluator knownDiffs) {
        Network expectedNetwork = prepareNetworkForEQComparison(expected);
        Network actualNetwork = prepareNetworkForEQComparison(actual);

        // Export original and only EQ
        ExportOptions exportOptions = new ExportOptions();
        exportOptions.setExtensions(Set.of("cgmesControlAreas"));
        exportOptions.setSorted(true);

        Path expectedPath = tmpDir.resolve("expected.xml");
        Path actualPath = tmpDir.resolve("actual.xml");
        NetworkSerDe.write(expectedNetwork, exportOptions, expectedPath);
        NetworkSerDe.write(actualNetwork, exportOptions, actualPath);
        NetworkSerDe.validate(actualPath);

        // Compare
        compareTemporaryLimits(Network.read(expectedPath), Network.read(actualPath));
        return ExportXmlCompare.compareEQNetworks(expectedPath, actualPath, knownDiffs);
    }

    private void compareTemporaryLimits(Network expected, Network actual) {
        for (Line line : actual.getLines()) {
            Identifiable identifiable = expected.getIdentifiable(line.getId());
            if (identifiable instanceof Branch) {
                compareBranchLimits((Branch) identifiable, line);
            } else {
                compareFlowBranchLimits((FlowsLimitsHolder) identifiable, line);
            }
        }
        for (TwoWindingsTransformer twt : actual.getTwoWindingsTransformers()) {
            compareBranchLimits((Branch) expected.getIdentifiable(twt.getId()), twt);
        }
        for (ThreeWindingsTransformer twt : actual.getThreeWindingsTransformers()) {
            ThreeWindingsTransformer expectedTwt = (ThreeWindingsTransformer) expected.getIdentifiable(twt.getId());
            compareFlowLimits(expectedTwt.getLeg1(), twt.getLeg1());
            compareFlowLimits(expectedTwt.getLeg2(), twt.getLeg2());
            compareFlowLimits(expectedTwt.getLeg3(), twt.getLeg3());
        }
        for (DanglingLine danglingLine : actual.getDanglingLines(DanglingLineFilter.ALL)) {
            compareFlowLimits((FlowsLimitsHolder) expected.getIdentifiable(danglingLine.getId()), danglingLine);
        }
    }

    private void compareBranchLimits(Branch<?> expected, Branch<?> actual) {
        actual.getActivePowerLimits1().ifPresent(lim -> compareLoadingLimits(expected.getActivePowerLimits1().orElse(null), lim));
        actual.getActivePowerLimits2().ifPresent(lim -> compareLoadingLimits(expected.getActivePowerLimits2().orElse(null), lim));
        actual.getApparentPowerLimits1().ifPresent(lim -> compareLoadingLimits(expected.getApparentPowerLimits1().orElse(null), lim));
        actual.getApparentPowerLimits2().ifPresent(lim -> compareLoadingLimits(expected.getApparentPowerLimits2().orElse(null), lim));
        actual.getCurrentLimits1().ifPresent(lim -> compareLoadingLimits(expected.getCurrentLimits1().orElse(null), lim));
        actual.getCurrentLimits2().ifPresent(lim -> compareLoadingLimits(expected.getCurrentLimits2().orElse(null), lim));
    }

    private void compareFlowBranchLimits(FlowsLimitsHolder expected, Line actual) {
        actual.getActivePowerLimits1().ifPresent(lim -> compareLoadingLimits(expected.getActivePowerLimits().orElse(null), lim));
        actual.getActivePowerLimits2().ifPresent(lim -> compareLoadingLimits(expected.getActivePowerLimits().orElse(null), lim));
        actual.getApparentPowerLimits1().ifPresent(lim -> compareLoadingLimits(expected.getApparentPowerLimits().orElse(null), lim));
        actual.getApparentPowerLimits2().ifPresent(lim -> compareLoadingLimits(expected.getApparentPowerLimits().orElse(null), lim));
        actual.getCurrentLimits1().ifPresent(lim -> compareLoadingLimits(expected.getCurrentLimits().orElse(null), lim));
        actual.getCurrentLimits2().ifPresent(lim -> compareLoadingLimits(expected.getCurrentLimits().orElse(null), lim));
    }

    private void compareFlowLimits(FlowsLimitsHolder expected, FlowsLimitsHolder actual) {
        actual.getActivePowerLimits().ifPresent(lim -> compareLoadingLimits(expected.getActivePowerLimits().orElse(null), lim));
        actual.getApparentPowerLimits().ifPresent(lim -> compareLoadingLimits(expected.getApparentPowerLimits().orElse(null), lim));
        actual.getCurrentLimits().ifPresent(lim -> compareLoadingLimits(expected.getCurrentLimits().orElse(null), lim));
    }

    private void compareLoadingLimits(LoadingLimits expected, LoadingLimits actual) {
        if (!actual.getTemporaryLimits().isEmpty()) {
            assertFalse(expected.getTemporaryLimits().isEmpty());
            for (LoadingLimits.TemporaryLimit temporaryLimit : actual.getTemporaryLimits()) {
                int acceptableDuration = temporaryLimit.getAcceptableDuration();
                assertEquals(expected.getTemporaryLimit(acceptableDuration).getValue(), temporaryLimit.getValue(), 0.0);
            }
        } else {
            assertTrue(expected.getTemporaryLimits().isEmpty());
        }
    }

    private Network prepareNetworkForEQComparison(Network network) {
        network.getAliases().forEach(network::removeAlias);
        network.getIdentifiables().forEach(identifiable -> identifiable.getAliases().forEach(identifiable::removeAlias));

        network.getVoltageLevels().forEach(vl ->
                vl.getBusView().getBuses().forEach(bus -> {
                    bus.setV(Double.NaN);
                    bus.setAngle(Double.NaN);
                })
        );
        network.getIdentifiables().forEach(identifiable -> {
            if (identifiable instanceof Bus) {
                // Nothing to do
            } else if (identifiable instanceof BusbarSection) {
                // Nothing to do
            } else if (identifiable instanceof ShuntCompensator) {
                ShuntCompensator shuntCompensator = (ShuntCompensator) identifiable;
                shuntCompensator.setVoltageRegulatorOn(false);
                shuntCompensator.setTargetV(Double.NaN);
                shuntCompensator.setTargetDeadband(Double.NaN);
                shuntCompensator.getTerminal().setQ(0.0);
                shuntCompensator.getTerminal().setP(0.0);
            } else if (identifiable instanceof Generator) {
                Generator generator = (Generator) identifiable;
                generator.setVoltageRegulatorOn(false);
                generator.setTargetV(Double.NaN);
                generator.getTerminal().setP(0.0).setQ(0.0);
            } else if (identifiable instanceof StaticVarCompensator) {
                StaticVarCompensator staticVarCompensator = (StaticVarCompensator) identifiable;
                staticVarCompensator.setRegulationMode(StaticVarCompensator.RegulationMode.OFF).setVoltageSetpoint(0.0);
                staticVarCompensator.getTerminal().setP(0.0).setQ(0.0);
            } else if (identifiable instanceof VscConverterStation) {
                VscConverterStation converter = (VscConverterStation) identifiable;
                converter.setVoltageRegulatorOn(false);
                converter.setLossFactor(0.8f);
                converter.setVoltageSetpoint(Double.NaN);
                converter.getTerminal().setP(0.0).setQ(0.0);
            } else if (identifiable instanceof LccConverterStation) {
                LccConverterStation converter = (LccConverterStation) identifiable;
                converter.setPowerFactor(0.8f);
                converter.getTerminal().setP(0.0).setQ(0.0);
            } else if (identifiable instanceof Injection) {
                Injection injection = (Injection) identifiable;
                injection.getTerminal().setP(0.0).setQ(0.0);
            } else if (identifiable instanceof HvdcLine) {
                HvdcLine hvdcLine = (HvdcLine) identifiable;
                hvdcLine.setActivePowerSetpoint(0.0);
                hvdcLine.setMaxP(0.0);
                hvdcLine.getConverterStation1().getTerminal().setP(0.0).setQ(0.0);
                hvdcLine.getConverterStation2().getTerminal().setP(0.0).setQ(0.0);
            } else if (identifiable instanceof Branch) {
                Branch branch = (Branch) identifiable;
                branch.getTerminal1().setP(0.0).setQ(0.0);
                branch.getTerminal2().setP(0.0).setQ(0.0);
            } else if (identifiable instanceof ThreeWindingsTransformer) {
                ThreeWindingsTransformer threeWindingsTransformer = (ThreeWindingsTransformer) identifiable;
                threeWindingsTransformer.getLeg1().getTerminal().setP(0.0).setQ(0.0);
                threeWindingsTransformer.getLeg2().getTerminal().setP(0.0).setQ(0.0);
                threeWindingsTransformer.getLeg3().getTerminal().setP(0.0).setQ(0.0);
            }
        });
        for (Load load : network.getLoads()) {
            load.setP0(0.0).setQ0(0.0);
        }
        for (Area area : network.getAreas()) {
            area.setInterchangeTarget(0.0);
        }

        network.removeExtension(CgmesModelExtension.class);
        network.removeExtension(CgmesMetadataModels.class);
        network.removeExtension(CimCharacteristics.class);

        return network;
    }

    private static Network allGeneratingUnitTypesNetwork() {
        Network network = NetworkFactory.findDefault().createNetwork("network", "test");
        Substation substation1 = network.newSubstation()
                .setId("substation1")
                .setCountry(Country.FR)
                .setTso("TSO1")
                .setGeographicalTags("region1")
                .add();
        VoltageLevel voltageLevel1 = substation1.newVoltageLevel()
                .setId("voltageLevel1")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        VoltageLevel.NodeBreakerView topology1 = voltageLevel1.getNodeBreakerView();
        topology1.newBusbarSection()
                .setId("voltageLevel1BusbarSection1")
                .setNode(0)
                .add();
        voltageLevel1.newGenerator()
                .setId("other")
                .setNode(1)
                .setMinP(0.0)
                .setMaxP(100.0)
                .setTargetP(25.0)
                .setTargetQ(10.0)
                .setVoltageRegulatorOn(false)
                .add();
        voltageLevel1.newGenerator()
                .setId("nuclear")
                .setNode(2)
                .setMinP(0.0)
                .setMaxP(100.0)
                .setTargetP(25.0)
                .setTargetQ(10.0)
                .setVoltageRegulatorOn(false)
                .setEnergySource(EnergySource.NUCLEAR)
                .add();
        voltageLevel1.newGenerator()
                .setId("thermal")
                .setNode(3)
                .setMinP(0.0)
                .setMaxP(100.0)
                .setTargetP(25.0)
                .setTargetQ(10.0)
                .setVoltageRegulatorOn(false)
                .setEnergySource(EnergySource.THERMAL)
                .add();
        voltageLevel1.newGenerator()
                .setId("hydro")
                .setNode(4)
                .setMinP(0.0)
                .setMaxP(100.0)
                .setTargetP(25.0)
                .setTargetQ(10.0)
                .setVoltageRegulatorOn(false)
                .setEnergySource(EnergySource.HYDRO)
                .add();
        voltageLevel1.newGenerator()
                .setId("solar")
                .setNode(5)
                .setMinP(0.0)
                .setMaxP(100.0)
                .setTargetP(25.0)
                .setTargetQ(10.0)
                .setVoltageRegulatorOn(false)
                .setEnergySource(EnergySource.SOLAR)
                .add();
        Generator windOnshore = voltageLevel1.newGenerator()
                .setId("wind_onshore")
                .setNode(6)
                .setMinP(0.0)
                .setMaxP(100.0)
                .setTargetP(25.0)
                .setTargetQ(10.0)
                .setVoltageRegulatorOn(false)
                .setEnergySource(EnergySource.WIND)
                .add();
        Generator windOffshore = voltageLevel1.newGenerator()
                .setId("wind_offshore")
                .setNode(7)
                .setMinP(0.0)
                .setMaxP(100.0)
                .setTargetP(25.0)
                .setTargetQ(10.0)
                .setVoltageRegulatorOn(false)
                .setEnergySource(EnergySource.WIND)
                .add();
        topology1.newInternalConnection().setNode1(0).setNode2(1).add();
        topology1.newInternalConnection().setNode1(0).setNode2(2).add();
        topology1.newInternalConnection().setNode1(0).setNode2(3).add();
        topology1.newInternalConnection().setNode1(0).setNode2(4).add();
        topology1.newInternalConnection().setNode1(0).setNode2(5).add();
        topology1.newInternalConnection().setNode1(0).setNode2(6).add();
        topology1.newInternalConnection().setNode1(0).setNode2(7).add();
        windOnshore.setProperty(Conversion.PROPERTY_WIND_GEN_UNIT_TYPE, "onshore");
        windOffshore.setProperty(Conversion.PROPERTY_WIND_GEN_UNIT_TYPE, "offshore");
        return network;
    }

    @Test
    void generatingUnitTypesTest() throws IOException {
        Network network = allGeneratingUnitTypesNetwork();

        // Export as cgmes
        String eqXml = writeCgmesProfile(network, "EQ", tmpDir);

        assertTrue(eqXml.contains("<cim:GeneratingUnit rdf:ID=\"_other_GU\">"));
        assertTrue(eqXml.contains("<cim:NuclearGeneratingUnit rdf:ID=\"_nuclear_NGU\">"));
        assertTrue(eqXml.contains("<cim:ThermalGeneratingUnit rdf:ID=\"_thermal_TGU\">"));
        assertTrue(eqXml.contains("<cim:HydroGeneratingUnit rdf:ID=\"_hydro_HGU\">"));
        assertTrue(eqXml.contains("<cim:SolarGeneratingUnit rdf:ID=\"_solar_SGU\">"));
        assertTrue(eqXml.contains("<cim:WindGeneratingUnit rdf:ID=\"_wind_onshore_WGU\">"));
        assertTrue(eqXml.contains("<cim:WindGeneratingUnit rdf:ID=\"_wind_offshore_WGU\">"));

        String sPattern = "<cim:WindGeneratingUnit rdf:ID=\"${rdfId}\">.*?" +
                "<cim:WindGeneratingUnit.windGenUnitType rdf:resource=\"http://iec.ch/TC57/2013/CIM-schema-cim16#WindGenUnitKind.(.*?)\"/>";

        Pattern onshorePattern = Pattern.compile(sPattern.replace("${rdfId}", "_wind_onshore_WGU"), Pattern.DOTALL);
        assertEquals("onshore", getFirstMatch(eqXml, onshorePattern));
        Pattern offshorePattern = Pattern.compile(sPattern.replace("${rdfId}", "_wind_offshore_WGU"), Pattern.DOTALL);
        assertEquals("offshore", getFirstMatch(eqXml, offshorePattern));
    }
}
