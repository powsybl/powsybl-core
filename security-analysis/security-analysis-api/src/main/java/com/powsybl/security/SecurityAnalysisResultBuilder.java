/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security;

import com.google.common.collect.ImmutableList;
import com.powsybl.contingency.Contingency;
import com.powsybl.loadflow.LoadFlowResult;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;
import com.powsybl.security.interceptors.SecurityAnalysisResultContext;
import com.powsybl.security.results.*;
import com.powsybl.security.results.OperatorStrategyResult;
import com.powsybl.security.strategy.OperatorStrategy;

import java.util.*;

/**
 * Facilitates the creation of security analysis results.
 * <p>
 * Encapsulates filtering of limit violations with a provided {@link LimitViolationFilter},
 * as well as notifications to {@link SecurityAnalysisInterceptor}s.
 *
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
public class SecurityAnalysisResultBuilder {

    private final LimitViolationFilter filter;
    private final SecurityAnalysisResultContext context;
    private final List<SecurityAnalysisInterceptor> interceptors;

    // Below are volatile objects used for building the actual complete result
    private PreContingencyResult preContingencyResult;
    private final List<PostContingencyResult> postContingencyResults = Collections.synchronizedList(new ArrayList<>());
    private final List<OperatorStrategyResult> operatorStrategyResults = Collections.synchronizedList(new ArrayList<>());

    public SecurityAnalysisResultBuilder(LimitViolationFilter filter, SecurityAnalysisResultContext context,
                                         Collection<SecurityAnalysisInterceptor> interceptors) {
        this.filter = Objects.requireNonNull(filter);
        this.context = Objects.requireNonNull(context);
        this.interceptors = ImmutableList.copyOf(interceptors);
        this.preContingencyResult = new PreContingencyResult();
    }

    public SecurityAnalysisResultBuilder(LimitViolationFilter filter, SecurityAnalysisResultContext context) {
        this(filter, context, Collections.emptyList());
    }

    private void addPostContingencyResult(PostContingencyResult result) {
        postContingencyResults.add(Objects.requireNonNull(result));
    }

    /**
     * Initiates the creation of the result for N situation.
     *
     * @return a {@link PreContingencyResultBuilder} instance.
     */
    public PreContingencyResultBuilder preContingency() {
        return new PreContingencyResultBuilder(context);
    }

    /**
     * Initiates the creation of the result for N situation
     *
     * @param preContingencyResultContext the context used when create the result
     * @return a {@link PreContingencyResultBuilder} instance.
     */
    public PreContingencyResultBuilder preContingency(SecurityAnalysisResultContext preContingencyResultContext) {
        return new PreContingencyResultBuilder(preContingencyResultContext);
    }

    /**
     * Initiates the creation of the result for one {@link Contingency}.
     *
     * @param contingency the contingency for which a result should be created
     * @return a {@link PostContingencyResultBuilder} instance.
     */
    public PostContingencyResultBuilder contingency(Contingency contingency) {
        return new PostContingencyResultBuilder(contingency, context);
    }

    /**
     * Initiates the creation of the result for one {@link Contingency}
     *
     * @param contingency                  the contingency for which a result should be created
     * @param postContingencyResultContext the context used when create the result
     * @return a {@link PostContingencyResultBuilder} instance.
     */
    public PostContingencyResultBuilder contingency(Contingency contingency, SecurityAnalysisResultContext postContingencyResultContext) {
        return new PostContingencyResultBuilder(contingency, postContingencyResultContext);
    }

    /**
     * Initiates the creation of the result for one {@link OperatorStrategy}.
     *
     * @param strategy the operator strategy for which a result should be created
     * @return a {@link OperatorStrategyResultBuilder} instance.
     */
    public OperatorStrategyResultBuilder operatorStrategy(OperatorStrategy strategy) {
        return operatorStrategy(strategy, context);
    }

    /**
     * Initiates the creation of the result for one {@link OperatorStrategy}.
     *
     * @param strategy the operator strategy for which a result should be created
     * @return a {@link OperatorStrategyResultBuilder} instance.
     */
    public OperatorStrategyResultBuilder operatorStrategy(OperatorStrategy strategy, SecurityAnalysisResultContext strategyContext) {
        return new OperatorStrategyResultBuilder(strategy, strategyContext);
    }

    /**
     * Finalizes the result.
     *
     * @return the N situation result builder
     */
    public SecurityAnalysisResult build() {
        if (preContingencyResult == null) {
            throw new IllegalStateException("Pre-contingency result is not yet defined, cannot build security analysis result.");
        }

        SecurityAnalysisResult res = new SecurityAnalysisResult(preContingencyResult, postContingencyResults, operatorStrategyResults);
        res.setNetworkMetadata(new NetworkMetadata(context.getNetwork()));
        interceptors.forEach(i -> i.onSecurityAnalysisResult(res, context));

        return res;
    }

    /**
     * Base class for the pre and post contingency builders.
     */
    public abstract class AbstractLimitViolationsResultBuilder<B extends AbstractLimitViolationsResultBuilder<B>> {

        protected final List<BranchResult> branchResults = new ArrayList<>();

        protected final List<BusResult> busResults = new ArrayList<>();

        protected final List<ThreeWindingsTransformerResult> threeWindingsTransformerResults = new ArrayList<>();

        protected final List<LimitViolation> violations = new ArrayList<>();

        protected final SecurityAnalysisResultContext resultContext;

        /**
         * Initiates a result builder with a {@link SecurityAnalysisResultContext}.
         *
         * @param resultContext The context would be used when creation result or as default context when a limit violation added.
         */
        private AbstractLimitViolationsResultBuilder(SecurityAnalysisResultContext resultContext) {
            this.resultContext = Objects.requireNonNull(resultContext);
        }

        /**
         * Adds a {@link LimitViolation} to the builder.
         * The default result context would be supplied to interceptors.
         *
         * @param violation
         * @return
         */
        public B addViolation(LimitViolation violation) {
            addViolation(violation, resultContext);
            return (B) this;
        }

        /**
         * Adds a {@link LimitViolation} to the builder with a context.
         *
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

        public B addBranchResult(BranchResult branchResult) {
            this.branchResults.add(branchResult);
            return (B) this;
        }

        public B addBusResult(BusResult busResult) {
            this.busResults.add(busResult);
            return (B) this;
        }

        public B addThreeWindingsTransformerResult(ThreeWindingsTransformerResult threeWindingsTransformerResult) {
            this.threeWindingsTransformerResults.add(threeWindingsTransformerResult);
            return (B) this;
        }

    }

    /**
     * Builder for the pre-contingency result
     */
    public class PreContingencyResultBuilder extends AbstractLimitViolationsResultBuilder<PreContingencyResultBuilder> {

        private LoadFlowResult.ComponentResult.Status status = LoadFlowResult.ComponentResult.Status.CONVERGED;

        PreContingencyResultBuilder(SecurityAnalysisResultContext resultContext) {
            super(resultContext);
        }

        public PreContingencyResultBuilder setStatus(LoadFlowResult.ComponentResult.Status status) {
            this.status = status;
            return this;
        }

        /**
         * Finalize the creation of the PreContingencyResult instance
         *
         * @return the parent {@link SecurityAnalysisResultBuilder} instance.
         */
        public SecurityAnalysisResultBuilder endPreContingency() {
            List<LimitViolation> filteredViolations = filter.apply(violations, context.getNetwork());
            preContingencyResult = new PreContingencyResult(status, new LimitViolationsResult(filteredViolations), new NetworkResult(branchResults, busResults, threeWindingsTransformerResults));
            interceptors.forEach(i -> i.onPreContingencyResult(preContingencyResult, resultContext));
            return SecurityAnalysisResultBuilder.this;
        }
    }

    public class PostContingencyResultBuilder extends AbstractLimitViolationsResultBuilder<PostContingencyResultBuilder> {

        private final Contingency contingency;

        private PostContingencyComputationStatus status;

        private ConnectivityResult connectivityResult;

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

        public PostContingencyResultBuilder setStatus(PostContingencyComputationStatus status) {
            this.status = status;
            return this;
        }

        public PostContingencyResultBuilder setConnectivityResult(ConnectivityResult connectivityResult) {
            this.connectivityResult = connectivityResult;
            return this;
        }

        /**
         * Finalize the creation of the PostContingencyResult instance
         *
         * @return the parent {@link SecurityAnalysisResultBuilder} instance.
         */
        public SecurityAnalysisResultBuilder endContingency() {
            List<LimitViolation> filteredViolations = filter.apply(violations, context.getNetwork());
            PostContingencyResult res = new PostContingencyResult(contingency, status, filteredViolations,
                    branchResults, busResults, threeWindingsTransformerResults, connectivityResult);
            interceptors.forEach(i -> i.onPostContingencyResult(res, resultContext));
            addPostContingencyResult(res);

            return SecurityAnalysisResultBuilder.this;
        }
    }

    public class OperatorStrategyResultBuilder {

        private final OperatorStrategy strategy;

        private final List<OperatorStrategyResult.ConditionalActionsResult> conditionalActionsResult = Collections.synchronizedList(new ArrayList<>());

        SecurityAnalysisResultContext resultContext;

        OperatorStrategyResultBuilder(OperatorStrategy strategy, SecurityAnalysisResultContext resultContext) {
            this.strategy = Objects.requireNonNull(strategy);
            this.resultContext = resultContext;
        }

        public ConditionalActionsResultBuilder newConditionalActionsResult(String conditionalActionsId) {
            return new ConditionalActionsResultBuilder(conditionalActionsId, resultContext);
        }

        /**
         * Finalize the creation of the OperatorStrategyResult instance
         *
         * @return the parent {@link SecurityAnalysisResultBuilder} instance.
         */
        public SecurityAnalysisResultBuilder endOperatorStrategy() {
            OperatorStrategyResult res = new OperatorStrategyResult(strategy, conditionalActionsResult);
            //TODO: call to interceptors
            operatorStrategyResults.add(res);
            return SecurityAnalysisResultBuilder.this;
        }

        public class ConditionalActionsResultBuilder extends AbstractLimitViolationsResultBuilder<ConditionalActionsResultBuilder> {

            private final String conditionalActionsId;

            private PostContingencyComputationStatus status = PostContingencyComputationStatus.CONVERGED;

            ConditionalActionsResultBuilder(String conditionalActionsId, SecurityAnalysisResultContext resultContext) {
                super(Objects.requireNonNull(resultContext));
                this.conditionalActionsId = conditionalActionsId;
            }

            @Override
            public ConditionalActionsResultBuilder addViolation(LimitViolation violation, SecurityAnalysisResultContext limitViolationContext) {
                Objects.requireNonNull(limitViolationContext);
                violations.add(Objects.requireNonNull(violation));
                //TODO: call to interceptors
                return this;
            }

            public ConditionalActionsResultBuilder setStatus(PostContingencyComputationStatus status) {
                this.status = status;
                return this;
            }

            /**
             * Finalize the creation of the Conditional actions result instance
             *
             * @return the parent {@link SecurityAnalysisResultBuilder} instance.
             */
            public OperatorStrategyResultBuilder endConditionalActions() {
                List<LimitViolation> filteredViolations = filter.apply(violations, context.getNetwork());
                LimitViolationsResult limitViolationsResult = new LimitViolationsResult(filteredViolations);
                NetworkResult networkResult = new NetworkResult(branchResults, busResults, threeWindingsTransformerResults);
                conditionalActionsResult.add(new OperatorStrategyResult.ConditionalActionsResult(conditionalActionsId, status, limitViolationsResult, networkResult));
                return OperatorStrategyResultBuilder.this;
            }
        }
    }
}
