/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.detectors.DefaultLimitViolationDetector;

/**
 *
 * TODO: deprecate priority parameter
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface SecurityAnalysisFactory {

    /**
     * Creates a {@link SecurityAnalysis} for specified {@link Network},
     * using specified {@link ComputationManager} to handle computations.
     * Limit violations should be detected using {@link DefaultLimitViolationDetector},
     * and then filtered using a default {@link LimitViolationFilter}.
     *
     * @param network            The network for which computation will be performed.
     * @param computationManager The computation manager to use to handle computations
     * @return                   The created security analysis.
     */
    SecurityAnalysis create(Network network, ComputationManager computationManager, int priority);

    /**
     * Creates a {@link SecurityAnalysis} for specified {@link Network},
     * using specified {@link ComputationManager} to handle computations.
     * Limit violations should be detected using {@link DefaultLimitViolationDetector},
     * and then filtered using the specified {@link LimitViolationFilter}.
     *
     * @param network            The network for which computation will be performed.
     * @param computationManager The computation manager to use to handle computations
     * @param filter             A filter to filter out unwanted limit violations.
     * @return                   The created security analysis.
     */
    default SecurityAnalysis create(Network network, LimitViolationFilter filter, ComputationManager computationManager, int priority) {
        return create(network, computationManager, priority);
    }

    /**
     * Creates a {@link SecurityAnalysis} for specified {@link Network},
     * using specified {@link ComputationManager} to handle computations.
     * Limit violations will be detected using the specified {@link LimitViolationDetector},
     * and then filtered using the specified {@link LimitViolationFilter}.
     *
     * @param network            The network for which computation will be performed.
     * @param detector           The detector used to evaluate the occurence of limit violations.
     * @param filter             A filter to filter out unwanted limit violations.
     * @param computationManager The computation manager to use to handle computations
     * @return                   The created security analysis.
     */
    default SecurityAnalysis create(Network network, LimitViolationDetector detector, LimitViolationFilter filter,
                                    ComputationManager computationManager, int priority) {
        return create(network, filter, computationManager, priority);
    }

}
