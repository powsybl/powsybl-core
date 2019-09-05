/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.comparator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.powsybl.iidm.network.Branch;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.LimitViolationType;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class SecurityAnalysisResultComparisonWriterTest {

    LimitViolation vlViolation;
    LimitViolation lineViolation;
    LimitViolation similarLineViolation;
    List<String> actions;
    Writer writer;
    SecurityAnalysisResultComparisonWriter comparisonWriter;

    @Before
    public void setUp() {
        vlViolation = new LimitViolation("VL1", LimitViolationType.HIGH_VOLTAGE, 200.0, 1, 250.0);
        lineViolation = new LimitViolation("NHV1_NHV2_1", LimitViolationType.CURRENT, "PermanentLimit", Integer.MAX_VALUE, 1000.0, 1, 1100.0, Branch.Side.ONE);
        similarLineViolation = new LimitViolation("NHV1_NHV2_1", LimitViolationType.CURRENT, "PermanentLimit", Integer.MAX_VALUE, 1000.0, 1, 1100.09, Branch.Side.ONE);
        actions = Arrays.asList("action1", "action2");
        writer = new StringWriter();
        comparisonWriter = new SecurityAnalysisResultComparisonWriter(writer);
    }

    @After
    public void tearDown() throws IOException {
        comparisonWriter.close();
    }

    @Test
    public void write() {
        String content = String.join(System.lineSeparator(),
                                     "Security Analysis Results Comparison",
                                     String.join(";", "Contingency", "StatusResult1", "StatusResult2", "Equipment", "End", "ViolationType",
                                                 "ViolationNameResult1", "ValueResult1", "LimitResult1", "ViolationNameResult2", "ValueResult2",
                                                 "LimitResult2", "ActionsResult1", "ActionsResult2", "Comparison"),
                                     String.join(";", "", "converge", "converge", "", "", "", "", "", "", "", "", "", "", "", "equivalent"),
                                     String.join(";", "", "", "", "VL1", "", LimitViolationType.HIGH_VOLTAGE.name(),
                                                 "", String.format(Locale.getDefault(), "%g", 250.0), String.format(Locale.getDefault(), "%g", 200.0),
                                                 "", String.format(Locale.getDefault(), "%g", 250.0), String.format(Locale.getDefault(), "%g", 200.0),
                                                 "", "", "equivalent"),
                                     String.join(";", "contingency", "converge", "converge", "", "", "", "", "", "", "", "", "", "", "", "equivalent"),
                                     String.join(";", "contingency", "", "", "VL1", "", LimitViolationType.HIGH_VOLTAGE.name(),
                                                 "", String.format(Locale.getDefault(), "%g", 250.0), String.format(Locale.getDefault(), "%g", 200.0),
                                                 "", String.format(Locale.getDefault(), "%g", 250.0), String.format(Locale.getDefault(), "%g", 200.0),
                                                 "", "", "equivalent"),
                                     String.join(";", "contingency", "", "", "NHV1_NHV2_1", Branch.Side.ONE.name(), LimitViolationType.CURRENT.name(),
                                                 "PermanentLimit", String.format(Locale.getDefault(), "%g", 1100.0), String.format(Locale.getDefault(), "%g", 1000.0),
                                                 "PermanentLimit", String.format(Locale.getDefault(), "%g", 1100.09), String.format(Locale.getDefault(), "%g", 1000.0),
                                                 "", "",  "equivalent"),
                                     String.join(";", "contingency", "", "", "", "", "", "", "", "", "", "", "", actions.toString(), actions.toString(), "equivalent"));

        // precontingency violations results
        comparisonWriter.write(true, true, true);
        comparisonWriter.write(vlViolation, vlViolation, true);
        comparisonWriter.write(Collections.emptyList(), Collections.emptyList(), true);

        // postcontingency violations results
        comparisonWriter.setContingency("contingency");
        comparisonWriter.write(true, true, true);
        comparisonWriter.write(vlViolation, vlViolation, true);
        comparisonWriter.write(lineViolation, similarLineViolation, true);
        comparisonWriter.write(actions, actions, true);

        assertEquals(content, writer.toString().trim());
    }

    @Test
    public void writeMissingResult1() {
        String content = String.join(System.lineSeparator(),
                                     "Security Analysis Results Comparison",
                                     String.join(";", "Contingency", "StatusResult1", "StatusResult2", "Equipment", "End", "ViolationType",
                                                 "ViolationNameResult1", "ValueResult1", "LimitResult1", "ViolationNameResult2", "ValueResult2",
                                                 "LimitResult2", "ActionsResult1", "ActionsResult2", "Comparison"),
                                     String.join(";", "", "converge", "converge", "", "", "", "", "", "", "", "", "", "", "", "equivalent"),
                                     String.join(";", "", "", "", "VL1", "", LimitViolationType.HIGH_VOLTAGE.name(),
                                                 "", String.format(Locale.getDefault(), "%g", 250.0), String.format(Locale.getDefault(), "%g", 200.0),
                                                 "", String.format(Locale.getDefault(), "%g", 250.0), String.format(Locale.getDefault(), "%g", 200.0),
                                                 "", "", "equivalent"),
                                     String.join(";", "contingency", "diverge", "converge", "", "", "", "", "", "", "", "", "", "", "", "different"),
                                     String.join(";", "contingency", "", "", "VL1", "", LimitViolationType.HIGH_VOLTAGE.name(),
                                                 "", "", "",
                                                 "", String.format(Locale.getDefault(), "%g", 250.0), String.format(Locale.getDefault(), "%g", 200.0),
                                                 "", "", "different"),
                                     String.join(";", "contingency", "", "", "NHV1_NHV2_1", Branch.Side.ONE.name(), LimitViolationType.CURRENT.name(),
                                                 "", "", "",
                                                 "PermanentLimit", String.format(Locale.getDefault(), "%g", 1100.0), String.format(Locale.getDefault(), "%g", 1000.0),
                                                 "", "", "different"),
                                     String.join(";", "contingency", "", "", "", "", "", "", "", "", "", "", "", "", actions.toString(), "different"));

        // precontingency violations results
        comparisonWriter.write(true, true, true);
        comparisonWriter.write(vlViolation, vlViolation, true);
        comparisonWriter.write(Collections.emptyList(), Collections.emptyList(), true);

        // missing postcontingency results
        comparisonWriter.setContingency("contingency");
        comparisonWriter.write(false, true, false);
        comparisonWriter.write(null, vlViolation, false);
        comparisonWriter.write(null, lineViolation, false);
        comparisonWriter.write(null, actions, false);

        assertEquals(content, writer.toString().trim());
    }

    @Test
    public void writeMissingResult2() {
        String content = String.join(System.lineSeparator(),
                                     "Security Analysis Results Comparison",
                                     String.join(";", "Contingency", "StatusResult1", "StatusResult2", "Equipment", "End", "ViolationType",
                                                 "ViolationNameResult1", "ValueResult1", "LimitResult1", "ViolationNameResult2", "ValueResult2",
                                                 "LimitResult2", "ActionsResult1", "ActionsResult2", "Comparison"),
                                     String.join(";", "", "converge", "converge", "", "", "", "", "", "", "", "", "", "", "",  "equivalent"),
                                     String.join(";", "", "", "", "VL1", "", LimitViolationType.HIGH_VOLTAGE.name(),
                                                 "", String.format(Locale.getDefault(), "%g", 250.0), String.format(Locale.getDefault(), "%g", 200.0),
                                                 "", String.format(Locale.getDefault(), "%g", 250.0), String.format(Locale.getDefault(), "%g", 200.0),
                                                 "", "",  "equivalent"),
                                     String.join(";", "contingency", "converge", "", "", "", "", "", "", "", "", "", "", "", "", "different"),
                                     String.join(";", "contingency", "", "", "VL1", "", LimitViolationType.HIGH_VOLTAGE.name(),
                                                 "", String.format(Locale.getDefault(), "%g", 250.0), String.format(Locale.getDefault(), "%g", 200.0),
                                                 "", "", "", "", "", "different"),
                                     String.join(";", "contingency", "", "", "NHV1_NHV2_1", Branch.Side.ONE.name(), LimitViolationType.CURRENT.name(),
                                                 "PermanentLimit", String.format(Locale.getDefault(), "%g", 1100.0), String.format(Locale.getDefault(), "%g", 1000.0),
                                                 "", "", "", "", "", "different"),
                                     String.join(";", "contingency", "", "", "", "", "", "", "", "", "", "", "", actions.toString(), "", "different"));

        // precontingency violations results
        comparisonWriter.write(true, true, true);
        comparisonWriter.write(vlViolation, vlViolation, true);
        comparisonWriter.write(Collections.emptyList(), Collections.emptyList(), true);

        // missing postcontingency results
        comparisonWriter.setContingency("contingency");
        comparisonWriter.write(true, null, false);
        comparisonWriter.write(vlViolation, null, false);
        comparisonWriter.write(lineViolation, null, false);
        comparisonWriter.write(actions, null, false);

        assertEquals(content, writer.toString().trim());
    }

    @Test
    public void nullInput() {
        try {
            comparisonWriter.write((Boolean) null, null, true);
            fail();
        } catch (Exception ignored) {
        }
        try {
            comparisonWriter.write((LimitViolation) null, null, true);
            fail();
        } catch (Exception ignored) {
        }
        try {
            comparisonWriter.write((List<String>) null, null, true);
            fail();
        } catch (Exception ignored) {
        }
    }

}
