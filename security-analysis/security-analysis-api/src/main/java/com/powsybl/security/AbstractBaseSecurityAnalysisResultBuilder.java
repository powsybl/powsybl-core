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
public abstract class AbstractBaseSecurityAnalysisResultBuilder<
    C extends AbstractBaseSecurityAnalysisResultBuilder<C, D>.BasePreContingencyResultBuilder<C>,
    D extends AbstractBaseSecurityAnalysisResultBuilder<C, D>.BasePostContingencyResultBuilder<D>> {

    private final LimitViolationFilter filter;
    private final RunningContext context;
    private final List<SecurityAnalysisInterceptor> interceptors;

    // Below are volatile objects used for building the actual complete result
    private LimitViolationsResult preContingencyResult;
    private final List<PostContingencyResult> postContingencyResults = Collections.synchronizedList(new ArrayList<>());

    public AbstractBaseSecurityAnalysisResultBuilder(LimitViolationFilter filter, RunningContext context,
                                         Collection<SecurityAnalysisInterceptor> interceptors) {
        this.filter = Objects.requireNonNull(filter);
        this.context = Objects.requireNonNull(context);
        this.interceptors = ImmutableList.copyOf(interceptors);
    }

    public AbstractBaseSecurityAnalysisResultBuilder(LimitViolationFilter filter, RunningContext context) {
        this(filter, context, Collections.emptyList());
    }

    private void setPreContingencyResult(LimitViolationsResult preContingencyResult) {
        this.preContingencyResult = Objects.requireNonNull(preContingencyResult);
    }

    private void addPostContingencyResult(PostContingencyResult result) {
        postContingencyResults.add(Objects.requireNonNull(result));
    }

    /**
     * Initiates the creation of the result for N situation.
     * @return a {@link PreContingencyResultBuilder} instance.
     */
    public abstract C preContingency();

    /**
     * Initiates the creation of the result for one {@link Contingency}.
     * @param contingency  the contingency for which a result should be created
     * @return a {@link PostContingencyResultBuilder} instance.
     */
    public abstract D contingency(Contingency contingency);

    /**
     * Finalizes the result.
     * @return the N situation result builder
     */
    public SecurityAnalysisResult build() {
        if (preContingencyResult == null) {
            throw new IllegalStateException("Pre-contingency result is not yet defined, cannot build security analysis result.");
        }

        SecurityAnalysisResult res = new SecurityAnalysisResult(preContingencyResult, postContingencyResults);
        res.setNetworkMetadata(new NetworkMetadata(context.getNetwork()));
        interceptors.forEach(i -> i.onSecurityAnalysisResult(context, res));

        return res;
    }

    protected RunningContext getContext() {
        return context;
    }

    /**
     * Base class for the pre and post contingency builders.
     */
    public abstract static class AbstractLimitViolationsResultBuilder<B extends AbstractLimitViolationsResultBuilder<B>> {

        protected boolean computationOk;

        protected final List<LimitViolation> violations = new ArrayList<>();

        public B setComputationOk(boolean computationOk) {
            this.computationOk = computationOk;
            return (B) this;
        }

        public B addViolation(LimitViolation violation) {
            violations.add(Objects.requireNonNull(violation));
            return (B) this;
        }

        public B addViolations(List<LimitViolation> violations) {
            violations.forEach(this::addViolation);
            return (B) this;
        }

    }

    /**
     * Builder for the pre-contingency result
     */
    public class BasePreContingencyResultBuilder<B extends BasePreContingencyResultBuilder<B>> extends AbstractLimitViolationsResultBuilder<B> {

        protected BasePreContingencyResultBuilder() {
            // reduce visibility of constructor to protected
        }

        /**
         * Finalize the creation of the PreContingencyResult instance
         * @return the parent {@link SecurityAnalysisResultBuilder} instance.
         */
        public AbstractBaseSecurityAnalysisResultBuilder<C, D> endPreContingency() {
            List<LimitViolation> filteredViolations = filter.apply(violations, context.getNetwork());
            LimitViolationsResult res = new LimitViolationsResult(computationOk, filteredViolations);
            interceptors.forEach(i -> i.onPreContingencyResult(context, res));
            setPreContingencyResult(res);

            return AbstractBaseSecurityAnalysisResultBuilder.this;
        }
    }

    public class BasePostContingencyResultBuilder<B extends BasePostContingencyResultBuilder<B>> extends AbstractLimitViolationsResultBuilder<B> {

        private final Contingency contingency;

        protected BasePostContingencyResultBuilder(Contingency contingency) {
            this.contingency = Objects.requireNonNull(contingency);
        }

        /**
         * Finalize the creation of the PostContingencyResult instance
         * @return the parent {@link SecurityAnalysisResultBuilder} instance.
         */
        public AbstractBaseSecurityAnalysisResultBuilder<C, D> endContingency() {
            List<LimitViolation> filteredViolations = filter.apply(violations, context.getNetwork());
            PostContingencyResult res = new PostContingencyResult(contingency, computationOk, filteredViolations);
            interceptors.forEach(i -> i.onPostContingencyResult(context, res));
            addPostContingencyResult(res);

            return AbstractBaseSecurityAnalysisResultBuilder.this;
        }
    }

}
