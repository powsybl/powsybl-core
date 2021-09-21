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
import java.util.List;

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
    public void createResultsWithNullValues() {
        exception.expect(NullPointerException.class);
        new SensitivityAnalysisResult(null);
    }

    @Test
    public void getSensitivityValues() {
        List<SensitivityValue> values = new ArrayList<>();
        values.add(new SensitivityValue(factorOk, "", Double.NaN, Double.NaN));
        values.add(new SensitivityValue(factorNok, "", Double.NaN, Double.NaN));
        SensitivityAnalysisResult results = new SensitivityAnalysisResult(values);
        assertEquals(values.size(), results.getValues().size());
    }

    @Test
    public void getPreContingencySensitivityValues() {
        List<SensitivityValue> values = new ArrayList<>();
        values.add(new SensitivityValue(factorOk, null, Double.NaN, Double.NaN));
        values.add(new SensitivityValue(factorNok, null, Double.NaN, Double.NaN));
        SensitivityAnalysisResult results = new SensitivityAnalysisResult(values);
        assertEquals(values.size(), results.getPreContingencyValues().size());
    }

    @Test
    public void getSensitivityValuesContingency() {
        List<SensitivityValue> values = new ArrayList<>();
        values.add(new SensitivityValue(factorOk, "Contingency", Double.NaN, Double.NaN));
        values.add(new SensitivityValue(factorNok, "Contingency", Double.NaN, Double.NaN));
        SensitivityAnalysisResult results = new SensitivityAnalysisResult(values);
        assertEquals(2, results.getValues("Contingency").size());
    }

    @Test
    public void getSensitivityValue() {
        List<SensitivityValue> values = new ArrayList<>();
        values.add(new SensitivityValue(factorOk, "ContingencyOk", Double.NaN, 1.0));
        values.add(new SensitivityValue(factorNok, "ContingencyNok", Double.NaN, 2.0));
        SensitivityAnalysisResult results = new SensitivityAnalysisResult(values);
        assertSame(factorOk, results.getValue("ContingencyOk", factorOk.getFunctionId(), factorOk.getVariableId()).getFactor());
        assertSame(factorNok, results.getValue("ContingencyNok", factorNok.getFunctionId(), factorNok.getVariableId()).getFactor());
        assertEquals(1.0, results.getFunctionReferenceValue("ContingencyOk", factorOk.getFunctionId()), 0.01);
        assertEquals(2.0, results.getFunctionReferenceValue("ContingencyNok", factorNok.getFunctionId()), 0.01);
    }

    @Test
    public void emptyMethod() {
        SensitivityAnalysisResult result = SensitivityAnalysisResult.empty();
        assertTrue(result.getValues().isEmpty());
    }
}
