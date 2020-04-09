/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.google.common.collect.ImmutableList;
import com.powsybl.contingency.Contingency;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;
import com.powsybl.security.interceptors.SecurityAnalysisResultContext;

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
    private final SecurityAnalysisResultContext context;
    private final List<SecurityAnalysisInterceptor> interceptors;

    // Below are volatile objects used for building the actual complete result
    private LimitViolationsResult preContingencyResult;
    private final List<PostContingencyResult> postContingencyResults = Collections.synchronizedList(new ArrayList<>());

    public SecurityAnalysisResultBuilder(LimitViolationFilter filter, SecurityAnalysisResultContext context,
                                         Collection<SecurityAnalysisInterceptor> interceptors) {
        this.filter = Objects.requireNonNull(filter);
        this.context = Objects.requireNonNull(context);
        this.interceptors = ImmutableList.copyOf(interceptors);
    }

    public SecurityAnalysisResultBuilder(LimitViolationFilter filter, SecurityAnalysisResultContext context) {
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
    public PreContingencyResultBuilder preContingency() {
        return new PreContingencyResultBuilder(context);
    }

    /**
     * Initiates the creation of the result for N situation
     * @param preContingencyResultContext the context used when create the result
     * @return a {@link PreContingencyResultBuilder} instance.
     */
    public PreContingencyResultBuilder preContingency(SecurityAnalysisResultContext preContingencyResultContext) {
        return new PreContingencyResultBuilder(preContingencyResultContext);
    }

    /**
     * Initiates the creation of the result for one {@link Contingency}.
     * @param contingency the contingency for which a result should be created
     * @return a {@link PostContingencyResultBuilder} instance.
     */
    public PostContingencyResultBuilder contingency(Contingency contingency) {
        return new PostContingencyResultBuilder(contingency, context);
    }

    /**
     * Initiates the creation of the result for one {@link Contingency}
     * @param contingency the contingency for which a result should be created
     * @param postContingencyResultContext the context used when create the result
     * @return a {@link PostContingencyResultBuilder} instance.
     */
    public PostContingencyResultBuilder contingency(Contingency contingency, SecurityAnalysisResultContext postContingencyResultContext) {
        return new PostContingencyResultBuilder(contingency, postContingencyResultContext);
    }

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
        interceptors.forEach(i -> i.onSecurityAnalysisResult(res, context));

        return res;
    }

    /**
     * Base class for the pre and post contingency builders.
     */
    public abstract class AbstractLimitViolationsResultBuilder<B extends AbstractLimitViolationsResultBuilder<B>> {

        protected boolean computationOk;

        protected final List<LimitViolation> violations = new ArrayList<>();

        protected final SecurityAnalysisResultContext resultContext;

        public B setComputationOk(boolean computationOk) {
            this.computationOk = computationOk;
            return (B) this;
        }

        /**
         * Initiates a result builder with a {@link SecurityAnalysisResultContext}.
         * @param resultContext The context would be used when creation result or as default context when a limit violation added.
         */
        private AbstractLimitViolationsResultBuilder(SecurityAnalysisResultContext resultContext) {
            this.resultContext = Objects.requireNonNull(resultContext);
        }

        /**
         * Adds a {@link LimitViolation} to the builder.
         * The default result context would be supplied to interceptors.
         * @param violation
         * @return
         */
        public B addViolation(LimitViolation violation) {
            addViolation(violation, resultContext);
            return (B) this;
        }

        /**
         * Adds a {@link LimitViolation} to the builder with a context.
         * @param violation the context would be supplied to interceptors.
         * @return
         */
        public B addViolation(LimitViolation violation, SecurityAnalysisResultContext limitViolationContext) {
            Objects.requireNonNull(limitViolationContext);
            violations.add(Objects.requireNonNull(violation));
            interceptors.forEach(i -> i.onLimitViolation(violation, limitViolationContext));
            return (B) this;
        }

        public B addViolations(List<LimitViolation> violations, SecurityAnalysisResultContext limitViolationContext) {
            Objects.requireNonNull(violations).forEach(limitViolation -> addViolation(limitViolation, limitViolationContext));
            return (B) this;
        }

        public B addViolations(List<LimitViolation> violations) {
            return addViolations(violations, resultContext);
        }

    }

    /**
     * Builder for the pre-contingency result
     */
    public class PreContingencyResultBuilder extends AbstractLimitViolationsResultBuilder<PreContingencyResultBuilder> {

        PreContingencyResultBuilder(SecurityAnalysisResultContext resultContext) {
            super(resultContext);
        }

        /**
         * Finalize the creation of the PreContingencyResult instance
         * @return the parent {@link SecurityAnalysisResultBuilder} instance.
         */
        public SecurityAnalysisResultBuilder endPreContingency() {
            List<LimitViolation> filteredViolations = filter.apply(violations, context.getNetwork());
            LimitViolationsResult res = new LimitViolationsResult(computationOk, filteredViolations);
            interceptors.forEach(i -> i.onPreContingencyResult(res, resultContext));
            setPreContingencyResult(res);

            return SecurityAnalysisResultBuilder.this;
        }
    }

    public class PostContingencyResultBuilder extends AbstractLimitViolationsResultBuilder<PostContingencyResultBuilder> {

        private final Contingency contingency;

        PostContingencyResultBuilder(Contingency contingency, SecurityAnalysisResultContext resultContext) {
            super(Objects.requireNonNull(resultContext));
            this.contingency = Objects.requireNonNull(contingency);
        }

        @Override
        public PostContingencyResultBuilder addViolation(LimitViolation violation, SecurityAnalysisResultContext limitViolationContext) {
            Objects.requireNonNull(limitViolationContext);
            violations.add(Objects.requireNonNull(violation));
            interceptors.forEach(i -> i.onLimitViolation(contingency, violation, limitViolationContext));
            return this;
        }

        /**
         * Finalize the creation of the PostContingencyResult instance
         * @return the parent {@link SecurityAnalysisResultBuilder} instance.
         */
        public SecurityAnalysisResultBuilder endContingency() {
            List<LimitViolation> filteredViolations = filter.apply(violations, context.getNetwork());
            PostContingencyResult res = new PostContingencyResult(contingency, computationOk, filteredViolations);
            interceptors.forEach(i -> i.onPostContingencyResult(res, resultContext));
            addPostContingencyResult(res);

            return SecurityAnalysisResultBuilder.this;
        }
    }
}
