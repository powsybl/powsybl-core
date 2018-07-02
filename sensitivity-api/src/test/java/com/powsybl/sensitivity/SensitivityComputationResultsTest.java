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
public class SensitivityComputationResultsTest {

    private SensitivityFactor factorOk;
    private SensitivityFactor factorNok;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() {
        SensitivityVariable sensitivityVariable = Mockito.mock(SensitivityVariable.class);
        // Both factors hae same variable but different function
        factorOk = Mockito.mock(SensitivityFactor.class);
        Mockito.when(factorOk.getFunction()).thenReturn(Mockito.mock(SensitivityFunction.class));
        Mockito.when(factorOk.getVariable()).thenReturn(sensitivityVariable);
        factorNok = Mockito.mock(SensitivityFactor.class);
        Mockito.when(factorNok.getFunction()).thenReturn(Mockito.mock(SensitivityFunction.class));
        Mockito.when(factorNok.getVariable()).thenReturn(sensitivityVariable);
    }

    @Test
    public void isOk() {
        SensitivityComputationResults resultsOk = new SensitivityComputationResults(true, Collections.emptyMap(), "", Collections.emptyList());
        assertTrue(resultsOk.isOk());

        SensitivityComputationResults resultsNok = new SensitivityComputationResults(false, Collections.emptyMap(), "", Collections.emptyList());
        assertFalse(resultsNok.isOk());
    }

    @Test
    public void getMetrics() {
        Map<String, String > metrics = new HashMap<>();
        metrics.put("Key 1", "Val 1");
        metrics.put("Key 2", "Val 2");
        metrics.put("Key 3", "Val 3");
        SensitivityComputationResults results = new SensitivityComputationResults(true, metrics, "", Collections.emptyList());
        assertEquals(metrics.size(), results.getMetrics().size());
        assertEquals("Val 1", results.getMetrics().get("Key 1"));
        assertEquals("Val 2", results.getMetrics().get("Key 2"));
        assertEquals("Val 3", results.getMetrics().get("Key 3"));
    }

    @Test
    public void getLogs() {
        String logs = "I don't know half of you half as well as I should like; and I like less than half of you half as well as you deserve.";
        SensitivityComputationResults results = new SensitivityComputationResults(true, Collections.emptyMap(), logs, Collections.emptyList());
        assertEquals(logs, results.getLogs());
    }

    @Test
    public void createResultsWithNullValues() {
        exception.expect(NullPointerException.class);
        new SensitivityComputationResults(true, Collections.emptyMap(), "", null);
    }

    @Test
    public void getSensitivityValues() {
        List<SensitivityValue> values = new ArrayList<>();
        values.add(new SensitivityValue(factorOk, Double.NaN, Double.NaN, Double.NaN));
        values.add(new SensitivityValue(factorNok, Double.NaN, Double.NaN, Double.NaN));
        SensitivityComputationResults results = new SensitivityComputationResults(true, Collections.emptyMap(), "", values);
        assertEquals(values.size(), results.getSensitivityValues().size());
    }

    @Test
    public void getSensitivityValuesByFunction() {
        List<SensitivityValue> values = new ArrayList<>();
        values.add(new SensitivityValue(factorOk, Double.NaN, Double.NaN, Double.NaN));
        values.add(new SensitivityValue(factorNok, Double.NaN, Double.NaN, Double.NaN));
        SensitivityComputationResults results = new SensitivityComputationResults(true, Collections.emptyMap(), "", values);
        assertEquals(1, results.getSensitivityValuesByFunction(factorOk.getFunction()).size());
        assertEquals(1, results.getSensitivityValuesByFunction(factorNok.getFunction()).size());
    }

    @Test
    public void getSensitivityValuesByVariable() {
        List<SensitivityValue> values = new ArrayList<>();
        values.add(new SensitivityValue(factorOk, Double.NaN, Double.NaN, Double.NaN));
        values.add(new SensitivityValue(factorNok, Double.NaN, Double.NaN, Double.NaN));
        SensitivityComputationResults results = new SensitivityComputationResults(true, Collections.emptyMap(), "", values);
        assertEquals(2, results.getSensitivityValuesByVariable(factorOk.getVariable()).size());
        assertEquals(2, results.getSensitivityValuesByVariable(factorNok.getVariable()).size());
    }

    @Test
    public void getNonExistingSensitivityValueByFunctionAndVariable() {
        List<SensitivityValue> values = new ArrayList<>();
        values.add(new SensitivityValue(factorOk, Double.NaN, Double.NaN, Double.NaN));
        values.add(new SensitivityValue(factorNok, Double.NaN, Double.NaN, Double.NaN));
        SensitivityComputationResults results = new SensitivityComputationResults(true, Collections.emptyMap(), "", values);

        SensitivityFunction wrongFunction = Mockito.mock(SensitivityFunction.class);
        SensitivityVariable wrongVariable = Mockito.mock(SensitivityVariable.class);
        exception.expect(NoSuchElementException.class);
        results.getSensitivityValue(wrongFunction, wrongVariable);
    }

    @Test
    public void getNonExistingSensitivityValueByFactor() {
        List<SensitivityValue> values = new ArrayList<>();
        values.add(new SensitivityValue(factorOk, Double.NaN, Double.NaN, Double.NaN));
        values.add(new SensitivityValue(factorNok, Double.NaN, Double.NaN, Double.NaN));
        SensitivityComputationResults results = new SensitivityComputationResults(true, Collections.emptyMap(), "", values);

        SensitivityFactor wrongFactor = Mockito.mock(SensitivityFactor.class);
        Mockito.when(wrongFactor.getFunction()).thenReturn(Mockito.mock(SensitivityFunction.class));
        Mockito.when(wrongFactor.getVariable()).thenReturn(Mockito.mock(SensitivityVariable.class));
        exception.expect(NoSuchElementException.class);
        results.getSensitivityValue(wrongFactor);
    }

    @Test
    public void getSensitivityValue() {
        List<SensitivityValue> values = new ArrayList<>();
        values.add(new SensitivityValue(factorOk, Double.NaN, Double.NaN, Double.NaN));
        values.add(new SensitivityValue(factorNok, Double.NaN, Double.NaN, Double.NaN));
        SensitivityComputationResults results = new SensitivityComputationResults(true, Collections.emptyMap(), "", values);
        assertSame(factorOk, results.getSensitivityValue(factorOk.getFunction(), factorOk.getVariable()).getFactor());
        assertSame(factorNok, results.getSensitivityValue(factorNok.getFunction(), factorNok.getVariable()).getFactor());
        assertSame(factorOk, results.getSensitivityValue(factorOk).getFactor());
        assertSame(factorNok, results.getSensitivityValue(factorNok).getFactor());
    }
}
