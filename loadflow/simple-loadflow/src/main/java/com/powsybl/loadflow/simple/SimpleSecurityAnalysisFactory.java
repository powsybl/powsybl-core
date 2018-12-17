/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.simple;

import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.*;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class SimpleSecurityAnalysisFactory implements SecurityAnalysisFactory {

    @Override
    public SecurityAnalysis create(Network network, ComputationManager computationManager, int priority) {
        return create(network, new LimitViolationFilter(), computationManager, priority);
    }

    @Override
    public SecurityAnalysis create(Network network, LimitViolationFilter filter, ComputationManager computationManager, int priority) {
        return create(network, new DefaultLimitViolationDetector(), filter, computationManager, priority);
    }

    @Override
    public SecurityAnalysis create(Network network, LimitViolationDetector detector, LimitViolationFilter filter,
                                   ComputationManager computationManager, int priority) {
        return new SimpleSecurityAnalysis(network, detector, filter);
    }
}
