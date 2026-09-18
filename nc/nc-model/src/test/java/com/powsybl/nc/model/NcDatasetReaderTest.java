/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nc.model;

import com.powsybl.commons.datasource.ReadOnlyMemDataSource;
import com.powsybl.commons.report.PowsyblCoreReportResourceBundle;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.nc.model.assessedelement.NcAssessedElement;
import com.powsybl.nc.model.contingency.NcContingency;
import com.powsybl.nc.model.io.NcDatasetReader;
import com.powsybl.nc.model.remedialaction.NcPropertyReference;
import com.powsybl.nc.model.remedialaction.NcRelativeDirectionKind;
import com.powsybl.nc.model.remedialaction.NcRotatingMachineAction;
import com.powsybl.nc.model.remedialaction.NcShuntCompensatorModification;
import com.powsybl.nc.model.remedialaction.NcStaticPropertyRange;
import com.powsybl.nc.model.remedialaction.NcTopologyAction;
import com.powsybl.nc.model.remedialaction.NcValueOffsetKind;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Roman Vykuka {@literal <vykuka at gmail.com>}
 */
class NcDatasetReaderTest {

    private static final OffsetDateTime PROFILE_TIMESTAMP = OffsetDateTime.parse("2024-01-31T12:00:00Z");
    private final List<NcDataset> datasets = new ArrayList<>();

    @AfterEach
    void closeDatasets() {
        datasets.forEach(NcDataset::close);
    }

    @Test
    void readsContingencyProfile() {
        NcDataset dataset = track(NcDatasetReader.read(profileDataSource("/profiles/contingency",
            "RTE_CO.xml", "RTE_SSI.xml")));
        NcModel baseline = dataset.getModel();
        NcModel timestampModel = dataset.forTimestamp(PROFILE_TIMESTAMP);

        Set<NcContingency> contingencies = baseline.getContingencies();
        assertEquals(12, contingencies.size());
        assertEquals(16, baseline.getContingencyEquipments().size());

        Map<String, NcContingency> baselineByName = contingencies.stream()
            .filter(contingency -> contingency.name() != null)
            .collect(Collectors.toMap(NcContingency::name, Function.identity()));
        Map<String, NcContingency> timestampByName = timestampModel.getContingencies().stream()
            .filter(contingency -> contingency.name() != null)
            .collect(Collectors.toMap(NcContingency::name, Function.identity()));

        assertTrue(baselineByName.get("CO1").mustStudy());
        assertFalse(baselineByName.get("CO9").mustStudy());
        assertFalse(timestampByName.get("CO1").mustStudy());
        assertTrue(timestampByName.get("CO9").mustStudy());
    }

    @Test
    void ignoresProfilesNotApplicableToTimestamp() {
        NcDataset dataset = track(NcDatasetReader.read(profileDataSource("/profiles/contingency",
            "RTE_CO.xml", "RTE_SSI.xml")));

        // The CO profile declares a validity interval starting in 2023.
        NcModel outOfRange = dataset.forTimestamp(OffsetDateTime.parse("1999-01-01T00:00:00Z"));
        assertTrue(outOfRange.getProfileMetadata().isEmpty());
        assertTrue(outOfRange.getContingencies().isEmpty());
        assertTrue(outOfRange.getContingencyEquipments().isEmpty());

        // A timestamp inside the interval still resolves the same data as the baseline.
        NcModel inRange = dataset.forTimestamp(PROFILE_TIMESTAMP);
        assertEquals(12, inRange.getContingencies().size());
        assertEquals(12, dataset.getModel().getContingencies().size());
    }

    @Test
    void readsKeywordBoundToAnyDcatPrefix() {
        ReadOnlyMemDataSource dataSource = new ReadOnlyMemDataSource("nc-profiles");
        String profile = """
            <?xml version="1.0" encoding="UTF-8"?>
            <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" \
            xmlns:catalog="http://www.w3.org/ns/dcat#" \
            xmlns:dcterms="http://purl.org/dc/terms/#" \
            xmlns:md="http://iec.ch/TC57/61970-552/ModelDescription/1#" \
            xmlns:cim="http://iec.ch/TC57/CIM100#" xmlns:nc="http://entsoe.eu/ns/nc#">
              <md:FullModel rdf:about="urn:uuid:00000000-0000-0000-0000-000000000001">
                <catalog:keyword>CO</catalog:keyword>
                <dcterms:Model.conformsTo>http://entsoe.eu/ns/CIM/Contingency-EU/2.4</dcterms:Model.conformsTo>
              </md:FullModel>
              <nc:OrdinaryContingency rdf:ID="_contingency-x">
                <cim:IdentifiedObject.mRID>contingency-x</cim:IdentifiedObject.mRID>
                <cim:IdentifiedObject.name>COX</cim:IdentifiedObject.name>
                <nc:Contingency.normalMustStudy>true</nc:Contingency.normalMustStudy>
              </nc:OrdinaryContingency>
            </rdf:RDF>
            """;
        dataSource.putData("profile.xml", profile.getBytes(StandardCharsets.UTF_8));

        NcDataset dataset = track(NcDatasetReader.read(dataSource));
        assertEquals(1, dataset.getModel().getContingencies().size());
        assertEquals(NcKeyword.CONTINGENCY,
            dataset.getModel().getProfileMetadata().values().iterator().next().keyword());
    }

    @Test
    void readsWithExplicitTripleStoreImplementationParameter() {
        Properties parameters = new Properties();
        parameters.setProperty(NcDatasetReader.TRIPLESTORE_IMPLEMENTATION, "rdf4j");

        NcDataset dataset = track(NcDatasetReader.read(
            profileDataSource("/profiles/contingency", "RTE_CO.xml"), parameters, ReportNode.NO_OP));

        assertEquals(12, dataset.getModel().getContingencies().size());
    }

    @Test
    void reportsProfilesInTheReportNode() throws IOException {
        ReportNode reportNode = ReportNode.newRootReportNode()
            .withResourceBundles(PowsyblCoreReportResourceBundle.BASE_NAME)
            .withMessageTemplate("core.nc.model.readingNcProfiles")
            .withUntypedValue("dataSource", "nc-profiles")
            .build();

        track(NcDatasetReader.read(profileDataSource("/profiles/contingency", "RTE_CO.xml"), reportNode));

        StringWriter writer = new StringWriter();
        reportNode.print(writer);
        String report = writer.toString();
        assertTrue(report.contains("NC profile RTE_CO.xml (CO)"), report);
    }

    @Test
    void rejectsXmlWithoutNcProfileKeyword() {
        ReadOnlyMemDataSource dataSource = new ReadOnlyMemDataSource("invalid-nc-profile");
        dataSource.putData("profile.xml", "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"/>"
            .getBytes(java.nio.charset.StandardCharsets.UTF_8));

        NcException exception = assertThrows(NcException.class, () -> NcDatasetReader.read(dataSource));
        assertEquals("Missing NC profile keyword in profile.xml", exception.getMessage());
    }

    @Test
    void rejectsKeywordOutsideFullModelHeader() {
        ReadOnlyMemDataSource dataSource = new ReadOnlyMemDataSource("invalid-nc-profile");
        String profile = """
            <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                     xmlns:md="http://iec.ch/TC57/61970-552/ModelDescription/1#"
                     xmlns:dcat="http://www.w3.org/ns/dcat#">
              <md:FullModel rdf:about="urn:uuid:00000000-0000-0000-0000-000000000001"/>
              <rdf:Description rdf:about="urn:uuid:00000000-0000-0000-0000-000000000002">
                <dcat:keyword>CO</dcat:keyword>
              </rdf:Description>
            </rdf:RDF>
            """;
        dataSource.putData("profile.xml", profile.getBytes(StandardCharsets.UTF_8));

        NcException exception = assertThrows(NcException.class, () -> NcDatasetReader.read(dataSource));
        assertEquals("Missing NC profile keyword in profile.xml", exception.getMessage());
    }

    @Test
    void rejectsUnsupportedProfileVersion() {
        ReadOnlyMemDataSource dataSource = new ReadOnlyMemDataSource("invalid-nc-profile");
        String profile = """
            <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                     xmlns:md="http://iec.ch/TC57/61970-552/ModelDescription/1#"
                     xmlns:dcat="http://www.w3.org/ns/dcat#"
                     xmlns:dcterms="http://purl.org/dc/terms/#">
              <md:FullModel rdf:about="urn:uuid:00000000-0000-0000-0000-000000000001">
                <dcat:keyword>CO</dcat:keyword>
                <dcterms:Model.conformsTo>http://entsoe.eu/ns/CIM/Contingency-EU/9.9</dcterms:Model.conformsTo>
              </md:FullModel>
            </rdf:RDF>
            """;
        dataSource.putData("profile.xml", profile.getBytes(StandardCharsets.UTF_8));

        NcException exception = assertThrows(NcException.class, () -> NcDatasetReader.read(dataSource));
        assertTrue(exception.getMessage().contains("declares unsupported version 9.9"));
    }

    @Test
    void readsAssessedElementProfile() {
        NcDataset dataset = track(NcDatasetReader.read(profileDataSource("/profiles/assessed-element",
            "RTE_AE.xml", "RTE_SSI.xml")));
        NcModel model = dataset.getModel();
        Set<NcAssessedElement> assessedElements = model.getAssessedElements();
        assertEquals(14, assessedElements.size());
        assertEquals(6, model.getAssessedElementWithContingencies().size());
        assertEquals(1, model.getAssessedElementWithRemedialActions().size());
        Map<String, NcAssessedElement> assessedElementsByName = assessedElements.stream()
            .collect(Collectors.toMap(NcAssessedElement::name, Function.identity()));
        NcAssessedElement ae1 = assessedElementsByName.get("AE1");
        assertEquals("assessed-element-1", ae1.mrid());
        assertTrue(ae1.inBaseCase());
        assertTrue(ae1.isCombinableWithContingency());
        assertTrue(ae1.enabled());
        assertEquals("http://energy.referencedata.eu/EIC/10XFR-RTE------Q", ae1.operator());
        assertNull(ae1.conductingEquipment());
        assertNotNull(ae1.operationalLimit());
        assertEquals(0d, ae1.flowReliabilityMargin());

        NcAssessedElement ae8 = assessedElementsByName.get("AE8");
        assertFalse(ae8.enabled());
        assertNotNull(ae8.conductingEquipment());
        assertNull(ae8.operationalLimit());

        NcModel timestampModel = dataset.forTimestamp(PROFILE_TIMESTAMP);
        Map<String, NcAssessedElement> timestampByName = timestampModel.getAssessedElements().stream()
            .collect(Collectors.toMap(NcAssessedElement::name, Function.identity()));
        assertFalse(timestampByName.get("AE1").enabled());
        assertTrue(timestampByName.get("AE8").enabled());
        assertTrue(findByMrid(model.getAssessedElementWithContingencies(), "ae1xco1").enabled());
        assertFalse(findByMrid(timestampModel.getAssessedElementWithContingencies(), "ae1xco1").enabled());
        assertTrue(findByMrid(model.getAssessedElementWithRemedialActions(), "ae1xra1").enabled());
        assertFalse(findByMrid(timestampModel.getAssessedElementWithRemedialActions(), "ae1xra1").enabled());
    }

    @Test
    void readsRemedialActionsProfile() {
        NcDataset dataset = track(NcDatasetReader.read(profileDataSource("/profiles/remedial-action",
            "RTE_RA.xml", "RTE_SSI.xml")));
        NcModel model = dataset.getModel();

        Set<NcTopologyAction> topologyActions = model.getTopologyActions();
        Set<NcRotatingMachineAction> rotatingMachineActions = model.getRotatingMachineActions();
        Set<NcShuntCompensatorModification> shuntModifications = model.getShuntCompensatorModifications();
        Set<NcStaticPropertyRange> ranges = model.getStaticPropertyRanges();

        assertEquals(20, model.getGridStateAlterationRemedialActions().size());

        assertEquals(12, topologyActions.size());
        assertTrue(topologyActions.stream().allMatch(action -> action.propertyReference() == NcPropertyReference.SWITCH_OPEN));

        assertEquals(1, rotatingMachineActions.size());
        assertSame(NcPropertyReference.ROTATING_MACHINE_P, rotatingMachineActions.iterator().next().propertyReference());

        assertEquals(1, shuntModifications.size());
        assertSame(NcPropertyReference.SHUNT_COMPENSATOR_SECTIONS, shuntModifications.iterator().next().propertyReference());

        assertEquals(8, model.getTapPositionActions().size());
        assertTrue(model.getTapPositionActions().stream()
            .allMatch(action -> action.propertyReference() == NcPropertyReference.TAP_CHANGER_STEP));

        assertEquals(15, ranges.size());
        assertTrue(ranges.stream().allMatch(range -> range.propertyReference() != NcPropertyReference.UNKNOWN));
        assertTrue(ranges.stream().allMatch(range -> range.valueKind() != NcValueOffsetKind.UNKNOWN));
        assertTrue(ranges.stream().allMatch(range -> range.direction() != NcRelativeDirectionKind.UNKNOWN));
        assertEquals(11, model.getContingencyWithRemedialActions().size());
        assertEquals(11, model.getRemedialActionGroups().size());
        assertEquals(22, model.getRemedialActionDependencies().size());
        NcModel timestampModel = dataset.forTimestamp(PROFILE_TIMESTAMP);
        assertTrue(findByMrid(model.getGridStateAlterationRemedialActions(), "remedial-action-1").available());
        assertFalse(findByMrid(timestampModel.getGridStateAlterationRemedialActions(), "remedial-action-1").available());
        assertTrue(findByMrid(model.getTopologyActions(), "topology-action-1").enabled());
        assertFalse(findByMrid(timestampModel.getTopologyActions(), "topology-action-1").enabled());
        assertTrue(findByMrid(model.getTapPositionActions(), "tap-position-action-1").enabled());
        assertFalse(findByMrid(timestampModel.getTapPositionActions(), "tap-position-action-1").enabled());
        assertTrue(findByMrid(model.getRotatingMachineActions(), "rotating-machine-action-1").enabled());
        assertFalse(findByMrid(timestampModel.getRotatingMachineActions(), "rotating-machine-action-1").enabled());
        assertTrue(findByMrid(model.getShuntCompensatorModifications(), "shunt-compensator-modification-1").enabled());
        assertFalse(findByMrid(timestampModel.getShuntCompensatorModifications(), "shunt-compensator-modification-1").enabled());
        assertEquals(2d, findByMrid(model.getStaticPropertyRanges(), "static-property-range-for-tap-position-action-1").value());
        assertEquals(3d, findByMrid(timestampModel.getStaticPropertyRanges(), "static-property-range-for-tap-position-action-1").value());
        assertTrue(findByMrid(model.getContingencyWithRemedialActions(), "co1xra3").enabled());
        assertFalse(findByMrid(timestampModel.getContingencyWithRemedialActions(), "co1xra3").enabled());
        String dependencyMrid = "1bdfc408-5ff3-4968-a96f-75494908b7e7";
        assertTrue(findByMrid(model.getRemedialActionDependencies(), dependencyMrid).enabled());
        assertFalse(findByMrid(timestampModel.getRemedialActionDependencies(), dependencyMrid).enabled());
    }

    private NcDataset track(NcDataset dataset) {
        datasets.add(dataset);
        return dataset;
    }

    private static <T extends NcObject> T findByMrid(Set<T> objects, String mrid) {
        return objects.stream()
            .filter(object -> mrid.equals(object.mrid()))
            .findFirst()
            .orElseThrow();
    }

    private static ReadOnlyMemDataSource profileDataSource(String resourceDirectory, String... fileNames) {
        ReadOnlyMemDataSource dataSource = new ReadOnlyMemDataSource("nc-profiles");
        for (String fileName : fileNames) {
            var input = NcDatasetReaderTest.class.getResourceAsStream(resourceDirectory + "/" + fileName);
            assertNotNull(input);
            dataSource.putData(fileName, input);
        }
        return dataSource;
    }

}
