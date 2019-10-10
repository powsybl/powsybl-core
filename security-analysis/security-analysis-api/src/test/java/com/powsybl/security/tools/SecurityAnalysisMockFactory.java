/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.tools;

import com.powsybl.commons.PowsyblException;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.LimitViolationDetector;
import com.powsybl.security.LimitViolationFilter;
import com.powsybl.security.SecurityAnalysis;
import com.powsybl.security.SecurityAnalysisFactory;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
@Deprecated
public class SecurityAnalysisMockFactory implements SecurityAnalysisFactory {

    private SecurityAnalysis mock;

    private final boolean failed;

    SecurityAnalysisMockFactory() {
        this(false);
    }

    SecurityAnalysisMockFactory(boolean failed) {
        this.failed = failed;
    }

    @Override
    public SecurityAnalysis create(Network network, ComputationManager computationManager, int priority) {
        return mock == null ? mockSa() : mock;
    }

    @Override
    public SecurityAnalysis create(Network network, LimitViolationFilter filter, ComputationManager computationManager, int priority) {
        return mock == null ? mockSa() : mock;
    }

    @Override
    public SecurityAnalysis create(Network network, LimitViolationDetector detector, LimitViolationFilter filter, ComputationManager computationManager, int priority) {
        return mock == null ? mockSa() : mock;
    }

    private SecurityAnalysis mockSa() {
        throw new PowsyblException("Deprecated");
    }
}
