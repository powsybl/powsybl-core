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
public class LoadFlowResultsCompletionTest extends AbstractLoadFlowResultsCompletionTest {

    @Test
    public void run() throws Exception {
        setNanValues();

        LoadFlowResultsCompletionParameters parameters = new LoadFlowResultsCompletionParameters();
        LoadFlowParameters lfParameters = new LoadFlowParameters();
        new LoadFlowResultsCompletion(parameters, lfParameters).run(network, null);

        checkResultsCompletion();
    }

}
