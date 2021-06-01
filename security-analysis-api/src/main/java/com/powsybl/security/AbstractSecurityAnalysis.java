/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.powsybl.iidm.network.Network;
import com.powsybl.security.detectors.DefaultLimitViolationDetector;
import com.powsybl.security.interceptors.RunningContext;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

/**
 *
 * Implements some common methods of interface {@link SecurityAnalysis},
 * and provides a {@link SecurityAnalysisResultBuilder} to ease creation of results.
 *
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 */
public abstract class AbstractSecurityAnalysis implements SecurityAnalysis {

    protected final Network network;
    protected final LimitViolationDetector violationDetector;
    protected final LimitViolationFilter violationFilter;

    protected final List<SecurityAnalysisInterceptor> interceptors;

    protected AbstractSecurityAnalysis(Network network, LimitViolationFilter violationFilter) {
        this(network, new DefaultLimitViolationDetector(EnumSet.allOf(DefaultLimitViolationDetector.CurrentLimitType.class)), violationFilter);
    }

    protected AbstractSecurityAnalysis(Network network, LimitViolationDetector detector, LimitViolationFilter filter) {
        this.network = Objects.requireNonNull(network);
        this.violationDetector = Objects.requireNonNull(detector);
        this.violationFilter = Objects.requireNonNull(filter);
        this.interceptors = new ArrayList<>();
    }

    @Override
    public void addInterceptor(SecurityAnalysisInterceptor interceptor) {
        interceptors.add(Objects.requireNonNull(interceptor));
    }

    @Override
    public boolean removeInterceptor(SecurityAnalysisInterceptor interceptor) {
        return interceptors.remove(interceptor);
    }

    protected SecurityAnalysisResultBuilder createResultBuilder(String initialWorkingStateId) {
        return new SecurityAnalysisResultBuilder(violationFilter, new RunningContext(network, initialWorkingStateId), interceptors);
    }
}
