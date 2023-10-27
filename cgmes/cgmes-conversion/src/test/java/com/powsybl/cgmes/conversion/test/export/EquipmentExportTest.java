/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
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
import com.powsybl.commons.test.AbstractConverterTest;
import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.ThreeWindingsTransformer.Side;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import com.powsybl.iidm.xml.ExportOptions;
import com.powsybl.iidm.xml.NetworkXml;
import com.powsybl.iidm.xml.XMLImporter;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import com.powsybl.iidm.network.util.BranchData;
import com.powsybl.iidm.network.util.TwtData;

import org.apache.commons.lang3.tuple.Pair;
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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
class EquipmentExportTest extends AbstractConverterTest {

    @Test
    void smallGridHvdc() throws IOException, XMLStreamException {
        ReadOnlyDataSource dataSource = CgmesConformity1Catalog.smallNodeBreakerHvdc().dataSource();
        Network expected = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), null);
        Network actual = exportImportNodeBreaker(expected, dataSource);
        compareNetworksEQdata(expected, actual);
    }

    @Test
    void smallNodeBreaker() throws IOException, XMLStreamException {
        ReadOnlyDataSource dataSource = CgmesConformity1Catalog.smallNodeBreaker().dataSource();
        Network expected = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), null);
        Network actual = exportImportNodeBreaker(expected, dataSource);
        compareNetworksEQdata(expected, actual);
    }

    @Test
    void smallBusBranch() throws IOException, XMLStreamException {
        ReadOnlyDataSource dataSource = CgmesConformity1Catalog.smallBusBranch().dataSource();
        Network expected = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), null);
        Network actual = exportImportBusBranch(expected, dataSource);
        compareNetworksEQdata(expected, actual);
    }

    @Test
    void smallGridHvdcWithCapabilityCurve() throws IOException, XMLStreamException {
        ReadOnlyDataSource dataSource = CgmesConformity1ModifiedCatalog.smallNodeBreakerHvdcWithVsCapabilityCurve().dataSource();
        Network expected = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), null);
        Network actual = exportImportNodeBreaker(expected, dataSource);
        compareNetworksEQdata(expected, actual);
    }

    @Test
    void miniNodeBreaker() throws IOException, XMLStreamException {
        ReadOnlyDataSource dataSource = CgmesConformity1Catalog.miniNodeBreaker().dataSource();
        Network expected = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), null);
        Network actual = exportImportNodeBreaker(expected, dataSource);
        compareNetworksEQdata(expected, actual);
    }

    @Test
    void miniBusBranch() throws IOException, XMLStreamException {
        ReadOnlyDataSource dataSource = CgmesConformity1Catalog.miniBusBranch().dataSource();
        Network expected = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), null);
        Network actual = exportImportBusBranchNoBoundaries(expected, dataSource);
        compareNetworksEQdata(expected, actual);
    }

    @Test
    void microGridWithTieFlowMappedToEquivalentInjection() throws IOException, XMLStreamException {
        ReadOnlyDataSource dataSource = CgmesConformity1ModifiedCatalog.microGridBaseCaseBEWithTieFlowMappedToEquivalentInjection().dataSource();
        Network expected = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), null);
        Network actual = exportImportBusBranch(expected, dataSource);
        compareNetworksEQdata(expected, actual);
    }

    @Test
    void microGridBaseCaseAssembledSwitchAtBoundary() throws XMLStreamException, IOException {
        ReadOnlyDataSource dataSource = CgmesConformity1ModifiedCatalog.microGridBaseCaseAssembledSwitchAtBoundary().dataSource();
        Network network = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), null);

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
        CgmesControlArea actualCgmesControlArea = actual.getExtension(CgmesControlAreas.class).getCgmesControlArea("controlAreaId");
        boolean tieFlowsAtTieLinesAreSupported = false;
        if (tieFlowsAtTieLinesAreSupported) {
            assertEquals(1, actualCgmesControlArea.getBoundaries().size());
            assertEquals("7f43f508-2496-4b64-9146-0a40406cbe49", actualCgmesControlArea.getBoundaries().iterator().next().getDanglingLine().getId());
        } else {
            assertEquals(0, actualCgmesControlArea.getBoundaries().size());
        }
    }

    @Test
    void microGrid() throws IOException, XMLStreamException {
        ReadOnlyDataSource dataSource = CgmesConformity1Catalog.microGridType4BE().dataSource();
        Network expected = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), null);
        Network actual = exportImportBusBranch(expected, dataSource);
        compareNetworksEQdata(expected, actual);
    }

    @Test
    void microGridCreateEquivalentInjectionAliases() throws IOException, XMLStreamException {
        ReadOnlyDataSource dataSource = CgmesConformity1Catalog.microGridBaseCaseBE().dataSource();
        Network expected = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), null);
        // Remove aliases of equivalent injections, so they will have to be created during export
        for (DanglingLine danglingLine : expected.getDanglingLines(DanglingLineFilter.ALL)) {
            danglingLine.removeProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "EquivalentInjection");
            danglingLine.removeProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "EquivalentInjectionTerminal");
        }
        Network actual = exportImportBusBranch(expected, dataSource);
        compareNetworksEQdata(expected, actual);
    }

    @Test
    void nordic32() throws IOException, XMLStreamException {
        ReadOnlyDataSource dataSource = new ResourceDataSource("nordic32", new ResourceSet("/cim14", "nordic32.xiidm"));
        Network network = new XMLImporter().importData(dataSource, NetworkFactory.findDefault(), null);
        exportToCgmesEQ(network);
        exportToCgmesTP(network);
        // Import EQ & TP file, no additional information (boundaries) are required
        Network actual = new CgmesImport().importData(new FileDataSource(tmpDir, "exported"), NetworkFactory.findDefault(), null);

        // The xiidm file does not contain ratedS values, but during the cgmes export process default values
        // are exported for each transformer that are reading in the import process.
        // we reset the default imported ratedS values before comparing
        TwoWindingsTransformer twta = actual.getTwoWindingsTransformerStream().findFirst().orElseThrow();
        network.getTwoWindingsTransformers().forEach(twtn -> twtn.setRatedS(twta.getRatedS()));

        compareNetworksEQdata(network, actual);
    }

    @Test
    void nordic32SortTransformerEnds() throws IOException, XMLStreamException {
        ReadOnlyDataSource dataSource = new ResourceDataSource("nordic32", new ResourceSet("/cim14", "nordic32.xiidm"));
        Network network = new XMLImporter().importData(dataSource, NetworkFactory.findDefault(), null);
        exportToCgmesEQ(network, true);
        exportToCgmesTP(network);
        // Import EQ & TP file, no additional information (boundaries) are required
        Network actual = new CgmesImport().importData(new FileDataSource(tmpDir, "exported"), NetworkFactory.findDefault(), null);
        // Before comparing, interchange ends in twoWindingsTransformers that do not follow the high voltage at end1 rule
        prepareNetworkForSortedTransformerEndsComparison(network);

        // The xiidm file does not contain ratedS values, but during the cgmes export process default values
        // are exported for each transformer that are reading in the import process.
        // we reset the default imported ratedS values before comparing
        TwoWindingsTransformer twta = actual.getTwoWindingsTransformerStream().findFirst().orElseThrow();
        network.getTwoWindingsTransformers().forEach(twtn -> twtn.setRatedS(twta.getRatedS()));

        compareNetworksEQdata(network, actual);
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

        Network network = new CgmesImport().importData(ds, NetworkFactory.findDefault(), null);
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
        Network network1 = new CgmesImport().importData(new FileDataSource(tmpDir, "exportedEq"), NetworkFactory.findDefault(), null);
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
        Network network2 = new CgmesImport().importData(new FileDataSource(tmpDir, "exportedEq"), NetworkFactory.findDefault(), null);
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
        assertEquals(twtData.getComputedP(Side.ONE), twtDataSorted.getComputedP(Side.THREE), tol);
        assertEquals(twtData.getComputedQ(Side.ONE), twtDataSorted.getComputedQ(Side.THREE), tol);
        assertEquals(twtData.getComputedP(Side.TWO), twtDataSorted.getComputedP(Side.ONE), tol);
        assertEquals(twtData.getComputedQ(Side.TWO), twtDataSorted.getComputedQ(Side.ONE), tol);
        assertEquals(twtData.getComputedP(Side.THREE), twtDataSorted.getComputedP(Side.TWO), tol);
        assertEquals(twtData.getComputedQ(Side.THREE), twtDataSorted.getComputedQ(Side.TWO), tol);
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
        Network expected = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), null);
        Network actual = exportImportBusBranch(expected, dataSource);
        compareNetworksEQdata(expected, actual);
    }

    @Test
    void microGridCgmesExportPreservingOriginalClassesOfLoads() throws IOException, XMLStreamException {
        ReadOnlyDataSource dataSource = Cgmes3ModifiedCatalog.microGridBaseCaseAllTypesOfLoads().dataSource();
        Properties properties = new Properties();
        properties.setProperty("iidm.import.cgmes.convert-boundary", "true");
        Network expected = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), properties);
        Network actual = exportImportNodeBreaker(expected, dataSource);

        assertEquals(loadsCreatedFromOriginalClassCount(expected, CgmesNames.ASYNCHRONOUS_MACHINE), loadsCreatedFromOriginalClassCount(actual, CgmesNames.ASYNCHRONOUS_MACHINE));
        assertEquals(loadsCreatedFromOriginalClassCount(expected, CgmesNames.ENERGY_SOURCE), loadsCreatedFromOriginalClassCount(actual, CgmesNames.ENERGY_SOURCE));
        assertEquals(loadsCreatedFromOriginalClassCount(expected, CgmesNames.SV_INJECTION), loadsCreatedFromOriginalClassCount(actual, CgmesNames.SV_INJECTION));
        assertEquals(loadsCreatedFromOriginalClassCount(expected, CgmesNames.ENERGY_CONSUMER), loadsCreatedFromOriginalClassCount(actual, CgmesNames.ENERGY_CONSUMER));
        assertEquals(loadsCreatedFromOriginalClassCount(expected, CgmesNames.CONFORM_LOAD), loadsCreatedFromOriginalClassCount(actual, CgmesNames.CONFORM_LOAD));
        assertEquals(loadsCreatedFromOriginalClassCount(expected, CgmesNames.NONCONFORM_LOAD), loadsCreatedFromOriginalClassCount(actual, CgmesNames.NONCONFORM_LOAD));
        assertEquals(loadsCreatedFromOriginalClassCount(expected, CgmesNames.STATION_SUPPLY), loadsCreatedFromOriginalClassCount(actual, CgmesNames.STATION_SUPPLY));

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
        compareNetworksEQdata(expected, actual, knownDiffs);
    }

    private static long loadsCreatedFromOriginalClassCount(Network network, String originalClass) {
        return network.getLoadStream().filter(load -> loadOriginalClass(load, originalClass)).count();
    }

    private static boolean loadOriginalClass(Load load, String originalClass) {
        String cgmesClass = load.getProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS);
        return cgmesClass != null && cgmesClass.equals(originalClass);
    }

    @Test
    void equivalentShuntTest() throws IOException {
        ReadOnlyDataSource ds = CgmesConformity1ModifiedCatalog.microGridBaseCaseBEEquivalentShunt().dataSource();
        Network network = new CgmesImport().importData(ds, NetworkFactory.findDefault(), null);

        // Export as cgmes
        Path outputPath = tmpDir.resolve("temp.cgmesExport");
        Files.createDirectories(outputPath);
        String baseName = "microGridEquivalentShunt";
        new CgmesExport().export(network, new Properties(), new FileDataSource(outputPath, baseName));

        // re-import after adding the original boundary files
        copyBoundary(outputPath, baseName, ds);
        Network actual = new CgmesImport().importData(new FileDataSource(outputPath, baseName), NetworkFactory.findDefault(), new Properties());

        ShuntCompensator expectedEquivalentShunt = network.getShuntCompensator("d771118f-36e9-4115-a128-cc3d9ce3e3da");
        ShuntCompensator actualEquivalentShunt = actual.getShuntCompensator("d771118f-36e9-4115-a128-cc3d9ce3e3da");
        assertTrue(equivalentShuntsAreEqual(expectedEquivalentShunt, actualEquivalentShunt));
    }

    @Test
    void equivalentShuntWithZeroSectionCountTest() throws IOException {
        ReadOnlyDataSource ds = CgmesConformity1ModifiedCatalog.microGridBaseCaseBEEquivalentShunt().dataSource();
        Network network = new CgmesImport().importData(ds, NetworkFactory.findDefault(), null);
        ShuntCompensator equivalentShunt = network.getShuntCompensator("d771118f-36e9-4115-a128-cc3d9ce3e3da");
        equivalentShunt.setSectionCount(0);

        // Export as cgmes
        Path outputPath = tmpDir.resolve("temp.cgmesExport");
        Files.createDirectories(outputPath);
        String baseName = "microGridEquivalentShunt";
        new CgmesExport().export(network, new Properties(), new FileDataSource(outputPath, baseName));

        // re-import after adding the original boundary files
        copyBoundary(outputPath, baseName, ds);
        Network actual = new CgmesImport().importData(new FileDataSource(outputPath, baseName), NetworkFactory.findDefault(), new Properties());

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
        Network network = new CgmesImport().importData(ds, NetworkFactory.findDefault(), new Properties());

        TwoWindingsTransformer twtNetwork = network.getTwoWindingsTransformer("e8a7eaec-51d6-4571-b3d9-c36d52073c33");
        twtNetwork.getPhaseTapChanger().setRegulationMode(PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL)
                .setRegulationValue(75.24)
                .setTargetDeadband(2.0)
                .setRegulationTerminal(twtNetwork.getTerminal1());

        // Export as cgmes
        Path outputPath = tmpDir.resolve("temp.cgmesExport");
        Files.createDirectories(outputPath);
        String baseName = "microGridTapChangerDefineControl";
        new CgmesExport().export(network, new Properties(), new FileDataSource(outputPath, baseName));

        // re-import after adding the original boundary files
        copyBoundary(outputPath, baseName, ds);
        Network actual = new CgmesImport().importData(new FileDataSource(outputPath, baseName), NetworkFactory.findDefault(), new Properties());
        TwoWindingsTransformer twtActual = actual.getTwoWindingsTransformer("e8a7eaec-51d6-4571-b3d9-c36d52073c33");

        assertEquals(twtNetwork.getPhaseTapChanger().getRegulationMode().name(), twtActual.getPhaseTapChanger().getRegulationMode().name());
        assertEquals(twtNetwork.getPhaseTapChanger().getRegulationValue(), twtActual.getPhaseTapChanger().getRegulationValue());
        assertEquals(twtNetwork.getPhaseTapChanger().getTargetDeadband(), twtActual.getPhaseTapChanger().getTargetDeadband());
    }

    @Test
    void tapChangerControlDefineRatioTapChangerAndPhaseTapChangerTest() throws IOException {
        ReadOnlyDataSource ds = Cgmes3Catalog.miniGrid().dataSource();
        Network network = new CgmesImport().importData(ds, NetworkFactory.findDefault(), new Properties());

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
        new CgmesExport().export(network, new Properties(), new FileDataSource(outputPath, baseName));

        // re-import after adding the original boundary files
        copyBoundary(outputPath, baseName, ds);
        Network actual = new CgmesImport().importData(new FileDataSource(outputPath, baseName), NetworkFactory.findDefault(), new Properties());
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
        Network network = new CgmesImport().importData(ds, NetworkFactory.findDefault(), new Properties());

        ThreeWindingsTransformer twtNetwork = network.getThreeWindingsTransformer("84ed55f4-61f5-4d9d-8755-bba7b877a246");
        addRatioTapChangerAndPhaseTapChanger(twtNetwork.getLeg1());

        // Export as cgmes
        Path outputPath = tmpDir.resolve("temp.cgmesExport");
        Files.createDirectories(outputPath);
        String baseName = "microGridTapChangerDefineRatioTapChangerAndPhaseTapChangerLeg1";
        new CgmesExport().export(network, new Properties(), new FileDataSource(outputPath, baseName));

        // re-import after adding the original boundary files
        copyBoundary(outputPath, baseName, ds);
        Network actual = new CgmesImport().importData(new FileDataSource(outputPath, baseName), NetworkFactory.findDefault(), new Properties());
        ThreeWindingsTransformer twtActual = actual.getThreeWindingsTransformer("84ed55f4-61f5-4d9d-8755-bba7b877a246");

        checkLeg(twtNetwork.getLeg1(), twtActual.getLeg1());
    }

    @Test
    void tapChangerControlDefineRatioTapChangerAndPhaseTapChangerT3wLeg2Test() throws IOException {
        ReadOnlyDataSource ds = Cgmes3Catalog.miniGrid().dataSource();
        Network network = new CgmesImport().importData(ds, NetworkFactory.findDefault(), new Properties());
        ThreeWindingsTransformer twtNetwork = network.getThreeWindingsTransformer("411b5401-0a43-404a-acb4-05c3d7d0c95c");
        addRatioTapChangerAndPhaseTapChanger(twtNetwork.getLeg2());

        // Export as cgmes
        Path outputPath = tmpDir.resolve("temp.cgmesExport");
        Files.createDirectories(outputPath);
        String baseName = "microGridTapChangerDefineRatioTapChangerAndPhaseTapChangerLeg2";
        new CgmesExport().export(network, new Properties(), new FileDataSource(outputPath, baseName));

        // re-import after adding the original boundary files
        copyBoundary(outputPath, baseName, ds);
        Network actual = new CgmesImport().importData(new FileDataSource(outputPath, baseName), NetworkFactory.findDefault(), new Properties());
        ThreeWindingsTransformer twtActual = actual.getThreeWindingsTransformer("411b5401-0a43-404a-acb4-05c3d7d0c95c");

        checkLeg(twtNetwork.getLeg2(), twtActual.getLeg2());
    }

    @Test
    void tapChangerControlDefineRatioTapChangerAndPhaseTapChangerT3wLeg3Test() throws IOException {
        ReadOnlyDataSource ds = Cgmes3Catalog.miniGrid().dataSource();
        Network network = new CgmesImport().importData(ds, NetworkFactory.findDefault(), new Properties());
        ThreeWindingsTransformer twtNetwork = network.getThreeWindingsTransformer("411b5401-0a43-404a-acb4-05c3d7d0c95c");
        addRatioTapChangerAndPhaseTapChanger(twtNetwork.getLeg3());

        // Export as cgmes
        Path outputPath = tmpDir.resolve("temp.cgmesExport");
        Files.createDirectories(outputPath);
        String baseName = "microGridTapChangerDefineRatioTapChangerAndPhaseTapChangerLeg3";
        new CgmesExport().export(network, new Properties(), new FileDataSource(outputPath, baseName));

        // re-import after adding the original boundary files
        copyBoundary(outputPath, baseName, ds);
        Network actual = new CgmesImport().importData(new FileDataSource(outputPath, baseName), NetworkFactory.findDefault(), new Properties());
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
        return Network.read(repackaged, LocalComputationManager.getDefault(), ImportConfig.load(), null);
    }

    private Path exportToCgmesEQ(Network network) throws IOException, XMLStreamException {
        return exportToCgmesEQ(network, false);
    }

    private Path exportToCgmesEQ(Network network, boolean transformersWithHighestVoltageAtEnd1) throws IOException, XMLStreamException {
        // Export CGMES EQ file
        Path exportedEq = tmpDir.resolve("exportedEq.xml");
        try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(exportedEq))) {
            XMLStreamWriter writer = XmlUtil.initializeWriter(true, "    ", os);
            CgmesExportContext context = new CgmesExportContext(network).setExportEquipment(true).setExportTransformersWithHighestVoltageAtEnd1(transformersWithHighestVoltageAtEnd1);
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

    private void compareNetworksEQdata(Network expected, Network actual) throws IOException {
        DifferenceEvaluator knownDiffs = DifferenceEvaluators.chain(
                DifferenceEvaluators.Default,
                ExportXmlCompare::numericDifferenceEvaluator,
                ExportXmlCompare::ignoringNonEQ);
        compareNetworksEQdata(expected, actual, knownDiffs);
    }

    private void compareNetworksEQdata(Network expected, Network actual, DifferenceEvaluator knownDiffs) throws IOException {
        Network expectedNetwork = prepareNetworkForEQComparison(expected);
        Network actualNetwork = prepareNetworkForEQComparison(actual);

        // Export original and only EQ
        ExportOptions exportOptions = new ExportOptions();
        exportOptions.setExtensions(Collections.emptySet());
        exportOptions.setSorted(true);
        NetworkXml.writeAndValidate(expectedNetwork, exportOptions, tmpDir.resolve("expected.xml"));
        NetworkXml.writeAndValidate(actualNetwork, exportOptions, tmpDir.resolve("actual.xml"));

        // Compare
        ExportXmlCompare.compareEQNetworks(tmpDir.resolve("expected.xml"), tmpDir.resolve("actual.xml"), knownDiffs);

        compareTemporaryLimits(Network.read(tmpDir.resolve("expected.xml")), Network.read(tmpDir.resolve("actual.xml")));
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

        network.removeExtension(CgmesModelExtension.class);
        network.removeExtension(CgmesSshMetadata.class);
        network.removeExtension(CgmesSvMetadata.class);
        network.removeExtension(CimCharacteristics.class);

        return network;
    }
}
