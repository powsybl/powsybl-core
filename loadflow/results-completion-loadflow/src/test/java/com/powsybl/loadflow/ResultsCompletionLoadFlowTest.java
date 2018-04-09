/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow;

import org.junit.Test;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class ResultsCompletionLoadFlowTest extends AbstractResultsCompletionLoadFlowTest {

    @Test
    public void run() throws Exception {
        setNanValues();

        LoadFlowParameters parameters = new LoadFlowParameters();
        ResultsCompletionLoadFlowParametersExtension parametersExtension = new ResultsCompletionLoadFlowParametersExtension();
        parameters.addExtension(ResultsCompletionLoadFlowParametersExtension.class, parametersExtension);
        new ResultsCompletionLoadFlow(network).run(parameters);

        checkResultsCompletion();
    }

}
