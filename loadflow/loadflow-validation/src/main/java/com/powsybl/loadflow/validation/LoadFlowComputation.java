/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.validation;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VariantManagerConstants;
import com.powsybl.loadflow.LoadFlow;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.loadflow.LoadFlowResult;

import java.util.Objects;

/**
 * Load flow as a computation candidate for validation.
 *
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
@AutoService(CandidateComputation.class)
public class LoadFlowComputation implements CandidateComputation {

    @Override
    public String getName() {
        return "loadflow";
    }

    @Override
    public void run(Network network, ComputationManager computationManager) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(computationManager);

        LoadFlowParameters parameters = LoadFlowParameters.load();
        String loadFlowName = ValidationConfig.load().getLoadFlowName().orElse(null);
        LoadFlowResult result = LoadFlow.find(loadFlowName)
                                        .run(network, VariantManagerConstants.INITIAL_VARIANT_ID, computationManager, parameters);
        if (!result.isOk()) {
            throw new PowsyblException("Loadflow on network " + network.getId() + " does not converge");
        }
    }
}
