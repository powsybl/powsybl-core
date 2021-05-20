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
import java.util.NoSuchElementException;

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
        SensitivityAnalysisResult resultsOk = new SensitivityAnalysisResult(true, Collections.emptyMap(), "", Collections.emptyList(), Collections.emptyMap());
        assertTrue(resultsOk.isOk());

        SensitivityAnalysisResult resultsNok = new SensitivityAnalysisResult(false, Collections.emptyMap(), "", Collections.emptyList(), Collections.emptyMap());
        assertFalse(resultsNok.isOk());
    }

    @Test
    public void getMetrics() {
        Map<String, String > metrics = new HashMap<>();
        metrics.put("Key 1", "Val 1");
        metrics.put("Key 2", "Val 2");
        metrics.put("Key 3", "Val 3");
        SensitivityAnalysisResult results = new SensitivityAnalysisResult(true, metrics, "", Collections.emptyList(), Collections.emptyMap());
        assertEquals(metrics.size(), results.getMetrics().size());
        assertEquals("Val 1", results.getMetrics().get("Key 1"));
        assertEquals("Val 2", results.getMetrics().get("Key 2"));
        assertEquals("Val 3", results.getMetrics().get("Key 3"));
    }

    @Test
    public void getLogs() {
        String logs = "I don't know half of you half as well as I should like; and I like less than half of you half as well as you deserve.";
        SensitivityAnalysisResult results = new SensitivityAnalysisResult(true, Collections.emptyMap(), logs, Collections.emptyList(), Collections.emptyMap());
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
        SensitivityAnalysisResult results = new SensitivityAnalysisResult(true, Collections.emptyMap(), "", values, Collections.emptyMap());
        assertEquals(values.size(), results.getSensitivityValues().size());
    }

    @Test
    public void getSensitivityValuesContingency() {
        List<SensitivityValue> values = new ArrayList<>();
        values.add(new SensitivityValue(factorOk, "", Double.NaN, Double.NaN));
        values.add(new SensitivityValue(factorNok, "", Double.NaN, Double.NaN));
        Map<String, List<SensitivityValue>> valuesContingency = Collections.singletonMap("Contingency", values);
        SensitivityAnalysisResult results = new SensitivityAnalysisResult(true, Collections.emptyMap(), "", values, valuesContingency);
        assertEquals(valuesContingency.get("Contingency").size(), results.getSensitivityValuesContingencies().get("Contingency").size());
    }

    @Test
    public void getSensitivityValuesByFunction() {
        List<SensitivityValue> values = new ArrayList<>();
        values.add(new SensitivityValue(factorOk, "", Double.NaN, Double.NaN));
        values.add(new SensitivityValue(factorNok, "", Double.NaN, Double.NaN));
        SensitivityAnalysisResult results = new SensitivityAnalysisResult(true, Collections.emptyMap(), "", values, Collections.emptyMap());
        assertEquals(1, results.getSensitivityValuesByFunction(factorOk.getFunctionType()).size());
        assertEquals(1, results.getSensitivityValuesByFunction(factorNok.getFunctionType()).size());
    }

    @Test
    public void getSensitivityValuesByFunctionContingency() {
        List<SensitivityValue> values = new ArrayList<>();
        values.add(new SensitivityValue(factorOk, "", Double.NaN, Double.NaN));
        values.add(new SensitivityValue(factorNok, "", Double.NaN, Double.NaN));
        Map<String, List<SensitivityValue>> valuesContingency = Collections.singletonMap("Contingency", values);
        SensitivityAnalysisResult results = new SensitivityAnalysisResult(true, Collections.emptyMap(), "", values, valuesContingency);
        assertEquals(1, results.getSensitivityValuesByFunction(factorOk.getFunctionType(), "Contingency").size());
        assertEquals(1, results.getSensitivityValuesByFunction(factorNok.getFunctionType(), "Contingency").size());
    }

    @Test
    public void getSensitivityValuesByVariable() {
        List<SensitivityValue> values = new ArrayList<>();
        values.add(new SensitivityValue(factorOk, "", Double.NaN, Double.NaN));
        values.add(new SensitivityValue(factorNok, "", Double.NaN, Double.NaN));
        SensitivityAnalysisResult results = new SensitivityAnalysisResult(true, Collections.emptyMap(), "", values, Collections.emptyMap());
        assertEquals(2, results.getSensitivityValuesByVariable(factorOk.getVariableType()).size());
        assertEquals(2, results.getSensitivityValuesByVariable(factorNok.getVariableType()).size());
    }

    @Test
    public void getSensitivityValuesByVariableContingency() {
        List<SensitivityValue> values = new ArrayList<>();
        values.add(new SensitivityValue(factorOk, "", Double.NaN, Double.NaN));
        values.add(new SensitivityValue(factorNok, "", Double.NaN, Double.NaN));
        Map<String, List<SensitivityValue>> valuesContingency = Collections.singletonMap("Contingency", values);
        SensitivityAnalysisResult results = new SensitivityAnalysisResult(true, Collections.emptyMap(), "", values, valuesContingency);
        assertEquals(2, results.getSensitivityValuesByVariable(factorOk.getVariableType(), "Contingency").size());
        assertEquals(2, results.getSensitivityValuesByVariable(factorNok.getVariableType(), "Contingency").size());
    }

    @Test
    public void getNonExistingSensitivityValueByFunctionAndVariable() {
        List<SensitivityValue> values = new ArrayList<>();
        values.add(new SensitivityValue(factorOk, "", Double.NaN, Double.NaN));
        values.add(new SensitivityValue(factorNok, "", Double.NaN, Double.NaN));
        SensitivityAnalysisResult results = new SensitivityAnalysisResult(true, Collections.emptyMap(), "", values, Collections.emptyMap());

        exception.expect(NoSuchElementException.class);
        results.getSensitivityValue(SensitivityFunctionType.BRANCH_ACTIVE_POWER, SensitivityVariableType.TRANSFORMER_PHASE);
    }

    @Test
    public void getNonExistingSensitivityValueByFunctionAndVariableContingency() {
        List<SensitivityValue> values = new ArrayList<>();
        values.add(new SensitivityValue(factorOk, "", Double.NaN, Double.NaN));
        values.add(new SensitivityValue(factorNok, "", Double.NaN, Double.NaN));
        Map<String, List<SensitivityValue>> valuesContingency = Collections.singletonMap("Contingency", values);
        SensitivityAnalysisResult results = new SensitivityAnalysisResult(true, Collections.emptyMap(), "", values, valuesContingency);

        exception.expect(NoSuchElementException.class);
        results.getSensitivityValue(SensitivityFunctionType.BRANCH_ACTIVE_POWER, SensitivityVariableType.TRANSFORMER_PHASE, "Contingency");
    }

    @Test
    public void getNonExistingSensitivityValueByFactor() {
        List<SensitivityValue> values = new ArrayList<>();
        values.add(new SensitivityValue(factorOk, "", Double.NaN, Double.NaN));
        values.add(new SensitivityValue(factorNok, "", Double.NaN, Double.NaN));
        SensitivityAnalysisResult results = new SensitivityAnalysisResult(true, Collections.emptyMap(), "", values, Collections.emptyMap());

        SensitivityFactor wrongFactor = Mockito.mock(SensitivityFactor.class);
        Mockito.when(wrongFactor.getFunctionType()).thenReturn(SensitivityFunctionType.BRANCH_ACTIVE_POWER);
        Mockito.when(wrongFactor.getVariableType()).thenReturn(SensitivityVariableType.TRANSFORMER_PHASE);
        exception.expect(NoSuchElementException.class);
        results.getSensitivityValue(wrongFactor);
    }

    @Test
    public void getNonExistingSensitivityValueByFactorContingency() {
        List<SensitivityValue> values = new ArrayList<>();
        values.add(new SensitivityValue(factorOk, "", Double.NaN, Double.NaN));
        values.add(new SensitivityValue(factorNok, "", Double.NaN, Double.NaN));
        Map<String, List<SensitivityValue>> valuesContingency = Collections.singletonMap("Contingency", values);
        SensitivityAnalysisResult results = new SensitivityAnalysisResult(true, Collections.emptyMap(), "", values, valuesContingency);

        SensitivityFactor wrongFactor = Mockito.mock(SensitivityFactor.class);
        Mockito.when(wrongFactor.getFunctionType()).thenReturn(SensitivityFunctionType.BRANCH_ACTIVE_POWER);
        Mockito.when(wrongFactor.getVariableType()).thenReturn(SensitivityVariableType.TRANSFORMER_PHASE);
        exception.expect(NoSuchElementException.class);
        results.getSensitivityValue(wrongFactor, "Contingency");
    }

    @Test
    public void getSensitivityValue() {
        List<SensitivityValue> values = new ArrayList<>();
        values.add(new SensitivityValue(factorOk, "", Double.NaN, Double.NaN));
        values.add(new SensitivityValue(factorNok, "", Double.NaN, Double.NaN));
        SensitivityAnalysisResult results = new SensitivityAnalysisResult(true, Collections.emptyMap(), "", values, Collections.emptyMap());
        assertSame(factorOk, results.getSensitivityValue(factorOk.getFunctionType(), factorOk.getVariableType()).getFactor());
        assertSame(factorNok, results.getSensitivityValue(factorNok.getFunctionType(), factorNok.getVariableType()).getFactor());
        assertSame(factorOk, results.getSensitivityValue(factorOk).getFactor());
        assertSame(factorNok, results.getSensitivityValue(factorNok).getFactor());
    }

    @Test
    public void getSensitivityValueContingency() {
        List<SensitivityValue> values = new ArrayList<>();
        values.add(new SensitivityValue(factorOk, "", Double.NaN, Double.NaN));
        values.add(new SensitivityValue(factorNok, "", Double.NaN, Double.NaN));
        Map<String, List<SensitivityValue>> valuesContingency = Collections.singletonMap("Contingency", values);
        SensitivityAnalysisResult results = new SensitivityAnalysisResult(true, Collections.emptyMap(), "", values, valuesContingency);
        assertSame(factorOk, results.getSensitivityValue(factorOk.getFunctionType(), factorOk.getVariableType(), "Contingency").getFactor());
        assertSame(factorNok, results.getSensitivityValue(factorNok.getFunctionType(), factorNok.getVariableType(), "Contingency").getFactor());
        assertSame(factorOk, results.getSensitivityValue(factorOk, "Contingency").getFactor());
        assertSame(factorNok, results.getSensitivityValue(factorNok, "Contingency").getFactor());
    }

    @Test
    public void shortConstructor() {
        SensitivityAnalysisResult result = new SensitivityAnalysisResult(true, new HashMap<>(),
            "fake logs", new ArrayList<>());
        assertTrue(result.getSensitivityValuesContingencies().isEmpty());
    }

    @Test
    public void emptyMethod() {
        SensitivityAnalysisResult result = SensitivityAnalysisResult.empty();
        assertFalse(result.isOk());
        assertTrue(result.getMetrics().isEmpty());
        assertTrue(result.getLogs().isEmpty());
        assertTrue(result.getSensitivityValues().isEmpty());
        assertTrue(result.getSensitivityValuesContingencies().isEmpty());
    }
}
