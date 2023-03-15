/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ampl.executor;

import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.ExecutionEnvironment;
import com.powsybl.iidm.network.Network;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

/**
 * @author Nicolas Pierre <nicolas.pierre@artelys.com>
 */
public final class AmplModelRunner {
    private AmplModelRunner() {
    }

    public static AmplResults run(Network network, String variantId, AmplModel model, ComputationManager manager,
                                  AmplParameters parameters) {
        ExecutionEnvironment env = new ExecutionEnvironment(Collections.emptyMap(), "ampl_", true);
        AmplModelExecutionHandler handler = new AmplModelExecutionHandler(model, network, variantId, AmplConfig.load(),
                parameters);
        CompletableFuture<AmplResults> result = manager.execute(env, handler);
        return result.join();
    }

}
