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

    //Below are volatile objects used for building the actual complete result
    private LimitViolationsResult preContingencyResult;
    private List<PostContingencyResult> postContingencyResults;
    private AbstractStateResultBuilder stateBuilder;

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
     * @return             the result builder for this contingency
     */
    public PostContingencyResultBuilder contingency(Contingency contingency) {
        return new PostContingencyResultBuilder(contingency);
    }

    /**
     * Initiates the creation of the result for N situation.
     * @return the N situation result builder
     */
    public NSituationResultBuilder preContingency() {
        return new NSituationResultBuilder();
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

    public SecurityAnalysisResultBuilder startNSituation() {
        stateBuilder = new NSituationResultBuilder();
        return this;
    }

    public SecurityAnalysisResultBuilder startContingency(Contingency contingency) {
        stateBuilder = new PostContingencyResultBuilder(contingency);
        return this;
    }

    private void checkState() {
        if (stateBuilder == null) {
            throw new IllegalStateException("No state result builder is set.");
        }
    }

    public SecurityAnalysisResultBuilder addViolation(LimitViolation violation) {
        checkState();
        stateBuilder.addViolation(violation);
        return this;
    }

    public SecurityAnalysisResultBuilder computationOk(boolean ok) {
        checkState();
        stateBuilder.computationOk(ok);
        return this;
    }

    public SecurityAnalysisResultBuilder endState() {
        checkState();
        stateBuilder.add();
        stateBuilder = null;
        return this;
    }

    private abstract static class AbstractStateResultBuilder {

        protected final List<LimitViolation> violations;
        protected boolean computationOk;

        AbstractStateResultBuilder() {
            this.violations = new ArrayList<>();
            this.computationOk = false;
        }

        void addViolation(LimitViolation violation) {
            violations.add(violation);
        }

        void computationOk(boolean ok) {
            computationOk = ok;
        }

        abstract void add();
    }

    private class PostContingencyResultBuilder extends AbstractStateResultBuilder {

        private final Contingency contingency;

        PostContingencyResultBuilder(Contingency contingency) {
            super();
            this.contingency = Objects.requireNonNull(contingency);
        }

        @Override
        void add() {
            List<LimitViolation> filteredViolations = filter.apply(violations, context.getNetwork());
            PostContingencyResult res = new PostContingencyResult(contingency, computationOk, filteredViolations);
            interceptors.forEach(i -> i.onPostContingencyResult(context, res));
            postContingencyResults.add(res);
        }
    }

    private class NSituationResultBuilder extends AbstractStateResultBuilder {

        @Override
        void add() {
            List<LimitViolation> filteredViolations = filter.apply(violations, context.getNetwork());

            LimitViolationsResult res = new LimitViolationsResult(computationOk, filteredViolations);
            interceptors.forEach(i -> i.onPreContingencyResult(context, res));

            preContingencyResult = res;
        }
    }
}
