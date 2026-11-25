/**
 * Copyright (c) 2016-2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security;

import com.powsybl.commons.io.table.CsvTableFormatterFactory;
import com.powsybl.commons.io.table.TableFormatterConfig;
import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.ContingencyContext;
import com.powsybl.contingency.strategy.ConditionalActions;
import com.powsybl.contingency.strategy.OperatorStrategy;
import com.powsybl.contingency.strategy.condition.AtLeastOneViolationCondition;
import com.powsybl.contingency.violations.LimitViolation;
import com.powsybl.contingency.violations.LimitViolationFilter;
import com.powsybl.contingency.violations.LimitViolationHelper;
import com.powsybl.contingency.violations.LimitViolationType;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.DcDetailedNetworkFactory;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import com.powsybl.loadflow.LoadFlowResult;
import com.powsybl.security.limitscaling.SimpleLimitsComputer;
import com.powsybl.security.results.ConnectivityResult;
import com.powsybl.security.results.NetworkResult;
import com.powsybl.security.results.OperatorStrategyResult;
import com.powsybl.security.results.PostContingencyResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.StringWriter;
import java.util.*;

import static com.powsybl.security.DetailedDcEquipmentLimitViolationTestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class SecurityTest {

    TableFormatterConfig formatterConfig;

    private final CsvTableFormatterFactory formatterFactory = new CsvTableFormatterFactory();

    private SecurityAnalysisResult result;
    private LimitViolation line1Violation;
    private LimitViolation line2Violation;
    private Network network;

    @BeforeEach
    void setUp() {
        formatterConfig = new TableFormatterConfig(Locale.US, ',', "inv", true, true);

        network = EurostagTutorialExample1Factory.createWithFixedCurrentLimits();

        // create pre-contingency results, just one violation on line1
        line1Violation = LimitViolation.builder()
            .subject("NHV1_NHV2_1")
            .type(LimitViolationType.CURRENT)
            .limitName(LoadingLimits.DEFAULT_PERMANENT_LIMIT_NAME)
            .limit(1000.0)
            .scaling(0.95f)
            .value(1100.0)
            .side(TwoSides.ONE)
            .build();
        LimitViolationsResult preContingencyResult = new LimitViolationsResult(Collections.singletonList(line1Violation), Collections.singletonList("action1"));

        // create post-contingency results, still the line1 violation plus line2 violation
        Contingency contingency1 = Mockito.mock(Contingency.class);
        Mockito.when(contingency1.getId()).thenReturn("contingency1");
        line2Violation = LimitViolation.builder()
            .subject("NHV1_NHV2_2")
            .type(LimitViolationType.CURRENT)
            .limitName("permanent2")
            .limit(900.0)
            .scaling(0.95f)
            .value(950.0)
            .side(TwoSides.ONE)
            .build();
        PostContingencyResult postContingencyResult = new PostContingencyResult(contingency1,
            PostContingencyComputationStatus.CONVERGED,
            new LimitViolationsResult(Arrays.asList(line1Violation, line2Violation), Collections.singletonList("action2")),
            NetworkResult.empty(), ConnectivityResult.empty(), Double.NaN, Collections.emptyList());

        List<OperatorStrategyResult> operatorStrategyResults = new ArrayList<>();
        OperatorStrategyResult opStrategyResult1 = new OperatorStrategyResult(
            new OperatorStrategy("strategy1", ContingencyContext.specificContingency(contingency1.getId()),
                List.of(new ConditionalActions("stage1", new AtLeastOneViolationCondition(Collections.singletonList("violationId1")), Collections.singletonList("actionId1")))),
            List.of(new OperatorStrategyResult.ConditionalActionsResult("strategy1", PostContingencyComputationStatus.CONVERGED,
                    new LimitViolationsResult(Arrays.asList(line1Violation, line2Violation), Collections.singletonList("action2")),
                    NetworkResult.empty(),
                    3.45)
            )
        );
        operatorStrategyResults.add(opStrategyResult1);

        result = new SecurityAnalysisResult(preContingencyResult,
                LoadFlowResult.ComponentResult.Status.CONVERGED,
                Collections.singletonList(postContingencyResult),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                operatorStrategyResults
        );
    }

    @Test
    void printPreContingencyViolations() throws Exception {
        StringWriter writer = new StringWriter();
        try {
            Security.printPreContingencyViolations(result, network, writer, formatterFactory, formatterConfig, null);
        } finally {
            writer.close();
        }
        assertEquals(String.join(System.lineSeparator(),
                                 "Pre-contingency violations",
                                 "Action,Equipment (1),End,Country,Base voltage,Violation type,Violation name,Value,Limit,abs(value-limit),Loading rate %",
                                 "action1,,,,,,,,,,",
                                 ",NHV1_NHV2_1,VLHV1,FR,380,CURRENT,permanent,1100.0000,950.0000,150.0000,110.00"),
                     writer.toString().trim());
    }

    @Test
    void printPostContingencyViolations() throws Exception {
        StringWriter writer = new StringWriter();
        try {
            Security.printPostContingencyViolations(result, network, writer, formatterFactory, formatterConfig, null, false);
        } finally {
            writer.close();
        }
        assertEquals(String.join(System.lineSeparator(),
                                 "Post-contingency limit violations",
                                 "Contingency,Status,Action,Equipment (2),End,Country,Base voltage,Violation type,Violation name,Value,Limit,abs(value-limit),Loading rate %",
                                 "contingency1,CONVERGED,,Equipment (2),,,,,,,,,",
                                 ",,action2,,,,,,,,,,",
                                 ",,,NHV1_NHV2_1,VLHV1,FR,380,CURRENT,permanent,1100.0000,950.0000,150.0000,110.00",
                                 ",,,NHV1_NHV2_2,VLHV1,FR,380,CURRENT,permanent2,950.0000,855.0000,95.0000,105.56"),
                     writer.toString().trim());
    }

    @Test
    void printPostContingencyViolationsWithPreContingencyViolationsFiltering() throws Exception {
        StringWriter writer = new StringWriter();
        try {
            Security.printPostContingencyViolations(result, network, writer, formatterFactory, formatterConfig, null, true);
        } finally {
            writer.close();
        }
        assertEquals(String.join(System.lineSeparator(),
                                 "Post-contingency limit violations",
                                 "Contingency,Status,Action,Equipment (1),End,Country,Base voltage,Violation type,Violation name,Value,Limit,abs(value-limit),Loading rate %",
                                 "contingency1,CONVERGED,,Equipment (1),,,,,,,,,",
                                 ",,action2,,,,,,,,,,",
                                 ",,,NHV1_NHV2_2,VLHV1,FR,380,CURRENT,permanent2,950.0000,855.0000,95.0000,105.56"),
                     writer.toString().trim());
    }

    @Test
    void printOperatorStrategyViolations() throws Exception {
        StringWriter writer = new StringWriter();
        try (writer) {
            Security.printOperatorStrategyViolations(result, network, writer, formatterFactory, formatterConfig, null, false);
        } finally {
            writer.close();
        }
        assertEquals(String.join(System.lineSeparator(),
                        "Operator strategy limit violations",
                        "Operator strategy,Status,Contingency,Equipment (2),End,Country,Base voltage,Violation type,Violation name,Value,Limit,abs(value-limit),Loading rate %",
                        "strategy1,CONVERGED,contingency1,Equipment (2),,,,,,,,,",
                        ",,action2,,,,,,,,,,",
                        ",,,NHV1_NHV2_1,VLHV1,FR,380,CURRENT,permanent,1100.0000,950.0000,150.0000,110.00",
                        ",,,NHV1_NHV2_2,VLHV1,FR,380,CURRENT,permanent2,950.0000,855.0000,95.0000,105.56"),
                writer.toString().trim());
    }

    @Test
    void printOperatorStrategyViolationsWithPreContingencyViolationsFiltering() throws Exception {
        StringWriter writer = new StringWriter();
        try (writer) {
            Security.printOperatorStrategyViolations(result, network, writer, formatterFactory, formatterConfig, null, true);
        } finally {
            writer.close();
        }
        assertEquals(String.join(System.lineSeparator(),
                        "Operator strategy limit violations",
                        "Operator strategy,Status,Contingency,Equipment (1),End,Country,Base voltage,Violation type,Violation name,Value,Limit,abs(value-limit),Loading rate %",
                        "strategy1,CONVERGED,contingency1,Equipment (1),,,,,,,,,",
                        ",,action2,,,,,,,,,,",
                        ",,,NHV1_NHV2_2,VLHV1,FR,380,CURRENT,permanent2,950.0000,855.0000,95.0000,105.56"),
                writer.toString().trim());
    }

    @Test
    void printLimitsViolations() {
        assertEquals("""
                     +---------------+-------+---------+--------------+----------------+----------------+-----------+----------+------------------+----------------+
                     | Equipment (2) | End   | Country | Base voltage | Violation type | Violation name | Value     | Limit    | abs(value-limit) | Loading rate % |
                     +---------------+-------+---------+--------------+----------------+----------------+-----------+----------+------------------+----------------+
                     | NHV1_NHV2_1   | VLHV1 | FR      |          380 | CURRENT        | permanent      | 1100.0000 | 950.0000 |         150.0000 |         110.00 |
                     | NHV1_NHV2_2   | VLHV1 | FR      |          380 | CURRENT        | permanent2     |  950.0000 | 855.0000 |          95.0000 |         105.56 |
                     +---------------+-------+---------+--------------+----------------+----------------+-----------+----------+------------------+----------------+""",
                     Security.printLimitsViolations(Arrays.asList(line1Violation, line2Violation), network, new LimitViolationFilter(), formatterConfig));
    }

    @Test
    void checkLimits() {
        List<LimitViolation> violations = Security.checkLimits(network);
        assertViolations(violations);

        violations = Security.checkLimits(network, 1);
        assertViolations(violations);
    }

    @Test
    void checkLimits05() {
        Line line = network.getLine(EurostagTutorialExample1Factory.NHV1_NHV2_1);
        line.getCurrentLimits1().ifPresent(l -> l.setPermanentLimit(2000));

        List<LimitViolation> violations = Security.checkLimits(network);
        assertTrue(getCurrentLimitViolationOnLine1Side1(violations).isEmpty());

        violations = Security.checkLimits(network, 0.5);
        assertViolation05(violations);

        violations = Security.checkLimits(network, new SimpleLimitsComputer(0.5));
        assertViolation05(violations);
    }

    private static Optional<LimitViolation> getCurrentLimitViolationOnLine1Side1(List<LimitViolation> violations) {
        return violations.stream().filter(l -> EurostagTutorialExample1Factory.NHV1_NHV2_1.equals(l.getSubjectId()) &&
                l.getSide() == ThreeSides.ONE && l.getLimitType() == LimitViolationType.CURRENT).findFirst();
    }

    private static void assertViolation05(List<LimitViolation> violations) {
        Optional<LimitViolation> violation = getCurrentLimitViolationOnLine1Side1(violations);
        assertTrue(violation.isPresent());
        assertEquals(0.5, violation.get().getLimitScaling(), 0.001d);
        assertEquals(2000, violation.get().getLimit(), 0.001d);
        assertEquals(1192, violation.get().getValue(), 1d);
    }

    private static void assertViolationsForThreeWindingsTransformer(List<LimitViolation> violations) {
        assertEquals(3, violations.size());
        violations.forEach(violation -> {
            assertEquals("3WT", violation.getSubjectId());
            assertEquals(LimitViolationType.CURRENT, violation.getLimitType());
        });
    }

    @Test
    void checkLimitsWithThreeWindingsTransformer() {
        Network otherNetwork = ThreeWindingsTransformerNetworkFactory.createWithCurrentLimitsAndTerminalsPAndQ();
        List<LimitViolation> violations = Security.checkLimits(otherNetwork);
        assertViolationsForThreeWindingsTransformer(violations);

        violations = Security.checkLimits(otherNetwork, 1);
        assertViolationsForThreeWindingsTransformer(violations);
    }

    @Test
    void checkLimitsDC() {
        var eBadLimit = assertThrows(IllegalArgumentException.class, () -> Security.checkLimitsDc(network, -0.2, 0.95));
        assertEquals("Limit scaling should be positive, got -0.2", eBadLimit.getMessage());

        var eLowCosPhi = assertThrows(IllegalArgumentException.class, () -> Security.checkLimitsDc(network, 0.7f, -0.1));
        assertEquals("Invalid DC power factor -0.1", eLowCosPhi.getMessage());

        var eHighCosPhi = assertThrows(IllegalArgumentException.class, () -> Security.checkLimitsDc(network, 0.7f, 1.2));
        assertEquals("Invalid DC power factor 1.2", eHighCosPhi.getMessage());

        List<LimitViolation> violations = Security.checkLimitsDc(network, 1, 0.95);
        assertCurrentViolations(violations);
    }

    @Test
    void checkLimitsDCOnThreeWindingsTransformer() {
        Network otherNetwork = ThreeWindingsTransformerNetworkFactory.createWithCurrentLimitsAndTerminalsPAndQ();

        var eBadLimit = assertThrows(IllegalArgumentException.class, () -> Security.checkLimitsDc(otherNetwork, -0.3, 0.95));
        assertEquals("Limit scaling should be positive, got -0.3", eBadLimit.getMessage());

        var eLowCosPhi = assertThrows(IllegalArgumentException.class, () -> Security.checkLimitsDc(otherNetwork, 0.7f, -0.1));
        assertEquals("Invalid DC power factor -0.1", eLowCosPhi.getMessage());

        var eHighCosPhi = assertThrows(IllegalArgumentException.class, () -> Security.checkLimitsDc(otherNetwork, 0.7f, 1.2));
        assertEquals("Invalid DC power factor 1.2", eHighCosPhi.getMessage());

        List<LimitViolation> violations = Security.checkLimitsDc(otherNetwork, 1, 0.95);
        assertEquals(3, violations.size());
        violations.forEach(violation -> {
            assertEquals("3WT", violation.getSubjectId());
            assertEquals(LimitViolationType.CURRENT, violation.getLimitType());
        });
    }

    private static void assertViolations(List<LimitViolation> violations) {
        assertEquals(5, violations.size());
        violations.forEach(violation -> {
            assertTrue(Arrays.asList("VLHV1", "NHV1_NHV2_1", "NHV1_NHV2_2").contains(violation.getSubjectId()));
            if ("VLHV1".equals(violation.getSubjectId())) {
                assertEquals(LimitViolationType.LOW_VOLTAGE, violation.getLimitType());
            } else {
                assertEquals(LimitViolationType.CURRENT, violation.getLimitType());
            }
        });
    }

    private static void assertCurrentViolations(List<LimitViolation> violations) {
        assertEquals(4, violations.size());
        violations.forEach(violation -> {
            assertTrue(Arrays.asList("VLHV1", "NHV1_NHV2_1", "NHV1_NHV2_2").contains(violation.getSubjectId()));
            assertEquals(LimitViolationType.CURRENT, violation.getLimitType());
        });
    }

    private static List<Country> getCountries(Network n, List<LimitViolation> violations) {
        return violations.stream()
                .map(v -> LimitViolationHelper.getCountry(v, n))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    @Test
    void testCountry() {
        List<LimitViolation> violations = Security.checkLimits(network);

        List<Country> expectedCountries = Arrays.asList(Country.FR, Country.BE, Country.FR, Country.BE, Country.FR);
        List<Country> countries = getCountries(network, violations);

        assertEquals(expectedCountries, countries);
    }

    @Test
    void emptyCountry() {
        network.getSubstation("P2").setCountry(null);

        List<LimitViolation> violations = Security.checkLimits(network);

        List<Country> expectedCountries = Arrays.asList(Country.FR, Country.FR, Country.FR);
        List<Country> countries = getCountries(network, violations);

        assertEquals(expectedCountries, countries);
    }

    @Test
    void testNominalVoltages() {
        List<LimitViolation> violations = Security.checkLimits(network);

        List<Double> expectedVoltages = Arrays.asList(380.0, 380.0, 380.0, 380.0, 380.0);
        List<Double> voltages = violations.stream()
                .map(v -> LimitViolationHelper.getNominalVoltage(v, network))
                .toList();

        assertEquals(expectedVoltages, voltages);
    }

    @Test
    void testVoltageLevelIds() {
        List<LimitViolation> violations = Security.checkLimits(network);

        List<String> expectedVoltageLevelIds = Arrays.asList("VLHV1", "VLHV2", "VLHV1", "VLHV2", "VLHV1");
        List<String> voltages = violations.stream()
                .map(v -> LimitViolationHelper.getVoltageLevelId(v, network))
                .toList();

        assertEquals(expectedVoltageLevelIds, voltages);
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Compatibility with limit violations in detailed DC networks: the subject of a violation can be a DC node or a DC
    // line.
    // ----------------------------------------------------------------------------------------------------------------

    private static List<LimitViolation> createDetailedDcEquipmentViolations() {
        return List.of(
            new LimitViolation(DcDetailedNetworkFactory.DC_NODE_FR_NEG, "FR negative pole", null, LimitViolationType.HIGH_VOLTAGE,
                null, Integer.MAX_VALUE, 525., 1.0, -530., null, null),
            new LimitViolation("dcLinePos", null, null, LimitViolationType.CURRENT,
                null, Integer.MAX_VALUE, 1000., 1.0, -1200., null, null));
    }

    // Rows are sorted by subject id: dcLinePos, dcNodeFrNeg.
    // - DC line: no location; abs(value-limit) on |I1|: ||-1200| - 1000| = 200; default permanent limit name
    // - DC node: no location; abs(value-limit) on |V|: ||-530| - 525| = 5
    private static final String DETAILED_DC_LINE_ROW = "dcLinePos,,,,CURRENT,permanent,-1200.0000,1000.0000,200.0000,120.00";
    private static final String DETAILED_DC_NODE_ROW = "dcNodeFrNeg,,,,HIGH_VOLTAGE,,-530.0000,525.0000,5.0000,100.95";

    @Test
    void printPreContingencyDetailedDcEquipmentViolations() {
        SecurityAnalysisResult dcResult = new SecurityAnalysisResult(new LimitViolationsResult(createDetailedDcEquipmentViolations()),
            LoadFlowResult.ComponentResult.Status.CONVERGED, List.of());

        StringWriter writer = new StringWriter();
        Security.printPreContingencyViolations(dcResult, DcDetailedNetworkFactory.createVscSymmetricalMonopole(), writer, formatterFactory, formatterConfig, null);

        assertEquals(String.join(System.lineSeparator(),
                "Pre-contingency violations",
                "Action,Equipment (2),End,Country,Base voltage,Violation type,Violation name,Value,Limit,abs(value-limit),Loading rate %",
                "," + DETAILED_DC_LINE_ROW,
                "," + DETAILED_DC_NODE_ROW),
            writer.toString().trim());
    }

    @Test
    void printPostContingencyDetailedDcEquipmentViolations() {
        PostContingencyResult postContingencyResult = new PostContingencyResult(new Contingency("contingency", List.of()),
            PostContingencyComputationStatus.CONVERGED, new LimitViolationsResult(createDetailedDcEquipmentViolations()),
            NetworkResult.empty(), ConnectivityResult.empty(), Double.NaN, List.of());
        SecurityAnalysisResult dcResult = new SecurityAnalysisResult(LimitViolationsResult.empty(),
            LoadFlowResult.ComponentResult.Status.CONVERGED, List.of(postContingencyResult));

        StringWriter writer = new StringWriter();
        Security.printPostContingencyViolations(dcResult, DcDetailedNetworkFactory.createVscSymmetricalMonopole(), writer, formatterFactory, formatterConfig, null, false);

        assertEquals(String.join(System.lineSeparator(),
                "Post-contingency limit violations",
                "Contingency,Status,Action,Equipment (2),End,Country,Base voltage,Violation type,Violation name,Value,Limit,abs(value-limit),Loading rate %",
                "contingency,CONVERGED,,Equipment (2),,,,,,,,,",
                ",,," + DETAILED_DC_LINE_ROW,
                ",,," + DETAILED_DC_NODE_ROW),
            writer.toString().trim());
    }

    @Test
    void printLimitsViolationsWithDetailedDcEquipmentViolations() {
        Network dcNetwork = DcDetailedNetworkFactory.createVscSymmetricalMonopole();
        List<LimitViolation> violations = createDetailedDcEquipmentViolations();

        assertEquals("""
            +---------------+-----+---------+--------------+----------------+----------------+------------+-----------+------------------+----------------+
            | Equipment (2) | End | Country | Base voltage | Violation type | Violation name | Value      | Limit     | abs(value-limit) | Loading rate % |
            +---------------+-----+---------+--------------+----------------+----------------+------------+-----------+------------------+----------------+
            | dcLinePos     |     |         |              | CURRENT        | permanent      | -1200.0000 | 1000.0000 |         200.0000 |         120.00 |
            | dcNodeFrNeg   |     |         |              | HIGH_VOLTAGE   |                |  -530.0000 |  525.0000 |           5.0000 |         100.95 |
            +---------------+-----+---------+--------------+----------------+----------------+------------+-----------+------------------+----------------+""",
            Security.printLimitsViolations(violations, dcNetwork, new LimitViolationFilter(), formatterConfig));

        // writeName = true: subject names replace ids, and DC rows still have empty location cells
        String tableWithNames = Security.printLimitsViolations(violations, dcNetwork,
            new Security.LimitViolationWriteConfig(null, formatterConfig, true));
        assertTrue(tableWithNames.lines().anyMatch(l -> l.matches("\\| FR negative pole\\s+\\|\\s+\\|\\s+\\|\\s+\\| HIGH_VOLTAGE .*")), tableWithNames);
    }

    @Test
    void checkLimitsWithDetailedDcEquipmentViolations() {
        Network dcNetwork = DcDetailedNetworkFactory.createVscSymmetricalMonopole();
        setUpAcAndDetailedDcEquipmentViolations(dcNetwork);

        assertEquals(List.of(AC_SUBJECT + "/HIGH_VOLTAGE", "dcLinePos/CURRENT", "dcNodeFrPos/HIGH_VOLTAGE"),
            subjects(Security.checkLimits(dcNetwork)));
        // No AC voltage violation in DC mode
        assertEquals(DETAILED_DC_SUBJECTS, subjects(Security.checkLimitsDc(dcNetwork, 1, 0.95)));
    }
}
