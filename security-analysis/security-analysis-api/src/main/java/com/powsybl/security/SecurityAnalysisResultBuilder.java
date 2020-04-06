/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.google.common.collect.ImmutableList;
import com.powsybl.contingency.Contingency;
import com.powsybl.security.interceptors.RunningContext;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;

import java.util.*;

/**
 *
 * Facilitates the creation of security analysis results, in particular
 * for subclasses of {@link AbstractSecurityAnalysis}.
 *
 * Encapsulates filtering of limit violations with a provided {@link LimitViolationFilter},
 * as well as notifications to {@link SecurityAnalysisInterceptor}s.
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class SecurityAnalysisResultBuilder {

    private final LimitViolationFilter filter;
    private final RunningContext context;
    private final List<SecurityAnalysisInterceptor> interceptors;

    // Below are volatile objects used for building the actual complete result
    private LimitViolationsResult preContingencyResult;
    private List<PostContingencyResult> postContingencyResults;

    private ResultBuilder currentBuilder = null;

    public SecurityAnalysisResultBuilder(LimitViolationFilter filter, RunningContext context,
                                         Collection<SecurityAnalysisInterceptor> interceptors) {
        this.filter = Objects.requireNonNull(filter);
        this.context = Objects.requireNonNull(context);
        this.interceptors = ImmutableList.copyOf(interceptors);
        this.postContingencyResults = new ArrayList<>();
    }

    public SecurityAnalysisResultBuilder(LimitViolationFilter filter, RunningContext context) {
        this(filter, context, Collections.emptyList());
    }

    /**
     * Initiates the creation of the result for one {@link Contingency}.
     * @param contingency  the contingency for which a result should be created
     * @return             this SecurityAnalysisResultBuilder instance
     */
    public SecurityAnalysisResultBuilder contingency(Contingency contingency) {
        if (currentBuilder != null) {
            throw new IllegalStateException("Cannot start a new post-contingency result");
        }
        currentBuilder = new PostContingencyResultBuilder(contingency);
        return this;
    }

    /**
     * Initiates the creation of the result for N situation.
     * @return this SecurityAnalysisResultBuilder instance.
     */
    public SecurityAnalysisResultBuilder preContingency() {
        if (currentBuilder != null) {
            throw new IllegalStateException("Cannot start a new pre-contingency result");
        }

        currentBuilder = new PreContingencyResultBuilder();
        return this;
    }

    /**
     * Finalize the creation of the PreContingencyResult instance
     * @return this SecurityAnalysisResultBuilder instance.
     */
    public SecurityAnalysisResultBuilder endPreContingency() {
        if (currentBuilder instanceof PreContingencyResultBuilder) {
            ((PreContingencyResultBuilder) currentBuilder).endPreContingency();
            currentBuilder = null;
            return this;
        }

        throw new IllegalStateException("Cannot create the pre-contingency result");
    }

    /**
     * Finalize the creation of the PostContingencyResult instance
     * @return this SecurityAnalysisResultBuilder instance.
     */
    public SecurityAnalysisResultBuilder endContingency() {
        if (currentBuilder instanceof PostContingencyResultBuilder) {
            ((PostContingencyResultBuilder) currentBuilder).endContingency();
            currentBuilder = null;
            return this;
        }

        throw new IllegalStateException("Cannot create the post-contingency result");
    }

    /**
     * Add a violation for the current result
     * @return this SecurityAnalysisResultBuilder instance.
     */
    public SecurityAnalysisResultBuilder addViolation(LimitViolation violation) {
        if (currentBuilder == null) {
            throw new IllegalStateException("Cannot add the violation: currentBuilder is not set");
        }

        currentBuilder.addViolation(violation);
        return this;
    }

    public SecurityAnalysisResultBuilder setComputationOk(boolean computationOk) {
        if (currentBuilder == null) {
            throw new IllegalStateException("Cannot set computation status: currentBuilder is not set");
        }

        currentBuilder.setComputationOk(computationOk);
        return this;
    }

    /**
     * Finalizes the result.
     * @return the N situation result builder
     */
    public SecurityAnalysisResult build() {
        if (currentBuilder != null) {
            throw new IllegalStateException("Cannot build the result: the currentBuilder is not terminated");
        }
        if (preContingencyResult == null) {
            throw new IllegalStateException("Pre-contingency result is not yet defined, cannot build security analysis result.");
        }

        SecurityAnalysisResult res = new SecurityAnalysisResult(preContingencyResult, postContingencyResults);
        res.setNetworkMetadata(new NetworkMetadata(context.getNetwork()));
        interceptors.forEach(i -> i.onSecurityAnalysisResult(context, res));

        return res;
    }

    /**
     * Provides access to the security analysis running context to children classes.
     * @return the security analysis running context.
     */
    protected RunningContext getContext() {
        return context;
    }

    private interface ResultBuilder {

        void setComputationOk(boolean computationOk);

        void addViolation(LimitViolation violation);

    }

    private class LimitViolationsResultBuilder implements ResultBuilder {

        protected boolean computationOk;

        protected final List<LimitViolation> violations = new ArrayList<>();

        @Override
        public void setComputationOk(boolean computationOk) {
            this.computationOk = computationOk;
        }

        @Override
        public void addViolation(LimitViolation violation) {
            violations.add(Objects.requireNonNull(violation));
        }

    }

    private class PreContingencyResultBuilder extends LimitViolationsResultBuilder {

        void endPreContingency() {
            List<LimitViolation> filteredViolations = filter.apply(violations, context.getNetwork());
            LimitViolationsResult res = new LimitViolationsResult(computationOk, filteredViolations);
            interceptors.forEach(i -> i.onPreContingencyResult(context, res));
            preContingencyResult = res;
        }
    }

    private class PostContingencyResultBuilder extends LimitViolationsResultBuilder {

        private final Contingency contingency;

        PostContingencyResultBuilder(Contingency contingency) {
            this.contingency = Objects.requireNonNull(contingency);
        }

        void endContingency() {
            List<LimitViolation> filteredViolations = filter.apply(violations, context.getNetwork());
            PostContingencyResult res = new PostContingencyResult(contingency, computationOk, filteredViolations);
            interceptors.forEach(i -> i.onPostContingencyResult(context, res));
            postContingencyResults.add(res);
        }
    }
}
