/**
 * Copyright (c) 2016-2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.powsybl.commons.io.table.AsciiTableFormatterFactory;
import com.powsybl.commons.io.table.CsvTableFormatterFactory;
import com.powsybl.commons.io.table.TableFormatterConfig;
import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SecurityTest {

    TableFormatterConfig formatterConfig;

    private final CsvTableFormatterFactory formatterFactory = new CsvTableFormatterFactory();
    private final AsciiTableFormatterFactory formatterFactoryAscii = new AsciiTableFormatterFactory();

    private SecurityAnalysisResult result;
    private LimitViolation line1Violation;
    private LimitViolation line2Violation;
    private Network network;

    @Before
    public void setUp() {
        formatterConfig = new TableFormatterConfig(Locale.US, ',', "inv", true, true);

        network = EurostagTutorialExample1Factory.createWithCurrentLimits();

        // create pre-contingency results, just one violation on line1
        line1Violation = new LimitViolation("NHV1_NHV2_1", LimitViolationType.CURRENT, null, Integer.MAX_VALUE, 1000.0, 0.95f, 1100.0, Branch.Side.ONE);
        LimitViolationsResult preContingencyResult = new LimitViolationsResult(true, Collections.singletonList(line1Violation), Collections.singletonList("action1"));

        // create post-contingency results, still the line1 violation plus line2 violation
        Contingency contingency1 = Mockito.mock(Contingency.class);
        Mockito.when(contingency1.getId()).thenReturn("contingency1");
        line2Violation = new LimitViolation("NHV1_NHV2_2", LimitViolationType.CURRENT, null, Integer.MAX_VALUE, 900.0, 0.95f, 950.0, Branch.Side.ONE);
        PostContingencyResult postContingencyResult = new PostContingencyResult(contingency1, true, Arrays.asList(line1Violation, line2Violation), Collections.singletonList("action2"));

        result = new SecurityAnalysisResult(preContingencyResult, Collections.singletonList(postContingencyResult));
    }

    @Test
    public void printPreContingencyViolations() throws Exception {
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
                                 ",NHV1_NHV2_1,VLHV1,FR,380,CURRENT,Permanent limit,1100.0000,950.0000,150.0000,110.00"),
                     writer.toString().trim());
    }

    @Test
    public void printPostContingencyViolations() throws Exception {
        StringWriter writer = new StringWriter();
        try {
            Security.printPostContingencyViolations(result, network, writer, formatterFactory, formatterConfig, null, false);
        } finally {
            writer.close();
        }
        assertEquals(String.join(System.lineSeparator(),
                                 "Post-contingency limit violations",
                                 "Contingency,Status,Action,Equipment (2),End,Country,Base voltage,Violation type,Violation name,Value,Limit,abs(value-limit),Loading rate %",
                                 "contingency1,converge,,Equipment (2),,,,,,,,,",
                                 ",,action2,,,,,,,,,,",
                                 ",,,NHV1_NHV2_1,VLHV1,FR,380,CURRENT,Permanent limit,1100.0000,950.0000,150.0000,110.00",
                                 ",,,NHV1_NHV2_2,VLHV1,FR,380,CURRENT,Permanent limit,950.0000,855.0000,95.0000,105.56"),
                     writer.toString().trim());
    }

    @Test
    public void printPostContingencyViolationsWithPreContingencyViolationsFiltering() throws Exception {
        StringWriter writer = new StringWriter();
        try {
            Security.printPostContingencyViolations(result, network, writer, formatterFactory, formatterConfig, null, true);
        } finally {
            writer.close();
        }
        assertEquals(String.join(System.lineSeparator(),
                                 "Post-contingency limit violations",
                                 "Contingency,Status,Action,Equipment (1),End,Country,Base voltage,Violation type,Violation name,Value,Limit,abs(value-limit),Loading rate %",
                                 "contingency1,converge,,Equipment (1),,,,,,,,,",
                                 ",,action2,,,,,,,,,,",
                                 ",,,NHV1_NHV2_2,VLHV1,FR,380,CURRENT,Permanent limit,950.0000,855.0000,95.0000,105.56"),
                     writer.toString().trim());
    }

    @Test
    public void printLimitsViolations() throws Exception {
        assertEquals("+---------------+-------+---------+--------------+----------------+-----------------+-----------+----------+------------------+----------------+\n" +
                     "| Equipment (2) | End   | Country | Base voltage | Violation type | Violation name  | Value     | Limit    | abs(value-limit) | Loading rate % |\n" +
                     "+---------------+-------+---------+--------------+----------------+-----------------+-----------+----------+------------------+----------------+\n" +
                     "| NHV1_NHV2_1   | VLHV1 | FR      |          380 | CURRENT        | Permanent limit | 1100.0000 | 950.0000 |         150.0000 |         110.00 |\n" +
                     "| NHV1_NHV2_2   | VLHV1 | FR      |          380 | CURRENT        | Permanent limit |  950.0000 | 855.0000 |          95.0000 |         105.56 |\n" +
                     "+---------------+-------+---------+--------------+----------------+-----------------+-----------+----------+------------------+----------------+",
                     Security.printLimitsViolations(Arrays.asList(line1Violation, line2Violation), network, new LimitViolationFilter(), formatterConfig));
    }

    @Test
    public void checkLimits() {
        List<LimitViolation> violations = Security.checkLimits(network);

        assertEquals(5, violations.size());
        assertViolations(violations);

        violations = Security.checkLimits(network, 1);
        assertViolations(violations);
    }

    private static void assertViolations(List<LimitViolation> violations) {
        assertEquals(5, violations.size());
        violations.forEach(violation ->  {
            assertTrue(Arrays.asList("VLHV1", "NHV1_NHV2_1", "NHV1_NHV2_2").contains(violation.getSubjectId()));
            if ("VLHV1".equals(violation.getSubjectId())) {
                assertEquals(LimitViolationType.LOW_VOLTAGE, violation.getLimitType());
            } else {
                assertEquals(LimitViolationType.CURRENT, violation.getLimitType());
            }
        });
    }
}
