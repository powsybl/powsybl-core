/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.validation;

import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;

/**
 * A computation carried out on a network, which result
 * may be validated through the validation tool.
 *
 * All computation tools which want to pass the validation tests
 * must implement that interface and register itself as a known
 * candidate through the use of @AutoService.
 *
 * Must be thread-safe.
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public interface CandidateComputation {

    /**
     * A name which uniquely identifies that computation.
     */
    String getName();

    /**
     * A computation carried out on the {@param network}.
     */
    void run(Network network, ComputationManager computationManager);

}
