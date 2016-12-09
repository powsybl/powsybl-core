/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.security;

import eu.itesla_project.commons.io.table.CsvTableFormatterFactory;
import eu.itesla_project.commons.io.table.TableFormatterConfig;
import eu.itesla_project.contingency.Contingency;
import eu.itesla_project.iidm.network.Identifiable;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SecurityTest {

    private final TableFormatterConfig formatterConfig = new TableFormatterConfig(Locale.US, ',', "inv", true, true);

    private final CsvTableFormatterFactory formatterFactory = new CsvTableFormatterFactory();

    private static SecurityAnalysisResult createResult() {
        // create pre-contingency results, just one violation on line1
        Identifiable line1 = Mockito.mock(Identifiable.class);
        Mockito.when(line1.getId()).thenReturn("line1");
        LimitViolation line1Violation = new LimitViolation(line1, LimitViolationType.CURRENT, 1000f, "20'", 1100);
        PreContingencyResult preContingencyResult = new PreContingencyResult(true, Arrays.asList(line1Violation));

        // create post-contingency results, still the line1 violation plus line2 violation
        Contingency contingency1 = Mockito.mock(Contingency.class);
        Mockito.when(contingency1.getId()).thenReturn("contingency1");
        Identifiable line2 = Mockito.mock(Identifiable.class);
        Mockito.when(line2.getId()).thenReturn("line2");
        LimitViolation line2Violation = new LimitViolation(line2, LimitViolationType.CURRENT, 900f, "10'", 950);
        PostContingencyResult postContingencyResult = new PostContingencyResult(contingency1, true, Arrays.asList(line1Violation, line2Violation));
        return new SecurityAnalysisResult(preContingencyResult, Arrays.asList(postContingencyResult));
    }

    @Test
    public void printPreContingencyViolations() throws Exception {
        StringWriter writer = new StringWriter();
        try {
            Security.printPreContingencyViolations(createResult(), writer, formatterFactory, formatterConfig, null);
        } finally {
            writer.close();
        }
        assertEquals(writer.toString(), "Pre-contingency violations\n" +
                                               "Action,Equipment,Violation type,Violation name,Value,Limit,Charge %\n" +
                                               ",line1,CURRENT,20',1100.00,1000.0,110\n");
    }

    @Test
    public void printPostContingencyViolations() throws Exception {
        StringWriter writer = new StringWriter();
        try {
            Security.printPostContingencyViolations(createResult(), writer, formatterFactory, formatterConfig, null, false);
        } finally {
            writer.close();
        }
        assertEquals(writer.toString(), "Post-contingency limit violations\n" +
                                               "Contingency,Status,Action,Equipment,Violation type,Violation name,Value,Limit,Charge %\n" +
                                               "contingency1,converge,,,,,,,\n" +
                                               ",,,line1,CURRENT,20',1100.00,1000.0,110\n" +
                                               ",,,line2,CURRENT,10',950.000,900.0,106\n");
    }

    @Test
    public void printPostContingencyViolationsWithPreContingencyViolationsFiltering() throws Exception {
        StringWriter writer = new StringWriter();
        try {
            Security.printPostContingencyViolations(createResult(), writer, formatterFactory, formatterConfig, null, true);
        } finally {
            writer.close();
        }
        assertEquals(writer.toString(), "Post-contingency limit violations\n" +
                                               "Contingency,Status,Action,Equipment,Violation type,Violation name,Value,Limit,Charge %\n" +
                                               "contingency1,converge,,,,,,,\n" +
                                               ",,,line2,CURRENT,10',950.000,900.0,106\n");
    }
}