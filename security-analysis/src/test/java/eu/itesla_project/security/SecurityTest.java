/**
 * Copyright (c) 2016-2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import eu.itesla_project.commons.io.table.CsvTableFormatterFactory;
import eu.itesla_project.commons.io.table.TableFormatterConfig;
import eu.itesla_project.contingency.Contingency;
import eu.itesla_project.iidm.network.Bus;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.test.EurostagTutorialExample1Factory;
import eu.itesla_project.security.Security.CurrentLimitType;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SecurityTest {

    private final TableFormatterConfig formatterConfig = new TableFormatterConfig(Locale.US, ',', "inv", true, true);

    private final CsvTableFormatterFactory formatterFactory = new CsvTableFormatterFactory();
    
    private SecurityAnalysisResult result;
    private LimitViolation line1Violation;
    private LimitViolation line2Violation;

    @Before
    public void setUp() {
        // create pre-contingency results, just one violation on line1
        line1Violation = new LimitViolation("line1", LimitViolationType.CURRENT, 1000f, "20'", 1100);
        LimitViolationsResult preContingencyResult = new LimitViolationsResult(true, Collections.singletonList(line1Violation), Collections.singletonList("action1"));

        // create post-contingency results, still the line1 violation plus line2 violation
        Contingency contingency1 = Mockito.mock(Contingency.class);
        Mockito.when(contingency1.getId()).thenReturn("contingency1");
        line2Violation = new LimitViolation("line2", LimitViolationType.CURRENT, 900f, "10'", 950);
        PostContingencyResult postContingencyResult = new PostContingencyResult(contingency1, true, Arrays.asList(line1Violation, line2Violation), Collections.singletonList("action2"));
        result = new SecurityAnalysisResult(preContingencyResult, Collections.singletonList(postContingencyResult));
    }

    @Test
    public void printPreContingencyViolations() throws Exception {
        StringWriter writer = new StringWriter();
        try {
            Security.printPreContingencyViolations(result, writer, formatterFactory, formatterConfig, null);
        } finally {
            writer.close();
        }
        assertEquals(String.join(System.lineSeparator(), 
                                 "Pre-contingency violations",
                                 "Action,Equipment,Violation type,Violation name,Value,Limit,Charge %",
                                 "action1,,,,,,",
                                 ",line1,CURRENT,20',1100.00,1000.0,110"),
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
                                 "Contingency,Status,Action,Equipment,Violation type,Violation name,Value,Limit,Charge %",
                                 "contingency1,converge,,,,,,,",
                                 ",,action2,,,,,,",
                                 ",,,line1,CURRENT,20',1100.00,1000.0,110",
                                 ",,,line2,CURRENT,10',950.000,900.0,106"),
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
                                 "Contingency,Status,Action,Equipment,Violation type,Violation name,Value,Limit,Charge %",
                                 "contingency1,converge,,,,,,,",
                                 ",,action2,,,,,,",
                                 ",,,line2,CURRENT,10',950.000,900.0,106"),
                     writer.toString().trim());
    }
    
    @Test
    public void printLimitsViolations() {
        assertEquals(String.join("\n",
                                 "+---------+--------------+---------------+----------------+----------------+--------+--------+------------------+----------+",
                                 "| Country | Base voltage | Equipment (2) | Violation type | Violation name | value  | limit  | abs(value-limit) | charge % |",
                                 "+---------+--------------+---------------+----------------+----------------+--------+--------+------------------+----------+",
                                 "|         |              | line1         | CURRENT        | 20'            | 1100.0 | 1000.0 | 100.0            | 110      |",
                                 "|         |              | line2         | CURRENT        | 10'            | 950.0  | 900.0  | 50.0             | 106      |",
                                 "+---------+--------------+---------------+----------------+----------------+--------+--------+------------------+----------+"),
                     Security.printLimitsViolations(Arrays.asList(line1Violation, line2Violation), new LimitViolationFilter()));
    }
    
    @Test
    public void checkLimits() {
        Network network = EurostagTutorialExample1Factory.create();
        ((Bus) network.getIdentifiable("NHV1")).setV(380f).getVoltageLevel().setLowVoltageLimit(400f).setHighVoltageLimit(500f);
        ((Bus) network.getIdentifiable("NHV2")).setV(380f).getVoltageLevel().setLowVoltageLimit(300f).setHighVoltageLimit(500f);
        network.getLine("NHV1_NHV2_1").getTerminal1().setP(560f).setQ(550f);
        network.getLine("NHV1_NHV2_1").getTerminal2().setP(560f).setQ(550f);
        network.getLine("NHV1_NHV2_1").newCurrentLimits1().setPermanentLimit(1500f).add();
        network.getLine("NHV1_NHV2_1").newCurrentLimits2()
            .setPermanentLimit(1100f)
            .beginTemporaryLimit()
                .setName("10'")
                .setAcceptableDuration(10 * 60)
                .setValue(1200)
            .endTemporaryLimit()
            .add();
        network.getLine("NHV1_NHV2_2").getTerminal1().setP(560f).setQ(550f);
        network.getLine("NHV1_NHV2_2").getTerminal2().setP(560f).setQ(550f);
        network.getLine("NHV1_NHV2_2").newCurrentLimits1()
            .setPermanentLimit(1100f)
            .beginTemporaryLimit()
                .setName("20'")
                .setAcceptableDuration(20 * 60)
                .setValue(1200)
            .endTemporaryLimit()
            .add();
        network.getLine("NHV1_NHV2_2").newCurrentLimits2().setPermanentLimit(1500f).add();
        
        List<LimitViolation> violations = Security.checkLimits(network);

        assertEquals(3, violations.size());
        violations.forEach( violation -> 
        {
            assertTrue(Arrays.asList("VLHV1", "NHV1_NHV2_1", "NHV1_NHV2_2").contains(violation.getSubjectId()));
            if ( "VLHV1".equals(violation.getSubjectId()) ) {
                assertEquals(LimitViolationType.LOW_VOLTAGE, violation.getLimitType()); 
            } else {
                assertEquals(LimitViolationType.CURRENT, violation.getLimitType());
            }
        });
        
        violations = Security.checkLimits(network, CurrentLimitType.TATL, 1);
        
        assertEquals(3, violations.size());
        violations.forEach( violation -> 
        {
            assertTrue(Arrays.asList("VLHV1", "NHV1_NHV2_1", "NHV1_NHV2_2").contains(violation.getSubjectId()));
            if ( "VLHV1".equals(violation.getSubjectId()) ) {
                assertEquals(LimitViolationType.LOW_VOLTAGE, violation.getLimitType()); 
            } else {
                assertEquals(LimitViolationType.CURRENT, violation.getLimitType());
            }
        });
    }
}
