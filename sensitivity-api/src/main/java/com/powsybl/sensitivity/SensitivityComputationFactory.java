/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;

/**
 * Sensitivity computation factory
 * <p>
 *     Factory class for sensitivity computation instances
 * </p>
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 * @see SensitivityComputation
 */
public interface SensitivityComputationFactory {

    /**
     * Creates a sensitivity computation instance
     *
     * @param network IIDM input network on which the sensitivity computation will be done
     * @param computationManager Managing interface to deal with the computation
     * @param priority Priority of the computation task (used for task ordering)
     * @return the created sensitivity computation instance
     */
    SensitivityComputation create(Network network, ComputationManager computationManager, int priority);

}
