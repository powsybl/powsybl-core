/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.google.common.collect.ImmutableList;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.contingency.Contingency;
import com.powsybl.security.interceptors.ContingencyContext;
import com.powsybl.security.interceptors.RunningContext;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;
import com.powsybl.security.interceptors.ViolationContext;

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
    private final List<PostContingencyResult> postContingencyResults = Collections.synchronizedList(new ArrayList<>());

    public SecurityAnalysisResultBuilder(LimitViolationFilter filter, RunningContext context,
                                         Collection<SecurityAnalysisInterceptor> interceptors) {
        this.filter = Objects.requireNonNull(filter);
        this.context = Objects.requireNonNull(context);
        this.interceptors = ImmutableList.copyOf(interceptors);
    }

    public SecurityAnalysisResultBuilder(LimitViolationFilter filter, RunningContext context) {
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
        return new PreContingencyResultBuilder();
    }

    /**
     * Initiates the creation of the result for one {@link Contingency}.
     * @param contingency the contingency for which a result should be created
     * @return a {@link PostContingencyResultBuilder} instance.
     */
    public PostContingencyResultBuilder contingency(Contingency contingency) {
        return new PostContingencyResultBuilder(contingency);
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
        interceptors.forEach(i -> i.onSecurityAnalysisResult(context, res));

        return res;
    }

    /**
     * Base class for the pre and post contingency builders.
     */
    public abstract static class AbstractLimitViolationsResultBuilder<B extends AbstractLimitViolationsResultBuilder<B>> {

        protected boolean computationOk;

        protected final List<LimitViolation> violations = new ArrayList<>();

        protected final Map<LimitViolation, List<Extension<ViolationContext>>> extensionsByViolation = new HashMap<>();

        public B setComputationOk(boolean computationOk) {
            this.computationOk = computationOk;
            return (B) this;
        }

        public B addViolation(LimitViolation violation) {
            violations.add(Objects.requireNonNull(violation));
            extensionsByViolation.put(violation, new ArrayList<>());
            return (B) this;
        }

        public B addViolations(List<LimitViolation> violations) {
            Objects.requireNonNull(violations).forEach(this::addViolation);
            return (B) this;
        }

        /**
         * @param limitViolation if it exists already, binding extension to it. Otherwise, it would be added first.
         * @param extension
         * @return
         */
        public B addViolationContextExtension(LimitViolation limitViolation, Extension<ViolationContext> extension) {
            Objects.requireNonNull(limitViolation);
            Objects.requireNonNull(extension);
            if (!violations.contains(limitViolation)) {
                this.addViolation(limitViolation);
            }
            extensionsByViolation.computeIfAbsent(limitViolation, lv -> new ArrayList<>());
            extensionsByViolation.computeIfPresent(limitViolation, (lv, list) -> {
                list.add(extension);
                return list;
            });
            return (B) this;
        }

        private ViolationContext createViolationContextWithExtensions(RunningContext context, LimitViolation limitViolation) {
            ViolationContext violationContext = new ViolationContext(context);
            List<Extension<ViolationContext>> extensions = extensionsByViolation.get(limitViolation);
            extensions.forEach(extension -> violationContext.addExtension((Class<? super Extension<ViolationContext>>) extension.getClass(), extension));
            return violationContext;
        }

        protected List<LimitViolation> filterViolations(RunningContext context, LimitViolationFilter filter, List<SecurityAnalysisInterceptor> interceptors) {
            List<LimitViolation> filteredViolations = filter.apply(violations, context.getNetwork());
            for (LimitViolation lv : filteredViolations) {
                ViolationContext violationContextWithExtensions = createViolationContextWithExtensions(context, lv);
                interceptors.forEach(i -> i.onLimitViolation(violationContextWithExtensions, lv));
            }
            return filteredViolations;
        }
    }

    /**
     * Builder for the pre-contingency result
     */
    public class PreContingencyResultBuilder extends AbstractLimitViolationsResultBuilder<PreContingencyResultBuilder> {

        /**
         * Finalize the creation of the PreContingencyResult instance
         * @return the parent {@link SecurityAnalysisResultBuilder} instance.
         */
        public SecurityAnalysisResultBuilder endPreContingency() {
            List<LimitViolation> filteredViolations = filterViolations(context, filter, interceptors);
            LimitViolationsResult res = new LimitViolationsResult(computationOk, filteredViolations);
            interceptors.forEach(i -> i.onPreContingencyResult(context, res));
            setPreContingencyResult(res);

            return SecurityAnalysisResultBuilder.this;
        }
    }

    public class PostContingencyResultBuilder extends AbstractLimitViolationsResultBuilder<PostContingencyResultBuilder> {

        private final Contingency contingency;
        private final List<Extension<ContingencyContext>> contingencyContextExtensions;

        PostContingencyResultBuilder(Contingency contingency) {
            this.contingency = Objects.requireNonNull(contingency);
            this.contingencyContextExtensions = new ArrayList<>();
        }

        public PostContingencyResultBuilder addContingencyContextExtension(Extension<ContingencyContext> extension) {
            contingencyContextExtensions.add(Objects.requireNonNull(extension));
            return this;
        }

        public PostContingencyResultBuilder addContingencyContextExtensions(List<Extension<ContingencyContext>> extensions) {
            Objects.requireNonNull(extensions).forEach(this::addContingencyContextExtension);
            return this;
        }

        /**
         * Finalize the creation of the PostContingencyResult instance
         * @return the parent {@link SecurityAnalysisResultBuilder} instance.
         */
        public SecurityAnalysisResultBuilder endContingency() {
            List<LimitViolation> filteredViolations = filterViolations(context, filter, interceptors);
            PostContingencyResult res = new PostContingencyResult(contingency, computationOk, filteredViolations);
            ContingencyContext contingencyContext = new ContingencyContext(context);
            contingencyContextExtensions.forEach(ext -> contingencyContext.addExtension((Class<? super Extension<ContingencyContext>>) ext.getClass(), ext));
            interceptors.forEach(i -> i.onPostContingencyResult(contingencyContext, res));
            addPostContingencyResult(res);

            return SecurityAnalysisResultBuilder.this;
        }
    }
}
