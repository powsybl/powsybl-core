/**
 * Copyright (c) 2016-2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.powsybl.commons.io.table.CsvTableFormatterFactory;
import com.powsybl.commons.io.table.TableFormatterConfig;
import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Network;
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

    private final TableFormatterConfig formatterConfig = new TableFormatterConfig(Locale.US, ',', "inv", true, true);

    private final CsvTableFormatterFactory formatterFactory = new CsvTableFormatterFactory();

    private Network network;
    private SecurityAnalysisResult result;
    private LimitViolation line1Violation;
    private LimitViolation line2Violation;

    @Before
    public void setUp() {
        network = TestingNetworkFactory.create();

        // create pre-contingency results, just one violation on line1
        line1Violation = new LimitViolation("LINE1", LimitViolationType.CURRENT, "20'", 1000f, 1.0f, 1100.0f, Branch.Side.ONE);
        LimitViolationsResult preContingencyResult = new LimitViolationsResult(true, Collections.singletonList(line1Violation), Collections.singletonList("action1"));

        // create post-contingency results, still the line1 violation plus line2 violation
        Contingency contingency1 = Mockito.mock(Contingency.class);
        Mockito.when(contingency1.getId()).thenReturn("contingency1");
        line2Violation = new LimitViolation("LINE2", LimitViolationType.CURRENT, "10'", 900f, 1.0f, 950, Branch.Side.ONE);
        PostContingencyResult postContingencyResult = new PostContingencyResult(contingency1, true, Arrays.asList(line1Violation, line2Violation), Collections.singletonList("action2"));
        result = new SecurityAnalysisResult(network, preContingencyResult, Collections.singletonList(postContingencyResult));
    }

    @Test
    public void printPreContingencyViolations() throws Exception {
        StringWriter writer = new StringWriter();
        //System.out.println(formatterConfig);
        try {
            Security.printPreContingencyViolations(result, writer, formatterFactory, formatterConfig, null);
        } finally {
            writer.close();
        }
        assertEquals(String.join(System.lineSeparator(),
                                 "Pre-contingency violations",
                                 "Action,Equipment,Violation type,Violation name,Value,Limit,Loading rate %",
                                 "action1,,,,,,",
                                 ",LINE1,CURRENT,20',1100.0000,1000.0000,110.0000"),
                     writer.toString().trim());
    }

    @Test
    public void printPostContingencyViolations() throws Exception {
        StringWriter writer = new StringWriter();
        try {
            Security.printPostContingencyViolations(result, writer, formatterFactory, formatterConfig, null, false);
        } finally {
            writer.close();
        }
        assertEquals(String.join(System.lineSeparator(),
                                 "Post-contingency limit violations",
                                 "Contingency,Status,Action,Equipment,Violation type,Violation name,Value,Limit,Loading rate %",
                                 "contingency1,converge,,,,,,,",
                                 ",,action2,,,,,,",
                                 ",,,LINE1,CURRENT,20',1100.0000,1000.0000,110.0000",
                                 ",,,LINE2,CURRENT,10',950.0000,900.0000,105.5556"),
                     writer.toString().trim());
    }

    @Test
    public void printPostContingencyViolationsWithPreContingencyViolationsFiltering() throws Exception {
        StringWriter writer = new StringWriter();
        try {
            Security.printPostContingencyViolations(result, writer, formatterFactory, formatterConfig, null, true);
        } finally {
            writer.close();
        }
        assertEquals(String.join(System.lineSeparator(),
                                 "Post-contingency limit violations",
                                 "Contingency,Status,Action,Equipment,Violation type,Violation name,Value,Limit,Loading rate %",
                                 "contingency1,converge,,,,,,,",
                                 ",,action2,,,,,,",
                                 ",,,LINE2,CURRENT,10',950.0000,900.0000,105.5556"),
                     writer.toString().trim());
    }

    @Test
    public void printLimitsViolations() throws Exception {
        assertEquals("+---------+--------------+---------------+----------------+----------------+-----------+-----------+------------------+----------------+\n" +
                     "| Country | Base voltage | Equipment (2) | Violation type | Violation name | Value     | Limit     | abs(value-limit) | Loading rate % |\n" +
                     "+---------+--------------+---------------+----------------+----------------+-----------+-----------+------------------+----------------+\n" +
                     "| FR      | 380.0        | LINE1         | CURRENT        | 20'            | 1100.0000 | 1000.0000 |        1000.0000 |       110.0000 |\n" +
                     "| FR      | 220.0        | LINE2         | CURRENT        | 10'            |  950.0000 |  900.0000 |         900.0000 |       105.5556 |\n" +
                     "+---------+--------------+---------------+----------------+----------------+-----------+-----------+------------------+----------------+",
                     Security.printLimitsViolations(network, Arrays.asList(line1Violation, line2Violation), new LimitViolationFilter(), formatterConfig));
    }


    @Test
    public void checkLimits() {
        Network network = TestingNetworkFactory.createFromEurostag();

        List<LimitViolation> violations = Security.checkLimits(network);
        checkLimits(violations);

        violations = Security.checkLimits(network, 1);
        checkLimits(violations);
    }

    private void checkLimits(List<LimitViolation> violations) {
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
