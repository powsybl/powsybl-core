/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.distributed;

import com.powsybl.computation.Partition;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.contingency.SubContingenciesProvider;
import com.powsybl.security.SecurityAnalysis;
import com.powsybl.security.SecurityAnalysisParameters;
import com.powsybl.security.SecurityAnalysisResult;
import com.powsybl.security.preprocessor.SecurityAnalysisPreprocessors;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * An implementation of {@link SecurityAnalysis} which executes only
 * the specified {@link Partition} of a security analysis.
 *
 * @deprecated seems a more clear design to use a preprocessing of inputs instead,
 *             as this class does not actually implement any computation.
 *             See {@link SecurityAnalysisPreprocessors#makeSubtask SecurityAnalysisConfigurers.makeSubtask}
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
@Deprecated
public class SecurityAnalysisTask implements SecurityAnalysis {

    private SecurityAnalysis delegate;
    private Partition partition;

    /**
     * @param delegate  The security analysis implementation which will actually perform the computation
     * @param partition The part of the computation to be performed by this task
     */
    public SecurityAnalysisTask(SecurityAnalysis delegate, Partition partition) {
        this.delegate = Objects.requireNonNull(delegate);
        this.partition = Objects.requireNonNull(partition);
    }

    @Override
    public void addInterceptor(SecurityAnalysisInterceptor interceptor) {
        delegate.addInterceptor(interceptor);
    }

    @Override
    public boolean removeInterceptor(SecurityAnalysisInterceptor interceptor) {
        return delegate.removeInterceptor(interceptor);
    }

    @Override
    public CompletableFuture<SecurityAnalysisResult> run(String workingStateId, SecurityAnalysisParameters parameters, ContingenciesProvider contingenciesProvider) {
        ContingenciesProvider subTaskContingenciesProvider = new SubContingenciesProvider(contingenciesProvider, partition);
        return delegate.run(workingStateId, parameters, subTaskContingenciesProvider);
    }
}
