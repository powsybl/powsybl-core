/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow;

import static org.junit.Assert.assertEquals;

import java.util.stream.Stream;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.powsybl.iidm.network.Network;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class ResultsCompletionLoadFlowTest extends AbstractResultsCompletionLoadFlowTest {

    @Test
    public void run() throws Exception {
        Mockito.when(lineTerminal1.getP()).thenReturn(Float.NaN);
        Mockito.when(lineTerminal1.getQ()).thenReturn(Float.NaN);
        Mockito.when(twtTerminal1.getP()).thenReturn(Float.NaN);
        Mockito.when(twtTerminal1.getQ()).thenReturn(Float.NaN);

        Network network = Mockito.mock(Network.class);
        Mockito.when(network.getId()).thenReturn("network");
        Mockito.when(network.getLineStream()).thenAnswer(dummy -> Stream.of(line));
        Mockito.when(network.getTwoWindingsTransformerStream()).thenAnswer(dummy -> Stream.of(transformer));

        LoadFlowParameters parameters = new LoadFlowParameters();
        ResultsCompletionLoadFlowParametersExtension parametersExtension = new ResultsCompletionLoadFlowParametersExtension();
        parameters.addExtension(ResultsCompletionLoadFlowParametersExtension.class, parametersExtension);
        new ResultsCompletionLoadFlow(network).run(parameters);

        ArgumentCaptor<Float> setterCaptor = ArgumentCaptor.forClass(Float.class);
        Mockito.verify(lineTerminal1, Mockito.times(1)).setP(setterCaptor.capture());
        assertEquals(lineP1, setterCaptor.getValue(), 0001f);
        Mockito.verify(lineTerminal1, Mockito.times(1)).setQ(setterCaptor.capture());
        assertEquals(lineQ1, setterCaptor.getValue(), 0001f);
        Mockito.verify(lineTerminal2, Mockito.times(0)).setP(Matchers.anyFloat());
        Mockito.verify(lineTerminal2, Mockito.times(0)).setQ(Matchers.anyFloat());
        Mockito.verify(twtTerminal1, Mockito.times(1)).setP(setterCaptor.capture());
        assertEquals(twtP1, setterCaptor.getValue(), 0001f);
        Mockito.verify(twtTerminal1, Mockito.times(1)).setQ(setterCaptor.capture());
        assertEquals(twtQ1, setterCaptor.getValue(), 0001f);
        Mockito.verify(twtTerminal2, Mockito.times(0)).setP(Matchers.anyFloat());
        Mockito.verify(twtTerminal2, Mockito.times(0)).setQ(Matchers.anyFloat());
    }

}
