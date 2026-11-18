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

import static com.powsybl.security.DetailedDcLimitViolationTestUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    private static final String VSC_FR = "VscFr";
    private static final String VSC_GB = "VscGb";
    private static final String LCC_FR = "LccFr";

    private Network network;
    private List<LimitViolation> violations;

    @BeforeEach
    void setUp() {
        network = DcDetailedNetworkFactory.createVscSymmetricalMonopole();
        violations = new ArrayList<>();
    }

    private List<LimitViolation> detect(Network n) {
        LimitViolationDetection.checkAllDetailedDc(n, ALL_LIMIT_TYPES, violations::add);
        return violations;
    }

    /**
     * Checks the content that every detailed DC violation must have, plus the fields given as arguments.
     *
     * @param side expected side, {@code null} for a DC node voltage violation
     */
    private static void assertDetailedDcViolation(LimitViolation violation, String subjectId, LimitViolationType type,
                                                  double limit, double value, ThreeSides side) {
        assertEquals(subjectId, violation.getSubjectId());
        assertEquals(type, violation.getLimitType());
        assertEquals(limit, violation.getLimit(), 0.0);
        assertEquals(value, violation.getValue(), 0.0);
        assertEquals(1.0, violation.getLimitScaling(), 0.0);
        assertEquals(side, violation.getSide());
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
        // limits defined everywhere, but V, I and P are left NaN
        network.getDcNodeStream().forEach(n -> n.setLowVoltageLimit(475.).setHighVoltageLimit(525.));
        network.getDcLineStream().forEach(l -> l.setPermanentCurrentLimit(1000.));
        network.getVoltageSourceConverterStream().forEach(c -> c.setMinP(10.).setMaxP(200.));

        List<LimitViolation> detected = detect(network);
        assertNoViolation(detected);
    }

    @Test
    void violationCarriesTheOptionalNameOfTheSubject() {
        setUpAcAndDetailedDcViolations(network);
        network.getDcNode(DC_NODE_FR_POS).setName("FR positive pole");
        network.getDcLine(DC_LINE_POS).setName("Positive pole cable");

        Map<String, String> names = new HashMap<>();
        detect(network).forEach(v -> names.put(v.getSubjectId(), v.getSubjectName()));

        assertEquals("FR positive pole", names.get(DC_NODE_FR_POS));
        assertEquals("Positive pole cable", names.get(DC_LINE_POS));
        assertTrue(names.containsKey(VSC_FR));
        assertNull(names.get(VSC_FR));
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

        assertDetailedDcViolation(single(detect(network)), nodeId, expectedType, expectedLimit, v, null);
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
        result.forEach(v -> assertDetailedDcViolation(v, v.getSubjectId(), LimitViolationType.HIGH_VOLTAGE, 550., 600., null));
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
        // both terminals exceed the limit, a single violation on side ONE
        "1500.,  -1500.",
    })
    void dcLineCurrentViolationIsReported(double i1, double i2) {
        // Somewhat contrived: in principle there should be the same current at both
        // terminals. Let's check this is well defined nonetheless.
        DcLine line = network.getDcLine(DC_LINE_POS).setPermanentCurrentLimit(1000.);
        line.getDcTerminal1().setI(i1);
        line.getDcTerminal2().setI(i2);

        assertDetailedDcViolation(single(detect(network)), DC_LINE_POS, LimitViolationType.CURRENT, 1000., i1, ThreeSides.ONE);
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

    // ---------------------------------------------------------------------------------------------------------------
    // AC/DC converter active power
    // ---------------------------------------------------------------------------------------------------------------

    private VoltageSourceConverter vscFr() {
        VoltageSourceConverter vsc = network.getVoltageSourceConverter(VSC_FR);
        // Precondition of the PCC tests: the PCC terminal is the 400 kV transformer terminal, not the converter one
        assertNotSame(vsc.getTerminal1(), vsc.getPccTerminal());
        return vsc;
    }

    private VoltageSourceConverter setUpVscFr(Double minP, Double maxP, double pccP, Double ac1P) {
        VoltageSourceConverter vsc = vscFr();
        Optional.ofNullable(minP).ifPresent(vsc::setMinP);
        Optional.ofNullable(maxP).ifPresent(vsc::setMaxP);
        vsc.getPccTerminal().setP(pccP);
        Optional.ofNullable(ac1P).ifPresent(p -> vsc.getTerminal1().setP(p));
        return vsc;
    }

    @ParameterizedTest(name = "bounds [{0}, {1}], P at PCC = {2}, P at AC terminal = {3}")
    @CsvSource({
        // minP,  maxP, P at PCC, P at AC terminal 1, expected limit
        "-200.,  200., 200.,     ,                   200.",
        "-200.,  200., 250.,     ,                   200.",
        // The signed value is reported
        "-200.,  200., -250.,    ,                   -200.",
        "50.,    200., 50.,      ,                   50.",
        // P = 0 with minP > 0 is a violation
        "10.,    200., 0.,       ,                   10.",
        // the PCC value is checked, even if the AC terminal value is within bounds
        "-200.,  200., 250.,     150.,               200.",
    })
    void converterActivePowerViolationIsReported(Double minP, Double maxP, double pccP, Double ac1P, double expectedLimit) {
        setUpVscFr(minP, maxP, pccP, ac1P);

        assertDetailedDcViolation(single(detect(network)), VSC_FR, LimitViolationType.ACTIVE_POWER, expectedLimit, pccP, ThreeSides.ONE);
    }

    @ParameterizedTest(name = "bounds [{0}, {1}], P at PCC = {2}, P at AC terminal = {3}")
    @CsvSource({
        // minP,  maxP, P at PCC,                 P at AC terminal 1
        "-200.,  200., -199.99,                  ",
        "-200.,  200., 0.,                       ",
        "-200.,  200., 199.99,                   ",
        // the PCC value is within bounds, the AC terminal value is not
        "-200.,  200., 150.,                     250.",
        // bounds left at their default values never produce a violation
        ",       ,     1e9,                      ",
        ",       ,     -1e9,                     ",
        // Only one bound defined
        ",       200., -1e9,                     ",
        "-200.,  ,     1e9,                      ",
        // NaN at the PCC terminal, even if the AC terminal holds an out-of-bounds value
        "10.,    200., NaN,                      1000.",
    })
    void converterActivePowerViolationIsNotReported(Double minP, Double maxP, double pccP, Double ac1P) {
        setUpVscFr(minP, maxP, pccP, ac1P);

        assertNoViolation(assertDoesNotThrow(() -> detect(network)));
    }

    @Test
    void everyConverterIsChecked() {
        setUpVscFr(null, 200., 250., null);
        network.getVoltageSourceConverter(VSC_GB).setMinP(-200.).getPccTerminal().setP(-250.);

        assertEquals(List.of(VSC_FR + "/ACTIVE_POWER", VSC_GB + "/ACTIVE_POWER"), subjects(detect(network)));
    }

    @ParameterizedTest(name = "P at PCC = {0}, P1 = {1}, P2 = {2}")
    @CsvSource({
        // P at PCC, P1,   P2,   expected limit
        "300.,      100., 100., 250.",
        // The sum of the AC terminals would exceed maxP, but the PCC terminal is set and takes precedence
        "200.,      200., 200., ",
    })
    void lineCommutatedConverterIsCheckedAtPcc(double pccP, double p1, double p2, Double expectedLimit) {
        // LccFr has two AC terminals and its PCC terminal is on an AC line
        Network lccNetwork = DcDetailedNetworkFactory.createLccMonopoleGroundReturn();
        LineCommutatedConverter lcc = lccNetwork.getLineCommutatedConverter(LCC_FR).setMinP(-250.).setMaxP(250.);
        lcc.getPccTerminal().setP(pccP);
        lcc.getTerminal1().setP(p1);
        lcc.getTerminal2().orElseThrow().setP(p2);

        List<LimitViolation> result = detect(lccNetwork);

        if (expectedLimit == null) {
            assertNoViolation(result);
        } else {
            assertDetailedDcViolation(single(result), LCC_FR, LimitViolationType.ACTIVE_POWER, expectedLimit, pccP, ThreeSides.ONE);
        }
    }

    // ---------------------------------------------------------------------------------------------------------------
    // AC/DC converter without PCC terminal
    // The reference implementation always sets a PCC terminal, so another Network implementation is mocked.
    // The mocked network exposes the converters through their typed streams and iterables, and nothing else.
    // ---------------------------------------------------------------------------------------------------------------

    private static Terminal terminalWithP(double p) {
        Terminal terminal = mock(Terminal.class);
        when(terminal.getP()).thenReturn(p);
        return terminal;
    }

    private static <C extends AcDcConverter<C>> C mockConverterWithoutPcc(Class<C> type, String id, double minP, double maxP,
                                                                         Terminal terminal1, Terminal terminal2) {
        C converter = mock(type);
        when(converter.getId()).thenReturn(id);
        when(converter.getOptionalName()).thenReturn(Optional.empty());
        when(converter.getNameOrId()).thenReturn(id);
        when(converter.getMinP()).thenReturn(minP);
        when(converter.getMaxP()).thenReturn(maxP);
        when(converter.getPccTerminal()).thenReturn(null);
        when(converter.getTerminal1()).thenReturn(terminal1);
        when(converter.getTerminal(TerminalNumber.ONE)).thenReturn(Optional.of(terminal1));
        when(converter.getTerminal2()).thenReturn(Optional.ofNullable(terminal2));
        when(converter.getTerminal(TerminalNumber.TWO)).thenReturn(Optional.ofNullable(terminal2));
        doReturn(terminal2 == null ? List.of(terminal1) : List.of(terminal1, terminal2))
                .when(converter).getTerminals();
        return converter;
    }

    private static Network mockNetworkWith(List<VoltageSourceConverter> vscs, List<LineCommutatedConverter> lccs) {
        Network mockedNetwork = mock(Network.class);
        when(mockedNetwork.getId()).thenReturn("mockedNetwork");
        when(mockedNetwork.getVoltageSourceConverterStream()).thenAnswer(inv -> vscs.stream());
        when(mockedNetwork.getVoltageSourceConverters()).thenAnswer(inv -> vscs);
        when(mockedNetwork.getVoltageSourceConverterCount()).thenReturn(vscs.size());
        when(mockedNetwork.getLineCommutatedConverterStream()).thenAnswer(inv -> lccs.stream());
        when(mockedNetwork.getLineCommutatedConverters()).thenAnswer(inv -> lccs);
        when(mockedNetwork.getLineCommutatedConverterCount()).thenReturn(lccs.size());
        vscs.forEach(c -> when(c.getNetwork()).thenReturn(mockedNetwork));
        lccs.forEach(c -> when(c.getNetwork()).thenReturn(mockedNetwork));
        return mockedNetwork;
    }

    @ParameterizedTest(name = "bounds [{2}, {3}], P1 = {0}, P2 = {1}")
    @CsvSource({
        // P1,   P2,   minP,  maxP, expected limit
        // A single AC terminal (VSC): P = P1
        "120.,  ,     -100., 100., 100.",
        "NaN,   ,     10.,   100., ",
        // Two AC terminals (LCC): P = P1 + P2. Neither terminal exceeds the bound on its own, the sum does.
        "60.,   50.,  -100., 100., 100.",
        "-60.,  -50., -100., 100., -100.",
        // 60 - 50 = 10: within bounds, although terminal 1 alone would not be
        "60.,   -50., -50.,  50.,  ",
    })
    void converterWithoutPccIsCheckedOnItsAcTerminals(double p1, Double p2, double minP, double maxP, Double expectedLimit) {
        Network mockedNetwork;
        String id;
        if (p2 == null) {
            id = "vscNoPcc";
            mockedNetwork = mockNetworkWith(List.of(mockConverterWithoutPcc(VoltageSourceConverter.class, id, minP, maxP,
                    terminalWithP(p1), null)), List.of());
        } else {
            id = "lccNoPcc";
            mockedNetwork = mockNetworkWith(List.of(), List.of(mockConverterWithoutPcc(LineCommutatedConverter.class, id, minP, maxP,
                    terminalWithP(p1), terminalWithP(p2))));
        }

        List<LimitViolation> result = assertDoesNotThrow(() -> detect(mockedNetwork));

        if (expectedLimit == null) {
            assertNoViolation(result);
        } else {
            double expectedP = p1 + (p2 == null ? 0. : p2);
            assertDetailedDcViolation(single(result), id, LimitViolationType.ACTIVE_POWER, expectedLimit, expectedP, ThreeSides.ONE);
        }
    }

    // ================================================================================================================
    // Level 2: wiring of the static entry points
    // ================================================================================================================

    @Test
    void entryPointsAreConsistentOnDetailedDcViolations() {
        setUpAcAndDetailedDcViolations(network);
        List<LimitViolation> detailedDc = new ArrayList<>();
        List<LimitViolation> all = new ArrayList<>();
        List<LimitViolation> allDc = new ArrayList<>();

        LimitViolationDetection.checkAllDetailedDc(network, ALL_LIMIT_TYPES, detailedDc::add);
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
        setUpAcAndDetailedDcViolations(network);
        List<LimitViolation> withPowerFactor1 = new ArrayList<>();
        LimitViolationDetection.checkAllDc(network, 1.0, ALL_LIMIT_TYPES, NO_SCALING, withPowerFactor1::add);

        LimitViolationDetection.checkAllDc(network, 0.5, ALL_LIMIT_TYPES, NO_SCALING, violations::add);

        assertEquals(describeFully(withPowerFactor1), describeFully(violations));
        assertEquals(DETAILED_DC_SUBJECTS, subjects(violations));
    }

    @Test
    void dcLineIsNotCheckedWithoutPermanentLimitType() {
        // only TATL is checked: no DC line violation, the other detailed DC checks are not affected
        setUpAcAndDetailedDcViolations(network);
        List<LimitViolation> detailedDc = new ArrayList<>();
        List<LimitViolation> all = new ArrayList<>();

        LimitViolationDetection.checkAllDetailedDc(network, EnumSet.of(LoadingLimitType.TATL), detailedDc::add);
        LimitViolationDetection.checkAll(network, EnumSet.of(LoadingLimitType.TATL), NO_SCALING, all::add);

        assertAll(
                () -> assertEquals(List.of("VscFr/ACTIVE_POWER", "dcNodeFrPos/HIGH_VOLTAGE"), subjects(detailedDc), "checkAllDetailedDc"),
                () -> assertEquals(List.of(AC_SUBJECT + "/HIGH_VOLTAGE", "VscFr/ACTIVE_POWER", "dcNodeFrPos/HIGH_VOLTAGE"), subjects(all), "checkAll")
        );
    }

    @Test
    void limitsComputerIsIgnoredForDetailedDcEquipment() {
        setUpAcAndDetailedDcViolations(network);
        network.getBusBreakerView().getBus("BUS-FR").setV(400.);
        // with a 0.5 scaling, these values would all be violations if limits were scaled
        network.getDcNode(DcDetailedNetworkFactory.DC_NODE_GB_POS).setHighVoltageLimit(525.).setV(300.);
        network.getDcLine("dcLineNeg").setPermanentCurrentLimit(1000.).getDcTerminal1().setI(600.);
        network.getVoltageSourceConverter(VSC_GB).setMaxP(200.).getPccTerminal().setP(150.);

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
        setUpAcAndDetailedDcViolations(network);
        Network dcSubnetwork = network.getSubnetwork("VscSymmetricalMonopole");
        assertNotNull(dcSubnetwork.getDcNode(DC_NODE_FR_POS));

        LimitViolationDetection.checkAll(network, ALL_LIMIT_TYPES, NO_SCALING, violations::add);
        List<LimitViolation> fromSubnetwork = new ArrayList<>();
        LimitViolationDetection.checkAllDetailedDc(dcSubnetwork, ALL_LIMIT_TYPES, fromSubnetwork::add);

        assertTrue(subjects(violations).containsAll(DETAILED_DC_SUBJECTS), () -> subjects(violations).toString());
        assertEquals(DETAILED_DC_SUBJECTS, subjects(fromSubnetwork));
    }
}
