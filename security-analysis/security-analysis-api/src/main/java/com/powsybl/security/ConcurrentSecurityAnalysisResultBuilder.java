/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.powsybl.contingency.Contingency;
import com.powsybl.security.interceptors.RunningContext;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;

import java.util.Collection;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class ConcurrentSecurityAnalysisResultBuilder extends SecurityAnalysisResultBuilder {

    private final ReentrantLock lock = new ReentrantLock();

    public ConcurrentSecurityAnalysisResultBuilder(LimitViolationFilter filter, RunningContext context, Collection<SecurityAnalysisInterceptor> interceptors) {
        super(filter, context, interceptors);
    }

    public ConcurrentSecurityAnalysisResultBuilder(LimitViolationFilter filter, RunningContext context) {
        super(filter, context);
    }

    @Override
    public SecurityAnalysisResultBuilder contingency(Contingency contingency) {
        lock.lock();
        return super.contingency(contingency);
    }

    @Override
    public SecurityAnalysisResultBuilder preContingency() {
        lock.lock();
        return super.preContingency();
    }

    @Override
    public SecurityAnalysisResultBuilder endPreContingency() {
        try {
            return super.endPreContingency();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public SecurityAnalysisResultBuilder endContingency() {
        try {
            return super.endContingency();
        } finally {
            lock.unlock();
        }
    }

}
