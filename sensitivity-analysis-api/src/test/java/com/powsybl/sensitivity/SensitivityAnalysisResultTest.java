/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class SensitivityAnalysisResultTest {

    private SensitivityFactor factorOk;
    private SensitivityFactor factorNok;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() {
        SensitivityVariableType sensitivityVariableType = SensitivityVariableType.BUS_TARGET_VOLTAGE;
        String sensitivityVariableId = "Variable Id";
        // Both factors have same variable but different function
        factorOk = Mockito.mock(SensitivityFactor.class);
        Mockito.when(factorOk.getFunctionType()).thenReturn(SensitivityFunctionType.BUS_VOLTAGE);
        Mockito.when(factorOk.getFunctionId()).thenReturn("Function OK");
        Mockito.when(factorOk.getVariableType()).thenReturn(sensitivityVariableType);
        Mockito.when(factorOk.getVariableId()).thenReturn(sensitivityVariableId);
        factorNok = Mockito.mock(SensitivityFactor.class);
        Mockito.when(factorNok.getFunctionType()).thenReturn(SensitivityFunctionType.BRANCH_CURRENT);
        Mockito.when(factorNok.getFunctionId()).thenReturn("Function NOK");
        Mockito.when(factorNok.getVariableType()).thenReturn(sensitivityVariableType);
        Mockito.when(factorNok.getVariableId()).thenReturn(sensitivityVariableId);
    }

    @Test
    public void isOk() {
        SensitivityAnalysisResult resultsOk = new SensitivityAnalysisResult(true, Collections.emptyMap(), "", Collections.emptyList());
        assertTrue(resultsOk.isOk());

        SensitivityAnalysisResult resultsNok = new SensitivityAnalysisResult(false, Collections.emptyMap(), "", Collections.emptyList());
        assertFalse(resultsNok.isOk());
    }

    @Test
    public void getMetrics() {
        Map<String, String > metrics = new HashMap<>();
        metrics.put("Key 1", "Val 1");
        metrics.put("Key 2", "Val 2");
        metrics.put("Key 3", "Val 3");
        SensitivityAnalysisResult results = new SensitivityAnalysisResult(true, metrics, "", Collections.emptyList());
        assertEquals(metrics.size(), results.getMetrics().size());
        assertEquals("Val 1", results.getMetrics().get("Key 1"));
        assertEquals("Val 2", results.getMetrics().get("Key 2"));
        assertEquals("Val 3", results.getMetrics().get("Key 3"));
    }

    @Test
    public void getLogs() {
        String logs = "I don't know half of you half as well as I should like; and I like less than half of you half as well as you deserve.";
        SensitivityAnalysisResult results = new SensitivityAnalysisResult(true, Collections.emptyMap(), logs, Collections.emptyList());
        assertEquals(logs, results.getLogs());
    }

    @Test
    public void createResultsWithNullValues() {
        exception.expect(NullPointerException.class);
        new SensitivityAnalysisResult(true, Collections.emptyMap(), "", null);
    }

    @Test
    public void getSensitivityValues() {
        List<SensitivityValue> values = new ArrayList<>();
        values.add(new SensitivityValue(factorOk, "", Double.NaN, Double.NaN));
        values.add(new SensitivityValue(factorNok, "", Double.NaN, Double.NaN));
        SensitivityAnalysisResult results = new SensitivityAnalysisResult(true, Collections.emptyMap(), "", values);
        assertEquals(values.size(), results.getValues().size());
    }

    @Test
    public void getPreContingencySensitivityValues() {
        List<SensitivityValue> values = new ArrayList<>();
        values.add(new SensitivityValue(factorOk, null, Double.NaN, Double.NaN));
        values.add(new SensitivityValue(factorNok, null, Double.NaN, Double.NaN));
        SensitivityAnalysisResult results = new SensitivityAnalysisResult(true, Collections.emptyMap(), "", values);
        assertEquals(values.size(), results.getPreContingencyValues().size());
    }

    @Test
    public void getSensitivityValuesContingency() {
        List<SensitivityValue> values = new ArrayList<>();
        values.add(new SensitivityValue(factorOk, "Contingency", Double.NaN, Double.NaN));
        values.add(new SensitivityValue(factorNok, "Contingency", Double.NaN, Double.NaN));
        SensitivityAnalysisResult results = new SensitivityAnalysisResult(true, Collections.emptyMap(), "", values);
        assertEquals(2, results.getValues("Contingency").size());
    }

    @Test
    public void getSensitivityValue() {
        List<SensitivityValue> values = new ArrayList<>();
        values.add(new SensitivityValue(factorOk, "ContingencyOk", Double.NaN, Double.NaN));
        values.add(new SensitivityValue(factorNok, "ContingencyNok", Double.NaN, Double.NaN));
        SensitivityAnalysisResult results = new SensitivityAnalysisResult(true, Collections.emptyMap(), "", values);
        assertSame(factorOk, results.getValue("ContingencyOk", factorOk.getFunctionId(), factorOk.getVariableId()).getFactor());
        assertSame(factorNok, results.getValue("ContingencyNok", factorNok.getFunctionId(), factorNok.getVariableId()).getFactor());
    }

    @Test
    public void emptyMethod() {
        SensitivityAnalysisResult result = SensitivityAnalysisResult.empty();
        assertFalse(result.isOk());
        assertTrue(result.getMetrics().isEmpty());
        assertTrue(result.getLogs().isEmpty());
        assertTrue(result.getValues().isEmpty());
    }
}
