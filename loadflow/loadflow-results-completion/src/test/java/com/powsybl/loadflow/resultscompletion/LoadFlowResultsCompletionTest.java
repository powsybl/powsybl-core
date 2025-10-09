/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.resultscompletion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.loadflow.validation.CandidateComputation;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
class LoadFlowResultsCompletionTest extends AbstractLoadFlowResultsCompletionTest {

    @Test
    void run() throws Exception {
        setNanValues();

        LoadFlowResultsCompletionParameters parameters = new LoadFlowResultsCompletionParameters();
        LoadFlowParameters lfParameters = new LoadFlowParameters();
        CandidateComputation computation = new LoadFlowResultsCompletion(parameters, lfParameters);
        assertEquals(LoadFlowResultsCompletion.NAME, computation.getName());
        computation.run(network, null);

        checkResultsCompletion();
    }

}
