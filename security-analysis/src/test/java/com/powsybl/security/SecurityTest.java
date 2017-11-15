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
import com.powsybl.iidm.network.Bus;
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

    private SecurityAnalysisResult result;
    private LimitViolation line1Violation;
    private LimitViolation line2Violation;
    private Network network;

    private Network createNetwork() {
        Network network = EurostagTutorialExample1Factory.create();
        ((Bus) network.getIdentifiable("NHV1")).setV(380f).getVoltageLevel().setLowVoltageLimit(400f).setHighVoltageLimit(500f);
        ((Bus) network.getIdentifiable("NHV2")).setV(380f).getVoltageLevel().setLowVoltageLimit(300f).setHighVoltageLimit(500f);
        network.getLine("NHV1_NHV2_1").getTerminal1().setP(560f).setQ(550f);
        network.getLine("NHV1_NHV2_1").getTerminal2().setP(560f).setQ(550f);
        network.getLine("NHV1_NHV2_1").newCurrentLimits1().setPermanentLimit(500f).add();
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
        network.getLine("NHV1_NHV2_2").newCurrentLimits2().setPermanentLimit(500f).add();

        return network;
    }

    @Before
    public void setUp() {
        formatterConfig = new TableFormatterConfig(Locale.US, ',', "inv", true, true);
        // create pre-contingency results, just one violation on line1
        line1Violation = new LimitViolation("NHV1_NHV2_1", LimitViolationType.CURRENT, "Permanent limit", 1000f, 20, 1100, Branch.Side.ONE);
        LimitViolationsResult preContingencyResult = new LimitViolationsResult(true, Collections.singletonList(line1Violation), Collections.singletonList("action1"));

        // create post-contingency results, still the line1 violation plus line2 violation
        Contingency contingency1 = Mockito.mock(Contingency.class);
        Mockito.when(contingency1.getId()).thenReturn("contingency1");
        line2Violation = new LimitViolation("NHV1_NHV2_2", LimitViolationType.CURRENT, "Permanent limit", 900f, 10, 950, Branch.Side.ONE);
        PostContingencyResult postContingencyResult = new PostContingencyResult(contingency1, true, Arrays.asList(line1Violation, line2Violation), Collections.singletonList("action2"));
        result = new SecurityAnalysisResult(preContingencyResult, Collections.singletonList(postContingencyResult));

        network = createNetwork();
    }

    @Test
    public void printPreContingencyViolations() throws Exception {
        StringWriter writer = new StringWriter();
        //System.out.println(formatterConfig);
        try {
            Security.printPreContingencyViolations(result, network, writer, formatterFactory, formatterConfig, null);
        } finally {
            writer.close();
        }
        assertEquals(String.join(System.lineSeparator(),
                                 "Pre-contingency violations",
                                 "Action,Country,Base voltage,Equipment (1),End,Violation type,Violation name,Value,Limit,abs(value-limit),Loading rate %",
                                 "action1,,,,,,,,,,",
                                 ",FR,380,NHV1_NHV2_1,VLHV1,CURRENT,Permanent limit,1100.0000,20000.0000,18900.0000,110.00"),
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
                                 "Contingency,Status,Action,Country,Base voltage,Equipment (2),End,Violation type,Violation name,Value,Limit,abs(value-limit),Loading rate %",
                                 "contingency1,converge,,,,(2),,,,,,,",
                                 ",,action2,,,,,,,,,,",
                                 ",,,FR,380,NHV1_NHV2_1,VLHV1,CURRENT,Permanent limit,1100.0000,20000.0000,18900.0000,110.00",
                                 ",,,FR,380,NHV1_NHV2_2,VLHV1,CURRENT,Permanent limit,950.0000,9000.0000,8050.0000,105.56"),
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
                                 "Contingency,Status,Action,Country,Base voltage,Equipment (1),End,Violation type,Violation name,Value,Limit,abs(value-limit),Loading rate %",
                                 "contingency1,converge,,,,(1),,,,,,,",
                                 ",,action2,,,,,,,,,,",
                                 ",,,FR,380,NHV1_NHV2_2,VLHV1,CURRENT,Permanent limit,950.0000,9000.0000,8050.0000,105.56"),
                     writer.toString().trim());
    }

    @Test
    public void printLimitsViolations() throws Exception {
        assertEquals("+---------+--------------+---------------+-------+----------------+-----------------+-----------+------------+------------------+----------------+\n" +
                     "| Country | Base voltage | Equipment (2) | End   | Violation type | Violation name  | Value     | Limit      | abs(value-limit) | Loading rate % |\n" +
                     "+---------+--------------+---------------+-------+----------------+-----------------+-----------+------------+------------------+----------------+\n" +
                     "| FR      |          380 | NHV1_NHV2_1   | VLHV1 | CURRENT        | Permanent limit | 1100.0000 | 20000.0000 |       18900.0000 |         110.00 |\n" +
                     "| FR      |          380 | NHV1_NHV2_2   | VLHV1 | CURRENT        | Permanent limit |  950.0000 |  9000.0000 |        8050.0000 |         105.56 |\n" +
                     "+---------+--------------+---------------+-------+----------------+-----------------+-----------+------------+------------------+----------------+",
                     Security.printLimitsViolations(Arrays.asList(line1Violation, line2Violation), network, new LimitViolationFilter(), formatterConfig));
    }

    @Test
    public void checkLimits() {
        List<LimitViolation> violations = Security.checkLimits(network);

        assertEquals(5, violations.size());
        violations.forEach(violation -> {
            assertTrue(Arrays.asList("VLHV1", "NHV1_NHV2_1", "NHV1_NHV2_2").contains(violation.getSubjectId()));
            if ("VLHV1".equals(violation.getSubjectId())) {
                assertEquals(LimitViolationType.LOW_VOLTAGE, violation.getLimitType());
            } else {
                assertEquals(LimitViolationType.CURRENT, violation.getLimitType());
            }
        });

        violations = Security.checkLimits(network, 1);

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
