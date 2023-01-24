/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ampl.executor;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.ExecutionEnvironment;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.network.Importers;
import com.powsybl.iidm.network.Network;

public final class AmplModelRunner {
    private AmplModelRunner() {
    }

    public static AmplResults run(Network network, String variantId, IAmplModel model, ComputationManager manager) {
        ExecutionEnvironment env = new ExecutionEnvironment(Collections.emptyMap(), "ampl_", true);
        AmplModelExecutionHandler handler = new AmplModelExecutionHandler(model, network, variantId);
        CompletableFuture<AmplResults> result = manager.execute(env, handler);
        return result.join();
    }

    public static void main(String[] args) throws Exception {
        Network network = Importers.importData("XIIDM", "./", "ieee14", new Properties());
        String variantId = network.getVariantManager().getWorkingVariantId();
        AmplModel reactiveOpf = AmplModel.REACTIVE_OPF;
        ComputationManager manager = LocalComputationManager.getDefault();
        AmplModelRunner.run(network, variantId, reactiveOpf, manager);
    }
}
