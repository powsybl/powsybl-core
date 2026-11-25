/**
 * Copyright (c) 2026, SuperGrid Institute (https://www.supergrid-institute.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security;

import com.powsybl.contingency.violations.LimitViolation;
import com.powsybl.contingency.violations.LimitViolationType;
import com.powsybl.contingency.violations.LoadingLimitType;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.limitmodification.LimitsComputer;
import com.powsybl.iidm.network.test.DcDetailedNetworkFactory;
import com.powsybl.security.limitscaling.SimpleLimitsComputer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.*;

import static com.powsybl.security.DetailedDcEquipmentLimitViolationTestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Detailed DC limit violation detection through the static entry points of {@link LimitViolationDetection}.
 *
 * The fixtures come from {@link DcDetailedNetworkFactory}. In these networks, no AC value is set, so no AC violation
 * can be reported unless a test writes one.
 *
 * @author Landry Huet {@literal <landry.huet at supergrid-institute.com>}
 */
class LimitViolationDetectionDetailedDcTest {

    private static final Set<LoadingLimitType> ALL_LIMIT_TYPES = EnumSet.allOf(LoadingLimitType.class);
    private static final LimitsComputer<Identifiable<?>, LoadingLimits> NO_SCALING = new LimitsComputer.NoModificationsImpl();
    private static final double DC_POWER_FACTOR = 0.95;

    private static final String DC_NODE_FR_POS = DcDetailedNetworkFactory.DC_NODE_FR_POS;
    private static final String DC_NODE_FR_NEG = DcDetailedNetworkFactory.DC_NODE_FR_NEG;
    private static final String DC_LINE_POS = "dcLinePos";

    private Network network;
    private List<LimitViolation> violations;

    @BeforeEach
    void setUp() {
        network = DcDetailedNetworkFactory.createVscSymmetricalMonopole();
        violations = new ArrayList<>();
    }

    private List<LimitViolation> detect(Network n) {
        LimitViolationDetection.checkAllDetailedDcEquipment(n, ALL_LIMIT_TYPES, violations::add);
        return violations;
    }

    /**
     * Checks the content that every detailed DC violation must have, plus the fields given as arguments.
     */
    private static void assertDetailedDcViolation(LimitViolation violation, String subjectId, LimitViolationType type,
                                                  double limit, double value) {
        assertEquals(subjectId, violation.getSubjectId());
        assertEquals(type, violation.getLimitType());
        assertEquals(limit, violation.getLimit(), 0.0);
        assertEquals(value, violation.getValue(), 0.0);
        assertEquals(1.0, violation.getLimitScaling(), 0.0);
        assertNull(violation.getSide());
        assertNull(violation.getOperationalLimitsGroupId());
        assertNull(violation.getLimitName());
        assertEquals(Optional.empty(), violation.getViolationLocation());
        assertEquals(Integer.MAX_VALUE, violation.getAcceptableDuration());
    }

    private static LimitViolation single(List<LimitViolation> violations) {
        assertEquals(1, violations.size(), () -> "Expected exactly one violation, got " + subjects(violations));
        return violations.getFirst();
    }

    private static void assertNoViolation(List<LimitViolation> violations) {
        assertTrue(violations.isEmpty(), () -> "Expected no violation, got " + subjects(violations));
    }

    // ================================================================================================================
    // Level 1: detection scenarios, on checkAllDetailedDc
    // ================================================================================================================

    @Test
    void noViolationWhenNoSolvedValueIsWritten() {
        // limits defined everywhere, but V and I are left NaN
        network.getDcNodeStream().forEach(n -> n.setLowVoltageLimit(475.).setHighVoltageLimit(525.));
        network.getDcLineStream().forEach(l -> l.setPermanentCurrentLimit(1000.));

        List<LimitViolation> detected = detect(network);
        assertNoViolation(detected);
    }

    @Test
    void violationCarriesTheOptionalNameOfTheSubject() {
        setUpAcAndDetailedDcEquipmentViolations(network);
        network.getDcNode(DC_NODE_FR_POS).setName("FR positive pole");
        network.getDcLine(DC_LINE_POS).setName("Positive pole cable");

        Map<String, String> names = new HashMap<>();
        detect(network).forEach(v -> names.put(v.getSubjectId(), v.getSubjectName()));

        assertEquals("FR positive pole", names.get(DC_NODE_FR_POS));
        assertEquals("Positive pole cable", names.get(DC_LINE_POS));
    }

    // ---------------------------------------------------------------------------------------------------------------
    // DC node voltage
    // ---------------------------------------------------------------------------------------------------------------

    @ParameterizedTest(name = "{0}: limits [{1}, {2}], V = {3}")
    @CsvSource({
        // node,        low,  high, V,    expected type, expected limit
        "dcNodeFrPos,   475., 525., 525.,  HIGH_VOLTAGE, 525.",
        "dcNodeFrPos,   475., 525., 530.,  HIGH_VOLTAGE, 525.",
        // |V| is checked, the signed value is reported
        "dcNodeFrNeg,   475., 525., -530., HIGH_VOLTAGE, 525.",
        "dcNodeFrPos,   475., 525., 475.,  LOW_VOLTAGE,  475.",
        "dcNodeFrNeg,   475., 525., -450., LOW_VOLTAGE,  475.",
        // A zero low limit is valid and is reached by a zero voltage
        "dcNodeFrPos,   0.,   ,     0.,    LOW_VOLTAGE,  0.",
    })
    void dcNodeVoltageViolationIsReported(String nodeId, Double low, Double high, double v,
                                          LimitViolationType expectedType, double expectedLimit) {
        DcNode node = network.getDcNode(nodeId).setV(v);
        Optional.ofNullable(low).ifPresent(node::setLowVoltageLimit);
        Optional.ofNullable(high).ifPresent(node::setHighVoltageLimit);

        assertDetailedDcViolation(single(detect(network)), nodeId, expectedType, expectedLimit, v);
    }

    @ParameterizedTest(name = "limits [{0}, {1}], V = {2}")
    @CsvSource({
        // low, high, V
        // Within limits, both signs
        "475., 525., 475.01",
        "475., 525., 500.",
        "475., 525., 524.99",
        "475., 525., -475.01",
        "475., 525., -500.",
        "475., 525., -524.99",
        // Undefined limits
        ",     ,     10000.",
        ",     ,     0.",
        // Only one limit defined
        ",     525., 0.",
        "475., ,     10000.",
        // undefined voltage
        "475., 525., NaN",
    })
    void dcNodeVoltageViolationIsNotReported(Double low, Double high, double v) {
        DcNode node = network.getDcNode(DC_NODE_FR_POS).setV(v);
        Optional.ofNullable(low).ifPresent(node::setLowVoltageLimit);
        Optional.ofNullable(high).ifPresent(node::setHighVoltageLimit);

        assertNoViolation(assertDoesNotThrow(() -> detect(network)));
    }

    @Test
    void dcNodesConnectedByClosedDcSwitchAreReportedSeparately() {
        // no merging of DC nodes, even when a closed DC switch connects them
        Network switchNetwork = DcDetailedNetworkFactory.createSimple2NodesDcSwitch();
        switchNetwork.getDcNode("dcNode1").setHighVoltageLimit(550.).setV(600.);
        switchNetwork.getDcNode("dcNode2").setHighVoltageLimit(550.).setV(600.);

        List<LimitViolation> result = detect(switchNetwork);

        assertEquals(List.of("dcNode1/HIGH_VOLTAGE", "dcNode2/HIGH_VOLTAGE"), subjects(result));
        result.forEach(v -> assertDetailedDcViolation(v, v.getSubjectId(), LimitViolationType.HIGH_VOLTAGE, 550., 600.));
    }

    @Test
    void severalDcNodesInViolationAreAllReported() {
        network.getDcNode(DC_NODE_FR_POS).setHighVoltageLimit(525.).setV(530.);
        network.getDcNode(DC_NODE_FR_NEG).setLowVoltageLimit(475.).setV(-400.);

        assertEquals(List.of(DC_NODE_FR_NEG + "/LOW_VOLTAGE", DC_NODE_FR_POS + "/HIGH_VOLTAGE"), subjects(detect(network)));
    }

    // ---------------------------------------------------------------------------------------------------------------
    // DC line current
    // ---------------------------------------------------------------------------------------------------------------

    @ParameterizedTest(name = "I1 = {0}, I2 = {1}")
    @CsvSource({
        // I1,    I2
        "1000.,  -1000.",
        // The signed value is reported
        "-1200., 1200.",
        // both terminals exceed the limit, a single violation without side
        "1500.,  -1500.",
    })
    void dcLineCurrentViolationIsReported(double i1, double i2) {
        // Somewhat contrived: in principle there should be the same current magnitude at both
        // terminals. Let's check this is well defined nonetheless.
        DcLine line = network.getDcLine(DC_LINE_POS).setPermanentCurrentLimit(1000.);
        line.getDcTerminal1().setI(i1);
        line.getDcTerminal2().setI(i2);

        assertDetailedDcViolation(single(detect(network)), DC_LINE_POS, LimitViolationType.CURRENT, 1000., i1);
    }

    @ParameterizedTest(name = "limit {0}, I1 = {1}, I2 = {2}")
    @CsvSource({
        // limit, I1,      I2
        "1000.,  0.,      0.",
        "1000.,  999.99,  -999.99",
        "1000.,  -999.99, 999.99",
        // I1 is the checked value, a value at terminal 2 alone does not produce a violation
        "1000.,  100.,    1500.",
        // Undefined limit
        ",       100000., -100000.",
    })
    void dcLineCurrentViolationIsNotReported(Double limit, double i1, double i2) {
        DcLine line = network.getDcLine(DC_LINE_POS);
        Optional.ofNullable(limit).ifPresent(line::setPermanentCurrentLimit);
        line.getDcTerminal1().setI(i1);
        line.getDcTerminal2().setI(i2);

        assertNoViolation(detect(network));
    }

    @Test
    void dcLineWithNaNCurrentIsNotReportedNorApproximated() {
        // I1 is NaN. Neither the current at terminal 2 nor the DC power at terminal 1 is used instead.
        DcLine line = network.getDcLine(DC_LINE_POS).setPermanentCurrentLimit(1.);
        line.getDcTerminal1().setI(Double.NaN).setP(1000.);
        line.getDcTerminal2().setI(1500.).setP(-1000.);

        assertNoViolation(assertDoesNotThrow(() -> detect(network)));
    }

    // ================================================================================================================
    // Level 2: wiring of the static entry points
    // ================================================================================================================

    @Test
    void entryPointsAreConsistentOnDetailedDcEquipmentViolations() {
        setUpAcAndDetailedDcEquipmentViolations(network);
        List<LimitViolation> detailedDc = new ArrayList<>();
        List<LimitViolation> all = new ArrayList<>();
        List<LimitViolation> allDc = new ArrayList<>();

        LimitViolationDetection.checkAllDetailedDcEquipment(network, ALL_LIMIT_TYPES, detailedDc::add);
        LimitViolationDetection.checkAll(network, ALL_LIMIT_TYPES, NO_SCALING, all::add);
        LimitViolationDetection.checkAllDc(network, DC_POWER_FACTOR, ALL_LIMIT_TYPES, NO_SCALING, allDc::add);

        // anchor: consistency alone would not catch the three entry points missing the same violations
        assertEquals(DETAILED_DC_SUBJECTS, subjects(detailedDc));
        // only checkAll reports the AC voltage violation (AC voltages are not checked in DC load flow mode);
        // every entry point reports the same detailed DC violations, in the same consumer as the AC ones
        List<String> expectedWithAcVoltage = new ArrayList<>(DETAILED_DC_SUBJECTS);
        expectedWithAcVoltage.add(AC_SUBJECT + "/" + LimitViolationType.HIGH_VOLTAGE);
        Collections.sort(expectedWithAcVoltage);
        assertAll(
                () -> assertEquals(expectedWithAcVoltage, subjects(all), "checkAll"),
                () -> assertEquals(DETAILED_DC_SUBJECTS, subjects(allDc), "checkAllDc"),
                () -> assertEquals(describeFully(detailedDc), describeFully(withoutAcSubject(all)), "checkAll"),
                () -> assertEquals(describeFully(detailedDc), describeFully(allDc), "checkAllDc")
        );
    }

    private static List<LimitViolation> withoutAcSubject(List<LimitViolation> violations) {
        return violations.stream().filter(v -> !v.getSubjectId().equals(AC_SUBJECT)).toList();
    }

    // ================================================================================================================
    // Behaviors specific to the static entry points
    // ================================================================================================================

    @Test
    void checkAllDcDoesNotDependOnTheDcPowerFactorForDetailedDcEquipment() {
        setUpAcAndDetailedDcEquipmentViolations(network);
        List<LimitViolation> withPowerFactor1 = new ArrayList<>();
        LimitViolationDetection.checkAllDc(network, 1.0, ALL_LIMIT_TYPES, NO_SCALING, withPowerFactor1::add);

        LimitViolationDetection.checkAllDc(network, 0.5, ALL_LIMIT_TYPES, NO_SCALING, violations::add);

        assertEquals(describeFully(withPowerFactor1), describeFully(violations));
        assertEquals(DETAILED_DC_SUBJECTS, subjects(violations));
    }

    @Test
    void dcLineIsNotCheckedWithoutPermanentLimitType() {
        // only TATL is checked: no DC line violation, the other detailed DC checks are not affected
        setUpAcAndDetailedDcEquipmentViolations(network);
        List<LimitViolation> detailedDc = new ArrayList<>();
        List<LimitViolation> all = new ArrayList<>();

        LimitViolationDetection.checkAllDetailedDcEquipment(network, EnumSet.of(LoadingLimitType.TATL), detailedDc::add);
        LimitViolationDetection.checkAll(network, EnumSet.of(LoadingLimitType.TATL), NO_SCALING, all::add);

        assertAll(
                () -> assertEquals(List.of("dcNodeFrPos/HIGH_VOLTAGE"), subjects(detailedDc), "checkAllDetailedDc"),
                () -> assertEquals(List.of(AC_SUBJECT + "/HIGH_VOLTAGE", "dcNodeFrPos/HIGH_VOLTAGE"), subjects(all), "checkAll")
        );
    }

    @Test
    void limitsComputerIsIgnoredForDetailedDcEquipment() {
        setUpAcAndDetailedDcEquipmentViolations(network);
        network.getBusBreakerView().getBus("BUS-FR").setV(400.);
        // with a 0.5 scaling, these values would all be violations if limits were scaled
        network.getDcNode(DcDetailedNetworkFactory.DC_NODE_GB_POS).setHighVoltageLimit(525.).setV(300.);
        network.getDcLine("dcLineNeg").setPermanentCurrentLimit(1000.).getDcTerminal1().setI(600.);

        LimitsComputer<Identifiable<?>, LoadingLimits> halfLimits = new SimpleLimitsComputer(0.5);
        LimitViolationDetection.checkAll(network, ALL_LIMIT_TYPES, halfLimits, violations::add);
        List<LimitViolation> allDc = new ArrayList<>();
        LimitViolationDetection.checkAllDc(network, DC_POWER_FACTOR, ALL_LIMIT_TYPES, halfLimits, allDc::add);

        assertEquals(DETAILED_DC_SUBJECTS, subjects(violations), "checkAll");
        assertEquals(DETAILED_DC_SUBJECTS, subjects(allDc), "checkAllDc");
        violations.forEach(v -> assertEquals(1.0, v.getLimitScaling(), 0.0, v.getSubjectId()));
        allDc.forEach(v -> assertEquals(1.0, v.getLimitScaling(), 0.0, v.getSubjectId()));
        LimitViolation lineViolation = violations.stream().filter(v -> v.getSubjectId().equals(DC_LINE_POS)).findFirst().orElseThrow();
        assertEquals(1000., lineViolation.getLimit(), 0.0);
    }

    @Test
    void detailedDcEquipmentOfSubnetworksIsChecked() {
        // the DC equipment of the fixture belongs to the "VscSymmetricalMonopole" subnetwork
        setUpAcAndDetailedDcEquipmentViolations(network);
        Network dcSubnetwork = network.getSubnetwork("VscSymmetricalMonopole");
        assertNotNull(dcSubnetwork.getDcNode(DC_NODE_FR_POS));

        LimitViolationDetection.checkAll(network, ALL_LIMIT_TYPES, NO_SCALING, violations::add);
        List<LimitViolation> fromSubnetwork = new ArrayList<>();
        LimitViolationDetection.checkAllDetailedDcEquipment(dcSubnetwork, ALL_LIMIT_TYPES, fromSubnetwork::add);

        assertTrue(subjects(violations).containsAll(DETAILED_DC_SUBJECTS), () -> subjects(violations).toString());
        assertEquals(DETAILED_DC_SUBJECTS, subjects(fromSubnetwork));
    }
}
